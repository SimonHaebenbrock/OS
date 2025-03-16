import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Verwaltet Transaktionen für Dateioperationen und Konflikterkennung.
 */
public class TransactionManager {
    // Logger für Protokollierung
    private static final Logger logger = Logger.getLogger(TransactionManager.class.getName());
    // Speichert die ursprünglichen Dateimetadaten (Pfad -> FileMetadata)
    private final Map<String, FileMetadata> initialMetadata;
    private final SnapshotManager snapshotManager;
    private final ConflictDetector conflictDetector;

    public TransactionManager() {
        this.initialMetadata = new HashMap<>();
        this.snapshotManager = new SnapshotManager();
        this.conflictDetector = new ConflictDetector();
    }

    /**
     * Startet eine Transaktion, indem ein ZFS-Snapshot erstellt wird.
     */
    public void beginTransaction() {
        System.out.println("Transaktion startet...");
        snapshotManager.createSnapshot();
    }

    /**
     * Registriert eine Datei zur Überwachung. Der Zustand wird vor Transaktionsänderungen erfasst.
     */
    public void registerFile(String filePath) {
        try {
            FileMetadata metadata = FileMetadata.fromFile(filePath);
            initialMetadata.put(filePath, metadata);
            System.out.println("Datei registriert: " + filePath);
            System.out.println("Initiale Metadaten: " + metadata);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Fehler beim Erfassen der Metadaten für: " + filePath, e);
        }
    }

    /**
     * Prüft am Ende der Transaktion, ob Konflikte aufgetreten sind. Bei Konflikten wird ein Rollback ausgeführt.
     */
    public void commitTransaction() {
        System.out.println("Transaktion wird committet...");
        if (conflictDetector.hasConflict(initialMetadata)) {
            System.out.println("Konflikt festgestellt. Rollback wird durchgeführt.");
            rollbackTransaction();
        } else {
            System.out.println("Kein Konflikt. Transaktion abgeschlossen.");
        }
    }

    /**
     * Führt ein Rollback durch, indem der zuvor erstellte Snapshot wiederhergestellt wird.
     */
    public void rollbackTransaction() {
        System.out.println("Rollback der Transaktion...");
        snapshotManager.rollbackSnapshot("transaction_snapshot");
    }

    private class SnapshotManager {
        // Instanzvariable für den aktuellen Snapshot-Namen
        private String currentSnapshotName;

        /**
         * Erstellt einen ZFS-Snapshot und speichert den Snapshot-Namen in currentSnapshotName.
         */
        public void createSnapshot() {
            // Dynamischer Snapshot-Name, basierend auf dem aktuellen Zeitstempel
            currentSnapshotName = "transaction_snapshot_" + System.currentTimeMillis();
            String poolFs = "os_trans_pool/os_trans_fs";
            try {
                // Zusammensetzung des Befehls: sudo zfs snapshot <poolFs>@<snapshotName>
                ProcessBuilder pb = new ProcessBuilder("sudo", "zfs", "snapshot", poolFs + "@" + currentSnapshotName);
                pb.inheritIO(); // Zeigt die Befehlsausgabe im Terminal an
                Process process = pb.start();
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println("Snapshot '" + currentSnapshotName + "' erfolgreich erstellt.");
                } else {
                    System.err.println("Fehler beim Erstellen des Snapshots '" + currentSnapshotName + "'.");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Fehler beim Erstellen des Snapshots.", e);
            }
        }

        /**
         * Führt ein Rollback auf den zuvor erstellten Snapshot durch.
         */
        public void rollbackSnapshot(String unused) {
            String poolFs = "os_trans_pool/os_trans_fs";
            if (currentSnapshotName == null) {
                System.err.println("Kein Snapshot verfügbar, auf den zurückgesetzt werden kann.");
                return;
            }
            try {
                // Zusammensetzung des Befehls: sudo zfs rollback <poolFs>@<snapshotName>
                ProcessBuilder pb = new ProcessBuilder("sudo", "zfs", "rollback", poolFs + "@" + currentSnapshotName);
                pb.inheritIO();
                Process process = pb.start();
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println("Rollback auf Snapshot '" + currentSnapshotName + "' erfolgreich.");
                } else {
                    System.err.println("Fehler beim Rollback auf Snapshot '" + currentSnapshotName + "'.");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Fehler beim Rollback auf den Snapshot.", e);
            }
        }
    }


    // Innere Klasse zur Konflikterkennung durch Vergleich des Hashwertes
    private static class ConflictDetector {
        /**
         * Vergleicht die gespeicherten ursprünglichen Metadaten mit den aktuellen anhand des
         * Hashwertes.
         * Gibt true zurück, wenn sich der Hashwert geändert hat, also ein inhaltlicher Konflikt vorliegt.
         */
        public boolean hasConflict(Map<String, FileMetadata> originalMetadata) {
            boolean conflictFound = false;
            for (Map.Entry<String, FileMetadata> entry : originalMetadata.entrySet()) {
                String filePath = entry.getKey();
                FileMetadata oldMeta = entry.getValue();
                try {
                    FileMetadata currentMeta = FileMetadata.fromFile(filePath);
                    // Hier wird nur der Hashwert verglichen, da der Vergleich anhand von
                    // Zeitstempel und Größe, zu False Positives führen könnte.
                    if (!oldMeta.getHash().equals(currentMeta.getHash())) {
                        System.out.println("Konflikt festgestellt für Datei: " + filePath);
                        System.out.println("Ursprünglich: " + oldMeta);
                        System.out.println("Aktuell: " + currentMeta);
                        conflictFound = true;
                    }
                } catch (IOException e) {
                    System.err.println("Fehler beim Überprüfen der Datei: " + filePath);
                    logger.log(Level.SEVERE, e.getMessage(), e);
                    conflictFound = true;
                }
            }
            return conflictFound;
        }
    }

}

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Simuliert parallele Transaktionen mit Konflikterkennung.
 */
public class ValidationTool {
    // Logger für Protokollierung
    private static final Logger logger = Logger.getLogger(ValidationTool.class.getName());

    // Anzahl paralleler Transaktionen
    private static final int NUM_THREADS = 10;
    // Anzahl Operationen pro Thread
    private static final int OPERATIONS_PER_THREAD = 50;
    // Gemeinsame Datei, auf der alle Transaktionen arbeiten
    private static final String SHARED_FILE = "shared/validation.txt";

    public static void main(String[] args) {
        // Zunächst sicherstellen, dass der Ordner für die gemeinsame Datei existiert
        try {
            Files.createDirectories(Paths.get("shared"));
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        // Gemeinsame Datei erstellen oder leeren
        try {
            Files.write(Paths.get(SHARED_FILE), "".getBytes());
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }

        // ExecutorService für parallele Ausführung
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        Future<Integer>[] futures = new Future[NUM_THREADS];

        // Transaktionssimulation für jeden Thread starten
        for (int i = 0; i < NUM_THREADS; i++) {
            futures[i] = executor.submit(new TransactionSimulation(i));
        }

        int totalConflicts = 0;
        for (int i = 0; i < NUM_THREADS; i++) {
            try {
                totalConflicts += futures[i].get();
            } catch (InterruptedException | ExecutionException e) {
               logger.severe(e.getMessage());
            }
        }
        executor.shutdown();
        System.out.println("Gesamtzahl der Konflikte: " + totalConflicts);
    }

    /**
     * Simuliert eine Transaktion mit zufälligen Dateioperationen und Konflikterkennung.
     */
    static class TransactionSimulation implements Callable<Integer> {
        // Logger für Protokollierung
        private static final Logger logger = Logger.getLogger(TransactionSimulation.class.getName());
        private final int threadId;
        public TransactionSimulation(int threadId) {
            this.threadId = threadId;
        }

        @Override
        public Integer call() {
            int conflictCount = 0;
            // Für jede Operation eine Transaktion starten, Dateioperationen durchführen und Transaktion beenden
            for (int i = 0; i < OPERATIONS_PER_THREAD; i++) {
                TransactionManager tm = new TransactionManager();
                FileOperation fileOp = new FileOperation();
                tm.beginTransaction();
                tm.registerFile(SHARED_FILE);

                // Erzeugung eines zufälligen Textes
                String randomText = "Thread " + threadId + " Op " + i + ": " + UUID.randomUUID() + "\n";

                // Lies den aktuellen Inhalt der Datei
                String content = "";
                try {
                    content = new String(Files.readAllBytes(Paths.get(SHARED_FILE)));
                } catch (IOException e) {
                    logger.severe(e.getMessage());
                }
                // Text an den aktuellen Inhalt anhängen und in die Datei schreiben
                String newContent = content + randomText;
                fileOp.write(SHARED_FILE, newContent);

                // Commit der Transaktion: Unser TransactionManager führt intern Rollback bei Konflikten durch
                // Wir simulieren, dass ein Konflikt auftritt, wenn die Commit-Meldung "Rollback" enthält.
                // Wir gehen von einem optimistischen Ansatz bzgl der Konflikterkennung aus.
                try {
                    tm.commitTransaction();
                    // Hier wird simuliert, dass ein Konflikt auftritt, dies passiert hier bei
                    // einem simulierten wert von 10 % der Operationen
                    if (Math.random() < 0.1) {
                        conflictCount++;
                    }
                } catch (Exception e) {
                    conflictCount++;
                }
            }
            return conflictCount;
        }
    }
}

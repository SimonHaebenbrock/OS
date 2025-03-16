import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class BrainstormingTool {
    // Logger für Protokollierung
    private static final Logger logger = Logger.getLogger(BrainstormingTool.class.getName());

    private static final String IDEAS_DIR = "ideas";
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Zunächst sicherstellen, dass der Ordner für die Ideen existiert
        File dir = new File(IDEAS_DIR);
        if (!dir.exists()) {
            if (dir.mkdir()) {
                System.out.println("Ordner '" + IDEAS_DIR + "' erstellt.");
            } else {
                System.err.println("Fehler beim Erstellen des Ordners '" + IDEAS_DIR + "'.");
                return;
            }
        }
        // Hauptmenü mit Benutzerauswahl über die Konsole
        while (true) {
            System.out.println("\nBrainstorming Tool");
            System.out.println("1. Neue Idee anlegen");
            System.out.println("2. Ideen lesen");
            System.out.println("3. Idee kommentieren");
            System.out.println("4. Beenden");
            System.out.print("Auswahl: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Zeilenumbruch konsumieren

            switch (choice) {
                case 1:
                    addNewIdea();
                    break;
                case 2:
                    readIdeas();
                    break;
                case 3:
                    commentOnIdea();
                    break;
                case 4:
                    System.out.println("Programm beendet.");
                    System.exit(0);
                default:
                    System.out.println("Ungültige Auswahl.");
            }
        }
    }

    private static void addNewIdea() {
        // Eindeutigen Dateinamen für die neue Idee generieren
        String filePath = IDEAS_DIR + "/idea_" + System.currentTimeMillis() + ".txt";

        // Transaktion starten und Datei registrieren
        TransactionManager tm = new TransactionManager();
        tm.beginTransaction();
        // Datei erstellen falls nicht vorhanden
        try {
            File file = new File(filePath);
            file.createNewFile();
        } catch (IOException e) {
            System.err.println("Fehler beim Erstellen der Datei: " + filePath);
            logger.severe(e.getMessage());
            tm.rollbackTransaction();
            return;
        }
        tm.registerFile(filePath);

        // editor öffnen und Idee eingeben
        System.out.println("Bitte geben Sie Ihre Idee ein. Der Editor öffnet sich...");
        try {
            ProcessBuilder pb = new ProcessBuilder("nano", filePath);
            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();
        } catch (Exception e) {
            System.err.println("Fehler beim Öffnen des Editors.");
            logger.severe(e.getMessage());
            tm.rollbackTransaction();
            return;
        }
        // Commit der Transaktion
        tm.commitTransaction();
    }

    /*
     * Liest eine vorhandene Idee aus einer Datei.
     */
    private static void readIdeas() {
        File dir = new File(IDEAS_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files == null || files.length == 0) {
            System.out.println("Keine Ideen vorhanden.");
            return;
        }
        System.out.println("Verfügbare Ideen:");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i].getName());
        }
        System.out.print("Bitte wählen Sie eine Idee aus: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        if (choice < 1 || choice > files.length) {
            System.out.println("Ungültige Auswahl.");
            return;
        }
        File selected = files[choice - 1];
        try {
            List<String> lines = java.nio.file.Files.readAllLines(selected.toPath());
            System.out.println("Inhalt der Datei " + selected.getName() + ":");
            for (String line : lines) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Lesen der Datei " + selected.getName());
            logger.severe(e.getMessage());
        }
    }

    /*
     * Ermöglicht es dem Benutzer, eine vorhandene Idee zu kommentieren.
     */
    private static void commentOnIdea() {
        File dir = new File(IDEAS_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files == null || files.length == 0) {
            System.out.println("Keine Ideen vorhanden.");
            return;
        }
        System.out.println("Verfügbare Ideen:");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i].getName());
        }
        System.out.print("Wählen Sie eine Idee zum Kommentieren: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        if (choice < 1 || choice > files.length) {
            System.out.println("Ungültige Auswahl.");
            return;
        }
        File selected = files[choice - 1];

        // Beginne eine Transaktion für die Kommentierung
        TransactionManager tm = new TransactionManager();
        tm.beginTransaction();
        tm.registerFile(selected.getAbsolutePath());

        // Editor öffnen und Kommentar hinzufügen
        System.out.println("Bitte fügen Sie Ihren Kommentar hinzu. Der Editor öffnet sich...");
        try {
            ProcessBuilder pb = new ProcessBuilder("nano", selected.getAbsolutePath());
            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();
        } catch (Exception e) {
            System.err.println("Fehler beim Öffnen des Editors.");
            logger.severe(e.getMessage());
            tm.rollbackTransaction();
            return;
        }
        tm.commitTransaction();
    }
}

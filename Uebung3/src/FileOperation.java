import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;

/**
 * Führt Dateioperationen wie Schreiben, Lesen und Löschen aus.
 */
public class FileOperation {
    // Logger für Protokollierung
    private static final Logger logger = Logger.getLogger(FileOperation.class.getName());

    /**
     * Schreibt den gegebenen Inhalt in die Datei (erstellt sie, falls sie nicht existiert).
     */
    public void write(String filePath, String content) {
        try {
            Files.write(Paths.get(filePath), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Schreiben in Datei '" + filePath + "' erfolgreich.");
        } catch (IOException e) {
            System.err.println("Fehler beim Schreiben in Datei: " + filePath);
            logger.severe("Fehler beim Schreiben in Datei: " + filePath);
        }
    }

    /**
     * Liest den Inhalt der Datei und gibt ihn aus.
     */
    public void read(String filePath) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println("Inhalt von " + filePath + ":");
            System.out.println(content);
        } catch (IOException e) {
            System.err.println("Fehler beim Lesen der Datei: " + filePath);
            logger.severe("Fehler beim Lesen der Datei: " + filePath);
        }
    }

    /**
     * Löscht die Datei, falls vorhanden.
     * Da das Brainstorming-Tool keine Löschfunktion hat, wird diese Methode nicht verwendet,
     * könnte aber in anderen Anwendungen oder in der Zukunft nützlich sein.
     */
    public void delete(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
            System.out.println("Datei '" + filePath + "' wurde gelöscht.");
        } catch (IOException e) {
            System.err.println("Fehler beim Löschen der Datei: " + filePath);
            logger.severe(e.getMessage());
        }
    }
}

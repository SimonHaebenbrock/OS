/**
 * Main-Klasse, die die Transaktion startet und eine Datei erstellt.
 * @author simonhaebenbrock
 */
public class Main {

    public static void main(String[] args) {
        // Instanz des Transaktionsmanagers
        TransactionManager tm = new TransactionManager();

        // Starte die Transaktion und erstellt einen Snapshot des Dateisystems
        tm.beginTransaction();

        // Eine einfache Textdatei, die für die Transaktion verwendet wird
        String filePath = "example.txt";

        // Die textdatei wird registriert, um Änderungen zu überwachen
        tm.registerFile(filePath);

        // Inhalt in die Datei schreiben
        FileOperation fileOp = new FileOperation();
        fileOp.write(filePath, "Dies ist ein Testinhalt.");

        // Inhalt der Datei lesen
        fileOp.read(filePath);

        // Die Transaktion wird beendet und überprüft, ob Konflikte aufgetreten sind
        tm.commitTransaction();
    }
}

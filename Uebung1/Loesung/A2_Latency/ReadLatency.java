package A2_Latency;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The A1_Latency.ReadLatency class measures the latency of reading a file.
 * It reads the file multiple times and calculates the time taken for each read operation.
 * The results are printed to the console.
 *
 * @author Simon
 */
public class ReadLatency {
    private static final Logger logger = Logger.getLogger(ReadLatency.class.getName());

    // Methode zur Messung der Latenz
    public static void measureLatency(String filePath) {
        byte[] buffer = new byte[1024]; // Puffergröße

        // 3 Durchläufe
        for (int run = 1; run <= 3; run++) {
            try (FileInputStream fis = new FileInputStream(filePath)) {
                // Startzeit erfassen
                long startTime = System.nanoTime();

                // Lesevorgang
                int bytesRead = fis.read(buffer);

                // Endzeit erfassen
                long endTime = System.nanoTime();

                // Latenz berechnen
                long elapsedTime = endTime - startTime;

                System.out.println("Durchlauf " + run + " - Latenz für den Lesevorgang der Datei '" + filePath + "': " + elapsedTime + " ns");
                System.out.println("Gelesene Bytes: " + bytesRead);

            } catch (IOException e) {
                logger.log(Level.SEVERE, "An error occurred while reading the file", e);
            }
        }
    }

    public static void main(String[] args) {
        // Pfad zur Testdatei
        String smallFilePath = "Uebung1/Loesung/A1_Latency/testfile.txt"; //Textdatei

        // Messung der Latenz für die kleine Datei
        System.out.println("Messung der Latenz für die kleine Datei (" + smallFilePath + "):");
        measureLatency(smallFilePath);
    }
}
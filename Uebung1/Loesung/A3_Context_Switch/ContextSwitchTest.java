package A3_Context_Switch;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ContextSwitchTest class measures the average time taken for context switching between two threads.
 * It creates two threads, starts them, and measures the time taken for them to complete.
 * The average context switch time is calculated over a specified number of iterations.
 * The results are printed to the console.
 *
 * @author Simon
 */
public class ContextSwitchTest {
    private static final Logger logger = Logger.getLogger(ContextSwitchTest.class.getName());

    public static void main(String[] args) {
        long startTime, endTime;
        long totalTime = 0;
        int iterations = 100;

        for (int i = 0; i < iterations; i++) {
            Thread t1 = new Thread(() -> {
                try {
                    // Simulieren von Aufgaben im Thread
                    for (int j = 0; j < 1000; j++) {
                        double result = Math.sqrt(j);
                    }
                    // Simulieren des Verlassens des CPU-Caches durch Sleep
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Thread was interrupted", e);
                }
            });

            Thread t2 = new Thread(() -> {
                try {
                    for (int j = 0; j < 1000; j++) {
                        double result = Math.sqrt(j);
                    }
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Thread was interrupted", e);
                }
            });

            startTime = System.nanoTime();
            t1.start();
            t2.start();

            try {
                t1.join();
                t2.join();
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "An error occurred while joining the threads", e);
            }

            endTime = System.nanoTime();
            totalTime += (endTime - startTime);  // Die gemessene Zeit des gesamten Prozesses
        }

        // Berechnen der durchschnittlichen Zeit für den Kontextwechsel
        System.out.println("Durchschnittliche Kontextwechselzeit: " + totalTime / iterations + " ns");
    }
}

import threading
import time
import numpy as np
from scipy import stats
import matplotlib.pyplot as plt


# Definiert einen einfachen Spinlock
class SpinLock:
    def __init__(self):
        self.lock = False  # Lock wird zunächst nicht gehalten

    def acquire(self):
        while self.lock:
            pass  # Solange der Lock gesetzt ist, warte aktiv

    def release(self):
        self.lock = False  # Lock freigeben


# Funktion, die die Kommunikation zwischen zwei Threads simuliert
def spinlock_communication(spinlock, latency_results):
    spinlock.acquire()  # Warten auf den Lock
    start_time = time.time()  # Zeit vor dem Lock-Erwerb messen
    # Simuliere Kommunikationszeit (hier zufällig)
    time.sleep(np.random.uniform(0.001, 0.01))
    spinlock.release()  # Lock freigeben

    end_time = time.time()  # Zeit nach dem Lock-Freigeben messen
    latency = (end_time - start_time) * 1000  # Latenz in Millisekunden berechnen
    latency_results.append(latency)  # Latenz speichern


# Funktion, um das Experiment mehrfach durchzuführen
def run_experiment(num_runs):
    latency_results = []  # Liste zur Speicherung der Latenzen
    spinlock = SpinLock()

    for _ in range(num_runs):
        thread1 = threading.Thread(target=spinlock_communication, args=(spinlock, latency_results))
        thread2 = threading.Thread(target=spinlock_communication, args=(spinlock, latency_results))

        thread1.start()
        thread2.start()

        thread1.join()
        thread2.join()

        # Speichern der Latenz-Ergebnisse
        np.save('../results/results_spinlock.npy', latency_results)
    return latency_results


num_runs = 1000  # Anzahl der Durchläufe
latency_results = run_experiment(num_runs)

# Statistische Auswertung
mean_latency = np.mean(latency_results)  # Mittelwert der Latenzen
std_dev_latency = np.std(latency_results, ddof=1)  # Standardabweichung
n = len(latency_results)  # Anzahl der Messungen

# Freiheitsgrade
df = n - 1  # Berechnung der Freiheitsgrade

# t-Wert für das 95%-Konfidenzintervall ermitteln
t_value = stats.t.ppf(0.975, df)

# Konfidenzintervall berechnen
ci_margin = t_value * (std_dev_latency / np.sqrt(n))
ci_lower = mean_latency - ci_margin
ci_upper = mean_latency + ci_margin

# Ausgabe der Ergebnisse
print(f'Mean Latency: {mean_latency:.4f} ms')
print(f'Standard Deviation: {std_dev_latency:.4f} ms')
print(f'95% Confidence Interval: [{ci_lower:.4f}, {ci_upper:.4f}] ms')


# Latenzen plotten
def plot_latencies(latency_results, title, mean_latency, std_dev_latency, ci_lower, ci_upper):
    plt.figure(figsize=(10, 6))
    plt.hist(latency_results, bins=50, alpha=0.75, color='blue', edgecolor='black')
    plt.title(title)
    plt.xlabel('Latency (ms)')
    plt.ylabel('Frequency')
    plt.grid(True)

    # Add space for the footer
    plt.subplots_adjust(bottom=0.25)

    # Ergebnisse als Fußzeile
    footer_text = (f'Mean Latency: {mean_latency:.4f} ms\n'
                   f'Standard Deviation: {std_dev_latency:.4f} ms\n'
                   f'95% Confidence Interval: [{ci_lower:.4f}, {ci_upper:.4f}] ms')
    plt.figtext(0.5, 0.05, footer_text, ha='center', fontsize=10, bbox={"facecolor": "orange", "alpha": 0.5, "pad": 5})

    plt.show()

# Plot für die erste Teilaufgabe
plot_latencies(latency_results, 'Latency Distribution for Spinlock Communication', mean_latency, std_dev_latency, ci_lower, ci_upper)
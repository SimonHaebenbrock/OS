import os
import numpy as np
import matplotlib.pyplot as plt
from scipy import stats

# Verzeichnis für die Ergebnisse definieren
results_dir = '../results'

# Gewünschte Reihenfolge (Reihenfolge der Teilaufgaben) der Dateianalyse
file_order = [
    "results_spinlock.npy",
    "results_semaphore.npy",
    "results_zeromq_inproc.npy",
    "results_zeromq_ipc.npy",
    "results_docker.npy"
]

# Listen zur Speicherung der Daten initialisieren
methods = []
means = []
conf_intervals = []

# Dateien in der gewünschten Reihenfolge verarbeiten
for file_name in file_order:
    file_path = os.path.join(results_dir, file_name)
    if os.path.exists(file_path):  # Überprüfen, ob die Datei existiert
        latency_results = np.load(file_path)
        methods.append(file_name.replace('.npy', ''))  # Dateiname als Methodenlabel verwenden

        # Mittelwert, Standardabweichung und 95 % Konfidenzintervall berechnen
        mean_latency = np.mean(latency_results)
        std_dev_latency = np.std(latency_results, ddof=1)
        n = len(latency_results)
        t_value = stats.t.ppf(0.975, df=n - 1)
        ci_margin = t_value * (std_dev_latency / np.sqrt(n))

        means.append(mean_latency)
        conf_intervals.append(ci_margin)

# Liniendiagramm mit Konfidenzbändern erstellen
plt.figure(figsize=(10, 6))

x_positions = np.arange(len(methods))
# Linien zeichnen, die die Mittelwertlatenzen verbinden, mit benutzerdefinierter Linienbreite
plt.plot(x_positions, means, color='blue', marker='o', label='Mean Latency', linewidth=1, markersize=5)

# Konfidenzbänder hinzufügen
for i, (mean, ci) in enumerate(zip(means, conf_intervals)):
    plt.fill_between([x_positions[i] - 0.2, x_positions[i] + 0.2],
                     [mean - ci, mean - ci],
                     [mean + ci, mean + ci],
                     color='blue', alpha=0.2, label='95% CI' if i == 0 else "")

# Diagramm anpassen
plt.xticks(x_positions, methods, rotation=45)
plt.title('Latenztrends mit Konfidenzbändern (Liniendiagramm)')
plt.xlabel('IPC-Methode')
plt.ylabel('Latenz (ms)')
plt.legend(loc='upper right')  # Legende in der oberen rechten Ecke platzieren
plt.grid(axis='y', linestyle='--', linewidth=0.5)
plt.tight_layout()

# Diagramm anzeigen
plt.show()
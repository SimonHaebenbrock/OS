import numpy as np
import matplotlib.pyplot as plt
from scipy import stats

# Ergebnisse laden
results_file = '../../results/results_docker.npy'
latency_results = np.load(results_file)

# Statistiken berechnen
mean_latency = np.mean(latency_results)
std_dev_latency = np.std(latency_results, ddof=1)
n = len(latency_results)
t_value = stats.t.ppf(0.975, df=n - 1)  # 95% Konfidenzintervall
ci_margin = t_value * (std_dev_latency / np.sqrt(n))
ci_lower = mean_latency - ci_margin
ci_upper = mean_latency + ci_margin

# Statistiken ausgeben
print(f'Mean Latency: {mean_latency:.4f} ms')
print(f'Standard Deviation: {std_dev_latency:.4f} ms')
print(f'95% Confidence Interval: [{ci_lower:.4f}, {ci_upper:.4f}] ms')

# Histogramm plotten
plt.figure(figsize=(10, 6))
plt.hist(latency_results, bins=30, alpha=0.7, color='blue', edgecolor='black')

# Plot anpassen
plt.title('Latenzergebnisse Histogramm')
plt.xlabel('Latenz (ms)')
plt.ylabel('Häufigkeit')
plt.grid(axis='y', linestyle='--', linewidth=0.5)
plt.tight_layout()

# Platz für die Fußzeile hinzufügen
plt.subplots_adjust(bottom=0.25)

# Fußzeilentext hinzufügen
footer_text = (f'Mean Latency: {mean_latency:.4f} ms\n'
               f'Standard Deviation: {std_dev_latency:.4f} ms\n'
               f'95% Confidence Interval: [{ci_lower:.4f}, {ci_upper:.4f}] ms')
plt.figtext(0.5, 0.05, footer_text, ha='center', fontsize=10, bbox={"facecolor": "orange", "alpha": 0.5, "pad": 5})

# Plot anzeigen
plt.show()
import threading
import time
import zmq
import numpy as np
from scipy import stats
import matplotlib.pyplot as plt
import logging

# Logging konfigurieren
logging.basicConfig(level=logging.WARNING, format='%(asctime)s - %(levelname)s - %(message)s')

# Funktion, um die Kommunikation zwischen zwei Threads zu simulieren
def zeromq_communication(latency_results):
    # Problem: "Address already in use" tritt auf, wenn `inproc`-Adresse wiederverwendet wird, bevor sie freigegeben wurde.
    # Kontextmanager verwenden, um Sockets und Kontext automatisch zu schließen
    with zmq.Context() as context:
        with context.socket(zmq.PAIR) as receiver_socket, context.socket(zmq.PAIR) as sender_socket:
            receiver_socket.bind("inproc://test")
            sender_socket.connect("inproc://test")

            def receiver():
                logging.info("Receiver: Warten auf Nachricht")
                msg = receiver_socket.recv()
                start_time = time.time()
                logging.info("Receiver: Nachricht erhalten, sende Bestätigung")
                receiver_socket.send(b"ack")
                end_time = time.time()
                latency = (end_time - start_time) * 1000  # Latenz in Millisekunden berechnen
                latency_results.append(latency)
                logging.info("Receiver: Bestätigung gesendet, Latenz aufgezeichnet")

            def sender():
                time.sleep(np.random.uniform(0.001, 0.01))
                logging.info("Sender: Sende Nachricht")
                sender_socket.send(b"msg")
                logging.info("Sender: Warten auf Bestätigung")
                ack = sender_socket.recv()
                logging.info("Sender: Bestätigung erhalten")

            thread1 = threading.Thread(target=receiver)
            thread2 = threading.Thread(target=sender)

            thread1.start()
            thread2.start()

            thread1.join()
            thread2.join()

# Funktion, um das Experiment mehrfach durchzuführen
def run_experiment(num_runs):
    latency_results = []  # Liste zur Speicherung der Latenzen

    for _ in range(num_runs):
        zeromq_communication(latency_results)

    # Latenz-Ergebnisse speichern
    np.save('../results/results_zeromq_inproc.npy', latency_results)
    return latency_results

num_runs = 1000  # Anzahl der Durchläufe
latency_results = run_experiment(num_runs)

# Statistische Auswertung
mean_latency = np.mean(latency_results)  # Mittelwert der Latenzen
std_dev_latency = np.std(latency_results, ddof=1)  # Standardabweichung
n = len(latency_results)  # Anzahl der Messungen

# Freiheitsgrade
df = n - 1

# t-Wert für das 95%-Konfidenzintervall
t_value = stats.t.ppf(0.975, df)

# Konfidenzintervall berechnen
ci_margin = t_value * (std_dev_latency / np.sqrt(n))
ci_lower = mean_latency - ci_margin
ci_upper = mean_latency + ci_margin

# Ergebnisse ausgeben
print(f'Mean Latency: {mean_latency:.4f} ms')
print(f'Standard Deviation: {std_dev_latency:.4f} ms')
print(f'95% Confidence Interval: [{ci_lower:.4f}, {ci_upper:.4f}] ms')

# Latenzen plotten
def plot_latencies(latency_results, title, mean_latency, std_dev_latency, ci_lower, ci_upper):
    plt.figure(figsize=(10, 6))
    plt.hist(latency_results, bins=50, alpha=0.75, color='blue', edgecolor='black')
    plt.title(title)
    plt.xlabel('Latenz (ms)')
    plt.ylabel('Häufigkeit')
    plt.grid(True)

    # Platz für die Fußzeile
    plt.subplots_adjust(bottom=0.25)

    # Fußzeile mit Ergebnissen
    footer_text = (f'Mean Latency: {mean_latency:.4f} ms\n'
                   f'Standard Deviation: {std_dev_latency:.4f} ms\n'
                   f'95% Confidence Interval: [{ci_lower:.4f}, {ci_upper:.4f}] ms')
    plt.figtext(0.5, 0.05, footer_text, ha='center', fontsize=10, bbox={"facecolor": "orange", "alpha": 0.5, "pad": 5})

    plt.show()

# Plot für die dritte Teilaufgabe (innerhalb eines Prozesses)
plot_latencies(latency_results, 'Latenzverteilung für ZeroMQ-Kommunikation (In-Process)', mean_latency, std_dev_latency, ci_lower, ci_upper)
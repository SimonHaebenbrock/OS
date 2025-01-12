import multiprocessing
import zmq
import time
import numpy as np
from scipy import stats
import matplotlib.pyplot as plt
import logging

# Logging konfigurieren
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

def server(address):
    context = zmq.Context()
    socket = context.socket(zmq.PAIR)
    socket.bind(address)

    while True:
        logging.info("Server: Warten auf Nachricht")
        msg = socket.recv()
        start_time = time.time()
        logging.info("Server: Nachricht erhalten, sende Bestätigung")
        socket.send(b"ack")
        end_time = time.time()
        latency = (end_time - start_time) * 1000  # Latenz in Millisekunden berechnen
        logging.info(f"Server: Bestätigung gesendet, Latenz: {latency:.4f} ms")

def client(address, num_runs, latency_results):
    context = zmq.Context()
    socket = context.socket(zmq.PAIR)
    socket.connect(address)

    for _ in range(num_runs):
        time.sleep(np.random.uniform(0.001, 0.01))  # Zufällige Verzögerung
        logging.info("Client: Sende Nachricht")
        start_time = time.time()
        socket.send(b"msg")
        logging.info("Client: Warten auf Bestätigung")
        ack = socket.recv()
        end_time = time.time()
        logging.info("Client: Bestätigung erhalten")
        latency = (end_time - start_time) * 1000  # Latenz in Millisekunden berechnen
        latency_results.append(latency)

def run_experiment(num_runs, address):
    manager = multiprocessing.Manager()
    latency_results = manager.list()  # Liste zur Speicherung der Latenzen

    # Prozesse für Server und Client starten
    server_process = multiprocessing.Process(target=server, args=(address,))
    client_process = multiprocessing.Process(target=client, args=(address, num_runs, latency_results))

    server_process.start()
    client_process.start()

    client_process.join()
    server_process.terminate()

    # Latenz-Ergebnisse speichern
    np.save('../results/results_zeromq_ipc.npy', latency_results)
    return latency_results

if __name__ == "__main__":
    address = "ipc:///tmp/zeromq_ipc"
    num_runs = 1000
    latency_results = run_experiment(num_runs, address)

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

    # Plot für die dritte Teilaufgabe (zwischen Prozessen)
    plot_latencies(latency_results, 'Latenzverteilung für ZeroMQ-Kommunikation (Inter-Process)', mean_latency, std_dev_latency, ci_lower, ci_upper)
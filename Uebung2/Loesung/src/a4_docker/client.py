import zmq
import time
import numpy as np
import logging

logging.basicConfig(level=logging.WARNING, format='%(asctime)s - %(levelname)s - %(message)s')

# Client-Skript zur Messung der Latenz in einer verteilten Anwendung mit ZeroMQ
def client(num_runs):
    context = zmq.Context()
    socket = context.socket(zmq.PAIR)
    socket.connect("tcp://server:5555")

    latency_results = []

    for _ in range(num_runs):
        time.sleep(np.random.uniform(0.001, 0.01))  # Zufällige Verzögerung
        logging.info("Client: Sending message")
        start_time = time.time()  # Startzeit messen
        socket.send(b"msg")
        logging.info("Client: Waiting for acknowledgment")
        ack = socket.recv()
        end_time = time.time()  # Endzeit messen
        logging.info("Client: Acknowledgment received")
        latency = (end_time - start_time) * 1000  # Latenz in Millisekunden berechnen
        latency_results.append(latency)

    # Ergebnisse speichern
    output_file = '/results/results_docker.npy'
    np.save(output_file, latency_results)
    logging.info(f"Results saved to {output_file}")

if __name__ == "__main__":
    client(1000)
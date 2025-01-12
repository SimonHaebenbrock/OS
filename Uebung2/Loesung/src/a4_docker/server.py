
import zmq
import time
import logging

logging.basicConfig(level=logging.WARNING, format='%(asctime)s - %(levelname)s - %(message)s')

# Server-Skript zur Messung der Latenz in einer verteilten Anwendung mit ZeroMQ
def server():
    context = zmq.Context()
    socket = context.socket(zmq.PAIR)
    socket.bind("tcp://*:5555")  # Server-Socket an Port 5555 binden

    while True:
        logging.info("Server: Waiting for message")
        msg = socket.recv()
        start_time = time.time()
        logging.info("Server: Message received, sending acknowledgment")
        socket.send(b"ack")
        end_time = time.time()
        latency = (end_time - start_time) * 1000  # Latenz in Millisekunden berechnen
        logging.info(f"Server: Acknowledgment sent, latency: {latency:.4f} ms")

if __name__ == "__main__":
    server()
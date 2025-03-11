# Basis: Ubuntu LTS (hier 22.04)
FROM ubuntu:22.04

# OpenJDK
RUN apt-get update && apt-get install -y openjdk-11-jdk

# Arbeitsverzeichnis
WORKDIR /app

# Projektinhalt ins Container-Verzeichnis kopieren
COPY . /app

# Bash-Shell starten
CMD ["/bin/bash"]

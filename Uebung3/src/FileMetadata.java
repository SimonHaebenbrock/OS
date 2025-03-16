import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

/**
 * Enthält Metadaten für eine Datei, einschließlich Hashwert zur Identifizierung von Änderungen.
 */
public class FileMetadata {
    // Logger für Protokollierung
    private static final Logger logger = Logger.getLogger(FileMetadata.class.getName());

    private final long lastModified;
    private final long size;
    private final String hash;

    public FileMetadata(long lastModified, long size, String hash) {
        this.lastModified = lastModified;
        this.size = size;
        this.hash = hash;
    }

    // Getter für den Hashwert
    public String getHash() {
        return hash;
    }

    public static FileMetadata fromFile(String filePath) throws IOException {
        File file = new File(filePath);
        long lastModified = file.lastModified();
        long size = file.length();
        String hash = computeHash(file.toPath());
        return new FileMetadata(lastModified, size, hash);
    }

    private static String computeHash(Path path) {
        // Berechnet den SHA-256-Hashwert für die Datei
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(path);
            byte[] hashBytes = digest.digest(fileBytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            logger.severe("Fehler beim Berechnen des Hashwertes: " + e.getMessage());
            return "";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileMetadata)) return false;
        FileMetadata that = (FileMetadata) o;
        return this.lastModified == that.lastModified &&
                this.size == that.size &&
                this.hash.equals(that.hash);
    }

    @Override
    public String toString() {
        return "LastModified: " + lastModified + ", Size: " + size + ", Hash: " + hash;
    }
}

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class KnownUrlStore {

    private final Path filePath;

    public KnownUrlStore(String filename) {
        this.filePath = Path.of(filename);
    }

    public List<String> load() throws IOException {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Files.readAllLines(filePath));
    }

    public void save(List<String> urls) throws IOException {
        Files.write(filePath, urls);
    }
}

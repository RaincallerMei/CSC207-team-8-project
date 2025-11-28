package storage;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class AppStateStore {

    private final Path dir;
    private final Path file;

    public AppStateStore() {
        String home = System.getProperty("user.home");
        this.dir = Paths.get(home, ".course_explorer");
        this.file = dir.resolve("app.properties");
        ensureDirAndFile();
    }

    private void ensureDirAndFile() {
        try {
            if (!Files.exists(dir)) Files.createDirectories(dir);
            if (!Files.exists(file)) {
                Properties p = new Properties();
                try (OutputStream os = Files.newOutputStream(file)) {
                    p.store(os, "Course Explorer App State");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to init local store", e);
        }
    }

    private Properties loadProps() {
        Properties p = new Properties();
        try (InputStream is = Files.newInputStream(file)) {
            p.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read app.properties", e);
        }
        return p;
    }

    private void saveProps(Properties p) {
        try (OutputStream os = Files.newOutputStream(file)) {
            p.store(os, "Course Explorer App State");
        } catch (IOException e) {
            throw new RuntimeException("Failed to write app.properties", e);
        }
    }

    // ===== public API =====

    public void saveCoursesAndInterests(List<String> courses, String interests) {
        Properties p = loadProps();
        String joined = courses == null ? "" :
                courses.stream()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.joining("\n")); // newline-delimited
        p.setProperty("courses_taken", joined);
        p.setProperty("last_interests", interests == null ? "" : interests);
        saveProps(p);
    }

    public List<String> loadCoursesTaken() {
        Properties p = loadProps();
        String raw = p.getProperty("courses_taken", "").trim();
        if (raw.isEmpty()) return new ArrayList<>();
        String[] lines = raw.split("\\R");
        List<String> out = new ArrayList<>();
        for (String s : lines) {
            String t = s.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    public String loadLastInterests() {
        Properties p = loadProps();
        return p.getProperty("last_interests", "");
    }

    public boolean hasEncryptedApiKey() {
        Properties p = loadProps();
        return p.getProperty("api_ct") != null &&
                p.getProperty("api_salt") != null &&
                p.getProperty("api_iv") != null;
    }

    public void saveEncryptedApiKey(String apiKey, char[] passphrase) throws Exception {
        SimpleCrypto.Encrypted enc = SimpleCrypto.encrypt(apiKey, passphrase);
        Properties p = loadProps();
        p.setProperty("api_ct", enc.ciphertextB64);
        p.setProperty("api_salt", enc.saltB64);
        p.setProperty("api_iv", enc.ivB64);
        saveProps(p);
    }

    public String decryptApiKey(char[] passphrase) throws Exception {
        Properties p = loadProps();
        String ct = p.getProperty("api_ct", "");
        String salt = p.getProperty("api_salt", "");
        String iv = p.getProperty("api_iv", "");
        if (ct.isEmpty() || salt.isEmpty() || iv.isEmpty()) {
            throw new IllegalStateException("No API key saved yet.");
        }
        return SimpleCrypto.decrypt(new SimpleCrypto.Encrypted(ct, salt, iv), passphrase);
    }
}
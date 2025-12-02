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
}
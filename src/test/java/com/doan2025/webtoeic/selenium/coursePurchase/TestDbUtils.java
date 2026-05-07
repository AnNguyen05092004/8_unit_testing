package com.doan2025.webtoeic.selenium.coursePurchase;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public final class TestDbUtils {

    private TestDbUtils() { }

    public static void resetDatabase(String action) {
        try {
            String userDir = System.getProperty("user.dir");
            File script = new File(userDir, "../reset-test-db.sh");
            String scriptPath = script.getCanonicalPath();

            ProcessBuilder pb = new ProcessBuilder("bash", scriptPath, action == null ? "reset" : action);
            pb.environment().putIfAbsent("MYSQL_HOST", System.getenv().getOrDefault("MYSQL_HOST", "127.0.0.1"));
            pb.environment().putIfAbsent("MYSQL_PORT", System.getenv().getOrDefault("MYSQL_PORT", "3307"));
            pb.environment().putIfAbsent("MYSQL_USER", System.getenv().getOrDefault("MYSQL_USER", "root"));
            String mysqlPassword = System.getenv().getOrDefault("MYSQL_PASSWORD", System.getProperty("mysql.password", "An198205"));
            if (mysqlPassword != null && !mysqlPassword.isEmpty()) {
                pb.environment().putIfAbsent("MYSQL_PASSWORD", mysqlPassword);
            }

            pb.redirectErrorStream(true);
            Process p = pb.start();

            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String out = r.lines().collect(Collectors.joining(System.lineSeparator()));
                int exit = p.waitFor();
                if (exit != 0) {
                    throw new RuntimeException("reset-test-db.sh failed (exit=" + exit + "):\n" + out);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset test database", e);
        }
    }
}

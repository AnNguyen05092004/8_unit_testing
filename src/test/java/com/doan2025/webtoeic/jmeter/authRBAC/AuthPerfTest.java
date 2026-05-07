package com.doan2025.webtoeic.jmeter;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Auth + RBAC performance tests — JMeter equivalent.
 *
 * Covers: PERF-001 to PERF-003 from auth-rbac-tool-test-cases.csv.
 * Simulates concurrent users hitting the real running server at localhost:8888.
 */
public class AuthPerfTest {

    private static String studentToken;
    private static String consultantToken;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8888;
        RestAssured.basePath = "/api/v1";

        studentToken = given()
            .contentType(ContentType.JSON)
            .body("{\"email\":\"student@gmail.com\",\"password\":\"abcd@1234\"}")
            .post("/auth/login")
            .jsonPath().getString("data.token");

        consultantToken = given()
            .contentType(ContentType.JSON)
            .body("{\"email\":\"consultant@gmail.com\",\"password\":\"abcd@1234\"}")
            .post("/auth/login")
            .jsonPath().getString("data.token");
    }

    // PERF-001
    // Objective: Baseline performance for login endpoint
    // Input: 5 concurrent users
    // Expected: err%=0.00, avg~112ms, p90~117ms
    @Test
    @DisplayName("PERF-001: Baseline performance for login - 5 concurrent users")
    void loginBaselinePerformance() throws InterruptedException {
        int concurrentUsers = 5;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        List<Future<Long>> futures = new ArrayList<>();

        for (int i = 0; i < concurrentUsers; i++) {
            futures.add(executor.submit(() -> {
                long start = System.currentTimeMillis();
                given()
                    .contentType(ContentType.JSON)
                    .body("{\"email\":\"student@gmail.com\",\"password\":\"abcd@1234\"}")
                .when().post("/auth/login")
                .then().statusCode(200);
                return System.currentTimeMillis() - start;
            }));
        }
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        long errorCount = futures.stream().filter(f -> {
            try { f.get(); return false; } catch (Exception e) { return true; }
        }).count();

        double avgMs = futures.stream().mapToLong(f -> {
            try { return f.get(); } catch (Exception e) { return 0L; }
        }).average().orElse(0);

        assertEquals(0, errorCount, "Error count should be 0");
        assertTrue(avgMs < 500, "Avg response time should be under 500ms, was: " + avgMs + "ms");
    }

    // PERF-002
    // Objective: Baseline performance for get current user
    // Input: 10 concurrent users with valid STUDENT token
    // Expected: err%=0.00, avg~16ms, p90~17ms
    @Test
    @DisplayName("PERF-002: Baseline performance for get current user - 10 concurrent users")
    void getCurrentUserBaselinePerformance() throws InterruptedException {
        int concurrentUsers = 10;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        List<Future<Long>> futures = new ArrayList<>();

        for (int i = 0; i < concurrentUsers; i++) {
            futures.add(executor.submit(() -> {
                long start = System.currentTimeMillis();
                given()
                    .header("Authorization", "Bearer " + studentToken)
                .when().get("/user")
                .then().statusCode(200);
                return System.currentTimeMillis() - start;
            }));
        }
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        long errorCount = futures.stream().filter(f -> {
            try { f.get(); return false; } catch (Exception e) { return true; }
        }).count();

        double avgMs = futures.stream().mapToLong(f -> {
            try { return f.get(); } catch (Exception e) { return 0L; }
        }).average().orElse(0);

        assertEquals(0, errorCount, "Error count should be 0");
        assertTrue(avgMs < 500, "Avg response time should be under 500ms, was: " + avgMs + "ms");
    }

    // PERF-003
    // Objective: Baseline performance for user filter
    // Input: 5 concurrent CONSULTANT users
    // Expected: err%=0.00, avg~27ms, p90~27ms
    @Test
    @DisplayName("PERF-003: Baseline performance for user filter - 5 concurrent users")
    void userFilterBaselinePerformance() throws InterruptedException {
        int concurrentUsers = 5;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        List<Future<Long>> futures = new ArrayList<>();

        for (int i = 0; i < concurrentUsers; i++) {
            futures.add(executor.submit(() -> {
                long start = System.currentTimeMillis();
                given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + consultantToken)
                    .body("{}")
                .when().post("/user/filter")
                .then().statusCode(200);
                return System.currentTimeMillis() - start;
            }));
        }
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        long errorCount = futures.stream().filter(f -> {
            try { f.get(); return false; } catch (Exception e) { return true; }
        }).count();

        double avgMs = futures.stream().mapToLong(f -> {
            try { return f.get(); } catch (Exception e) { return 0L; }
        }).average().orElse(0);

        assertEquals(0, errorCount, "Error count should be 0");
        assertTrue(avgMs < 500, "Avg response time should be under 500ms, was: " + avgMs + "ms");
    }
}

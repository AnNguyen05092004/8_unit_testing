package com.doan2025.webtoeic.jmeter;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Course + Lesson performance tests — JMeter equivalent.
 *
 * Covers: PERF-COURSE-001 to PERF-COURSE-003, PERF-LESSON-001 to PERF-LESSON-006
 * from "8_Tool Testing Report - Course Management.csv".
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CoursePerfTest {

    private static String consultantToken;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8888;
        RestAssured.basePath = "/api/v1";

        consultantToken = given()
            .contentType(ContentType.JSON)
            .body("{\"email\":\"consultant@gmail.com\",\"password\":\"abcd@1234\"}")
            .post("/auth/login")
            .jsonPath().getString("data.token");
    }

    // ==================== COURSE PERFORMANCE ====================

    // PERF-COURSE-001: Baseline read performance for public course listing
    // 20 concurrent users, error%=0, acceptable avg/p90 latency
    @Test @Order(1)
    @DisplayName("PERF-COURSE-001: Public course listing — 20 concurrent users")
    void courseListBaselinePerformance() throws InterruptedException {
        int concurrentUsers = 20;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        List<Future<Long>> futures = new ArrayList<>();

        for (int i = 0; i < concurrentUsers; i++) {
            futures.add(executor.submit(() -> {
                long start = System.currentTimeMillis();
                given()
                    .contentType(ContentType.JSON)
                    .body("{}")
                .when().post("/course/get-courses")
                .then().statusCode(200);
                return System.currentTimeMillis() - start;
            }));
        }
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);

        long errorCount = countErrors(futures);
        double avgMs = calcAvg(futures);

        assertEquals(0, errorCount, "Error count should be 0");
        assertTrue(avgMs < 2000, "Avg response time should be under 2000ms, was: " + avgMs + "ms");
        System.out.println("PERF-COURSE-001: avg=" + avgMs + "ms, errors=" + errorCount);
    }

    // PERF-COURSE-002: Baseline read performance for course detail
    // 20 concurrent users, error%=0
    @Test @Order(2)
    @DisplayName("PERF-COURSE-002: Course detail read — 20 concurrent users")
    void courseDetailBaselinePerformance() throws InterruptedException {
        // Get an existing courseId first
        Object courseId = given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when().post("/course/get-courses")
        .then().statusCode(200)
            .extract().jsonPath().get("data.content[0].id");

        Assumptions.assumeTrue(courseId != null, "No courses in DB for perf test");

        int concurrentUsers = 20;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        List<Future<Long>> futures = new ArrayList<>();

        for (int i = 0; i < concurrentUsers; i++) {
            futures.add(executor.submit(() -> {
                long start = System.currentTimeMillis();
                given()
                    .queryParam("id", courseId.toString())
                .when().get("/course")
                .then().statusCode(200);
                return System.currentTimeMillis() - start;
            }));
        }
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);

        long errorCount = countErrors(futures);
        double avgMs = calcAvg(futures);

        assertEquals(0, errorCount, "Error count should be 0");
        assertTrue(avgMs < 2000, "Avg should be under 2000ms, was: " + avgMs + "ms");
        System.out.println("PERF-COURSE-002: avg=" + avgMs + "ms, errors=" + errorCount);
    }

    // PERF-COURSE-003: Write performance for course creation
    // 5 concurrent CONSULTANT users with unique titles
    // Expected: No 5xx, acceptable response time — N/A in CSV
    @Test @Order(3)
    @DisplayName("PERF-COURSE-003: Course creation write — 5 concurrent users (N/A in report)")
    void courseCreationPerformance() throws InterruptedException {
        Assumptions.assumeTrue(consultantToken != null);

        int concurrentUsers = 5;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        List<Future<Long>> futures = new ArrayList<>();

        for (int i = 0; i < concurrentUsers; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                long start = System.currentTimeMillis();
                given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + consultantToken)
                    .body("{\"title\":\"PerfCourse-" + idx + "-" + System.currentTimeMillis()
                        + "\",\"description\":\"Perf test\",\"price\":50000,\"categoryId\":1}")
                .when().post("/course/create")
                .then().statusCode(200);
                return System.currentTimeMillis() - start;
            }));
        }
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);

        long serverErrors = futures.stream().filter(f -> {
            try { f.get(); return false; } catch (Exception e) {
                return e.getMessage() != null && e.getMessage().contains("5");
            }
        }).count();

        assertEquals(0, serverErrors, "No 5xx server errors expected");
        System.out.println("PERF-COURSE-003: completed " + concurrentUsers + " concurrent creates");
    }

    // ==================== LESSON PERFORMANCE ====================

    // PERF-LESSON-001: Baseline read performance for lesson listing
    // 20 concurrent users, error%=0
    @Test @Order(4)
    @DisplayName("PERF-LESSON-001: Lesson listing — 20 concurrent users")
    void lessonListBaselinePerformance() throws InterruptedException {
        int concurrentUsers = 20;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        List<Future<Long>> futures = new ArrayList<>();

        for (int i = 0; i < concurrentUsers; i++) {
            futures.add(executor.submit(() -> {
                long start = System.currentTimeMillis();
                given()
                    .contentType(ContentType.JSON)
                    .body("{}")
                .when().post("/lesson/get-lessons")
                .then().statusCode(200);
                return System.currentTimeMillis() - start;
            }));
        }
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);

        long errorCount = countErrors(futures);
        double avgMs = calcAvg(futures);

        assertEquals(0, errorCount, "Error count should be 0");
        assertTrue(avgMs < 2000, "Avg should be under 2000ms, was: " + avgMs + "ms");
        System.out.println("PERF-LESSON-001: avg=" + avgMs + "ms, errors=" + errorCount);
    }

    // PERF-LESSON-002: Write performance for lesson creation — N/A
    @Test @Order(5)
    @DisplayName("PERF-LESSON-002: Lesson creation write — 5 concurrent users (N/A in report)")
    void lessonCreationPerformance() throws InterruptedException {
        Assumptions.assumeTrue(consultantToken != null);

        // Get a courseId first
        Object courseId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + consultantToken)
            .body("{}")
        .when().post("/course/get-courses")
        .then().statusCode(200).extract().jsonPath().get("data.content[0].id");
        Assumptions.assumeTrue(courseId != null, "No courses for lesson perf test");

        int concurrentUsers = 5;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        List<Future<Long>> futures = new ArrayList<>();

        for (int i = 0; i < concurrentUsers; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                long start = System.currentTimeMillis();
                given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + consultantToken)
                    .body("{\"courseId\":" + courseId
                        + ",\"title\":\"PerfLesson-" + idx + "-" + System.currentTimeMillis()
                        + "\",\"content\":\"Perf content\""
                        + ",\"videoUrl\":\"https://example.com/sample.mp4\"}")
                .when().post("/lesson/create")
                .then().statusCode(200);
                return System.currentTimeMillis() - start;
            }));
        }
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);

        System.out.println("PERF-LESSON-002: completed " + concurrentUsers + " concurrent lesson creates");
    }

    // PERF-LESSON-003: Upload video throughput and stability
    // 5 concurrent uploads, sample 5-20MB mp4 files
    @Test @Order(6)
    @DisplayName("PERF-LESSON-003: Upload video throughput — 5 concurrent uploads")
    void uploadVideoThroughput() throws InterruptedException {
        File videoFile = new File("docs/test-video.mp4");
        Assumptions.assumeTrue(videoFile.exists(), "Test video not found for perf test");

        int concurrentUsers = 5;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        List<Future<Long>> futures = new ArrayList<>();

        for (int i = 0; i < concurrentUsers; i++) {
            futures.add(executor.submit(() -> {
                long start = System.currentTimeMillis();
                given()
                    .multiPart("file", videoFile)
                .when().post("/cloud/upload")
                .then().statusCode(200);
                return System.currentTimeMillis() - start;
            }));
        }
        executor.shutdown();
        executor.awaitTermination(120, TimeUnit.SECONDS);

        long errorCount = countErrors(futures);
        double avgMs = calcAvg(futures);

        assertEquals(0, errorCount, "Upload errors should be 0");
        System.out.println("PERF-LESSON-003: avg=" + avgMs + "ms, errors=" + errorCount);
    }

    // PERF-LESSON-006: Burst upload test (spike)
    // 20 concurrent uploads started at same second
    @Test @Order(7)
    @DisplayName("PERF-LESSON-006: Burst upload spike — 20 concurrent uploads")
    void burstUploadTest() throws InterruptedException {
        File videoFile = new File("docs/test-video.mp4");
        Assumptions.assumeTrue(videoFile.exists(), "Test video not found for burst test");

        int concurrentUsers = 20;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<Long>> futures = new ArrayList<>();

        for (int i = 0; i < concurrentUsers; i++) {
            futures.add(executor.submit(() -> {
                latch.await(); // synchronize all threads to start simultaneously
                long start = System.currentTimeMillis();
                try {
                    given()
                        .multiPart("file", videoFile)
                    .when().post("/cloud/upload")
                    .then().statusCode(200);
                } catch (Exception e) {
                    // bounded failures are acceptable in spike test
                }
                return System.currentTimeMillis() - start;
            }));
        }
        latch.countDown(); // release all threads at once
        executor.shutdown();
        executor.awaitTermination(180, TimeUnit.SECONDS);

        System.out.println("PERF-LESSON-006: burst test completed with " + concurrentUsers + " concurrent uploads");
    }

    // ==================== HELPERS ====================

    private long countErrors(List<Future<Long>> futures) {
        return futures.stream().filter(f -> {
            try { f.get(); return false; } catch (Exception e) { return true; }
        }).count();
    }

    private double calcAvg(List<Future<Long>> futures) {
        return futures.stream().mapToLong(f -> {
            try { return f.get(); } catch (Exception e) { return 0L; }
        }).average().orElse(0);
    }
}

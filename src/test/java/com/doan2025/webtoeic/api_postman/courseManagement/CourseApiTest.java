package com.doan2025.webtoeic.postman;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.io.File;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Course + Lesson API functional tests — Postman equivalent.
 *
 * Covers: API-COURSE-001 to API-COURSE-012, API-LESSON-001 to API-LESSON-010
 * from "8_Tool Testing Report - Course Management.csv".
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CourseApiTest {

    private static String consultantToken;
    private static String managerToken;
    private static String studentToken;
    private static Long createdCourseId;
    private static Long createdLessonId;
    private static String uploadedVideoUrl;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8888;
        RestAssured.basePath = "/api/v1";
    }

    // ==================== COURSE API TESTS ====================

    // API-COURSE-001: Get CONSULTANT token
    @Test @Order(1)
    @DisplayName("API-COURSE-001: Get CONSULTANT token for course/lesson tests")
    void getConsultantToken() {
        Response res = given()
            .contentType(ContentType.JSON)
            .body("{\"email\":\"consultant@gmail.com\",\"password\":\"abcd@1234\"}")
        .when().post("/auth/login")
        .then().statusCode(200).body("data.token", notNullValue())
            .extract().response();
        consultantToken = res.jsonPath().getString("data.token");
        assertNotNull(consultantToken);
    }

    // API-COURSE-002: Get MANAGER token
    @Test @Order(2)
    @DisplayName("API-COURSE-002: Get MANAGER token for RBAC checks")
    void getManagerToken() {
        Response res = given()
            .contentType(ContentType.JSON)
            .body("{\"email\":\"manager@gmail.com\",\"password\":\"abcd@1234\"}")
        .when().post("/auth/login")
        .then().statusCode(200).body("data.token", notNullValue())
            .extract().response();
        managerToken = res.jsonPath().getString("data.token");
        assertNotNull(managerToken);
    }

    // API-COURSE-003: Get STUDENT token
    @Test @Order(3)
    @DisplayName("API-COURSE-003: Get STUDENT token for RBAC negative checks")
    void getStudentToken() {
        Response res = given()
            .contentType(ContentType.JSON)
            .body("{\"email\":\"student@gmail.com\",\"password\":\"abcd@1234\"}")
        .when().post("/auth/login")
        .then().statusCode(200).body("data.token", notNullValue())
            .extract().response();
        studentToken = res.jsonPath().getString("data.token");
        assertNotNull(studentToken);
    }

    // API-COURSE-004: Get public course list without token
    @Test @Order(4)
    @DisplayName("API-COURSE-004: Get public course list without token")
    void getPublicCourseList() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when().post("/course/get-courses")
        .then().statusCode(200);
    }

    // API-COURSE-005: Get course detail publicly
    @Test @Order(5)
    @DisplayName("API-COURSE-005: Get course detail publicly")
    void getCourseDetailPublicly() {
        // First get a course ID from public list
        Response listRes = given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when().post("/course/get-courses")
        .then().statusCode(200).extract().response();

        Object firstId = listRes.jsonPath().get("data.content[0].id");
        Assumptions.assumeTrue(firstId != null, "No courses in DB");

        given()
            .queryParam("id", firstId.toString())
        .when().get("/course")
        .then().statusCode(200);
    }

    // API-COURSE-006: Create new course as CONSULTANT
    @Test @Order(6)
    @DisplayName("API-COURSE-006: Create new course as CONSULTANT")
    void createCourseAsConsultant() {
        Assumptions.assumeTrue(consultantToken != null);
        String body = "{\"title\":\"Postman Test Course " + System.currentTimeMillis()
            + "\",\"description\":\"Auto-created by Postman test\""
            + ",\"price\":100000,\"categoryId\":1}";
        Response res = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + consultantToken)
            .body(body)
        .when().post("/course/create")
        .then().statusCode(200).extract().response();

        createdCourseId = res.jsonPath().getLong("data.id");
        assertNotNull(createdCourseId);
    }

    // API-COURSE-007: RBAC deny STUDENT create course
    @Test @Order(7)
    @DisplayName("API-COURSE-007: RBAC deny STUDENT create course")
    void studentCannotCreateCourse() {
        Assumptions.assumeTrue(studentToken != null);
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + studentToken)
            .body("{\"title\":\"Hacked\",\"price\":0}")
        .when().post("/course/create")
        .then().statusCode(403);
    }

    // API-COURSE-008: Update existing course as CONSULTANT
    @Test @Order(8)
    @DisplayName("API-COURSE-008: Update existing course as CONSULTANT")
    void updateCourseAsConsultant() {
        Assumptions.assumeTrue(consultantToken != null && createdCourseId != null);
        String body = "{\"id\":" + createdCourseId
            + ",\"title\":\"Updated Course Title\""
            + ",\"description\":\"Updated description\""
            + ",\"price\":200000}";
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + consultantToken)
            .body(body)
        .when().post("/course/update-info")
        .then().statusCode(200);
    }

    // API-COURSE-009: RBAC deny CONSULTANT approve/disable course
    @Test @Order(9)
    @DisplayName("API-COURSE-009: RBAC deny CONSULTANT approve/disable course")
    void consultantCannotUpdateCourseStatus() {
        Assumptions.assumeTrue(consultantToken != null && createdCourseId != null);
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + consultantToken)
            .body("{\"id\":" + createdCourseId + ",\"isActive\":false,\"isDelete\":false}")
        .when().post("/course/update-status")
        .then().statusCode(403);
    }

    // API-COURSE-010: Manager can update status course
    @Test @Order(10)
    @DisplayName("API-COURSE-010: Manager can update status course")
    void managerCanUpdateCourseStatus() {
        Assumptions.assumeTrue(managerToken != null && createdCourseId != null);
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + managerToken)
            .body("{\"id\":" + createdCourseId + ",\"isActive\":false,\"isDelete\":false}")
        .when().post("/course/update-status")
        .then().statusCode(200);
    }

    // API-COURSE-011: Manager can list all courses
    @Test @Order(11)
    @DisplayName("API-COURSE-011: Manager can list all courses")
    void managerCanListAllCourses() {
        Assumptions.assumeTrue(managerToken != null);
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + managerToken)
            .body("{}")
        .when().post("/course/all-courses")
        .then().statusCode(200);
    }

    // API-COURSE-012: RBAC deny STUDENT list all courses
    @Test @Order(12)
    @DisplayName("API-COURSE-012: RBAC deny STUDENT list all courses")
    void studentCannotListAllCourses() {
        Assumptions.assumeTrue(studentToken != null);
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + studentToken)
            .body("{}")
        .when().post("/course/all-courses")
        .then().statusCode(403);
    }

    // ==================== LESSON API TESTS ====================

    // API-LESSON-001: Upload video file for lesson content
    @Test @Order(13)
    @DisplayName("API-LESSON-001: Upload video file for lesson content")
    void uploadVideoFile() {
        File videoFile = new File("docs/test-video.mp4");
        Assumptions.assumeTrue(videoFile.exists(), "Test video not found");

        Response res = given()
            .multiPart("file", videoFile)
        .when().post("/cloud/upload")
        .then().statusCode(200).extract().response();

        uploadedVideoUrl = res.jsonPath().getString("data");
        assertNotNull(uploadedVideoUrl);
    }

    // API-LESSON-001A: Reject upload when file is missing
    @Test @Order(14)
    @DisplayName("API-LESSON-001A: Reject upload when file is missing")
    void rejectUploadWhenFileMissing() {
        given()
            .contentType("multipart/form-data")
        .when().post("/cloud/upload")
        .then().statusCode(anyOf(equalTo(400), equalTo(500)));
    }

    // API-LESSON-001B: Upload document cannot get file video
    @Test @Order(15)
    @DisplayName("API-LESSON-001B: Upload document should reject video file — FAIL: still accepts")
    void uploadDocumentRejectsVideoFile() {
        File videoFile = new File("docs/test-video.mp4");
        Assumptions.assumeTrue(videoFile.exists());

        Response res = given()
            .multiPart("file", videoFile)
        .when().post("/cloud/upload")
        .then().extract().response();

        // CSV says: FAIL — still accepts video file
        // Expecting 4xx but API returns 200
        assertTrue(res.statusCode() >= 400,
            "EXPECTED FAIL: API should reject video in document upload, but got " + res.statusCode());
    }

    // API-LESSON-001D: Upload video then verify URL is reusable in lesson/create
    @Test @Order(16)
    @DisplayName("API-LESSON-001D: Upload video then verify URL is reusable in lesson/create")
    void uploadVideoThenCreateLesson() {
        Assumptions.assumeTrue(consultantToken != null && uploadedVideoUrl != null && createdCourseId != null);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + consultantToken)
            .body("{\"courseId\":" + createdCourseId
                + ",\"title\":\"Reuse URL Lesson\""
                + ",\"content\":\"Test content\""
                + ",\"videoUrl\":\"" + uploadedVideoUrl + "\"}")
        .when().post("/lesson/create")
        .then().statusCode(200);
    }

    // API-LESSON-001F: Delete uploaded video twice (idempotency check)
    @Test @Order(17)
    @DisplayName("API-LESSON-001F: Delete uploaded video twice (idempotency check)")
    void deleteUploadedVideoIdempotency() {
        Assumptions.assumeTrue(uploadedVideoUrl != null);

        // First delete
        given()
            .contentType(ContentType.JSON)
            .body("{\"url\":\"" + uploadedVideoUrl + "\"}")
        .when().post("/cloud/delete")
        .then().statusCode(200);

        // Second delete (should be safe no-op or 4xx)
        Response secondRes = given()
            .contentType(ContentType.JSON)
            .body("{\"url\":\"" + uploadedVideoUrl + "\"}")
        .when().post("/cloud/delete")
        .then().extract().response();

        assertTrue(secondRes.statusCode() < 500,
            "Second delete should not cause 5xx server error");
    }

    // API-LESSON-002: Create lesson with uploaded video URL
    @Test @Order(18)
    @DisplayName("API-LESSON-002: Create lesson with uploaded video URL")
    void createLessonWithVideoUrl() {
        Assumptions.assumeTrue(consultantToken != null && createdCourseId != null);

        // Upload a fresh video first
        File videoFile = new File("docs/test-video.mp4");
        Assumptions.assumeTrue(videoFile.exists());
        String freshUrl = given()
            .multiPart("file", videoFile)
        .when().post("/cloud/upload")
        .then().statusCode(200).extract().jsonPath().getString("data");

        Response res = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + consultantToken)
            .body("{\"courseId\":" + createdCourseId
                + ",\"title\":\"Test Lesson " + System.currentTimeMillis() + "\""
                + ",\"content\":\"Lesson content\""
                + ",\"videoUrl\":\"" + freshUrl + "\"}")
        .when().post("/lesson/create")
        .then().statusCode(200).extract().response();

        createdLessonId = res.jsonPath().getLong("data.id");
        assertNotNull(createdLessonId);
    }

    // API-LESSON-003: Validation reject empty videoUrl
    @Test @Order(19)
    @DisplayName("API-LESSON-003: Validation reject empty videoUrl")
    void rejectEmptyVideoUrl() {
        Assumptions.assumeTrue(consultantToken != null && createdCourseId != null);
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + consultantToken)
            .body("{\"courseId\":" + createdCourseId
                + ",\"title\":\"No Video Lesson\""
                + ",\"content\":\"content\""
                + ",\"videoUrl\":\"\"}")
        .when().post("/lesson/create")
        .then().statusCode(400);
    }

    // API-LESSON-004: Get lesson list by course filters
    @Test @Order(20)
    @DisplayName("API-LESSON-004: Get lesson list by course filters")
    void getLessonListByFilters() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"searchString\":\"\",\"categories\":[]}")
        .when().post("/lesson/get-lessons")
        .then().statusCode(200);
    }

    // API-LESSON-005: Get lesson detail with valid lesson ID
    @Test @Order(21)
    @DisplayName("API-LESSON-005: Get lesson detail with valid lesson ID")
    void getLessonDetail() {
        Assumptions.assumeTrue(createdLessonId != null);
        given()
            .queryParam("id", createdLessonId)
        .when().get("/lesson")
        .then().statusCode(200);
    }

    // API-LESSON-006: Update lesson metadata by CONSULTANT owner
    @Test @Order(22)
    @DisplayName("API-LESSON-006: Update lesson metadata/video URL by CONSULTANT owner")
    void updateLessonByConsultant() {
        Assumptions.assumeTrue(consultantToken != null && createdLessonId != null);
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + consultantToken)
            .body("{\"id\":" + createdLessonId
                + ",\"title\":\"Updated Lesson\""
                + ",\"content\":\"Updated content\"}")
        .when().post("/lesson/update-info")
        .then().statusCode(200);
    }

    // API-LESSON-007: Disable lesson by MANAGER
    @Test @Order(23)
    @DisplayName("API-LESSON-007: Disable lesson by MANAGER")
    void disableLessonByManager() {
        Assumptions.assumeTrue(managerToken != null && createdLessonId != null);
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + managerToken)
            .body("{\"id\":" + createdLessonId + ",\"isActive\":false,\"isDelete\":false}")
        .when().post("/lesson/update-status")
        .then().statusCode(200);
    }

    // API-LESSON-008: CONSULTANT can list own lessons
    @Test @Order(24)
    @DisplayName("API-LESSON-008: CONSULTANT can list own lessons")
    void consultantListOwnLessons() {
        Assumptions.assumeTrue(consultantToken != null);
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + consultantToken)
            .body("{}")
        .when().post("/lesson/get-own-lessons")
        .then().statusCode(200);
    }

    // API-LESSON-009: RBAC deny STUDENT list own lessons
    @Test @Order(25)
    @DisplayName("API-LESSON-009: RBAC deny STUDENT list own lessons endpoint")
    void studentCannotListOwnLessons() {
        Assumptions.assumeTrue(studentToken != null);
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + studentToken)
            .body("{}")
        .when().post("/lesson/get-own-lessons")
        .then().statusCode(403);
    }

    // API-LESSON-010: Delete uploaded video resource after test
    @Test @Order(26)
    @DisplayName("API-LESSON-010: Delete uploaded video resource after test")
    void deleteUploadedVideoCleanup() {
        Assumptions.assumeTrue(uploadedVideoUrl != null);
        given()
            .contentType(ContentType.JSON)
            .body("{\"url\":\"" + uploadedVideoUrl + "\"}")
        .when().post("/cloud/delete")
        .then().statusCode(200);
    }
}

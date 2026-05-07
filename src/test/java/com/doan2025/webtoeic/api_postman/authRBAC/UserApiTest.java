package com.doan2025.webtoeic.postman;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * User API + RBAC functional tests — Postman equivalent.
 * Covers: API-009 to API-013 from auth-rbac-tool-test-cases.csv.
 */
public class UserApiTest {

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

    @Test
    @DisplayName("API-009: Get current user rejected without token")
    void getCurrentUserRejectedWithoutToken() {
        given().when().get("/user").then().statusCode(401);
    }

    @Test
    @DisplayName("API-010: Get current user returns profile for STUDENT")
    void getCurrentUserReturnsProfileForStudent() {
        given()
            .header("Authorization", "Bearer " + studentToken)
        .when().get("/user")
        .then().statusCode(200).body("data", notNullValue());
    }

    @Test
    @DisplayName("API-011: Filter users forbidden for STUDENT role")
    void filterUsersForbiddenForStudent() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + studentToken)
            .body("{}")
        .when().post("/user/filter")
        .then().statusCode(403);
    }

    @Test
    @DisplayName("API-012: Filter users allowed for CONSULTANT role")
    void filterUsersAllowedForConsultant() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + consultantToken)
            .body("{}")
        .when().post("/user/filter")
        .then().statusCode(200).body("data", notNullValue());
    }

    @Test
    @DisplayName("API-013: Delete user forbidden for STUDENT role")
    void deleteUserForbiddenForStudent() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + studentToken)
            .body("{\"id\":999}")
        .when().post("/user/delete-user")
        .then().statusCode(403);
    }
}

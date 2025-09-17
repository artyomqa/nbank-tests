package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import models.LoginRequest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import requests.LoginRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static io.restassured.RestAssured.given;

public class BaseTest {
    protected SoftAssertions softly;
    protected static String adminToken;

    //TODO убрать отсюда функционал получения токена админа и утильные методы
    @BeforeAll
    public static void globalSetup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 4111;

        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        adminToken = new LoginRequester(RequestSpecs.noAuth(), ResponseSpecs.returnsOk())
                .send(request)
                .extract()
                .header("Authorization");
    }

    @BeforeEach
    public void setupTest() {
        softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }

    protected static int createBankAccount(String userToken) {
        return given()
                .header("Authorization", userToken)
                .when()
                .post("/api/v1/accounts")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    protected float getAccountBalance(String userToken, int accountId) {
        return given()
                .header("Authorization", userToken)
                .when()
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(200)
                .extract()
                .path("find { it.id == %s }.balance".formatted(accountId));
    }
}

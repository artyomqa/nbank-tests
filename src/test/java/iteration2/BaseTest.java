package iteration2;

import io.restassured.RestAssured;
import models.LoginRequest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import requests.LoginRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class BaseTest {
    protected static final float MAX_DEPOSIT_AMOUNT = 5000.00f;
    protected static final float MAX_TRANSFER_AMOUNT = 10000.00f;

    protected SoftAssertions softly;
    protected static String adminToken;

    //TODO убрать отсюда функционал получения токена админа
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
}

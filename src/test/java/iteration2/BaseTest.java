package iteration2;

import io.restassured.RestAssured;
import models.LoginRequest;
import org.junit.jupiter.api.BeforeAll;
import requests.LoginRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static io.restassured.RestAssured.given;

public class BaseTest {
    protected static String adminToken;

    //TODO убрать отсюда функционал получения токена админа
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 4111;

        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        adminToken = new LoginRequester(RequestSpecs.unAuth(), ResponseSpecs.returnsOk())
                .send(request)
                .extract()
                .header("Authorization");

        // Получение токена админа
//        adminToken = given()
//                .contentType(ContentType.JSON)
//                .body("""
//                        {
//                            "username": "admin",
//                            "password": "admin"
//                        }
//                        """)
//                .when()
//                .post("/api/v1/auth/login")
//                .then()
//                .statusCode(200)
//                .extract()
//                .header("Authorization");
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

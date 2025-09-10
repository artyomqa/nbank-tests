package iteration2;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import static io.restassured.RestAssured.given;

public class ChangeUsernameTest {
    private static String adminToken;
    private static String userToken;
    private static int userId;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 4111;

        // Получение токена админа
        adminToken = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "admin",
                            "password": "admin"
                        }
                        """)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .header("Authorization");

        // Создание пользователя и получение токена и id
        Response responseCreatedUser = given()
                .header("Authorization", adminToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "user",
                            "password": "pass123W!",
                            "role": "USER"
                        }
                        """)
                .when()
                .post("/api/v1/admin/users")
                .then()
                .statusCode(201)
                .extract()
                .response();

        userToken = responseCreatedUser.header("Authorization");
        userId = responseCreatedUser.jsonPath().getInt("id");
    }

    // Удаляем пользователя после прохождения тестов
    @AfterAll
    public static void deleteUser() {
        given()
                .header("Authorization", adminToken)
                .when()
                .delete("/api/v1/admin/users/" + userId)
                .then()
                .statusCode(200);
    }
}

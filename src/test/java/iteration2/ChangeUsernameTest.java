package iteration2;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

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
                            "username": "user1",
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

    @ParameterizedTest
    @DisplayName("Изменение имени (позитивные сценарии)")
    @ValueSource(strings = {"Petr Ivanov", "petr ivanov", "PETR IVANOV"})
    public void changeNamePositiveTest(String name) {
        // Изменяем имя
        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(generateRequestBody(name))
                .when()
                .put("/api/v1/customer/profile")
                .then()
                .statusCode(200)
                .body("customer.id", equalTo(userId))
                .body("customer.name", equalTo(name));

        // Проверяем, что имя обновилось
        given()
                .header("Authorization", userToken)
                .when()
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(200)
                .body("name", equalTo(name));
    }

    @ParameterizedTest
    @DisplayName("Изменение имени (некорректные имена)")
    @ValueSource(strings = {"Ivan", "Ivan Petrovich Ivanov", "Ivan  Petrovich", "Ivan Perovich1",
            "Ivan_Petrovich", "Ivan-Petrovich", "Ivan Petrovich.", "Иван Иванов", "   ", ""})
    public void changeNameNegativeTest(String name) {
        // Пытаемся изменить имя
        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body(generateRequestBody(name))
                .when()
                .put("/api/v1/customer/profile")
                .then()
                .statusCode(400);

        // Проверяем, что имя не обновилось
        given()
                .header("Authorization", userToken)
                .when()
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(200)
                .body("name", not(equalTo(name)));
    }

    @Test
    @DisplayName("Изменение имени у администратора")
    public void changeAdminName() {
        given()
                .header("Authorization", adminToken)
                .contentType(ContentType.JSON)
                .body(generateRequestBody("Petr Ivanov"))
                .when()
                .put("/api/v1/customer/profile")
                .then()
                .statusCode(403);
    }


    private String generateRequestBody(String name) {
        return """
                {
                    "name": "%s"
                }
                """.formatted(name);
    }
}

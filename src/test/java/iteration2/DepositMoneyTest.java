package iteration2;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

// в методе createUsersAndAccounts есть лишние действия, которые нужны для других классов с тестами (напр. второй юзер)
public class DepositMoneyTest {
    private static String adminToken;
    private static String firstUserToken;
    private static int firstUserId;
    private static String secondUserToken;
    private static int secondUserId;
    private static int firstUserAccountId1;
    private static int firstUserAccountId2;
    private static int secondUserAccountId1;

    @BeforeAll
    public static void createUsersAndAccounts() {
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
                .post("api/v1/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .header("Authorization");


        // Создание первого пользователя и получение токена
        Response responseCreatedFirstUser = given()
                .header("Authorization", adminToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "Alex",
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

        firstUserToken = responseCreatedFirstUser.header("Authorization");
        firstUserId = responseCreatedFirstUser.jsonPath().getInt("id");

        // Создание второго пользователя и получение токена
        Response responseCreatedSecondUser = given()
                .header("Authorization", adminToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "Anna",
                            "password": "pass111X!",
                            "role": "USER"
                        }
                        """)
                .when()
                .post("/api/v1/admin/users")
                .then()
                .statusCode(201)
                .extract()
                .response();

        secondUserToken = responseCreatedSecondUser.header("Authorization");
        secondUserId = responseCreatedSecondUser.jsonPath().getInt("id");

        // Создание счета у первого пользователя
        firstUserAccountId1 = given()
                .header("Authorization", firstUserToken)
                .when()
                .post("/api/v1/accounts")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .path("id");

        // Создание второго счета у первого пользователя
        firstUserAccountId2 = given()
                .header("Authorization", firstUserToken)
                .when()
                .post("/api/v1/accounts")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .path("id");

        // Создание счета у второго пользователя
        secondUserAccountId1 = given()
                .header("Authorization", secondUserToken)
                .when()
                .post("/api/v1/accounts")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    // Удаляем юзеров после прохождения всех тестов
    @AfterAll
    public static void deleteUsers() {
        given()
                .header("Authorization", adminToken)
                .when()
                .get("/api/v1/admin/users")
                .then()
                .log().body();

        given()
                .header("Authorization", adminToken)
                .when()
                .delete("/api/v1/admin/users/" + firstUserId);

        given()
                .header("Authorization", adminToken)
                .when()
                .delete("/api/v1/admin/users/" + secondUserId);
    }

    @ParameterizedTest
    @DisplayName("Пополнение баланса (позитивные сценарии)")
    @MethodSource("getValidAmounts")
    public void depositMoneyPositiveTest(double value, double totalSum) {
        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .body(generateRequestBody(firstUserAccountId1, value))
                .when()
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(200)
                .body("id", equalTo(firstUserAccountId1))
                .body("balance", equalTo((float) totalSum));
    }

    public static Stream<Arguments> getValidAmounts() {
        return Stream.of(
                Arguments.of(100, 100),
                Arguments.of(115.99, 215.99),
                Arguments.of(20.5, 236.49)
        );
    }

    @ParameterizedTest
    @DisplayName("Невалидные значения суммы")
    @ValueSource(ints = {0, -1})
    public void depositWithInvalidAmountTest(int amount) {
        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .body(generateRequestBody(firstUserAccountId1, amount))
                .when()
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Пополнение чужого баланса")
    public void depositToAnotherAccountTest() {
        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .body(generateRequestBody(secondUserAccountId1, 10))
                .when()
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Пополнение баланса на несуществующем id счета")
    public void depositToInvalidAccountTest() {
        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .body(generateRequestBody(firstUserAccountId1 + secondUserAccountId1, 10))
                .when()
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Пополнение баланса от имени администратора")
    public void depositByAdminTest() {
        given()
                .header("Authorization", adminToken)
                .contentType(ContentType.JSON)
                .body(generateRequestBody(firstUserAccountId1, 10))
                .when()
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Пополнение баланса неавторизованным пользователем")
    public void depositByUnauthorizedUserTest() {
        given()
                .contentType(ContentType.JSON)
                .body(generateRequestBody(firstUserAccountId1, 10))
                .when()
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(401);
    }

    private String generateRequestBody(int id, Number balance) {
        return """
                {
                    "id": %d,
                    "balance": %s
                }
                """.formatted(id, balance);
    }
}

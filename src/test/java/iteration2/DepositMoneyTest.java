package iteration2;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO в методе createUsersAndAccounts есть лишние действия, которые нужны для других классов с тестами (напр. второй юзер)
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
        firstUserAccountId1 = createBankAccount(firstUserToken);

        // Создание второго счета у первого пользователя
        firstUserAccountId2 = createBankAccount(firstUserToken);

        // Создание счета у второго пользователя
        secondUserAccountId1 = createBankAccount(secondUserToken);
    }

    // Удаляем юзеров после прохождения всех тестов
    @AfterAll
    public static void deleteUsers() {
//        given()
//                .header("Authorization", adminToken)
//                .when()
//                .get("/api/v1/admin/users")
//                .then()
//                .log().body();

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
    @ValueSource(floats = {100f, 115.99f, 20.5f, 0.01f, 5000.00f})
    public void depositMoneyPositiveTest(float amount) {
        // Получаем текущий баланс пользователя
        float currentBalance = getAccountBalance(firstUserToken, firstUserAccountId1);

        // Ожидаемый баланс после внесения депозита (округляем до 2 знаков после запятой, чтобы избежать неточностей)
        float expectedBalance = (float) Math.round((currentBalance + amount) * 100) / 100;

        // Пополняем баланс и проверяем, что в ответе получено корректное значение баланса
        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .body(generateRequestBody(firstUserAccountId1, amount))
                .when()
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(200)
                .body("id", equalTo(firstUserAccountId1))
                .body("balance", equalTo(expectedBalance));

        // Проверяем, что баланс пользователя изменился
        float actualBalance = getAccountBalance(firstUserToken, firstUserAccountId1);
        assertEquals(expectedBalance, actualBalance);
    }

    @ParameterizedTest
    @DisplayName("Невалидные значения суммы")
    @ValueSource(floats = {0f, 5000.01f, -1f})
    public void depositWithInvalidAmountTest(float amount) {
        // Получаем текущий баланс пользователя
        float currentBalance = getAccountBalance(firstUserToken, firstUserAccountId1);

        // Пытаемся пополнить баланс и проверяем, что в ответе статус-код 400
        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .body(generateRequestBody(firstUserAccountId1, amount))
                .when()
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(400);

        // Проверяем, что баланс пользователя не изменился
        float actualBalance = getAccountBalance(firstUserToken, firstUserAccountId1);
        assertEquals(currentBalance, actualBalance);
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

    private String generateRequestBody(int accountId, Number balance) {
        return """
                {
                    "id": %d,
                    "balance": %s
                }
                """.formatted(accountId, balance);
    }

    private static int createBankAccount(String userToken) {
        return given()
                .header("Authorization", userToken)
                .when()
                .post("/api/v1/accounts")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    private float getAccountBalance(String userToken, int accountId) {
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

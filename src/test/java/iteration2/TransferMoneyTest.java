package iteration2;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferMoneyTest {
    private static String adminToken;

    private static String firstUserToken;
    private static int firstUserId;
    private static int firstUserAccountId1;
    private static int firstUserAccountId2;

    private static String secondUserToken;
    private static int secondUserId;
    private static int secondUserAccountId1;

    // Перед запуском всех тестов получаем токен админа
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
    }

    // Перед запуском каждого теста создаем пользователей и счета
    @BeforeEach
    public void createUsersAndAccounts() {
        // Создание первого пользователя и получение токена
        Response responseCreatedFirstUser = given()
                .header("Authorization", adminToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "%s",
                            "password": "pass123W!",
                            "role": "USER"
                        }
                        """.formatted("user" + (int)(Math.random() * 9000)))
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
                            "username": "%s",
                            "password": "pass111X!",
                            "role": "USER"
                        }
                        """.formatted("user" + (int)(Math.random() * 9000)))
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

    // Удаляем юзеров после прохождения каждого теста
    @AfterEach
    public void deleteUsers() {
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
    @DisplayName("Перевод между своими счетами")
    @ValueSource(floats = {100f, 20.5f, 115.99f, 0.01f, 10000.00f})
    public void transferBetweenOwnAccountsTest(float amount) {
        // Пополняем счет первого пользователя на 10000
        for (int i = 0; i < 2; i++) {
            given()
                    .header("Authorization", firstUserToken)
                    .contentType(ContentType.JSON)
                    .body("""
                            {
                                "id": %d,
                                "balance": 5000
                            }
                            """.formatted(firstUserAccountId1))
                    .when()
                    .post("/api/v1/accounts/deposit")
                    .then()
                    .statusCode(200);
        }

        // Выполняем перевод
        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .body(generateRequestBody(firstUserAccountId1, firstUserAccountId2, amount))
                .when()
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(200)
                .body("message", equalTo("Transfer successful"))
                .body("amount", equalTo(amount))
                .body("senderAccountId", equalTo(firstUserAccountId1))
                .body("receiverAccountId", equalTo(firstUserAccountId2));

        // Получаем баланс счета-отправителя после списания средств
        float senderFinalBalance = getAccountBalance(firstUserToken, firstUserAccountId1);

        // Получаем баланс счета-получателя после зачисления средств
        float receiverFinalBalance = getAccountBalance(firstUserToken, firstUserAccountId2);

        // Ожидаемый баланс счета-отправителя (округляем до 2 знаков после запятой, чтобы избежать неточностей)
        float senderExpectedBalance = (float) Math.round((10000f - amount) * 100) / 100;

        assertEquals(senderExpectedBalance, senderFinalBalance);
        assertEquals(amount, receiverFinalBalance);
    }

    @ParameterizedTest
    @DisplayName("Перевод на счет другого пользователя")
    @ValueSource(floats = {100f, 20.5f, 115.99f, 0.01f, 10000.00f})
    public void transferToAnotherUsersAccountTest(float amount) {
        // Пополняем счет первого пользователя на 10000
        for (int i = 0; i < 2; i++) {
            given()
                    .header("Authorization", firstUserToken)
                    .contentType(ContentType.JSON)
                    .body("""
                            {
                                "id": %d,
                                "balance": 5000
                            }
                            """.formatted(firstUserAccountId1))
                    .when()
                    .post("/api/v1/accounts/deposit")
                    .then()
                    .statusCode(200);
        }

        // Выполняем перевод
        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .body(generateRequestBody(firstUserAccountId1, secondUserAccountId1, amount))
                .when()
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(200)
                .body("message", equalTo("Transfer successful"))
                .body("amount", equalTo(amount))
                .body("senderAccountId", equalTo(firstUserAccountId1))
                .body("receiverAccountId", equalTo(secondUserAccountId1));

        // Получаем баланс счета-отправителя после списания средств
        float senderFinalBalance = getAccountBalance(firstUserToken, firstUserAccountId1);

        // Получаем баланс счета-получателя после зачисления средств
        float receiverFinalBalance = getAccountBalance(secondUserToken, secondUserAccountId1);

        // Ожидаемый баланс счета-отправителя (округляем до 2 знаков после запятой, чтобы избежать неточностей)
        float senderExpectedBalance = (float) Math.round((10000f - amount) * 100) / 100;

        assertEquals(senderExpectedBalance, senderFinalBalance);
        assertEquals(amount, receiverFinalBalance);
    }





















    private String generateRequestBody(int senderAccountId, int receiverAccountId, Number balance) {
        return """
                {
                    "senderAccountId": %d,
                    "receiverAccountId": %d,
                    "amount": %s
                }
                """.formatted(senderAccountId, receiverAccountId, balance);
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

package iteration2;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferMoneyTest extends BaseTest {
    private static String firstUserToken;
    private static int firstUserId;
    private static int firstUserAccountId1;
    private static int firstUserAccountId2;

    private static String secondUserToken;
    private static int secondUserId;
    private static int secondUserAccountId1;

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
                        """.formatted("user" + (int) (Math.random() * 9000)))
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
                        """.formatted("user" + (int) (Math.random() * 9000)))
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
                .delete("/api/v1/admin/users/" + firstUserId)
                .then()
                .statusCode(200);

        given()
                .header("Authorization", adminToken)
                .when()
                .delete("/api/v1/admin/users/" + secondUserId)
                .then()
                .statusCode(200);
    }


    @ParameterizedTest
    @DisplayName("Перевод между своими счетами")
    @ValueSource(floats = {100f, 20.5f, 115.99f, 0.01f, 10000.00f})
    public void transferBetweenOwnAccountsTest(float amount) {
        // Пополняем счет первого пользователя на 10000
        for (int i = 0; i < 2; i++) {
            depositMoney(firstUserToken, firstUserAccountId1, 5000f);
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
            depositMoney(firstUserToken, firstUserAccountId1, 5000f);
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

    @ParameterizedTest
    @DisplayName("Перевод невалидной суммы")
    @ValueSource(floats = {50.01f, 0, -1})
    public void transferInvalidAmountTest(float amount) {
        depositMoney(firstUserToken, firstUserAccountId1, 50.0f);

        // Пробуем выполнить перевод
        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .body(generateRequestBody(firstUserAccountId1, secondUserAccountId1, amount))
                .when()
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Перевод с чужого счета")
    public void transferFromElseAccountTest() {
        depositMoney(secondUserToken, secondUserAccountId1, 50.0f);

        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .body(generateRequestBody(secondUserAccountId1, firstUserAccountId1, 10))
                .when()
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Перевод с несуществующего счета")
    public void transferFromInvalidAccountTest() {
        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .body(generateRequestBody(firstUserAccountId1 + secondUserAccountId1, firstUserAccountId1, 10))
                .when()
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Перевод на несуществующий счет")
    public void transferToInvalidAccountTest() {
        depositMoney(firstUserToken, firstUserAccountId1, 50.0f);

        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .body(generateRequestBody(firstUserAccountId1, firstUserAccountId1 + secondUserAccountId1, 10))
                .when()
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Перевод на тот же самый счет")
    @Disabled("Тест временно отключен. Есть дефект.")
    public void transferToTheSameAccountTest() {
        depositMoney(firstUserToken, firstUserAccountId1, 50.0f);

        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .body(generateRequestBody(firstUserAccountId1, firstUserAccountId1, 10))
                .when()
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Перевод от имени администратора")
    public void transferMoneyByAdminTest() {
        depositMoney(firstUserToken, firstUserAccountId1, 50.0f);

        given()
                .header("Authorization", adminToken)
                .contentType(ContentType.JSON)
                .body(generateRequestBody(firstUserAccountId1, firstUserAccountId2, 10))
                .when()
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Перевод неавторизованным пользователем")
    public void transferMoneyByUnauthorizedUserTest() {
        depositMoney(firstUserToken, firstUserAccountId1, 50.0f);

        given()
                .contentType(ContentType.JSON)
                .body(generateRequestBody(firstUserAccountId1, firstUserAccountId2, 10))
                .when()
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Получение информации о транзакциях")
    public void getTransactionsInfoTest() {
        float transferAmount = 20.0f;

        depositMoney(firstUserToken, firstUserAccountId1, 50.0f);

        // Выполняем перевод
        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .body(generateRequestBody(firstUserAccountId1, firstUserAccountId2, transferAmount))
                .when()
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(200);

        /*
        Проверяем:
        1) на счете-отправителе есть транзакция на нужную сумму (amount) и она связана со счетом-получателем (relatedAccountId)
        2) на счете-получателе есть транзакция на нужную сумму (amount) и она связана со счетом-отправителем (relatedAccountId)
         */
        given()
                .header("Authorization", firstUserToken)
                .when()
                .get("/api/v1/accounts/%d/transactions".formatted(firstUserAccountId1))
                .then()
                .statusCode(200)
                .body("find { it.type == 'TRANSFER_OUT' }.amount", equalTo(transferAmount))
                .body("find { it.type == 'TRANSFER_OUT' }.relatedAccountId", equalTo(firstUserAccountId2));

        given()
                .header("Authorization", firstUserToken)
                .when()
                .get("/api/v1/accounts/%d/transactions".formatted(firstUserAccountId2))
                .then()
                .statusCode(200)
                .body("find { it.type == 'TRANSFER_IN' }.amount", equalTo(transferAmount))
                .body("find { it.type == 'TRANSFER_IN' }.relatedAccountId", equalTo(firstUserAccountId1));
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

    private void depositMoney(String userToken, int accountId, float amount) {
        given()
                .header("Authorization", userToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "id": %d,
                            "balance": %s
                        }
                        """.formatted(accountId, amount))
                .when()
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(200);
    }
}

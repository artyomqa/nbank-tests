package iteration2;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
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

        // Пополнение счета первого пользователя на 30000
        for (int i = 0; i < 6; i++) {
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
    @DisplayName("Перевод между своими счетами")
    @ValueSource(floats = {100f, 20.5f, 115.99f, 0.01f, 10000.00f})
    public void transferBetweenOwnAccountsTest(float amount) {
        // Получаем текущий баланс на счете-отправителе
        float senderInitialBalance = getAccountBalance(firstUserToken, firstUserAccountId1);

        // Получаем текущий баланс на счете-получателе
        float receiverInitialBalance = getAccountBalance(firstUserToken, firstUserAccountId2);

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
        float senderExpectedBalance = (float) Math.round((senderInitialBalance - amount) * 100) / 100;

        // Ожидаемый баланс счета-получаетля (округляем до 2 знаков после запятой, чтобы избежать неточностей)
        float receiverExpectedBalance = (float) Math.round((receiverInitialBalance + amount) * 100) / 100;

        assertEquals(senderExpectedBalance, senderFinalBalance);
        assertEquals(receiverExpectedBalance, receiverFinalBalance);
    }

    @ParameterizedTest
    @DisplayName("Перевод на счет другого пользователя")
    @ValueSource(floats = {100f, 20.5f, 115.99f, 0.01f, 10000.00f})
    public void transferToAnotherUsersAccountTest(float amount) {
        // Получаем текущий баланс на счете-отправителе
        float senderInitialBalance = getAccountBalance(firstUserToken, firstUserAccountId1);

        // Получаем текущий баланс на счете-получателе
        float receiverInitialBalance = getAccountBalance(secondUserToken, secondUserAccountId1);

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
        float senderExpectedBalance = (float) Math.round((senderInitialBalance - amount) * 100) / 100;

        // Ожидаемый баланс счета-получаетля (округляем до 2 знаков после запятой, чтобы избежать неточностей)
        float receiverExpectedBalance = (float) Math.round((receiverInitialBalance + amount) * 100) / 100;

        assertEquals(senderExpectedBalance, senderFinalBalance);
        assertEquals(receiverExpectedBalance, receiverFinalBalance);
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

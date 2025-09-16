package iteration2;

import io.restassured.http.ContentType;
import models.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DepositMoneyTest extends BaseTest {
    private static User firstUser;
    private static User secondUser;

    private static String firstUserToken;
    private static int firstUserId;
    private static int firstUserAccountId1;

    private static String secondUserToken;
    private static int secondUserId;
    private static int secondUserAccountId1;

    @BeforeAll
    public static void createUsersAndAccounts() {
        // Создание первого пользователя и получение токена
        firstUser = new User.Builder()
                .createRandomUser()
                .createFirstAccount()
                .build();

        firstUserToken = firstUser.token();
        firstUserId = firstUser.id();

        // Создание второго пользователя и получение токена
        secondUser = new User.Builder()
                .createRandomUser()
                .createFirstAccount()
                .build();

        secondUserToken = secondUser.token();
        secondUserId = secondUser.id();

        // Создание счета у первого пользователя
        firstUserAccountId1 = firstUser.firstAccountId();

        // Создание счета у второго пользователя
        secondUserAccountId1 = secondUser.firstAccountId();
    }

    // Удаляем юзеров после прохождения всех тестов
    @AfterAll
    public static void deleteUsers() {
        new DeleteUserRequester(RequestSpecs.authAsAdmin(), ResponseSpecs.returnsOk())
                .send(firstUser.id());

        new DeleteUserRequester(RequestSpecs.authAsAdmin(), ResponseSpecs.returnsOk())
                .send(secondUser.id());

//        given()
//                .header("Authorization", adminToken)
//                .when()
//                .delete("/api/v1/admin/users/" + firstUserId)
//                .then()
//                .statusCode(200);
//
//        given()
//                .header("Authorization", adminToken)
//                .when()
//                .delete("/api/v1/admin/users/" + secondUserId)
//                .then()
//                .statusCode(200);
    }


    @ParameterizedTest
    @DisplayName("Пополнение баланса (позитивные сценарии)")
    @ValueSource(floats = {100f, 115.99f, 20.5f, 0.01f, 5000.00f})
    public void depositMoneyPositiveTest(float amount) {
        // Получаем текущий баланс пользователя
        float currentBalance = firstUser.getFirstAccountBalance();

//        float currentBalance = getAccountBalance(firstUserToken, firstUserAccountId1);

        // Ожидаемый баланс после внесения депозита (округляем до 2 знаков после запятой, чтобы избежать неточностей)
        float expectedBalance = (float) Math.round((currentBalance + amount) * 100) / 100;

        // Пополняем баланс и проверяем, что в ответе получено корректное значение баланса
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(firstUser.firstAccountId())
                .balance(amount)
                .build();

        DepositMoneyResponse response = new DepositMoneyRequester(RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsOk())
                .send(request)
                .extract()
                .as(DepositMoneyResponse.class);

        softly.assertThat(response.getId()).isEqualTo(request.getId());
        softly.assertThat(response.getBalance()).isEqualTo(expectedBalance);


//        given()
//                .header("Authorization", firstUserToken)
//                .contentType(ContentType.JSON)
//                .body(generateRequestBody(firstUserAccountId1, amount))
//                .when()
//                .post("/api/v1/accounts/deposit")
//                .then()
//                .statusCode(200)
//                .body("id", equalTo(firstUserAccountId1))
//                .body("balance", equalTo(expectedBalance));

        // Проверяем, что баланс пользователя изменился
        float actualBalance = firstUser.getFirstAccountBalance();
        softly.assertThat(expectedBalance).isEqualTo(actualBalance);

//        float actualBalance = getAccountBalance(firstUserToken, firstUserAccountId1);
//        assertEquals(expectedBalance, actualBalance);
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

    @Test
    @DisplayName("Получение информации о депозите")
    public void getDepositInfoTest() {
        float depositAmount = 20.0f;
        int accountId = createBankAccount(firstUserToken);

        // Пополняем баланс
        given()
                .header("Authorization", firstUserToken)
                .contentType(ContentType.JSON)
                .body(generateRequestBody(accountId, depositAmount))
                .when()
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(200);

        // Проверяем, что на счете есть депозит на нужную сумму (amount) и он связан с этим счетом (relatedAccountId)
        given()
                .header("Authorization", firstUserToken)
                .when()
                .get("/api/v1/accounts/%d/transactions".formatted(accountId))
                .then()
                .statusCode(200)
                .body("find { it.type == 'DEPOSIT' }.amount", equalTo(depositAmount))
                .body("find { it.type == 'DEPOSIT' }.relatedAccountId", equalTo(accountId));
    }


    private String generateRequestBody(int accountId, Number balance) {
        return """
                {
                    "id": %d,
                    "balance": %s
                }
                """.formatted(accountId, balance);
    }
}

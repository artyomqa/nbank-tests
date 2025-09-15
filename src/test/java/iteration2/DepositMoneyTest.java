package iteration2;

import generators.RandomData;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import models.CreateUserRequest;
import models.User;
import models.UserRole;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.CreateUserRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DepositMoneyTest extends BaseTest {
    private static User firstUser;

    private static String firstUserToken;
    private static int firstUserId;
    private static int firstUserAccountId1;

    private static String secondUserToken;
    private static int secondUserId;
    private static int secondUserAccountId1;

    @BeforeAll
    public static void createUsersAndAccounts() {
        // Создание первого пользователя и получение токена --TODO
        CreateUserRequest createFirstUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        Response responseCreatedFirstUser = new CreateUserRequester(RequestSpecs.authAsAdmin(), ResponseSpecs.returnsCreated())
                .send(createFirstUserRequest)
                .extract()
                .response();

//        Response responseCreatedFirstUser = given()
//                .header("Authorization", adminToken)
//                .contentType(ContentType.JSON)
//                .body("""
//                        {
//                            "username": "Alex",
//                            "password": "%s",
//                            "role": "USER"
//                        }
//                        """.formatted(RandomData.getPassword()))
//                .when()
//                .post("/api/v1/admin/users")
//                .then()
//                .statusCode(201)
//                .extract()
//                .response();

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

        // Создание счета у второго пользователя
        secondUserAccountId1 = createBankAccount(secondUserToken);
    }

    // Удаляем юзеров после прохождения всех тестов
    @AfterAll
    public static void deleteUsers() {
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

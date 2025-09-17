package iteration2;

import generators.RandomData;
import models.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static org.assertj.core.api.Assertions.assertThat;

public class DepositMoneyTest extends BaseTest {
    private static User firstUser;
    private static User secondUser;

    @BeforeAll
    public static void createUsersAndAccounts() {
        // Создание первого пользователя и двух счетов
        firstUser = new User.Builder()
                .createRandomUser()
                .createFirstAccount()
                .createSecondAccount()
                .build();

        // Создание второго пользователя и счета
        secondUser = new User.Builder()
                .createRandomUser()
                .createFirstAccount()
                .build();
    }

    // Удаляем юзеров после прохождения всех тестов
    @AfterAll
    public static void deleteUsers() {
        new DeleteUserRequester(RequestSpecs.authAsAdmin(), ResponseSpecs.returnsOk())
                .send(firstUser.id());

        new DeleteUserRequester(RequestSpecs.authAsAdmin(), ResponseSpecs.returnsOk())
                .send(secondUser.id());
    }


    @ParameterizedTest
    @DisplayName("Пополнение баланса (позитивные сценарии)")
    @ValueSource(floats = {100f, 115.99f, 20.5f, 0.01f, 5000.00f})
    public void depositMoneyPositiveTest(float amount) {
        // Получаем текущий баланс пользователя
        float initialBalance = firstUser.getFirstAccountBalance();

        // Ожидаемый баланс после внесения депозита (округляем до 2 знаков после запятой, чтобы избежать неточностей)
        float expectedBalance = (float) Math.round((initialBalance + amount) * 100) / 100;

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

        // Проверяем, что баланс пользователя изменился
        float actualBalance = firstUser.getFirstAccountBalance();
        softly.assertThat(expectedBalance).isEqualTo(actualBalance);
    }

    @ParameterizedTest
    @DisplayName("Невалидные значения суммы")
    @ValueSource(floats = {0f, 5000.01f, -1f})
    public void depositWithInvalidAmountTest(float amount) {
        // Получаем текущий баланс пользователя
        float initialBalance = firstUser.getFirstAccountBalance();

        // Пытаемся пополнить баланс и проверяем, что в ответе статус-код 400
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(firstUser.firstAccountId())
                .balance(amount)
                .build();

        new DepositMoneyRequester(RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsBadRequest())
                .send(request);

        // Проверяем, что баланс пользователя не изменился
        float actualBalance = firstUser.getFirstAccountBalance();
        assertThat(actualBalance).isEqualTo(initialBalance);
    }

    @Test
    @DisplayName("Пополнение чужого баланса")
    public void depositToAnotherAccountTest() {
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(secondUser.firstAccountId())
                .balance(RandomData.getDepositAmount())
                .build();

        new DepositMoneyRequester(RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsForbidden())
                .send(request);
    }

    @Test
    @DisplayName("Пополнение баланса на несуществующем id счета")
    public void depositToInvalidAccountTest() {
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(firstUser.firstAccountId() + secondUser.firstAccountId())
                .balance(RandomData.getDepositAmount())
                .build();

        new DepositMoneyRequester(RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsForbidden())
                .send(request);
    }

    @Test
    @DisplayName("Пополнение баланса от имени администратора")
    public void depositByAdminTest() {
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(firstUser.firstAccountId())
                .balance(RandomData.getDepositAmount())
                .build();

        new DepositMoneyRequester(RequestSpecs.authAsAdmin(), ResponseSpecs.returnsForbidden())
                .send(request);
    }

    @Test
    @DisplayName("Пополнение баланса неавторизованным пользователем")
    public void depositByUnauthorizedUserTest() {
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(firstUser.firstAccountId())
                .balance(RandomData.getDepositAmount())
                .build();

        new DepositMoneyRequester(RequestSpecs.noAuth(), ResponseSpecs.returnsUnauthorized())
                .send(request);
    }

    @Test
    @DisplayName("Получение информации о депозите")
    public void getDepositInfoTest() {
        float depositAmount = RandomData.getDepositAmount();

        // Пополняем баланс
        new DepositMoneyRequester(RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsOk())
                .send(DepositMoneyRequest.builder().id(firstUser.secondAccountId()).balance(depositAmount).build());

        // Получаем список транзакций по счету
        Transaction[] transactions = new GetAccountTransactionsRequester(RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsOk())
                .send(firstUser.secondAccountId())
                .extract()
                .as(Transaction[].class);

        // Ищем нужную транзакцию
        Transaction currentTransaction = null;

        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("DEPOSIT") && transaction.getAmount() == depositAmount) {
                currentTransaction = transaction;
            }
        }

        // Проверяем, что депозит был найден, сумма (amount) корректная и он связан с этим счетом (relatedAccountId)
        assertThat(currentTransaction).isNotNull();
        softly.assertThat(depositAmount).isEqualTo(currentTransaction.getAmount());
        softly.assertThat(firstUser.secondAccountId()).isEqualTo(currentTransaction.getRelatedAccountId());
    }
}

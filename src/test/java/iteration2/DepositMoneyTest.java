package iteration2;

import generators.RandomData;
import models.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import steps.User;
import utils.TestUtils;

import java.util.List;

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
        firstUser.deleteUser();
        secondUser.deleteUser();
    }


    @ParameterizedTest
    @DisplayName("Пополнение баланса (позитивные сценарии)")
    @ValueSource(floats = {100f, 115.99f, 20.5f, 0.01f, 5000.00f})
    public void depositMoneyPositiveTest(float amount) {
        // Получаем текущий баланс пользователя
        float initialBalance = firstUser.getFirstAccountBalance();

        // Ожидаемый баланс после внесения депозита (округляем до 2 знаков после запятой, чтобы избежать неточностей)
        float expectedBalance = TestUtils.getCorrectAmount(initialBalance + amount);

        // Пополняем баланс
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(firstUser.firstAccountId())
                .balance(amount)
                .build();

        BankAccount response = new DepositMoneyRequester(RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsOk())
                .send(request)
                .extract()
                .as(BankAccount.class);

        softly.assertThat(response.getId()).isEqualTo(request.getId());
        softly.assertThat(response.getBalance()).isEqualTo(expectedBalance);

        // Проверяем, что баланс пользователя изменился
        float actualBalance = firstUser.getFirstAccountBalance();
        softly.assertThat(expectedBalance).isEqualTo(actualBalance);
    }

    @ParameterizedTest
    @DisplayName("Невалидные значения суммы")
    @ValueSource(floats = {0f, 5000.01f, -1f})
    public void userCannotDepositWithInvalidAmountTest(float amount) {
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
    public void userCannotDepositToAnotherAccountTest() {
        // Получаем текущий баланс другого пользователя
        float initialBalance = secondUser.getFirstAccountBalance();

        // Пытаемся пополнить баланс другого пользователя
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(secondUser.firstAccountId())
                .balance(RandomData.getAmount(MAX_DEPOSIT_AMOUNT))
                .build();

        new DepositMoneyRequester(RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsForbidden())
                .send(request);

        // Проверяем, что баланс пользователя не изменился
        float actualBalance = secondUser.getFirstAccountBalance();
        assertThat(actualBalance).isEqualTo(initialBalance);
    }

    @Test
    @DisplayName("Пополнение баланса на несуществующем id счета")
    public void userCannotDepositToInvalidAccountTest() {
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(firstUser.firstAccountId() + secondUser.firstAccountId())
                .balance(RandomData.getAmount(MAX_DEPOSIT_AMOUNT))
                .build();

        new DepositMoneyRequester(RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsForbidden())
                .send(request);
    }

    @Test
    @DisplayName("Пополнение баланса от имени администратора")
    public void adminCannotDepositTest() {
        // Получаем текущий баланс пользователя
        float initialBalance = firstUser.getFirstAccountBalance();

        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(firstUser.firstAccountId())
                .balance(RandomData.getAmount(MAX_DEPOSIT_AMOUNT))
                .build();

        new DepositMoneyRequester(RequestSpecs.authAsAdmin(), ResponseSpecs.returnsForbidden())
                .send(request);

        // Проверяем, что баланс пользователя не изменился
        float actualBalance = firstUser.getFirstAccountBalance();
        assertThat(actualBalance).isEqualTo(initialBalance);
    }

    @Test
    @DisplayName("Пополнение баланса неавторизованным пользователем")
    public void unauthorizedUserCannotDepositTest() {
        // Получаем текущий баланс пользователя
        float initialBalance = firstUser.getFirstAccountBalance();

        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(firstUser.firstAccountId())
                .balance(RandomData.getAmount(MAX_DEPOSIT_AMOUNT))
                .build();

        new DepositMoneyRequester(RequestSpecs.noAuth(), ResponseSpecs.returnsUnauthorized())
                .send(request);

        // Проверяем, что баланс пользователя не изменился
        float actualBalance = firstUser.getFirstAccountBalance();
        assertThat(actualBalance).isEqualTo(initialBalance);
    }

    @Test
    @DisplayName("Получение информации о депозите")
    public void getDepositInfoTest() {
        float depositAmount = RandomData.getAmount(MAX_DEPOSIT_AMOUNT);

        // Пополняем баланс
        new DepositMoneyRequester(RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsOk())
                .send(new DepositMoneyRequest(firstUser.secondAccountId(), depositAmount));

        // Получаем список транзакций по счету
        List<Transaction> transactions = new GetAccountTransactionsRequester(RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsOk())
                .send(firstUser.secondAccountId())
                .extract()
                .jsonPath()
                .getList("", Transaction.class);


        // Проверяем, что в списке есть депозит на эту сумму (amount) и он связан с этим счетом (relatedAccountId)
        softly.assertThat(transactions)
                .anyMatch(transaction ->
                        transaction.getType().equals("DEPOSIT") &&
                                transaction.getAmount() == depositAmount &&
                                transaction.getRelatedAccountId() == firstUser.secondAccountId());
    }
}

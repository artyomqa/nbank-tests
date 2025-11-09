package iteration2.api;

import api.db.dao.AccountDAO;
import api.db.dao.TransactionDAO;
import api.db.entities.TransactionEntity;
import api.generators.RandomData;
import api.models.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import api.requests.Endpoint;
import api.requests.requesters.ModelRequester;
import api.requests.requesters.ValidationRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.steps.User;
import api.utils.TestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DepositMoneyTest extends BaseAPITest {
    private User firstUser;
    private User secondUser;

    @BeforeEach
    public void createUsersAndAccounts() {
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
    @AfterEach
    public void deleteUsers() {
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
        BankAccount response = firstUser.depositFirstAccount(amount);

        softly.assertThat(response.getId()).isEqualTo(firstUser.firstAccountId());
        softly.assertThat(response.getBalance()).isEqualTo(expectedBalance);

        // Проверяем в API, что баланс пользователя изменился
        float apiBalance = firstUser.getFirstAccountBalance();
        softly.assertThat(apiBalance).isEqualTo(expectedBalance);

        // Проверяем в БД, что баланс пользователя изменился
        BigDecimal dbBalance = new AccountDAO().findByAccountId(firstUser.firstAccountId()).getBalance();
        softly.assertThat(dbBalance).isEqualTo(TestUtils.getCorrectBigDecimal(expectedBalance));
    }

    @ParameterizedTest
    @DisplayName("Невалидные значения суммы")
    @ValueSource(floats = {0f, 5000.01f, -1f})
    public void userCannotDepositWithInvalidAmountTest(float amount) {
        // Получаем текущий баланс пользователя
        float initialBalance = firstUser.getFirstAccountBalance();

        // Пытаемся пополнить баланс и проверяем, что в ответе статус-код 400
        new ValidationRequester(Endpoint.DEPOSIT_MONEY,
                RequestSpecs.authWithToken(firstUser.token()),
                ResponseSpecs.returnsBadRequest())
                .send(new DepositMoneyRequest(firstUser.firstAccountId(), amount));

        // Проверяем в API, что баланс пользователя не изменился
        float apiBalance = firstUser.getFirstAccountBalance();
        assertThat(apiBalance).isEqualTo(initialBalance);

        // Проверяем в БД, что баланс пользователя не изменился
        BigDecimal dbBalance = new AccountDAO().findByAccountId(firstUser.firstAccountId()).getBalance();
        softly.assertThat(dbBalance).isEqualTo(TestUtils.getCorrectBigDecimal(initialBalance));
    }

    @Test
    @DisplayName("Пополнение чужого баланса")
    public void userCannotDepositToAnotherAccountTest() {
        // Получаем текущий баланс другого пользователя
        float initialBalance = secondUser.getFirstAccountBalance();

        // Пытаемся пополнить баланс другого пользователя
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .accountId(secondUser.firstAccountId())
                .amount(RandomData.getAmount(MAX_DEPOSIT_AMOUNT))
                .build();

        new ValidationRequester(Endpoint.DEPOSIT_MONEY,
                RequestSpecs.authWithToken(firstUser.token()),
                ResponseSpecs.returnsForbidden())
                .send(request);

        // Проверяем в API, что баланс пользователя не изменился
        float apiBalance = secondUser.getFirstAccountBalance();
        assertThat(apiBalance).isEqualTo(initialBalance);

        // Проверяем в БД, что баланс пользователя не изменился
        BigDecimal dbBalance = new AccountDAO().findByAccountId(secondUser.firstAccountId()).getBalance();
        softly.assertThat(dbBalance).isEqualTo(TestUtils.getCorrectBigDecimal(initialBalance));
    }

    @Test
    @DisplayName("Пополнение баланса на несуществующем id счета")
    public void userCannotDepositToInvalidAccountTest() {
        int invalidAccountId = firstUser.firstAccountId() + secondUser.firstAccountId();

        // Проверяем в API, что счет не существует
        List<Integer> accountIds = new ModelRequester<UserProfiles>(Endpoint.GET_ALL_USERS, RequestSpecs.authAsAdmin(), ResponseSpecs.returnsOk())
                .send()
                .getUserProfiles()
                .stream()
                .flatMap(userProfile -> userProfile.getAccounts().stream())
                .map(BankAccount::getId)
                .toList();

        assertThat(accountIds).doesNotContain(invalidAccountId);

        // Проверяем в БД, что счет не существует
        assertThat(new AccountDAO().findByAccountId(invalidAccountId)).isNull();

        // Пытаемся пополнить несуществующий счет
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .accountId(invalidAccountId)
                .amount(RandomData.getAmount(MAX_DEPOSIT_AMOUNT))
                .build();

        new ValidationRequester(Endpoint.DEPOSIT_MONEY,
                RequestSpecs.authWithToken(firstUser.token()),
                ResponseSpecs.returnsForbidden())
                .send(request);
    }

    @Test
    @DisplayName("Пополнение баланса от имени администратора")
    public void adminCannotDepositTest() {
        // Получаем текущий баланс пользователя
        float initialBalance = firstUser.getFirstAccountBalance();

        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .accountId(firstUser.firstAccountId())
                .amount(RandomData.getAmount(MAX_DEPOSIT_AMOUNT))
                .build();

        new ValidationRequester(Endpoint.DEPOSIT_MONEY, RequestSpecs.authAsAdmin(), ResponseSpecs.returnsForbidden())
                .send(request);

        // Проверяем в API, что баланс пользователя не изменился
        float apiBalance = firstUser.getFirstAccountBalance();
        assertThat(apiBalance).isEqualTo(initialBalance);

        // Проверяем в БД, что баланс пользователя не изменился
        BigDecimal dbBalance = new AccountDAO().findByAccountId(firstUser.firstAccountId()).getBalance();
        softly.assertThat(dbBalance).isEqualTo(TestUtils.getCorrectBigDecimal(initialBalance));
    }

    @Test
    @DisplayName("Пополнение баланса неавторизованным пользователем")
    public void unauthorizedUserCannotDepositTest() {
        // Получаем текущий баланс пользователя
        float initialBalance = firstUser.getFirstAccountBalance();

        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .accountId(firstUser.firstAccountId())
                .amount(RandomData.getAmount(MAX_DEPOSIT_AMOUNT))
                .build();

        new ValidationRequester(Endpoint.DEPOSIT_MONEY, RequestSpecs.noAuth(), ResponseSpecs.returnsUnauthorized())
                .send(request);

        // Проверяем в API, что баланс пользователя не изменился
        float apiBalance = firstUser.getFirstAccountBalance();
        assertThat(apiBalance).isEqualTo(initialBalance);

        // Проверяем в БД, что баланс пользователя не изменился
        BigDecimal dbBalance = new AccountDAO().findByAccountId(firstUser.firstAccountId()).getBalance();
        softly.assertThat(dbBalance).isEqualTo(TestUtils.getCorrectBigDecimal(initialBalance));
    }

    @Test
    @DisplayName("Получение информации о депозите")
    public void getDepositInfoTest() {
        float depositAmount = RandomData.getAmount(MAX_DEPOSIT_AMOUNT);

        // Пополняем баланс
        firstUser.depositSecondAccount(depositAmount);

        // Получаем из API список транзакций по счету
        List<Transaction> apiTransactions = firstUser.getSecondAccountTransactions();

        // Проверяем в API, что в списке есть депозит на эту сумму (amount) и он связан с этим счетом (relatedAccountId)
        softly.assertThat(apiTransactions)
                .anyMatch(transaction ->
                        transaction.getType().equals(TransactionType.DEPOSIT.toString()) &&
                                transaction.getAmount() == depositAmount &&
                                transaction.getRelatedAccountId() == firstUser.secondAccountId());

        // Получаем из БД список транзакций по счету
        List<TransactionEntity> dbTransactions = new TransactionDAO().findAll();

        // Проверяем в БД, что в таблице есть депозит на эту сумму (amount) и он связан с этим счетом (relatedAccountId)
        softly.assertThat(dbTransactions)
                .anyMatch(transaction ->
                        transaction.getType().equals(TransactionType.DEPOSIT.toString()) &&
                        transaction.getAmount().compareTo(TestUtils.getCorrectBigDecimal(depositAmount)) == 0 &&
                        transaction.getRelatedAccountId() == firstUser.secondAccountId());
    }
}

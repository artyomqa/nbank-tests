package iteration2.api;

import api.generators.RandomData;
import api.models.*;
import api.requests.Endpoint;
import api.requests.requesters.ModelRequester;
import api.requests.requesters.ValidationRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import api.utils.TestUtils;
import api.utils.annotations.FraudCheckMock;
import api.utils.extensions.FraudCheckWireMockExtension;
import common.steps.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(FraudCheckWireMockExtension.class)
@FraudCheckMock
public class TransferWithFraudCheckTest extends BaseAPITest {
    private User firstUser;
    private User secondUser;

    // Перед запуском каждого теста создаем пользователей и счета
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

    // Удаляем юзеров после прохождения каждого теста
    @AfterEach
    public void deleteUsers() {
        firstUser.deleteUser();
        secondUser.deleteUser();
    }

    @ParameterizedTest
    @DisplayName("Перевод между своими счетами")
    @ValueSource(floats = {100f, 20.5f, 115.99f, 0.01f, 10000.00f})
    public void transferBetweenOwnAccountsTest(float amount) {
        // Пополняем счет первого пользователя на 10000
        firstUser.depositFirstAccount(5000f, 2);

        // Выполняем перевод
        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(firstUser.firstAccountId())
                .receiverAccountId(firstUser.secondAccountId())
                .amount(amount)
                .build();

        new ModelRequester<TransferFraudCheckResponse>(
                Endpoint.TRANSFER_FRAUD_CHECK,
                RequestSpecs.authWithToken(firstUser.token()),
                ResponseSpecs.successfulFraudCheck(request.getSenderAccountId(), request.getReceiverAccountId(), amount))
                .send(request);

        // Ожидаемый баланс счета-отправителя
        float senderExpectedBalance = TestUtils.getCorrectAmount(10000f - amount);

        // Проверяем баланс
        softly.assertThat(firstUser.getFirstAccountBalance()).isEqualTo(senderExpectedBalance);
        softly.assertThat(firstUser.getSecondAccountBalance()).isEqualTo(amount);
    }

    @ParameterizedTest
    @DisplayName("Перевод на счет другого пользователя")
    @ValueSource(floats = {100f, 20.5f, 115.99f, 0.01f, 10000.00f})
    public void transferToAnotherUsersAccountTest(float amount) {
        // Пополняем счет первого пользователя на 10000
        firstUser.depositFirstAccount(5000f, 2);

        // Выполняем перевод
        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(firstUser.firstAccountId())
                .receiverAccountId(secondUser.firstAccountId())
                .amount(amount)
                .build();

        new ModelRequester<TransferFraudCheckResponse>(
                Endpoint.TRANSFER_FRAUD_CHECK,
                RequestSpecs.authWithToken(firstUser.token()),
                ResponseSpecs.successfulFraudCheck(request.getSenderAccountId(), request.getReceiverAccountId(), amount))
                .send(request);

        // Ожидаемый баланс счета-отправителя
        float senderExpectedBalance = TestUtils.getCorrectAmount(10000f - amount);

        // Проверяем баланс
        softly.assertThat(firstUser.getFirstAccountBalance()).isEqualTo(senderExpectedBalance);
        softly.assertThat(secondUser.getFirstAccountBalance()).isEqualTo(amount);
    }

    @ParameterizedTest
    @DisplayName("Перевод невалидной суммы")
    @ValueSource(floats = {50.01f, 0, -1})
    public void userCannotTransferWithInvalidAmountTest(float amount) {
        float initialBalance = 50f;
        firstUser.depositFirstAccount(initialBalance);

        new ValidationRequester(Endpoint.TRANSFER_FRAUD_CHECK,
                RequestSpecs.authWithToken(firstUser.token()),
                ResponseSpecs.returnsBadRequest())
                .send(new TransferMoneyRequest(firstUser.firstAccountId(), secondUser.firstAccountId(), amount));

        // Проверяем, что балансы пользователей не изменились
        assertThat(firstUser.getFirstAccountBalance()).isEqualTo(initialBalance);
        assertThat(secondUser.getFirstAccountBalance()).isEqualTo(0);
    }

    @Test
    @DisplayName("Перевод с чужого счета")
    public void userCannotTransferFromElseAccountTest() {
        float amount = RandomData.getAmount(MAX_DEPOSIT_AMOUNT);
        secondUser.depositFirstAccount(amount);

        new ValidationRequester(Endpoint.TRANSFER_FRAUD_CHECK,
                RequestSpecs.authWithToken(firstUser.token()),
                ResponseSpecs.returnsForbidden())
                .send(new TransferMoneyRequest(secondUser.firstAccountId(), firstUser.firstAccountId(), amount));

        // Проверяем, что балансы пользователей не изменились
        assertThat(secondUser.getFirstAccountBalance()).isEqualTo(amount);
        assertThat(firstUser.getFirstAccountBalance()).isEqualTo(0);
    }

    @Test
    @DisplayName("Перевод с несуществующего счета")
    public void userCannotTransferFromInvalidAccountTest() {
        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(firstUser.firstAccountId() + secondUser.firstAccountId())
                .receiverAccountId(firstUser.firstAccountId())
                .amount(RandomData.getAmount(MAX_TRANSFER_AMOUNT))
                .build();

        new ValidationRequester(Endpoint.TRANSFER_FRAUD_CHECK,
                RequestSpecs.authWithToken(firstUser.token()),
                ResponseSpecs.returnsForbidden())
                .send(request);

        // Проверяем, что баланс получателя не изменился
        assertThat(firstUser.getFirstAccountBalance()).isEqualTo(0);
    }

    @Test
    @DisplayName("Перевод на несуществующий счет")
    public void userCannotTransferToInvalidAccountTest() {
        float amount = RandomData.getAmount(MAX_DEPOSIT_AMOUNT);
        firstUser.depositFirstAccount(amount);

        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(firstUser.firstAccountId())
                .receiverAccountId(firstUser.firstAccountId() + secondUser.firstAccountId())
                .amount(amount)
                .build();

        new ValidationRequester(Endpoint.TRANSFER_FRAUD_CHECK,
                RequestSpecs.authWithToken(firstUser.token()),
                ResponseSpecs.returnsBadRequest())
                .send(request);

        // Проверяем, что баланс отправителя не изменился
        assertThat(firstUser.getFirstAccountBalance()).isEqualTo(amount);
    }

    @Test
    @DisplayName("Перевод на тот же самый счет")
    @Disabled("Тест временно отключен. Есть дефект.")
    public void userCannotTransferToTheSameAccountTest() {
        float amount = RandomData.getAmount(MAX_DEPOSIT_AMOUNT);
        firstUser.depositFirstAccount(amount);

        new ValidationRequester(Endpoint.TRANSFER_FRAUD_CHECK,
                RequestSpecs.authWithToken(firstUser.token()),
                ResponseSpecs.returnsBadRequest())
                .send(new TransferMoneyRequest(firstUser.firstAccountId(), firstUser.firstAccountId(), amount));

        // Проверяем, что баланс пользователя не изменился
        assertThat(firstUser.getFirstAccountBalance()).isEqualTo(amount);
    }

    @Test
    @DisplayName("Перевод от имени администратора")
    public void adminCannotTransferTest() {
        float amount = RandomData.getAmount(MAX_DEPOSIT_AMOUNT);
        firstUser.depositFirstAccount(amount);

        new ValidationRequester(Endpoint.TRANSFER_FRAUD_CHECK, RequestSpecs.authAsAdmin(), ResponseSpecs.returnsForbidden())
                .send(new TransferMoneyRequest(firstUser.firstAccountId(), firstUser.secondAccountId(), amount));

        // Проверяем, что балансы пользователя не изменилсь
        assertThat(firstUser.getFirstAccountBalance()).isEqualTo(amount);
        assertThat(firstUser.getSecondAccountBalance()).isEqualTo(0);
    }

    @Test
    @DisplayName("Перевод неавторизованным пользователем")
    public void unauthorizedUserCannotTransferTest() {
        float amount = RandomData.getAmount(MAX_DEPOSIT_AMOUNT);
        firstUser.depositFirstAccount(amount);

        new ValidationRequester(Endpoint.TRANSFER_FRAUD_CHECK, RequestSpecs.noAuth(), ResponseSpecs.returnsUnauthorized())
                .send(new TransferMoneyRequest(firstUser.firstAccountId(), firstUser.secondAccountId(), amount));

        // Проверяем, что балансы пользователя не изменились
        assertThat(firstUser.getFirstAccountBalance()).isEqualTo(amount);
        assertThat(firstUser.getSecondAccountBalance()).isEqualTo(0);
    }

    @Test
    @DisplayName("Получение информации о транзакциях")
    public void getTransactionsInfoTest() {
        float transferAmount = RandomData.getAmount(MAX_DEPOSIT_AMOUNT);

        // Пополняем счет пользователя
        firstUser.depositFirstAccount(MAX_DEPOSIT_AMOUNT);

        // Выполняем перевод
        new ValidationRequester(Endpoint.TRANSFER_FRAUD_CHECK, RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsOk())
                .send(new TransferMoneyRequest(firstUser.firstAccountId(), firstUser.secondAccountId(), transferAmount));

        /*
        Проверяем:
        1) на счете-отправителе есть транзакция на нужную сумму (amount) и она связана со счетом-получателем (relatedAccountId)
        2) на счете-получателе есть транзакция на нужную сумму (amount) и она связана со счетом-отправителем (relatedAccountId)
         */
        List<Transaction> firstAccountTransactions = firstUser.getFirstAccountTransactions();

        softly.assertThat(firstAccountTransactions)
                .anyMatch(transaction ->
                        transaction.getType().equals(TransactionType.TRANSFER_OUT.toString()) &&
                                transaction.getAmount() == transferAmount &&
                                transaction.getRelatedAccountId() == firstUser.secondAccountId());

        List<Transaction> secondAccountTransactions = firstUser.getSecondAccountTransactions();

        softly.assertThat(secondAccountTransactions)
                .anyMatch(transaction ->
                        transaction.getType().equals(TransactionType.TRANSFER_IN.toString()) &&
                                transaction.getAmount() == transferAmount &&
                                transaction.getRelatedAccountId() == firstUser.firstAccountId());
    }
}

package iteration2;

import generators.RandomData;
import models.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.DeleteUserRequester;
import requests.GetAccountTransactionsRequester;
import requests.TransferMoneyRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import utils.TestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferMoneyTest extends BaseTest {
    private static User firstUser;
    private static User secondUser;

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
        new DeleteUserRequester(RequestSpecs.authAsAdmin(), ResponseSpecs.returnsOk())
                .send(firstUser.id());

        new DeleteUserRequester(RequestSpecs.authAsAdmin(), ResponseSpecs.returnsOk())
                .send(secondUser.id());
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

        TransferMoneyResponse response = new TransferMoneyRequester(
                RequestSpecs.authWithToken(firstUser.token()),
                ResponseSpecs.successfulTransfer())
                .send(request)
                .extract()
                .as(TransferMoneyResponse.class);

        softly.assertThat(response.getAmount()).isEqualTo(amount);
        softly.assertThat(response.getSenderAccountId()).isEqualTo(request.getSenderAccountId());
        softly.assertThat(response.getReceiverAccountId()).isEqualTo(request.getReceiverAccountId());

        // Ожидаемый баланс счета-отправителя (округляем до 2 знаков после запятой, чтобы избежать неточностей)
        float senderExpectedBalance = TestUtils.getCorrectAmount(10000f - amount);

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

        TransferMoneyResponse response = new TransferMoneyRequester(
                RequestSpecs.authWithToken(firstUser.token()),
                ResponseSpecs.successfulTransfer())
                .send(request)
                .extract()
                .as(TransferMoneyResponse.class);

        softly.assertThat(response.getAmount()).isEqualTo(amount);
        softly.assertThat(response.getSenderAccountId()).isEqualTo(request.getSenderAccountId());
        softly.assertThat(response.getReceiverAccountId()).isEqualTo(request.getReceiverAccountId());

        // Ожидаемый баланс счета-отправителя (округляем до 2 знаков после запятой, чтобы избежать неточностей)
        float senderExpectedBalance = TestUtils.getCorrectAmount(10000f - amount);

        softly.assertThat(firstUser.getFirstAccountBalance()).isEqualTo(senderExpectedBalance);
        softly.assertThat(secondUser.getFirstAccountBalance()).isEqualTo(amount);
    }

    @ParameterizedTest
    @DisplayName("Перевод невалидной суммы")
    @ValueSource(floats = {50.01f, 0, -1})
    public void userCannotTransferWithInvalidAmountTest(float amount) {
        float initialBalance = 50f;
        firstUser.depositFirstAccount(initialBalance);

        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(firstUser.firstAccountId())
                .receiverAccountId(secondUser.firstAccountId())
                .amount(amount)
                .build();

        new TransferMoneyRequester(RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsBadRequest())
                .send(request);

        // Проверяем, что балансы пользователей не изменились
        assertThat(firstUser.getFirstAccountBalance()).isEqualTo(initialBalance);
        assertThat(secondUser.getFirstAccountBalance()).isEqualTo(0);
    }

    @Test
    @DisplayName("Перевод с чужого счета")
    public void userCannotTransferFromElseAccountTest() {
        float amount = RandomData.getAmount(MAX_DEPOSIT_AMOUNT);
        secondUser.depositFirstAccount(amount);

        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(secondUser.firstAccountId())
                .receiverAccountId(firstUser.firstAccountId())
                .amount(amount)
                .build();

        new TransferMoneyRequester(RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsForbidden())
                .send(request);

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

        new TransferMoneyRequester(RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsForbidden())
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

        new TransferMoneyRequester(RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsBadRequest())
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

        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(firstUser.firstAccountId())
                .receiverAccountId(firstUser.firstAccountId())
                .amount(amount)
                .build();

        new TransferMoneyRequester(RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsBadRequest())
                .send(request);

        // Проверяем, что баланс пользователя не изменился
        assertThat(firstUser.getFirstAccountBalance()).isEqualTo(amount);
    }

    @Test
    @DisplayName("Перевод от имени администратора")
    public void adminCannotTransferTest() {
        float amount = RandomData.getAmount(MAX_DEPOSIT_AMOUNT);
        firstUser.depositFirstAccount(amount);

        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(firstUser.firstAccountId())
                .receiverAccountId(firstUser.secondAccountId())
                .amount(amount)
                .build();

        new TransferMoneyRequester(RequestSpecs.authAsAdmin(), ResponseSpecs.returnsForbidden())
                .send(request);

        // Проверяем, что балансы пользователя не изменилсь
        assertThat(firstUser.getFirstAccountBalance()).isEqualTo(amount);
        assertThat(firstUser.getSecondAccountBalance()).isEqualTo(0);
    }

    @Test
    @DisplayName("Перевод неавторизованным пользователем")
    public void unauthorizedUserCannotTransferTest() {
        float amount = RandomData.getAmount(MAX_DEPOSIT_AMOUNT);
        firstUser.depositFirstAccount(amount);

        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(firstUser.firstAccountId())
                .receiverAccountId(firstUser.secondAccountId())
                .amount(amount)
                .build();

        new TransferMoneyRequester(RequestSpecs.noAuth(), ResponseSpecs.returnsUnauthorized())
                .send(request);

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
        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(firstUser.firstAccountId())
                .receiverAccountId(firstUser.secondAccountId())
                .amount(transferAmount)
                .build();

        new TransferMoneyRequester(RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsOk())
                .send(request);

        /*
        Проверяем:
        1) на счете-отправителе есть транзакция на нужную сумму (amount) и она связана со счетом-получателем (relatedAccountId)
        2) на счете-получателе есть транзакция на нужную сумму (amount) и она связана со счетом-отправителем (relatedAccountId)
         */
        GetAccountTransactionsRequester getTransactionsRequester = new GetAccountTransactionsRequester(
                RequestSpecs.authWithToken(firstUser.token()),
                ResponseSpecs.returnsOk());

        List<Transaction> firstAccountTransactions = getTransactionsRequester.send(firstUser.firstAccountId())
                .extract()
                .jsonPath()
                .getList("", Transaction.class);

        softly.assertThat(firstAccountTransactions)
                .anyMatch(transaction ->
                        transaction.getType().equals("TRANSFER_OUT") &&
                                transaction.getAmount() == transferAmount &&
                                transaction.getRelatedAccountId() == firstUser.secondAccountId());

        List<Transaction> secondAccountTransactions = getTransactionsRequester.send(firstUser.secondAccountId())
                .extract()
                .jsonPath()
                .getList("", Transaction.class);

        softly.assertThat(secondAccountTransactions)
                .anyMatch(transaction ->
                        transaction.getType().equals("TRANSFER_IN") &&
                                transaction.getAmount() == transferAmount &&
                                transaction.getRelatedAccountId() == firstUser.firstAccountId());
    }
}

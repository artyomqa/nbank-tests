package iteration2;

import models.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.DeleteUserRequester;
import requests.GetAccountTransactionsRequester;
import requests.TransferMoneyRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

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
        for (int i = 0; i < 2; i++) {
            firstUser.depositFirstAccount(5000f);
        }

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
        float senderExpectedBalance = (float) Math.round((10000f - amount) * 100) / 100;

        softly.assertThat(firstUser.getFirstAccountBalance()).isEqualTo(senderExpectedBalance);
        softly.assertThat(firstUser.getSecondAccountBalance()).isEqualTo(amount);
    }

    @ParameterizedTest
    @DisplayName("Перевод на счет другого пользователя")
    @ValueSource(floats = {100f, 20.5f, 115.99f, 0.01f, 10000.00f})
    public void transferToAnotherUsersAccountTest(float amount) {
        // Пополняем счет первого пользователя на 10000
        for (int i = 0; i < 2; i++) {
            firstUser.depositFirstAccount(5000f);
        }

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
        float senderExpectedBalance = (float) Math.round((10000f - amount) * 100) / 100;

        softly.assertThat(firstUser.getFirstAccountBalance()).isEqualTo(senderExpectedBalance);
        softly.assertThat(secondUser.getFirstAccountBalance()).isEqualTo(amount);
    }

    @ParameterizedTest
    @DisplayName("Перевод невалидной суммы")
    @ValueSource(floats = {50.01f, 0, -1})
    public void transferInvalidAmountTest(float amount) {
        firstUser.depositFirstAccount(50f);

        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(firstUser.firstAccountId())
                .receiverAccountId(secondUser.firstAccountId())
                .amount(amount)
                .build();

        new TransferMoneyRequester(RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsBadRequest())
                .send(request);
    }

    @Test
    @DisplayName("Перевод с чужого счета")
    public void transferFromElseAccountTest() {
        secondUser.depositFirstAccount(50f);

        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(secondUser.firstAccountId())
                .receiverAccountId(firstUser.firstAccountId())
                .amount(10f)
                .build();

        new TransferMoneyRequester(RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsForbidden())
                .send(request);
    }

    @Test
    @DisplayName("Перевод с несуществующего счета")
    public void transferFromInvalidAccountTest() {
        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(firstUser.firstAccountId() + secondUser.firstAccountId())
                .receiverAccountId(firstUser.firstAccountId())
                .amount(10f)
                .build();

        new TransferMoneyRequester(RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsForbidden())
                .send(request);
    }

    @Test
    @DisplayName("Перевод на несуществующий счет")
    public void transferToInvalidAccountTest() {
        firstUser.depositFirstAccount(50f);

        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(firstUser.firstAccountId())
                .receiverAccountId(firstUser.firstAccountId() + secondUser.firstAccountId())
                .amount(10f)
                .build();

        new TransferMoneyRequester(RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsBadRequest())
                .send(request);
    }

    @Test
    @DisplayName("Перевод на тот же самый счет")
    @Disabled("Тест временно отключен. Есть дефект.")
    public void transferToTheSameAccountTest() {
        firstUser.depositFirstAccount(50f);

        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(firstUser.firstAccountId())
                .receiverAccountId(firstUser.firstAccountId())
                .amount(10f)
                .build();

        new TransferMoneyRequester(RequestSpecs.authWithToken(firstUser.token()), ResponseSpecs.returnsBadRequest())
                .send(request);
    }

    @Test
    @DisplayName("Перевод от имени администратора")
    public void transferMoneyByAdminTest() {
        firstUser.depositFirstAccount(50f);

        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(firstUser.firstAccountId())
                .receiverAccountId(firstUser.secondAccountId())
                .amount(10f)
                .build();

        new TransferMoneyRequester(RequestSpecs.authAsAdmin(), ResponseSpecs.returnsForbidden())
                .send(request);
    }

    @Test
    @DisplayName("Перевод неавторизованным пользователем")
    public void transferMoneyByUnauthorizedUserTest() {
        firstUser.depositFirstAccount(50f);

        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(firstUser.firstAccountId())
                .receiverAccountId(firstUser.secondAccountId())
                .amount(10f)
                .build();

        new TransferMoneyRequester(RequestSpecs.noAuth(), ResponseSpecs.returnsUnauthorized())
                .send(request);
    }

    @Test
    @DisplayName("Получение информации о транзакциях")
    public void getTransactionsInfoTest() {
        float transferAmount = 20.0f;

        // Пополняем счет пользователя
        firstUser.depositFirstAccount(50f);

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

        Transaction[] firstAccountTransactions = getTransactionsRequester.send(firstUser.firstAccountId())
                .extract()
                .as(Transaction[].class);

        Transaction transferOut = null;

        for (Transaction transaction : firstAccountTransactions) {
            if (transaction.getType().equals("TRANSFER_OUT") && transaction.getAmount() == transferAmount) {
                transferOut = transaction;
            }
        }

        assertThat(transferOut).isNotNull();
        softly.assertThat(transferOut.getRelatedAccountId()).isEqualTo(firstUser.secondAccountId());

        Transaction[] secondAccountTransactions = getTransactionsRequester.send(firstUser.secondAccountId())
                .extract()
                .as(Transaction[].class);

        Transaction transferIn = null;

        for (Transaction transaction : secondAccountTransactions) {
            if (transaction.getType().equals("TRANSFER_IN") && transaction.getAmount() == transferAmount) {
                transferIn = transaction;
            }
        }

        assertThat(transferIn).isNotNull();
        softly.assertThat(transferIn.getRelatedAccountId()).isEqualTo(firstUser.firstAccountId());
    }
}

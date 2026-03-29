package iteration2.ui;

import api.generators.RandomData;
import api.models.TransactionType;
import api.models.TransferMoneyRequest;
import api.models.TransferMoneyResponse;
import api.requests.Endpoint;
import api.requests.requesters.ModelRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import common.steps.User;
import api.utils.TestUtils;
import ui.elements.TransactionItem;
import ui.pages.BankAlert;
import ui.pages.TransferMoneyPage;
import ui.pages.UserDashboardPage;
import ui.utils.annotations.UserSession;
import ui.utils.storage.SessionStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferMoneyTest extends BaseUITest {
    private static User firstUser;
    private static User secondUser;

    @BeforeEach
    public void init() {
        firstUser = SessionStorage.getUser(1);
        secondUser = SessionStorage.getUser(2);

        // Пополняем счет отправителя
        firstUser.depositFirstAccount(MAX_DEPOSIT_AMOUNT, 2);

        // Задаем имя получателю
        secondUser.changeName(RandomData.getName());
    }

    // Удаляем юзеров после прохождения каждого теста
    @AfterEach
    public void deleteUsers() {
        firstUser.deleteUser();
        secondUser.deleteUser();
    }

    @Test
    @DisplayName("Успешный перевод")
    @UserSession(users = 2)
    public void userCanTransferMoneyTest() {
        String receiverName = secondUser.getProfile().getName();
        String receiverAccountNumber = secondUser.getFirstAccountNumber();
        float amount = RandomData.getAmount(MAX_TRANSFER_AMOUNT);

        new UserDashboardPage().open()
                .goToMakeTransferPage()
                .onPage(TransferMoneyPage.class)
                .transfer(firstUser.firstAccountId(), receiverName, receiverAccountNumber, amount, true)
                .checkAlertMessageAndAccept(BankAlert.SUCCESS_TRANSFER.format(amount, receiverAccountNumber))
                .shouldBeOpened();

        // Проверяем, что балансы пользователей изменились
        float expectedSenderBalance = TestUtils.getCorrectAmount(MAX_TRANSFER_AMOUNT - amount);
        assertThat(firstUser.getFirstAccountBalance()).isEqualTo(expectedSenderBalance);
        assertThat(secondUser.getFirstAccountBalance()).isEqualTo(amount);
    }

    @Test
    @DisplayName("Перевод на сумму, превышающую максимальную")
    @UserSession(users = 2)
    public void userCannotTransferWithGreaterThanMaximumAmountTest() {
        String receiverName = secondUser.getProfile().getName();
        String receiverAccountNumber = secondUser.getFirstAccountNumber();

        new UserDashboardPage().open()
                .goToMakeTransferPage()
                .onPage(TransferMoneyPage.class)
                .transfer(firstUser.firstAccountId(), receiverName, receiverAccountNumber, MAX_TRANSFER_AMOUNT + 0.01f, true)
                .checkAlertMessageAndAccept(BankAlert.MAX_TRANSFER_EXCEEDED.getMessage())
                .shouldBeOpened();

        // Проверяем, что балансы пользователей не изменились
        assertThat(firstUser.getFirstAccountBalance()).isEqualTo(MAX_TRANSFER_AMOUNT);
        assertThat(secondUser.getFirstAccountBalance()).isEqualTo(0);
    }

    @Test
    @DisplayName("Невалидная сумма при переводе")
    @UserSession(users = 2)
    public void userCannotTransferWithInvalidAmountTest() {
        String receiverName = secondUser.getProfile().getName();
        String receiverAccountNumber = secondUser.getFirstAccountNumber();

        new UserDashboardPage().open()
                .goToMakeTransferPage()
                .onPage(TransferMoneyPage.class)
                .transfer(firstUser.firstAccountId(), receiverName, receiverAccountNumber, 0f, true)
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_AMOUNT_LESS_MIN.getMessage())
                .shouldBeOpened();

        // Проверяем, что балансы пользователей не изменились
        assertThat(firstUser.getFirstAccountBalance()).isEqualTo(MAX_TRANSFER_AMOUNT);
        assertThat(secondUser.getFirstAccountBalance()).isEqualTo(0);
    }

    @Test
    @DisplayName("Не выбрано подтверждение корректности данных")
    @UserSession(users = 2)
    public void userCannotTransferWithoutConfirmationTest() {
        String receiverName = secondUser.getProfile().getName();
        String receiverAccountNumber = secondUser.getFirstAccountNumber();
        float amount = RandomData.getAmount(MAX_TRANSFER_AMOUNT);

        new UserDashboardPage().open()
                .goToMakeTransferPage()
                .onPage(TransferMoneyPage.class)
                .transfer(firstUser.firstAccountId(), receiverName, receiverAccountNumber, amount, false)
                .checkAlertMessageAndAccept(BankAlert.MISSING_REQUIRED_FIELD.getMessage())
                .shouldBeOpened();

        // Проверяем, что балансы пользователей не изменились
        assertThat(firstUser.getFirstAccountBalance()).isEqualTo(MAX_TRANSFER_AMOUNT);
        assertThat(secondUser.getFirstAccountBalance()).isEqualTo(0);
    }

    @Test
    @DisplayName("Получение списка транзакций")
    @UserSession(users = 2)
    public void userCanGetListOfTransactions() {
        float amount = RandomData.getAmount(MAX_TRANSFER_AMOUNT);

        // Выполняем перевод
        new ModelRequester<TransferMoneyResponse>(
                Endpoint.TRANSFER_MONEY,
                RequestSpecs.authWithToken(firstUser.token()),
                ResponseSpecs.successfulTransfer())
                .send(new TransferMoneyRequest(firstUser.firstAccountId(), secondUser.firstAccountId(), amount));

        List<TransactionItem> transactions = new UserDashboardPage().open()
                .goToMakeTransferPage()
                .onPage(TransferMoneyPage.class)
                .switchToTransferAgain()
                .getAllTransactions();

        // Проверяем, что в списке есть элемент, содержащий выполненный перевод
        assertThat(transactions)
                .anyMatch(item -> item.getType().equals(TransactionType.TRANSFER_OUT.toString()) && item.getAmount() == amount);
    }
}

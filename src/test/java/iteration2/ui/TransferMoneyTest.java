package iteration2.ui;

import api.generators.RandomData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import api.steps.User;
import api.utils.TestUtils;
import ui.pages.BankAlert;
import ui.pages.TransferMoneyPage;
import ui.pages.UserDashboardPage;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferMoneyTest extends BaseUITest {
    private static User firstUser;
    private static User secondUser;

    // Перед запуском каждого теста создаем пользователей и счета
    @BeforeEach
    public void createUsersAndOpenDashboardPage() {
        // Создание первого пользователя и счета
        firstUser = new User.Builder()
                .createRandomUser()
                .createFirstAccount()
                .build();

        // Пополняем счет отправителя
        firstUser.depositFirstAccount(MAX_DEPOSIT_AMOUNT, 2);

        // Создание второго пользователя и счета
        secondUser = new User.Builder()
                .createRandomUser()
                .createFirstAccount()
                .build();

        // Задаем имя получателю
        secondUser.changeName(RandomData.getName());

        // Авторизуемся как firstUser
        auth(firstUser);
    }

    // Удаляем юзеров после прохождения каждого теста
    @AfterEach
    public void deleteUsers() {
        firstUser.deleteUser();
        secondUser.deleteUser();
    }

    @Test
    @DisplayName("Успешный перевод")
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
    public void userCannotTransferWithGreaterThanMaximumAmountTest() {
        String receiverName = secondUser.getProfile().getName();
        String receiverAccountNumber = secondUser.getFirstAccountNumber();

        new UserDashboardPage().open()
                .goToMakeTransferPage()
                .onPage(TransferMoneyPage.class)
                .transfer(firstUser.firstAccountId(), receiverName, receiverAccountNumber, MAX_TRANSFER_AMOUNT + 0.01f, true)
                .checkAlertMessageAndAccept(BankAlert.INVALID_TRANSFER_AMOUNT.getMessage())
                .shouldBeOpened();

        // Проверяем, что балансы пользователей не изменились
        assertThat(firstUser.getFirstAccountBalance()).isEqualTo(MAX_TRANSFER_AMOUNT);
        assertThat(secondUser.getFirstAccountBalance()).isEqualTo(0);
    }

    @Test
    @DisplayName("Невалидная сумма при переводе")
    public void userCannotTransferWithInvalidAmountTest() {
        String receiverName = secondUser.getProfile().getName();
        String receiverAccountNumber = secondUser.getFirstAccountNumber();

        new UserDashboardPage().open()
                .goToMakeTransferPage()
                .onPage(TransferMoneyPage.class)
                .transfer(firstUser.firstAccountId(), receiverName, receiverAccountNumber, 0f, true)
                .checkAlertMessageAndAccept(BankAlert.INVALID_TRANSFER_AMOUNT.getMessage())
                .shouldBeOpened();

        // Проверяем, что балансы пользователей не изменились
        assertThat(firstUser.getFirstAccountBalance()).isEqualTo(MAX_TRANSFER_AMOUNT);
        assertThat(secondUser.getFirstAccountBalance()).isEqualTo(0);
    }

    @Test
    @DisplayName("Не выбрано подтверждение корректности данных")
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
}

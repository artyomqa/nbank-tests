package iteration2.ui;

import api.generators.RandomData;
import org.junit.jupiter.api.*;
import common.steps.User;
import api.utils.TestUtils;
import ui.pages.BankAlert;
import ui.pages.DepositMoneyPage;
import ui.pages.UserDashboardPage;
import ui.utils.annotations.UserSession;
import ui.utils.storage.SessionStorage;

import static org.assertj.core.api.Assertions.assertThat;

public class DepositMoneyTest extends BaseUITest {
    private User user;

    @BeforeEach
    public void init() {
        user = SessionStorage.getUser();
    }

    // Удаляем юзера после прохождения всех тестов
    @AfterEach
    public void deleteUser() {
        user.deleteUser();
    }

    @Test
    @DisplayName("Успешное пополнение баланса")
    @UserSession
    public void userCanDepositTest() {
        String accountNumber = user.getFirstAccountNumber();
        float initialBalance = user.getFirstAccountBalance();
        float amount = RandomData.getAmount(MAX_DEPOSIT_AMOUNT);

        new UserDashboardPage().open()
                .goToDepositMoneyPage()
                .onPage(DepositMoneyPage.class)
                .depositMoney(user.firstAccountId(), amount)
                .checkAlertMessageAndAccept(BankAlert.SUCCESS_DEPOSIT.format(amount, accountNumber))
                .onPage(UserDashboardPage.class);

        // Проверяем, что баланс пользователя изменился
        float expectedBalance = TestUtils.getCorrectAmount(initialBalance + amount);
        assertThat(user.getFirstAccountBalance()).isEqualTo(expectedBalance);
    }

    @Test
    @DisplayName("Пополнение баланса на сумму, превышающую максимальную")
    @UserSession
    public void userCannotDepositGreaterThanAllowedAmountTest() {
        // Получаем текущий баланс пользователя
        float initialBalance = user.getFirstAccountBalance();

        new UserDashboardPage().open()
                .goToDepositMoneyPage()
                .onPage(DepositMoneyPage.class)
                .depositMoney(user.firstAccountId(), MAX_DEPOSIT_AMOUNT + 0.01f)
                .checkAlertMessageAndAccept(BankAlert.MAX_DEPOSIT_EXCEEDED.getMessage())
                .onPage(DepositMoneyPage.class);

        // Проверяем, что баланс пользователя не изменился
        assertThat(user.getFirstAccountBalance()).isEqualTo(initialBalance);
    }

    @Test
    @DisplayName("Невалидная сумма при пополнении баланса")
    @UserSession
    public void userCannotDepositWithInvalidAmountTest() {
        // Получаем текущий баланс пользователя
        float initialBalance = user.getFirstAccountBalance();

        new UserDashboardPage().open()
                .goToDepositMoneyPage()
                .onPage(DepositMoneyPage.class)
                .depositMoney(user.firstAccountId(), 0f)
                .checkAlertMessageAndAccept(BankAlert.INVALID_DEPOSIT_AMOUNT.getMessage())
                .onPage(DepositMoneyPage.class);

        // Проверяем, что баланс пользователя не изменился
        assertThat(user.getFirstAccountBalance()).isEqualTo(initialBalance);
    }
}

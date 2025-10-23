package iteration2.ui;

import com.codeborne.selenide.Condition;
import api.generators.RandomData;
import org.junit.jupiter.api.*;
import org.openqa.selenium.Alert;
import api.steps.User;
import api.utils.TestUtils;
import ui.pages.BankAlert;
import ui.pages.DepositMoneyPage;
import ui.pages.UserDashboardPage;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositMoneyTest extends BaseUITest {
    private static User user;

    @BeforeAll
    public static void createUserAndAccount() {
        // Создание пользователя и счета
        user = new User.Builder()
                .createRandomUser()
                .createFirstAccount()
                .build();
    }

    @BeforeEach
    public void authAsUser() {
        auth(user);
    }

    // Удаляем юзера после прохождения всех тестов
    @AfterAll
    public static void deleteUser() {
        user.deleteUser();
    }

    @Test
    @DisplayName("Успешное пополнение баланса")
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
    public void userCannotDepositGreaterThanAllowedAmountTest() {
        // Получаем текущий баланс пользователя
        float initialBalance = user.getFirstAccountBalance();

        $$("button").findBy(Condition.text("Deposit Money")).click();

        // Проверям, что открыта страница Deposit Money
        $(".deposit-input").shouldBe(Condition.visible);

        $(".account-selector").selectOptionByValue(String.valueOf(user.firstAccountId()));

        $(".deposit-input").setValue(String.valueOf(MAX_DEPOSIT_AMOUNT + 0.01f));

        $$("button").findBy(Condition.text("Deposit")).click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("Please deposit less or equal to 5000$");
        alert.accept();

        // Проверям, что открыта страница Deposit Money
        $(".deposit-input").shouldBe(Condition.visible);

        // Проверяем, что баланс пользователя не изменился
        assertThat(user.getFirstAccountBalance()).isEqualTo(initialBalance);
    }

    @Test
    @DisplayName("Невалидная сумма при пополнении баланса")
    public void userCannotDepositWithInvalidAmountTest() {
        // Получаем текущий баланс пользователя
        float initialBalance = user.getFirstAccountBalance();

        $$("button").findBy(Condition.text("Deposit Money")).click();

        // Проверям, что открыта страница Deposit Money
        $(".deposit-input").shouldBe(Condition.visible);

        $(".account-selector").selectOptionByValue(String.valueOf(user.firstAccountId()));

        $(".deposit-input").setValue("0");

        $$("button").findBy(Condition.text("Deposit")).click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("Please enter a valid amount");
        alert.accept();

        // Проверям, что открыта страница Deposit Money
        $(".deposit-input").shouldBe(Condition.visible);

        // Проверяем, что баланс пользователя не изменился
        assertThat(user.getFirstAccountBalance()).isEqualTo(initialBalance);
    }
}

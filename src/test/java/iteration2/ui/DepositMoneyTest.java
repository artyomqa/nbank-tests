package iteration2.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import generators.RandomData;
import org.junit.jupiter.api.*;
import org.openqa.selenium.Alert;
import steps.User;
import utils.TestUtils;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositMoneyTest extends BaseUITest {
    private static User user;

    @BeforeAll
    public static void createUsersAndAccounts() {
        // Создание пользователя и счета
        user = new User.Builder()
                .createRandomUser()
                .createFirstAccount()
                .build();
    }

    @BeforeEach
    public void openDashboardPageAsUser() {
        // Открываем браузер и сохраняем токен в local storage
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", user.token());

        Selenide.open("/dashboard");
    }

    // Удаляем юзера после прохождения всех тестов
    @AfterAll
    public static void deleteUsers() {
        user.deleteUser();
    }

    @Test
    @DisplayName("Успешное пополнение баланса")
    public void userCanDepositTest() {
        String accountNumber = user.getFirstAccountNumber();
        float initialBalance = user.getFirstAccountBalance();

        $$("button").findBy(Condition.text("Deposit Money")).click();

        // Проверям, что открыта страница Deposit Money
        $(".deposit-input").shouldBe(Condition.visible);

        $(".account-selector").selectOptionByValue(String.valueOf(user.firstAccountId()));

        float amount = RandomData.getAmount(MAX_DEPOSIT_AMOUNT);
        $(".deposit-input").setValue(String.valueOf(amount));

        $$("button").findBy(Condition.text("Deposit")).click();

        Alert alert = switchTo().alert();
        String expectedMessage = String.format("Successfully deposited $%s to account %s!", amount, accountNumber);
        assertThat(alert.getText()).contains(expectedMessage);
        alert.accept();

        // Проверяем, что выполнен переход на страницу User Dashboard
        $(".welcome-text").shouldBe(Condition.visible);

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

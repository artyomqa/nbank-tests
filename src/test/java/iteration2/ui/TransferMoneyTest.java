package iteration2.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import api.generators.RandomData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import api.steps.User;
import api.utils.TestUtils;

import static com.codeborne.selenide.Selenide.*;
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

        // Открываем браузер и сохраняем токен в local storage
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", firstUser.token());

        Selenide.open("/dashboard");
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

        $$("button").findBy(Condition.text("Make a Transfer")).click();

        // Проверям, что открыта страница Make a Transfer
        $$("button").findBy(Condition.text("Send Transfer")).shouldBe(Condition.visible);

        $(".account-selector").selectOptionByValue(String.valueOf(firstUser.firstAccountId()));

        $("input[placeholder='Enter recipient name']").setValue(receiverName);

        $("input[placeholder='Enter recipient account number']").setValue(receiverAccountNumber);

        $("input[placeholder='Enter amount']").setValue(String.valueOf(amount));

        $("#confirmCheck").click();

        $$("button").findBy(Condition.text("Send Transfer")).click();

        Alert alert = switchTo().alert();
        String expectedAlertMessage = String.format("Successfully transferred $%s to account %s!", amount, receiverAccountNumber);
        assertThat(alert.getText()).contains(expectedAlertMessage);
        alert.accept();

        // Проверям, что открыта страница Make a Transfer
        $$("button").findBy(Condition.text("Send Transfer")).shouldBe(Condition.visible);

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

        $$("button").findBy(Condition.text("Make a Transfer")).click();

        // Проверям, что открыта страница Make a Transfer
        $$("button").findBy(Condition.text("Send Transfer")).shouldBe(Condition.visible);

        $(".account-selector").selectOptionByValue(String.valueOf(firstUser.firstAccountId()));

        $("input[placeholder='Enter recipient name']").setValue(receiverName);

        $("input[placeholder='Enter recipient account number']").setValue(receiverAccountNumber);

        $("input[placeholder='Enter amount']").setValue(String.valueOf(MAX_TRANSFER_AMOUNT + 0.01f));

        $("#confirmCheck").click();

        $$("button").findBy(Condition.text("Send Transfer")).click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("Error: Invalid transfer: insufficient funds or invalid accounts");
        alert.accept();

        // Проверям, что открыта страница Make a Transfer
        $$("button").findBy(Condition.text("Send Transfer")).shouldBe(Condition.visible);

        // Проверяем, что балансы пользователей не изменились
        assertThat(firstUser.getFirstAccountBalance()).isEqualTo(MAX_TRANSFER_AMOUNT);
        assertThat(secondUser.getFirstAccountBalance()).isEqualTo(0);
    }

    @Test
    @DisplayName("Невалидная сумма при переводе")
    public void userCannotTransferWithInvalidAmountTest() {
        String receiverName = secondUser.getProfile().getName();
        String receiverAccountNumber = secondUser.getFirstAccountNumber();

        $$("button").findBy(Condition.text("Make a Transfer")).click();

        // Проверям, что открыта страница Make a Transfer
        $$("button").findBy(Condition.text("Send Transfer")).shouldBe(Condition.visible);

        $(".account-selector").selectOptionByValue(String.valueOf(firstUser.firstAccountId()));

        $("input[placeholder='Enter recipient name']").setValue(receiverName);

        $("input[placeholder='Enter recipient account number']").setValue(receiverAccountNumber);

        $("input[placeholder='Enter amount']").setValue("0");

        $("#confirmCheck").click();

        $$("button").findBy(Condition.text("Send Transfer")).click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("Error: Invalid transfer: insufficient funds or invalid accounts");
        alert.accept();

        // Проверям, что открыта страница Make a Transfer
        $$("button").findBy(Condition.text("Send Transfer")).shouldBe(Condition.visible);

        // Проверяем, что балансы пользователей не изменились
        assertThat(firstUser.getFirstAccountBalance()).isEqualTo(MAX_TRANSFER_AMOUNT);
        assertThat(secondUser.getFirstAccountBalance()).isEqualTo(0);
    }

    @Test
    @DisplayName("Не выбрано подтверждение корректности данных")
    public void userCannotTransferWithoutConfirmationTest() {
        String receiverName = secondUser.getProfile().getName();
        String receiverAccountNumber = secondUser.getFirstAccountNumber();

        $$("button").findBy(Condition.text("Make a Transfer")).click();

        // Проверям, что открыта страница Make a Transfer
        $$("button").findBy(Condition.text("Send Transfer")).shouldBe(Condition.visible);

        $(".account-selector").selectOptionByValue(String.valueOf(firstUser.firstAccountId()));

        $("input[placeholder='Enter recipient name']").setValue(receiverName);

        $("input[placeholder='Enter recipient account number']").setValue(receiverAccountNumber);

        $("input[placeholder='Enter amount']").setValue(String.valueOf(RandomData.getAmount(MAX_TRANSFER_AMOUNT)));

        // Проверяем, что чекбокс снят
        $("#confirmCheck").shouldNotBe(Condition.selected);

        $$("button").findBy(Condition.text("Send Transfer")).click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("Please fill all fields and confirm");
        alert.accept();

        // Проверям, что открыта страница Make a Transfer
        $$("button").findBy(Condition.text("Send Transfer")).shouldBe(Condition.visible);

        // Проверяем, что балансы пользователей не изменились
        assertThat(firstUser.getFirstAccountBalance()).isEqualTo(MAX_TRANSFER_AMOUNT);
        assertThat(secondUser.getFirstAccountBalance()).isEqualTo(0);
    }
}

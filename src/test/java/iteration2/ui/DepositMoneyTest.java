package iteration2.ui;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import steps.User;

import static com.codeborne.selenide.Selenide.executeJavaScript;

public class DepositMoneyTest extends BaseTest {
    private static User user;

    @BeforeAll
    public static void createUsersAndAccounts() {
        // Создание пользователя и счета
        user = new User.Builder()
                .createRandomUser()
                .createFirstAccount()
                .build();
//        new ValidationRequester(Endpoint.LOGIN, RequestSpecs.authAsAdmin(), ResponseSpecs.returnsOk())
//                .send(new LoginRequest("admin", "admin"));
    }

    // Удаляем юзера после прохождения всех тестов
    @AfterAll
    public static void deleteUsers() {
        user.deleteUser();
    }

    @Test
    @DisplayName("Успешное пополнение баланса")
    public void userCanDepositTest() {
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", user.token());

        Selenide.open("/dashboard");
    }
}

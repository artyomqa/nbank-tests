package iteration2.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import api.generators.RandomData;
import org.junit.jupiter.api.*;
import org.openqa.selenium.Alert;
import api.steps.User;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangeUsernameTest extends BaseUITest {
    private static User user;

    // Перед запуском всех тестов создаем пользователя
    @BeforeAll
    public static void createUser() {
        user = new User.Builder()
                .createRandomUser()
                .build();
    }

    @BeforeEach
    public void openDashboardPageAsUser() {
        // Открываем браузер и сохраняем токен в local storage
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", user.token());

        Selenide.open("/dashboard");
    }

    // Удаляем пользователя после прохождения тестов
    @AfterAll
    public static void deleteUser() {
        user.deleteUser();
    }

    @Test
    @DisplayName("Успешное изменение имени")
    @Disabled("Тест временно отключен. Есть дефект.")
    public void userCanChangeNameTest() {
        String newName = RandomData.getName();

        $(".user-info").click();

        // Проверяем, что открыта страница Edit Profile
        $$("h1").findBy(Condition.text("Edit Profile")).shouldBe(Condition.visible);

        $("input[placeholder='Enter new name']").setValue(newName);

        $$("button").findBy(Condition.text("Save Changes")).click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("Name updated successfully!");
        alert.accept();

        // Проверяем, что открыта страница Edit Profile
        $$("h1").findBy(Condition.text("Edit Profile")).shouldBe(Condition.visible);

        $$("button").findBy(Condition.text("Home")).click();

        // Проверяем, что открыта страница User Dashboard
        $(".welcome-text").shouldBe(Condition.visible);

        // Проверяем, что на странице отображается новое имя (в 2 местах)
        $(".welcome-text").shouldHave(Condition.text("Welcome, " + newName));
        $(".user-name").shouldHave(Condition.text(newName));

        assertThat(user.getProfile().getName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("Установка невалидного имени")
    public void userCannotChangeToInvalidNameTest() {
        String initialName = user.getProfile().getName();
        String expectedNameOnUI = initialName == null ? "noname" : initialName;

        $(".user-info").click();

        // Проверяем, что открыта страница Edit Profile
        $$("h1").findBy(Condition.text("Edit Profile")).shouldBe(Condition.visible);

        $("input[placeholder='Enter new name']").setValue(RandomData.getUsername());

        $$("button").findBy(Condition.text("Save Changes")).click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("Name must contain two words with letters only");
        alert.accept();

        // Проверяем, что открыта страница Edit Profile
        $$("h1").findBy(Condition.text("Edit Profile")).shouldBe(Condition.visible);

        $$("button").findBy(Condition.text("Home")).click();

        // Проверяем, что открыта страница User Dashboard
        $(".welcome-text").shouldBe(Condition.visible);

        // Проверяем, что на странице отображается прежнее имя (в 2 местах)
        $(".welcome-text").shouldHave(Condition.text("Welcome, " + expectedNameOnUI));
        $(".user-name").shouldHave(Condition.text(expectedNameOnUI));

        // Проверяем, что имя не обновилось
        assertThat(user.getProfile().getName()).isEqualTo(initialName);
    }
}

package iteration2.ui;

import api.generators.RandomData;
import org.junit.jupiter.api.*;
import api.steps.User;
import ui.pages.BankAlert;
import ui.pages.EditProfilePage;
import ui.pages.UserDashboardPage;

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
    public void authAsUser() {
        auth(user);
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

        new UserDashboardPage().open()
                .goToEditProfilePage()
                .onPage(EditProfilePage.class)
                .changeName(newName)
                .checkAlertMessageAndAccept(BankAlert.SUCCESS_CHANGE_NAME.getMessage())
                .onPage(EditProfilePage.class)
                .goToHome()
                .onPage(UserDashboardPage.class)
                .checkUsernameEquals(newName);

        // Проверяем, что имя обновилось
        assertThat(user.getProfile().getName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("Установка невалидного имени")
    public void userCannotChangeToInvalidNameTest() {
        String initialName = user.getProfile().getName();
        String expectedNameOnUI = initialName == null ? "noname" : initialName;

        new UserDashboardPage().open()
                .goToEditProfilePage()
                .onPage(EditProfilePage.class)
                .changeName(RandomData.getUsername())
                .checkAlertMessageAndAccept(BankAlert.INVALID_NAME.getMessage())
                .onPage(EditProfilePage.class)
                .goToHome()
                .onPage(UserDashboardPage.class)
                .checkUsernameEquals(expectedNameOnUI);

        // Проверяем, что имя не обновилось
        assertThat(user.getProfile().getName()).isEqualTo(initialName);
    }
}

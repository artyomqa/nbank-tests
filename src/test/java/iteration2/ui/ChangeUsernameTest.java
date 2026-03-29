package iteration2.ui;

import api.generators.RandomData;
import org.junit.jupiter.api.*;
import common.steps.User;
import ui.pages.BankAlert;
import ui.pages.EditProfilePage;
import ui.pages.UserDashboardPage;
import ui.utils.annotations.Browsers;
import ui.utils.annotations.UserSession;
import ui.utils.storage.SessionStorage;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangeUsernameTest extends BaseUITest {
    private static User user;

    @BeforeEach
    public void init() {
        user = SessionStorage.getUser();
    }

    // Удаляем пользователя после прохождения тестов
    @AfterEach
    public void deleteUser() {
        user.deleteUser();
    }

    @Test
    @DisplayName("Успешное изменение имени")
    @Disabled("Тест временно отключен. Есть дефект.")
    @UserSession(userAccounts = 0)
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
    @UserSession(userAccounts = 0)
    @Browsers("chrome")
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

package iteration2;

import models.ChangeNameRequest;
import models.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.ChangeNameRequester;
import requests.DeleteUserRequester;
import requests.GetUserProfileRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class ChangeUsernameTest extends BaseTest {
    private static User user;

    // Перед запуском всех тестов создаем пользователя
    @BeforeAll
    public static void createUser() {
        user = new User.Builder()
                .createRandomUser()
                .build();
    }

    // Удаляем пользователя после прохождения тестов
    @AfterAll
    public static void deleteUser() {
        new DeleteUserRequester(RequestSpecs.authAsAdmin(), ResponseSpecs.returnsOk())
                .send(user.id());
    }


    @ParameterizedTest
    @DisplayName("Изменение имени (позитивные сценарии)")
    @ValueSource(strings = {"Petr Ivanov", "petr ivanov", "PETR IVANOV"})
    public void changeNamePositiveTest(String name) {
        // Изменяем имя
        new ChangeNameRequester(RequestSpecs.authWithToken(user.token()), ResponseSpecs.successfulChangeName(name))
                .send(new ChangeNameRequest(name));

        // Проверяем, что имя обновилось
        new GetUserProfileRequester(RequestSpecs.authWithToken(user.token()), ResponseSpecs.returnsOk())
                .send()
                .body("name", equalTo(name));
    }

    @ParameterizedTest
    @DisplayName("Изменение имени (некорректные имена)")
    @ValueSource(strings = {"Ivan", "Ivan Petrovich Ivanov", "Ivan  Petrovich", "Ivan Perovich1",
            "Ivan_Petrovich", "Ivan-Petrovich", "Ivan Petrovich.", "Иван Иванов", "   ", ""})
    public void changeNameNegativeTest(String name) {
        // Пытаемся изменить имя
        new ChangeNameRequester(RequestSpecs.authWithToken(user.token()), ResponseSpecs.returnsBadRequest())
                .send(new ChangeNameRequest(name));

        // Проверяем, что имя не обновилось
        new GetUserProfileRequester(RequestSpecs.authWithToken(user.token()), ResponseSpecs.returnsOk())
                .send()
                .body("name", not(equalTo(name)));
    }

    @Test
    @DisplayName("Изменение имени у администратора")
    public void changeAdminName() {
        new ChangeNameRequester(RequestSpecs.authAsAdmin(), ResponseSpecs.returnsForbidden())
                .send(new ChangeNameRequest("Petr Ivanov"));
    }
}

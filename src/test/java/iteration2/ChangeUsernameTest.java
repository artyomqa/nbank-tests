package iteration2;

import generators.RandomData;
import models.ChangeNameRequest;
import steps.User;
import models.UserProfile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.ChangeNameRequester;
import requests.GetUserProfileRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static org.assertj.core.api.Assertions.assertThat;

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
        user.deleteUser();
    }


    @ParameterizedTest
    @DisplayName("Изменение имени (позитивные сценарии)")
    @ValueSource(strings = {"Petr Ivanov", "petr ivanov", "PETR IVANOV"})
    public void changeNamePositiveTest(String name) {
        // Изменяем имя
        new ChangeNameRequester(RequestSpecs.authWithToken(user.token()), ResponseSpecs.successfulChangeName(name))
                .send(new ChangeNameRequest(name));

        // Проверяем, что имя обновилось
        UserProfile response = new GetUserProfileRequester(RequestSpecs.authWithToken(user.token()), ResponseSpecs.returnsOk())
                .send()
                .extract()
                .as(UserProfile.class);

        assertThat(response.getName()).isEqualTo(name);
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
        UserProfile response = new GetUserProfileRequester(RequestSpecs.authWithToken(user.token()), ResponseSpecs.returnsOk())
                .send()
                .extract()
                .as(UserProfile.class);

        assertThat(response.getName()).isNotEqualTo(name);
    }

    @Test
    @DisplayName("Изменение имени у администратора")
    public void adminCannotChangeNameTest() {
        new ChangeNameRequester(RequestSpecs.authAsAdmin(), ResponseSpecs.returnsForbidden())
                .send(new ChangeNameRequest(RandomData.getName()));
    }
}

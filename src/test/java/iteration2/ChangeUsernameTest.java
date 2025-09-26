package iteration2;

import generators.RandomModel;
import models.ChangeNameRequest;
import requests.Endpoint;
import requests.requesters.ValidationRequester;
import steps.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
        user.changeName(name);

        // Проверяем, что имя обновилось
        assertThat(user.getProfile().getName()).isEqualTo(name);
    }

    @ParameterizedTest
    @DisplayName("Изменение имени (некорректные имена)")
    @ValueSource(strings = {"Ivan", "Ivan Petrovich Ivanov", "Ivan  Petrovich", "Ivan Perovich1",
            "Ivan_Petrovich", "Ivan-Petrovich", "Ivan Petrovich.", "Иван Иванов", "   ", ""})
    public void changeNameNegativeTest(String name) {
        // Пытаемся изменить имя
        new ValidationRequester(Endpoint.CHANGE_NAME, RequestSpecs.authWithToken(user.token()), ResponseSpecs.returnsBadRequest())
                .send(new ChangeNameRequest(name));

        // Проверяем, что имя не обновилось
        assertThat(user.getProfile().getName()).isNotEqualTo(name);
    }

    @Test
    @DisplayName("Изменение имени у администратора")
    public void adminCannotChangeNameTest() {
        new ValidationRequester(Endpoint.CHANGE_NAME, RequestSpecs.authAsAdmin(), ResponseSpecs.returnsForbidden())
                .send(RandomModel.generate(ChangeNameRequest.class));
    }
}

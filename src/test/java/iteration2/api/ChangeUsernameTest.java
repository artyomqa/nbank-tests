package iteration2.api;

import api.db.dao.CustomerDAO;
import api.generators.RandomModel;
import api.models.ChangeNameRequest;
import api.requests.Endpoint;
import api.requests.requesters.ValidationRequester;
import common.steps.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangeUsernameTest extends BaseAPITest {
    private User user;

    // Перед запуском всех тестов создаем пользователя
    @BeforeEach
    public void createUser() {
        user = new User.Builder()
                .createRandomUser()
                .build();
    }

    // Удаляем пользователя после прохождения тестов
    @AfterEach
    public void deleteUser() {
        user.deleteUser();
    }


    @ParameterizedTest
    @DisplayName("Изменение имени (позитивные сценарии)")
    @ValueSource(strings = {"Petr Ivanov", "petr ivanov", "PETR IVANOV"})
    public void changeNamePositiveTest(String name) {
        // Изменяем имя
        user.changeName(name);

        // Проверяем в API, что имя обновилось
        assertThat(user.getProfile().getName()).isEqualTo(name);

        // Проверяем в БД, что имя обновилось
        String dbName = new CustomerDAO().findById(user.id()).getName();
        assertThat(dbName).isEqualTo(name);
    }

    @ParameterizedTest
    @DisplayName("Изменение имени (некорректные имена)")
    @ValueSource(strings = {"Ivan", "Ivan Petrovich Ivanov", "Ivan  Petrovich", "Ivan Perovich1",
            "Ivan_Petrovich", "Ivan-Petrovich", "Ivan Petrovich.", "Иван Иванов", "   ", ""})
    public void changeNameNegativeTest(String name) {
        // Пытаемся изменить имя
        new ValidationRequester(Endpoint.CHANGE_NAME, RequestSpecs.authWithToken(user.token()), ResponseSpecs.returnsBadRequest())
                .send(new ChangeNameRequest(name));

        // Проверяем в API, что имя не обновилось
        assertThat(user.getProfile().getName()).isNotEqualTo(name);

        // Проверяем в БД, что имя не обновилось
        String dbName = new CustomerDAO().findById(user.id()).getName();
        assertThat(dbName).isNotEqualTo(name);
    }

    @Test
    @DisplayName("Изменение имени у администратора")
    public void adminCannotChangeNameTest() {
        new ValidationRequester(Endpoint.CHANGE_NAME, RequestSpecs.authAsAdmin(), ResponseSpecs.returnsForbidden())
                .send(RandomModel.generate(ChangeNameRequest.class));
    }
}

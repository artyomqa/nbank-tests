package models;

/*
 Это не модель запроса/ответа, а класс для хранения данных о пользователе,
 которые используются в тестах.
*/

import io.restassured.response.Response;
import lombok.Setter;

public class User {
    private final String token;
    private final int id;
    @Setter
    private int firstAccountId;
    @Setter
    private int secondAccountId;

    public User(Response createUserResponse) {
        this.token = createUserResponse.header("Authorization");
        this.id = createUserResponse.jsonPath().getInt("id");
    }

    public String token() {
        return token;
    }

    public int id() {
        return id;
    }

    public int firstAccountId() {
        return firstAccountId;
    }

    public int secondAccountId() {
        return secondAccountId;
    }

}

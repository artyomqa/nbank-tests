package models;

import generators.RandomModel;
import io.restassured.response.Response;
import requests.skelethon.Endpoint;
import requests.skelethon.Requester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class User {
    private final String token;
    private final int id;
    private final int firstAccountId;
    private final int secondAccountId;

    private User(Builder builder) {
        this.token = builder.token;
        this.id = builder.id;
        this.firstAccountId = builder.firstAccountId;
        this.secondAccountId = builder.secondAccountId;
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

    public float getFirstAccountBalance() {
        return new Requester(Endpoint.GET_USER_ACCOUNTS, RequestSpecs.authWithToken(token), ResponseSpecs.returnsOk())
                .send()
                .extract()
                .path("find { it.id == %s }.balance".formatted(firstAccountId));
    }

    public float getSecondAccountBalance() {
        return new Requester(Endpoint.GET_USER_ACCOUNTS, RequestSpecs.authWithToken(token), ResponseSpecs.returnsOk())
                .send()
                .extract()
                .path("find { it.id == %s }.balance".formatted(secondAccountId));
    }

    public void depositFirstAccount(float amount) {
        new Requester(Endpoint.DEPOSIT_MONEY, RequestSpecs.authWithToken(token), ResponseSpecs.returnsOk())
                .send(new DepositMoneyRequest(firstAccountId, amount));
    }

    public void depositSecondAccount(float amount) {
        new Requester(Endpoint.DEPOSIT_MONEY, RequestSpecs.authWithToken(token), ResponseSpecs.returnsOk())
                .send(new DepositMoneyRequest(secondAccountId, amount));
    }

    public static class Builder {
        private String token;
        private int id;
        private int firstAccountId;
        private int secondAccountId;

        public Builder crateUser(String username, String password) {
            CreateUserRequest request = new CreateUserRequest(username, password, UserRole.USER.toString());

            Response response = new Requester(Endpoint.CREATE_USER, RequestSpecs.authAsAdmin(), ResponseSpecs.returnsCreated())
                    .send(request)
                    .extract()
                    .response();

            token = response.header("Authorization");
            id = response.jsonPath().getInt("id");

            return this;
        }

        public Builder createRandomUser() {
            CreateUserRequest request = RandomModel.generate(CreateUserRequest.class);

            Response response = new Requester(Endpoint.CREATE_USER, RequestSpecs.authAsAdmin(), ResponseSpecs.returnsCreated())
                    .send(request)
                    .extract()
                    .response();

            token = response.header("Authorization");
            id = response.jsonPath().getInt("id");

            return this;
        }

        public Builder createFirstAccount() {
            firstAccountId = new Requester(Endpoint.CREATE_BANK_ACCOUNT, RequestSpecs.authWithToken(token), ResponseSpecs.returnsCreated())
                    .send()
                    .extract()
                    .path("id");

            return this;
        }

        public Builder createSecondAccount() {
            secondAccountId = new Requester(Endpoint.CREATE_BANK_ACCOUNT, RequestSpecs.authWithToken(token), ResponseSpecs.returnsCreated())
                    .send()
                    .extract()
                    .path("id");

            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}

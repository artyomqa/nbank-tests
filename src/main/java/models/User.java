package models;

import generators.RandomData;
import io.restassured.response.Response;
import requests.CreateBankAccountRequester;
import requests.CreateUserRequester;
import requests.DepositMoneyRequester;
import requests.GetBalanceRequester;
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
        return new GetBalanceRequester(RequestSpecs.authWithToken(token), ResponseSpecs.returnsOk())
                .send()
                .extract()
                .path("find { it.id == %s }.balance".formatted(firstAccountId));
    }

    public float getSecondAccountBalance() {
        return new GetBalanceRequester(RequestSpecs.authWithToken(token), ResponseSpecs.returnsOk())
                .send()
                .extract()
                .path("find { it.id == %s }.balance".formatted(secondAccountId));
    }

    public void depositFirstAccount(float amount) {
        new DepositMoneyRequester(
                RequestSpecs.authWithToken(token),
                ResponseSpecs.returnsOk())
                .send(DepositMoneyRequest.builder().id(firstAccountId).balance(amount).build());
    }

    public void depositSecondAccount(float amount) {
        new DepositMoneyRequester(
                RequestSpecs.authWithToken(token),
                ResponseSpecs.returnsOk())
                .send(DepositMoneyRequest.builder().id(secondAccountId).balance(amount).build());
    }

    public static class Builder {
        private String token;
        private int id;
        private int firstAccountId;
        private int secondAccountId;

        public Builder crateUser(String username, String password) {
            CreateUserRequest request = CreateUserRequest.builder()
                    .username(username)
                    .password(password)
                    .role(UserRole.USER.toString())
                    .build();

            Response response = new CreateUserRequester(RequestSpecs.authAsAdmin(), ResponseSpecs.returnsCreated())
                    .send(request)
                    .extract()
                    .response();

            token = response.header("Authorization");
            id = response.jsonPath().getInt("id");

            return this;
        }

        public Builder createRandomUser() {
            CreateUserRequest request = CreateUserRequest.builder()
                    .username(RandomData.getUsername())
                    .password(RandomData.getPassword())
                    .role(UserRole.USER.toString())
                    .build();

            Response response = new CreateUserRequester(RequestSpecs.authAsAdmin(), ResponseSpecs.returnsCreated())
                    .send(request)
                    .extract()
                    .response();

            token = response.header("Authorization");
            id = response.jsonPath().getInt("id");

            return this;
        }

        public Builder createFirstAccount() {
            firstAccountId = new CreateBankAccountRequester(RequestSpecs.authWithToken(token), ResponseSpecs.returnsCreated())
                    .send()
                    .extract()
                    .path("id");

            return this;
        }

        public Builder createSecondAccount() {
            secondAccountId = new CreateBankAccountRequester(RequestSpecs.authWithToken(token), ResponseSpecs.returnsCreated())
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

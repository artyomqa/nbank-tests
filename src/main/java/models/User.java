package models;

import generators.RandomModel;
import io.restassured.response.Response;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ModelRequester;
import requests.skelethon.requesters.ValidationRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;

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
        List<BankAccount> userAccounts = new ModelRequester<BankAccounts>(
                Endpoint.GET_USER_ACCOUNTS,
                RequestSpecs.authWithToken(token),
                ResponseSpecs.returnsOk())
                .send()
                .getAccounts();

        for (BankAccount account : userAccounts) {
            if (account.getId() == firstAccountId) {
                return account.getBalance();
            }
        }

        throw new AssertionError("Ошибка при получении баланса. Счет с id = " + firstAccountId + " не найден.");
    }

    public float getSecondAccountBalance() {
        List<BankAccount> userAccounts = new ModelRequester<BankAccounts>(
                Endpoint.GET_USER_ACCOUNTS,
                RequestSpecs.authWithToken(token),
                ResponseSpecs.returnsOk())
                .send()
                .getAccounts();

        for (BankAccount account : userAccounts) {
            if (account.getId() == secondAccountId) {
                return account.getBalance();
            }
        }

        throw new AssertionError("Ошибка при получении баланса. Счет с id = " + secondAccountId + " не найден.");
    }

    public void depositFirstAccount(float amount) {
        new ValidationRequester(Endpoint.DEPOSIT_MONEY, RequestSpecs.authWithToken(token), ResponseSpecs.returnsOk())
                .send(new DepositMoneyRequest(firstAccountId, amount));
    }

    public void depositFirstAccount(float amount, int repeat) {
        ValidationRequester requester = new ValidationRequester(Endpoint.DEPOSIT_MONEY, RequestSpecs.authWithToken(token), ResponseSpecs.returnsOk());

        for (int i = 0; i < repeat; i++) {
            requester.send(new DepositMoneyRequest(firstAccountId, amount));
        }
    }

    public void depositSecondAccount(float amount) {
        new ValidationRequester(Endpoint.DEPOSIT_MONEY, RequestSpecs.authWithToken(token), ResponseSpecs.returnsOk())
                .send(new DepositMoneyRequest(secondAccountId, amount));
    }

    public void depositSecondAccount(float amount, int repeat) {
        ValidationRequester requester = new ValidationRequester(Endpoint.DEPOSIT_MONEY, RequestSpecs.authWithToken(token), ResponseSpecs.returnsOk());

        for (int i = 0; i < repeat; i++) {
            requester.send(new DepositMoneyRequest(secondAccountId, amount));
        }
    }

    public static class Builder {
        private String token;
        private int id;
        private int firstAccountId;
        private int secondAccountId;

        public Builder crateUser(String username, String password) {
            CreateUserRequest request = new CreateUserRequest(username, password, UserRole.USER.toString());

            Response response = new ValidationRequester(Endpoint.CREATE_USER, RequestSpecs.authAsAdmin(), ResponseSpecs.returnsCreated())
                    .send(request)
                    .extract()
                    .response();

            token = response.header("Authorization");
            id = response.jsonPath().getInt("id");

            return this;
        }

        public Builder createRandomUser() {
            CreateUserRequest request = RandomModel.generate(CreateUserRequest.class);

            Response response = new ValidationRequester(Endpoint.CREATE_USER, RequestSpecs.authAsAdmin(), ResponseSpecs.returnsCreated())
                    .send(request)
                    .extract()
                    .response();

            token = response.header("Authorization");
            id = response.jsonPath().getInt("id");

            return this;
        }

        public Builder createFirstAccount() {
            firstAccountId = new ValidationRequester(Endpoint.CREATE_BANK_ACCOUNT, RequestSpecs.authWithToken(token), ResponseSpecs.returnsCreated())
                    .send()
                    .extract()
                    .as(BankAccount.class)
                    .getId();

            return this;
        }

        public Builder createSecondAccount() {
            secondAccountId = new ValidationRequester(Endpoint.CREATE_BANK_ACCOUNT, RequestSpecs.authWithToken(token), ResponseSpecs.returnsCreated())
                    .send()
                    .extract()
                    .as(BankAccount.class)
                    .getId();

            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}

package common.steps;

import api.generators.RandomModel;
import api.models.*;
import api.requests.requesters.ModelRequester;
import api.requests.requesters.ValidationRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import io.restassured.response.Response;
import org.openqa.selenium.NoSuchElementException;
import api.requests.Endpoint;

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

    public String getFirstAccountNumber() {
        return this.getProfile()
                .getAccounts()
                .stream()
                .filter(acc -> acc.getId() == firstAccountId)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Счет с id: " + firstAccountId + " не найден!"))
                .getAccountNumber();
    }

    public String getSecondAccountNumber() {
        return this.getProfile()
                .getAccounts()
                .stream()
                .filter(acc -> acc.getId() == secondAccountId)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Счет с id: " + secondAccountId + " не найден!"))
                .getAccountNumber();
    }

    public List<Transaction> getFirstAccountTransactions() {
        return new ModelRequester<Transactions>(Endpoint.GET_ACCOUNT_TRANSACTIONS, RequestSpecs.authWithToken(token), ResponseSpecs.returnsOk())
                .send(firstAccountId)
                .getTransactions();
    }

    public List<Transaction> getSecondAccountTransactions() {
        return new ModelRequester<Transactions>(Endpoint.GET_ACCOUNT_TRANSACTIONS, RequestSpecs.authWithToken(token), ResponseSpecs.returnsOk())
                .send(secondAccountId)
                .getTransactions();
    }

    public UserProfile getProfile() {
        return new ModelRequester<UserProfile>(Endpoint.GET_USER_PROFILE, RequestSpecs.authWithToken(token), ResponseSpecs.returnsOk())
                .send();
    }

    public BankAccount depositFirstAccount(float amount) {
        return new ModelRequester<BankAccount>(Endpoint.DEPOSIT_MONEY, RequestSpecs.authWithToken(token), ResponseSpecs.returnsOk())
                .send(new DepositMoneyRequest(firstAccountId, amount));
    }

    public void depositFirstAccount(float amount, int repeat) {
        ValidationRequester requester = new ValidationRequester(Endpoint.DEPOSIT_MONEY, RequestSpecs.authWithToken(token), ResponseSpecs.returnsOk());

        for (int i = 0; i < repeat; i++) {
            requester.send(new DepositMoneyRequest(firstAccountId, amount));
        }
    }

    public BankAccount depositSecondAccount(float amount) {
        return new ModelRequester<BankAccount>(Endpoint.DEPOSIT_MONEY, RequestSpecs.authWithToken(token), ResponseSpecs.returnsOk())
                .send(new DepositMoneyRequest(secondAccountId, amount));
    }

    public void depositSecondAccount(float amount, int repeat) {
        ValidationRequester requester = new ValidationRequester(Endpoint.DEPOSIT_MONEY, RequestSpecs.authWithToken(token), ResponseSpecs.returnsOk());

        for (int i = 0; i < repeat; i++) {
            requester.send(new DepositMoneyRequest(secondAccountId, amount));
        }
    }

    public void changeName(String newName) {
        new ModelRequester<ChangeNameResponse>(Endpoint.CHANGE_NAME,
                RequestSpecs.authWithToken(token),
                ResponseSpecs.successfulChangeName(newName))
                .send(new ChangeNameRequest(newName));
    }

    public void deleteUser() {
        new ValidationRequester(Endpoint.DELETE_USER, RequestSpecs.authAsAdmin(), ResponseSpecs.returnsOk())
                .send(id);
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

        public Builder createAccounts(int count) {
            if (count >= 1) createFirstAccount();
            if (count >= 2) createSecondAccount();
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}

package requests.skelethon;

import io.restassured.http.Method;
import lombok.AllArgsConstructor;
import lombok.Getter;
import models.*;

@AllArgsConstructor
@Getter
public enum Endpoint {
    CREATE_USER(Method.POST, "/admin/users", CreateUserRequest.class, UserProfile.class),
    CREATE_BANK_ACCOUNT(Method.POST, "/accounts", null, BankAccount.class),
    GET_USER_ACCOUNTS(Method.GET, "/customer/accounts", null, BankAccounts.class),
    DEPOSIT_MONEY(Method.POST, "/accounts/deposit", DepositMoneyRequest.class, BankAccount.class);

    private final Method method;
    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}

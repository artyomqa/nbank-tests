package requests.skelethon;

import io.restassured.http.Method;
import lombok.AllArgsConstructor;
import lombok.Getter;
import models.BaseModel;
import models.CreateBankAccountResponse;
import models.CreateUserRequest;
import models.CreateUserResponse;

@AllArgsConstructor
@Getter
public enum Endpoint {
    CREATE_USER(Method.POST, "/admin/users", CreateUserRequest.class, CreateUserResponse.class),
    CREATE_BANK_ACCOUNT(Method.POST, "/accounts", null, CreateBankAccountResponse.class);

    private final Method method;
    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}

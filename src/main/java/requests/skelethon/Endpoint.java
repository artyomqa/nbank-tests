package requests.skelethon;

import io.restassured.http.Method;
import lombok.AllArgsConstructor;
import lombok.Getter;
import models.BaseModel;
import models.CreateUserRequest;

@AllArgsConstructor
@Getter
public enum Endpoint {
    CREATE_USER(Method.POST, "/api/v1/admin/users", CreateUserRequest.class, null);

    private final Method method;
    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}

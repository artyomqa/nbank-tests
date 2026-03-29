package api.specs;

import common.configs.Config;
import api.models.LoginRequest;
import api.utils.HttpLoggingFilter;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import api.requests.Endpoint;
import api.requests.requesters.ValidationRequester;

public class RequestSpecs {
    private static String adminToken;

    private RequestSpecs() {}

    private static RequestSpecBuilder defaultRequestBuilder() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new HttpLoggingFilter());
    }

    public static RequestSpecification noAuth() {
        return defaultRequestBuilder().build();
    }

    public static RequestSpecification authAsAdmin() {
        if (adminToken == null) {
            adminToken = new ValidationRequester(Endpoint.LOGIN, RequestSpecs.noAuth(), ResponseSpecs.returnsOk())
                    .send(new LoginRequest(Config.getString("admin.username"), Config.getString("admin.password")))
                    .extract()
                    .header("Authorization");
        }

        return defaultRequestBuilder()
                .addHeader("Authorization", adminToken)
                .build();
    }

    public static RequestSpecification authWithToken(String userToken) {
        return defaultRequestBuilder()
                .addHeader("Authorization", userToken)
                .build();
    }
}

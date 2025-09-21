package specs;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import models.LoginRequest;
import requests.LoginRequester;
import utils.HttpLoggingFilter;

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
            adminToken = new LoginRequester(RequestSpecs.noAuth(), ResponseSpecs.returnsOk())
                    .send(LoginRequest.builder().username("admin").password("admin").build())
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

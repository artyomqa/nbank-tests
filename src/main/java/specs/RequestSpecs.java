package specs;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import models.LoginRequest;
import requests.LoginRequester;

public class RequestSpecs {
    private RequestSpecs() {};

    private static RequestSpecBuilder defaultRequestBuilder() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON);
    }

    public static RequestSpecification unAuth() {
        return defaultRequestBuilder().build();
    }

    public static RequestSpecification authAsAdmin() {
        String adminToken = new LoginRequester(RequestSpecs.unAuth(), ResponseSpecs.returnsOk())
                .send(LoginRequest.builder().username("admin").password("admin").build())
                .extract()
                .header("Authorization");

        return defaultRequestBuilder()
                .addHeader("Authorization", adminToken)
                .build();
    }
}

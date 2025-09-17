package specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

public class ResponseSpecs {
    private ResponseSpecs() {}

    private static ResponseSpecBuilder defaultResponseBuilder() {
        return new ResponseSpecBuilder();
    }

    public static ResponseSpecification returnsOk() {
        return defaultResponseBuilder()
                .expectStatusCode(200)
                .build();
    }

    public static ResponseSpecification returnsCreated() {
        return defaultResponseBuilder()
                .expectStatusCode(201)
                .build();
    }

    public static ResponseSpecification returnsBadRequest() {
        return defaultResponseBuilder()
                .expectStatusCode(400)
                .build();
    }

    public static ResponseSpecification returnsUnauthorized() {
        return defaultResponseBuilder()
                .expectStatusCode(401)
                .build();
    }

    public static ResponseSpecification returnsForbidden() {
        return defaultResponseBuilder()
                .expectStatusCode(403)
                .build();
    }
}

package specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

public class ResponseSpecs {
    private ResponseSpecs() {};

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
}

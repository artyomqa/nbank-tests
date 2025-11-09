package api.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

import static org.hamcrest.Matchers.equalTo;

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

    public static ResponseSpecification successfulTransfer() {
        return defaultResponseBuilder()
                .expectStatusCode(200)
                .expectBody("message", equalTo("Transfer successful"))
                .build();
    }

    public static ResponseSpecification successfulFraudCheck(int senderAccountId, int receiverAccountId, float amount) {
        return defaultResponseBuilder()
                .expectStatusCode(200)
                .expectBody("senderAccountId", equalTo(senderAccountId))
                .expectBody("receiverAccountId", equalTo(receiverAccountId))
                .expectBody("amount", equalTo(amount))
                .expectBody("status", equalTo("APPROVED"))
                .expectBody("message", equalTo("Transfer approved and processed immediately"))
                .expectBody("fraudReason", equalTo("Low risk transaction"))
                .expectBody("requiresVerification", equalTo(false))
                .expectBody("requiresManualReview", equalTo(false))
                .build();
    }

    public static ResponseSpecification successfulChangeName(String newName) {
        return defaultResponseBuilder()
                .expectStatusCode(200)
                .expectBody("customer.name", equalTo(newName))
                .expectBody("message", equalTo("Profile updated successfully"))
                .build();
    }
}

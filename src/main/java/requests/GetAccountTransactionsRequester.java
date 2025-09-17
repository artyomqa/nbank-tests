package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

public class GetAccountTransactionsRequester extends Requester {
    public GetAccountTransactionsRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    public ValidatableResponse send(int accountId) {
        return given()
                .spec(requestSpecification)
                .get("/api/v1/accounts/%d/transactions".formatted(accountId))
                .then()
                .spec(responseSpecification);
    }
}

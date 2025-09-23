package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.DepositMoneyRequest;

import static io.restassured.RestAssured.given;

public class DepositMoneyRequester extends Requester {
    public DepositMoneyRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    public ValidatableResponse send(DepositMoneyRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("/api/v1/accounts/deposit")
                .then()
                .spec(responseSpecification);
    }
}

package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

public class CreateBankAccountRequester extends Requester {
    public CreateBankAccountRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    public ValidatableResponse send() {
        return given()
                .spec(requestSpecification)
                .post("/api/v1/accounts")
                .then()
                .spec(responseSpecification);
    }
}

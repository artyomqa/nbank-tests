package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.LoginRequest;

import static io.restassured.RestAssured.given;

public class LoginRequester extends Requester {
    public LoginRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    public ValidatableResponse send(LoginRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("/api/v1/auth/login")
                .then()
                .spec(responseSpecification);
    }
}

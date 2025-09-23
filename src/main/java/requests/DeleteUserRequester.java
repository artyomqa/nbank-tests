package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

public class DeleteUserRequester extends Requester {
    public DeleteUserRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    public ValidatableResponse send(int userId) {
        return given()
                .spec(requestSpecification)
                .delete("/api/v1/admin/users/" + userId)
                .then()
                .spec(responseSpecification);
    }
}

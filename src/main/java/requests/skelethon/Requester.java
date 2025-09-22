package requests.skelethon;

import configs.Config;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;

import static io.restassured.RestAssured.given;

public class Requester {
    private final Endpoint endpoint;
    private final RequestSpecification requestSpecification;
    private final ResponseSpecification responseSpecification;
    private final String apiVersion = Config.getString("apiVersion");

    public Requester(Endpoint endpoint, RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        this.endpoint = endpoint;
        this.requestSpecification = requestSpecification;
        this.responseSpecification = responseSpecification;
    }

    public ValidatableResponse send() {
        return given()
                .spec(requestSpecification)
                .request(endpoint.getMethod(), apiVersion + endpoint.getUrl())
                .then()
                .spec(responseSpecification);
    }

    public ValidatableResponse send(BaseModel model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .request(endpoint.getMethod(), apiVersion + endpoint.getUrl())
                .then()
                .spec(responseSpecification);
    }
}

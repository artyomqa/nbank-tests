package requests.skelethon;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;

import static io.restassured.RestAssured.given;

public class Requester {
    Endpoint endpoint;
    protected RequestSpecification requestSpecification;
    protected ResponseSpecification responseSpecification;

    public Requester(Endpoint endpoint, RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        this.endpoint = endpoint;
        this.requestSpecification = requestSpecification;
        this.responseSpecification = responseSpecification;
    }

    // перегружаем его
    public ValidatableResponse send(BaseModel model) {
        return given()
                .log().uri()
                .log().body()
                .spec(requestSpecification)
                .body(model)
                .request(endpoint.getMethod(), endpoint.getUrl())
                .then()
                .log().status()
                .log().body()
                .spec(responseSpecification);
    }
}

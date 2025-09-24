package requests.skelethon.requesters;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import requests.skelethon.Endpoint;

import static io.restassured.RestAssured.given;

public class ModelRequester<T extends BaseModel> extends Requester {
    public ModelRequester(Endpoint endpoint, RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(endpoint, requestSpecification, responseSpecification);
    }

    public T send() {
        return given()
                .spec(requestSpecification)
                .request(endpoint.getMethod(), apiVersion + endpoint.getUrl())
                .then()
                .spec(responseSpecification)
                .extract()
                .as((Class<T>) endpoint.getResponseModel());
    }

    public T send(BaseModel model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .request(endpoint.getMethod(), apiVersion + endpoint.getUrl())
                .then()
                .spec(responseSpecification)
                .extract()
                .as((Class<T>) endpoint.getResponseModel());
    }
}

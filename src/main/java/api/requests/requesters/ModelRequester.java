package api.requests.requesters;

import api.models.BaseModel;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.requests.Endpoint;

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

    public T send(int entityId) {
        return given()
                .spec(requestSpecification)
                .request(endpoint.getMethod(), apiVersion + endpoint.getUrl().formatted(entityId))
                .then()
                .spec(responseSpecification)
                .extract()
                .as((Class<T>) endpoint.getResponseModel());
    }
}

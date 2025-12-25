package api.requests.requesters;

import api.models.BaseModel;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.requests.Endpoint;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;

public class ValidationRequester extends Requester {
    public ValidationRequester(Endpoint endpoint, RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(endpoint, requestSpecification, responseSpecification);
    }

    public ValidatableResponse send() {
        return step("[API] Отправляем " + endpoint.getMethod() + "-запрос на " + endpoint.getUrl(), () ->
                given()
                .spec(requestSpecification)
                .request(endpoint.getMethod(), apiVersion + endpoint.getUrl())
                .then()
                .spec(responseSpecification));
    }

    public ValidatableResponse send(BaseModel model) {
        return step("[API] Отправляем " + endpoint.getMethod() + "-запрос на " + endpoint.getUrl(), () ->
                given()
                .spec(requestSpecification)
                .body(model)
                .request(endpoint.getMethod(), apiVersion + endpoint.getUrl())
                .then()
                .spec(responseSpecification));
    }

    public ValidatableResponse send(int entityId) {
        return step("[API] Отправляем " + endpoint.getMethod() + "-запрос на " + endpoint.getUrl().formatted(entityId), () ->
                given()
                .spec(requestSpecification)
                .request(endpoint.getMethod(), apiVersion + endpoint.getUrl().formatted(entityId))
                .then()
                .spec(responseSpecification));
    }
}

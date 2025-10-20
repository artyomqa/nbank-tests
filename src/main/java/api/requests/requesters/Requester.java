package api.requests.requesters;

import api.configs.Config;
import api.requests.Endpoint;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public abstract class Requester {
    protected final Endpoint endpoint;
    protected final RequestSpecification requestSpecification;
    protected final ResponseSpecification responseSpecification;
    protected final String apiVersion = Config.getString("service.apiVersion");

    public Requester(Endpoint endpoint, RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        this.endpoint = endpoint;
        this.requestSpecification = requestSpecification;
        this.responseSpecification = responseSpecification;
    }
}

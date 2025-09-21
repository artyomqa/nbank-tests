package utils;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class HttpLoggingFilter implements Filter {
    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext context) {
        System.out.println("➡\uFE0F [REQUEST] " + requestSpec.getMethod() + " " + requestSpec.getURI());

        Response response = context.next(requestSpec, responseSpec);

        System.out.println("⬅\uFE0F [RESPONSE] " + response.getStatusLine() + " " + response.getBody().asPrettyString() + "\n");

        return response;
    }
}

package utils;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class HttpLoggingFilter implements Filter {
    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext context) {
        System.out.println("➡️ [REQUEST] " + requestSpec.getMethod() + " " + requestSpec.getURI());

        String requestBody = requestSpec.getBody();
        if (requestBody != null && !requestBody.isEmpty()) {
            try {
                // выводим JSON в отформатированном виде
                String prettyBody = JsonPath.from(requestBody).prettify();
                System.out.println(prettyBody);
            } catch (Exception e) {
                // если тело не является валидным JSON, выводим как есть
                System.out.println(requestBody);
            }
        }

        Response response = context.next(requestSpec, responseSpec);

        System.out.println("⬅️ [RESPONSE] " + response.getStatusLine() + "\n" + response.getBody().asPrettyString() + "\n\n");

        return response;
    }
}

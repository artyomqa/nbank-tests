package api.specs;

import com.github.viclovsky.swagger.coverage.FileSystemOutputWriter;
import com.github.viclovsky.swagger.coverage.SwaggerCoverageRestAssured;
import common.configs.Config;
import api.models.LoginRequest;
import api.utils.HttpLoggingFilter;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import api.requests.Endpoint;
import api.requests.requesters.ValidationRequester;

import java.nio.file.Paths;

import static com.github.viclovsky.swagger.coverage.SwaggerCoverageConstants.OUTPUT_DIRECTORY;

public class RequestSpecs {
    private static String adminToken;

    private RequestSpecs() {}

    private static RequestSpecBuilder defaultRequestBuilder() {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .addFilter(new AllureRestAssured())
                .addFilter(new SwaggerCoverageRestAssured(new FileSystemOutputWriter(Paths.get("target/" + OUTPUT_DIRECTORY))))
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON);

        if (Config.getBoolean("log.http.enabled")) {
            builder.addFilter(new HttpLoggingFilter());
        }

        return builder;
    }

    public static RequestSpecification noAuth() {
        return defaultRequestBuilder().build();
    }

    public static RequestSpecification authAsAdmin() {
        if (adminToken == null) {
            adminToken = new ValidationRequester(Endpoint.LOGIN, RequestSpecs.noAuth(), ResponseSpecs.returnsOk())
                    .send(new LoginRequest(Config.getString("admin.username"), Config.getString("admin.password")))
                    .extract()
                    .header("Authorization");
        }

        return defaultRequestBuilder()
                .addHeader("Authorization", adminToken)
                .build();
    }

    public static RequestSpecification authWithToken(String userToken) {
        return defaultRequestBuilder()
                .addHeader("Authorization", userToken)
                .build();
    }
}

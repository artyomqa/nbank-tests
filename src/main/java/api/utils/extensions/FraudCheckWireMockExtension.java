package api.utils.extensions;

import api.utils.annotations.FraudCheckMock;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Locale;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class FraudCheckWireMockExtension implements BeforeEachCallback, AfterEachCallback {
    private WireMockServer wireMockServer;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        FraudCheckMock mockConfig = context.getTestMethod()
                .map(method -> method.getAnnotation(FraudCheckMock.class))
                .orElseGet(() -> context.getTestClass()
                        .map(clazz -> clazz.getAnnotation(FraudCheckMock.class))
                        .orElse(null));

        if (mockConfig != null) {
            setupWireMock(mockConfig);
        }
    }

    private void setupWireMock(FraudCheckMock config) {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig());
        wireMockServer.start();
        WireMock.configureFor("0.0.0.0", config.port());

        String responseBody = String.format(Locale.US, """
                        {
                          "status": "%s",
                          "decision": "%s",
                          "riskScore": "%.1f",
                          "reason": "%s",
                          "requiresManualReview": %s,
                          "additionalVerificationRequired": %s
                        }""",
                config.status(),
                config.decision(),
                config.riskScore(),
                config.reason(),
                config.requiresManualReview(),
                config.additionalVerificationRequired());

        stubFor(post(urlPathMatching(config.endpoint()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}

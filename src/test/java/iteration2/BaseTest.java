package iteration2;

import com.codeborne.selenide.Configuration;
import api.configs.Config;
import io.restassured.RestAssured;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;

public class BaseTest {
    protected static final float MAX_DEPOSIT_AMOUNT = 5000.00f;
    protected static final float MAX_TRANSFER_AMOUNT = 10000.00f;

    protected SoftAssertions softly;

    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.baseURI = Config.getString("service.host");
        RestAssured.port = Config.getInt("service.port");
    }

    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = Config.getString("selenoid.url");
        Configuration.baseUrl = Config.getString("service.host");
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability(
                "selenoid:options",
                Map.of("enableVNC", true, "enableLog", true));
    }

    @BeforeEach
    public void setupTest() {
        softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }
}

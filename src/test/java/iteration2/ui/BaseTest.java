package iteration2.ui;

import com.codeborne.selenide.Configuration;
import configs.Config;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

import java.util.Map;

public class BaseTest {
    protected static final float MAX_DEPOSIT_AMOUNT = 5000.00f;
    protected static final float MAX_TRANSFER_AMOUNT = 10000.00f;

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
}

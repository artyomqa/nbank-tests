package iteration2.ui;

import api.configs.Config;
import api.steps.User;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import iteration2.BaseTest;
import org.junit.jupiter.api.BeforeAll;

import java.util.Map;

import static com.codeborne.selenide.Selenide.executeJavaScript;

public class BaseUITest extends BaseTest {
    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = Config.getString("selenoid.url");
        Configuration.baseUrl = Config.getString("service.host");
        Configuration.browser = Config.getString("selenoid.browser");
        Configuration.browserSize = Config.getString("selenoid.browserSize");

        Configuration.browserCapabilities.setCapability(
                "selenoid:options",
                Map.of("enableVNC", true, "enableLog", true));
    }

    public void auth(User user) {
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", user.token());
    }
}

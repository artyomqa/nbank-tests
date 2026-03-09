package iteration2.ui;

import common.configs.Config;
import com.codeborne.selenide.Configuration;
import iteration2.BaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import ui.utils.extensions.BrowserMatchExtension;
import ui.utils.extensions.UserSessionExtension;

import java.util.Map;

@ExtendWith(UserSessionExtension.class)
@ExtendWith(BrowserMatchExtension.class)
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
}

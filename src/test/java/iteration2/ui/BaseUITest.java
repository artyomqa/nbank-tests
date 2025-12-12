package iteration2.ui;

import com.codeborne.selenide.logevents.SelenideLogger;
import common.configs.Config;
import com.codeborne.selenide.Configuration;
import io.qameta.allure.selenide.AllureSelenide;
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
        Configuration.baseUrl = Config.getString("frontend.url");
        Configuration.browser = Config.getString("selenoid.browser");
        Configuration.browserSize = Config.getString("selenoid.browser.size");

        Configuration.browserCapabilities.setCapability(
                "selenoid:options",
                Map.of("enableVNC", true, "enableLog", true));

        SelenideLogger.addListener("AllureSelenide", new AllureSelenide()
                .screenshots(true)
                .savePageSource(true));
    }
}

package iteration2.ui;

import api.steps.User;
import com.codeborne.selenide.Selenide;
import iteration2.BaseTest;

import static com.codeborne.selenide.Selenide.executeJavaScript;

public class BaseUITest extends BaseTest {
    public void auth(User user) {
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", user.token());
    }
}

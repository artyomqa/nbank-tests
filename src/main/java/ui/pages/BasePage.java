package ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import common.steps.User;
import com.codeborne.selenide.Selenide;
import org.openqa.selenium.Alert;
import ui.elements.BaseElement;

import java.util.List;
import java.util.function.Function;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class BasePage<T extends BasePage<T>> {
    public abstract String url();
    public abstract T shouldBeOpened();

    // Получение Page Object страницы + проверка, что страница открыта
    public <P extends BasePage<P>> P onPage(Class<P> pageClass) {
        return page(pageClass).shouldBeOpened();
    }

    public T checkAlertMessageAndAccept(String message) {
        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains(message);
        alert.accept();
        return (T) this;
    }

    public static void auth(User user) {
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", user.token());
    }

    protected <E extends BaseElement> List<E> generatePageElements(ElementsCollection elements, Function<SelenideElement, E> constructor) {
        return elements.stream().map(constructor).toList();
    }
}

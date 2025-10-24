package ui.pages;

import org.openqa.selenium.Alert;

import static com.codeborne.selenide.Selenide.page;
import static com.codeborne.selenide.Selenide.switchTo;
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
}

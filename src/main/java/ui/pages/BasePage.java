package ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import common.steps.User;
import com.codeborne.selenide.Selenide;
import io.qameta.allure.Allure;
import org.openqa.selenium.Alert;
import ui.elements.BaseElement;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

import static com.codeborne.selenide.Selenide.*;
import static io.qameta.allure.Allure.step;
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
        step("[UI] Авторизуемся от имени пользователя с id " + user.id(), () -> {
            Selenide.open("/");
            executeJavaScript("localStorage.setItem('authToken', arguments[0]);", user.token());
            attachScreenshot();
        });

    }

    protected <E extends BaseElement> List<E> generatePageElements(ElementsCollection elements, Function<SelenideElement, E> constructor) {
        return elements.stream().map(constructor).toList();
    }

    protected static void attachScreenshot() {
        String fileName = "Attachment_" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss_SSS"));

        String screenshotFile = screenshot(fileName);

        if (screenshotFile == null) {
            Allure.addAttachment(fileName, "text/plain", "Screenshot was not created");
            return;
        }

        try (InputStream inputStream = new FileInputStream(new File(new URI(screenshotFile)))) {
            Allure.addAttachment(fileName, "image/png", inputStream, ".png");
        } catch (Exception e) {
            Allure.addAttachment(fileName + "_error", "text/plain",
                    "Failed to attach screenshot: " + e.getMessage());
        }
    }
}

package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static io.qameta.allure.Allure.step;

public class UserDashboardPage extends BasePage<UserDashboardPage> {
    private final SelenideElement userInfoBlock = $(".user-info");
    private final SelenideElement userNameInHeaderText = $(".user-name");
    private final SelenideElement welcomeText = $(".welcome-text");
    private final SelenideElement depositMoneyButton = $$("button").findBy(Condition.text("Deposit Money"));
    private final SelenideElement transferMoneyButton = $$("button").findBy(Condition.text("Make a Transfer"));

    @Override
    public String url() {
        return "/dashboard";
    }

    @Override
    public UserDashboardPage shouldBeOpened() {
        step("[UI] Проверяем, что страница " + url() + " открыта", () -> {
            welcomeText.shouldBe(Condition.visible);
            attachScreenshot();
        });

        return this;
    }

    public UserDashboardPage open() {
        return step("[UI] Переходим на страницу " + url(), () -> {
            var page = Selenide.open(url(), UserDashboardPage.class);
            attachScreenshot();
            return page;
        });
    }

    public UserDashboardPage goToEditProfilePage() {
        step("[UI] Клик по меню профиля", () -> {
            userInfoBlock.click();
        });

        return this;
    }

    public UserDashboardPage goToDepositMoneyPage() {
        step("[UI] Клик по кнопке Deposit", () -> {
            depositMoneyButton.click();
        });

        return this;
    }

    public UserDashboardPage goToMakeTransferPage() {
        step("[UI] Клик по кнопке Make a Transfer", () -> {
            transferMoneyButton.click();
        });

        return this;
    }

    // Проверка, что на странице отображается ожидаемое имя
    public UserDashboardPage checkUsernameEquals(String name) {
        step("[UI] Проверка отображение имени на странице " + url(), () -> {
            welcomeText.shouldHave(Condition.text("Welcome, " + name));
            userNameInHeaderText.shouldHave(Condition.text(name));
            attachScreenshot();
        });

        return this;
    }
}

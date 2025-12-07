package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

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
        welcomeText.shouldBe(Condition.visible);
        return this;
    }

    public UserDashboardPage open() {
        return Selenide.open(url(), UserDashboardPage.class);
    }

    public UserDashboardPage goToEditProfilePage() {
        userInfoBlock.click();
        return this;
    }

    public UserDashboardPage goToDepositMoneyPage() {
        depositMoneyButton.click();
        return this;
    }

    public UserDashboardPage goToMakeTransferPage() {
        transferMoneyButton.click();
        return this;
    }

    // Проверка, что на странице отображается ожидаемое имя
    public UserDashboardPage checkUsernameEquals(String name) {
        welcomeText.shouldHave(Condition.text("Welcome, " + name));
        userNameInHeaderText.shouldHave(Condition.text(name));
        return this;
    }
}

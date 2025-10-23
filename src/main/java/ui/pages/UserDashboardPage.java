package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class UserDashboardPage extends BasePage<UserDashboardPage> {
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

    public UserDashboardPage goToDepositMoneyPage() {
        depositMoneyButton.click();
        return this;
    }

    public UserDashboardPage goToMakeTransferPage() {
        transferMoneyButton.click();
        return this;
    }
}

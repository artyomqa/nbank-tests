package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class DepositMoneyPage extends BasePage<DepositMoneyPage> {
    private final SelenideElement accountSelect = $(".account-selector");
    private final SelenideElement amountInput = $(".deposit-input");
    private final SelenideElement depositButton = $$("button").findBy(Condition.text("Deposit"));

    @Override
    public String url() {
        return "/deposit";
    }

    public DepositMoneyPage shouldBeOpened() {
        amountInput.shouldBe(Condition.visible);
        return this;
    }

    public DepositMoneyPage depositMoney(int accountId, float amount) {
        accountSelect.selectOptionByValue(String.valueOf(accountId));
        amountInput.setValue(String.valueOf(amount));
        depositButton.click();
        return this;
    }
}

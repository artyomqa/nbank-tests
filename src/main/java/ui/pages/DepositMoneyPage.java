package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.*;
import static io.qameta.allure.Allure.step;

public class DepositMoneyPage extends BasePage<DepositMoneyPage> {
    private final SelenideElement accountSelect = $(".account-selector");
    private final SelenideElement amountInput = $(".deposit-input");
    private final SelenideElement depositButton = $$("button").findBy(Condition.text("Deposit"));

    @Override
    public String url() {
        return "/deposit";
    }

    public DepositMoneyPage shouldBeOpened() {
        step("[UI] Проверяем, что страница " + url() + " открыта", () -> {
            amountInput.shouldBe(Condition.visible);
            attachScreenshot();
        });

        return this;
    }

    public DepositMoneyPage depositMoney(int accountId, float amount) {
        step("[UI] Выбираем счет", () -> {
            accountSelect.selectOptionByValue(String.valueOf(accountId));
            attachScreenshot();
        });

        step("[UI] Вводим сумму", () -> {
            amountInput.setValue(String.valueOf(amount));
            attachScreenshot();
        });

        step("[UI] Клик по кнопке Deposit", () -> {
            depositButton.click();
        });

        return this;
    }
}

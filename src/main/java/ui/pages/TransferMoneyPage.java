package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import ui.elements.TransactionItem;

import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static io.qameta.allure.Allure.step;

public class TransferMoneyPage extends BasePage<TransferMoneyPage> {
    private final SelenideElement transferAgainButton = $$("button").findBy(Condition.text("Transfer Again"));
    private final SelenideElement senderAccountSelector = $(".account-selector");
    private final SelenideElement recipientNameInput = $("input[placeholder='Enter recipient name']");
    private final SelenideElement recipientAccountInput = $("input[placeholder='Enter recipient account number']");
    private final SelenideElement amountInput = $("input[placeholder='Enter amount']");
    private final SelenideElement confirmCheckbox = $("#confirmCheck");
    private final SelenideElement transferButton = $$("button").findBy(Condition.text("Send Transfer"));

    @Override
    public String url() {
        return "/transfer";
    }

    @Override
    public TransferMoneyPage shouldBeOpened() {
        step("[UI] Проверяем, что страница " + url() + " открыта", () -> {
            transferButton.shouldBe(Condition.visible);
            attachScreenshot();
        });

        return this;
    }

    public TransferMoneyPage transfer(int senderAccountId, String receiverName, String receiverAccountNumber, float amount, boolean confirm) {
        step("[UI] Выбираем счет отправителя", () -> {
            senderAccountSelector.selectOptionByValue(String.valueOf(senderAccountId));
            attachScreenshot();
        });

        step("[UI] Вводим имя получателя", () -> {
            recipientNameInput.setValue(receiverName);
            attachScreenshot();
        });


        step("[UI] Вводим счет получателя", () -> {
            recipientAccountInput.setValue(receiverAccountNumber);
            attachScreenshot();
        });

        step("[UI] Вводим сумму", () -> {
            amountInput.setValue(String.valueOf(amount));
            attachScreenshot();
        });

        step("[UI] Проставляем значение чекбокса", () -> {
            confirmCheckbox.shouldNotBe(Condition.selected);
            if (confirm) {
                confirmCheckbox.click();
            }
            attachScreenshot();
        });

        step("[UI] Клик по кнопке Send Transfer", () -> {
            transferButton.click();
        });

        return this;
    }

    public TransferMoneyPage switchToTransferAgain() {
        step("[UI] Клик по табу Transfer Again", () -> {
            transferAgainButton.click();
            attachScreenshot();
        });

        return this;
    }

    public List<TransactionItem> getAllTransactions() {
        return step("[UI] Получаем список транзакций", () -> {
            ElementsCollection elements = $(".list-group").findAll("li");
            return generatePageElements(elements, TransactionItem::new);
        });
    }
}

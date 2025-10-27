package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import ui.elements.TransactionItem;

import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

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
        transferButton.shouldBe(Condition.visible);
        return this;
    }

    public TransferMoneyPage transfer(int senderAccountId, String receiverName, String receiverAccountNumber, float amount, boolean confirm) {
        senderAccountSelector.selectOptionByValue(String.valueOf(senderAccountId));
        recipientNameInput.setValue(receiverName);
        recipientAccountInput.setValue(receiverAccountNumber);
        amountInput.setValue(String.valueOf(amount));

        confirmCheckbox.shouldNotBe(Condition.selected);
        if (confirm) {
            confirmCheckbox.click();
        }

        transferButton.click();
        return this;
    }

    public TransferMoneyPage switchToTransferAgain() {
        transferAgainButton.click();
        return this;
    }

    public List<TransactionItem> getAllTransactions() {
        ElementsCollection elements = $(".list-group").findAll("li");
        return generatePageElements(elements, TransactionItem::new);
    }
}

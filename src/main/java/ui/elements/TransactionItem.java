package ui.elements;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

@Getter
public class TransactionItem extends BaseElement {
    private final String type;
    private final float amount;

    public TransactionItem(SelenideElement element) {
        super(element);

        String text = element.find("span").getText().split("\n")[0];
        String[] parts = text.split(" - ");

        this.type = parts[0].trim();
        this.amount = Float.parseFloat(parts[1].replace("$", "").trim());
    }
}

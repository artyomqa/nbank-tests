package ui.elements;

import com.codeborne.selenide.SelenideElement;

public class BaseElement {
    protected final SelenideElement element;

    public BaseElement(SelenideElement element) {
        this.element = element;
    }
}

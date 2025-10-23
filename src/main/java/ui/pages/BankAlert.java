package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    SUCCESS_DEPOSIT("Successfully deposited $%s to account %s!");

    private final String message;

    BankAlert(String message) {
        this.message = message;
    }

    public String format(Object... args) {
        return message.formatted(args);
    }
}

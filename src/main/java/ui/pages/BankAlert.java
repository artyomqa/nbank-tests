package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    SUCCESS_DEPOSIT("Successfully deposited $%s to account %s!"),
    MAX_DEPOSIT_EXCEEDED("Please deposit less or equal to 5000$"),
    INVALID_DEPOSIT_AMOUNT("Please enter a valid amount");

    private final String message;

    BankAlert(String message) {
        this.message = message;
    }

    public String format(Object... args) {
        return message.formatted(args);
    }
}

package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    SUCCESS_DEPOSIT("Successfully deposited $%s to account %s!"),
    MAX_DEPOSIT_EXCEEDED("Please deposit less or equal to 5000$"),
    INVALID_DEPOSIT_AMOUNT("Please enter a valid amount"),
    SUCCESS_TRANSFER("Successfully transferred $%s to account %s!"),
    INVALID_TRANSFER_AMOUNT("Error: Invalid transfer: insufficient funds or invalid accounts"),
    MAX_TRANSFER_EXCEEDED("Error: Transfer amount cannot exceed 10000"),
    TRANSFER_AMOUNT_LESS_MIN("Transfer amount must be at least 0.01"),
    MISSING_REQUIRED_FIELD("Please fill all fields and confirm"),
    SUCCESS_CHANGE_NAME("Name updated successfully!"),
    INVALID_NAME("Name must contain two words with letters only");

    private final String message;

    BankAlert(String message) {
        this.message = message;
    }

    public String format(Object... args) {
        return message.formatted(args);
    }
}

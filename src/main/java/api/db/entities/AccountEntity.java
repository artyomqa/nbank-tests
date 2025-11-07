package api.db.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@AllArgsConstructor
@Builder
@Getter
@ToString
public class AccountEntity {
    private long id;
    private String accountNumber;
    private BigDecimal balance;
    private long customerId;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public AccountEntity(ResultSet result) throws SQLException {
        this.id = result.getLong("id");
        this.accountNumber = result.getString("account_number");
        this.balance = result.getBigDecimal("balance");
        this.customerId = result.getLong("customer_id");
        this.createdAt = result.getTimestamp("created_at");
        this.updatedAt = result.getTimestamp("updated_at");
    }
}

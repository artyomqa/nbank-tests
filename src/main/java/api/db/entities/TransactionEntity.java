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
public class TransactionEntity {
    private long id;
    private BigDecimal amount;
    private String type;
    private Timestamp timestamp;
    private long accountId;
    private long relatedAccountId;
    private Timestamp createdAt;

    public TransactionEntity(ResultSet result) throws SQLException {
        this.id = result.getLong("id");
        this.amount = result.getBigDecimal("amount");
        this.type = result.getString("type");
        this.timestamp = result.getTimestamp("timestamp");
        this.accountId = result.getLong("account_id");
        this.relatedAccountId = result.getLong("related_account_id");
        this.createdAt = result.getTimestamp("created_at");
    }
}

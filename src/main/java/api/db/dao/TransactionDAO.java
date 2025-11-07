package api.db.dao;

import api.db.DBConnection;
import api.db.entities.TransactionEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    public List<TransactionEntity> findAll() {
        List<TransactionEntity> transactions = new ArrayList<>();
        String query = "SELECT id, amount, type, timestamp, account_id, related_account_id, created_at FROM transactions";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            ResultSet result = statement.executeQuery();

            while (result.next()) {
                transactions.add(new TransactionEntity(result));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении транзакции из БД: ", e);
        }

        return transactions;
    }
}

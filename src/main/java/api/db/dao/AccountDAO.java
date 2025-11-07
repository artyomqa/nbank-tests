package api.db.dao;

import api.db.DBConnection;
import api.db.entities.AccountEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountDAO {
    public AccountEntity findByAccountId(int accountId) {
        String query = "SELECT id, account_number, balance, customer_id, created_at, updated_at FROM accounts WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, accountId);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return new AccountEntity(result);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении счета из БД: ", e);
        }

        return null;
    }
}

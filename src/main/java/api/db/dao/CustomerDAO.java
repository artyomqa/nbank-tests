package api.db.dao;

import api.db.DBConnection;
import api.db.entities.CustomerEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerDAO {
    public CustomerEntity findById(int customerId) {
        String query = "SELECT id, username, password, name, role, created_at, updated_at FROM customers WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, customerId);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return new CustomerEntity(result);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении счета из БД: ", e);
        }

        return null;
    }
}

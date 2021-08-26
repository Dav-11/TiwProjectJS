package it.polimi.tiw.tiwprojectjs.dao;

import it.polimi.tiw.tiwprojectjs.beans.Item;

import java.sql.*;

public class ItemDAO {

    private Connection connection;

    public ItemDAO(Connection connection) {
        this.connection = connection;
    }

    public int createItem(Item item) throws SQLException {

        String query = "INSERT INTO item ( name, description, picture, id_auction) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {

            preparedStatement.setString(1, item.getName());
            preparedStatement.setString(2, item.getDescription());
            preparedStatement.setBinaryStream(3, item.getPicture());
            preparedStatement.setInt(4, item.getId_auction());

            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()){

                if (generatedKeys.next()){

                    return generatedKeys.getInt(1);
                }

                return -1;
            }
        }
    }
}

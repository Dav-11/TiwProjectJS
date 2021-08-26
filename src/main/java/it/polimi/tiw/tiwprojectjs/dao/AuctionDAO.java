package it.polimi.tiw.tiwprojectjs.dao;

import it.polimi.tiw.tiwprojectjs.beans.Auction;

import java.sql.*;

public class AuctionDAO {

    private Connection connection;

    public AuctionDAO(Connection connection) {
        this.connection = connection;
    }

    public int createAuction(Auction auction) throws SQLException {

        String query = "INSERT INTO auction (start_date, end_date, min_rise, id_user) VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {

            preparedStatement.setTimestamp(1, new Timestamp(auction.getStart_date().getTime()));
            preparedStatement.setTimestamp(2, new Timestamp(auction.getEnd_date().getTime()));
            preparedStatement.setFloat(3, auction.getMin_rise());
            preparedStatement.setInt(4, auction.getId_user());

            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()){

                if (generatedKeys.next()){

                    return generatedKeys.getInt(1);
                }

                return -1;
            }
        }
    }

    public void closeAuction(int auctionId) throws SQLException{

        String query = "UPDATE auction SET open = 0 WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {

            preparedStatement.setInt(1, auctionId);

            preparedStatement.executeUpdate();
        }
    }
}

package com.work.oblikcars.Utils.DB;

import com.work.oblikcars.model._CarDisposal;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CarDisposalUtil {
    private static CarDisposalUtil instance;

    private CarDisposalUtil() {}

    public static CarDisposalUtil getInstance() {
        if (instance == null) {
            instance = new CarDisposalUtil();
        }
        return instance;
    }

    private Connection Connect() {
        return DBUtil.getInstance().Connect();
    }


    /**
     * Повертає всі _CarDisposal, дата яких лежить між startDate та endDate (включно).
     */
    public List<_CarDisposal> getDisposalsBetweenDates(LocalDate startDate, LocalDate endDate) {
        List<_CarDisposal> list = new ArrayList<>();
        String sql = """
        SELECT id,
               carid,
               date,
               reason,
               price,
               description
          FROM cardisposals
         WHERE date BETWEEN ? AND ?
      ORDER BY carid, date
        """;

        try (Connection conn = Connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new _CarDisposal(
                            rs.getInt("id"),
                            rs.getInt("carid"),
                            rs.getDate("date").toLocalDate(),
                            rs.getString("reason"),
                            rs.getDouble("price"),
                            rs.getString("description")
                    ));
                }
            }

        } catch (SQLException ex) {
            System.err.println("Error fetching disposals between dates: " + ex.getMessage());
        }

        return list;
    }


    public List<_CarDisposal> getAllDisposals() {
        List<_CarDisposal> list = new ArrayList<>();
        String sql = "SELECT * FROM cardisposals";
        try (Connection connection = Connect();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                int carId = rs.getInt("carid");
                LocalDate date = rs.getDate("date").toLocalDate();
                String reason = rs.getString("reason");
                double price = rs.getDouble("price");
                String description = rs.getString("description");

                list.add(new _CarDisposal(id, carId, date, reason, price, description));
            }

        } catch (SQLException e) {
            System.err.println("Error loading car disposals: " + e.getMessage());
        }

        return list;
    }

    public void addDisposal(_CarDisposal disposal) {
        String sql = "INSERT INTO cardisposals (carid, date, reason, price, description) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = Connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, disposal.getCarId());
            stmt.setDate(2, Date.valueOf(disposal.getDate()));
            stmt.setString(3, disposal.getReason());
            stmt.setDouble(4, disposal.getPrice());
            stmt.setString(5, disposal.getDescription());

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding car disposal: " + e.getMessage());
        }
    }

    public void editDisposal(_CarDisposal disposal) {
        String sql = "UPDATE cardisposals SET carid = ?, date = ?, reason = ?, price = ?, description = ? WHERE id = ?";

        try (Connection connection = Connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, disposal.getCarId());
            stmt.setDate(2, Date.valueOf(disposal.getDate()));
            stmt.setString(3, disposal.getReason());
            stmt.setDouble(4, disposal.getPrice());
            stmt.setString(5, disposal.getDescription());
            stmt.setInt(6, disposal.getId());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                System.err.println("Disposal with ID " + disposal.getId() + " not found.");
            }

        } catch (SQLException e) {
            System.err.println("Error editing car disposal: " + e.getMessage());
        }
    }

    public void deleteDisposalById(int id) {
        String sql = "DELETE FROM cardisposals WHERE id = ?";

        try (Connection connection = Connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();

            if (rows == 0) {
                System.err.println("Disposal with ID " + id + " not found.");
            }

        } catch (SQLException e) {
            System.err.println("Error deleting car disposal: " + e.getMessage());
        }
    }

    public void deleteDisposal(_CarDisposal disposal) {
        if (disposal != null) {
            deleteDisposalById(disposal.getId());
        } else {
            System.err.println("Disposal object is null.");
        }
    }
}

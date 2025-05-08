package com.work.oblikcars.Utils.DB;

import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._CarDepreciation;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CarDepreciationUtil {
    private static CarDepreciationUtil instance;
    public static CarDepreciationUtil getInstance() {
        if (instance == null) {
            instance = new CarDepreciationUtil();
        }
        return instance;
    }
    private CarDepreciationUtil() {}

    public List<_CarDepreciation> getAllDepreciation() {
        List<_CarDepreciation> carsDepreciation = new ArrayList<>();
        String sql = "SELECT * FROM cardepreciations";
        try (Connection con = Connect(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) carsDepreciation.add(mapResultSetToCarDepreciation(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return carsDepreciation;
    }

    public List<_CarDepreciation> getDepreciationsByCarsDates(LocalDate startDate, LocalDate endDate, List<Integer> carIds) {
        List<_CarDepreciation> list = new ArrayList<>();
        if (carIds == null || carIds.isEmpty()) {
            return list;
        }

        // 1) Формуємо рядок плейсхолдерів "?, ?, ?, ..."
        String placeholders = String.join(",", carIds.stream().map(i -> "?").toList());

        String sql = """
        SELECT id,
               carid,
               date,
               price,
               description
          FROM cardepreciations
         WHERE carid IN (""" + placeholders + ") AND date BETWEEN ? AND ? ORDER BY carid, date";

        try (Connection conn = Connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // 2) Вставляємо всi carId
            int idx = 1;
            for (Integer cid : carIds) {
                stmt.setInt(idx++, cid);
            }
            // 3) Додаємо діапазон дат
            stmt.setDate(idx++, Date.valueOf(startDate));
            stmt.setDate(idx,   Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    _CarDepreciation cd = new _CarDepreciation(
                            rs.getInt("id"),
                            rs.getInt("carid"),
                            rs.getDate("date").toLocalDate(),
                            rs.getDouble("price"),
                            rs.getString("description")
                    );
                    list.add(cd);
                }
            }

        } catch (SQLException ex) {
            System.err.println("Error fetching depreciations by car IDs and dates: " + ex.getMessage());
        }

        return list;
    }

    public _CarDepreciation getDepreciationById(int id) {
        String sql = "SELECT * FROM cardepreciations WHERE id = ?";
        try (Connection connection = Connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSetToCarDepreciation(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching car by id: " + e.getMessage());
        }
        return null;
    }

    public void addDepreciation(_CarDepreciation depreciation) {
        String sql = "INSERT INTO cardepreciations (carid, date, price, description) VALUES (?, ?, ?, ?)";

        try (Connection con = Connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, depreciation.getCarId());
            ps.setDate(2, Date.valueOf(depreciation.getDate()));
            ps.setDouble(3, depreciation.getPrice());
            ps.setString(4, depreciation.getDescription());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding depreciation: " + e.getMessage());
        }
    }

    public void editDepreciation(_CarDepreciation depreciation) {
        String sql = "UPDATE cardepreciations SET carid = ?, date = ?, price = ?, description = ? WHERE id = ?";

        try (Connection con = Connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, depreciation.getCarId());
            ps.setDate(2, Date.valueOf(depreciation.getDate()));
            ps.setDouble(3, depreciation.getPrice());
            ps.setString(4, depreciation.getDescription());
            ps.setInt(5, depreciation.getId());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                System.err.println("Знецінення з ID " + depreciation.getId() + " не знайдено.");
            }
        } catch (SQLException e) {
            System.err.println("Error editing depreciation: " + e.getMessage());
        }
    }

    public void deleteDepreciationPermanentlyById(int id) {
        String sql = "DELETE FROM cardepreciations WHERE id = ?";

        try (Connection connection = Connect();
             PreparedStatement deleteStmt = connection.prepareStatement(sql)) {

            deleteStmt.setInt(1, id);
            int affectedRows = deleteStmt.executeUpdate();

            if (affectedRows == 0) {
                System.err.println("Знецінення з ID " + id + " не знайдено.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting depreciation: " + e.getMessage());
        }
    }



    private _CarDepreciation mapResultSetToCarDepreciation(ResultSet rs) throws SQLException {

        _CarDepreciation depreciation =  new _CarDepreciation(
                rs.getInt("id"),
                rs.getInt("carId"),
                rs.getDate("date").toLocalDate(),
                rs.getDouble("price"),
                rs.getString("description")
        );
        return depreciation;
    }

    private Connection Connect() {
        DBUtil dbUtil = DBUtil.getInstance();
        return  dbUtil.Connect();
    }
}

package com.work.oblikcars.Utils.DB;

import com.work.oblikcars.model._Insurance;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InsuranceUtil {
    private static InsuranceUtil instance;

    private InsuranceUtil() {}

    public static InsuranceUtil getInstance() {
        if (instance == null) {
            instance = new InsuranceUtil();
        }
        return instance;
    }

    public List<_Insurance> getAllInsurances() {
        List<_Insurance> list = new ArrayList<>();
        String sql = "SELECT * FROM insurances";

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                int carId = rs.getInt("carid");
                LocalDate startDate = rs.getDate("startdate").toLocalDate();
                LocalDate endDate = rs.getDate("enddate").toLocalDate();
                double price = rs.getDouble("price");

                _Insurance insurance = new _Insurance(id, carId, startDate, endDate, price);
                list.add(insurance);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching insurances: " + e.getMessage());
        }
        return list;
    }

    public void addInsurance(_Insurance insurance) {
        String sql = "INSERT INTO insurances (carid, startdate, enddate, price) VALUES (?, ?, ?, ?)";

        try (Connection connection = Connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, insurance.getCarId());
            stmt.setDate(2, Date.valueOf(insurance.getStartDate()));
            stmt.setDate(3, Date.valueOf(insurance.getEndDate()));
            stmt.setDouble(4, insurance.getPrice());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error adding insurance: " + e.getMessage());
        }
    }

    public void editInsurance(_Insurance insurance) {
        String sql = "UPDATE insurances SET carid = ?, startdate = ?, enddate = ?, price = ? WHERE id = ?";

        try (Connection connection = Connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, insurance.getCarId());
            stmt.setDate(2, Date.valueOf(insurance.getStartDate()));
            stmt.setDate(3, Date.valueOf(insurance.getEndDate()));
            stmt.setDouble(4, insurance.getPrice());
            stmt.setInt(5, insurance.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating insurance: " + e.getMessage());
        }
    }

    public void deleteInsurancePermanently(_Insurance insurance) {
        if (insurance == null) {
            System.err.println("Insurance object is null.");
            return;
        }

        String sql = "DELETE FROM insurances WHERE id = ?";

        try (Connection connection = Connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, insurance.getId());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                System.err.println("Страховку з ID " + insurance.getId() + " не знайдено.");
            }

        } catch (SQLException e) {
            System.err.println("Error deleting insurance: " + e.getMessage());
        }
    }

    private Connection Connect() {
        DBUtil dbUtil = DBUtil.getInstance();
        return dbUtil.Connect();
    }
}

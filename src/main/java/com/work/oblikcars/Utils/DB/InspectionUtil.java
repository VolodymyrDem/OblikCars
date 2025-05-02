package com.work.oblikcars.Utils.DB;

import com.work.oblikcars.model.WorkType;
import com.work.oblikcars.model._Inspection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InspectionUtil {
    private static InspectionUtil instance;

    private InspectionUtil() {}

    public static InspectionUtil getInstance() {
        if (instance == null) {
            instance = new InspectionUtil();
        }
        return instance;
    }

    public List<_Inspection> getAllInspections() {
        List<_Inspection> inspections = new ArrayList<>();
        String sql = "SELECT * FROM inspections";

        try (Connection connection = connect();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                int carId = rs.getInt("carid");
                int workTypeCode = rs.getInt("worktype");
                double price = rs.getDouble("price");
                String description = rs.getString("description");

                inspections.add(new _Inspection(id, carId, WorkType.fromCode(workTypeCode), price, description));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching inspections: " + e.getMessage());
        }

        return inspections;
    }

    public void addInspection(_Inspection inspection) {
        String sql = "INSERT INTO inspections (carid, workType, price, description) VALUES (?, ?, ?, ?)";

        try (Connection connection = connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, inspection.getCarId());
            stmt.setInt(2, inspection.getWorkType().getCode());
            stmt.setDouble(3, inspection.getPrice());
            stmt.setString(4, inspection.getDescription());

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding inspection: " + e.getMessage());
        }
    }

    public void editInspection(_Inspection inspection) {
        String sql = "UPDATE inspections SET carid = ?, workType = ?, price = ?, description = ? WHERE id = ?";

        try (Connection connection = connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, inspection.getCarId());
            stmt.setInt(2, inspection.getWorkType().getCode());
            stmt.setDouble(3, inspection.getPrice());
            stmt.setString(4, inspection.getDescription());
            stmt.setInt(5, inspection.getId());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                System.err.println("Інспекцію з ID " + inspection.getId() + " не знайдено.");
            }

        } catch (SQLException e) {
            System.err.println("Error editing inspection: " + e.getMessage());
        }
    }

    public void deleteInspectionById(int id) {
        String sql = "DELETE FROM inspections WHERE id = ?";

        try (Connection connection = connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                System.err.println("Інспекцію з ID " + id + " не знайдено.");
            }

        } catch (SQLException e) {
            System.err.println("Error deleting inspection: " + e.getMessage());
        }
    }

    public void deleteInspection(_Inspection inspection) {
        if (inspection != null) {
            deleteInspectionById(inspection.getId());
        } else {
            System.err.println("Inspection object is null.");
        }
    }

    private Connection connect() {
        return DBUtil.getInstance().Connect();
    }
}

package com.work.oblikcars.Utils.DB;

import com.work.oblikcars.model._InsuranceCase;
import com.work.oblikcars.model.InsuranceCaseType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InsuranceCaseUtil {
    private static InsuranceCaseUtil insuranceCaseUtil;
    public static InsuranceCaseUtil getInsuranceCaseUtil() {
        if (insuranceCaseUtil == null) {
            insuranceCaseUtil = new InsuranceCaseUtil();
        }
        return insuranceCaseUtil;
    }

    private InsuranceCaseUtil() {}

    private Connection Connect() {
        return DBUtil.getInstance().Connect();
    }

    public List<_InsuranceCase> getAllInsuranceCases() {
        String sql = "SELECT * FROM insurancecase";
        List<_InsuranceCase> result = new ArrayList<>();

        try (Connection conn = DBUtil.getInstance().Connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int insuranceCaseId = rs.getInt("insuranceCaseId");
                int carId = rs.getInt("carId");

                java.sql.Date d = rs.getDate("date");
                java.time.LocalDate date = d == null ? null : d.toLocalDate();

                String description = rs.getString("description");

                int typeCode = rs.getInt("type");
                InsuranceCaseType type = null;
                try {
                    type = InsuranceCaseType.fromCode(typeCode);
                } catch (IllegalArgumentException ex) {
                    // unknown code - leave null
                }

                java.sql.Date pd = rs.getDate("payDate");
                java.time.LocalDate payDate = pd == null ? null : pd.toLocalDate();

                _InsuranceCase ic = new _InsuranceCase(insuranceCaseId, carId, date, description, type, payDate);
                result.add(ic);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public _InsuranceCase getInsuranceCaseById(int id) {
        String sql = "SELECT * FROM insurancecase WHERE insuranceCaseId = ?";
        try (Connection conn = Connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int insuranceCaseId = rs.getInt("insuranceCaseId");
                    int carId = rs.getInt("carId");
                    java.sql.Date d = rs.getDate("date");
                    java.time.LocalDate date = d == null ? null : d.toLocalDate();
                    String description = rs.getString("description");
                    int typeCode = rs.getInt("type");
                    InsuranceCaseType type = null;
                    try { type = InsuranceCaseType.fromCode(typeCode);} catch (IllegalArgumentException ex) {}
                    java.sql.Date pd = rs.getDate("payDate");
                    java.time.LocalDate payDate = pd == null ? null : pd.toLocalDate();
                    return new _InsuranceCase(insuranceCaseId, carId, date, description, type, payDate);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching insurance case by id: " + e.getMessage());
        }
        return null;
    }

    public List<_InsuranceCase> getInsuranceCasesByCarId(int carId) {
        List<_InsuranceCase> list = new ArrayList<>();
        String sql = "SELECT * FROM insurancecase WHERE carId = ? ORDER BY date";
        try (Connection conn = Connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, carId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int insuranceCaseId = rs.getInt("insuranceCaseId");
                    java.sql.Date d = rs.getDate("date");
                    java.time.LocalDate date = d == null ? null : d.toLocalDate();
                    String description = rs.getString("description");
                    int typeCode = rs.getInt("type");
                    InsuranceCaseType type = null;
                    try { type = InsuranceCaseType.fromCode(typeCode);} catch (IllegalArgumentException ex) {}
                    java.sql.Date pd = rs.getDate("payDate");
                    java.time.LocalDate payDate = pd == null ? null : pd.toLocalDate();
                    list.add(new _InsuranceCase(insuranceCaseId, carId, date, description, type, payDate));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching insurance cases by carId: " + e.getMessage());
        }
        return list;
    }

    public List<_InsuranceCase> getInsuranceCasesByType(InsuranceCaseType t) {
        List<_InsuranceCase> list = new ArrayList<>();
        if (t == null) return list;
        String sql = "SELECT * FROM insurancecase WHERE type = ? ORDER BY date";
        try (Connection conn = Connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getCode());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int insuranceCaseId = rs.getInt("insuranceCaseId");
                    int carId = rs.getInt("carId");
                    java.sql.Date d = rs.getDate("date");
                    java.time.LocalDate date = d == null ? null : d.toLocalDate();
                    String description = rs.getString("description");
                    java.sql.Date pd = rs.getDate("payDate");
                    java.time.LocalDate payDate = pd == null ? null : pd.toLocalDate();
                    list.add(new _InsuranceCase(insuranceCaseId, carId, date, description, t, payDate));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching insurance cases by type: " + e.getMessage());
        }
        return list;
    }

    public void addInsuranceCase(_InsuranceCase ic) {
        String sql = "INSERT INTO insurancecase (carId, date, description, type, payDate) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ic.getCarId());
            ps.setDate(2, ic.getDate() == null ? null : java.sql.Date.valueOf(ic.getDate()));
            ps.setString(3, ic.getDescription());
            ps.setInt(4, ic.getType() == null ? 0 : ic.getType().getCode());
            ps.setDate(5, ic.getPayDate() == null ? null : java.sql.Date.valueOf(ic.getPayDate()));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding insurance case: " + e.getMessage());
        }
    }

    public void editInsuranceCase(_InsuranceCase ic) {
        String sql = "UPDATE insurancecase SET carId = ?, date = ?, description = ?, type = ?, payDate = ? WHERE insuranceCaseId = ?";
        try (Connection conn = Connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ic.getCarId());
            ps.setDate(2, ic.getDate() == null ? null : java.sql.Date.valueOf(ic.getDate()));
            ps.setString(3, ic.getDescription());
            ps.setInt(4, ic.getType() == null ? 0 : ic.getType().getCode());
            ps.setDate(5, ic.getPayDate() == null ? null : java.sql.Date.valueOf(ic.getPayDate()));
            ps.setInt(6, ic.getInsuranceCaseId());
            int rows = ps.executeUpdate();
            if (rows == 0) System.err.println("Insurance case with ID " + ic.getInsuranceCaseId() + " not found.");
        } catch (SQLException e) {
            System.err.println("Error editing insurance case: " + e.getMessage());
        }
    }

    public void deleteInsuranceCaseById(int id) {
        String sql = "DELETE FROM insurancecase WHERE insuranceCaseId = ?";
        try (Connection conn = Connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) System.err.println("Insurance case with ID " + id + " not found.");
        } catch (SQLException e) {
            System.err.println("Error deleting insurance case: " + e.getMessage());
        }
    }

    public List<_InsuranceCase> getInsuranceCasesByCarsDates(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            List<Integer> carIds
    ) {
        List<_InsuranceCase> list = new ArrayList<>();

        if (carIds == null || carIds.isEmpty()) {
            return list;
        }

        String placeholders = String.join(",", carIds.stream().map(id -> "?").toList());
        java.time.LocalDate start = (startDate == null) ? java.time.LocalDate.of(1970, 1, 1) : startDate;
        java.time.LocalDate end = (endDate == null) ? java.time.LocalDate.of(2999, 12, 31) : endDate;

        String sql = "SELECT * FROM insurancecase WHERE carId IN (" + placeholders + ") " +
                "AND date BETWEEN ? AND ? ORDER BY carId, date";

        try (Connection conn = Connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int index = 1;
            for (Integer id : carIds) {
                ps.setInt(index++, id);
            }

            ps.setDate(index++, java.sql.Date.valueOf(start));
            ps.setDate(index, java.sql.Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int insuranceCaseId = rs.getInt("insuranceCaseId");
                    int carId = rs.getInt("carId");
                    java.sql.Date d = rs.getDate("date");
                    java.time.LocalDate date = d == null ? null : d.toLocalDate();
                    String description = rs.getString("description");
                    int typeCode = rs.getInt("type");
                    InsuranceCaseType type = null;
                    try { type = InsuranceCaseType.fromCode(typeCode);} catch (IllegalArgumentException ex) {}
                    java.sql.Date pd = rs.getDate("payDate");
                    java.time.LocalDate payDate = pd == null ? null : pd.toLocalDate();

                    list.add(new _InsuranceCase(insuranceCaseId, carId, date, description, type, payDate));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching insurance cases by cars and dates: " + e.getMessage());
        }

        return list;
    }

}

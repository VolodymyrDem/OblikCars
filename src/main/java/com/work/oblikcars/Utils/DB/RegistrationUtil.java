package com.work.oblikcars.Utils.DB;

import com.work.oblikcars.model._Registration;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RegistrationUtil {
    private static RegistrationUtil instance;

    private RegistrationUtil() {}

    public static RegistrationUtil getInstance() {
        if (instance == null) {
            instance = new RegistrationUtil();
        }
        return instance;
    }

    public List<_Registration> getRegistrationsByCarsDates(LocalDate startDate,
                                                           LocalDate endDate,
                                                           List<Integer> carIds) {
        List<_Registration> list = new ArrayList<>();
        if (carIds == null || carIds.isEmpty()) {
            return list;
        }

        // Створюємо плейсхолдери "?, ?, ?, ..."
        String placeholders = String.join(",", carIds.stream().map(id -> "?").toList());

        String sql = """
        SELECT id,
               carId,
               price,
               registrationdate
          FROM registrations
         WHERE carId IN (""" + placeholders + ") AND registrationdate BETWEEN ? AND ? ORDER BY carId, registrationdate";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // 1) Проставляємо carId
            int idx = 1;
            for (Integer cid : carIds) {
                stmt.setInt(idx++, cid);
            }
            // 2) Проставляємо дати
            stmt.setDate(idx++, Date.valueOf(startDate));
            stmt.setDate(idx,   Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    _Registration reg = new _Registration(
                            rs.getInt("id"),
                            rs.getInt("carId"),
                            rs.getDouble("price"),
                            rs.getDate("registrationdate").toLocalDate()
                    );
                    list.add(reg);
                }
            }

        } catch (SQLException ex) {
            System.err.println("Error fetching registrations by car IDs and dates: " + ex.getMessage());
        }

        return list;
    }

    public List<_Registration> getAllRegistrations() {
        List<_Registration> list = new ArrayList<>();
        String sql = "SELECT * FROM registrations";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                int carId = rs.getInt("carId");
                double price = rs.getDouble("price");
                LocalDate date = rs.getDate("registrationdate").toLocalDate();

                list.add(new _Registration(id, carId, price, date));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching registrations: " + e.getMessage());
        }

        return list;
    }

    public List<_Registration> getRegistrationsByCarId(int carId) {
        List<_Registration> list = new ArrayList<>();
        String sql = "SELECT * FROM registrations WHERE carId = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    double price = rs.getDouble("price");
                    LocalDate date = rs.getDate("registrationdate").toLocalDate();

                    list.add(new _Registration(id, carId, price, date));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching registrations by carId: " + e.getMessage());
        }

        return list;
    }

    public void addRegistration(_Registration registration) {
        String sql = "INSERT INTO registrations (carId, price, registrationdate) VALUES (?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, registration.getCarId());
            stmt.setDouble(2, registration.getPrice());
            stmt.setDate(3, Date.valueOf(registration.getRegistrationDate()));

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error adding registration: " + e.getMessage());
        }
    }

    public void editRegistration(_Registration registration) {
        String sql = "UPDATE registrations SET carId = ?, price = ?, registrationdate = ? WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, registration.getCarId());
            stmt.setDouble(2, registration.getPrice());
            stmt.setDate(3, Date.valueOf(registration.getRegistrationDate()));
            stmt.setInt(4, registration.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("Помилка: реєстрацію з ID " + registration.getId() + " не знайдено.");
            }

        } catch (SQLException e) {
            System.err.println("Error editing registration: " + e.getMessage());
        }
    }

    public void deleteRegistrationById(int id) {
        String sql = "DELETE FROM registrations WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                System.err.println("Реєстрація з ID " + id + " не знайдена для видалення.");
            }

        } catch (SQLException e) {
            System.err.println("Error deleting registration: " + e.getMessage());
        }
    }

    public void deleteRegistration(_Registration registration) {
        if (registration != null) {
            deleteRegistrationById(registration.getId());
        } else {
            System.err.println("Registration object is null.");
        }
    }

    private Connection connect() {
        return DBUtil.getInstance().Connect();
    }
}


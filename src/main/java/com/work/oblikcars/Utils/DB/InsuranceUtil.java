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

    public List<_Insurance> getBetweenDates(LocalDate start, LocalDate end) {
        List<_Insurance> list = new ArrayList<>();

        // Перший день місяця для старту
        LocalDate startMonth = start.withDayOfMonth(1);
        // Останній день місяця для кінця
        LocalDate endMonth = end.withDayOfMonth(end.lengthOfMonth());

        String sql = """
        SELECT id,
               NumberOfCars,
               PayDate,
               month,
               price
          FROM insurances
         WHERE month >= ?
           AND month <= ?
        """;

        try (Connection conn = Connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startMonth));
            stmt.setDate(2, Date.valueOf(endMonth));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id            = rs.getInt("id");
                    int numberOfCars  = rs.getInt("NumberOfCars");
                    LocalDate payDate = rs.getDate("PayDate").toLocalDate();
                    LocalDate month   = rs.getDate("month").toLocalDate();
                    double price      = rs.getDouble("price");

                    _Insurance ins = new _Insurance(
                            id,
                            numberOfCars,
                            payDate,
                            month,
                            price
                    );
                    list.add(ins);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching insurances between dates: " + e.getMessage());
        }
        return list;
    }


    /** Повертає всі записи зі стовпцями: id, NumberOfCars, PayDate, month, price */
    public List<_Insurance> getAllInsurances() {
        List<_Insurance> list = new ArrayList<>();
        String sql = """
            SELECT id,
                   NumberOfCars,
                   PayDate,
                   month,
                   price
              FROM insurances
            """;

        try (Connection conn = Connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id            = rs.getInt("id");
                int numberOfCars  = rs.getInt("NumberOfCars");
                LocalDate payDate = rs.getDate("PayDate").toLocalDate();
                LocalDate month   = rs.getDate("month").toLocalDate();
                double price      = rs.getDouble("price");

                // Викликаємо конструктор, який заповнює monthStr автоматично
                _Insurance ins = new _Insurance(
                        id,
                        numberOfCars,
                        payDate,
                        month,
                        price
                );
                list.add(ins);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching insurances: " + e.getMessage());
        }
        return list;
    }

    /** Додає новий запис у таблицю insurances */
    public void addInsurance(_Insurance insurance) {
        String sql = """
            INSERT INTO insurances
                (NumberOfCars, PayDate, month, price)
            VALUES
                (?, ?, ?, ?)
            """;

        try (Connection conn = Connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, insurance.getNumberOfCars());
            stmt.setDate(2, Date.valueOf(insurance.getPayDate()));
            stmt.setDate(3, Date.valueOf(insurance.getMonth()));
            stmt.setDouble(4, insurance.getPrice());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating insurance failed, no rows affected.");
            }
            // Отримаємо згенерований ID і виставимо його в обʼєкті
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    insurance.setId(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error adding insurance: " + e.getMessage());
        }
    }

    /** Оновлює існуючий запис за полем id */
    public void editInsurance(_Insurance insurance) {
        String sql = """
            UPDATE insurances
               SET NumberOfCars = ?,
                   PayDate      = ?,
                   month        = ?,
                   price        = ?
             WHERE id = ?
            """;

        try (Connection conn = Connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, insurance.getNumberOfCars());
            stmt.setDate(2, Date.valueOf(insurance.getPayDate()));
            stmt.setDate(3, Date.valueOf(insurance.getMonth()));
            stmt.setDouble(4, insurance.getPrice());
            stmt.setInt(5, insurance.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating insurance: " + e.getMessage());
        }
    }

    /** Видаляє запис із таблиці за id */
    public void deleteInsurancePermanently(_Insurance insurance) {
        if (insurance == null) {
            System.err.println("Insurance object is null.");
            return;
        }

        String sql = "DELETE FROM insurances WHERE id = ?";

        try (Connection conn = Connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, insurance.getId());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("Insurance with ID " + insurance.getId() + " not found.");
            }

        } catch (SQLException e) {
            System.err.println("Error deleting insurance: " + e.getMessage());
        }
    }

    /** Встановлює зʼєднання з БД */
    private Connection Connect() {
        return DBUtil.getInstance().Connect();
    }
}

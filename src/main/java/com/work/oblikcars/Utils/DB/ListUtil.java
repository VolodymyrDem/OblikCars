package com.work.oblikcars.Utils.DB;

import com.work.oblikcars.model._Car;
import com.work.oblikcars.model._List;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ListUtil {
    private static ListUtil instance;
    private ListUtil() {}

    public static ListUtil getInstance() {
        if (instance == null) {
            instance = new ListUtil();
        }
        return instance;
    }

    public List<_List> getAllLists() {
        List<_List> lists = new ArrayList<>();
        String sql = "SELECT * FROM `lists`";

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                _List list;
                int id = resultSet.getInt("id");
                int carId = resultSet.getInt("carid");
                double startMileage = resultSet.getDouble("startmileage");
                LocalDate startDate = resultSet.getDate("startdate").toLocalDate();
                boolean done = resultSet.getBoolean("done");
                String description = resultSet.getString("description");
                if (done) {
                    double endMileage = resultSet.getDouble("endmileage");
                    LocalDate endDate = resultSet.getDate("enddate").toLocalDate();
                    int  rents = resultSet.getInt("rents");
                    int rentDays = resultSet.getInt("rentDays");
                    double income = resultSet.getDouble("income");
                    list = new _List(id, carId, startMileage, startDate, endMileage, endDate, rents, rentDays, done, income, description);
                } else {
                    list = new _List(id, carId, startMileage, startDate, done, description);
                }
                lists.add(list);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return lists;
    }

    public List<_List> getListsByCarDates(LocalDate startDate, LocalDate endDate, int carId) {
        List<_List> lists = new ArrayList<>();
        String sql = """
        SELECT * FROM lists
        WHERE carid = ?
        AND (
            (startdate BETWEEN ? AND ?)
            OR (enddate BETWEEN ? AND ?)
        )
        """;

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, carId);
            statement.setDate(2, Date.valueOf(startDate));
            statement.setDate(3, Date.valueOf(endDate));
            statement.setDate(4, Date.valueOf(startDate));
            statement.setDate(5, Date.valueOf(endDate));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    _List list;
                    int id = resultSet.getInt("id");
                    double startMileage = resultSet.getDouble("startmileage");
                    LocalDate sDate = resultSet.getDate("startdate").toLocalDate();
                    boolean done = resultSet.getBoolean("done");
                    String description = resultSet.getString("description");
                    if (done) {
                        double endMileage = resultSet.getDouble("endmileage");
                        LocalDate eDate = resultSet.getDate("enddate").toLocalDate();

                        int  rents = resultSet.getInt("rents");
                        int rentDays = resultSet.getInt("rentDays");

                        double income = resultSet.getDouble("income");
                        list = new _List(id, carId, startMileage, startDate, endMileage, eDate, rents, rentDays, done, income, description);
                    } else {
                        list = new _List(id, carId, startMileage, sDate, false, description);
                    }

                    lists.add(list);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching lists by car and dates: " + e.getMessage());
        }

        return lists;
    }

    public List<_List> getListsByCarsDates(LocalDate startDate, LocalDate endDate, List<Integer> carIds) {
        List<_List> lists = new ArrayList<>();

        if (carIds == null || carIds.isEmpty()) {
            return lists;
        }

        // Створення плейсхолдерів (?, ?, ?, ...)
        String placeholders = String.join(",", carIds.stream().map(id -> "?").toList());

        String sql = """
        SELECT * FROM lists
        WHERE carid IN (""" + placeholders + ")" +
         "AND ((startdate BETWEEN ? AND ?) OR (enddate BETWEEN ? AND ?)) " +
        "ORDER BY carid, startdate ";

        try (Connection connection = Connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            int index = 1;
            for (Integer id : carIds) {
                stmt.setInt(index++, id);
            }

            stmt.setDate(index++, java.sql.Date.valueOf(startDate));
            stmt.setDate(index++, java.sql.Date.valueOf(endDate));
            stmt.setDate(index++, java.sql.Date.valueOf(startDate));
            stmt.setDate(index, java.sql.Date.valueOf(endDate));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                int carId = rs.getInt("carid");
                double startMileage = rs.getDouble("startmileage");
                LocalDate sDate = rs.getDate("startdate").toLocalDate();
                boolean done = rs.getBoolean("done");
                String description = rs.getString("description");
                _List list;
                if (done) {
                    double endMileage = rs.getDouble("endmileage");
                    LocalDate eDate = rs.getDate("enddate").toLocalDate();
                    int  rents = rs.getInt("rents");
                    int rentDays = rs.getInt("rentDays");
                    double income = rs.getDouble("income");
                    list = new _List(id, carId, startMileage, sDate, endMileage, eDate, rents, rentDays, done, income,  description);
                } else {
                    list = new _List(id, carId, startMileage, sDate, false,  description);
                }

                lists.add(list);
            }

        } catch (SQLException e) {
            System.err.println("Error getting filtered lists by car IDs: " + e.getMessage());
        }

        return lists;
    }


    public void addList(_List list) {
        String sql = (list.isDone())?"INSERT INTO lists (carid, startmileage, startdate, endmileage, enddate, done, income, rents, rentDays, description)\n" +
                "VALUES" +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)":"INSERT INTO lists (carid, startmileage, startdate, done, description)\n" +
                "VALUES" +
                "(?, ?, ?, ?, ?)";

        try (Connection connection = Connect();
             PreparedStatement insertStmt = connection.prepareStatement(sql)) {

            insertStmt.setInt(1, list.getCarId());
            insertStmt.setDouble(2, list.getStartMileage());
            insertStmt.setDate(3, java.sql.Date.valueOf(list.getStartDate()));
            if((list.isDone())){
                insertStmt.setDouble(4, list.getEndMileage());
                insertStmt.setDate(5, java.sql.Date.valueOf(list.getEndDate()));
                insertStmt.setBoolean(6, list.isDone());
                insertStmt.setDouble(7, list.getIncome());
                insertStmt.setInt(8, list.getRents());
                insertStmt.setInt(9, list.getRentDays());
                insertStmt.setString(10, list.getDescription());


            } else {
                insertStmt.setBoolean(4, list.isDone());
                insertStmt.setString(5, list.getDescription());
            }

            insertStmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding list: " + e.getMessage());
        }
    }

    public void editList(_List list) {
        String sql =(list.isDone())? "UPDATE lists SET carid = ?, startmileage = ?, startdate = ?, endmileage = ?, enddate = ?, done = ?, income = ?, rents = ?, rentDays = ?, description = ?" +
                " WHERE id = ?":"UPDATE lists SET carid = ?, startmileage = ?, startdate = ?, done = ?, description = ?" +
                " WHERE id = ?";

        try (Connection connection = Connect();
             PreparedStatement updateStmt = connection.prepareStatement(sql)) {

            updateStmt.setInt(1, list.getCarId());
            updateStmt.setDouble(2, list.getStartMileage());
            updateStmt.setDate(3, java.sql.Date.valueOf(list.getStartDate()));
            if((list.isDone())){
                updateStmt.setDouble(4, list.getEndMileage());
                updateStmt.setDate(5, java.sql.Date.valueOf(list.getEndDate()));
                updateStmt.setBoolean(6, list.isDone());
                updateStmt.setDouble(7, list.getIncome());
                updateStmt.setInt(8, list.getRents());
                updateStmt.setInt(9, list.getRentDays());
                updateStmt.setString(10, list.getDescription());
                updateStmt.setInt(11, list.getId());
            } else {
                updateStmt.setBoolean(4, list.isDone());
                updateStmt.setString(5, list.getDescription());
                updateStmt.setInt(6, list.getId());

            }


            int affectedRows = updateStmt.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("Помилка: лист з ID " + list.getId() + " не знайдено.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating list: " + e.getMessage());
        }
    }

    public void deleteListPermanentlyById(int id) {
        String sql = "DELETE FROM lists WHERE id = ?";

        try (Connection connection = Connect();
             PreparedStatement deleteStmt = connection.prepareStatement(sql)) {

            deleteStmt.setInt(1, id);
            int affectedRows = deleteStmt.executeUpdate();

            if (affectedRows == 0) {
                System.err.println("лист з ID " + id + " не знайдено.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting list: " + e.getMessage());
        }
    }

    public void deleteListPermanently(_List list) {
        if (list != null) {
            deleteListPermanentlyById(list.getId());
        } else {
            System.err.println("list object is null.");
        }
    }

    private Connection Connect() {
        DBUtil dbUtil = DBUtil.getInstance();
        return  dbUtil.Connect();
    }
}

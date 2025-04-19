package com.work.oblikcars.Utils.DB;

import com.work.oblikcars.model._Car;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarUtil {

    private static CarUtil instance;
    private CarUtil() {}

    public static CarUtil getInstance() {
        if (instance == null) {
            instance = new CarUtil();
        }
        return instance;
    }

    public List<_Car> getAllCars() {
        List<_Car> cars = new ArrayList<>();
        String sql = "SELECT * FROM `cars`";

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {

                int id = resultSet.getInt("id");
                String vin = resultSet.getString("vin"); // use as pk
                String number = resultSet.getString("number");
                String model = resultSet.getString("model");
                String fuel = resultSet.getString("fuel");
                double engineVolume = resultSet.getDouble("engineVolume");
                LocalDate rentDate = resultSet.getDate("rentdate").toLocalDate();
                double mileageStart = resultSet.getDouble("mileageStart");
                LocalDate firstRegistrationDate = resultSet.getDate("firstRegistrationDate").toLocalDate();
                double priceOfFirstRegistration = resultSet.getDouble("priceOfFirstRegistration");
                int daysForReRegistration = resultSet.getInt("daysForReRegistration");
                double price = resultSet.getDouble("price");
                boolean valid = resultSet.getBoolean("valid");

                _Car car = new _Car(id, vin, number, model, fuel, engineVolume, rentDate, mileageStart, firstRegistrationDate, priceOfFirstRegistration, daysForReRegistration, price, valid);
                cars.add(car);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return cars;
    }

    public List<_Car> getFreeCars() {
        List<_Car> cars = new ArrayList<>();
        String sql = """
        SELECT * FROM cars c
        WHERE c.valid = TRUE AND NOT EXISTS (
            SELECT 1 FROM lists l
            WHERE l.carid = c.id AND l.done = FALSE
        )
        """;

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {

                int id = resultSet.getInt("id");
                String vin = resultSet.getString("vin"); // use as pk
                String number = resultSet.getString("number");
                String model = resultSet.getString("model");
                String fuel = resultSet.getString("fuel");
                double engineVolume = resultSet.getDouble("engineVolume");
                LocalDate rentDate = resultSet.getDate("rentdate").toLocalDate();
                double mileageStart = resultSet.getDouble("mileageStart");
                LocalDate firstRegistrationDate = resultSet.getDate("firstRegistrationDate").toLocalDate();
                double priceOfFirstRegistration = resultSet.getDouble("priceOfFirstRegistration");
                int daysForReRegistration = resultSet.getInt("daysForReRegistration");
                double price = resultSet.getDouble("price");
                boolean valid = resultSet.getBoolean("valid");

                _Car car = new _Car(id, vin, number, model, fuel, engineVolume, rentDate, mileageStart, firstRegistrationDate, priceOfFirstRegistration, daysForReRegistration, price, valid);
                cars.add(car);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return cars;
    }

    public _Car getCarById(int id) {
        String sql = "SELECT * FROM cars WHERE id = ?";
        try (Connection connection = Connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new _Car(
                            rs.getInt("id"),
                            rs.getString("vin"),
                            rs.getString("number"),
                            rs.getString("model"),
                            rs.getString("fuel"),
                            rs.getDouble("engineVolume"),
                            rs.getDate("rentdate").toLocalDate(),
                            rs.getDouble("mileageStart"),
                            rs.getDate("firstRegistrationDate").toLocalDate(),
                            rs.getDouble("priceOfFirstRegistration"),
                            rs.getInt("daysForReRegistration"),
                            rs.getDouble("price"),
                            rs.getBoolean("valid")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching car by id: " + e.getMessage());
        }
        return null;
    }

    public Map<Integer, String> getAllCarComboMap(boolean onlyValid) {
        Map<Integer, String> map = new HashMap<>();
        for (_Car car : getAllCars()) {
            if (!onlyValid || car.isValid()) {
                map.put(car.getId(), car.getBoxString());
            }
        }
        return map;
    }

    public Map<Integer, String> getCarWithNoListsComboMap(boolean onlyValid) {
        Map<Integer, String> map = new HashMap<>();
        for (_Car car : getFreeCars()) {
            if (!onlyValid || car.isValid()) {
                map.put(car.getId(), car.getBoxString());
            }
        }
        return map;
    }

    public Map<Integer, String> getCarAvailableForInsuranceComboMap(boolean onlyValid) {
        Map<Integer, String> map = new HashMap<>();
        String sql = """
        SELECT * FROM cars c
        WHERE c.valid = TRUE
        AND (
            NOT EXISTS (
                SELECT 1 FROM insurances i
                WHERE i.carid = c.id AND i.enddate >= CURRENT_DATE()
            )
        )
        """;

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String vin = resultSet.getString("vin");
                String number = resultSet.getString("number");
                String model = resultSet.getString("model");
                String fuel = resultSet.getString("fuel");
                double engineVolume = resultSet.getDouble("engineVolume");
                LocalDate rentDate = resultSet.getDate("rentdate").toLocalDate();
                double mileageStart = resultSet.getDouble("mileageStart");
                LocalDate firstRegistrationDate = resultSet.getDate("firstRegistrationDate").toLocalDate();
                double priceOfFirstRegistration = resultSet.getDouble("priceOfFirstRegistration");
                int daysForReRegistration = resultSet.getInt("daysForReRegistration");
                double price = resultSet.getDouble("price");
                boolean valid = resultSet.getBoolean("valid");

                _Car car = new _Car(id, vin, number, model, fuel, engineVolume, rentDate, mileageStart,
                        firstRegistrationDate, priceOfFirstRegistration, daysForReRegistration, price, valid);

                if (!onlyValid || car.isValid()) {
                    map.put(car.getId(), car.getBoxString());
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting cars available for insurance: " + e.getMessage());
        }

        return map;
    }

    public Map<Integer, String> getCarsWithExpiredOrNoRegistrationMap(boolean onlyValid) {
        Map<Integer, String> map = new HashMap<>();

        String sql = """
        SELECT c.*
        FROM cars c
        LEFT JOIN (
            SELECT r.carId, MAX(r.registrationdate) AS last_reg
            FROM registrations r
            GROUP BY r.carId
        ) reg ON c.id = reg.carId
        WHERE c.valid = TRUE
        AND (
            reg.carId IS NULL
            OR DATE_ADD(reg.last_reg, INTERVAL c.daysForReRegistration DAY) < CURRENT_DATE()
        )
        """;

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String vin = resultSet.getString("vin");
                String number = resultSet.getString("number");
                String model = resultSet.getString("model");
                String fuel = resultSet.getString("fuel");
                double engineVolume = resultSet.getDouble("engineVolume");
                LocalDate rentDate = resultSet.getDate("rentdate").toLocalDate();
                double mileageStart = resultSet.getDouble("mileageStart");
                LocalDate firstRegistrationDate = resultSet.getDate("firstRegistrationDate").toLocalDate();
                double priceOfFirstRegistration = resultSet.getDouble("priceOfFirstRegistration");
                int daysForReRegistration = resultSet.getInt("daysForReRegistration");
                double price = resultSet.getDouble("price");
                boolean valid = resultSet.getBoolean("valid");

                _Car car = new _Car(id, vin, number, model, fuel, engineVolume, rentDate, mileageStart,
                        firstRegistrationDate, priceOfFirstRegistration, daysForReRegistration, price, valid);

                if (!onlyValid || car.isValid()) {
                    map.put(car.getId(), car.getBoxString());
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting cars with expired or no registration: " + e.getMessage());
        }

        return map;
    }

    public double getCurrentMileage(int carId) {
        String sql = """
        SELECT MAX(endmileage) AS max_end_mileage
        FROM lists
        WHERE carid = ? AND done = TRUE
    """;

        try (Connection connection = Connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, carId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double maxEndMileage = rs.getDouble("max_end_mileage");
                    if (!rs.wasNull()) {
                        return maxEndMileage;
                    }
                }
            }

            // Якщо немає закритих листів — беремо з cars
            _Car car = getCarById(carId);
            if (car != null) {
                return car.getMileageStart();
            }

        } catch (SQLException e) {
            System.err.println("Помилка при отриманні пробігу: " + e.getMessage());
        }

        return 0.0; // fallback, якщо нічого не знайдено
    }

    public void addCar(_Car car) {
        String sql = "INSERT INTO cars (vin, number, model, fuel, engineVolume, rentdate, mileageStart, firstRegistrationDate, priceOfFirstRegistration, daysForReRegistration, price)\n" +
                "VALUES" +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = Connect();
             PreparedStatement insertStmt = connection.prepareStatement(sql)) {

            insertStmt.setString(1, car.getVin());
            insertStmt.setString(2, car.getNumber());
            insertStmt.setString(3, car.getModel());
            insertStmt.setString(4, car.getFuel());
            insertStmt.setDouble(5, car.getEngineVolume());
            insertStmt.setDate(6, java.sql.Date.valueOf(car.getRentDate()));
            insertStmt.setDouble(7, car.getMileageStart());
            insertStmt.setDate(8, java.sql.Date.valueOf(car.getFirstRegistrationDate()));
            insertStmt.setDouble(9, car.getPriceOfFirstRegistration());
            insertStmt.setInt(10, car.getDaysForReRegistration());
            insertStmt.setDouble(11, car.getPrice());

            insertStmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding car: " + e.getMessage());
        }
    }

    public void editCar(_Car car) {
        String sql = "UPDATE cars SET vin = ?, number = ?, model = ?, fuel = ?, engineVolume = ?, " +
                "rentdate = ?, mileageStart = ?, firstRegistrationDate = ?, priceOfFirstRegistration = ?, " +
                "daysForReRegistration = ?, price = ? WHERE id = ?";

        try (Connection connection = Connect();
             PreparedStatement updateStmt = connection.prepareStatement(sql)) {

            updateStmt.setString(1, car.getVin());
            updateStmt.setString(2, car.getNumber());
            updateStmt.setString(3, car.getModel());
            updateStmt.setString(4, car.getFuel());
            updateStmt.setDouble(5, car.getEngineVolume());
            updateStmt.setDate(6, java.sql.Date.valueOf(car.getRentDate()));
            updateStmt.setDouble(7, car.getMileageStart());
            updateStmt.setDate(8, java.sql.Date.valueOf(car.getFirstRegistrationDate()));
            updateStmt.setDouble(9, car.getPriceOfFirstRegistration());
            updateStmt.setInt(10, car.getDaysForReRegistration());
            updateStmt.setDouble(11, car.getPrice());
            updateStmt.setInt(12, car.getId()); // важливо: WHERE id = ?

            int affectedRows = updateStmt.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("Помилка: авто з ID " + car.getId() + " не знайдено.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating car: " + e.getMessage());
        }
    }

    public void deleteCarPermanentlyById(int id) {
        String sql = "DELETE FROM cars WHERE id = ?";

        try (Connection connection = Connect();
             PreparedStatement deleteStmt = connection.prepareStatement(sql)) {

            deleteStmt.setInt(1, id);
            int affectedRows = deleteStmt.executeUpdate();

            if (affectedRows == 0) {
                System.err.println("Авто з ID " + id + " не знайдено.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting car: " + e.getMessage());
        }
    }

    public void deleteCarPermanently(_Car car) {
        if (car != null) {
            deleteCarPermanentlyById(car.getId());
        } else {
            System.err.println("Car object is null.");
        }
    }

    public List<String> getUniqueCarsNumbersBox() {
        List<String> carNumbers = new ArrayList<>();

        String sql = "SELECT * FROM cars";

        try (Connection connection = Connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String vin = resultSet.getString("vin");
                String number = resultSet.getString("number");
                String model = resultSet.getString("model");
                String fuel = resultSet.getString("fuel");
                double engineVolume = resultSet.getDouble("engineVolume");
                LocalDate rentDate = resultSet.getDate("rentdate").toLocalDate();
                double mileageStart = resultSet.getDouble("mileageStart");
                LocalDate firstRegistrationDate = resultSet.getDate("firstRegistrationDate").toLocalDate();
                double priceOfFirstRegistration = resultSet.getDouble("priceOfFirstRegistration");
                int daysForReRegistration = resultSet.getInt("daysForReRegistration");
                double price = resultSet.getDouble("price");
                boolean valid = resultSet.getBoolean("valid");

                _Car car = new _Car(id, vin, number, model, fuel, engineVolume, rentDate, mileageStart, firstRegistrationDate, priceOfFirstRegistration, daysForReRegistration, price, valid);

                carNumbers.add(car.getBoxString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return carNumbers;
    }

    public Integer getCarIdByNumber(String number) {
        String sql = "SELECT id FROM cars WHERE number = ?";
        try (Connection connection = Connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, number);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting car ID by number: " + e.getMessage());
        }
        return null; // якщо не знайдено
    }

    public void markCarAsInvalidById(int id) {
        String sql = "UPDATE cars SET valid = FALSE WHERE id = ?";

        try (Connection connection = Connect();
             PreparedStatement updateStmt = connection.prepareStatement(sql)) {

            updateStmt.setInt(1, id);
            int affectedRows = updateStmt.executeUpdate();

            if (affectedRows == 0) {
                System.err.println("Не знайдено авто з ID " + id + " для деактивації.");
            }
        } catch (SQLException e) {
            System.err.println("Помилка при деактивації авто: " + e.getMessage());
        }
    }

    public void markCarAsInvalid(_Car car) {
        if (car != null) {
            markCarAsInvalidById(car.getId());
        } else {
            System.err.println("Car object is null.");
        }
    }

    private Connection Connect() {
        DBUtil dbUtil = DBUtil.getInstance();
        return  dbUtil.Connect();
    }
}

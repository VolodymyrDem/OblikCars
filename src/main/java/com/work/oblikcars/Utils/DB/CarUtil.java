package com.work.oblikcars.Utils.DB;

import com.work.oblikcars.dto.Registers.CarRegister.CarReportDTO;
import com.work.oblikcars.model.CarReportRow;
import com.work.oblikcars.model._Car;

import java.sql.*;
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

    private _Car mapResultSetToCar(ResultSet rs) throws SQLException {

        _Car car =  new _Car(
                rs.getInt("id"),
                rs.getString("vin"),
                rs.getString("number"),
                rs.getString("model"),
                rs.getInt("year"),
                rs.getString("color"),
                rs.getString("description"),
                rs.getString("fuel"),
                rs.getDouble("engineVolume"),
                rs.getDate("rentdate").toLocalDate(),
                rs.getDouble("mileageStart"),
                rs.getDate("firstRegistrationDate").toLocalDate(),
                rs.getDouble("priceOfFirstRegistration"),
                rs.getDouble("price"),
                rs.getBoolean("valid"),
                rs.getDouble("transportPrice")
        );
        if(rs.getDate("purchaseDate") != null) {
            car.setPurchaseDate(rs.getDate("purchaseDate").toLocalDate());
        }
        if(!rs.getBoolean("valid")) {
            car.setRemoveDate(rs.getDate("removeDate").toLocalDate()
            );
        }
        return car;
    }

    public List<_Car> getAllCars() {
        List<_Car> cars = new ArrayList<>();
        String sql = "SELECT * FROM cars";
        try (Connection con = Connect(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cars.add(mapResultSetToCar(rs));
        } catch (Exception e) { e.printStackTrace(); }
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
            while (resultSet.next())
                cars.add(mapResultSetToCar(resultSet));

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
                if (rs.next()) return mapResultSetToCar(rs);
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
                _Car car = mapResultSetToCar(resultSet);
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
                _Car car = mapResultSetToCar(resultSet);

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
            _Car car = getCarById(carId);
            if (car != null) {
                return car.getMileageStart();
            }

        } catch (SQLException e) {
            System.err.println("Помилка при отриманні пробігу: " + e.getMessage());
        }

        return 0.0;
    }

    public double getDateMileage(int carId, LocalDate date) {
        String sql = """
        SELECT 
            MAX(endmileage) AS max_end_mileage
        FROM lists
        WHERE 
            carid = ? 
            AND done = TRUE 
            AND enddate <= ?
    """;

        try (Connection connection = Connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, carId);
            stmt.setDate(2, java.sql.Date.valueOf(date));  // фільтр за датою

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double maxEndMileage = rs.getDouble("max_end_mileage");
                    if (!rs.wasNull()) {
                        return maxEndMileage;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Помилка при отриманні пробігу: " + e.getMessage());
        }

        // якщо жодного закритого пробігу до цієї дати не знайдено — повертаємо початковий
        _Car car = getCarById(carId);
        return (car != null) ? car.getMileageStart() : 0.0;
    }

    public void addCar(_Car car) {
        String sql = car.isValid()?"INSERT INTO cars (vin, number, year, color, description, model, fuel, engineVolume, rentdate, mileageStart, firstRegistrationDate, priceOfFirstRegistration, price, valid, transportPrice, purchaseDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)":
                                   "INSERT INTO cars (vin, number, year, color, description, model, fuel, engineVolume, rentdate, mileageStart, firstRegistrationDate, priceOfFirstRegistration, price, valid, removeDate, transportPrice, purchaseDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = Connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, car.getVin());
            ps.setString(2, car.getNumber());
            ps.setInt(3, car.getYear());
            ps.setString(4, car.getColor());
            ps.setString(5, car.getDescription());
            ps.setString(6, car.getModel());
            ps.setString(7, car.getFuel());
            ps.setDouble(8, car.getEngineVolume());
            ps.setDate(9, Date.valueOf(car.getRentDate()));
            ps.setDouble(10, car.getMileageStart());
            ps.setDate(11, Date.valueOf(car.getFirstRegistrationDate()));
            ps.setDouble(12, car.getPriceOfFirstRegistration());
            ps.setDouble(13, car.getPrice());
            ps.setBoolean(14, car.isValid());
            ps.setDouble(15, car.getTransportPrice());
            ps.setDate(16, Date.valueOf(car.getPurchaseDate()));
            if(!car.isValid()) {
                ps.setDate(15, Date.valueOf(car.getRemoveDate()));
                ps.setDouble(16, car.getTransportPrice());
                ps.setDate(17, Date.valueOf(car.getPurchaseDate()));
            }



            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void editCar(_Car car) {
        String sql = "UPDATE cars SET vin = ?, number = ?, year = ?, color = ?, description = ?, model = ?, fuel = ?, engineVolume = ?, rentdate = ?, mileageStart = ?, firstRegistrationDate = ?, priceOfFirstRegistration = ?, price = ?, valid = ?, removeDate = ?, transportPrice = ?, purchaseDate = ? WHERE id = ?";
        try (Connection con = Connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, car.getVin());
            ps.setString(2, car.getNumber());
            ps.setInt(3, car.getYear());
            ps.setString(4, car.getColor());
            ps.setString(5, car.getDescription());
            ps.setString(6, car.getModel());
            ps.setString(7, car.getFuel());
            ps.setDouble(8, car.getEngineVolume());
            ps.setDate(9, Date.valueOf(car.getRentDate()));
            ps.setDouble(10, car.getMileageStart());
            ps.setDate(11, Date.valueOf(car.getFirstRegistrationDate()));
            ps.setDouble(12, car.getPriceOfFirstRegistration());
            ps.setDouble(13, car.getPrice());
            ps.setBoolean(14, car.isValid());
            ps.setDate(15, car.isValid()?null:Date.valueOf(car.getRemoveDate()));
            ps.setDouble(16, car.getTransportPrice());
            ps.setDate(17, Date.valueOf(car.getPurchaseDate()));
            ps.setInt(18, car.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteCarPermanentlyById(int id) {
        String sql = "DELETE FROM cars WHERE id = ?";

        try (Connection connection = Connect();
             PreparedStatement deleteStmt = connection.prepareStatement(sql)) {

            deleteStmt.setInt(1, id);
            int affectedRows = deleteStmt.executeUpdate();

            if (affectedRows == 0) {
                System.err.println("Транспортний засіб з ID " + id + " не знайдено.");
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
                carNumbers.add(mapResultSetToCar(resultSet).getBoxString());
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

    public void markCarAsInvalidById(int id, LocalDate date) {
        String sql = "UPDATE cars SET removeDate = ?, valid = FALSE WHERE id = ?";

        try (Connection connection = Connect();
             PreparedStatement updateStmt = connection.prepareStatement(sql)) {

            updateStmt.setDate(1, Date.valueOf(date));
            updateStmt.setInt(2, id);
            int affectedRows = updateStmt.executeUpdate();

            if (affectedRows == 0) {
                System.err.println("Не знайдено Транспортний засіб з ID " + id + " для деактивації.");
            }
        } catch (SQLException e) {
            System.err.println("Помилка при деактивації Транспортний засіб: " + e.getMessage());
        }
    }

    public void markCarAsValidById(int id) {
        String sql = "UPDATE cars SET removeDate=null, valid = TRUE WHERE id = ?";

        try (Connection connection = Connect();
             PreparedStatement updateStmt = connection.prepareStatement(sql)) {

            updateStmt.setInt(1, id);
            int affectedRows = updateStmt.executeUpdate();

            if (affectedRows == 0) {
                System.err.println("Не знайдено Транспортний засіб з ID " + id + " для деактивації.");
            }
        } catch (SQLException e) {
            System.err.println("Помилка при деактивації Транспортний засіб: " + e.getMessage());
        }
    }

    public List<CarReportRow> getCarReportRows(LocalDate reportDate) {

        List<CarReportRow> rows = new ArrayList<>();
        String sql = """
        SELECT * FROM cars
        ORDER BY cars.id
    """;

        try (Connection conn = Connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int idx = 1;
            while (rs.next()) {
                // purchaseDate може бути NULL -> перевіряємо
                Date purchaseSql = rs.getDate("purchaseDate");
                LocalDate purchaseDate = (purchaseSql != null) ? purchaseSql.toLocalDate() : null;

                // Якщо є purchaseDate і вона після reportDate — пропускаємо
                if (purchaseDate == null || purchaseDate.isAfter(reportDate)) {
                    continue;
                }

                int carId            = rs.getInt("id");
                String model         = rs.getString("model");
                String color         = rs.getString("color");
                String number        = rs.getString("number");
                int year             = rs.getInt("year");
                double price         = rs.getDouble("price");
                double startMiles    = rs.getDouble("mileageStart");

                // ЦЕ ВАЖЛИВО: читати як Double (може бути з копійками або NULL)
                Double firstRegD     = rs.getObject("priceOfFirstRegistration", Double.class);
                Double transportD    = rs.getObject("transportPrice", Double.class);

                // якщо CarReportRow очікує int для firstReg — округли/приведи тут:
                // int firstReg = (firstRegD != null) ? (int)Math.round(firstRegD) : 0;
                // але краще, щоб CarReportRow приймав double
                double firstReg = (firstRegD != null) ? firstRegD : 0.0;
                double transportPrice = (transportD != null) ? transportD : 0.0;

                // rentdate у схемі NOT NULL, але нехай буде обережно
                Date rentSql = rs.getDate("rentdate");
                LocalDate rentDate = (rentSql != null) ? rentSql.toLocalDate() : null;

                // Останній пробіг на дату
                double lastMiles = getDateMileage(carId, reportDate);
                double rentalMiles = getRentalMileageTillDate(carId, reportDate);
                if (rentalMiles < 0) rentalMiles = 0.0;

                // "Передано в рент" — якщо rentDate існує і <= reportDate
                boolean rented = (rentDate != null) && !rentDate.isAfter(reportDate);

                rows.add(new CarReportRow(
                        idx++,
                        model,
                        color,
                        number,
                        year,
                        price,
                        rented ? "Так" : "Ні",
                        rentalMiles,
                        // якщо у CarReportRow тут int — заміни параметр на (int)Math.round(firstReg)
                        firstReg,
                        transportPrice,
                        rentDate,
                        purchaseDate,
                        lastMiles
                ));
            }
        } catch (Exception e) { // ловимо будь-які рантайм винятки, не лише SQLException
            e.printStackTrace();
        }

        return rows;
    }

    /**
     * Sum of all finished lists mileage for a car:
     * Σ max(endmileage - startmileage, 0)
     */
    public double getTotalRentalMileage(int carId) {
        String sql = """
        SELECT COALESCE(SUM(GREATEST(endmileage - startmileage, 0)), 0) AS total
        FROM lists
        WHERE carid = ? AND done = TRUE
    """;

        try (Connection connection = Connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, carId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total rental mileage: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Sum of finished lists mileage for a car up to a date (inclusive):
     * only lists with enddate <= reportDate are counted
     */
    public double getRentalMileageTillDate(int carId, LocalDate reportDate) {
        String sql = """
        SELECT COALESCE(SUM(GREATEST(endmileage - startmileage, 0)), 0) AS total
        FROM lists
        WHERE carid = ? AND done = TRUE AND enddate <= ?
    """;

        try (Connection connection = Connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, carId);
            stmt.setDate(2, java.sql.Date.valueOf(reportDate));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating rental mileage till date: " + e.getMessage());
        }
        return 0.0;
    }


    public List<CarReportDTO> getCarReportRowsDTO(LocalDate reportDate) {
        List<CarReportDTO> rows = new ArrayList<>();
        String sql = """
        SELECT *
          FROM cars
      ORDER BY cars.id
    """;

        try (Connection conn = Connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int idx = 1;
            while (rs.next()) {
                // --- purchaseDate ---
                Date purchaseSql = rs.getDate("purchaseDate");
                LocalDate purchaseDate = (purchaseSql != null) ? purchaseSql.toLocalDate() : null;

                // якщо є дата купівлі, але вона після reportDate → пропускаємо
                if (purchaseDate != null && purchaseDate.isAfter(reportDate)) {
                    continue;
                }

                int carId        = rs.getInt("id");
                String model     = rs.getString("model");
                String color     = rs.getString("color");
                String number    = rs.getString("number");
                int year         = rs.getInt("year");
                double price     = rs.getDouble("price");
                double startMiles= rs.getDouble("mileageStart");

                Double firstRegD  = rs.getObject("priceOfFirstRegistration", Double.class);
                Double transportD = rs.getObject("transportPrice", Double.class);

                double firstReg       = (firstRegD != null) ? firstRegD : 0.0;
                double transportPrice = (transportD != null) ? transportD : 0.0;

                Date rentSql = rs.getDate("rentdate");
                LocalDate rentDate = (rentSql != null) ? rentSql.toLocalDate() : null;

                // останній пробіг на дату
                double lastMiles   = getDateMileage(carId, reportDate);
                double rentalMiles = getRentalMileageTillDate(carId, reportDate);
                if (rentalMiles < 0) rentalMiles = 0.0;

                boolean rented = (rentDate != null) && !rentDate.isAfter(reportDate);

                CarReportDTO dto = new CarReportDTO(
                        idx++,
                        model,
                        color,
                        number,
                        year,
                        price,
                        rented ? "Так" : "Ні",
                        rentalMiles,
                        firstReg,
                        transportPrice,
                        rentDate,
                        purchaseDate, // ← може бути null
                        lastMiles
                );
                rows.add(dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rows;
    }




    private Connection Connect() {
        DBUtil dbUtil = DBUtil.getInstance();
        return  dbUtil.Connect();
    }
}

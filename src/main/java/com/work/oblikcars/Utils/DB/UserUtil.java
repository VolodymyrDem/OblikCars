package com.work.oblikcars.Utils.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserUtil {

    private static UserUtil instance;

    private static String GuestUSERNAME = "GUEST";
    private static String GuestPASSWORD = "GUEST";

    private UserUtil() {}

    public static UserUtil getInstance() {
        if (instance == null) {
            instance = new UserUtil();
        }
        return instance;
    }
    public static String getGuestUSERNAME() {
        return GuestUSERNAME;
    }
    public static void  setGuestUSERNAME(String guestUSERNAME) {
        GuestUSERNAME = guestUSERNAME;
    }
    public static String getGuestPASSWORD() {
        return GuestPASSWORD;
    }
    public static void setGuestPASSWORD(String guestPASSWORD) {
        GuestPASSWORD = guestPASSWORD;
    }

    public void addUser(String username, String password) {
        try (Connection connection = Connect();
             Statement statement = connection.createStatement()) {

            String createUserQuery = "CREATE USER IF NOT EXISTS '"+username+"'@'%' IDENTIFIED BY '"+password+"';";
            statement.executeUpdate(createUserQuery);

            String grantPrivilegesQuery = "GRANT ALL PRIVILEGES ON `"+ DBUtil.getInstance().getDatabase() +"`.* TO '"+username+"'@'%';";
            statement.executeUpdate(grantPrivilegesQuery);

            String flushPrivilegesQuery = "FLUSH PRIVILEGES;";
            statement.executeUpdate(flushPrivilegesQuery);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public List<String> getUsers() {
        List<String> users = new ArrayList<>();
        String query = "SELECT DISTINCT  User " +
                "FROM mysql.db " +
                "WHERE Db = ? AND (" +
                "Insert_priv = 'Y' OR " +
                "Update_priv = 'Y' OR " +
                "Delete_priv = 'Y' OR " +
                "Alter_priv = 'Y' OR " +
                "Create_priv = 'Y' OR " +
                "Drop_priv = 'Y' OR " +
                "Grant_priv = 'Y')";

        try (Connection connection = GuestConnect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, DBUtil.getInstance().getDatabase());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String user = resultSet.getString("User");
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    public void deleteUser(String selectedUser) {
        String deletelist = "DROP USER '"+selectedUser+"'@'%';";
        try (Connection connection = Connect();
             CallableStatement deletelistStmt = connection.prepareCall(deletelist)) {
            deletelistStmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error changing order: " + e.getMessage());
        }
    }

    public void CreateGuestUser() {
        List<String> dbnames = new ArrayList<>();
        try (Connection connection = Connect();
             Statement statement = connection.createStatement()) {


            String createUserQuery = "CREATE USER IF NOT EXISTS '"+GuestUSERNAME+"'@'%' IDENTIFIED BY '"+GuestPASSWORD+"';";
            statement.executeUpdate(createUserQuery);

            String grantPrivilegesQuery = "GRANT SHOW DATABASES ON *.* TO '"+GuestUSERNAME+"'@'%';";
            String grantDB = "GRANT SELECT ON mysql.db TO '"+GuestUSERNAME+"'@'%'";

            statement.executeUpdate(grantPrivilegesQuery);
            statement.executeUpdate(grantDB);

            String getDatabasesQuery = "SHOW DATABASES";
            try (ResultSet resultSet = statement.executeQuery(getDatabasesQuery)) {
                while (resultSet.next()) {
                    String dbName = resultSet.getString(1);

                    if (dbName.equalsIgnoreCase("information_schema") ||
                            dbName.equalsIgnoreCase("mysql") ||
                            dbName.equalsIgnoreCase("performance_schema") ||
                            dbName.equalsIgnoreCase("sys")) {
                        continue;
                    }
                    dbnames.add(dbName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try(Connection con = Connect();
            Statement stm = con.createStatement();) {

            for(String dbName : dbnames) {
                String grantQuery = "GRANT SELECT ON `" + dbName + "`.parameters TO '"+GuestUSERNAME+"'@'%';";
                stm.executeUpdate(grantQuery);
            }
            stm.executeUpdate("FLUSH PRIVILEGES");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection Connect() {
        DBUtil dbUtil = DBUtil.getInstance();
        return  dbUtil.Connect();
    }

    private Connection GuestConnect() {
        DBUtil dbUtil = DBUtil.getInstance();
        return  dbUtil.GuestConnect();
    }
}

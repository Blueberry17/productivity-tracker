package com.tracker.database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DatabaseManager {
    // TODO (back-end): implement getConnection() and initialise()
    private static final String URL = "jdbc:sqlite:tracker.db";

    // Get a connection to the database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // Initialise database (create tables)
    public static void initialise() {
        String sql =
                //create Sleep table
                "CREATE TABLE IF NOT EXISTS Sleep (" +
                "entryID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "entryDate TEXT NOT NULL," + //no specific DATE data type in SQLite, but TEXT is compatible with date methods
                "duration_hrs REAL NOT NULL," +
                "quality INTEGER NOT NULL);" +

                //create Screen time table
                "CREATE TABLE IF NOT EXISTS ScreenTime (" +
                "entryID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "entryDate TEXT NOT NULL," +
                "category TEXT NOT NULL," +
                "duration_mins INTEGER NOT NULL);" +

                //create Habits table
                "CREATE TABLE IF NOT EXISTS Habits (" +
                "habitID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "habitName TEXT NOT NULL," +
                "streak_days INTEGER NOT NULL, " +
                "isCompleted INTEGER NOT NULL);" +  //no BOOLEAN data type, but 0/1 recommended by documentation

                //create Goals table
                "CREATE TABLE IF NOT EXISTS Goals (" +
                "goalID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "goalName TEXT NOT NULL," +
                "unit TEXT NOT NULL," +
                "target INTEGER NOT NULL);" +

                "CREATE TABLE IF NOT EXISTS Pomodoro (" +
                "entryID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "entryDate TEXT NOT NULL," +
                "isCompleted INTEGER NOT NULL," +
                "durationRemaining TEXT NOT NULL);";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            System.out.println("Database initialised.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Most recent sleep entry's duration, or -1 if no data. */
    public static double getLastNightSleepHours() {
        String sql = "SELECT duration_hrs FROM Sleep ORDER BY entryDate DESC, entryID DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Sleep hours for each of the last 7 days (index 0 = 6 days ago, index 6 = today). Days with no entry are 0.
    public static double[] getSleepLast7Days() {
        double[] hours = new double[7];
        String sql = "SELECT entryDate, SUM(duration_hrs) FROM Sleep " +
                     "WHERE entryDate >= date('now','localtime','-6 days') " +
                     "GROUP BY entryDate ORDER BY entryDate ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            LocalDate today = LocalDate.now();
            while (rs.next()) {
                LocalDate date = LocalDate.parse(rs.getString(1), DateTimeFormatter.ISO_LOCAL_DATE);
                int idx = (int) (today.toEpochDay() - date.toEpochDay());
                if (idx >= 0 && idx < 7) hours[6 - idx] = rs.getDouble(2);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hours;
    }

    // Total screen time logged today, in minutes.
    public static int getTodayScreenTimeMinutes() {
        String sql = "SELECT COALESCE(SUM(duration_mins), 0) FROM ScreenTime " +
                     "WHERE entryDate = date('now','localtime')";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Returns {completed, total} habit counts.
    public static int[] getHabitsSummary() {
        String sqlTotal = "SELECT COUNT(*) FROM Habits";
        String sqlDone  = "SELECT COUNT(*) FROM Habits WHERE isCompleted = 1";
        int total = 0, done = 0;
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlTotal);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) total = rs.getInt(1);
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlDone);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) done = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new int[]{done, total};
    }

    // Returns {completed, total} pomodoro session counts for today.
    public static int[] getPomodoroSummaryToday() {
        String sqlTotal = "SELECT COUNT(*) FROM Pomodoro WHERE entryDate = date('now','localtime')";
        String sqlDone  = "SELECT COUNT(*) FROM Pomodoro WHERE entryDate = date('now','localtime') AND isCompleted = 1";
        int total = 0, done = 0;
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlTotal);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) total = rs.getInt(1);
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlDone);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) done = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new int[]{done, total};
    }
}
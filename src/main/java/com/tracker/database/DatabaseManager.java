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
    private static final String URL = "jdbc:sqlite:tracker.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initialise() {
        String[] statements = {
            "CREATE TABLE IF NOT EXISTS Sleep (" +
                "entryID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "entryDate TEXT NOT NULL," +
                "duration_hrs REAL NOT NULL," +
                "quality INTEGER NOT NULL)",

            "CREATE TABLE IF NOT EXISTS ScreenTime (" +
                "entryID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "entryDate TEXT NOT NULL," +
                "category TEXT NOT NULL," +
                "duration_mins INTEGER NOT NULL)",

            "CREATE TABLE IF NOT EXISTS Habits (" +
                "habitID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "habitName TEXT NOT NULL," +
                "streak_days INTEGER NOT NULL," +
                "isCompleted INTEGER NOT NULL," +
                "lastCheckedDate TEXT)",

            "CREATE TABLE IF NOT EXISTS Goals (" +
                "goalID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "goalName TEXT NOT NULL," +
                "unit TEXT NOT NULL," +
                "target REAL NOT NULL," +
                "current REAL DEFAULT 0)",

            "CREATE TABLE IF NOT EXISTS Pomodoro (" +
                "entryID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "entryDate TEXT NOT NULL," +
                "isCompleted INTEGER NOT NULL," +
                "durationRemaining TEXT NOT NULL)",

            "CREATE TABLE IF NOT EXISTS DailyTargets (" +
                "metricName TEXT NOT NULL PRIMARY KEY," +
                "targetValue REAL NOT NULL)"
        };

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                stmt.execute(sql);
            }
            System.out.println("Database initialised.");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        seedDailyTargets();
    }

    private static void seedDailyTargets() {
        String sql = "INSERT OR IGNORE INTO DailyTargets (metricName, targetValue) VALUES (?, ?)";
        String[][] defaults = {{"sleep_hours", "8.0"}, {"screen_time_mins", "240"}};
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String[] d : defaults) {
                ps.setString(1, d[0]);
                ps.setDouble(2, Double.parseDouble(d[1]));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static double getDailyTarget(String metricName, double defaultValue) {
        String sql = "SELECT targetValue FROM DailyTargets WHERE metricName = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, metricName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public static void setDailyTarget(String metricName, double value) {
        String sql = "INSERT OR REPLACE INTO DailyTargets (metricName, targetValue) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, metricName);
            ps.setDouble(2, value);
            ps.executeUpdate();
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

    // Sleep hours for each of the last 7 days (index 0 = 6 days ago, index 6 = today).
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
                try {
                    LocalDate date = LocalDate.parse(rs.getString(1), DateTimeFormatter.ISO_LOCAL_DATE);
                    int idx = (int) (today.toEpochDay() - date.toEpochDay());
                    if (idx >= 0 && idx < 7) hours[6 - idx] = rs.getDouble(2);
                } catch (java.time.format.DateTimeParseException ignored) {}
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hours;
    }

    // Screen time in minutes for each of the last 7 days (index 0 = 6 days ago, index 6 = today).
    public static int[] getScreenTimeLast7Days() {
        int[] mins = new int[7];
        String sql = "SELECT entryDate, SUM(duration_mins) FROM ScreenTime " +
                     "WHERE entryDate >= date('now','localtime','-6 days') " +
                     "GROUP BY entryDate ORDER BY entryDate ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            LocalDate today = LocalDate.now();
            while (rs.next()) {
                try {
                    LocalDate date = LocalDate.parse(rs.getString(1), DateTimeFormatter.ISO_LOCAL_DATE);
                    int idx = (int) (today.toEpochDay() - date.toEpochDay());
                    if (idx >= 0 && idx < 7) mins[6 - idx] = rs.getInt(2);
                } catch (java.time.format.DateTimeParseException ignored) {}
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mins;
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

    // Sleep quality (1-5) for each of the last 7 days (0 where no entry).
    public static double[] getQualityLast7Days() {
        double[] quality = new double[7];
        String sql = "SELECT entryDate, AVG(quality) FROM Sleep " +
                     "WHERE entryDate >= date('now','localtime','-6 days') " +
                     "GROUP BY entryDate ORDER BY entryDate ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            LocalDate today = LocalDate.now();
            while (rs.next()) {
                try {
                    LocalDate date = LocalDate.parse(rs.getString(1), DateTimeFormatter.ISO_LOCAL_DATE);
                    int idx = (int) (today.toEpochDay() - date.toEpochDay());
                    if (idx >= 0 && idx < 7) quality[6 - idx] = rs.getDouble(2);
                } catch (java.time.format.DateTimeParseException ignored) {}
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return quality;
    }

    // Pomodoro sessions completed for each of the last 7 days.
    public static int[] getPomodoroLast7Days() {
        int[] sessions = new int[7];
        String sql = "SELECT entryDate, COUNT(*) FROM Pomodoro " +
                     "WHERE entryDate >= date('now','localtime','-6 days') AND isCompleted = 1 " +
                     "GROUP BY entryDate ORDER BY entryDate ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            LocalDate today = LocalDate.now();
            while (rs.next()) {
                try {
                    LocalDate date = LocalDate.parse(rs.getString(1), DateTimeFormatter.ISO_LOCAL_DATE);
                    int idx = (int) (today.toEpochDay() - date.toEpochDay());
                    if (idx >= 0 && idx < 7) sessions[6 - idx] = rs.getInt(2);
                } catch (java.time.format.DateTimeParseException ignored) {}
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sessions;
    }

    // Returns [thisWeekAvgHrs, lastWeekAvgHrs] for sleep duration. 0 if no data.
    public static double[] getSleepWeekComparison() {
        String thisWeek = "SELECT AVG(duration_hrs) FROM Sleep " +
                          "WHERE entryDate >= date('now','localtime','-6 days')";
        String lastWeek = "SELECT AVG(duration_hrs) FROM Sleep " +
                          "WHERE entryDate >= date('now','localtime','-13 days') " +
                          "AND entryDate < date('now','localtime','-6 days')";
        return queryTwoWeeks(thisWeek, lastWeek);
    }

    // Returns [thisWeekTotalMins, lastWeekTotalMins] for screen time. 0 if no data.
    public static double[] getScreenTimeWeekComparison() {
        String thisWeek = "SELECT COALESCE(SUM(duration_mins), 0) FROM ScreenTime " +
                          "WHERE entryDate >= date('now','localtime','-6 days')";
        String lastWeek = "SELECT COALESCE(SUM(duration_mins), 0) FROM ScreenTime " +
                          "WHERE entryDate >= date('now','localtime','-13 days') " +
                          "AND entryDate < date('now','localtime','-6 days')";
        return queryTwoWeeks(thisWeek, lastWeek);
    }

    // Returns [thisWeekAvgQuality, lastWeekAvgQuality]. 0 if no data.
    public static double[] getSleepQualityWeekComparison() {
        String thisWeek = "SELECT AVG(quality) FROM Sleep " +
                          "WHERE entryDate >= date('now','localtime','-6 days')";
        String lastWeek = "SELECT AVG(quality) FROM Sleep " +
                          "WHERE entryDate >= date('now','localtime','-13 days') " +
                          "AND entryDate < date('now','localtime','-6 days')";
        return queryTwoWeeks(thisWeek, lastWeek);
    }

    // Returns [thisWeekCount, lastWeekCount] for completed Pomodoro sessions.
    public static double[] getPomodoroWeekComparison() {
        String thisWeek = "SELECT COUNT(*) FROM Pomodoro " +
                          "WHERE entryDate >= date('now','localtime','-6 days') AND isCompleted = 1";
        String lastWeek = "SELECT COUNT(*) FROM Pomodoro " +
                          "WHERE entryDate >= date('now','localtime','-13 days') " +
                          "AND entryDate < date('now','localtime','-6 days') AND isCompleted = 1";
        return queryTwoWeeks(thisWeek, lastWeek);
    }

    private static double[] queryTwoWeeks(String thisWeekSql, String lastWeekSql) {
        double[] result = new double[2];
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(thisWeekSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) result[0] = rs.getDouble(1);
            }
            try (PreparedStatement ps = conn.prepareStatement(lastWeekSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) result[1] = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
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
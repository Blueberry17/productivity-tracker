package com.tracker.database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
}
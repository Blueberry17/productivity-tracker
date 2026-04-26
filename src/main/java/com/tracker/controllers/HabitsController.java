package com.tracker.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.tracker.database.DatabaseManager;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class HabitsController {

    @FXML private VBox habitListContainer;
    @FXML private TextField newHabitField;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        resetDailyCompletions();
        habitListContainer.getChildren().clear();

        String sql = "SELECT * FROM Habits";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String name  = rs.getString("habitName");
                boolean done = rs.getInt("isCompleted") == 1;
                int streak   = rs.getInt("streak_days");
                int habitID  = rs.getInt("habitID");

                CheckBox check = new CheckBox(name);
                check.setSelected(done);
                check.setOnAction(e -> updateHabitCompletion(habitID, check.isSelected()));
                check.getStyleClass().add("habit-check");
                HBox.setHgrow(check, Priority.ALWAYS);
                check.setMaxWidth(Double.MAX_VALUE);

                HBox row = new HBox(12);
                row.getStyleClass().add("habit-row");
                row.setPadding(new Insets(8, 4, 8, 4));
                row.getChildren().add(check);

                if (streak > 0) {
                    Label badge = new Label("🔥 " + streak + " day streak");
                    badge.getStyleClass().add("streak-badge");
                    row.getChildren().add(badge);
                }

                habitListContainer.getChildren().addAll(row, new Separator());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Runs on every open: unchecks habits not completed today and breaks streaks for missed days.
    private void resetDailyCompletions() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            // Uncheck any habit that wasn't checked today
            stmt.execute("UPDATE Habits SET isCompleted = 0 " +
                         "WHERE lastCheckedDate IS NULL OR lastCheckedDate != '" + today + "'");
            // Break streak for any habit that missed yesterday (last check was before yesterday)
            stmt.execute("UPDATE Habits SET streak_days = 0 " +
                         "WHERE streak_days > 0 AND (" +
                         "lastCheckedDate IS NULL OR " +
                         "lastCheckedDate < date('now','localtime','-1 day'))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onAddHabit() {
        String habitName = newHabitField.getText();
        if (habitName == null || habitName.trim().isEmpty()) return;

        String sql = "INSERT INTO Habits (habitName, streak_days, isCompleted) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, habitName);
            stmt.setInt(2, 0);
            stmt.setInt(3, 0);
            stmt.executeUpdate();
            newHabitField.clear();
            initialize();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateHabitCompletion(int habitID, boolean isCompleted) {
        if (isCompleted) {
            String today     = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);

            String selectSql = "SELECT streak_days, lastCheckedDate FROM Habits WHERE habitID = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement select = conn.prepareStatement(selectSql)) {
                select.setInt(1, habitID);
                try (ResultSet rs = select.executeQuery()) {
                    if (rs.next()) {
                        int streak   = rs.getInt("streak_days");
                        String last  = rs.getString("lastCheckedDate");

                        int newStreak;
                        if (today.equals(last)) {
                            newStreak = streak; // already checked today, re-checking
                        } else if (yesterday.equals(last)) {
                            newStreak = streak + 1; // consecutive day
                        } else {
                            newStreak = 1; // first check or missed a day
                        }

                        String updateSql = "UPDATE Habits SET isCompleted = 1, streak_days = ?, " +
                                           "lastCheckedDate = ? WHERE habitID = ?";
                        try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                            update.setInt(1, newStreak);
                            update.setString(2, today);
                            update.setInt(3, habitID);
                            update.executeUpdate();
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // Uncheck only — leave streak and lastCheckedDate untouched
            String sql = "UPDATE Habits SET isCompleted = 0 WHERE habitID = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, habitID);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        initialize();
    }
}
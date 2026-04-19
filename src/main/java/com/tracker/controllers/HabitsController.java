package com.tracker.controllers;

import java.sql.Connection; 
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

        habitListContainer.getChildren().clear(); // clear existing habits

        String sql = "SELECT * FROM Habits";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String name   = rs.getString("habitName");
                boolean done  = rs.getInt("isCompleted") == 1;
                int streak    = rs.getInt("streak_days");

                CheckBox check = new CheckBox(name);
                check.setSelected(done);

                int habitID = rs.getInt("habitID");

                check.setOnAction(e -> {
                    updateHabitCompletion(habitID, check.isSelected());

                });

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


        }
        catch (SQLException e) {
            e.printStackTrace();  
        }
    }

    @FXML
    public void onAddHabit() {
        
        String habitName = newHabitField.getText();

        // Basic validation
        if (habitName == null || habitName.trim().isEmpty()) {
            System.out.println("Habit name cannot be empty");
            return;
        }

        String sql = "INSERT INTO Habits (habitName, streak_days, isCompleted) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, habitName);
            stmt.setInt(2, 0);
            stmt.setInt(3, 0);

            stmt.executeUpdate();
            
            System.out.println("Habit added: " + habitName);

            newHabitField.clear();

            initialize(); // Refresh the habit list to show the new habit

        }
        catch (SQLException e) {
            e.printStackTrace();  
        }

    }
    
    public void updateHabitCompletion(int habitID, boolean isCompleted) {
        String sql = "UPDATE Habits SET isCompleted = ? WHERE habitID = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, isCompleted ? 1 : 0);
            stmt.setInt(2, habitID);

            stmt.executeUpdate();
            
            System.out.println("Habit updated: ID=" + habitID + ", Completed=" + isCompleted);

            initialize(); // Refresh the habit list to reflect changes

        }
        catch (SQLException e) {
            e.printStackTrace();  
        }
    }
}
package com.tracker.controllers;

import com.tracker.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ScreenTimeController {

    @FXML private PieChart pieChart;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField minutesField;
    @FXML private Label totalLabel;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        datePicker.setValue(LocalDate.now());

        categoryCombo.getItems().addAll(
            "Study / Work", "Social Media", "Video / Streaming",
            "Gaming", "Communication", "Other"
        );
        categoryCombo.getSelectionModel().selectFirst();

        pieChart.setAnimated(false);
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> refreshChart());
        refreshChart();
    }

    private void refreshChart() {
        LocalDate date = datePicker.getValue();
        if (date == null) return;
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);

        String sql = "SELECT category, SUM(duration_mins) FROM ScreenTime " +
                     "WHERE entryDate = ? GROUP BY category ORDER BY SUM(duration_mins) DESC";

        pieChart.getData().clear();
        int totalMins = 0;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dateStr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String category = rs.getString(1);
                    int mins = rs.getInt(2);
                    totalMins += mins;
                    pieChart.getData().add(new PieChart.Data(category, mins));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int hours = totalMins / 60;
        int mins = totalMins % 60;
        if (totalMins == 0) {
            totalLabel.setText("No entries for this date");
        } else if (hours > 0) {
            totalLabel.setText(hours + "h " + mins + "m total");
        } else {
            totalLabel.setText(mins + "m total");
        }
    }

    @FXML
    public void onLog() {
        LocalDate date = datePicker.getValue();
        String minutesText = minutesField.getText().trim();

        if (date == null || minutesText.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        int minutes;
        try {
            minutes = Integer.parseInt(minutesText);
            if (minutes <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            statusLabel.setText("Minutes must be a positive whole number.");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        String category = categoryCombo.getValue();
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);

        String sql = "INSERT INTO ScreenTime (entryDate, category, duration_mins) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dateStr);
            ps.setString(2, category);
            ps.setInt(3, minutes);
            ps.executeUpdate();

            minutesField.clear();
            statusLabel.setText("Logged " + minutes + " min of " + category + ".");
            statusLabel.setStyle("-fx-text-fill: #27ae60;");
            refreshChart();
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error saving entry.");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }
}
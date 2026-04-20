package com.tracker.controllers;

import com.tracker.database.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

public class SleepController {

    @FXML private BarChart<String, Number> sleepChart;
    @FXML private DatePicker datePicker;
    @FXML private TextField hoursField;
    @FXML private Slider qualitySlider;
    @FXML private Label qualityLabel;
    @FXML private Label avgLabel;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        sleepChart.getData().clear();
        datePicker.setValue(LocalDate.now());
        qualityLabel.setText("3 / 5");
        qualitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            qualityLabel.setText((int) Math.round(newVal.doubleValue()) + " / 5");
        });

        // get results for the last 7 days in correct order
        String sqlSelect = "SELECT * FROM (SELECT entryDate, duration_hrs FROM Sleep ORDER BY entryDate DESC LIMIT 7) ORDER BY entryDate ASC";
        String[] days = new String[7];
        double[] hours = new double[7];
        int index = 0;
        double avg;

        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlSelect);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                days[index] = rs.getString("entryDate"); // put results into two arrays, one for days one for hours
                hours[index] = rs.getDouble("duration_hrs");
                index++;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        double sum = 0;
        for (double h : hours) sum += h;
        if (index > 0) {
            avg = sum / index;
        } else {
            avg = 0;
        }
        avgLabel.setText(String.format("7-day avg: %.1fh", avg));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < index; i++)
            series.getData().add(new XYChart.Data<>(days[i], hours[i]));

        sleepChart.setAnimated(false);
        sleepChart.setLegendVisible(false);
        sleepChart.getData().add(series);
        statusLabel.setText("");
    }

    @FXML
    public void onLogSleep() {
        LocalDate date = datePicker.getValue();
        double hours = Double.parseDouble(hoursField.getText().trim());
        int quality = (int) qualitySlider.getValue();
        String dateStr = date.format(java.time.format.DateTimeFormatter.ofPattern("d MMM"));

        try (Connection conn = DatabaseManager.getConnection()) {

            // Check for duplicate
            PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM Sleep WHERE entryDate = ?");
            check.setString(1, dateStr);
            ResultSet rs = check.executeQuery();
            if (rs.next() && rs.getInt(1) > 0){
                    statusLabel.setText("Entry already exists for this date.");
                    return;
            }

            // Insert
            PreparedStatement insert = conn.prepareStatement("INSERT INTO Sleep (entryDate, duration_hrs, quality) VALUES (?, ?, ?)");
            insert.setString(1, dateStr);
            insert.setDouble(2, hours);
            insert.setInt(3, quality);
            insert.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        initialize();
    }
}
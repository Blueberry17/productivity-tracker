package com.tracker.controllers;

import com.tracker.database.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import java.time.format.DateTimeFormatter;

public class SleepController {

    @FXML private BarChart<String, Number> sleepChart;
    @FXML private LineChart<String, Number> qualityChart;
    @FXML private DatePicker datePicker;
    @FXML private TextField hoursField;
    @FXML private Slider qualitySlider;
    @FXML private Label qualityLabel;
    @FXML private Label avgLabel;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        sleepChart.getData().clear();
        qualityChart.getData().clear();
        datePicker.setValue(LocalDate.now());
        qualityLabel.setText("3 / 5");
        qualitySlider.valueProperty().addListener((obs, oldVal, newVal) ->
            qualityLabel.setText((int) Math.round(newVal.doubleValue()) + " / 5"));

        String sqlSelect = "SELECT * FROM (SELECT entryDate, duration_hrs FROM Sleep " +
                           "ORDER BY entryDate DESC LIMIT 7) ORDER BY entryDate ASC";
        String[] days  = new String[7];
        double[] hours = new double[7];
        int index = 0;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlSelect);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                days[index]  = rs.getString("entryDate");
                hours[index] = rs.getDouble("duration_hrs");
                index++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        double sum = 0;
        for (double h : hours) sum += h;
        avgLabel.setText(index > 0
            ? String.format("7-day avg: %.1fh", sum / index)
            : "7-day avg: —");

        DateTimeFormatter displayFmt = DateTimeFormatter.ofPattern("d MMM");
        XYChart.Series<String, Number> hoursSeries = new XYChart.Series<>();
        for (int i = 0; i < index; i++) {
            String label;
            try { label = LocalDate.parse(days[i]).format(displayFmt); }
            catch (Exception ex) { label = days[i]; }
            hoursSeries.getData().add(new XYChart.Data<>(label, hours[i]));
        }
        sleepChart.setAnimated(false);
        sleepChart.setLegendVisible(false);
        sleepChart.getData().add(hoursSeries);

        loadQualityChart();
        statusLabel.setText("");
    }

    private void loadQualityChart() {
        double[] quality = DatabaseManager.getQualityLast7Days();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Quality (1–5)");
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMM");
        for (int i = 0; i < 7; i++) {
            String label = today.minusDays(6 - i).format(fmt);
            if (quality[i] > 0) {
                series.getData().add(new XYChart.Data<>(label, quality[i]));
            }
        }
        qualityChart.setAnimated(false);
        qualityChart.getData().add(series);
    }

    @FXML
    public void onLogSleep() {
        LocalDate date  = datePicker.getValue();
        double hours    = Double.parseDouble(hoursField.getText().trim());
        int quality     = (int) qualitySlider.getValue();
        String dateStr  = date.format(DateTimeFormatter.ISO_LOCAL_DATE);

        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement check = conn.prepareStatement(
                "SELECT COUNT(*) FROM Sleep WHERE entryDate = ?");
            check.setString(1, dateStr);
            ResultSet rs = check.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                statusLabel.setText("Entry already exists for this date.");
                return;
            }

            PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO Sleep (entryDate, duration_hrs, quality) VALUES (?, ?, ?)");
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
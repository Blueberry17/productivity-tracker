package com.tracker.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

import java.time.LocalDate;

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
        datePicker.setValue(LocalDate.now());
        qualityLabel.setText("3 / 5");
        avgLabel.setText("7-day avg: 7.3h");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] days  = {"3 Apr", "4 Apr", "5 Apr", "6 Apr", "7 Apr", "8 Apr", "9 Apr"};
        double[] hours = {6.5, 7.0, 8.2, 5.8, 7.5, 8.8, 7.2};
        for (int i = 0; i < days.length; i++)
            series.getData().add(new XYChart.Data<>(days[i], hours[i]));
        sleepChart.setAnimated(false);
        sleepChart.setLegendVisible(false);
        sleepChart.getData().add(series);
    }

    @FXML
    public void onLogSleep() {}
}
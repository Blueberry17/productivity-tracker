package com.tracker.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class DashboardController {

    @FXML private Label greetingLabel;
    @FXML private Label dateLabel;

    @FXML private Label sleepValue;
    @FXML private Label sleepSub;
    @FXML private Label screenValue;
    @FXML private Label screenSub;
    @FXML private Label productivityValue;
    @FXML private Label productivitySub;
    @FXML private Label habitsValue;
    @FXML private Label habitsSub;
    @FXML private Label avgLabel;

    @FXML private BarChart<String, Number> sleepMiniChart;
    @FXML private ProgressBar productivityBar;

    @FXML
    public void initialize() {
        greetingLabel.setText("Good afternoon, Ed");
        dateLabel.setText("Thursday, 9 April 2026");

        sleepValue.setText("7.2h");
        sleepSub.setText("+0.4h vs goal");
        sleepSub.getStyleClass().add("good");

        screenValue.setText("4h 18m");
        screenSub.setText("Near daily limit");
        screenSub.getStyleClass().add("warn");

        productivityValue.setText("74%");
        productivitySub.setText("+8% vs yesterday");
        productivitySub.getStyleClass().add("good");
        productivityBar.setProgress(0.74);

        habitsValue.setText("3 / 5");
        habitsSub.setText("2 remaining today");
        habitsSub.getStyleClass().add("warn");

        avgLabel.setText("7-day avg: 7.3h");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] days  = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        double[] hours = {6.5, 7.0, 8.2, 5.8, 7.5, 8.8, 7.2};
        for (int i = 0; i < days.length; i++)
            series.getData().add(new XYChart.Data<>(days[i], hours[i]));
        sleepMiniChart.setAnimated(false);
        sleepMiniChart.setLegendVisible(false);
        sleepMiniChart.getData().add(series);
    }
}
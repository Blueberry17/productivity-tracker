package com.tracker.controllers;

import com.tracker.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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
        setGreetingAndDate();
        loadSleep();
        loadScreenTime();
        loadHabits();
        loadProductivity();
    }

    private void setGreetingAndDate() {
        int hour = LocalTime.now().getHour();
        String timeOfDay = hour < 12 ? "morning" : hour < 18 ? "afternoon" : "evening";
        greetingLabel.setText("Good " + timeOfDay + ", Ed");
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")));
    }

    private void loadSleep() {
        double lastNight = DatabaseManager.getLastNightSleepHours();
        double[] last7 = DatabaseManager.getSleepLast7Days();

        if (lastNight < 0) {
            sleepValue.setText("—");
            sleepSub.setText("No data logged");
        } else {
            sleepValue.setText(String.format("%.1fh", lastNight));
            double diff = lastNight - 8.0;
            if (diff >= 0) {
                sleepSub.setText(String.format("+%.1fh vs goal", diff));
                sleepSub.getStyleClass().add("good");
            } else {
                sleepSub.setText(String.format("%.1fh vs goal", diff));
                sleepSub.getStyleClass().add("warn");
            }
        }

        double sum = 0;
        int count = 0;
        for (double h : last7) { if (h > 0) { sum += h; count++; } }
        avgLabel.setText(count > 0
                ? String.format("7-day avg: %.1fh", sum / count)
                : "7-day avg: —");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            String label = today.minusDays(6 - i).format(DateTimeFormatter.ofPattern("EEE"));
            series.getData().add(new XYChart.Data<>(label, last7[i]));
        }
        sleepMiniChart.setAnimated(false);
        sleepMiniChart.setLegendVisible(false);
        sleepMiniChart.getData().add(series);
    }

    private void loadScreenTime() {
        int totalMins = DatabaseManager.getTodayScreenTimeMinutes();
        int h = totalMins / 60;
        int m = totalMins % 60;
        screenValue.setText(h > 0 ? h + "h " + m + "m" : m + "m");

        if (totalMins == 0) {
            screenSub.setText("Nothing logged today");
        } else if (totalMins >= 240) {
            screenSub.setText("Above 4h daily limit");
            screenSub.getStyleClass().add("warn");
        } else {
            screenSub.setText("Within daily limit");
            screenSub.getStyleClass().add("good");
        }
    }

    private void loadHabits() {
        int[] habits = DatabaseManager.getHabitsSummary();
        int done = habits[0], total = habits[1];

        if (total == 0) {
            habitsValue.setText("—");
            habitsSub.setText("No habits tracked");
        } else {
            habitsValue.setText(done + " / " + total);
            int remaining = total - done;
            if (remaining == 0) {
                habitsSub.setText("All done today!");
                habitsSub.getStyleClass().add("good");
            } else {
                habitsSub.setText(remaining + " remaining today");
                habitsSub.getStyleClass().add("warn");
            }
        }
    }

    private void loadProductivity() {
        int[] pomo = DatabaseManager.getPomodoroSummaryToday();
        int done = pomo[0], total = pomo[1];

        if (total == 0) {
            productivityValue.setText("—");
            productivitySub.setText("No sessions today");
            productivityBar.setProgress(0);
        } else {
            double ratio = (double) done / total;
            int pct = (int) Math.round(ratio * 100);
            productivityValue.setText(pct + "%");
            productivitySub.setText(done + " of " + total + " sessions done");
            productivitySub.getStyleClass().add(pct >= 50 ? "good" : "warn");
            productivityBar.setProgress(ratio);
        }
    }
}
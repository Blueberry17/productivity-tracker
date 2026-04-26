package com.tracker.controllers;

import com.tracker.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

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
    @FXML private LineChart<String, Number> trendsChart;
    @FXML private VBox insightsContainer;

    @FXML
    public void initialize() {
        setGreetingAndDate();
        loadSleep();
        loadScreenTime();
        loadHabits();
        loadProductivity();
        loadTrendsChart();
        loadInsights();
    }

    private void setGreetingAndDate() {
        int hour = LocalTime.now().getHour();
        String timeOfDay = hour < 12 ? "morning" : hour < 18 ? "afternoon" : "evening";
        greetingLabel.setText("Good " + timeOfDay + ", Ed");
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")));
    }

    private void loadSleep() {
        double lastNight = DatabaseManager.getLastNightSleepHours();
        double[] last7   = DatabaseManager.getSleepLast7Days();
        double sleepGoal = DatabaseManager.getDailyTarget("sleep_hours", 8.0);

        if (lastNight < 0) {
            sleepValue.setText("—");
            sleepSub.setText("No data logged");
        } else {
            sleepValue.setText(String.format("%.1fh", lastNight));
            double diff = lastNight - sleepGoal;
            if (diff >= 0) {
                sleepSub.setText(String.format("+%.1fh vs goal", diff));
                sleepSub.getStyleClass().add("good");
            } else {
                sleepSub.setText(String.format("%.1fh vs goal", diff));
                sleepSub.getStyleClass().add("warn");
            }
        }

        double sum = 0; int count = 0;
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
        int totalMins   = DatabaseManager.getTodayScreenTimeMinutes();
        int screenLimit = (int) DatabaseManager.getDailyTarget("screen_time_mins", 240);
        int h = totalMins / 60, m = totalMins % 60;
        screenValue.setText(h > 0 ? h + "h " + m + "m" : m + "m");

        if (totalMins == 0) {
            screenSub.setText("Nothing logged today");
        } else if (totalMins >= screenLimit) {
            screenSub.setText("Above daily limit");
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
        int done = pomo[0];

        if (done == 0) {
            productivityValue.setText("—");
            productivitySub.setText("No sessions today");
        } else {
            productivityValue.setText(String.valueOf(done));
            productivitySub.setText(done == 1 ? "focus session today" : "focus sessions today");
            productivitySub.getStyleClass().add("good");
        }
    }

    // Plots sleep (hrs), screen time (hrs) and sleep quality over the last 7 days.
    private void loadTrendsChart() {
        double[] sleepData   = DatabaseManager.getSleepLast7Days();
        int[]    screenData  = DatabaseManager.getScreenTimeLast7Days();
        double[] qualityData = DatabaseManager.getQualityLast7Days();

        XYChart.Series<String, Number> sleepSeries   = new XYChart.Series<>();
        XYChart.Series<String, Number> screenSeries  = new XYChart.Series<>();
        XYChart.Series<String, Number> qualitySeries = new XYChart.Series<>();
        sleepSeries.setName("Sleep (hrs)");
        screenSeries.setName("Screen time (hrs)");
        qualitySeries.setName("Sleep quality (1–5)");

        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE");
        for (int i = 0; i < 7; i++) {
            String label = today.minusDays(6 - i).format(fmt);
            sleepSeries.getData().add(new XYChart.Data<>(label, sleepData[i]));
            screenSeries.getData().add(new XYChart.Data<>(label, screenData[i] / 60.0));
            qualitySeries.getData().add(new XYChart.Data<>(label, qualityData[i]));
        }

        trendsChart.setAnimated(false);
        trendsChart.getData().addAll(sleepSeries, screenSeries, qualitySeries);
    }

    private void loadInsights() {
        insightsContainer.getChildren().clear();

        // Sleep duration
        double[] sleep = DatabaseManager.getSleepWeekComparison();
        if (sleep[0] > 0 && sleep[1] > 0) {
            double pct = ((sleep[0] - sleep[1]) / sleep[1]) * 100;
            if (pct >= 5)
                addInsight(String.format("You slept %.0f%% more this week than last. Well rested!", pct), "good");
            else if (pct <= -5)
                addInsight(String.format("You slept %.0f%% less this week than last. Try to get more rest.", Math.abs(pct)), "warn");
            else
                addInsight("Your sleep duration is consistent with last week.", "neutral");
        } else if (sleep[0] > 0) {
            addInsight(String.format("Averaging %.1fh of sleep per night this week.", sleep[0]), "neutral");
        }

        // Screen time
        double[] screen = DatabaseManager.getScreenTimeWeekComparison();
        if (screen[0] > 0 && screen[1] > 0) {
            double pct = ((screen[0] - screen[1]) / screen[1]) * 100;
            if (pct <= -5)
                addInsight(String.format("Screen time is down %.0f%% vs last week. Keep it up!", Math.abs(pct)), "good");
            else if (pct >= 5)
                addInsight(String.format("Screen time is up %.0f%% vs last week. Consider cutting back.", pct), "warn");
            else
                addInsight("Your screen time is consistent with last week.", "neutral");
        } else if (screen[0] > 0) {
            int mins = (int) screen[0];
            addInsight(String.format("Logged %dh %dm of screen time this week.", mins / 60, mins % 60), "neutral");
        }

        // Sleep quality
        double[] quality = DatabaseManager.getSleepQualityWeekComparison();
        if (quality[0] > 0) {
            String qualMsg = String.format("Average sleep quality this week: %.1f / 5.", quality[0]);
            addInsight(qualMsg, quality[0] >= 4 ? "good" : quality[0] < 3 ? "warn" : "neutral");
        }

        // Pomodoro sessions
        double[] pomo = DatabaseManager.getPomodoroWeekComparison();
        if (pomo[0] > 0 && pomo[1] > 0) {
            int diff = (int) (pomo[0] - pomo[1]);
            if (diff > 0)
                addInsight(String.format("%.0f focus sessions this week, up %d from last week. Great work!", pomo[0], diff), "good");
            else if (diff < 0)
                addInsight(String.format("%.0f focus sessions this week, down %d from last week.", pomo[0], Math.abs(diff)), "warn");
            else
                addInsight(String.format("%.0f focus sessions this week, same as last week.", pomo[0]), "neutral");
        } else if (pomo[0] > 0) {
            addInsight(String.format("You completed %.0f focus sessions this week.", pomo[0]), "neutral");
        }

        if (insightsContainer.getChildren().isEmpty()) {
            addInsight("Log some data to start seeing your weekly insights.", "neutral");
        }
    }

    private void addInsight(String text, String type) {
        Label label = new Label("• " + text);
        label.setWrapText(true);
        if ("good".equals(type))    label.getStyleClass().add("good");
        else if ("warn".equals(type)) label.getStyleClass().add("warn");
        insightsContainer.getChildren().add(label);
    }
}
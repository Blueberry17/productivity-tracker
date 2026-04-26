package com.tracker.controllers;

import com.tracker.database.DatabaseManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PomodoroController {

    @FXML private Label timerLabel;
    @FXML private Label modeLabel;
    @FXML private Label sessionLabel;
    @FXML private Button startPauseBtn;
    @FXML private HBox dotsContainer;
    @FXML private TextField workInput;
    @FXML private TextField breakInput;
    @FXML private BarChart<String, Number> weekChart;

    private Timeline timeline;
    private int currentSecondsLeft;
    private int liveCyclesCompleted = 0;
    private int totalCyclesToday = 0;
    private boolean isPaused = true;

    private int workTime = 25;
    private int breakTime = 5;
    private int longBreakTime = 15;

    @FXML
    public void initialize() {
        totalCyclesToday = loadTodayCount();
        liveCyclesCompleted = totalCyclesToday % 4;
        currentSecondsLeft = workTime * 60;
        updateTimerLabel();
        modeLabel.setText("Focus session");
        updateSessionLabel();
        buildDots();
        loadWeekChart();
    }

    private int loadTodayCount() {
        String sql = "SELECT COUNT(*) FROM Pomodoro " +
                     "WHERE entryDate = date('now','localtime') AND isCompleted = 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @FXML
    public void onStartPause() {
        if (isPaused) {
            isPaused = false;
            startPauseBtn.setText("Pause");
            if (timeline == null) {
                setupTimeline();
            }
            timeline.play();
        } else {
            isPaused = true;
            startPauseBtn.setText("Resume");
            timeline.pause();
        }
    }

    @FXML
    public void onReset() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        isPaused = true;
        liveCyclesCompleted = 0;
        currentSecondsLeft = workTime * 60;
        startPauseBtn.setText("Start");
        modeLabel.setText("Focus session");
        updateTimerLabel();
        updateSessionLabel();
        updateDots();
    }

    @FXML
    public void onChangeTime() {
        try {
            int newWork = Integer.parseInt(workInput.getText().trim());
            int newBreak = Integer.parseInt(breakInput.getText().trim());
            if (newWork <= 0 || newBreak <= 0) throw new NumberFormatException();
            workTime = newWork;
            breakTime = newBreak;
            if (timeline != null) {
                timeline.stop();
                timeline = null;
            }
            isPaused = true;
            currentSecondsLeft = workTime * 60;
            modeLabel.setText("Focus session");
            startPauseBtn.setText("Start");
            updateTimerLabel();
        } catch (NumberFormatException e) {
            workInput.setText("");
            breakInput.setText("");
        }
    }

    private void setupTimeline() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (currentSecondsLeft > 0) {
                currentSecondsLeft--;
                updateTimerLabel();
            } else {
                handlePhaseTransition();
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    private void handlePhaseTransition() {
        if (modeLabel.getText().equals("Focus session")) {
            completeCycle();
            if (liveCyclesCompleted >= 4) {
                currentSecondsLeft = longBreakTime * 60;
                modeLabel.setText("Long Break");
            } else {
                currentSecondsLeft = breakTime * 60;
                modeLabel.setText("Short Break");
            }
        } else {
            if (liveCyclesCompleted >= 4) {
                liveCyclesCompleted = 0;
                updateDots();
            }
            currentSecondsLeft = workTime * 60;
            modeLabel.setText("Focus session");
        }
        updateTimerLabel();
    }

    private void completeCycle() {
        liveCyclesCompleted++;
        totalCyclesToday++;
        updateSessionLabel();
        updateDots();
        saveCompletedSession();
    }

    private void loadWeekChart() {
        int[] data = DatabaseManager.getPomodoroLast7Days();
        weekChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE");
        for (int i = 0; i < 7; i++) {
            String label = today.minusDays(6 - i).format(fmt);
            series.getData().add(new XYChart.Data<>(label, data[i]));
        }
        weekChart.setAnimated(false);
        weekChart.setLegendVisible(false);
        weekChart.getData().add(series);
    }

    private void saveCompletedSession() {
        String sql = "INSERT INTO Pomodoro (entryDate, isCompleted, durationRemaining) " +
                     "VALUES (date('now','localtime'), 1, '00:00')";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        loadWeekChart();
    }

    private void buildDots() {
        dotsContainer.getChildren().clear();
        for (int i = 0; i < 4; i++) {
            Circle dot = new Circle(8);
            dot.getStyleClass().add(i < liveCyclesCompleted ? "dot-done" : "dot-empty");
            dotsContainer.getChildren().add(dot);
        }
    }

    private void updateDots() {
        dotsContainer.getChildren().forEach(node -> {
            int i = dotsContainer.getChildren().indexOf(node);
            node.getStyleClass().setAll(i < liveCyclesCompleted ? "dot-done" : "dot-empty");
        });
    }

    private void updateTimerLabel() {
        timerLabel.setText(String.format("%02d:%02d", currentSecondsLeft / 60, currentSecondsLeft % 60));
    }

    private void updateSessionLabel() {
        sessionLabel.setText("Session " + (liveCyclesCompleted + 1) + "  \u00b7  " + totalCyclesToday + " completed today");
    }
}
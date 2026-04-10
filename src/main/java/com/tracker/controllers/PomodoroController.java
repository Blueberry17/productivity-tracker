package com.tracker.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;

public class PomodoroController {

    @FXML private Label timerLabel;
    @FXML private Label modeLabel;
    @FXML private Label sessionLabel;
    @FXML private Button startPauseBtn;
    @FXML private HBox dotsContainer;

    @FXML
    public void initialize() {
        timerLabel.setText("25:00");
        modeLabel.setText("Focus session");
        sessionLabel.setText("Session 1  ·  0 completed today");

        for (int i = 0; i < 4; i++) {
            Circle dot = new Circle(6);
            dot.getStyleClass().add("dot-empty");
            dotsContainer.getChildren().add(dot);
        }
    }

    @FXML
    public void onStartPause() {}

    @FXML
    public void onReset() {}
}
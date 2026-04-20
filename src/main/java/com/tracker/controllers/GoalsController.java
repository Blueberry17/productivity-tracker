package com.tracker.controllers;

import java.util.ArrayList;
import java.util.List;

import com.tracker.Goals;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class GoalsController {

    @FXML private VBox goalsContainer;
    @FXML private TextField goalNameField;
    @FXML private TextField goalTargetField;
    @FXML private TextField goalUnitField;
    @FXML private Label statusLabel;

    private static final List<Goals> goalsList = new ArrayList<>();

    @FXML
    public void initialize() {
        if (goalsList.isEmpty()) {
            goalsList.add(new Goals("Sleep", 8.0, "hours/night"));
            goalsList.add(new Goals("Daily steps", 10000.0, "steps"));
            goalsList.add(new Goals("Screen time limit", 3.0, "hours/day"));
            goalsList.add(new Goals("Study sessions / week", 5.0, "sessions"));
            goalsList.add(new Goals("Exercise", 4.0, "days/week"));

            goalsList.get(0).setCurrent(7.2);
            goalsList.get(1).setCurrent(6800.0);
            goalsList.get(2).setCurrent(4.3);
            goalsList.get(3).setCurrent(3.0);
            goalsList.get(4).setCurrent(3.0);
        }

        goalsContainer.getChildren().clear();
        for (Goals goal : goalsList) {
            addGoalCard(goal);
        }
    }

    @FXML
    public void onAddGoal() {
        String name = goalNameField.getText().trim();
        String targetText = goalTargetField.getText().trim();
        String unit = goalUnitField.getText().trim();

        if (name.isEmpty() || targetText.isEmpty() || unit.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            return;
        }

        double target;
        try {
            target = Double.parseDouble(targetText);
        } catch (NumberFormatException e) {
            statusLabel.setText("Target must be a number.");
            return;
        }

        Goals newGoal = new Goals(name, target, unit);
        goalsList.add(newGoal);
        addGoalCard(newGoal);

        goalNameField.clear();
        goalTargetField.clear();
        goalUnitField.clear();
        statusLabel.setText("Goal added!");
    }

    private void addGoalCard(Goals goal) {
        Label nameLabel = new Label(goal.getName());
        nameLabel.getStyleClass().add("goal-name");

        Label valueLabel = new Label(String.format("%.1f / %.1f %s",
                goal.getCurrent(), goal.getTarget(), goal.getUnit()));
        valueLabel.getStyleClass().add("goal-value");

        ProgressBar bar = new ProgressBar(goal.getProgress());
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.getStyleClass().add(goal.isMet() ? "progress-met" : "progress-bar");

        Label pctLabel = new Label(goal.isMet() ? "Goal met ✓" :
                String.format("%.0f%%", goal.getProgress() * 100));
        pctLabel.getStyleClass().add(goal.isMet() ? "goal-met" : "goal-pct");

        HBox header = new HBox(nameLabel, valueLabel);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        header.setSpacing(8);

        HBox footer = new HBox(bar, pctLabel);
        HBox.setHgrow(bar, Priority.ALWAYS);
        footer.setSpacing(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        TextField progressField = new TextField();
        progressField.setPromptText("Log progress...");
        progressField.setPrefWidth(120);
        progressField.getStyleClass().add("input");

        Button logBtn = new Button("Update");
        logBtn.getStyleClass().add("btn-primary");
        logBtn.setOnAction(e -> {
            try {
                double value = Double.parseDouble(progressField.getText().trim());
                goal.setCurrent(value);
                bar.setProgress(goal.getProgress());
                bar.getStyleClass().setAll(goal.isMet() ? "progress-met" : "progress-bar");
                pctLabel.setText(goal.isMet() ? "Goal met ✓" :
                        String.format("%.0f%%", goal.getProgress() * 100));
                pctLabel.getStyleClass().setAll(goal.isMet() ? "goal-met" : "goal-pct");
                valueLabel.setText(String.format("%.1f / %.1f %s",
                        goal.getCurrent(), goal.getTarget(), goal.getUnit()));
                progressField.clear();
            } catch (NumberFormatException ex) {
                progressField.setText("Numbers only");
            }
        });

        HBox logRow = new HBox(progressField, logBtn);
        logRow.setSpacing(8);
        logRow.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(6, header, footer, logRow);
        card.getStyleClass().add("goal-card");
        goalsContainer.getChildren().add(card);
    }
}
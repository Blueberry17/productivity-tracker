package com.tracker.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
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

    private static final Object[][] GOALS = {
        { "Sleep",                 7.2,    8.0,   "hours/night" },
        { "Daily steps",           6800.0, 10000.0, "steps"     },
        { "Screen time limit",     4.3,    3.0,   "hours/day"   },
        { "Study sessions / week", 3.0,    5.0,   "sessions"    },
        { "Exercise",              3.0,    4.0,   "days/week"   },
    };

    @FXML
    public void initialize() {
        for (Object[] g : GOALS) {
            String name    = (String) g[0];
            double current = (double) g[1];
            double target  = (double) g[2];
            String unit    = (String) g[3];
            double progress = Math.min(current / target, 1.0);
            boolean met     = current >= target;

            Label nameLabel = new Label(name);
            nameLabel.getStyleClass().add("goal-name");

            Label valueLabel = new Label(String.format("%.1f / %.1f %s", current, target, unit));
            valueLabel.getStyleClass().add("goal-value");

            ProgressBar bar = new ProgressBar(progress);
            bar.setMaxWidth(Double.MAX_VALUE);
            bar.getStyleClass().add(met ? "progress-met" : "progress-bar");

            Label pctLabel = new Label(met ? "Goal met ✓" : String.format("%.0f%%", progress * 100));
            pctLabel.getStyleClass().add(met ? "goal-met" : "goal-pct");

            HBox header = new HBox(nameLabel, valueLabel);
            HBox.setHgrow(nameLabel, Priority.ALWAYS);
            header.setSpacing(8);

            HBox footer = new HBox(bar, pctLabel);
            HBox.setHgrow(bar, Priority.ALWAYS);
            footer.setSpacing(10);
            footer.setAlignment(Pos.CENTER_LEFT);

            VBox card = new VBox(6, header, footer);
            card.getStyleClass().add("goal-card");
            goalsContainer.getChildren().add(card);
        }
    }

    @FXML
    public void onAddGoal() {}
}
package com.tracker.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class HabitsController {

    @FXML private VBox habitListContainer;
    @FXML private TextField newHabitField;
    @FXML private Label statusLabel;

    private static final Object[][] HABITS = {
        { "Morning run (30 min)",  true,  7 },
        { "Read (20 min)",         true,  3 },
        { "Drink 2L water",        true,  5 },
        { "No screens after 10pm", false, 0 },
        { "Study session (2 hrs)", false, 0 },
    };

    @FXML
    public void initialize() {
        for (Object[] h : HABITS) {
            String name   = (String)  h[0];
            boolean done  = (boolean) h[1];
            int streak    = (int)     h[2];

            CheckBox check = new CheckBox(name);
            check.setSelected(done);
            check.getStyleClass().add("habit-check");
            HBox.setHgrow(check, Priority.ALWAYS);
            check.setMaxWidth(Double.MAX_VALUE);

            HBox row = new HBox(12);
            row.getStyleClass().add("habit-row");
            row.setPadding(new Insets(8, 4, 8, 4));
            row.getChildren().add(check);

            if (streak > 0) {
                Label badge = new Label("🔥 " + streak + " day streak");
                badge.getStyleClass().add("streak-badge");
                row.getChildren().add(badge);
            }

            habitListContainer.getChildren().addAll(row, new Separator());
        }
    }

    @FXML
    public void onAddHabit() {}
}
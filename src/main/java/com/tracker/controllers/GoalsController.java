package com.tracker.controllers;

import com.tracker.Goals;
import com.tracker.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GoalsController {

    @FXML private VBox goalsContainer;
    @FXML private TextField goalNameField;
    @FXML private TextField goalTargetField;
    @FXML private TextField goalUnitField;
    @FXML private Label statusLabel;

    @FXML private TextField sleepTargetField;
    @FXML private TextField screenTargetField;
    @FXML private Label targetsStatusLabel;

    @FXML
    public void initialize() {
        seedDefaultGoalsIfEmpty();
        loadGoals();
        loadDailyTargets();
    }

    private void loadDailyTargets() {
        double sleep  = DatabaseManager.getDailyTarget("sleep_hours", 8.0);
        double screen = DatabaseManager.getDailyTarget("screen_time_mins", 240);
        sleepTargetField.setText(String.valueOf((int) sleep));
        screenTargetField.setText(String.valueOf((int) screen));
    }

    @FXML
    public void onSaveDailyTargets() {
        try {
            double sleep  = Double.parseDouble(sleepTargetField.getText().trim());
            double screen = Double.parseDouble(screenTargetField.getText().trim());
            if (sleep <= 0 || screen <= 0) throw new NumberFormatException();
            DatabaseManager.setDailyTarget("sleep_hours", sleep);
            DatabaseManager.setDailyTarget("screen_time_mins", screen);
            targetsStatusLabel.setText("Saved!");
        } catch (NumberFormatException e) {
            targetsStatusLabel.setText("Enter valid positive numbers.");
        }
    }

    private void seedDefaultGoalsIfEmpty() {
        String countSql = "SELECT COUNT(*) FROM Goals";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(countSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = "INSERT INTO Goals (goalName, target, unit, current) VALUES (?, ?, ?, 0)";
                String[][] defaults = {
                    {"Read 20 books",        "20",   "books"},
                    {"Run 100 km",           "100",  "km"},
                    {"Save £1,000",          "1000", "£"},
                    {"Learn 50 new words",   "50",   "words"},
                    {"Complete 30 workouts", "30",   "workouts"}
                };
                try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                    for (String[] g : defaults) {
                        insert.setString(1, g[0]);
                        insert.setDouble(2, Double.parseDouble(g[1]));
                        insert.setString(3, g[2]);
                        insert.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadGoals() {
        goalsContainer.getChildren().clear();
        String sql = "SELECT goalID, goalName, target, unit, current FROM Goals ORDER BY goalID ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Goals goal = new Goals(
                    rs.getInt(1),
                    rs.getString(2),
                    rs.getDouble(3),
                    rs.getString(4),
                    rs.getDouble(5)
                );
                addGoalCard(goal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onAddGoal() {
        String name       = goalNameField.getText().trim();
        String targetText = goalTargetField.getText().trim();
        String unit       = goalUnitField.getText().trim();

        if (name.isEmpty() || targetText.isEmpty() || unit.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            return;
        }

        double target;
        try {
            target = Double.parseDouble(targetText);
            if (target <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            statusLabel.setText("Target must be a positive number.");
            return;
        }

        String sql = "INSERT INTO Goals (goalName, target, unit, current) VALUES (?, ?, ?, 0)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setDouble(2, target);
            ps.setString(3, unit);
            ps.executeUpdate();

            goalNameField.clear();
            goalTargetField.clear();
            goalUnitField.clear();
            statusLabel.setText("Goal added!");
            loadGoals();
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error saving goal.");
        }
    }

    private void addGoalCard(Goals goal) {
        Label nameLabel = new Label(goal.getName());
        nameLabel.getStyleClass().add("goal-name");

        Label valueLabel = new Label(formatValue(goal.getCurrent()) + " / " +
                formatValue(goal.getTarget()) + " " + goal.getUnit());
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

        // Progress update row
        TextField progressField = new TextField();
        progressField.setPromptText("Add progress...");
        progressField.setPrefWidth(120);
        progressField.getStyleClass().add("input");

        Button logBtn = new Button("Update");
        logBtn.getStyleClass().add("btn-primary");
        logBtn.setOnAction(e -> {
            try {
                double added = Double.parseDouble(progressField.getText().trim());
                double newCurrent = Math.min(goal.getCurrent() + added, goal.getTarget());

                String sql = "UPDATE Goals SET current = ? WHERE goalID = ?";
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setDouble(1, newCurrent);
                    ps.setInt(2, goal.getId());
                    ps.executeUpdate();
                }

                goal.setCurrent(newCurrent);
                bar.setProgress(goal.getProgress());
                bar.getStyleClass().setAll(goal.isMet() ? "progress-met" : "progress-bar");
                pctLabel.setText(goal.isMet() ? "Goal met ✓" :
                        String.format("%.0f%%", goal.getProgress() * 100));
                pctLabel.getStyleClass().setAll(goal.isMet() ? "goal-met" : "goal-pct");
                valueLabel.setText(formatValue(goal.getCurrent()) + " / " +
                        formatValue(goal.getTarget()) + " " + goal.getUnit());
                progressField.clear();
            } catch (NumberFormatException ex) {
                progressField.setText("Numbers only");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        HBox logRow = new HBox(8, progressField, logBtn);
        logRow.setAlignment(Pos.CENTER_LEFT);

        // Edit row (hidden by default)
        TextField editName   = new TextField(goal.getName());
        TextField editTarget = new TextField(formatValue(goal.getTarget()));
        TextField editUnit   = new TextField(goal.getUnit());
        editName.setPrefWidth(160);
        editTarget.setPrefWidth(80);
        editUnit.setPrefWidth(80);
        editName.getStyleClass().add("input");
        editTarget.getStyleClass().add("input");
        editUnit.getStyleClass().add("input");

        Button saveEditBtn = new Button("Save");
        saveEditBtn.getStyleClass().add("btn-primary");
        Button cancelBtn = new Button("Cancel");

        HBox editRow = new HBox(8, editName, editTarget, editUnit, saveEditBtn, cancelBtn);
        editRow.setAlignment(Pos.CENTER_LEFT);
        editRow.setVisible(false);
        editRow.setManaged(false);

        // Edit and Delete buttons
        Button editBtn   = new Button("Edit");
        Button deleteBtn = new Button("Delete");
        editBtn.getStyleClass().add("btn-primary");
        deleteBtn.getStyleClass().add("btn-primary");

        editBtn.setOnAction(e -> {
            editRow.setVisible(true);
            editRow.setManaged(true);
            editBtn.setVisible(false);
            editBtn.setManaged(false);
        });

        cancelBtn.setOnAction(e -> {
            editRow.setVisible(false);
            editRow.setManaged(false);
            editBtn.setVisible(true);
            editBtn.setManaged(true);
        });

        saveEditBtn.setOnAction(e -> {
            String newName = editName.getText().trim();
            String newUnit = editUnit.getText().trim();
            try {
                double newTarget = Double.parseDouble(editTarget.getText().trim());
                if (newName.isEmpty() || newUnit.isEmpty() || newTarget <= 0)
                    throw new NumberFormatException();

                String sql = "UPDATE Goals SET goalName = ?, target = ?, unit = ? WHERE goalID = ?";
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, newName);
                    ps.setDouble(2, newTarget);
                    ps.setString(3, newUnit);
                    ps.setInt(4, goal.getId());
                    ps.executeUpdate();
                }
                loadGoals();
            } catch (NumberFormatException ex) {
                editTarget.setText("Invalid");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        deleteBtn.setOnAction(e -> {
            String sql = "DELETE FROM Goals WHERE goalID = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, goal.getId());
                ps.executeUpdate();
                loadGoals();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        HBox actionRow = new HBox(8, editBtn, deleteBtn);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(6, header, footer, logRow, actionRow, editRow);
        card.getStyleClass().add("goal-card");
        goalsContainer.getChildren().add(card);
    }

    private String formatValue(double value) {
        return value == Math.floor(value) ? String.valueOf((int) value) : String.format("%.1f", value);
    }
}
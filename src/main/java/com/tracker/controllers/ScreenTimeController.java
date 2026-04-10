package com.tracker.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class ScreenTimeController {

    @FXML private PieChart pieChart;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField minutesField;
    @FXML private Label totalLabel;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        datePicker.setValue(LocalDate.now());

        categoryCombo.getItems().addAll(
            "Study / Work", "Social Media", "Video / Streaming",
            "Gaming", "Communication", "Other"
        );
        categoryCombo.getSelectionModel().selectFirst();

        totalLabel.setText("Today's total: 4h 18m");

        pieChart.setAnimated(false);
        pieChart.getData().addAll(
            new PieChart.Data("Study / Work",     90),
            new PieChart.Data("Social Media",     57),
            new PieChart.Data("Video / Streaming", 46),
            new PieChart.Data("Gaming",            30),
            new PieChart.Data("Other",             35)
        );
    }

    @FXML
    public void onLog() {}
}
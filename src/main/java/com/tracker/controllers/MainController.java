package com.tracker.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.List;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Button btnDashboard;
    @FXML private Button btnSleep;
    @FXML private Button btnScreenTime;
    @FXML private Button btnHabits;
    @FXML private Button btnPomodoro;
    @FXML private Button btnCalendar;
    @FXML private Button btnGoals;

    private List<Button> navButtons;

    @FXML
    public void initialize() {
        navButtons = List.of(btnDashboard, btnSleep, btnScreenTime,
                             btnHabits, btnPomodoro, btnCalendar, btnGoals);
        showDashboard();
    }

    @FXML public void showDashboard()  { loadView("DashboardView.fxml",    btnDashboard);  }
    @FXML public void showSleep()      { loadView("SleepView.fxml",        btnSleep);      }
    @FXML public void showScreenTime() { loadView("ScreenTimeView.fxml",   btnScreenTime); }
    @FXML public void showHabits()     { loadView("HabitsView.fxml",       btnHabits);     }
    @FXML public void showPomodoro()   { loadView("PomodoroView.fxml",     btnPomodoro);   }
    @FXML public void showCalendar()   { loadView("CalendarView.fxml",     btnCalendar);   }
    @FXML public void showGoals()      { loadView("GoalsView.fxml",        btnGoals);      }

    private void loadView(String fxmlFile, Button activeBtn) {
        navButtons.forEach(b -> b.getStyleClass().remove("nav-active"));
        activeBtn.getStyleClass().add("nav-active");
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/tracker/views/" + fxmlFile)
            );
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Could not load: " + fxmlFile);
            e.printStackTrace();
        }
    }
}

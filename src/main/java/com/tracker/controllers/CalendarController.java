package com.tracker.controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.tracker.database.DatabaseManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

public class CalendarController {

    @FXML private StackPane calendarContainer;
    
    private CalendarView calendarView; 
    
    // Fix 1: Added <String> parameter here
    private Calendar<String> personalCalendar;

    @FXML
    public void initialize() {
        // Fix 2: Added <String> to the Calendar declarations and the generic <> to the constructors
        Calendar<String> health   = new Calendar<>("Health & Fitness");
        Calendar<String> study    = new Calendar<>("Study");
        personalCalendar = new Calendar<>("Personal");

        health.setStyle(Calendar.Style.STYLE1);
        study.setStyle(Calendar.Style.STYLE2);
        personalCalendar.setStyle(Calendar.Style.STYLE3);

        // Fetch data directly from the DatabaseManager
        loadDatabaseEntries(health, study, personalCalendar);

        CalendarSource source = new CalendarSource("Tracker");
        source.getCalendars().addAll(health, study, personalCalendar);

        calendarView = new CalendarView();
        calendarView.getCalendarSources().setAll(source);
        
        // Sets today to current date so it opens on the right month
        calendarView.setToday(LocalDate.now());
        calendarView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        calendarView.showMonthPage();

        calendarContainer.getChildren().add(calendarView);
    }
    
    // Fix 3: Added <String> parameters to the method signature
    private void loadDatabaseEntries(Calendar<String> health, Calendar<String> study, Calendar<String> personal) {
        // We use try-with-resources to ensure the connection closes automatically
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Fetch Sleep Data -> Health Calendar
            ResultSet sleepRs = stmt.executeQuery("SELECT entryDate, duration_hrs FROM Sleep");
            while (sleepRs.next()) {
                String dateStr = sleepRs.getString("entryDate");
                double duration = sleepRs.getDouble("duration_hrs");
                
                Entry<String> entry = createFullDayEntry("Sleep: " + duration + "h", dateStr);
                if (entry != null) health.addEntry(entry);
            }
            sleepRs.close();

            // 2. Fetch Pomodoro Data -> Study Calendar
            ResultSet pomRs = stmt.executeQuery("SELECT entryDate, isCompleted FROM Pomodoro WHERE isCompleted = 1");
            while (pomRs.next()) {
                String dateStr = pomRs.getString("entryDate");
                Entry<String> entry = createFullDayEntry("Pomodoro Completed", dateStr);
                if (entry != null) study.addEntry(entry);
            }
            pomRs.close();

            // 3. Fetch Screen Time Data -> Personal Calendar
            ResultSet screenRs = stmt.executeQuery("SELECT entryDate, category, duration_mins FROM ScreenTime");
            while (screenRs.next()) {
                String dateStr = screenRs.getString("entryDate");
                String category = screenRs.getString("category");
                int duration = screenRs.getInt("duration_mins");
                
                Entry<String> entry = createFullDayEntry("Screen (" + category + "): " + duration + "m", dateStr);
                if (entry != null) personal.addEntry(entry);
            }
            screenRs.close();

        } catch (SQLException e) {
            System.err.println("Database error loading calendar entries: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper method to parse the DB text date and create a CalendarFX Full-Day entry.
     * Assumes entryDate in DB is saved in "YYYY-MM-DD" format.
     */
    private Entry<String> createFullDayEntry(String title, String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString);
            Entry<String> entry = new Entry<>(title);
            entry.changeStartDate(date);
            entry.changeEndDate(date);
            entry.setFullDay(true);
            return entry;
        } catch (DateTimeParseException | NullPointerException e) {
            System.err.println("Could not parse date from DB: " + dateString);
            return null;
        }
    }
    
    @FXML
    private void handleAddTask(ActionEvent event) {
        calendarView.createEntryAt(ZonedDateTime.now(), personalCalendar);
    }
}
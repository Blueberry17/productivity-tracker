package com.tracker.controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.time.LocalTime;

public class CalendarController {

    @FXML private StackPane calendarContainer;

    @FXML
    public void initialize() {
        Calendar health  = new Calendar("Health & Fitness");
        Calendar study   = new Calendar("Study");
        Calendar personal = new Calendar("Personal");

        health.setStyle(Calendar.Style.STYLE1);
        study.setStyle(Calendar.Style.STYLE2);
        personal.setStyle(Calendar.Style.STYLE3);

        addEntries(health, study, personal);

        CalendarSource source = new CalendarSource("Tracker");
        source.getCalendars().addAll(health, study, personal);

        CalendarView calendarView = new CalendarView();
        calendarView.getCalendarSources().setAll(source);
        calendarView.setToday(LocalDate.of(2026, 4, 9));
        calendarView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        calendarView.showMonthPage();

        calendarContainer.getChildren().add(calendarView);
    }

    private void addEntries(Calendar health, Calendar study, Calendar personal) {
        LocalDate base = LocalDate.of(2026, 4, 1);

        // Health & Fitness entries
        health.addEntry(entry("Morning Run",    base.plusDays(0),  time(7, 0),  time(7, 30)));
        health.addEntry(entry("Gym Session",    base.plusDays(1),  time(8, 0),  time(9, 0)));
        health.addEntry(entry("Morning Run",    base.plusDays(2),  time(7, 0),  time(7, 30)));
        health.addEntry(entry("Yoga",           base.plusDays(3),  time(7, 30), time(8, 15)));
        health.addEntry(entry("Morning Run",    base.plusDays(6),  time(7, 0),  time(7, 30)));
        health.addEntry(entry("Gym Session",    base.plusDays(7),  time(8, 0),  time(9, 0)));
        health.addEntry(entry("Morning Run",    base.plusDays(8),  time(7, 0),  time(7, 30)));
        health.addEntry(entry("Gym Session",    base.plusDays(10), time(8, 0),  time(9, 0)));
        health.addEntry(entry("Morning Run",    base.plusDays(13), time(7, 0),  time(7, 30)));
        health.addEntry(entry("Gym Session",    base.plusDays(14), time(8, 0),  time(9, 0)));

        // Study entries
        study.addEntry(entry("Study Session",   base.plusDays(0),  time(14, 0), time(16, 0)));
        study.addEntry(entry("Study Session",   base.plusDays(2),  time(15, 0), time(17, 0)));
        study.addEntry(entry("Study Group",     base.plusDays(3),  time(13, 0), time(15, 0)));
        study.addEntry(entry("Revision Block",  base.plusDays(5),  time(10, 0), time(12, 0)));
        study.addEntry(entry("Assignment Due",  base.plusDays(6),  time(23, 59), time(23, 59)));
        study.addEntry(entry("Study Session",   base.plusDays(8),  time(14, 0), time(16, 0)));
        study.addEntry(entry("Study Group",     base.plusDays(10), time(13, 0), time(15, 0)));
        study.addEntry(entry("Exam Prep",       base.plusDays(12), time(9, 0),  time(12, 0)));
        study.addEntry(entry("Exam Prep",       base.plusDays(13), time(9, 0),  time(12, 0)));

        // Personal entries
        personal.addEntry(entry("Team Meeting",      base.plusDays(1),  time(10, 0), time(11, 0)));
        personal.addEntry(entry("Doctor Appointment",base.plusDays(2),  time(11, 0), time(11, 30)));
        personal.addEntry(entry("Team Meeting",      base.plusDays(8),  time(10, 0), time(11, 0)));
        personal.addEntry(entry("Dentist",           base.plusDays(9),  time(9, 0),  time(9, 30)));
        personal.addEntry(entry("Team Meeting",      base.plusDays(15), time(10, 0), time(11, 0)));
        personal.addEntry(entry("Birthday Dinner",   base.plusDays(11), time(19, 0), time(21, 0)));
    }

    private Entry<String> entry(String title, LocalDate date, LocalTime start, LocalTime end) {
        Entry<String> e = new Entry<>(title);
        e.setInterval(date.atTime(start), date.atTime(end));
        return e;
    }

    private LocalTime time(int hour, int minute) {
        return LocalTime.of(hour, minute);
    }
}
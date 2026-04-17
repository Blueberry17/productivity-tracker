package com.tracker;

public class Goals {
    private final String name;
    private double current;
    private final double target;
    private final String unit;

    public Goals(String name, double target, String unit) {
        this.name = name;
        this.target = target;
        this.unit = unit;
        this.current = 0.0;
    }

    public boolean isMet() {
        return current >= target;
    }

    public double getProgress() {
        return Math.min(current / target, 1.0);
    }

    // Getters and setters
    public String getName() { return name; }
    public double getCurrent() { return current; }
    public double getTarget() { return target; }
    public String getUnit() { return unit; }
    public void setCurrent(double current) { this.current = current; }

}
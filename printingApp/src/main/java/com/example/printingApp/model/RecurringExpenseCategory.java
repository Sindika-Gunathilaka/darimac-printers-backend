package com.example.printingApp.model;

public enum RecurringExpenseCategory {
    SALARY("Salary & Wages"),
    RENT("Rent"),
    UTILITIES("Utilities"),
    INSURANCE("Insurance"),
    MAINTENANCE("Equipment Maintenance"),
    SUPPLIES("Office Supplies"),
    MARKETING("Marketing & Advertising"),
    TRANSPORT("Transport & Fuel"),
    PROFESSIONAL_SERVICES("Professional Services"),
    TELECOMMUNICATIONS("Telecommunications"),
    OTHER("Other Fixed Costs");

    private final String displayName;

    RecurringExpenseCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
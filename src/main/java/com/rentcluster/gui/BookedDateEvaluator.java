package com.rentcluster.gui;

import com.toedter.calendar.IDateEvaluator;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BookedDateEvaluator implements IDateEvaluator {
    private List<String> bookedDates;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public BookedDateEvaluator(List<String> bookedDates) {
        this.bookedDates = bookedDates;
    }

    public void setBookedDates(List<String> bookedDates) {
        this.bookedDates = bookedDates;
    }

    @Override
    public boolean isSpecial(Date date) {
        return false;
    }

    @Override
    public Color getSpecialForegroundColor() { return null; }

    @Override
    public Color getSpecialBackroundColor() { return null; }

    @Override
    public String getSpecialTooltip() { return null; }

    // This method determines if a date should be disabled (unclickable)
    @Override
    public boolean isInvalid(Date date) {
        if (bookedDates == null) return false;
        String dateString = sdf.format(date);
        return bookedDates.contains(dateString); // Invalid if it is in the booked list
    }

    @Override
    public Color getInvalidForegroundColor() {
        return Color.WHITE; // White text for disabled dates
    }

    @Override
    public Color getInvalidBackroundColor() {
        return new Color(220, 53, 69); // Red background for booked dates
    }

    @Override
    public String getInvalidTooltip() {
        return "This date is already booked!";
    }
}
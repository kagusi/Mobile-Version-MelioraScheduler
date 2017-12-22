package edu.rochester.meliorascheduler.Models;

/**
 * Created by Kennedy Agusi on 11/11/2017.
 */

public class Time {
    String time;
    boolean isBooked;
    boolean isSelected;
    //Position in Adapter
    int position;

    public Time(String time, boolean isBooked, boolean isSelected) {
        this.time = time;
        this.isBooked = isBooked;
        this.isSelected = isSelected;
    }

    public String getTime() {
        return time;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setBooked(boolean booked) {
        isBooked = booked;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}

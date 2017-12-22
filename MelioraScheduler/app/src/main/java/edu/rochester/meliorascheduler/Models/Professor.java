package edu.rochester.meliorascheduler.Models;

import org.json.JSONArray;

import java.io.Serializable;

/**
 * Created by Kennedy Agusi on 11/7/2017.
 */

public class Professor implements Serializable {
    int id;
    String name;
    String email;
    String department;
    String officeLocation;
    String officeHrs;
    String[] officeDays;

    public void make(int id, String name, String email, String department, String officeLocation, String officeHrs) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.officeLocation = officeLocation;
        this.officeHrs = officeHrs;
    }

    public String[] getOfficeDays() {
        return officeDays;
    }

    public void setOfficeDays(String[] officeDays) {
        this.officeDays = officeDays;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getOfficeLocation() {
        return officeLocation;
    }

    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }

    public String getOfficeHrs() {
        return officeHrs;
    }

    public void setOfficeHrs(String officeHrs) {
        this.officeHrs = officeHrs;
    }
}

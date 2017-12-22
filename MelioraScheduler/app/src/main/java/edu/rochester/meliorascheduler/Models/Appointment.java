package edu.rochester.meliorascheduler.Models;

import java.io.Serializable;

/**
 * Created by Kennedy Agusi on 11/7/2017.
 */

public class Appointment implements Serializable {
    int profID;
    int appointmentID;
    String profName;
    String profEmail;
    String profOfficeLoc;
    String stdName;
    String stdEmail;
    String stdID;
    String appointmentTime;
    String appointmentDate;
    String reasoForAppointment;
    boolean isCompleted;
    boolean isCancelled;
    String reasonForCancel;
    String cancelledBy;

    public void make(int profID, String profName, String profEmail, String stdName, String stdEmail,
                     String stdID, String appointmentTime, String appointmentDate, String reasoForAppointment,
                     boolean isCompleted, boolean isCancelled, String reasonForCancel, String cancelledBy) {
        this.profID = profID;
        this.profName = profName;
        this.profEmail = profEmail;
        this.stdName = stdName;
        this.stdEmail = stdEmail;
        this.stdID = stdID;
        this.appointmentTime = appointmentTime;
        this.appointmentDate = appointmentDate;
        this.reasoForAppointment = reasoForAppointment;
        this.isCompleted = isCompleted;
        this.isCancelled = isCancelled;
        this.reasonForCancel = reasonForCancel;
        this.cancelledBy = cancelledBy;
    }

    public int getAppointmentID() {
        return appointmentID;
    }

    public void setAppointmentID(int appointmentID) {
        this.appointmentID = appointmentID;
    }

    public String getProfOfficeLoc() {
        return profOfficeLoc;
    }

    public void setProfOfficeLoc(String profOfficeLoc) {
        this.profOfficeLoc = profOfficeLoc;
    }

    public int getProfID() {
        return profID;
    }

    public void setProfID(int profID) {
        this.profID = profID;
    }

    public String getProfName() {
        return profName;
    }

    public void setProfName(String profName) {
        this.profName = profName;
    }

    public String getProfEmail() {
        return profEmail;
    }

    public void setProfEmail(String profEmail) {
        this.profEmail = profEmail;
    }

    public String getStdName() {
        return stdName;
    }

    public void setStdName(String stdName) {
        this.stdName = stdName;
    }

    public String getStdEmail() {
        return stdEmail;
    }

    public void setStdEmail(String stdEmail) {
        this.stdEmail = stdEmail;
    }

    public String getStdID() {
        return stdID;
    }

    public void setStdID(String stdID) {
        this.stdID = stdID;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getReasoForAppointment() {
        return reasoForAppointment;
    }

    public void setReasoForAppointment(String reasoForAppointment) {
        this.reasoForAppointment = reasoForAppointment;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    public String getReasonForCancel() {
        return reasonForCancel;
    }

    public void setReasonForCancel(String reasonForCancel) {
        this.reasonForCancel = reasonForCancel;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }
}

package edu.rochester.meliorascheduler.Models;

import java.io.Serializable;

/**
 * Created by Kennedy Agusi on 11/7/2017.
 */

public class Student implements Serializable {
    private int id;
    private String name;
    private String email;
    private String password;
    private String apiKey;
    private String dateCreated;

    public Student(){

    }

    public void make(int id, String name, String email, String password, String apiKey, String dateCreated) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.apiKey = apiKey;
        this.dateCreated = dateCreated;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }
}

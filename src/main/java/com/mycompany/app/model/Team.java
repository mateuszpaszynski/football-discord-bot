package com.mycompany.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Team {

    @Id 
    private Long id; 
    
    private String name;
    private String shortName;
    private String tla;

    public Long getId() {
        return this.id;
    }
    public String getName() {
        return this.name == null || this.name.equals("null") ? " - " :this.name;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getTla() {
        return this.tla == null || this.tla.equals("null") ? " - " : this.tla;
    }
    public void setTla(String tla) {
        this.tla = tla;
    }
    public String getShortName() {
        return this.shortName == null || this.shortName.equals("null") ? " - " : this.shortName;
    }
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
    public String getDisplayName() {
        return this.shortName == null || this.shortName.equals("null") ? this.name : this.shortName;
    }
    public Team(){
        
    }
    public Team(Long id, String name, String shortName, String tla){
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.tla = tla;
    }
}
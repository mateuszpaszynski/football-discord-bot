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
        return id;
    }
    public String getName() {
        return name;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getTla() {
        if ( this.tla == null || this.tla.equals(null)) {
            return " - ";
        }
        return this.tla;
    }
    public void setTla(String tla) {
        this.tla = tla;
    }
    public String getShortName() {
        return this.shortName;
    }
    public void setShortName(String shortName) {
        this.shortName = shortName;
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
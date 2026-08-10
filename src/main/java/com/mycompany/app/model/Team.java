package com.mycompany.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Team {

    @Id 
    private Long id; 
    
    private String name;

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void setId(Long _id) {
        this.id = _id;
    }
    public void setName(String _name) {
        this.name = _name;
    }

    public Team(){
        
    }
    public Team(Long _id, String _name){
        this.id = _id;
        this.name = _name;
    }
}
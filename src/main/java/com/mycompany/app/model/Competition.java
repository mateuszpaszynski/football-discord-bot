package com.mycompany.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Competition {
    @Id
    private Long id;

    private String name;

    private String type;

    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getName(){
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return this.type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public Competition() {

    }
    public Competition(Long id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }
}

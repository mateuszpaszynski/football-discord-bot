package com.mycompany.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Competition {
    @Id
    private Long id;

    private String name;
    private String code;
    private String type;

    private String country;

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
    public String getCode() {
        return this.code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getType() {
        return this.type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getCountry() {
        return this.country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public Competition() {

    }
    public Competition(Long id, String name, String code, String type, String country) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.type = type;
        this.country = country;
    }
}

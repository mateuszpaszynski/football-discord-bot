package com.mycompany.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
@Entity
public class Person {

    @Id
    private Long id;

    private String name;
    private String nationality;

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getNationality() {
        return nationality;
    }
    public void setId(Long _id) {
        this.id = _id;
    }
    public void setName(String _name) {
        this.name = _name;
    }
    public void setNationality(String _nationality) {
        this.nationality = _nationality;
    }
    public Person() {
        
    }
    public Person(Long _id, String _name, String _nationality) {
        this.id = _id;
        this.name = _name;
        this.nationality = _nationality;
    }
}
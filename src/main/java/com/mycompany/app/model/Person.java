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
        return this.id;
    }
    public String getName() {
        return this.name == null || this.name.equals("null") ? " - " : this.name;
    }
    public String getNationality() {
        return this.nationality == null || this.nationality.equals("null") ? " - " : this.nationality;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setNationality(String nationality) {
        this.nationality = nationality;
    }
    public Person() {
        
    }
    public Person(Long id, String name, String nationality) {
        this.id = id;
        this.name = name;
        this.nationality = nationality;
    }
}
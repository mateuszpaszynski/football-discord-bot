package com.mycompany.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GeneratedValue;

@Entity
public class Standing {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne
    @JoinColumn(name = "competition_id")
    private Competition competition;

    private Integer position;
    private Integer playedGames;
    private Integer points;
    private Integer goalsFor;
    private Integer goalsAgainst;
    
    public Standing() {

    }
    public Standing(Team team, Competition competition, Integer position, Integer playedGames, Integer points, Integer goalsFor, Integer goalsAgainst) {
        this.team = team;
        this.competition = competition;
        this.position = position;
        this.playedGames = playedGames;
        this.points = points;
        this.goalsFor = goalsFor;
        this.goalsAgainst = goalsAgainst;
    }
    public Team getTeam() {
        return this.team;
    }
    public void setTeam(Team team) {
        this.team = team;
    }
    public Competition getCompetition() {
        return this.competition;
    }
    public void setCompetition(Competition competition) {
        this.competition = competition;
    }
    public Integer getPosition() {
        return this.position;
    }
    public void setPosition(Integer position) {
        this.position = position;
    }
    public Integer getPlayedGames() {
        return this.playedGames;
    }
    public void setPlayedGames(Integer playedGames) {
        this.playedGames = playedGames;
    }
    public Integer getPoints() {
        return this.points;
    }
    public void setPoints(Integer points) {
        this.points = points;
    }
    public Integer getGoalsFor() {
        return this.goalsFor;
    }
    public void setGoalsFor(Integer goalsFor) {
        this.goalsFor = goalsFor;
    }
    public Integer getGoalsAgainst() {
        return this.goalsAgainst;
    }
    public void setGoalsAgainst(Integer goalsAgainst) {
        this.goalsAgainst = goalsAgainst;
    }

}

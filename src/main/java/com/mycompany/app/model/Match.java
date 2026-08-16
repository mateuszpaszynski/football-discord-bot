package com.mycompany.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
@Entity
public class Match {

    @Id
    private Long id;
    @ManyToOne
    @JoinColumn(name = "competition_id")
    private Competition competition;
    private String time;
    private String status;

    @ManyToOne
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;

    @ManyToOne
    @JoinColumn(name = "away_team_id")
    private Team awayTeam;

    private String score;

    public Match() {

    }
    public Match(Long id, Competition competition, String time, String status, Team homeTeam, Team awayTeam, String score) {
       this.id = id;
       this.competition = competition;
       this.time = time;
       this.status = status;
       this.homeTeam = homeTeam;
       this.awayTeam = awayTeam;
       this.score = score; 
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Competition getCompetition() {
        return this.competition;
    }
    public void setCompetition(Competition competitionId) {
        this.competition = competitionId;
    }

    public String getTime() {
        return this.time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getStatus() {
        return this.status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Team getHomeTeam() {
        return this.homeTeam;
    }
    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }
    public Team getAwayTeam() {
        return this.awayTeam;
    }
    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }
    public String getScore() {
        return this.score;
    }
    public void setScore(String score) {
        this.score = score;
    }
}
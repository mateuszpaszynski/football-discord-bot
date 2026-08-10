package com.mycompany.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
@Entity
public class Match {

    @Id
    private Long id;
    
    private Long competitionId;
    private String time;
    private String status;

    @ManyToOne
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;

    @ManyToOne
    @JoinColumn(name = "home_team_id")
    private Team awayTeam;

    private String score;

    public Match() {

    }
    public Match(Long id, Long competitionId, String time, String status, Team homeTeam, Team awayTeam, String score) {
       this.id = id;
       this.competitionId = competitionId;
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
    public Long getCompetitionId() {
        return this.competitionId;
    }
    public void setCompetitionId(Long competitionId) {
        this.competitionId = competitionId;
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
    public void setHomeTeamId(Team homeTeam) {
        this.homeTeam = homeTeam;
    }
    public Team getAwayTeamId() {
        return this.awayTeam;
    }
    public void setAwayTeamId(Team awayTeam) {
        this.awayTeam = awayTeam;
    }
    public String getScore() {
        return this.score;
    }
    public void setScore(String score) {
        this.score = score;
    }
}
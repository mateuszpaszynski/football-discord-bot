package com.mycompany.app.service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.mycompany.app.model.Competition;
import com.mycompany.app.model.Match;
import com.mycompany.app.model.Team;
import com.mycompany.app.repository.MatchRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class MatchService {

    private final MatchRepository matchRepository;

    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }
    public List<Match> getNextMatches(Competition competition) {
        return matchRepository.findNextMatchesForCompetition(competition,PageRequest.of(0,5));
    }

    public List<Match> getNextMatches(Team team) {
        return matchRepository.findNextMatchesForTeam(team,PageRequest.of(0, 5));
    }
    public List<Match> getLastMatches(Team team) {
        return matchRepository.findLastMatchesForTeam(team,PageRequest.of(0,5));
    }
    public List<Match> getLastMatches(Competition comp) {
        return matchRepository.findLastMatchesForCompetition(comp,PageRequest.of(0,5));
    }
    public List<Match> getTodayMatches() {

        ZoneId polishZone = ZoneId.of("Europe/Warsaw");
        LocalDate today = LocalDate.now(polishZone);
        String startOfDayUtc = today.atStartOfDay(polishZone)
                                .withZoneSameInstant(ZoneOffset.UTC)
                                .format(DateTimeFormatter.ISO_INSTANT);
        String endOfDayUtc = today.atTime(23, 59, 59).atZone(polishZone)
                                .withZoneSameInstant(ZoneOffset.UTC)
                                .format(DateTimeFormatter.ISO_INSTANT);
                                
        return matchRepository.findTodayMatches(startOfDayUtc, endOfDayUtc);
    }
    public List<Match> getLast24hMatches() {
        Instant now = Instant.now();
        Instant yesterday = now.minus(24, ChronoUnit.HOURS);
        return matchRepository.findRecentFinishedMatches(yesterday.toString(), now.toString());
    }
    public List<Match> getLiveMatches() {
        return matchRepository.findLiveMatches();
    }
}
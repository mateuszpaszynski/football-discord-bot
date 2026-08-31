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
    public List<Match> getNextMatches(Team team) {
        String now = Instant.now().toString();
        return matchRepository.findNextMatchesForTeam(team,PageRequest.of(0,5), now);
    }

    public List<Match> getNextMatches(Competition comp) {
        String now = Instant.now().toString();
        return matchRepository.findNextMatchesForCompetition(comp,PageRequest.of(0,5), now);
    }
    public List<Match> getLastMatches(Team team) {
        String twoHoursAgo = Instant.now().minus(2, ChronoUnit.HOURS).toString();
        return matchRepository.findLastMatchesForTeam(team, PageRequest.of(0, 5), twoHoursAgo);
    }

    public List<Match> getLastMatches(Competition comp) {
        String twoHoursAgo = Instant.now().minus(2, ChronoUnit.HOURS).toString();
        return matchRepository.findLastMatchesForCompetition(comp, PageRequest.of(0, 5), twoHoursAgo);
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
    public List<Match> getMatchesThatShouldBeLive() {
        Instant now = Instant.now();
        Instant timeWindowStart = now.minus(150, ChronoUnit.MINUTES); // 2.5h for extra times
        return matchRepository.findMatchesThatShouldBeLive(timeWindowStart.toString(), now.toString());
    }
}
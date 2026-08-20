package com.mycompany.app.discord.formatter;

import com.mycompany.app.model.Competition;
import com.mycompany.app.model.Match;
import com.mycompany.app.model.Team;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

public class MatchFormatterTest {
    
    @Test
    void shouldFormatMatchesScheduledListCorrectly() {
       Team team1 = new Team();
        team1.setId(1L);
        team1.setName("Real Madrid CF");
        team1.setShortName("Real Madrid");
        team1.setTla("RMA");

        Team team2 = new Team();
        team2.setId(2L);
        team2.setName("FC Barcelona");
        team2.setShortName(null);
        team2.setTla(null);
        
        Competition comp = new Competition();
        comp.setName("Primera Division");
        comp.setCode("PD");

        Match match = new Match();

        match.setHomeTeam(team1);
        match.setAwayTeam(team2);
        match.setCompetition(comp);
        match.setStatus("SCHEDULED");
        match.setTime("2026-08-22T19:30:00Z");

        List<Match> matches = List.of(match);

        String result = MatchFormatter.format(matches);
        assertThat(result)
        .contains("Real Madrid")
        .doesNotContain("Real Madrid CF")
        .contains("FC Barcelona")
        .contains("PD")
        .doesNotContain("Primera Division")
        .contains("<t:1787427000:f>")
        .doesNotContain("2026-08-22T19:30:00Z")
        .contains("hour").contains("will").contains("change")
        .doesNotContain("null");
    }

        @Test
    void shouldFormatMatchesTimedListCorrectly() {
       Team team1 = new Team();
        team1.setId(1L);
        team1.setName("Real Madrid CF");
        team1.setShortName("Real Madrid");
        team1.setTla("RMA");

        Team team2 = new Team();
        team2.setId(2L);
        team2.setName("FC Barcelona");
        team2.setShortName(null);
        team2.setTla(null);
        
        Competition comp = new Competition();
        comp.setName("Primera Division");
        comp.setCode("PD");

        Match match = new Match();

        match.setHomeTeam(team1);
        match.setAwayTeam(team2);
        match.setCompetition(comp);
        match.setStatus("TIMED");
        match.setTime("2026-08-22T19:30:00Z");

        List<Match> matches = List.of(match);

        String result = MatchFormatter.format(matches);
        assertThat(result)
        .contains("Real Madrid")
        .doesNotContain("Real Madrid CF")
        .contains("FC Barcelona")
        .contains("PD")
        .doesNotContain("Primera Division")
        .contains("<t:1787427000:f>")
        .contains("<t:1787427000:R>")
        .doesNotContain("2026-08-22T19:30:00Z")
        .doesNotContain("hour").doesNotContain("will").doesNotContain("change")
        .doesNotContain("null");
    }

    @Test
    void shouldHandleEmptyListWithoutCrashing() {
        List<Match> emptyList = Collections.emptyList();

        String result = MatchFormatter.format(emptyList);
        assertThat(result)
        .doesNotContain("null");
    }

}

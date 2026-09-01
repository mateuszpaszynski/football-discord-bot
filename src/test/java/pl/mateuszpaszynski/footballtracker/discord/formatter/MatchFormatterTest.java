package pl.mateuszpaszynski.footballtracker.discord.formatter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import pl.mateuszpaszynski.footballtracker.discord.formatter.MatchFormatter;
import pl.mateuszpaszynski.footballtracker.model.Competition;
import pl.mateuszpaszynski.footballtracker.model.Match;
import pl.mateuszpaszynski.footballtracker.model.Team;

public class MatchFormatterTest {
    
    @Test
    void shouldFormatScheduledMatchesCorrectly() {
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
        match.setLastUpdated(Instant.parse("2026-08-07T18:00:00Z"));
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
    void shouldFormatTimedMatchesCorrectly() {
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
        match.setLastUpdated(Instant.parse("2026-08-07T18:00:00Z"));
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
    void shouldFormatLiveMatchesCorrectly() {
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
        match.setStatus("LIVE");
        match.setTime("2026-08-22T19:30:00Z");
        match.setLastUpdated(Instant.parse("2026-08-07T18:00:00Z"));
        match.setScore("3 - 1");
        List<Match> matches = List.of(match);

        String result = MatchFormatter.format(matches);
        assertThat(result)
        .contains("Real Madrid")
        .doesNotContain("Real Madrid CF")
        .contains("FC Barcelona")
        .contains("PD")
        .doesNotContain("Primera Division")
        .contains("LIVE")
        .contains("3 - 1")
        .doesNotContain("2026-08-22T19:30:00Z")
        .doesNotContain("hour").doesNotContain("will").doesNotContain("change")
        .doesNotContain("null");
    }

    @Test
    void shouldFormatFinishedMatchesCorrectly() {
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
        match.setStatus("FINISHED");
        match.setTime("2026-08-22T19:30:00Z");
        match.setLastUpdated(Instant.parse("2026-08-07T18:00:00Z"));
        match.setScore("8 - 2");
        List<Match> matches = List.of(match);

        String result = MatchFormatter.format(matches);
        assertThat(result)
        .contains("Real Madrid")
        .doesNotContain("Real Madrid CF")
        .contains("FC Barcelona")
        .contains("PD")
        .doesNotContain("Primera Division")
        .doesNotContain("LIVE")
        .contains("8 - 2")
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

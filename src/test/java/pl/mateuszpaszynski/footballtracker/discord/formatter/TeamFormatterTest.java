package pl.mateuszpaszynski.footballtracker.discord.formatter;

import org.junit.jupiter.api.Test;

import pl.mateuszpaszynski.footballtracker.discord.formatter.TeamFormatter;
import pl.mateuszpaszynski.footballtracker.model.Team;

import java.util.List;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class TeamFormatterTest {
    
    @Test
    void shouldFormatTeamsListCorrectly() {
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

        List<Team> teams = List.of(team1,team2);
        
        String result = TeamFormatter.format(teams);
        assertThat(result)
        .startsWith("```")
        .contains("Team")
        .contains("code")
        .contains("Real Madrid")
        .doesNotContain("Real Madrid CF")
        .contains("RMA")
        .contains("FC Barcelona")
        .contains("Note")
        .doesNotContain("null");
        
    }

    @Test
    void shouldHandleEmptyListWithoutCrashing() {
        List<Team> emptyList = Collections.emptyList();

        String result = TeamFormatter.format(emptyList);
        assertThat(result)
        .startsWith("```")
        .contains("Team")
        .contains("code")
        .contains("Note")
        .doesNotContain("null");

    }
}

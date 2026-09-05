package pl.mateuszpaszynski.footballtracker.discord.formatter;

import org.junit.jupiter.api.Test;

import pl.mateuszpaszynski.footballtracker.model.Competition;
import pl.mateuszpaszynski.footballtracker.model.Standing;
import pl.mateuszpaszynski.footballtracker.model.Team;

import java.util.List;
import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;

public class StandingExtendedFormatterTest {
    
    @Test
    void shouldFormatMatchesListCorrectly() {
        
        
        Standing standing = new Standing();

        Competition comp1 = new Competition();
        comp1.setName("Premier League");
        comp1.setCode("PL");

        Team team = new Team();
        team.setId(2L);
        team.setName("Real Madrid CF");
        team.setShortName("Real Madrid");
        team.setTla("RMA");
        
        standing.setPosition(1);
        standing.setForm(null);
        standing.setCompetition(comp1);
        standing.setTeam(team);
        List<Standing> listOfStandings = List.of(standing);
        String result = StandingExtendedFormatter.format(listOfStandings);
        
        assertThat(result)
            .startsWith("```")
            .contains("Team")
            .contains("M")
            .contains("W")
            .contains("D")
            .contains("L")
            .contains("GF")
            .contains("GA")
            .contains("GD")
            .contains("Pts")
            .contains("Last 5")
            .contains("Real Madrid")
            .doesNotContain("Real Madrid CF")
            .doesNotContain("Premier League") // we split table into two messages so we dont add header inside formatter
            .doesNotContain("RMA")
            .doesNotContain("null");
    }
    @Test
    void shouldHandleEmptyListWithoutCrashing() {
        List<Standing> emptyList = Collections.emptyList();

        String result = StandingExtendedFormatter.format(emptyList);

        assertThat(result)
            .startsWith("```")
            .contains("Team")
            .contains("M")
            .contains("W")
            .contains("D")
            .contains("L")
            .contains("GF")
            .contains("GA")
            .contains("GD")
            .contains("Pts")
            .contains("Last 5")
            .doesNotContain("null");
    }

}

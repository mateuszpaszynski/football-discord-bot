package pl.mateuszpaszynski.footballtracker.discord.formatter;

import org.junit.jupiter.api.Test;

import pl.mateuszpaszynski.footballtracker.discord.formatter.CompetitionFormatter;
import pl.mateuszpaszynski.footballtracker.model.Competition;

import java.util.List;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class CompetitionFormatterTest {

    @Test
    void shouldFormatCompetitionsListCorrectly() {
        Competition comp1 = new Competition();
        comp1.setName("Premier League");
        comp1.setCode("PL");

        Competition comp2 = new Competition();
        comp2.setName("Primera Division");
        comp2.setCode("PD");

        List<Competition> competitions = List.of(comp1, comp2);

        String result = CompetitionFormatter.format(competitions);
        assertThat(result)
                .startsWith("**Available competitions**")
                .contains("Premier League")
                .contains("PL ")
                .contains("Primera Division")
                .contains("PD ")
                .endsWith("```");
    }
    @Test
    void shouldHandleEmptyListWithoutCrashing() {
        
        List<Competition> emptyList = Collections.emptyList();

        String result = CompetitionFormatter.format(emptyList);
        
        assertThat(result)
                .startsWith("**Available competitions**")
                .contains("Name")
                .contains("code")
                .endsWith("```");

        assertThat(result).doesNotContain("null"); 
    }
}
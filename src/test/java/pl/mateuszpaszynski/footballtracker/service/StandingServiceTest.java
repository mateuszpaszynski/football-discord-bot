package pl.mateuszpaszynski.footballtracker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.mateuszpaszynski.footballtracker.model.Competition;
import pl.mateuszpaszynski.footballtracker.model.Standing;
import pl.mateuszpaszynski.footballtracker.repository.StandingRepository;
import pl.mateuszpaszynski.footballtracker.service.StandingService;

import java.util.List;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StandingServiceTest {

    @Mock
    private StandingRepository standingRepository;

    @InjectMocks
    private StandingService standingService;

    @Test
    void shouldReturnStandingsForGivenCompetition() {

        Competition comp = new Competition();
        comp.setCode("PL");

        Standing standing1 = new Standing();
        standing1.setPosition(1);
        Standing standing2 = new Standing();
        standing2.setPosition(2);
        
        List<Standing> mockDatabaseResponse = List.of(standing1, standing2);

        when(standingRepository.findByCompetitionOrderByPositionAsc(comp))
                .thenReturn(mockDatabaseResponse);

        List<Standing> result = standingService.getStandings(comp);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPosition()).isEqualTo(1);

        verify(standingRepository, times(1)).findByCompetitionOrderByPositionAsc(comp);
    }

    @Test
    void shouldReturnEmptyListWhenDatabaseIsEmpty() {
       
        Competition comp = new Competition();
        comp.setCode("PD");

        when(standingRepository.findByCompetitionOrderByPositionAsc(comp))
                .thenReturn(Collections.emptyList());

        List<Standing> result = standingService.getStandings(comp);

        assertThat(result).isEmpty();
        verify(standingRepository, times(1)).findByCompetitionOrderByPositionAsc(comp);
    }
}
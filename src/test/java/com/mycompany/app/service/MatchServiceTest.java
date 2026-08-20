package com.mycompany.app.service;

import com.mycompany.app.model.Competition;
import com.mycompany.app.model.Match;
import com.mycompany.app.model.Team;
import com.mycompany.app.repository.MatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private MatchService matchService;
    @Test
    void shouldReturnNextMatchesForCompetition() {
        Competition comp = new Competition();
        comp.setCode("PL");

        Match match1 = new Match();
        match1.setId(1L);
        Match match2 = new Match();
        match2.setId(2L);

        List<Match> mockMatches = List.of(match1, match2);

        when(matchRepository.findNextMatchesForCompetition(comp, PageRequest.of(0, 5)))
                .thenReturn(mockMatches);

        List<Match> result = matchService.getMatches(comp);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);


        verify(matchRepository, times(1)).findNextMatchesForCompetition(comp, PageRequest.of(0, 5));
    }

    @Test
    void shouldReturnNextMatchesForTeam() {
        Team team = new Team();
        team.setTla("RMA");

        Match match1 = new Match();
        match1.setId(99L);

        List<Match> mockMatches = List.of(match1);

        when(matchRepository.findNextMatchesForTeam(team, PageRequest.of(0, 5)))
                .thenReturn(mockMatches);

        List<Match> result = matchService.getMatches(team);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(99L);

        verify(matchRepository, times(1)).findNextMatchesForTeam(team, PageRequest.of(0, 5));
    }
}
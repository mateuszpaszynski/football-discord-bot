package com.mycompany.app.service;

import com.mycompany.app.model.Competition;
import com.mycompany.app.model.Match;
import com.mycompany.app.model.Team;
import com.mycompany.app.repository.MatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
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

        List<Match> result = matchService.getNextMatches(comp);

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

        List<Match> result = matchService.getNextMatches(team);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(99L);

        verify(matchRepository, times(1)).findNextMatchesForTeam(team, PageRequest.of(0, 5));
    }

    @Test
    void shouldReturnLastMatchesForTeam() {
        Team team = new Team();
        team.setTla("RMA");

        Match match1 = new Match();
        match1.setId(99L);

        List<Match> mockMatches = List.of(match1);

        when(matchRepository.findLastMatchesForTeam(team, PageRequest.of(0, 5)))
                .thenReturn(mockMatches);

        List<Match> result = matchService.getLastMatches(team);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(99L);
        verify(matchRepository, times(1)).findLastMatchesForTeam(team, PageRequest.of(0, 5));
    }
    @Test
    void shouldReturnLastMatchesForCompetition() {
        Competition comp = new Competition();
        comp.setCode("PL");

        Match match1 = new Match();
        match1.setId(1L);
        Match match2 = new Match();
        match2.setId(2L);

        List<Match> mockMatches = List.of(match1, match2);

        when(matchRepository.findLastMatchesForCompetition(comp, PageRequest.of(0, 5)))
                .thenReturn(mockMatches);

        List<Match> result = matchService.getLastMatches(comp);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(matchRepository, times(1)).findLastMatchesForCompetition(comp, PageRequest.of(0, 5));
    }
    @Test
    void shouldReturnLiveMatches() {
        
        when(matchRepository.findLiveMatches()).thenReturn(Collections.emptyList());

        matchService.getLiveMatches();
        verify(matchRepository).findLiveMatches();
    }
    @Test
    void shouldReturnLast24hMatches() {
        Instant fixedNow = Instant.parse("2026-08-28T12:00:00Z");

    
        try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS)) {
            mockedInstant.when(Instant::now).thenReturn(fixedNow);

            matchService.getLast24hMatches();

            verify(matchRepository).findRecentFinishedMatches("2026-08-27T12:00:00Z", "2026-08-28T12:00:00Z");
        }
    }

    @Test
    void shouldReturnTodayMatches() {

        ZoneId polishZone = ZoneId.of("Europe/Warsaw");
        LocalDate fixedToday = LocalDate.of(2026, 8, 28);

        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mockedLocalDate.when(() -> LocalDate.now(polishZone)).thenReturn(fixedToday);

            matchService.getTodayMatches();
            verify(matchRepository).findTodayMatches("2026-08-27T22:00:00Z", "2026-08-28T21:59:59Z");
        }
    }
}
package com.mycompany.app.service;

import com.mycompany.app.model.Competition;
import com.mycompany.app.model.Standing;
import com.mycompany.app.model.Team;
import com.mycompany.app.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private StandingService standingService;

    @InjectMocks
    private TeamService teamService;

    @Test
    void shouldReturnTeamForGivenQueryByTla() {
        Team team1 = new Team();
        team1.setTla("RMA");
        
        Team team2 = new Team();
        team2.setTla("FCB");
        team2.setId(2L);

        Team team3 = new Team();
        team3.setTla("FCB");
        team3.setId(3L);

        when(teamRepository.findByTla("RMA")).thenReturn(List.of(team1));
        when(teamRepository.findByTla("FCB")).thenReturn(List.of(team2,team3));

        List<Team> resultRMA = teamService.getTeam("RMA");
        assertThat(resultRMA).hasSize(1);
        assertThat(resultRMA.get(0).getTla()).isEqualTo("RMA");

        List<Team> resultFCB = teamService.getTeam("FCB");
        assertThat(resultFCB).hasSize(2);
        assertThat(resultFCB.get(1).getId()).isEqualTo(3L);

        verify(teamRepository,times(1)).findByTla("RMA");
        verify(teamRepository,times(1)).findByTla("FCB");

        verify(teamRepository, never()).findByName(anyString());
        verify(teamRepository, never()).findById(anyLong());
        verify(teamRepository, never()).findByShortName(anyString());
    }

    @Test
    void shouldReturnTeamForGivenQueryByShortName() {
        Team team1 = new Team();
        team1.setTla(null);
        team1.setShortName("Real Madrid");
        
        when(teamRepository.findByTla("REAL MADRID")).thenReturn(Collections.emptyList());
        when(teamRepository.findByShortName("Real Madrid")).thenReturn(List.of(team1));
        
        List<Team> result = teamService.getTeam("Real Madrid");
        assertThat(result).hasSize(1);
        
        verify(teamRepository,times(1)).findByShortName("Real Madrid");
        
        verify(teamRepository, never()).findByName(anyString());
        verify(teamRepository, never()).findById(anyLong());
    }

    @Test
    void shouldReturnTeamForGivenQueryByName() {
        Team team1 = new Team();
        team1.setTla(null);
        team1.setShortName(null);
        team1.setName("Real Madrid CF");
        
        when(teamRepository.findByTla("REAL MADRID CF")).thenReturn(Collections.emptyList());
        when(teamRepository.findByShortName("Real Madrid CF")).thenReturn(Collections.emptyList());
        when(teamRepository.findByName("Real Madrid CF")).thenReturn(List.of(team1));
        
        List<Team> result = teamService.getTeam("Real Madrid CF");
        assertThat(result).hasSize(1);
        
        verify(teamRepository,times(1)).findByName("Real Madrid CF");
        
        verify(teamRepository, never()).findById(anyLong());
    }

    @Test
    void shouldReturnTeamForGivenQueryById() {
        Team team1 = new Team();
        team1.setTla(null);
        team1.setShortName(null);
        team1.setName(null);
        team1.setId(1L);

        when(teamRepository.findByTla("1")).thenReturn(Collections.emptyList());
        when(teamRepository.findByShortName("1")).thenReturn(Collections.emptyList());
        when(teamRepository.findByName("1")).thenReturn(Collections.emptyList());

        Optional<Team> t = Optional.of(team1);
        when(teamRepository.findById(1L)).thenReturn(t);
        
        List<Team> result = teamService.getTeam("1");
        assertThat(result).hasSize(1);
        verify(teamRepository,times(1)).findById(1L);
    }

    @Test
    void shouldReturnEmptyListWhenTeamNotFound() {
  
        when(teamRepository.findByTla("M2")).thenReturn(Collections.emptyList());
        when(teamRepository.findByShortName("M2")).thenReturn(Collections.emptyList());
        when(teamRepository.findByName("M2")).thenReturn(Collections.emptyList());
        
        List<Team> teams = teamService.getTeam("M2");
        assertThat(teams).hasSize(0);
        verify(teamRepository,times(0)).findById(anyLong());
    }

    @Test
    void shouldReturnTeamsForGivenCompetition() {

        Competition comp = new Competition();
        comp.setCode("PD");

        
        Standing standing1 = new Standing();
        standing1.setPosition(1);
        Standing standing2 = new Standing();
        standing2.setPosition(2);

        Team team1 = new Team();
        team1.setName("Real Madrid");
        team1.setId(1L);
        standing1.setTeam(team1);

        Team team2 = new Team();
        team2.setName("FC Barcelona");
        team2.setId(2L);
        standing2.setTeam(team2);

        List<Standing> mockDbResponse = List.of(standing1,standing2);

        when(standingService.getStandings(comp)).thenReturn(mockDbResponse);

        List<Team> result = teamService.getTeams(comp);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Real Madrid");

        verify(standingService, times(1)).getStandings(comp);
    }

    @Test
    void shouldReturnEmptyListWhenDatabaseIsEmpty() {
        Competition comp = new Competition();
        comp.setCode("PD");

        when(standingService.getStandings(comp)).thenReturn(Collections.emptyList());
        
        List<Team> result = teamService.getTeams(comp);

        assertThat(result).isEmpty();
        verify(standingService, times(1)).getStandings(comp);
    }
}
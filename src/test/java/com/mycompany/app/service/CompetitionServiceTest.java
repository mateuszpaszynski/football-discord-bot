package com.mycompany.app.service;


import com.mycompany.app.model.Competition;
import com.mycompany.app.repository.CompetitionRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompetitionServiceTest {
    
    @Mock
    CompetitionRepository competitionRepository;

    @InjectMocks
    CompetitionService competitionService;


    @Test
    void shouldGetCompetitionByCode() {
        Competition comp = new Competition();
        comp.setCode("PL");

        Optional<Competition> c = Optional.of(comp);
        when(competitionRepository.findByCode("PL")).thenReturn(c);

        Competition result = competitionService.getCompetition("pl"); // should be same as "PL"
        
        assertThat(result.getCode()).isEqualTo("PL");
        verify(competitionRepository,times(1)).findByCode(anyString());
        verify(competitionRepository,never()).findByName(anyString());
        verify(competitionRepository,never()).findById(anyLong());
        
    }

    @Test
    void shouldGetCompetitionByName() {
        Competition comp = new Competition();
        comp.setCode(null);
        comp.setName("Premier League");

        Optional<Competition> c = Optional.of(comp);

        when(competitionRepository.findByCode("PREMIER LEAGUE")).thenReturn(Optional.empty());
        when(competitionRepository.findByName("Premier League")).thenReturn(c);
        
        Competition result = competitionService.getCompetition("Premier League");
        assertThat(result.getName()).isEqualTo("Premier League");

        verify(competitionRepository,times(1)).findByName(anyString());
        verify(competitionRepository,never()).findById(anyLong());

    }
    @Test
    void shouldGetCompetitionById() { 
        Competition comp = new Competition();
        comp.setCode(null);
        comp.setName(null);
        comp.setId(1L);

        Optional<Competition> c = Optional.of(comp);
        when(competitionRepository.findByCode("1")).thenReturn(Optional.empty());
        when(competitionRepository.findByName("1")).thenReturn(Optional.empty());
        when(competitionRepository.findById(1L)).thenReturn(c);

        Competition result = competitionService.getCompetition("1");
        assertThat(result.getId()).isEqualTo(1L);

        verify(competitionRepository,times(1)).findById(anyLong());

    }
    @Test
    void shouldThrowWhenCompetitionNotFound() {
        
        when(competitionRepository.findByCode("EKSTRAKLASA")).thenReturn(Optional.empty());
        when(competitionRepository.findByName("Ekstraklasa")).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> competitionService.getCompetition("Ekstraklasa"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Ekstraklasa");
    }

    @Test
    void shouldReturnAllCompetitions() {

        Competition comp1 = new Competition();
        comp1.setCode("PL");

        Competition comp2 = new Competition();
        comp2.setCode("PD");

        List<Competition> mockDbResult = List.of(comp1,comp2);
        
        when(competitionRepository.findAll()).thenReturn(mockDbResult);

        List<Competition> result = competitionService.getCompetitions();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCode()).isEqualTo("PL");

        verify(competitionRepository,times(1)).findAll();
    }
    @Test
    void shouldReturnEmptyListWhenDatabaseIsEmpty() {
        List<Competition> emptyList = Collections.emptyList();

        when(competitionRepository.findAll()).thenReturn(emptyList);

        List<Competition> result = competitionService.getCompetitions();

        assertThat(result).isEmpty();
        verify(competitionRepository, times(1)).findAll();
    }
}

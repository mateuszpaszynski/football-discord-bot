package pl.mateuszpaszynski.footballtracker.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import pl.mateuszpaszynski.footballtracker.model.Competition;
import pl.mateuszpaszynski.footballtracker.repository.CompetitionRepository;

@Service
public class CompetitionService {

    private final CompetitionRepository competitionRepository;

    public CompetitionService(CompetitionRepository competitionRepository) {
        this.competitionRepository = competitionRepository;
    }

public Competition getCompetition(String query) {
    return competitionRepository.findByCode(query.toUpperCase())
        .or(() -> competitionRepository.findByName(query))
        .or(() -> query.matches("\\d+") 
                  ? competitionRepository.findById(Long.valueOf(query)) 
                  : Optional.empty())
        .orElseThrow(() -> new IllegalArgumentException("League '" + query + "' not found"));
}
    public List<Competition> getCompetitions() {
        List<Competition> allComps = competitionRepository.findAll();
        return allComps;
    }

}
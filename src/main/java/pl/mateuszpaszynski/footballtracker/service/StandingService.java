package pl.mateuszpaszynski.footballtracker.service;

import java.util.List;
import org.springframework.stereotype.Service;

import pl.mateuszpaszynski.footballtracker.model.Competition;
import pl.mateuszpaszynski.footballtracker.model.Standing;
import pl.mateuszpaszynski.footballtracker.repository.StandingRepository;

@Service
public class StandingService {

    private final StandingRepository standingRepository;

    public StandingService(StandingRepository standingRepository) {
        this.standingRepository = standingRepository;
    }

    public List<Standing> getStandings(Competition competition) {
        return standingRepository.findByCompetitionOrderByPositionAsc(competition);
    }
}
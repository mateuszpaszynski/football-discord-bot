package pl.mateuszpaszynski.footballtracker.event;

import pl.mateuszpaszynski.footballtracker.model.Match;
import pl.mateuszpaszynski.footballtracker.model.Team;

public record GoalScoredEvent(Match match, Team scoringTeam, String newScore) {}



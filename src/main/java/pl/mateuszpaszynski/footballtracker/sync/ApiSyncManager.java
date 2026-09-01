package pl.mateuszpaszynski.footballtracker.sync;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import pl.mateuszpaszynski.footballtracker.client.FootballApiClient;
import pl.mateuszpaszynski.footballtracker.model.Competition;
import pl.mateuszpaszynski.footballtracker.model.Match;
import pl.mateuszpaszynski.footballtracker.model.Standing;
import pl.mateuszpaszynski.footballtracker.model.Team;
import pl.mateuszpaszynski.footballtracker.repository.CompetitionRepository;
import pl.mateuszpaszynski.footballtracker.repository.MatchRepository;
import pl.mateuszpaszynski.footballtracker.repository.StandingRepository;
import pl.mateuszpaszynski.footballtracker.repository.TeamRepository;
import pl.mateuszpaszynski.footballtracker.service.CompetitionService;
import pl.mateuszpaszynski.footballtracker.service.MatchService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ApiSyncManager {


    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final CompetitionRepository competitionRepository;
    private final StandingRepository standingRepository;
    
    private final CompetitionService competitionService;
    private final MatchService matchService;
    private final FootballApiClient footballApiClient;

    private final RateLimitManager rateLimitManager;

    public ApiSyncManager(TeamRepository teamRepository, MatchRepository matchRepository, CompetitionRepository competitionRepository, StandingRepository standingRepository, 
                         CompetitionService competitionService, MatchService matchService, FootballApiClient footballApiClient, RateLimitManager rateLimitManager
    ) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.competitionRepository = competitionRepository;
        this.standingRepository = standingRepository;

        this.competitionService = competitionService;
        this.matchService = matchService;
        this.footballApiClient = footballApiClient;

        this.rateLimitManager = rateLimitManager;
    }
    public void parseMatchesFromJson(JsonNode matchesNode) {
        int updatedCount = 0;
        for (JsonNode matchJson : matchesNode) {
            try {
                if (!matchJson.hasNonNull("competition") || !matchJson.get("competition").hasNonNull("id")  || !matchJson.hasNonNull("homeTeam") || !matchJson.get("homeTeam").hasNonNull("id") || !matchJson.get("homeTeam").hasNonNull("name") 
                || !matchJson.hasNonNull("awayTeam") || !matchJson.get("awayTeam").hasNonNull("id") || !matchJson.get("awayTeam").hasNonNull("name")
                || !matchJson.hasNonNull("id") || !matchJson.hasNonNull("status")) {
                    throw new IllegalArgumentException("Missing crucial data for match");
                }
                Long competitionId = matchJson.get("competition").get("id").asLong();
                Competition competition = competitionRepository.findById(competitionId).orElseThrow(() -> new IllegalArgumentException("cannot decipher league")); // this cannot happen as we fetch only from what we have in db but you know how it is
                Instant lastUpdated = null;
                if (matchJson.hasNonNull("lastUpdated")) {
                    lastUpdated = Instant.parse(matchJson.get("lastUpdated").asText());
                }
                JsonNode homeTeamNode = matchJson.get("homeTeam");
                Long homeTeamId = homeTeamNode.get("id").asLong();
                String homeTeamName = homeTeamNode.get("name").asText();
                String homeTeamShortName = homeTeamNode.hasNonNull("shortName") ? homeTeamNode.get("shortName").asText() : null;
                String homeTla = homeTeamNode.hasNonNull("tla") ? homeTeamNode.get("tla").asText() : null;
                Team homeTeam = teamRepository.findById(homeTeamId)
                    .orElseGet(() -> teamRepository.save(new Team(homeTeamId, homeTeamName, homeTeamShortName, homeTla)));

                JsonNode awayTeamNode = matchJson.get("awayTeam");
                Long awayTeamId = awayTeamNode.get("id").asLong();
                String awayTeamName = awayTeamNode.get("name").asText(); 
                String awayTeamShortName = awayTeamNode.hasNonNull("shortName") ? awayTeamNode.get("shortName").asText() : null;
                String awayTeamTla = awayTeamNode.hasNonNull("tla") ? awayTeamNode.get("tla").asText() : null;
                Team awayTeam = teamRepository.findById(awayTeamId)
                    .orElseGet(() -> teamRepository.save(new Team(awayTeamId, awayTeamName, awayTeamShortName, awayTeamTla)));

                Long matchId = matchJson.get("id").asLong();
                String utcDate = matchJson.get("utcDate").asText();
                String status = matchJson.get("status").asText();

                // status sometimes arrives broken for certain leagues;
                if (status.matches(".*\\d+.*")) {
                    status = "TIMED"; 
                }

                Instant matchTime = Instant.parse(utcDate);
                Instant now = Instant.now();

                if (now.isAfter(matchTime.plus(2, ChronoUnit.HOURS))) {
                    status = "FINISHED"; // 2hours ago probably ended
                } else if (now.isAfter(matchTime)) {
                    status = "IN_PLAY"; // started but not enough time passed so we guess its in play
                }
                
                String score = "TBD";
                
                if (status.equals("FINISHED") || status.equals("LIVE") || status.equals("PAUSED") || status.equals("IN_PLAY")) {
                    JsonNode scoreNode = matchJson.get("score");
                    if (scoreNode != null && scoreNode.hasNonNull("fullTime") && 
                        scoreNode.get("fullTime").hasNonNull("home") && scoreNode.get("fullTime").hasNonNull("away")) {
                        
                        String homeGoals = scoreNode.get("fullTime").get("home").asText();
                        String awayGoals = scoreNode.get("fullTime").get("away").asText();
                        score = homeGoals + " - " + awayGoals;
                    }
                }
                
                Match match = new Match(matchId, competition, lastUpdated, utcDate, status, homeTeam, awayTeam, score);
                matchRepository.save(match);
                updatedCount++;
            } catch (Exception e) {
                log.error("Discarded match: {}",e.getMessage());
            }
        }
        log.info("Updated {} matches", updatedCount);

    }
    public void fetchFixturesForToday() {
        List<Match> matches = matchService.getMatchesThatShouldBeLive();
        if (matches.isEmpty()) {
            return;
        }
        HashSet<String> leagueCodes = new HashSet<>();
        for (Match match : matches) {
            leagueCodes.add(match.getCompetition().getCode());
        }
        StringBuilder query = new StringBuilder();
        for (String leagueCode : leagueCodes) {
            query.append(leagueCode + ",");
        }
        try {
            rateLimitManager.apply();
            JsonNode matchesNode = footballApiClient.fetchRawFixturesForToday(query.toString());
            parseMatchesFromJson(matchesNode);
        } catch (Exception e) {
                log.error("Critical error fetching matches for today: {}", e.getMessage());
        } 
    }
    public void fetchFixtures() {
        List<Competition> competitions = competitionService.getCompetitions();
        for (Competition competition : competitions) {
            String leagueCode = competition.getCode();

            try {
                rateLimitManager.apply(); 
                JsonNode matchesNode = footballApiClient.fetchRawFixtures(leagueCode);
                parseMatchesFromJson(matchesNode);
                
            } catch (Exception e) {
                log.error("Critical error fetching matches for competition {} cause: {}", leagueCode, e.getMessage());
            } 
        }
    }

    public void fetchStandings() {

        List<Competition> allComps = competitionService.getCompetitions();
        int updatedCount = 0;
        for (Competition competition : allComps) {
            if (competition.getType().equals("LEAGUE")) {
                
                rateLimitManager.apply();
                JsonNode standingsNode = footballApiClient.fetchRawStandings(competition.getId().toString());
                for (JsonNode standing : standingsNode) {

                    if (standing.get("type").asText().equals("TOTAL")) {
                        JsonNode table = standing.get("table");
                        for (JsonNode tableNode : table) {
                            try {
                                if (!tableNode.hasNonNull("position") || !tableNode.hasNonNull("team") || !tableNode.get("team").hasNonNull("id") 
                                || !tableNode.get("team").hasNonNull("name") || !tableNode.hasNonNull("playedGames") || !tableNode.hasNonNull("won")
                                || !tableNode.hasNonNull("draw") || !tableNode.hasNonNull("lost") || !tableNode.hasNonNull("points") || !tableNode.hasNonNull("goalsFor")
                                || !tableNode.hasNonNull("goalsAgainst") || !tableNode.hasNonNull("goalDifference")) 
                                {
                                    throw new IllegalArgumentException("Missing crucial data");
                                }
                                Integer position = tableNode.get("position").asInt();
                                JsonNode teamNode = tableNode.get("team");
                                Long teamId = teamNode.get("id").asLong();
                                String teamName = teamNode.get("name").asText();
                                String teamShortName = teamNode.hasNonNull("shortName") ? teamNode.get("shortName").asText() : null;
                                String teamTla = teamNode.hasNonNull("tla") ? teamNode.get("tla").asText() : null;
                                Team team = teamRepository.findById(teamId)
                                .orElseGet(() -> teamRepository.save(new Team(teamId, teamName, teamShortName, teamTla)));
                                
                                String form = tableNode.hasNonNull("form") ? tableNode.get("form").asText() : null;
                                
                                
                                Integer playedGames = tableNode.get("playedGames").asInt();
                                Integer gamesWon = tableNode.get("won").asInt();
                                Integer gamesDrawn = tableNode.get("draw").asInt();
                                Integer gamesLost = tableNode.get("lost").asInt();
                                Integer points = tableNode.get("points").asInt();
                                Integer goalsFor = tableNode.get("goalsFor").asInt();
                                Integer goalsAgainst = tableNode.get("goalsAgainst").asInt();
                                Integer goalDifference = tableNode.get("goalDifference").asInt();

                                Standing existingStanding = standingRepository.findByTeamIdAndCompetitionId(teamId, competition.getId())
                                .orElse(new Standing());
                                existingStanding.setTeam(team);
                                existingStanding.setCompetition(competition);
                                existingStanding.setPosition(position);
                                existingStanding.setPlayedGames(playedGames);
                                existingStanding.setForm(form);
                                existingStanding.setGamesWon(gamesWon);
                                existingStanding.setGamesDrawn(gamesDrawn);
                                existingStanding.setGamesLost(gamesLost);
                                existingStanding.setPoints(points);
                                existingStanding.setGoalsFor(goalsFor);
                                existingStanding.setGoalsAgainst(goalsAgainst);
                                existingStanding.setGoalDifference(goalDifference);
                                standingRepository.save(existingStanding);    
                                updatedCount++;
                            } catch (Exception e) {
                               log.error("One team from league {} discarded, reason: {}", competition.getName(), e.getMessage());
                            }
                        }
                    break;
                    }
                }
            }
        }
        log.info("Updated {} standings entries", updatedCount);
    }

    public void fetchTeams() {
        int updatedCount = 0;
        List<Competition> allComps = competitionService.getCompetitions();
        for (Competition comp : allComps) {
            String compId = comp.getId().toString();

            rateLimitManager.apply();
            JsonNode teamsNode = footballApiClient.fetchRawTeams(compId);
            for (JsonNode teamNode : teamsNode) {
                try {
                    if (!teamNode.hasNonNull("id") || !teamNode.hasNonNull("name")) {
                        throw new IllegalArgumentException("Missing crucial data: ID or Name");
                    }
                    Long teamId = teamNode.get("id").asLong();
                    String name = teamNode.get("name").asText();

                    String shortName = teamNode.hasNonNull("shortName") ? teamNode.get("shortName").asText() : null;
                    String tla = teamNode.hasNonNull("tla") ? teamNode.get("tla").asText() : null;
                    
                    Team team = new Team(teamId,name,shortName, tla);
                    teamRepository.save(team);
                    updatedCount++;
                } 
                catch (Exception e) {
                    log.error("One team from league " + comp.getName() + " discarded, reason: {}", e.getMessage());
                }
            }
        }
        log.info("Updated {} teams", updatedCount);
    }    
    public void fetchCompetitions() {
        int updatedCount = 0;
        rateLimitManager.apply();
        JsonNode competitionsNode = footballApiClient.fetchRawCompetitions();
        for (JsonNode competition : competitionsNode) {
            try {
                if (!competition.hasNonNull("id") || !competition.hasNonNull("name") ||
                 !competition.hasNonNull("code") || !competition.hasNonNull("type") ) {
                    throw new IllegalArgumentException("Missing crucial data: ID or Name");
                }
                Long competitionId = competition.get("id").asLong();
                String name = competition.get("name").asText();
                String code = competition.get("code").asText();
                String type = competition.get("type").asText();
                
                String country = "Unknown";
                if (competition.hasNonNull("area") && competition.get("area").hasNonNull("name")) {
                    country = competition.get("area").get("name").asText();
                }
                
                Competition comp = new Competition(competitionId, name, code, type, country);
                competitionRepository.save(comp);
                updatedCount++;
            } catch (Exception e) {
                log.error("One league discarded, reason: {}", e.getMessage());
            }
        }
        log.info("Updated {} competitions", updatedCount);
    }
}

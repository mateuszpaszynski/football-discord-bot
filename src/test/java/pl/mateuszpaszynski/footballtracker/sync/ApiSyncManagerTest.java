package pl.mateuszpaszynski.footballtracker.sync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import pl.mateuszpaszynski.footballtracker.sync.ApiSyncManager;
import pl.mateuszpaszynski.footballtracker.sync.RateLimitManager;


@ExtendWith (MockitoExtension.class)
public class ApiSyncManagerTest {
    
    @Mock
    FootballApiClient footballApiClient;

    @Mock
    StandingRepository standingRepository;

    @Mock
    CompetitionRepository competitionRepository;

    @Mock
    TeamRepository teamRepository;

    @Mock
    MatchRepository matchRepository;

    @Mock
    CompetitionService competitionService;
    
    @Mock
    MatchService matchService;

    @Mock
    RateLimitManager rateLimitManager;

    @InjectMocks
    ApiSyncManager apiSyncManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBulkFetchFixturesWithFullData() throws Exception {
        //region
    String apiResponse = """
    [
    {
      "area": {
        "id": 2163,
        "name": "Netherlands",
        "code": "NLD",
        "flag": "https://crests.football-data.org/8601.svg"
      },
      "competition": {
        "id": 2003,
        "name": "Eredivisie",
        "code": "DED",
        "type": "LEAGUE",
        "emblem": "https://crests.football-data.org/ED.png"
      },
      "season": {
        "id": 2493,
        "startDate": "2026-08-07",
        "endDate": "2027-05-23",
        "currentMatchday": 3,
        "winner": null
      },
      "id": 558214,
      "utcDate": "2026-08-07T18:00:00Z",
      "status": "FINISHED",
      "matchday": 1,
      "stage": "REGULAR_SEASON",
      "group": null,
      "lastUpdated": "2026-08-24T05:20:35Z",
      "homeTeam": {
        "id": 1909,
        "name": "SC Cambuur-Leeuwarden",
        "shortName": "Cambuur",
        "tla": "CAM",
        "crest": "https://crests.football-data.org/1909.png"
      },
      "awayTeam": {
        "id": 670,
        "name": "SBV Excelsior",
        "shortName": "Excelsior",
        "tla": "EXC",
        "crest": "https://crests.football-data.org/670.png"
      },
      "score": {
        "winner": "AWAY_TEAM",
        "duration": "REGULAR",
        "fullTime": {
          "home": 0,
          "away": 4
        },
        "halfTime": {
          "home": 0,
          "away": 3
        }
      },
      "odds": {
        "msg": "Activate Odds-Package in User-Panel to retrieve odds."
      },
      "referees": [
        {
          "id": 9561,
          "name": "Allard Lindhout",
          "type": "REFEREE",
          "nationality": "Netherlands"
        }
      ]
    }]          
    """;
       //endregion
       Competition comp = new Competition();
       comp.setId(2003L);
       comp.setName("Eredivisie");
       comp.setCode("DED");
       when(competitionRepository.findById(2003L)).thenReturn(Optional.of(comp));

       JsonNode mockJsonNode = objectMapper.readTree(apiResponse);
       Team hTeam = new Team();
       hTeam.setId(1909L);
       hTeam.setName("SC Cambuur-Leeuwarden");

       Team aTeam = new Team();
       aTeam.setId(670L);
       aTeam.setName("SBV Excelsior");
       Match m = new Match();
       m.setHomeTeam(hTeam);
       m.setAwayTeam(aTeam);
       m.setCompetition(comp);
       m.setId(7L);

       when(matchService.getMatchesThatShouldBeLive()).thenReturn(List.of(m));
       when(footballApiClient.fetchRawFixturesForToday("DED,")).thenReturn(mockJsonNode);
       when(teamRepository.findById(1909L)).thenReturn(Optional.of(hTeam));
       when(teamRepository.findById(670L)).thenReturn(Optional.of(aTeam));
       
       apiSyncManager.fetchFixturesForToday();
       
       ArgumentCaptor<Match> captor = ArgumentCaptor.forClass(Match.class);
       verify(matchRepository,times(1)).save(captor.capture());

       Match match = captor.getValue();
       Team homeTeam = match.getHomeTeam();
       Team awayTeam = match.getAwayTeam();
       assertThat(homeTeam.getName()).isEqualTo("SC Cambuur-Leeuwarden");
       assertThat(homeTeam.getId()).isEqualTo(1909L);
       assertThat(awayTeam.getName()).isEqualTo("SBV Excelsior");
       assertThat(awayTeam.getId()).isEqualTo(670L);
       assertThat(match.getId()).isEqualTo(558214L);
       assertThat(match.getCompetition()).isEqualTo(comp);
       assertThat(match.getTime()).isEqualTo("2026-08-07T18:00:00Z");
       assertThat(match.getScore()).isEqualTo("0 - 4");
    }
    @Test
    void shouldDiscardBulkFetchingWhenNoMatchIsLive() throws Exception {
        when(matchService.getMatchesThatShouldBeLive()).thenReturn(Collections.emptyList());
        apiSyncManager.fetchFixturesForToday();
        verify(matchRepository, never()).save(any(Match.class));
    }
    @Test
    void shouldFetchFixturesWithFullData() throws Exception {
       //region
    String apiResponse = """
    [
    {
      "area": {
        "id": 2163,
        "name": "Netherlands",
        "code": "NLD",
        "flag": "https://crests.football-data.org/8601.svg"
      },
      "competition": {
        "id": 2003,
        "name": "Eredivisie",
        "code": "DED",
        "type": "LEAGUE",
        "emblem": "https://crests.football-data.org/ED.png"
      },
      "season": {
        "id": 2493,
        "startDate": "2026-08-07",
        "endDate": "2027-05-23",
        "currentMatchday": 3,
        "winner": null
      },
      "id": 558214,
      "utcDate": "2026-08-07T18:00:00Z",
      "status": "FINISHED",
      "matchday": 1,
      "stage": "REGULAR_SEASON",
      "group": null,
      "lastUpdated": "2026-08-24T05:20:35Z",
      "homeTeam": {
        "id": 1909,
        "name": "SC Cambuur-Leeuwarden",
        "shortName": "Cambuur",
        "tla": "CAM",
        "crest": "https://crests.football-data.org/1909.png"
      },
      "awayTeam": {
        "id": 670,
        "name": "SBV Excelsior",
        "shortName": "Excelsior",
        "tla": "EXC",
        "crest": "https://crests.football-data.org/670.png"
      },
      "score": {
        "winner": "AWAY_TEAM",
        "duration": "REGULAR",
        "fullTime": {
          "home": 0,
          "away": 4
        },
        "halfTime": {
          "home": 0,
          "away": 3
        }
      },
      "odds": {
        "msg": "Activate Odds-Package in User-Panel to retrieve odds."
      },
      "referees": [
        {
          "id": 9561,
          "name": "Allard Lindhout",
          "type": "REFEREE",
          "nationality": "Netherlands"
        }
      ]
    }]          
    """;
       //endregion
       Competition comp = new Competition();
       comp.setId(2003L);
       comp.setName("Eredivisie");
       comp.setCode("DED");
       when(competitionService.getCompetitions()).thenReturn(List.of(comp));
       when(competitionRepository.findById(2003L)).thenReturn(Optional.of(comp));
       JsonNode mockJsonNode = objectMapper.readTree(apiResponse);
       Team hTeam = new Team();
       hTeam.setId(1909L);
       hTeam.setName("SC Cambuur-Leeuwarden");

       Team aTeam = new Team();
       aTeam.setId(670L);
       aTeam.setName("SBV Excelsior");

       when(footballApiClient.fetchRawFixtures("DED")).thenReturn(mockJsonNode);
       when(teamRepository.findById(1909L)).thenReturn(Optional.of(hTeam));
       when(teamRepository.findById(670L)).thenReturn(Optional.of(aTeam));
       
       apiSyncManager.fetchFixtures();
       
       ArgumentCaptor<Match> captor = ArgumentCaptor.forClass(Match.class);
       verify(matchRepository,times(1)).save(captor.capture());

       Match match = captor.getValue();
       Team homeTeam = match.getHomeTeam();
       Team awayTeam = match.getAwayTeam();
       assertThat(homeTeam.getName()).isEqualTo("SC Cambuur-Leeuwarden");
       assertThat(homeTeam.getId()).isEqualTo(1909L);
       assertThat(awayTeam.getName()).isEqualTo("SBV Excelsior");
       assertThat(awayTeam.getId()).isEqualTo(670L);

       assertThat(match.getId()).isEqualTo(558214L);
       assertThat(match.getCompetition()).isEqualTo(comp);
       assertThat(match.getStatus()).isEqualTo("FINISHED");
       assertThat(match.getTime()).isEqualTo("2026-08-07T18:00:00Z");
       assertThat(match.getScore()).isEqualTo("0 - 4");
    }
    @ParameterizedTest//region
    @ValueSource(strings = {
        //No home team
        """
        [{
          "id": 558214, "utcDate": "2026-08-07T18:00:00Z", "status": "SCHEDULED",
          "awayTeam": { "id": 670, "name": "SBV Excelsior" }
        }]
        """,
        // null name and all work and no play makes jack a dull boy
        """
        [{
          "id": 558214, "utcDate": "2026-08-07T18:00:00Z", "status": "SCHEDULED",
          "homeTeam": { "id": 1909, "name": null },
          "awayTeam": { "id": 670, "name": "SBV Excelsior" }
        }]
        """,
        //null id
        """
        [{
          "id": null, "utcDate": "2026-08-07T18:00:00Z", "status": "SCHEDULED",
          "homeTeam": { "id": 1909, "name": "SC Cambuur" },
          "awayTeam": { "id": 670, "name": "SBV Excelsior" }
        }]
        """,
        //no status
        """
        [{
          "id": 558214, "utcDate": "2026-08-07T18:00:00Z", 
          "homeTeam": { "id": 1909, "name": "SC Cambuur" },
          "awayTeam": { "id": 670, "name": "SBV Excelsior" }
        }]
        """

    })//endregion
    void shouldDiscardFixturesWithCrucialDataMissing(String brokenJsonResponse) throws Exception {
        JsonNode mockJsonNode = objectMapper.readTree(brokenJsonResponse);
        Competition comp = new Competition();
        comp.setId(2003L);
        comp.setCode("DED");
        
        when(competitionService.getCompetitions()).thenReturn(List.of(comp));
        
        when(footballApiClient.fetchRawFixtures("DED")).thenReturn(mockJsonNode);
        
        apiSyncManager.fetchFixtures();
        
        verify(matchRepository, never()).save(any(Match.class));
    }
    
    @Test
    void shouldFetchAndSaveStandingWithFullData() throws Exception {
        String apiResponse = """
            [{
                "stage": "REGULAR_SEASON",
                "type": "TOTAL",
                "group": null,
                "table": [
                    {
                        "position": 1,
                        "team": {
                            "id": 65,
                            "name": "Manchester City FC",
                            "shortName": "Man City",
                            "tla": "MCI",
                            "crest": "https://crests.football-data.org/65.png"
                        },
                        "playedGames": 37,
                        "form": "D,W,W,W,W",
                        "won": 28,
                        "draw": 6,
                        "lost": 3,
                        "points": 90,
                        "goalsFor": 96,
                        "goalsAgainst": 24,
                        "goalDifference": 72
                    }
                ]
            },
            {
                "stage": "REGULAR_SEASON",
                "type": "HOME",
                "group": null,
                "table": [
                    {
                        "position": 1,
                        "team": {
                            "id": 65,
                            "name": "Manchester City FC",
                            "shortName": "Man City",
                            "tla": "MCI",
                            "crest": "https://crests.football-data.org/65.png"
                        },
                        "playedGames": 37,
                        "form": "D,W,W,W,W",
                        "won": 28,
                        "draw": 6,
                        "lost": 3,
                        "points": 90,
                        "goalsFor": 96,
                        "goalsAgainst": 24,
                        "goalDifference": 72
                    }
                ]
            
            }]
            """;
            JsonNode mockJsonNode = objectMapper.readTree(apiResponse);
            Competition comp = new Competition();
            comp.setId(1L);
            comp.setName("Premier League");
            comp.setType("LEAGUE");
            comp.setCode("PL");

            when(competitionService.getCompetitions()).thenReturn(List.of(comp));
            when(footballApiClient.fetchRawStandings(comp.getId().toString())).thenReturn(mockJsonNode);

            Team mockTeam = new Team(65L, "Manchester City FC", "Man City", "MCI");
            when(teamRepository.findById(65L)).thenReturn(Optional.of(mockTeam));

            apiSyncManager.fetchStandings();
            ArgumentCaptor<Standing> captor = ArgumentCaptor.forClass(Standing.class);
            
            verify(standingRepository,times(1)).save(captor.capture());
            Standing standing = captor.getValue();
            Team team = standing.getTeam();
            Competition c = standing.getCompetition();

            assertThat(team.getName()).isEqualTo("Manchester City FC");
            assertThat(team.getId()).isEqualTo(65L);

            assertThat(c.getName()).isEqualTo("Premier League");
            assertThat(c.getId()).isEqualTo(1L);

            assertThat(standing.getPlayedGames()).isEqualTo(37);
            assertThat(standing.getForm()).isEqualTo("D,W,W,W,W");
            assertThat(standing.getGamesWon()).isEqualTo(28);
            assertThat(standing.getGamesDrawn()).isEqualTo(6);
            assertThat(standing.getGamesLost()).isEqualTo(3);
            assertThat(standing.getPoints()).isEqualTo(90);
            assertThat(standing.getGoalsFor()).isEqualTo(96);
            assertThat(standing.getGoalsAgainst()).isEqualTo(24);
            assertThat(standing.getGoalDifference()).isEqualTo(72);

    }

    @ParameterizedTest//region
    @ValueSource(strings = {
        //No position
        """
        [{ "type": "TOTAL", "table": [
            { "team": { "id": 65, "name": "Man City" }, "playedGames": 37, "won": 28, "draw": 6, "lost": 3, "points": 90, "goalsFor": 96, "goalsAgainst": 24, "goalDifference": 72 }
        ]}] 
        """,
        // null position
        """
        [{ "type": "TOTAL", "table": [
            { "position": null, "team": { "id": 65, "name": "Man City" }, "playedGames": 37, "won": 28, "draw": 6, "lost": 3, "points": 90, "goalsFor": 96, "goalsAgainst": 24, "goalDifference": 72 }
        ]}]
        """,

        // no team
        """
        [{ "type": "TOTAL", "table": [
            { "position": 1, "playedGames": 37, "won": 28, "draw": 6, "lost": 3, "points": 90, "goalsFor": 96, "goalsAgainst": 24, "goalDifference": 72 }
        ]}] 
        """,
        // null team id
        """
        [{ "type": "TOTAL", "table": [
            { "position": 1, "team": { "id": null, "name": "Man City" }, "playedGames": 37, "won": 28, "draw": 6, "lost": 3, "points": 90, "goalsFor": 96, "goalsAgainst": 24, "goalDifference": 72 }
        ]}]
        """,

        // no points
        """
        [{ "type": "TOTAL", "table": [
            { "position": 1, "team": { "id": 65, "name": "Man City" }, "playedGames": 37, "won": 28, "draw": 6, "lost": 3, "goalsFor": 96, "goalsAgainst": 24, "goalDifference": 72 }
        ]}] 
        """,
        // null goals
        """
        [{ "type": "TOTAL", "table": [
            { "position": 1, "team": { "id": 65, "name": "Man City" }, "playedGames": 37, "won": 28, "draw": 6, "lost": 3, "points": 90, "goalsFor": null, "goalsAgainst": 24, "goalDifference": 72 }
        ]}] 
        """
    })//endregion
    void shouldDiscardStandingWithMissingCrucialData(String brokenJsonResponse) throws Exception {
        JsonNode mockJsonNode = objectMapper.readTree(brokenJsonResponse);
        Competition comp = new Competition();
        comp.setId(1L);
        comp.setType("LEAGUE");
        
        when(competitionService.getCompetitions()).thenReturn(List.of(comp));
        when(footballApiClient.fetchRawStandings("1")).thenReturn(mockJsonNode);
        
        apiSyncManager.fetchStandings();
        
        verify(standingRepository, never()).save(any(Standing.class));
    }

    @Test
    void shouldFetchAndSaveTeamWithFullData() throws Exception {
        String apiResponse = """
            [{
                "id": 86, "name": "Real Madrid CF", "shortName": "Real Madrid", "tla": "RMA"  
            }]
            """;
        JsonNode mockJsonNode = objectMapper.readTree(apiResponse);

        Competition comp = new Competition();
        comp.setId(1L);
        when(competitionService.getCompetitions()).thenReturn(List.of(comp));
        when(footballApiClient.fetchRawTeams("1")).thenReturn(mockJsonNode);

        apiSyncManager.fetchTeams();

        ArgumentCaptor<Team> captor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository,times(1)).save(captor.capture());

        Team savedTeam = captor.getValue();
        assertThat(savedTeam.getId()).isEqualTo(86L);
        assertThat(savedTeam.getName()).isEqualTo("Real Madrid CF");
        assertThat(savedTeam.getShortName()).isEqualTo("Real Madrid");
        assertThat(savedTeam.getTla()).isEqualTo("RMA");
        
    }

    @ParameterizedTest//region
    @ValueSource(strings = {
        //no tla
        """ 
        [{"id": 86, "name": "Real Madrid CF", "shortName": "Real Madrid" }]
        """,
        //null tla
        """ 
        [{"id": 86, "name": "Real Madrid CF", "shortName": "Real Madrid", "tla": null }]
        """,
    })//endregion
    void shouldFetchAndSaveTeamWithNullValuesWhenTlaIsMissing(String apiResponse) throws Exception {
        
        JsonNode mockJsonNode = objectMapper.readTree(apiResponse);
        Competition comp = new Competition();
        comp.setId(1L);
        
        when(competitionService.getCompetitions()).thenReturn(List.of(comp));
        when(footballApiClient.fetchRawTeams("1")).thenReturn(mockJsonNode);
        
        apiSyncManager.fetchTeams();

        ArgumentCaptor<Team> captor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository,times(1)).save(captor.capture());

        Team savedTeam = captor.getValue();
        assertThat(savedTeam.getId()).isEqualTo(86L);
        assertThat(savedTeam.getName()).isEqualTo("Real Madrid CF");

        assertThat(savedTeam.getShortName()).isEqualTo("Real Madrid");
        assertThat(savedTeam.getTla()).isEqualTo(" - "); // getter is overwritten
    }
    
    @ParameterizedTest//region
    @ValueSource(strings = {
        //no shortname
        """ 
        [{"id": 86, "name": "Real Madrid CF", "tla": "RMA" }]
        """,
        //null shortName
        """ 
        [{"id": 86, "name": "Real Madrid CF", "shortName": null, "tla": "RMA" }]
        """,
    })//endregion
    void shouldFetchAndSaveTeamWithNullValuesWhenShortNameIsMissing(String apiResponse) throws Exception {
        
        JsonNode mockJsonNode = objectMapper.readTree(apiResponse);
        Competition comp = new Competition();
        comp.setId(1L);
        
        when(competitionService.getCompetitions()).thenReturn(List.of(comp));
        when(footballApiClient.fetchRawTeams("1")).thenReturn(mockJsonNode);
        
        apiSyncManager.fetchTeams();

        ArgumentCaptor<Team> captor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository,times(1)).save(captor.capture());

        Team savedTeam = captor.getValue();
        assertThat(savedTeam.getId()).isEqualTo(86L);
        assertThat(savedTeam.getName()).isEqualTo("Real Madrid CF");

        assertThat(savedTeam.getShortName()).isEqualTo(" - "); // getter is overwritten
        assertThat(savedTeam.getTla()).isEqualTo("RMA"); 
    }
    @ParameterizedTest//region
    @ValueSource(strings = {
        // no id
        """
        [{"name": "Real Madrid CF", "shortName": "RMA", "tla": "RMA" }]
        """,
        // null id
        """ 
        [{"id": null, "name": "Real Madrid CF", "shortName": "RMA", "tla": "RMA" }]
        """,
        // no name
        """ 
        [{"id": 86, "shortName": null, "tla": "RMA" }]
        """,
        // null name
        """ 
        [{"id": 86, "name": null, "shortName": "RMA", "tla": "RMA" }]
        """,
    })//endregion
    void shouldDiscardTeamsWithMissingCrucialData(String brokenJsonResponse) throws Exception {
        JsonNode mockJsonNode = objectMapper.readTree(brokenJsonResponse);
        Competition comp = new Competition();
        comp.setId(1L);
        when(competitionService.getCompetitions()).thenReturn(List.of(comp));
        when(footballApiClient.fetchRawTeams("1")).thenReturn(mockJsonNode);
        
        apiSyncManager.fetchTeams();
    
        verify(teamRepository,never()).save(any(Team.class));

    }

    @Test
    void shouldFetchAndSaveCompetitionWithFullData() throws Exception {
        String apiResponse = """
            [ { "id": 2021, "name": "Premier League", "code": "PL", "type": "LEAGUE", "area": { "name": "England" } } ]
            """;
        JsonNode mockJsonNode = objectMapper.readTree(apiResponse);
        when(footballApiClient.fetchRawCompetitions()).thenReturn(mockJsonNode);

        apiSyncManager.fetchCompetitions();
        
        ArgumentCaptor<Competition> captor = ArgumentCaptor.forClass(Competition.class);
        verify(competitionRepository, times(1)).save(captor.capture());

        Competition savedComp = captor.getValue();
        assertThat(savedComp.getCode()).isEqualTo("PL");
        assertThat(savedComp.getCountry()).isEqualTo("England");
    }

    @ParameterizedTest
    //region
    @ValueSource(strings = {
        """
        [ { "id": 2021, "name": "Premier League", "code": "PL", "type": "LEAGUE" } ]
        """,
        """
        [ { "id": 2021, "name": "Premier League", "code": "PL", "type": "LEAGUE", "area": null } ]
        """,
        """
        [ { "id": 2021, "name": "Premier League", "code": "PL", "type": "LEAGUE", "area": { "name": null } } ]
        """
    })//endregion
    void shouldFetchAndSaveCompetitionWithUnknownCountryWhenAreaIsMissing(String apiResponse) throws Exception {
        JsonNode mockJsonNode = objectMapper.readTree(apiResponse);
        when(footballApiClient.fetchRawCompetitions()).thenReturn(mockJsonNode);

        apiSyncManager.fetchCompetitions();
        
        ArgumentCaptor<Competition> captor = ArgumentCaptor.forClass(Competition.class);
        verify(competitionRepository, times(1)).save(captor.capture());

        Competition savedComp = captor.getValue();
        assertThat(savedComp.getCountry()).isEqualTo("Unknown"); 
    }
    
    @ParameterizedTest
    //region
    @ValueSource(strings = {
        // no ID
        """
        [ { "name": "Premier League", "code": "PL", "type": "LEAGUE" } ]
        """,
        //  null ID
        """
        [ { "id": null, "name": "Premier League", "code": "PL", "type": "LEAGUE" } ]
        """,
        //  no name
        """
        [ { "id": 2021, "code": "PL", "type": "LEAGUE" } ]
        """,
        // null name
        """
        [ { "id": 2021, "name": null, "code": "PL", "type": "LEAGUE" } ]
        """,
        //  no code
        """
        [ { "id": 2021, "name": "Premier League", "type": "LEAGUE" } ]
        """,
        // null code
        """
        [ { "id": 2021, "name": "Premier League", "code": null, "type": "LEAGUE" } ]
        """,
        //np type
        """
        [ { "id": 2021, "name": "Premier League", "code": "PL" } ]
        """,
        // null type
        """
        [ { "id": 2021, "name": "Premier League", "code": "PL", "type": null  } ]
        """,
    })//endregion
    void shouldDiscardCompetitionWithMissingCrucialData(String brokenJsonResponse) throws Exception {

        JsonNode mockJsonNode = objectMapper.readTree(brokenJsonResponse);
        when(footballApiClient.fetchRawCompetitions()).thenReturn(mockJsonNode);

        apiSyncManager.fetchCompetitions();
        verify(competitionRepository, never()).save(any(Competition.class));
    }
}

package com.codeoftheweb.controllers;


import com.codeoftheweb.models.*;
import com.codeoftheweb.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GamePlayerRepository gamePlayerRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ShipRepository shipRepository;
    @Autowired
    private SalvoRepository salvoRepository;
    @Autowired
    private ScoreRepository scoreRepository;

    @RequestMapping("/games")
    public Map<String, Object> getUser(Authentication auth) {
        Map<String, Object> dto = new LinkedHashMap<>();

        if (!isGuest(auth)) {
            dto.put("player", playerRepository.findByUserName(auth.getName()).makePlayerDTO());

        } else {
            dto.put("player", "Guest");
        }
        dto.put("games", getAllGames());

        return dto;
    }

    @RequestMapping("/game_view/{gamePlayerId}")
    public ResponseEntity<Map<String, Object>> findGamePlayer(Authentication auth, @PathVariable long gamePlayerId) {

        GamePlayer gp = gamePlayerRepository.findById(gamePlayerId).get();

        if (auth.getName() == gp.getPlayer().getUserName()) {
            return ResponseEntity.ok().body(makeGameViewDTO(auth, gp));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @RequestMapping("/leaderBoard")
    public List<Map<String, Object>> getAllPlayers() {
        return playerRepository.findAll()
                .stream()
                .map(player -> makeLeaderBoardDTO(player))
                .collect(Collectors.toList());

    }

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Object> register(@RequestParam String email, @RequestParam String password) {

        if (email.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
        }

        if (playerRepository.findByUserName(email) != null) {
            return new ResponseEntity<>("Name already in use", HttpStatus.FORBIDDEN);
        }

        playerRepository.save(new Player(email, passwordEncoder.encode(password)));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createGame(Authentication auth) {

        Player player = null;

        if (!isGuest(auth)) {
            player = playerRepository.findByUserName(auth.getName());
        }
        if (player == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Date d = new Date();
        Game g = gameRepository.save(new Game(d));
        GamePlayer gp = gamePlayerRepository.save(new GamePlayer(d, player, g));
        return new ResponseEntity<>(makeMap("gpid", gp.getId()), HttpStatus.CREATED);
    }

    @RequestMapping(path = "/game/{gameId}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> joinGame(Authentication auth, @PathVariable long gameId) {

        if (isGuest(auth)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Game game = gameRepository.findById(gameId).get();

        if (!gameRepository.findById(gameId).isPresent()) {
            return new ResponseEntity<>(makeMap("error", "No such game"), HttpStatus.FORBIDDEN);
        }

        if (game.getGamePlayers().stream().count() > 1) {
            return new ResponseEntity<>(makeMap("error", "Game is full"), HttpStatus.FORBIDDEN);
        }

        Date d = new Date();
        Player player = playerRepository.findByUserName(auth.getName());
        GamePlayer gp = gamePlayerRepository.save(new GamePlayer(d, player, game));
        return new ResponseEntity<>(makeMap("gpid", gp.getId()), HttpStatus.CREATED);
    }

    @RequestMapping(path = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addShips
            (@PathVariable Long gamePlayerId, @RequestBody Set<Ship> ships, Authentication auth) {

        GamePlayer gp = gamePlayerRepository.findById(gamePlayerId).get();
        Player player = playerRepository.findByUserName(auth.getName());

        if (isGuest(auth)) {
            return new ResponseEntity<>
                    (makeMap("error", "There is no current user logged in"), HttpStatus.UNAUTHORIZED);
        }

        if (!gamePlayerRepository.findById(gamePlayerId).isPresent()) {
            return new ResponseEntity<>
                    (makeMap("error", "There is no game player with the given ID"), HttpStatus.UNAUTHORIZED);
        }

        if (gp.getPlayer().getId() != player.getId()) {
            return new ResponseEntity<>(makeMap("error", "The current player is not the game player the ID references"),
                    HttpStatus.UNAUTHORIZED);
        }

        if (!gp.getShips().isEmpty()) {
            return new ResponseEntity<>
                    (makeMap("error", "The player already has ships placed"), HttpStatus.FORBIDDEN);
        }

        ships.forEach(ship -> ship.setGamePlayer(gp));
        shipRepository.saveAll(ships);
        return new ResponseEntity<>(makeMap("OK", "Ships created"), HttpStatus.CREATED);

    }

    @RequestMapping(path = "/games/players/{gamePlayerId}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addSalvoes
            (@PathVariable Long gamePlayerId, @RequestBody Salvo salvo, Authentication auth) {

        GamePlayer gp = gamePlayerRepository.findById(gamePlayerId).get();
        Player player = playerRepository.findByUserName(auth.getName());

        if (isGuest(auth)) {
            return new ResponseEntity<>
                    (makeMap("error", "There is no current user logged in"), HttpStatus.UNAUTHORIZED);
        }

        if (!gamePlayerRepository.findById(gamePlayerId).isPresent()) {
            return new ResponseEntity<>
                    (makeMap("error", "There is no game player with the given ID"), HttpStatus.UNAUTHORIZED);
        }

        if (gp.getPlayer().getId() != player.getId()) {
            return new ResponseEntity<>(makeMap("error", "The current player is not the game player the ID references"),
                    HttpStatus.UNAUTHORIZED);
        }


        Set<Salvo> salvoes = gp.getSalvoes();
        for (Salvo salvoX : salvoes) {
            if (salvo.getTurn() == salvoX.getTurn() || gp.getSalvoes().size() > getOpponent(gp).getSalvoes().size()) {
                return new ResponseEntity<>
                        (makeMap("error", "The player already has submitted a salvo for the turn listed"),
                                HttpStatus.FORBIDDEN);
            }
        }

        salvoRepository.save(new Salvo(salvoes.size() + 1, gp, salvo.getSalvoLocations()));
        return new ResponseEntity<>(makeMap("OK", "Salvoes save"), HttpStatus.CREATED);

    }

    private boolean isGuest(Authentication auth) {
        return auth == null || auth instanceof AnonymousAuthenticationToken;
    }

    private GamePlayer getOpponent(GamePlayer gamePlayer) {
        GamePlayer opponent = null;
        for (GamePlayer gp : gamePlayer.getGame().getGamePlayers()) {
            if (gp.getId() != gamePlayer.getId()) {
                opponent = gp;
            }
        }
        return opponent;
    }

    private Map<String, Object> makeGameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", game.getId());
        dto.put("created", game.getCreationDate());
        dto.put("gamePlayers", getAllGamePlayers(game.getGamePlayers()));
        dto.put("scores", getAllScores(game.getScores()));
        return dto;
    }

    private Map<String, Object> makeGamePlayersDTO(GamePlayer gp) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", gp.getId());
        dto.put("player", gp.getPlayer().makePlayerDTO());
        return dto;
    }

    private Map<String, Object> makeShipDTO(Ship ship) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("type", ship.getType());
        dto.put("locations", ship.getLocations());
        return dto;
    }

    private Map<String, Object> makeSalvoDTO(Salvo salvo) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turn", salvo.getTurn());
        dto.put("player", salvo.getGamePlayer().getPlayer().getId());
        dto.put("locations", salvo.getSalvoLocations());
        return dto;
    }

    private Map<String, Object> makeScoreDTO(Score score) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("player", score.getPlayer().getId());
        dto.put("score", score.getScore());
        dto.put("finishDate", score.getFinishDate());
        return dto;
    }

    private Map<String, Object> makeLeaderBoardDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        Map<String, Object> score = new LinkedHashMap<String, Object>();
        dto.put("id", player.getId());
        dto.put("email", player.getUserName());
        dto.put("score", score);
        score.put("total", player.getTotalScore());
        score.put("won", player.getWinScore());
        score.put("lost", player.getLostScore());
        score.put("tied", player.getTiedScore());
        return dto;
    }

    private Map<String, Object> makeMap(String gp, Object id) {
        Map<String, Object> map = new HashMap<>();
        map.put(gp, id);
        return map;
    }

    private Map<String, Object> makeGameViewDTO(Authentication auth, GamePlayer gp) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", gp.getGame().getId());
        dto.put("gameState", getGameState(gp));
        dto.put("created", gp.getGame().getCreationDate());
        dto.put("gamePlayers", getAllGamePlayers(gp.getGame().getGamePlayers()));
        dto.put("ships", getAllShips(gp.getShips()));
        dto.put("salvoes", getAllSalvoes(gp.getGame().getGamePlayers()));
        dto.put("hits", getHitsDTO(gp));
        return dto;
    }

    private Map<String, Object> getHitsDTO(GamePlayer gp) {
        Map<String, Object> dto = new LinkedHashMap<>();

        GamePlayer opponent = getOpponent(gp);

        if (opponent != null) {
            dto.put("self", getAllHits(getOpponent(gp)));
            dto.put("opponent", getAllHits(gp));
        } else {
            dto.put("self", new ArrayList<>());
            dto.put("opponent", new ArrayList<>());
        }

        return dto;
    }

    private Map<String, Object> getDamageDTO(int carrierHIT, int carrierDMG, int battleshipHIT, int battleshipDMG,
                                             int submarineHIT, int submarineDMG, int destroyerHIT, int destroyerDMG,
                                             int patrolboatHIT, int patrolboatDMG) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("carrierHits", carrierHIT);
        dto.put("battleshipHits", battleshipHIT);
        dto.put("submarineHits", submarineHIT);
        dto.put("destroyerHits", destroyerHIT);
        dto.put("patrolboatHits", patrolboatHIT);
        dto.put("carrier", carrierDMG);
        dto.put("battleship", battleshipDMG);
        dto.put("submarine", submarineDMG);
        dto.put("destroyer", destroyerDMG);
        dto.put("patrolboat", patrolboatDMG);
        return dto;
    }

    private List<Map<String, Object>> getAllGames() {
        return gameRepository.findAll()
                             .stream()
                             .map(game -> makeGameDTO(game))
                             .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getAllGamePlayers(Set<GamePlayer> gps) {
        return gps.stream()
                  .map(gamePlayer -> makeGamePlayersDTO(gamePlayer))
                  .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getAllShips(Set<Ship> ships) {
        return ships.stream()
                    .map(ship -> makeShipDTO(ship))
                    .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getAllSalvoes(Set<GamePlayer> gps) {
        return gps.stream()
                          .flatMap(gamePlayer -> getTurns(gamePlayer.getSalvoes()).stream())
                          .map(salvo -> makeSalvoDTO(salvo))
                          .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getAllScores(Set<Score> scores) {
        return scores.stream()
                .map(score -> makeScoreDTO(score))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getAllHits(GamePlayer gp) {
        List<Map<String, Object>> hitsList = new ArrayList<>();

        int carrierDMG = 0, battleshipDMG = 0, submarineDMG = 0, destroyerDMG = 0, patrolboatDMG = 0;
        List <Salvo> salvoes = getTurns(gp.getSalvoes());
        for (Salvo salvo : salvoes) {
            int carrierHIT = 0, battleshipHIT = 0, submarineHIT = 0, destroyerHIT = 0, patrolboatHIT = 0;
            List<String> hitLocations = new ArrayList<>();
            for (Ship ship : getOpponent(gp).getShips()) {
                List<String> hits = new ArrayList<>(salvo.getSalvoLocations());
                hits.retainAll(ship.getLocations());
                int shots = hits.size();
                if (shots != 0) {
                    hitLocations.addAll(hits);
                    switch (ship.getType()) {
                        case "carrier":
                            carrierHIT += shots;
                            carrierDMG += shots;
                            break;
                        case "battleship":
                            battleshipHIT += shots;
                            battleshipDMG += shots;
                            break;
                        case "submarine":
                            submarineHIT += shots;
                            submarineDMG += shots;
                            break;
                        case "destroyer":
                            destroyerHIT += shots;
                            destroyerDMG += shots;
                            break;
                        case "patrolboat":
                            patrolboatHIT += shots;
                            patrolboatDMG += shots;
                            break;
                    }
                }
            }
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("turn", salvo.getTurn());
            dto.put("hitLocations", hitLocations);
            dto.put("damages", getDamageDTO(carrierHIT, carrierDMG, battleshipHIT, battleshipDMG,
                    submarineHIT, submarineDMG, destroyerHIT, destroyerDMG, patrolboatHIT, patrolboatDMG));
            dto.put("missed", Math.max(0, salvo.getSalvoLocations().size() - hitLocations.size()));
            hitsList.add(dto);
        }
        return hitsList;
    }

    private List<Salvo> getTurns(Set<Salvo> salvoes){
        return salvoes.stream()
                .sorted(Comparator.comparing(Salvo::getTurn))
                .collect(Collectors.toList());
    }

    private String getGameState(GamePlayer gp) {

        String placeShips = "PLACESHIPS";
        String waiting = "WAITINGFOROPP";
        String wait = "WAIT";
        String play = "PLAY";
        String won = "WON";
        String lost = "LOST";
        String tie = "TIE";

        Date date = new Date();

        if (gp.getShips().size() == 0) {
            return placeShips;
        }

        if (getOpponent(gp) == null) {
            return waiting;
        }

        if (getOpponent(gp).getShips().isEmpty()) {
            return wait;
        }

        int self = getSunks(gp);
        int oppo = getSunks(getOpponent(gp));

        if (gp.getSalvoes().size() <= getOpponent(gp).getSalvoes().size() && self != 17 && oppo != 17) {
            return play;
        }

        if (gp.getSalvoes().size() > getOpponent(gp).getSalvoes().size()) {
            return wait;
        }

        if (self == 17 && oppo == 17) {
            if (gp.getGame().getScores().size() < 2) {
                scoreRepository.save(new Score(gp.getGame(), gp.getPlayer(), 0.5, date));
            }
            return tie;
        }

        if (self == 17 && oppo < 17) {
            if (gp.getGame().getScores().size() < 2) {
                scoreRepository.save(new Score(gp.getGame(), gp.getPlayer(), 0, date));
            }
            return lost;
        }

        if (oppo == 17 && self < 17) {
            if (gp.getGame().getScores().size() < 2) {
                scoreRepository.save(new Score(gp.getGame(), gp.getPlayer(), 1, date));
            }
            return won;
        }

        return wait;
    }

    private int getSunks(GamePlayer gp) {
        GamePlayer opp = getOpponent(gp);
        List<String> ships = new ArrayList<>();
        List<String> salvoes = new ArrayList<>();
        for (Ship ship : gp.getShips()) {
            ships.addAll(ship.getLocations());
        }
        for (Salvo salvo : opp.getSalvoes()) {
            salvoes.addAll(salvo.getSalvoLocations());
        }
        ships.retainAll(salvoes);
        return ships.size();
    }
}
































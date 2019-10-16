package com.codeoftheweb.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")

    private long id;
    private String userName;
    private String password;

    //In this method I create a One to many relationship between Player and GamePlayer
    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    private Set<GamePlayer> gamePlayers;

    //In this method I create a One to many relationship between Player and Score
    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    private Set<Score> scores;

    public Player() {
    }

    //Constructor
    public Player(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    //Getters
    public long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public Set<GamePlayer> getGamePlayers() {

        return gamePlayers;
    }

    public Set<Score> getScores() {

        return scores;
    }

    //Setters
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    //Methods for TotalScore - WIN - LOST - TIED
    public Double getTotalScore() {
        return this.getWinScore() * 1.0D + this.getTiedScore() * 0.5D + this.getLostScore() * 0D;
    }

    public long getWinScore() {
        return this.getScores().stream().filter(score -> score.getScore() == 1.0D).count();
    }

    public long getLostScore() {
        return this.getScores().stream().filter(score -> score.getScore() == 0.0D).count();
    }

    public long getTiedScore() {
        return this.getScores().stream().filter(score -> score.getScore() == 0.5D).count();
    }


    //DTO for Player where I have the ID and Username for each player
    public Map<String, Object> makePlayerDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", this.getId());
        dto.put("email", this.getUserName());
        return dto;
    }

}



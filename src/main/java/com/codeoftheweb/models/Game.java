package com.codeoftheweb.models;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;


@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")

    private long id;
    private Date creationDate;

    //In this method I create a One to many relationship between Game and GamePlayer
    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
    private Set<GamePlayer> gamePlayers;

    //In this method I create a One to many relationship between Game and Score
    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
    private Set<Score> scores;

    public Game() {
    }

    //Constructor
    public Game(Date creationDate) {

        this.creationDate = creationDate;
    }

    //Getters
    public long getId() {

        return id;
    }

    public Date getCreationDate() {

        return creationDate;
    }

    public Set<GamePlayer> getGamePlayers() {

        return gamePlayers;
    }

    public Set<Score> getScores() {
        return scores;
    }


}


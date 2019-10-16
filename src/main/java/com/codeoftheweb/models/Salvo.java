package com.codeoftheweb.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

@Entity
public class Salvo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")

    private long id;
    private int turn;

    //In this method I create a Many to one relationship between Salvo and GamePlayer
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;

    //Method for salvoLocations
    @ElementCollection
    @Column(name = "salvoLocations")
    private Set<String> salvoLocations = new LinkedHashSet<>();

    public Salvo() { }

    //Constructor
    public Salvo(int turn, GamePlayer gamePlayer, Set<String> salvoLocations) {
        this.turn = turn;
        this.gamePlayer = gamePlayer;
        this.salvoLocations = salvoLocations;
    }

    //Getters
    public long getId() {
        return id;
    }

    public int getTurn() {
        return turn;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public Set<String> getSalvoLocations() {
        return salvoLocations;
    }

}


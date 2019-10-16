package com.codeoftheweb.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

@Entity
public class Ship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")

    private long id;
    private String type;

    //In this method I create a Many to one relationship between Ship and GamePlayer
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;

    //Method for shipLocations
    @ElementCollection
    @Column(name = "locations")
    private Set<String> locations = new LinkedHashSet<>();

    public Ship() {
    }

    //Constructor
    public Ship(GamePlayer gamePlayer, String type, Set<String> location) {
        this.gamePlayer = gamePlayer;
        this.type = type;
        this.locations = location;
    }

    //Getters
    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public Set<String> getLocations() {
        return locations;
    }

    //Setters
    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public void setShipType(String shipType) {
        this.type = shipType;
    }

    public void setLocation(Set<String> location) {
        this.locations = location;
    }
}



package com.codeoftheweb.models;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class GamePlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")

    private long id;
    private Date joinDate;

    //In this method I create a Many to one relationship between GamePlayer and Player
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "Player_id")
    private Player player;

    //In this method I create a Many to one relationship between GamePlayer and Game
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "Game_id")
    private Game game;

    //In this method I create a One to many relationship between GamePlayer and Ship
    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER)
    private Set<Ship> ships = new LinkedHashSet<>();

    //In this method I create a One to many relationship between GamePlayer and Salvo
    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER)
    private Set<Salvo> salvoes = new LinkedHashSet<>();

    public GamePlayer() {
    }

    //Constructor
    public GamePlayer(Date joinDate, Player player, Game game) {
        this.player = player;
        this.game = game;
        this.joinDate = joinDate;
    }

    //Getters
    public long getId() {
        return id;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public Player getPlayer() {
        return player;
    }

    public Game getGame() {
        return game;
    }

    public Set<Ship> getShips() {
        return ships;
    }

    public Set<Salvo> getSalvoes() {
        return salvoes;
    }


}






























































































package com.codeoftheweb.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")

    private long id;
    private double score;
    private Date finishDate;

    //In this method I create a Many to one relationship between Score and Player
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "Player_id")
    private Player player;

    //In this method I create a Many to one relationship between Score and Game
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "Game_id")
    private Game game;

    public Score() {
    }

    //Constructor
    public Score(Game game, Player player, double score, Date finishDate) {
        this.game = game;
        this.player = player;
        this.score = score;
        this.finishDate = finishDate;
    }

    //Getters
    public long getId() {
        return id;
    }

    public double getScore() {
        return score;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public Player getPlayer() {
        return player;
    }

    public Game getGame() {
        return game;
    }

    //Setters
    public void setScore(double score) {
        this.score = score;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
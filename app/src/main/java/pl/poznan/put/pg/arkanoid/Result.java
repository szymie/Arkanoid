package pl.poznan.put.pg.arkanoid;

import java.io.Serializable;

public class Result implements Serializable, Comparable {

    private String nick;
    private int points;
    private int lives;

    public Result(String nick, int points, int lives) {
        this.nick = nick;
        this.points = points;
        this.lives = lives;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    @Override
    public String toString() {
        return nick + " | points: " + points + " lives: " + lives;
    }

    @Override
    public int compareTo(Object o) {
        Result result = (Result) o;
        return result.points - points;
    }
}

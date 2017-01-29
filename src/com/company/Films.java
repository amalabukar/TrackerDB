package com.company;

import java.util.ArrayList;

/**
 * Created by amalabukar on 1/20/17.
 */
public class Films extends ArrayList<Films> {
    int filmId;
    int userId;
    String title;
    String director;
    boolean seen;
  String year;
    String genre;

    public void setFilmId(int filmId) {
        this.filmId = filmId;
    }

    public Films() {
    }

    public Films(int filmId, int userId, String title, String director, String year, String genre,boolean seen) {
        this.userId = userId;
        this.title = title;
        this.director = director;
        this.seen = seen;
        this.year = year;
        this.genre = genre;
        this.filmId = filmId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getFilmId() {
        return genre;

    }

}
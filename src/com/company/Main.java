package com.company;

import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) throws SQLException {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);

        Spark.init();

        Spark.get(
                "/",
                ((request, response) -> {
                    HashMap movies = new HashMap();
                    Session session = request.session();
                    String userName = session.attribute("loginName");
                    String userPassword = session.attribute("loginPassword");
                    User user = selectUser(conn, userName);
                    if (user == null) {
                        return new ModelAndView(movies, "login.html");
                    } else {
                        ArrayList<Films> film = selectFilms(conn, user.id);

                        movies.put("loginName", userName);
                        movies.put("userPassword", userPassword);
                        movies.put("films", film);

                        return new ModelAndView(movies, "home.html");
                    }
                }),
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/login",
                ((request, response) -> {
                    Session session = request.session();
                    String enterName = request.queryParams("loginName");
                    String enterPassword = request.queryParams("loginPassword");
                    String userId = request.queryParams("userId");
                    User user = selectUser(conn, enterName);

                    if (enterName == null || enterPassword == null) {
                        throw new Exception("Enter name and password");
                    }

                    if (user == null) {
                        insertUser(conn, enterName, enterPassword);
                    } else if (!user.password.equals(enterPassword)) {
                        throw new Exception("Enter valid password");
                    }
                    session.attribute("loginName", enterName);
                    session.attribute("loginPassword", enterPassword);
                    session.attribute("userId", userId);
                    response.redirect("/");
                    return "";

                })
        );

        Spark.post(
                "/logout",
                ((request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";

                })
        );
        Spark.post(
                "/create-film-input",
                ((request, response) -> {
                    Session session = request.session();
                    String enterName = session.attribute("loginName");
                    User user = selectUser(conn, enterName);
                    if (user == null) {
                        throw new Exception("login please ");

                    }
                    int userId = user.id;
                    String title = request.queryParams("enterTitle");
                    String director = request.queryParams("enterDirector");

                    String genre = request.queryParams("enterGenre");
                    String year = request.queryParams("enterYear");

                    String seenString = request.queryParams("enterSeen");
                    if (title == null) {
                        throw new Exception("Enter a title");
                    }
                    boolean seen = Boolean.parseBoolean(seenString);
                    insertFilm(conn, userId, title, director, year, genre, seen);


                    response.redirect("/");
                    return " ";

                })
        );

        Spark.get(
                "/edit-film-input",
                ((request, response) -> {
                    HashMap movies = new HashMap();
                    String m = request.queryParams("filmId");
                    int filmId = Integer.parseInt(m);
                    Films film = selectFilms(conn, filmId);
                    movies.put("film", film);
                    return new ModelAndView(movies, "edit.html");
                }
                ));

        Spark.post(
                "/edit",
                ((request, response) -> {
                    Session session = request.session();
                    String enterName = session.attribute("loginName");
                    int id = session.attribute("filmId");
                    User user = selectUser(conn, enterName);
                    if (user == null) {
                        throw new Exception("Please log in");
                    }
                    String title = request.queryParams("enterTitle");
                    String director = request.queryParams("enterDirector");
                    String year = request.queryParams("enterYear");
                    String genre = request.queryParams("enterGenre");
                    String seenString = request.queryParams("enterSeen");
                    if (title == null) {
                        throw new Exception("Enter a title");
                    }
                    boolean seen = Boolean.parseBoolean(seenString);
                    editFilmInput(conn, id, title, director, year, genre, seen);
                    response.redirect("/");
                    return "";

                })
        );

        Spark.post(
                "/delete-film",
                ((request, response) -> {
                    String filmIdString = request.queryParams("deleteFilmId");
                    int filmId = Integer.parseInt(filmIdString);
                    deleteFilms(conn, filmId);
                    response.redirect("/");
                    return "";
                })
        );
    }

    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, name VARCHAR, password VARCHAR)");
        stmt.execute("CREATE TABLE IF NOT EXISTS film (film_id IDENTITY, user_id INT, title VARCHAR, director VARCHAR, year VARCHAR, genre  VARCHAR, seen BOOLEAN,)");

    }

    public static void insertUser(Connection conn, String name, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, password);
        stmt.execute();
    }


    public static User selectUser(Connection conn, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
        stmt.setString(1, name);
        ResultSet queryOut = stmt.executeQuery();
        if (queryOut.next()) {
            int id = queryOut.getInt("id");
            String password = queryOut.getString("password");
            return new User(id, name, password);
        }
        return null;
    }


    public static ArrayList<User> selectUsers(Connection conn) throws SQLException {
        ArrayList<User> users = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users");
        ResultSet queryOut = stmt.executeQuery();
        while (queryOut.next()) {
            int id = queryOut.getInt("users.id");
            String userName = queryOut.getString("users.name");
            String userPassword = queryOut.getString("users.password");
            User user = new User(id, userName, userPassword);
            users.add(user);
        }
        return users;
    }

    public static void insertFilm(Connection conn, int userId, String title, String director, String year, String genre, boolean seen) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO film VALUES (NULL, ?, ?, ?, ?, ?,?)");
        stmt.setInt(1, userId);
        stmt.setString(2, title);
        stmt.setString(3, director);
        stmt.setString(4, year);
        stmt.setString(5, genre);
        stmt.setBoolean(6, seen);

    }

    public static Films selectFilms(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM film INNER JOIN users ON film.userId = user.id WHERE film.id = ?");
        stmt.setInt(1, id);
        ResultSet queryOut = stmt.executeQuery();

        if (queryOut.next()) {
            int filmId = queryOut.getInt("film.id");
            int userId = queryOut.getInt("user.id");
            String title = queryOut.getString("title");
            String director = queryOut.getString("director");
            String year = queryOut.getString("year");
            String genre = queryOut.getString("genre");
            Boolean seen = queryOut.getBoolean("seen");
            return new Films(filmId, userId, title, director, year, genre, seen);
        }
        return null;

    }

    public static ArrayList<Films> selectFilm(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM film INNER JOIN users ON film.userId = users.id WHERE film.id = ?");
        stmt.setInt(1, id);
        ResultSet queryOut = stmt.executeQuery();
        ArrayList<Films> selectFilms = new ArrayList<>();
        while (queryOut.next()) {
            int filmId = queryOut.getInt("film.id");
            int userId = queryOut.getInt("user.id");
            String title = queryOut.getString("film.title");
            String director = queryOut.getString("film.director");
            String year = queryOut.getString("film.year");
            String genre = queryOut.getString("film.genre");
            Boolean seen = queryOut.getBoolean("film.seen");
            Films film = new Films(filmId, userId, title, director, year, genre, seen);
            selectFilms.add(film);
        }
        return selectFilms;

    }

    public static void editFilmInput(Connection conn, int id, String title, String director, String year, String genre, boolean seen) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE films SET title =? SET director =? SET year? SET genre? SET seen=? ");
        stmt.setInt(1, id);
        stmt.setString(2, title);
        stmt.setString(3, director);
        stmt.setString(4, year);
        stmt.setString(5, genre);
        stmt.setBoolean(6, seen);
        stmt.execute();
    }

    public static void deleteFilms(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM films WHERE id = ?");
        stmt.setInt(1, id);
        System.out.println(id);
        stmt.execute();
    }


}



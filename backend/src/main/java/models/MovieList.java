package models;

import java.util.ArrayList;

public class MovieList{
    int id;
    String title;
    int year;
    String director;
    ArrayList<String> genre = new ArrayList<>();
    ArrayList<String> stars = new ArrayList<>();
    double rating;
}

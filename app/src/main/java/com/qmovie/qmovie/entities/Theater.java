package com.qmovie.qmovie.entities;

import java.util.List;

public class Theater
{
    private String name;
    private List<String> shows;

    public Theater(List<String> shows, String name)
    {
        this.shows = shows;
        this.name = name;
    }

    public Theater()
    {

    }

    public List<String> getShows()
    {
        return shows;
    }

    public void setShows(List<String> shows)
    {
        this.shows = shows;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}

package com.qmovie.qmovie.entities;

import java.util.List;

public class showsPerTheater
{
    private String       theaterName;
    private List<Long> showDates;

    public showsPerTheater(List<Long> showDates, String theaterName)
    {
        this.showDates = showDates;
        this.theaterName = theaterName;
    }

    public showsPerTheater()
    {

    }

    public List<Long> getShowDates()
    {
        return showDates;
    }

    public void setShowDates(List<Long> showDates)
    {
        this.showDates = showDates;
    }

    public String getTheaterName()
    {
        return theaterName;
    }

    public void setTheaterName(String theaterName)
    {
        this.theaterName = theaterName;
    }
}

package com.qmovie.qmovie.entities;

import java.util.List;

public class showsPerDay
{
    private String dayName;
    private List<Long> hours;

    public showsPerDay(String dayName, List<Long> hours)
    {
        this.dayName = dayName;
        this.hours = hours;
    }

    public showsPerDay()
    {
    }

    public String getDayName()
    {
        return dayName;
    }

    public void setDayName(String dayName)
    {
        this.dayName = dayName;
    }

    public List<Long> getHours()
    {
        return hours;
    }

    public void setHours(List<Long> hours)
    {
        this.hours = hours;
    }
}

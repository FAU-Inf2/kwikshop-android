package de.cs.fau.mad.quickshop.android;

import java.util.Calendar;


public class CalendarEventDate{

    private long calendarEventId = -1;

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;

    public CalendarEventDate(){
        this.year = 0;
        this.month = 0;
        this.day = 0;
        this.hour = 0;
        this.minute = 0;
    }

    public CalendarEventDate(int year, int month, int day, int hour, int minute){
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
    }

    public CalendarEventDate(CalendarEventDate eventDate){
        this.year = eventDate.getYear();
        this.month = eventDate.getMonth();
        this.day = eventDate.getDay();
        this.hour = eventDate.getHour();
        this.minute = eventDate.getMinute();
    }


    //setter
    public void setCalendarEventId(long id){
        calendarEventId = id;
    }

    public void setYear(int year){
        this.year = year;
    }

    public void setMonth(int month){
        this.month = month;
    }

    public void setDay(int day){
        this.day = day;
    }

    public void setHour(int hour){
        this.hour = hour;
    }

    public void setMinute(int minute){
        this.minute = minute;
    }


    //getter
    public long getCalendarEventId(){
        return calendarEventId;
    }

    public int getYear(){
        return year;
    }

    public int getMonth(){
        return month;
    }

    public int getDay(){
        return day;
    }

    public int getHour(){
        return hour;
    }

    public int getMinute(){
        return minute;
    }



    //compares the id of a given CalendarEventDate
    //if id has default value current time is set, else time from eventDate
    public void inittialize(CalendarEventDate eventDate){
            if(eventDate.getCalendarEventId() == -1) {
                final Calendar c = Calendar.getInstance();
                setYear(c.get(Calendar.YEAR));
                setMonth(c.get(Calendar.MONTH));
                setDay(c.get(Calendar.DAY_OF_MONTH));
                setHour(c.get(Calendar.HOUR_OF_DAY));
                setMinute(c.get(Calendar.MINUTE));
            }else{
                setYear(eventDate.getYear());
                setMonth(eventDate.getMonth());
                setDay(eventDate.getDay());
                setHour(eventDate.getHour());
                setMinute(eventDate.getMinute());
            }


    }



}


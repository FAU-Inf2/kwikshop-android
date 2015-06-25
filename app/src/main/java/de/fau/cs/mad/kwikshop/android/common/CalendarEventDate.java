package de.fau.cs.mad.kwikshop.android.common;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Calendar;

@DatabaseTable(tableName = "calendarEvent")
public class CalendarEventDate {


    @DatabaseField(generatedId = true)
    private long calendarEventId;

    @DatabaseField
    private long androidCalendarId = -1;

    @DatabaseField
    private int year;

    @DatabaseField
    private int month;

    @DatabaseField
    private int day;

    @DatabaseField
    private int hour;

    @DatabaseField
    private int minute;

    public CalendarEventDate(){
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
        this.calendarEventId = eventDate.getCalendarEventId();
    }


    //setter

    public void setAndroidCalendarId(long value) {
        this.androidCalendarId = value;
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


    public long getAndroidCalendarId() {
        return this.androidCalendarId;
    }



    public static CalendarEventDate now() {

        final Calendar c = Calendar.getInstance();


        CalendarEventDate date = new CalendarEventDate();

        date.setYear(c.get(Calendar.YEAR));
        date.setMonth(c.get(Calendar.MONTH));
        date.setDay(c.get(Calendar.DAY_OF_MONTH));
        date.setHour(c.get(Calendar.HOUR_OF_DAY));
        date.setMinute(c.get(Calendar.MINUTE));

        return date;
    }

}


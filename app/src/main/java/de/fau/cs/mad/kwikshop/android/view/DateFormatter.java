package de.fau.cs.mad.kwikshop.android.view;

import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.joda.time.Period;
import org.joda.time.ReadableDuration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;

public class DateFormatter {

    private final ResourceProvider resourceProvider;


    public DateFormatter(ResourceProvider resourceProvider) {

        if(resourceProvider == null) {
            throw new IllegalArgumentException("'resourceProvider' must not be null");
        }

        this.resourceProvider = resourceProvider;
    }


    public String formatDate(Date date) {

        DateTime now= DateTime.now();
        DateTime toFormat = new DateTime(date);


        //same day
        if(now.getYear() == toFormat.getYear() && now.getDayOfYear() == toFormat.getDayOfYear()) {

            int passedHours = Hours.hoursBetween(toFormat, now).getHours();

            if(passedHours == 0) {
                int passedMinutes = Minutes.minutesBetween(toFormat, now).getMinutes();

                //within 5 minutes, just display "just now"
                if(passedMinutes <= 5) {
                    return resourceProvider.getString(R.string.dateDescription_just_now);
                //within same hour display something like "10 minutes ago"
                } else {
                    String formatString = resourceProvider.getString(R.string.dateDescription_minutes_ago);
                    return String.format(formatString, passedMinutes);
                }

            } else  {

                //larger periods: display time
                return SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT).format(date);
            }

        //other day
        } else {

            DateFormat format = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);
            return format.format(date);
        }

    }

}

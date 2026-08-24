package com.amitbharat.gallery.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    public static String formatDate(long millis) {
        if (millis <= 0) return "";
        Calendar itemCal = Calendar.getInstance();
        itemCal.setTimeInMillis(millis);

        Calendar todayCal = Calendar.getInstance();
        if (itemCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                itemCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)) {
            return "Today";
        }

        Calendar yesterdayCal = Calendar.getInstance();
        yesterdayCal.add(Calendar.DAY_OF_YEAR, -1);
        if (itemCal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR) &&
                itemCal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR)) {
            return "Yesterday";
        }

        return DATE_FORMAT.format(new Date(millis));
    }

    public static String formatDateTime(long millis) {
        if (millis <= 0) return "";
        return DATE_TIME_FORMAT.format(new Date(millis));
    }

    public static String formatTime(long millis) {
        if (millis <= 0) return "";
        return TIME_FORMAT.format(new Date(millis));
    }
}

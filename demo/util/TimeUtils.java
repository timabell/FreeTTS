/**
 * Copyright 2001-2003 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
import java.util.regex.Pattern;

public class TimeUtils {

    /**
     * Returns a phrase that conveys the exactness
     * of the time.
     *
     * @param hour the hour of the time
     * @param min the minute of the time
     *
     * @return a string phrase
     */
    private static String timeApprox(int hour, int min) {
        int mm;

        mm = min % 5;

        if (mm == 0) {
            return "exactly";
        } else if (mm == 1) {
            return "just after";
        } else if (mm == 2) {
            return "a little after";
        } else {
            return "almost";
        }
    }

    /**
     * Returns a phrase that conveys the minutes in relation
     * to the hour.
     *
     * @param hour the hour of the time
     * @param min the minute of the time
     *
     * @return a string phrase.
     */
    private static String timeMin(int hour, int min) {
        int mm;

        mm = min / 5;
        if ((min % 5) > 2) {
            mm += 1;
        }
        mm = mm * 5;
        if (mm > 55) {
            mm = 0;
        }

        if (mm == 0) {
            return "";
        } else if (mm == 5) {
            return "five past";
        } else if (mm == 10) {
            return "ten past";
        } else if (mm == 15) {
            return "quarter past";
        } else if (mm == 20) {
            return "twenty past";
        } else if (mm == 25) {
            return "twenty-five past";
        } else if (mm == 30) {
            return "half past";
        } else if (mm == 35) {
            return "twenty-five to";
        } else if (mm == 40) {
            return "twenty to";
        } else if (mm == 45) {
            return "quarter to";
        } else if (mm == 50) {
            return "ten to";
        } else if (mm == 55) {
            return "five to";
        } else {
            return "five to";
        }
    }

    /**
     * Returns a phrase that conveys the hour in relation
     * to the hour.
     *
     * @param hour the hour of the time
     * @param min the minute of the time
     *
     * @return a string phrase.
     */
    private static String timeHour(int hour, int min) {
        int hh;

        hh = hour;
        if (min > 32)  { // PBL: fixed from flite_time
            hh += 1;
        }
        if (hh == 24) {
            hh = 0;
        }
        if (hh > 12) {
            hh -= 12;
        }

        if (hh == 0) {
            return "midnight";
        } else if (hh == 1) {
            return "one";
        } else if (hh == 2) {
            return "two";
        } else if (hh == 3) {
            return "three";
        } else if (hh == 4) {
            return "four";
        } else if (hh == 5) {
            return "five";
        } else if (hh == 6) {
            return "six";
        } else if (hh == 7) {
            return "seven";
        } else if (hh == 8) {
            return "eight";
        } else if (hh == 9) {
            return "nine";
        } else if (hh == 10) {
            return "ten";
        } else if (hh == 11) {
            return "eleven";
        } else if (hh == 12) {
            return "twelve";
        } else {
            return "twelve";
        }
    }

    /**
     * Returns a phrase that conveys the time of day.
     *
     * @param hour the hour of the time
     * @param min the minute of the time
     *
     * @return a string phrase
     */
    private static String timeOfDay(int hour, int min) {
        int hh = hour;

        if (min > 58)
            hh++;

        if (hh == 24) {
            return "";
        } else if (hh > 17) {
            return "in the evening";
        } else if (hh > 11) {
            return "in the afternoon";
        } else if ((hh == 0) && (min < 33)) {
            return "";
        } else {
            return "in the morning";
        }
    }

    /**
     * Returns a string that corresponds to the given time.
     *
     * @param time the time in the form HH:MM
     *
     * @return the time in string, null if the given time is not in the
     *   form HH:MM 
     */
    public static String timeToString(String time) {
        String theTime = null;
        if (Pattern.matches("[012][0-9]:[0-5][0-9]", time)) {
            int hour = Integer.parseInt(time.substring(0, 2));
            int min = Integer.parseInt(time.substring(3));

            theTime = timeToString(hour, min);
        }
        return theTime;
    }

    /**
     * Returns a string that corresponds to the given time.
     *
     * @param hour the hour
     * @param min the minutes
     *
     * @return the time in string, null if the given time out of range
     */
    public static String timeToString(int hour, int min) {
        String theTime = "The time is now, " +
            timeApprox(hour, min) + " " +
            timeMin(hour, min) + " " +
            timeHour(hour, min) + ", " +
            timeOfDay(hour, min) + "." ;
        return theTime;
    }
}

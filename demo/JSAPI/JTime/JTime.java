/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import java.util.regex.Pattern;

import javax.speech.Central;
import javax.speech.Engine;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.SynthesizerProperties;

/**
 * Simple program showing how to use the Limited Domain (time)
 * FreeTTS synthesizer.
 */
public class JTime {

    Synthesizer synthesizer;

    /**
     * Construct a default JTime object. It creates the Limited Domain
     * synthesizer, speaks the current time, and asks the user to input
     * a new time in the format HH:MM.
     */
    public JTime() {
	try {
	    // Create a new SynthesizerModeDesc that will match the FreeTTS
	    // Synthesizer.
	    SynthesizerModeDesc desc = new SynthesizerModeDesc
		("Limited domain FreeTTS Speech Synthesizer from Sun Labs",
		 null,
		 null,
		 Boolean.FALSE,         // running?
		 null);                 // voice

	    synthesizer = Central.createSynthesizer(desc);

	    if (synthesizer == null) {
		String message = "Can't find synthesizer.\n" +
		    "Make sure that there is a \"speech.properties\" file " +
		    "at either of these locations: \n";
		message += "user.home: " + 
		    System.getProperty("user.home") + "\n";
		message += "java.home: " +
		    System.getProperty("java.home") + "\n";

		System.err.println(message);
		System.exit(1);
	    }
	    
	    // get it ready to speak
	    synthesizer.allocate();
	    synthesizer.resume();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Starts interactive mode. Reads text
     * from the console and gives it to the synthesizer to speak.
     * terminates on end of file.
     *
     * @param freetts the engine
     */
    public void interactiveMode() {
        try {
            while (true) {
                String text;
                BufferedReader reader = new BufferedReader
		    (new InputStreamReader(System.in));
                System.out.print("Enter time (HH:MM): ");
                System.out.flush();
                text = reader.readLine();
                if ((text == null) || (text.length() == 0)) {
		    System.exit(0);
                } else {
		    timeToSpeech(text);
                }
            }
        } catch (IOException ioe) {
	    ioe.printStackTrace();
        }
    }

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

        if ((mm == 0) || (mm == 4)) {
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

    /**
     * Speaks the given time. Time should be in the exact form
     * HH:MM where HH is the hour 00 to 23, and MM is the minute 00 to
     * 59.
     *
     * @param time the time in the form HH:MM
     *
     * @throws IllegalArgumentException if time is not in the form
     *   HH:MM
     */
    public void timeToSpeech(String time) {
        String theTime = timeToString(time);
        if (theTime != null) {
            synthesizer.speakPlainText(theTime, null);
        } else {
            // throw new IllegalArgumentException("Bad time format");
	    System.out.println("Bad time format. The format should be HH:MM");
        }
    }
    
    /**
     * Speaks the time given the hour and minute.
     *
     * @param hour the hour of the day (0 to 23)
     * @param min the minute of the hour (0 to 59)
     */
    public void timeToSpeech(int hour, int min) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Bad time format: hour");
        }

        if (min < 0 || min > 59) {
            throw new IllegalArgumentException("Bad time format: min");
        }

        String theTime = timeToString(hour, min);

        synthesizer.speakPlainText(theTime, null);
    }

    /**
     * Speaks the given time.  Prints an error message if the time
     * is ill-formed.
     *
     * @param time the time in the form HH:MM
     */
    public void safeTimeToSpeech(String time) {
        try {
            if (time.equals("now")) {
                speakNow();
            } else {
                timeToSpeech(time);
            }
        } catch (IllegalArgumentException iae) {
            System.err.println("Bad time format");
        }
    }

    /**
     * Tells the current time.
     */
    public void speakNow() {
        long now = System.currentTimeMillis();
        Calendar cal = new GregorianCalendar();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        timeToSpeech(hour, min);
    }

    public static void main(String[] argv) {
	try {
	    JTime jtime = new JTime();
	    jtime.speakNow();
	    jtime.interactiveMode();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}

	System.exit(0);
    }
}

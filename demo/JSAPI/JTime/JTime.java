/**
 * Copyright 2003 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import java.util.regex.Pattern;

import javax.speech.Central;
import javax.speech.Engine;
import javax.speech.EngineException;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.SynthesizerProperties;
import com.sun.speech.freetts.util.Utilities;

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
		(null,
		 "time",
		 Locale.US,
		 Boolean.FALSE,         // running?
		 null);                 // voice

	    synthesizer = Central.createSynthesizer(desc);

	    if (synthesizer == null) {
		String message = "Can't find synthesizer.\n" +
		    "Make sure that there is a \"speech.properties\" file " +
		    "at either of these locations: \n";
		message += "user.home    : " + 
		    System.getProperty("user.home") + "\n";
		message += "java.home/lib: " + System.getProperty("java.home")
		    + File.separator + "lib\n";

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
		    break;
                } else {
		    timeToSpeech(text);
                }
            }
        } catch (IOException ioe) {
	    ioe.printStackTrace();
        }
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
        String theTime = TimeUtils.timeToString(time);
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

        String theTime = TimeUtils.timeToString(hour, min);

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

    /**
     * Closes things down
     */
    public void close() {
	try {
	    synthesizer.deallocate();
	} catch (EngineException ee) {
	    System.out.println("Trouble deallocating synthesizer: " + ee);
	}
    }

    public static void main(String[] argv) {
	try {
	    JTime jtime = new JTime();
	    jtime.speakNow();
	    jtime.interactiveMode();
	    jtime.close();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}

	System.exit(0);
    }
}

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
import javax.speech.EngineList;
import javax.speech.EngineException;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.SynthesizerProperties;
import javax.speech.synthesis.Voice;

/**
 * Simple program showing how to use the Limited Domain (time)
 * FreeTTS synthesizer.
 */
public class JTime {

    Synthesizer synthesizer;

    /**
     * Returns a "no synthesizer" message, and asks 
     * the user to check if the "speech.properties" file is
     * at <code>user.home</code> or <code>java.home/lib</code>.
     *
     * @return a no synthesizer message
     */
    static private String noSynthesizerMessage() {
        String message =
            "No synthesizer created.  This may be the result of any\n" +
            "number of problems.  It's typically due to a missing\n" +
            "\"speech.properties\" file that should be at either of\n" +
            "these locations: \n\n";
        message += "user.home    : " + System.getProperty("user.home") + "\n";
        message += "java.home/lib: " + System.getProperty("java.home") +
	    File.separator + "lib\n\n" +
            "Another cause of this problem might be corrupt or missing\n" +
            "voice jar files in the freetts lib directory.  This problem\n" +
            "also sometimes arises when the freetts.jar file is corrupt\n" +
            "or missing.  Sorry about that.  Please check for these\n" +
            "various conditions and then try again.\n";
        return message;
    }

    /**
     * Example of how to list all the known voices for a specific
     * mode using just JSAPI.  FreeTTS maps the domain name to the
     * JSAPI mode name.  The currently supported domains are
     * "general," which means general purpose synthesis for tasks
     * such as reading e-mail, and "time" which means a domain that's
     * only good for speaking the time of day. 
     */
    public static void listAllVoices(String modeName) {
        
        System.out.println();
        System.out.println(
            "All " + modeName + " Mode JSAPI Synthesizers and Voices:");

        /* Create a template that tells JSAPI what kind of speech
         * synthesizer we are interested in.  In this case, we're
         * just looking for a general domain synthesizer for US
         * English.
         */ 
        SynthesizerModeDesc required = new SynthesizerModeDesc(
            null,      // engine name
            modeName,  // mode name
            Locale.US, // locale
            null,      // running
            null);     // voices

        /* Contact the primary entry point for JSAPI, which is
         * the Central class, to discover what synthesizers are
         * available that match the template we defined above.
         */
        EngineList engineList = Central.availableSynthesizers(required);
        for (int i = 0; i < engineList.size(); i++) {
            
            SynthesizerModeDesc desc = (SynthesizerModeDesc) engineList.get(i);
            System.out.println("    " + desc.getEngineName()
                               + " (mode=" + desc.getModeName()
                               + ", locale=" + desc.getLocale() + "):");
            Voice[] voices = desc.getVoices();
            for (int j = 0; j < voices.length; j++) {
                System.out.println("        " + voices[j].getName());
            }
        }
    }
    
    /**
     * Construct a default JTime object. It creates the Limited Domain
     * synthesizer, speaks the current time, and asks the user to input
     * a new time in the format HH:MM.
     */
    public JTime(String voiceName) {
	try {
	    /* Find a synthesizer that has the general domain voice
             * we are looking for.  NOTE:  this uses the Central class
             * of JSAPI to find a Synthesizer.  The Central class
             * expects to find a speech.properties file in user.home
             * or java.home/lib.
             *
             * If your situation doesn't allow you to set up a
             * speech.properties file, you can circumvent the Central
             * class and do a very non-JSAPI thing by talking to
             * FreeTTSEngineCentral directly.  See the WebStartClock
             * demo for an example of how to do this.
             */
	    SynthesizerModeDesc desc = new SynthesizerModeDesc(
                null,          // engine name
                "time",        // mode name
                Locale.US,     // locale
                null,          // running
                null);         // voice

	    synthesizer = Central.createSynthesizer(desc);

            /* Just an informational message to guide users that didn't
             * set up their speech.properties file. 
             */
	    if (synthesizer == null) {
		System.err.println(noSynthesizerMessage());
		System.exit(1);
	    }

	    /* Get the synthesizer ready to speak
             */
	    synthesizer.allocate();
	    synthesizer.resume();

            /* Choose the voice.
             */
            desc = (SynthesizerModeDesc) synthesizer.getEngineModeDesc();
            Voice[] voices = desc.getVoices();
            Voice voice = null;
            for (int i = 0; i < voices.length; i++) {
                if (voices[i].getName().equals(voiceName)) {
                    voice = voices[i];
                    break;
                }
            }
            if (voice == null) {
                System.err.println(
                    "Synthesizer does not have a voice named "
                    + voiceName + ".");
                System.exit(1);
            }
            synthesizer.getSynthesizerProperties().setVoice(voice);
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Starts interactive mode. Reads text from the console and gives
     * it to the synthesizer to speak.  Terminates on end of file.
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

    public static void main(String[] args) {

        /* List all the "time" domain voices, which are voices that
         * are only capable of speaking the time of day.
         */
        listAllVoices("time");

        String voiceName = (args.length > 0)
            ? args[0]
            : "alan";
        
        System.out.println();
        System.out.println("Using voice: " + voiceName);
        
	try {
	    JTime jtime = new JTime(voiceName);
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

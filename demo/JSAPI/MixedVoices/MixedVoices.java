/**
 * Copyright 2003 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
import java.io.File;
import java.util.Locale;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.speech.Central;
import javax.speech.Engine;
import javax.speech.EngineException;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.SynthesizerProperties;
import javax.speech.synthesis.Voice;
import javax.speech.synthesis.Speakable;
import javax.speech.synthesis.SpeakableAdapter;
import javax.speech.synthesis.SpeakableEvent;

/**
 * Sample program that demonstrates how multiple voices
 * and synthesizers can be used.
 */
public class MixedVoices {
    
    private final static void usage() {
	System.out.println("MixedVoices [-showEvents] [-showPropertyChanges]");
    }

    /**
     * Returns a "no synthesizer" message, and asks 
     * the user to check if the "speech.properties" file is
     * at <code>user.home</code> or <code>java.home/lib</code>.
     *
     * @return a no synthesizer message
     */
    static private String noSynthesizerMessage(String synthesizer) {
        String message =
            "Cannot find " + synthesizer + ".\n" +
            "This may be the result of any number of problems.  It's\n" +
            "typically due to a missing \"speech.properties\" file that\n" +
            "should be at either of these locations: \n\n";
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

    public static void main(String[] argv) {
	boolean showEvents = false;
	boolean showPropertyChanges = false;

	for (int i = 0; i < argv.length; i++) {
	    if (argv[i].equals("-showEvents")) {
		showEvents = true;
	    } else if (argv[i].equals("-showPropertyChanges")) {
		showPropertyChanges = true;
	    } else {
		usage();
		System.exit(0);
	    }
	}

	System.out.println(" ** Mixed Voices - JSAPI Demonstration program **");
	/* alan is a limited-domain voice that only knows how talk
         * about the time of day.
         */
	Voice alan = new Voice("alan", 
		Voice.GENDER_DONT_CARE, Voice.AGE_DONT_CARE, null);

	/* kevin in an 8khz general domain diphone voice
         */
	Voice kevin = new Voice("kevin", 
		Voice.GENDER_DONT_CARE, Voice.AGE_DONT_CARE, null);

        /* kevin16 in a 16khz general domain diphone voice
         */
	Voice kevinHQ = new Voice("kevin16", 
		Voice.GENDER_DONT_CARE, Voice.AGE_DONT_CARE, null);

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
	    SynthesizerModeDesc generalDesc = new SynthesizerModeDesc(
		null,          // engine name
                "general",     // mode name
                Locale.US,     // locale
                null,          // running
                null);         // voice
            
	    final Synthesizer synthesizer1 =
                Central.createSynthesizer(generalDesc);

	    if (synthesizer1 == null) {
		System.err.println(
                    noSynthesizerMessage("general domain synthesizer"));
		System.exit(1);
	    }

	    /* Find a synthesizer that has the time domain voice.
             */
	    SynthesizerModeDesc limitedDesc = new SynthesizerModeDesc(
                null,          // engine name
                "time",        // mode name
                Locale.US,     // locale
                null,          // running
                null);         // voice

	    final Synthesizer synthesizer2 =
                Central.createSynthesizer(limitedDesc);

	    if (synthesizer2 == null) {
		System.err.println(
                    noSynthesizerMessage("time domain synthesizer"));
		System.exit(1);
	    }

	    System.out.print("  Allocating synthesizers...");
	    synthesizer1.allocate();
	    synthesizer2.allocate();

	    /* get general domain synthesizer ready to speak
             */
	    System.out.print("Loading voices...");
	    synthesizer1.getSynthesizerProperties().setVoice(kevinHQ);
	    synthesizer1.getSynthesizerProperties().setVoice(kevin);

	    if (showPropertyChanges) {
		synthesizer1.getSynthesizerProperties().addPropertyChangeListener(
                    new PropertyChangeListener() {
                        public void propertyChange(
                            PropertyChangeEvent pce) {
                            if (pce.getNewValue() instanceof Voice) {
				String newVoice = 
				    ((Voice) pce.getNewValue()).getName();
				System.out.println(
                                    "  PCE Voice changed to " + newVoice);
			    } else {
				System.out.println(
                                    "  PCE " + pce.getPropertyName()
                                    + " changed from " 
				    + pce.getOldValue() + " to " +
				    pce.getNewValue() + ".");
			    }
			}
		    });
	    }
            
	    if (showEvents) {
		synthesizer1.addSpeakableListener(
		    new SpeakableAdapter() {
			public void markerReached(SpeakableEvent e) {
			    dumpEvent(e);
			}
			public void speakableCancelled(SpeakableEvent e) {
			    dumpEvent(e);
			}
			public void speakableEnded(SpeakableEvent e) {
			    dumpEvent(e);
			}
			public void speakablePaused(SpeakableEvent e) {
			    dumpEvent(e);
			}
			public void speakableResumed(SpeakableEvent e) {
			    dumpEvent(e);
			}
			public void speakableStarted(SpeakableEvent e) {
			    dumpEvent(e);
			}
			public void topOfQueue(SpeakableEvent e) {
			    dumpEvent(e);
			}
			public void wordStarted(SpeakableEvent e) {
			    dumpEvent(e);
			}
			private void dumpEvent(SpeakableEvent e) {
			    System.out.println(" EVT: " + e.paramString() 
                                               + " source: " + e.getSource());
			}
		    });
	    }
	    
	    System.out.println("And here we go!");
	    synthesizer1.resume();
	    synthesizer2.resume();
	    
	    // speak the "Hello world" string
	    synthesizer1.speakPlainText("Hello! My name is Kevin.", null);
	    synthesizer1.speakPlainText("I am a die phone synthesizer", null);
	    synthesizer1.speakPlainText("I have a friend named Alan.", null);
	    synthesizer1.speakPlainText("Listen to him count!", null);

	    // get synth2 ready to speak
	    synthesizer2.waitEngineState(Synthesizer.ALLOCATED);
	    synthesizer2.resume();

	    synthesizer1.waitEngineState(Synthesizer.QUEUE_EMPTY);
	    synthesizer2.speakPlainText("1 2 3 4 5 6 7 8 9 ten", null);

	    synthesizer2.waitEngineState(Synthesizer.QUEUE_EMPTY);

	    synthesizer1.speakPlainText("Now listen to me count!", null);
	    synthesizer1.speakPlainText("1 2 3 4 5 6 7 8 9 10.", null);

	    synthesizer1.speakPlainText(
		    "Now, let's try that a little bit faster.", null);
	    synthesizer1.waitEngineState(Synthesizer.QUEUE_EMPTY);

	    synthesizer1.getSynthesizerProperties().setSpeakingRate(240.0f);
	    synthesizer1.speakPlainText("1 2 3 4 5 6 7 8 9 10.", null);
	    synthesizer1.speakPlainText("That's pretty fast.", null);
	    synthesizer1.speakPlainText("Now lets go very slow.", null);
	    synthesizer1.waitEngineState(Synthesizer.QUEUE_EMPTY);

	    synthesizer1.getSynthesizerProperties().setSpeakingRate(80.0f);
	    synthesizer1.speakPlainText("1 2 3 4 5 6 7 8 9 10.", null);
	    synthesizer1.speakPlainText("That is pretty slow.", null);
	    synthesizer1.waitEngineState(Synthesizer.QUEUE_EMPTY);

	    synthesizer1.getSynthesizerProperties().setSpeakingRate(150.0f);
	    synthesizer1.speakPlainText("Now back to normal", null);
	    synthesizer1.waitEngineState(Synthesizer.QUEUE_EMPTY);

	    synthesizer1.getSynthesizerProperties().setPitch(200);
	    synthesizer1.speakPlainText("I can talk very high.", null);
	    synthesizer1.waitEngineState(Synthesizer.QUEUE_EMPTY);

	    synthesizer1.getSynthesizerProperties().setPitch(50);
	    synthesizer1.speakPlainText("and I can talk very low.", null);
	    synthesizer1.waitEngineState(Synthesizer.QUEUE_EMPTY);

	    synthesizer1.getSynthesizerProperties().setPitch(100);
	    synthesizer1.getSynthesizerProperties().setVolume(.8f);
	    synthesizer1.speakPlainText("and I can talk very softly.", null);
	    synthesizer1.waitEngineState(Synthesizer.QUEUE_EMPTY);

	    synthesizer1.getSynthesizerProperties().setVolume(1.0f);
	    synthesizer1.speakPlainText(
		"I can talk with a higher quality voice", null);
	    synthesizer1.speakPlainText(
		    "Here is a low quality tongue twister. "
		    + "She sells seashells by the seashore.", null);
	    synthesizer1.waitEngineState(Synthesizer.QUEUE_EMPTY);

	    synthesizer1.getSynthesizerProperties().setVoice(kevinHQ);
	    synthesizer1.speakPlainText("And this is high quality. "
		    + "She sells seashells by the seashore.", null);
	    synthesizer1.speakPlainText(
		    "The funny thing is, I do not have a tongue.", null);
	    synthesizer1.speakPlainText(
		"Hey Alan, what time is it where you are right now?", null);
	    synthesizer1.waitEngineState(Synthesizer.QUEUE_EMPTY);

	    synthesizer2.speakPlainText(
		    "the time is now twenty past six.", null);
	    synthesizer2.waitEngineState(Synthesizer.QUEUE_EMPTY);

	    synthesizer1.speakPlainText("Is that the exact time?", null);
	    synthesizer1.waitEngineState(Synthesizer.QUEUE_EMPTY);

	    synthesizer2.speakPlainText("Almost", null);
	    synthesizer2.waitEngineState(Synthesizer.QUEUE_EMPTY);

	    synthesizer1.speakPlainText(
		"Is it twenty past six In the morning or the evening?", null);
	    synthesizer1.waitEngineState(Synthesizer.QUEUE_EMPTY);

	    synthesizer2.speakPlainText("in the morning.", null);
	    synthesizer2.waitEngineState(Synthesizer.QUEUE_EMPTY);

	    synthesizer1.speakPlainText(
		    "Alan and I can talk at the same time", null);
	    synthesizer1.waitEngineState(Synthesizer.QUEUE_EMPTY);

	    synthesizer1.speakPlainText("1 2 3 4 5 6 7 8 9 11 12", null);
	    synthesizer2.speakPlainText("1 2 3 4 5 6 7 8 9", null);
	    synthesizer1.waitEngineState(Synthesizer.QUEUE_EMPTY);
	    synthesizer2.waitEngineState(Synthesizer.QUEUE_EMPTY);

	    synthesizer1.speakPlainText( "That is a bit confusing.", null);
	    synthesizer1.speakPlainText( "Well, thanks. This was fun.", null);
	    synthesizer1.speakPlainText("Goodbye everyone.", null);
	    synthesizer1.waitEngineState(Synthesizer.QUEUE_EMPTY);
	    
	    // clean up
	    synthesizer1.deallocate();
	    synthesizer2.deallocate();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}

	System.exit(0);
    }
}


/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
import com.sun.speech.freetts.ValidationException;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.audio.JavaClipAudioPlayer;
import com.sun.speech.freetts.en.us.CMULexicon;

import de.dfki.lt.freetts.en.us.MbrolaVoice;
import de.dfki.lt.freetts.en.us.MbrolaVoiceValidator;

/**
 * Simple program to demonstrate the use of the FreeTTS speech synthesizer.
 */
public class FreeTTSHelloWorld {

    public static void main(String[] args) {
	try {

	    String voiceClassName = (args.length > 0) ? args[0] : 
		"com.sun.speech.freetts.en.us.CMUDiphoneVoice";
	    
	    Class voiceClass = Class.forName(voiceClassName);


	    // instantiate the Voice

	    Voice helloVoice = (Voice) voiceClass.newInstance();

            if (helloVoice instanceof MbrolaVoice) {
                try {
                    (new MbrolaVoiceValidator((MbrolaVoice) helloVoice)).
                        validate();
                } catch (ValidationException ve) {
                    System.err.println(ve.getMessage());
                    throw new IllegalStateException
                        ("Problem starting MBROLA voice");
                }
            }
	    
	    // sets the lexicon to CMU lexicon

	    helloVoice.setLexicon(new CMULexicon());


	    // sets the AudioPlayer to the Java clip player

	    helloVoice.setAudioPlayer(new JavaClipAudioPlayer());
	    

	    // loads the Voice, which mainly is loading the lexicon

	    helloVoice.allocate();


	    // does the text-to-speech

	    helloVoice.speak
		("Thank you for giving me a voice. I'm so glad to say hello to this world.");

	    System.exit(0);

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}

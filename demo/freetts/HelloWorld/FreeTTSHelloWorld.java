/**
 * Copyright 2003 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.JavaClipAudioPlayer;

/**
 * Simple program to demonstrate the use of the FreeTTS speech synthesizer.
 */
public class FreeTTSHelloWorld {

    public static void main(String[] args) {
	try {

	    String voiceName = (args.length > 0) ? args[0] : 
		"kevin16";
	    

            VoiceManager voiceManager = VoiceManager.getInstance();

	    Voice helloVoice = voiceManager.getVoice(voiceName);

	    // sets the AudioPlayer to the Java clip player
	    helloVoice.setAudioPlayer(new JavaClipAudioPlayer());
	    

	    // loads the Voice, which mainly is loading the lexicon
	    helloVoice.allocate();


	    // does the text-to-speech
	    helloVoice.speak("Thank you for giving me a voice. I'm so glad to say hello to this world.");

	    System.exit(0);

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}

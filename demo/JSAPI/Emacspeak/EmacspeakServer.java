/**
 * Copyright 2003 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.Locale;

import javax.speech.Central;
import javax.speech.Engine;
import javax.speech.EngineException;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.SynthesizerProperties;
import javax.speech.synthesis.Voice;


/**
 * Provides text-to-speech server for Emacspeak.
 */
public class EmacspeakServer extends TTSServer {

    // synthesizer related variables
    private Synthesizer synthesizer;
    private String voiceName;
    private Voice voice;


    /**
     * Constructs a EmacspeakServer.
     */
    public EmacspeakServer(String voiceName) {
	loadSynthesizer(voiceName);
    }

    /**
     * Creates and loads the synthesizer.
     */
    private void loadSynthesizer(String voiceName) {

        voice = new Voice(voiceName, 
			  Voice.GENDER_DONT_CARE, 
			  Voice.AGE_DONT_CARE, 
			  null);

	SynthesizerModeDesc modeDesc = new SynthesizerModeDesc(
	    null, "general", Locale.US, null, null);

	try {
	    synthesizer = Central.createSynthesizer(modeDesc);

	    if (synthesizer == null) {
		System.err.println(noSynthesizerMessage());
		System.exit(1);
	    }

	    synthesizer.allocate();
	    synthesizer.resume();
	    synthesizer.getSynthesizerProperties().setVolume(1.0f);
	    synthesizer.getSynthesizerProperties().setVoice(voice);
	} catch (Exception e) {
	    System.out.println("Error creating synthesizer");
	    System.exit(1);
	}
    }


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
     * Spawns a ProtocolHandler depending on the current protocol.
     *
     * @param socket the socket that the spawned protocol handler will use
     */
    protected void spawnProtocolHandler(Socket socket) {
	try {
	    JSAPIEmacspeakHandler handler =
		new JSAPIEmacspeakHandler(socket, synthesizer);
	    (new Thread(handler)).start();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }


    /**
     * Sets the speaking rate of the voice.
     *
     * @param wpm the speaking rate (words per minute)
     */
    public void setRate(float wpm) {
        try {
            synthesizer.getSynthesizerProperties().setSpeakingRate(wpm);
        } catch (java.beans.PropertyVetoException e) {
            // ignore and do nothing
        }
    }
    
        
    /**
     * Starts this TTS Server.
     */
    public static void main(String[] args) {
        String voiceName = (args.length > 0)
            ? args[0]
            : "kevin16";
        
        System.out.println();
        System.out.println("Using voice: " + voiceName);
        System.out.println();

	EmacspeakServer server = new EmacspeakServer(voiceName);

        if (args.length > 1) {
            float wpm = Float.parseFloat(args[1]);
            server.setRate(wpm);
        }
        
	(new Thread(server)).start();
    }
}

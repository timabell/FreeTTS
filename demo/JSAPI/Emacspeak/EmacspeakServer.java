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
    private String synthesizerName;
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
	    System.out.println("Creating " + synthesizerName + "...");

	    synthesizer = Central.createSynthesizer(modeDesc);

	    if (synthesizer == null) {
		System.err.println(noSynthesizerMessage());
		System.exit(1);
	    }

	    System.out.println("Loading " + synthesizerName + "...");

	    synthesizer.allocate();
	    synthesizer.resume();
	    synthesizer.getSynthesizerProperties().setVolume(1.0f);
	    synthesizer.getSynthesizerProperties().setVoice(voice);

	    System.out.println("...Ready");
	} catch (Exception e) {
	    System.out.println("Error creating " + synthesizerName);
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
    private String noSynthesizerMessage() {
        String message = "No synthesizer created.\n" +
            "Make sure that there is a \"speech.properties\" file at either " +
            "of these locations: \n";
        message += "user.home    : " + System.getProperty("user.home") + "\n";
        message += "java.home/lib: " + System.getProperty("java.home")
	    + File.separator + "lib\n";
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

	(new Thread(server)).start();
    }
}

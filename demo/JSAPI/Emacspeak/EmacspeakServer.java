/**
 * Copyright 2001 Sun Microsystems, Inc.
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
    private String synthesizerName = System.getProperty
	("synthesizerName",
	 "Unlimited domain FreeTTS Speech Synthesizer from Sun Labs");
    private String modeName = null;
    private Locale locale = null;
    private Boolean running = null;
    private Voice[] voices = null;


    // kevinHQ in a 16khz unlimited-domain diphone voice
    private Voice kevinHQ = new Voice
	("kevin16", Voice.GENDER_DONT_CARE, Voice.AGE_DONT_CARE, null);


    /**
     * Constructs a EmacspeakServer.
     */
    public EmacspeakServer() {
	loadSynthesizer();
    }


    /**
     * Creates and loads the synthesizer.
     */
    private void loadSynthesizer() {
	SynthesizerModeDesc modeDesc = new SynthesizerModeDesc
	    (synthesizerName, modeName, locale, running, voices);

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
	    synthesizer.getSynthesizerProperties().setVoice(kevinHQ);

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
        String message = "PlayerModelImpl: no synthesizer created.\n" +
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
	    EmacspeakProtocolHandler handler =
		new EmacspeakProtocolHandler(socket, synthesizer);
	    (new Thread(handler)).start();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }


    /**
     * Starts this TTS Server.
     */
    public static void main(String[] args) {
	EmacspeakServer server = new EmacspeakServer();
	(new Thread(server)).start();
    }
}

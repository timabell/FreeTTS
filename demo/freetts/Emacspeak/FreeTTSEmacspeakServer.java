/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.audio.JavaClipAudioPlayer;
import com.sun.speech.freetts.en.us.CMULexicon;

import java.net.Socket;

/**
 * Provides text-to-speech server for Emacspeak.
 */
public class FreeTTSEmacspeakServer extends TTSServer {

    private Voice emacsVoice;
    
    /**
     * Constructs a EmacspeakServer.
     */
    public FreeTTSEmacspeakServer() {
	System.setProperty
	    ("com.sun.speech.freetts.audio.AudioPlayer.closeDelay", "0");
	createVoice();
    }


    /**
     * Creates and loads the Voice.
     */
    private void createVoice() {
	String voiceClassName = System.getProperty
	    ("voiceClass", "com.sun.speech.freetts.en.us.CMUDiphoneVoice");
	String diphoneDatabase = System.getProperty
	    ("diphoneDatabase", "cmu_kal/diphone_units16.bin");

	try {
	    Class voiceClass = Class.forName(voiceClassName);

	    System.out.println("Creating " + voiceClassName + "...");

	    emacsVoice = (Voice) voiceClass.newInstance();

	    System.out.println("Loading " + voiceClassName + "...");

	    emacsVoice.getFeatures().setString
		(Voice.DATABASE_NAME, diphoneDatabase);
	    emacsVoice.setLexicon(new CMULexicon());
	    emacsVoice.setOutputQueue(Voice.createOutputThread());
	    emacsVoice.load();
	    emacsVoice.setAudioPlayer(new JavaClipAudioPlayer());

	    System.out.println("...Ready");

	} catch (Exception e) {
	    System.out.println("Error creating " + voiceClassName);
	    System.exit(1);
	}
    }


    /**
     * Spawns a ProtocolHandler depending on the current protocol.
     * This method is inherited from TTSServer.
     *
     * @param socket the socket that the spawned protocol handler will use
     */
    protected void spawnProtocolHandler(Socket socket) {
	try {
	    FreeTTSEmacspeakHandler handler =
		new FreeTTSEmacspeakHandler(socket, emacsVoice);
	    (new Thread(handler)).start();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }


    /**
     * Starts this TTS Server.
     */
    public static void main(String[] args) {
	FreeTTSEmacspeakServer server = new FreeTTSEmacspeakServer();
	(new Thread(server)).start();
    }
}

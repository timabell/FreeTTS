/**
 * Copyright 2003 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */

import com.sun.speech.freetts.ValidationException;
import com.sun.speech.freetts.Validator;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.JavaClipAudioPlayer;
import com.sun.speech.freetts.audio.JavaStreamingAudioPlayer;

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
	    ("com.sun.speech.freetts.audio.AudioPlayer.cancelDelay", "0");
	createVoice();
    }


    /**
     * Creates and loads the Voice.
     */
    private void createVoice() {
	String voiceName = System.getProperty("voiceName", "kevin16");
        VoiceManager voiceManager = VoiceManager.getInstance();
        emacsVoice = voiceManager.getVoice(voiceName);

        emacsVoice.allocate();
        emacsVoice.setAudioPlayer(new JavaStreamingAudioPlayer());
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

/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */

import java.net.Socket;

import javax.speech.synthesis.Synthesizer;
import com.sun.speech.freetts.util.Utilities;


/**
 * Implements a very simplified version of the Emacspeak speech server.
 */
public class JSAPIEmacspeakHandler extends EmacspeakProtocolHandler {

    // synthesizer related variables
    private Synthesizer synthesizer;
    

    /**
     * Constructs a JSAPIEmacspeakHandler.
     *
     * @param socket the Socket that holds the TCP connection
     * @param synthesizer the JSAPI synthesizer to use
     */
    public JSAPIEmacspeakHandler(Socket socket, Synthesizer synthesizer) {
	setSocket(socket);
	this.synthesizer = synthesizer;
        setDebug(Utilities.getBoolean("debug"));
    }


    /**
     * Speaks the given input text.
     *
     * @param input the input text to speak.
     */
    public void speak(String input) {
	// split around "[*]"
	String[] parts = input.split(PARENS_STAR_REGEX);
	for (int i = 0; i < parts.length; i++) {
	    debugPrintln(parts[i]);
	    synthesizer.speakPlainText(parts[i], null);
	}
    }


    /**
     * Removes all the queued text.
     */
    public void cancelAll() {
        synthesizer.cancelAll();
    }


    /**
     * Sets the speaking rate.
     *
     * @param wpm the new speaking rate (words per minute)
     */
    public void setRate(float wpm) {
        try {
            synthesizer.getSynthesizerProperties().setSpeakingRate(wpm);
        } catch (java.beans.PropertyVetoException e) {
            // ignore and do nothing
        }
    }
}

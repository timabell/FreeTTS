/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */

import com.sun.speech.freetts.Voice;

import com.sun.speech.freetts.util.Utilities;
import java.net.Socket;
import java.util.Vector;


/**
 * Implements a simplified version of the Emacspeak speech server.
 */
public class FreeTTSEmacspeakHandler extends EmacspeakProtocolHandler {

    private SpeakCommandHandler speakCommandHandler;


    /**
     * Constructs a Emacspeak ProtocolHandler
     *
     * @param freetts the FreeTTS that this FreeTTSEmacspeakHandler belongs
     * @param socket the Socket that holds the TCP connection
     */
    public FreeTTSEmacspeakHandler(Socket socket, Voice voice) {
	setSocket(socket);
	this.speakCommandHandler = new SpeakCommandHandler(voice);
	this.speakCommandHandler.start();
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
	    speakCommandHandler.add(parts[i]);
	}
    }


    /**
     * Removes all the queued text.
     */
    public void cancelAll() {
        speakCommandHandler.removeAll();
    }


    /**
     * Sets the speaking rate.
     *
     * @param wpm the new speaking rate (words per minute)
     */
    public void setRate(float wpm) {
        speakCommandHandler.setRate(wpm);
    }
    
    
    /**
     * This thread is used to separate the handling of Voice.speak() from
     * the thread that accepts commands from the client, so that the 
     * latter won't be blocked by the former.
     */
    class SpeakCommandHandler extends Thread {
        
        private Voice voice;
        private boolean done = false;
        private Vector commandList = new Vector();
        
        
        /**
         * Constructs a default SpeakCommandHandler object.
         *
         * @param voice the Voice object use to speak
         */
        public SpeakCommandHandler(Voice voice) {
            this.voice = voice;
        }
        
        
        /**
         * Implements the run() method of the Thread class.
         */
        public void run() {
            while (!getSocket().isClosed() || commandList.size() > 0) {
                Object firstCommand = null;
                synchronized (commandList) {
                    while (commandList.size() == 0 &&
                           !getSocket().isClosed()) {
                        try {
                            commandList.wait();
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }
                    if (commandList.size() > 0) {
                        firstCommand = commandList.remove(0);
                    }
                }
                if (firstCommand != null) {
                    voice.speak((String) firstCommand);
                    debugPrintln("SPEAK: \"" + firstCommand + "\"");
                }
            }
            debugPrintln("SpeakCommandHandler: thread terminated");
        }
        
        
        /**
         * Adds the given command to this Handler.
         *
         * @param command the text to be spoken
         */
        public void add(String command) {
            synchronized (commandList) {
                commandList.add(command);
                commandList.notifyAll();
            }
        }
    
    
        /**
         * Removes all the commands from this Handler.
         */
        public void removeAll() {
            synchronized (commandList) {
                voice.getAudioPlayer().cancel();
                commandList.removeAllElements();
            }
        }
        
        
        /**
         * Sets the speaking rate.
         *
         * @param wpm the new speaking rate (words per minute)
         */
        public void setRate(float wpm) {
            voice.setRate(wpm);
        }
        
        
        /**
         * Terminates this SpeakCommandHandler thread.
         *
         * @param done true to terminate this thread
         */
        public synchronized void setDone(boolean done) {
            this.done = done;
        }
    }
}

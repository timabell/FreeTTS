/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */

import com.sun.speech.freetts.Voice;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.Socket;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;


/**
 * Implements a simplified version of the Emacspeak speech server.
 */
public class FreeTTSEmacspeakHandler implements Runnable {

    // network related variables
    private Socket socket;
    private BufferedReader reader;
    private DataOutputStream writer;

    private SpeakCommandHandler speakCommandHandler;

    private static final String PARENS_STAR_REGEX = "[.]*\\[\\*\\][.]*";
    private static final int NOT_HANDLED_COMMAND = 1;
    private static final int LETTER_COMMAND = 2;
    private static final int QUEUE_COMMAND = 3;
    private static final int TTS_SAY_COMMAND = 4;
    private static final int STOP_COMMAND = 5;
    private int lastSpokenCommandType;
    private String lastQueuedCommand;

    private String stopQuestion = 
    "Active processes exist; kill them and exit anyway?   yes or no";

    private static boolean debug = false;


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
	debug = Boolean.getBoolean("debug");
    }


    /**
     * Sets the Socket to be used by this ProtocolHandler.
     *
     * @param socket the Socket to be used
     */
    public void setSocket(Socket socket) {
	this.socket = socket;
	if (socket != null) {
	    try {
		reader = new BufferedReader
		    (new InputStreamReader(socket.getInputStream()));
		writer = new DataOutputStream(socket.getOutputStream());
	    } catch (IOException ioe) {
		ioe.printStackTrace();
		throw new Error();
	    }
	}
    }
    

    /**
     * Returns true if the given input string starts with the given
     * starting and ending sequence.
     *
     * @param start the starting character sequence
     * @param end the ending character sequence
     * 
     * @return true if the input string matches the given Pattern;
     *         false otherwise
     */
    private static boolean matches(String start, String end, String input) {
	return (input.startsWith(start) && input.endsWith(end));
    }


    /**
     * Returns the type of the given command.
     *
     * @param command the command from emacspeak
     *
     * @return the command type
     */
    private static int getCommandType(String command) {
	int type = NOT_HANDLED_COMMAND;
	if (matches("l {", "}", command)) {
	    type = LETTER_COMMAND;
	} else if (matches("q {", "}",  command)) {
	    type = QUEUE_COMMAND;
	} else if (matches("tts_say", "}", command)) {
	    type = TTS_SAY_COMMAND;
	} else if (command.equals("s")) {
	    type = STOP_COMMAND;
	}
	return type;
    }


    /**
     * Returns the text of the given input that is within curly brackets.
     *
     * @param input the input text
     *
     * @return text within curly brackets
     */
    public String textInCurlyBrackets(String input) {
	String result = "";
	if (input.length() > 0) {
	    int first = input.indexOf('{');
	    int last = input.lastIndexOf('}');
	    if (first != -1 && last != -1 && first < last) {
		result = input.substring(first+1, last);
	    }
	}
	return result;
    }


    /**
     * Speaks the given input text.
     *
     * @param input the input text to speak.
     */
    private void speak(String input) {
	// split around "[*]"
	String[] parts = input.split(PARENS_STAR_REGEX);
	for (int i = 0; i < parts.length; i++) {
	    speakCommandHandler.add(parts[i]);
	}
    }


    /**
     * Implements the run() method of Runnable
     */
    public void run() {
	try {
	    String command = "";
	    while (!socket.isClosed() && socket.isConnected()) {
		command = reader.readLine();
		if (command != null) {
		    command = command.trim();
		    debugPrintln("IN   : " + command);

		    int commandType = getCommandType(command);

		    if (commandType == STOP_COMMAND) {
			speakCommandHandler.removeAll();
                    } else if (commandType != NOT_HANDLED_COMMAND) {
                        String content = textInCurlyBrackets(command).trim();
                        if (content.length() > 0) {
			    speak(content);
			    lastSpokenCommandType = commandType;
			}
                        // detect if emacspeak is trying to quit
                        detectQuitting(commandType, content);
		    } else {
			debugPrintln("SPEAK:");
		    }
		}
	    }
            speakCommandHandler.setDone(true);
            socket.close();
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	} finally {
            debugPrintln("FreeTTSEmacspeakHandler: thread terminated");
        }
    }


    /**
     * Detects and handles a possible emacspeak quitting sequence 
     * of commands, by looking at the given command type and content.
     * If a quitting sequence is detected, it will close the socket.
     * Note that this is not the best way to trap a quitting sequence,
     * but I can't find another way to trap it.
     *
     * @param commandType the command type
     * @param content the contents of the command
     */
    private void detectQuitting(int commandType, String content) throws
    IOException {
        if (commandType == QUEUE_COMMAND) {
            lastQueuedCommand = content;
        } else if (commandType == TTS_SAY_COMMAND) {
            if (content.equals("no")) {
                lastQueuedCommand = "";
            } else if (content.equals("yes") &&
                  lastQueuedCommand.equals(stopQuestion)) {
                socket.close();
            }
        }
    }


    /**
     * Prints the given message if the <code>debug</code> System property
     * is set to <code>true</code>.
     *
     * @param message the message to print
     */ 
    protected static void debugPrintln(String message) {
	if (debug) {
	    System.out.println(message);
	}
    }
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
	while (!done) {
	    Object firstCommand = null;
	    synchronized (commandList) {
		while (commandList.size() == 0 && !done) {
		    try {
			commandList.wait();
		    } catch (InterruptedException ie) {
			ie.printStackTrace();
		    }
		}		
		firstCommand = commandList.remove(0);
	    }
	    if (firstCommand != null) {
		voice.speak((String) firstCommand);
		FreeTTSEmacspeakHandler.debugPrintln
		    ("SPEAK: \"" + firstCommand + "\"");
	    }
        }
        FreeTTSEmacspeakHandler.debugPrintln
            ("SpeakCommandHandler: thread terminated");
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
     * Terminates this SpeakCommandHandler thread.
     *
     * @param done true to terminate this thread
     */
    public synchronized void setDone(boolean done) {
        this.done = done;
    }
}

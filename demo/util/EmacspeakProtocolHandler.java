/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.Socket;


/**
 * Implements a very simplified version of the Emacspeak speech server.
 */
public abstract class EmacspeakProtocolHandler implements Runnable {

    // network related variables
    private Socket socket;
    private BufferedReader reader;
    private DataOutputStream writer;

    // synthesizer related variables
    protected static final String PARENS_STAR_REGEX = "[.]*\\[\\*\\][.]*";
    private int lastSpokenCommandType;
    private static final int NOT_HANDLED_COMMAND = 1;
    private static final int LETTER_COMMAND = 2;
    private static final int QUEUE_COMMAND = 3;
    private static final int TTS_SAY_COMMAND = 4;
    private static final int STOP_COMMAND = 5;
    private static final int EXIT_COMMAND = 6;

    private String lastQueuedCommand;
    private String stopQuestionStart = "Active processes exist;";

    private boolean debug = false;


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
     * Returns the socket used.
     *
     * @return the socket used
     */
    public Socket getSocket() {
        return socket;
    }


    /**
     * Set to debug mode, which will print out debug messages.
     *
     * @param true if set to debug mode, false if set to non-debug mode.
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
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
	} else if (command.equals("exit")) {
            type = EXIT_COMMAND;
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
    public static String textInCurlyBrackets(String input) {
	String result = "";
	if (input.length() > 0) {
	    int first = input.indexOf('{');
	    int last = input.lastIndexOf('}');
	    if (first != -1 && last != -1 &&
		first < last) {
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
    public abstract void speak(String input);


    /**
     * Removes all the queued text.
     */
    public abstract void cancelAll();


    /**
     * Implements the run() method of Runnable
     */
    public void run() {
        try {
            String command = "";
            while (!socket.isClosed() && socket.isConnected() &&
                   !socket.isInputShutdown() && !socket.isOutputShutdown()) {
                command = reader.readLine();
                if (command != null) {
                    command = command.trim();
                    debugPrintln("IN   : " + command);

                    int commandType = getCommandType(command);

                    if (commandType == EXIT_COMMAND) {
                        socket.close();
                        notifyAll();
                    } else if (commandType == STOP_COMMAND) {
                        cancelAll();
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
     * but I can't find another way to trap it. This method will
     * do a socket.notifyAll() to tell objects waiting on the socket
     * that it has been closed. 
     *
     * @param commandType the command type
     * @param content the contents of the command
     */
    private synchronized void detectQuitting
    (int commandType, String content) throws IOException {
        if (commandType == QUEUE_COMMAND) {
            lastQueuedCommand = content;
        } else if (commandType == TTS_SAY_COMMAND) {
            if (content.equals("no")) {
                lastQueuedCommand = "";
            } else if (content.equals("yes") &&
                       lastQueuedCommand.startsWith(stopQuestionStart)) {
                socket.close();
                notifyAll();
            }
        }
    }


    /**
     * Prints the given message if the <code>debug</code> System property
     * is set to <code>true</code>.
     *
     * @param message the message to print
     */ 
    public void debugPrintln(String message) {
	if (debug) {
	    System.out.println(message);
	}
    }
}

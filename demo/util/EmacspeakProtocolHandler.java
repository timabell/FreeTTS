/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
import com.sun.speech.freetts.util.Utilities;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.Socket;
import java.net.SocketTimeoutException;


/**
 * Implements a very simplified (and incomplete) version of the
 * Emacspeak speech server.
 *
 * See the Emacspeak protocol document at
 * http://emacspeak.sourceforge.net/info/html/TTS-Servers.html
 * for more information.
 */
public abstract class EmacspeakProtocolHandler implements Runnable {

    // network related variables
    private Socket socket;
    private BufferedReader reader;
    private OutputStream writer;

    // synthesizer related variables
    protected static final String PARENS_STAR_REGEX = "[.]*\\[\\*\\][.]*";
    private static final int NOT_HANDLED_COMMAND = 1;
    private static final int LETTER_COMMAND = 2;
    private static final int QUEUE_COMMAND = 3;
    private static final int TTS_SAY_COMMAND = 4;
    private static final int STOP_COMMAND = 5;
    private static final int EXIT_COMMAND = 6;
    private static final int RATE_COMMAND = 7;
    
    private String lastQueuedCommand;
    private String stopQuestionStart = "Active processes exist;";

    private boolean debug = false;

    /**
     * Sometimes emacspeak will embed DECTalk escape sequences in the
     * text.  These sequences are not meant to be spoken, and FreeTTS
     * currently does not interpret them.  This simple flag provides
     * a mechanism for FreeTTS to cut strings of the form "[...]"
     * out of text to be spoken (DECTalk escape sequences are of the
     * form "[...]").  Since this is a relatively heavy-handed thing
     * to do, this feature is turned off by default.  To turn it on,
     * add -DstripDECTalk=true to the command line.
     */
    private boolean stripDECTalk = false;

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
                socket.setKeepAlive(true);
                // socket.setSoTimeout(5000);
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
        if (command.startsWith("l ")) {
            type = LETTER_COMMAND;
        } else if (command.startsWith("q ")) {
            type = QUEUE_COMMAND;
        } else if (command.startsWith("tts_say ")) {
            type = TTS_SAY_COMMAND;
        } else if (command.startsWith("tts_set_speech_rate ")) {
            type = RATE_COMMAND;
        } else if (command.equals("s")) {
            type = STOP_COMMAND;
        } else if (command.equals("exit")) {
            type = EXIT_COMMAND;
        }
	return type;
    }

    
    /**
     * Returns the text of the given input that is within curly
     * brackets.  If there are no curly brackets (allowed in the
     * Emacspeak protocol if the text has no spaces), then it
     * just returns the text after the first space.
     *
     * @param input the input text
     *
     * @return text within curly brackets
     */
    public static String textInCurlyBrackets(String input) {
	String result = "";
	if (input.length() > 0) {
	    int first = input.indexOf('{');
            if (first == -1) {
                first = input.indexOf(' ');
            }
	    int last = input.lastIndexOf('}');
            if (last == -1) {
                last = input.length();
            }
	    if (first != -1 && last != -1 &&
		first < last) {
		result = input.substring(first+1, last);
	    }
	}
	return result.trim();
    }


    /**
     * Strips DECTalk commands from the input text.  The DECTalk
     * commands are anything inside "[" and "]".
     */
    public String stripDECTalkCommands(String content) {
        int startPos = content.indexOf('[');
        while (startPos != -1) {
            int endPos = content.indexOf(']');
            if (endPos != -1) {
                if (startPos == 0) {
                    if (endPos == (content.length() - 1)) {
                        content = "";
                    } else {
                        content = content.substring(endPos + 1);
                    }
                } else {
                    if (endPos == (content.length() - 1)) {
                        content = content.substring(0, startPos);
                    } else {
                        String firstPart = content.substring(0, startPos);
                        String secondPart = content.substring(endPos + 1);
                        content = firstPart + " " + secondPart;
                    }
                }
                startPos = content.indexOf('[');
            } else {
                break;
            }
        }
        return content.trim();
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
     * Sets the speaking rate.
     *
     * @param wpm the new speaking rate (words per minute)
     */
    public abstract void setRate(float wpm);


    /**
     * Implements the run() method of Runnable
     */
    public synchronized void run() {
        try {
            String command = "";
            stripDECTalk = Utilities.getBoolean("stripDECTalk");
            while (isSocketLive()) {
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
                    } else if (commandType == RATE_COMMAND) {
                        try {
                            setRate(Float.parseFloat(
                                        textInCurlyBrackets(command)));
                        } catch (NumberFormatException e) {
                            // ignore and do nothing
                        }
                    } else if (commandType != NOT_HANDLED_COMMAND) {
                        String content = textInCurlyBrackets(command);
                        if (stripDECTalk) {
                            content = stripDECTalkCommands(content);
                        }
                        if (content.length() > 0) {
                            speak(content);
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
            debugPrintln("EmacspeakProtocolHandler: thread terminated");
        }
    }


    /**
     * Returns true if the Socket is still alive.
     *
     * @return true if the Socket is still alive
     */
    private boolean isSocketLive() {
        return (socket.isBound() &&
                !socket.isClosed() && socket.isConnected() &&
                !socket.isInputShutdown() && !socket.isOutputShutdown());
    }


    /**
     * Read a line of text. A line is considered to be terminated 
     * by any one of a line feed ('\n'), a carriage return ('\r'),
     * or a carriage return followed immediately by a linefeed. 
     *
     * @return A String containing the contents of the line, 
     * not including any line-termination
     * characters, or null if the end of the stream has been reached
     *
     * @throws IOException if an I/O error occurs
     */
    private String readLine() throws IOException {
        String command = null;
        boolean repeat = false;
        do {
            try {
                command = reader.readLine();
                repeat = false;
            } catch (SocketTimeoutException ste) {
                System.out.println("timed out");
                /*
                writer.write(-1);
                writer.flush();
                */
                repeat = isSocketLive();
            }
        } while (repeat);
     
        return command;
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
    private synchronized void detectQuitting(int commandType, String content)
        throws IOException {
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

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

import javax.speech.synthesis.Synthesizer;


/**
 * Implements a very simplified version of the Emacspeak speech server.
 */
public class EmacspeakProtocolHandler implements Runnable {

    // network related variables
    private Socket socket;
    private BufferedReader reader;
    private DataOutputStream writer;

    // synthesizer related variables
    private Synthesizer synthesizer;
    private static final String PARENS_STAR_REGEX = "[.]*\\[\\*\\][.]*";
    private int lastSpokenCommandType;
    private static final int NOT_HANDLED_COMMAND = 1;
    private static final int LETTER_COMMAND = 2;
    private static final int QUEUE_COMMAND = 3;
    private static final int TTS_SAY_COMMAND = 4;
    private static final int STOP_COMMAND = 5;


    /**
     * Constructs a Emacspeak ProtocolHandler
     *
     * @param freetts the FreeTTS that this EmacspeakProtocolHandler belongs
     * @param socket the Socket that holds the TCP connection
     */
    public EmacspeakProtocolHandler(Socket socket, Synthesizer synthesizer) {
	setSocket(socket);
	this.synthesizer = synthesizer;
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
    private void speak(String input) {
	// split around "[*]"
	String[] parts = input.split(PARENS_STAR_REGEX);
	for (int i = 0; i < parts.length; i++) {
	    System.out.println(parts[i]);
	    synthesizer.speakPlainText(parts[i], null);
	}
    }


    /**
     * Implements the run() method of Runnable
     */
    public void run() {
	try {
	    String command = "";
	    while (true) {
		command = reader.readLine();
		if (command != null) {
		    command = command.trim();
		    System.out.println("IN   : " + command);

		    int commandType = getCommandType(command);

		    if (commandType == STOP_COMMAND) {
			synthesizer.cancelAll();
		    } else if (commandType != NOT_HANDLED_COMMAND) {
			String content = textInCurlyBrackets(command).trim();
			speak(content);
			System.out.println("SPEAK: \"" + content + "\"");
			lastSpokenCommandType = commandType;
		    } else {
			System.out.println("SPEAK:");
		    }
		}
	    }
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	}
    }
}    

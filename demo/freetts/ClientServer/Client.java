/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */


import com.sun.speech.freetts.audio.AudioPlayer;
import com.sun.speech.freetts.audio.JavaStreamingAudioPlayer;
import com.sun.speech.freetts.util.Utilities;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.net.Socket;

import javax.sound.sampled.AudioFormat;


/**
 * Implements a Java Client for the Client/Server demo. For details about
 * the protocol between client and server, consult the file
 * <code>Protocol.txt</code>.
 */
public class Client {

    private String serverAddress = Utilities.getProperty("server", "localhost");
    private int serverPort = Utilities.getInteger("port", 5555).intValue();

    private static final int AUDIO_BUFFER_SIZE = 256;

    private boolean debug = Utilities.getBoolean("debug");

    private BufferedReader systemInReader;  // for reading user input text
    private BufferedReader reader;
    private DataInputStream dataReader;     // for reading raw bytes
    private PrintWriter writer;
    private AudioPlayer audioPlayer;
    private int sampleRate = Utilities.getInteger("sampleRate", 16000).intValue();
    private int sampleSize = 16;            // in bits
    private byte[] socketBuffer = new byte[AUDIO_BUFFER_SIZE];


    private boolean metrics = Boolean.getBoolean("metrics");
    private long sendTime;             // time the text is sent to server
    private long receiveTime;          // time the first byte is received
    private long firstSoundTime;       // time the first play to audio
    private boolean firstByteReceived = false;


    private static final String FIRST_SENTENCE =
	"Type in what you want me to say.";


    /**
     * Constructs a default Client. It connects to the speech server, and
     * constructs an AudioPlayer.
     */
    public Client() {
	if (!connect()) {
	    System.out.println("Error connecting to " + serverAddress +
			       " at " + serverPort);
	    System.exit(1);
	}
	this.audioPlayer = new JavaStreamingAudioPlayer();
	this.audioPlayer.setAudioFormat
	    (new AudioFormat(sampleRate, sampleSize, 1, true, true));
    }


    /**
     * Connects this client to the server.
     *
     * @return  <code>true</code>  if successfully connected
     *          <code>false</code>  if failed to connect
     */
    private boolean connect() {
        try {
            Socket socket = new Socket(serverAddress, serverPort);
	    dataReader = new DataInputStream(socket.getInputStream());
	    systemInReader = new BufferedReader
		(new InputStreamReader(System.in));
	    writer = new PrintWriter(socket.getOutputStream(), true);
            return true;
        } catch (IOException ioe) {
	    ioe.printStackTrace();
	    return false;
        }
    }


    /**
     * Reads a line of text from the Socket.
     *
     * @return a line of text without the end of line character
     */
    private String readLine() throws IOException {
	int i;
	char c;
	StringBuffer buffer = new StringBuffer();

	while ((c = (char) dataReader.readByte()) != '\n') {
	    if (debug) {
		System.out.println(c);
	    }
	    buffer.append(c);
	}

	int lastCharIndex = buffer.length() - 1;
	
	// remove trailing ^M for Windows-based machines
	byte lastByte = (byte) buffer.charAt(lastCharIndex);
	if (lastByte == 13) {
	    return buffer.substring(0, lastCharIndex);
	} else {
	    return buffer.toString();
	}
    }


    /**
     * Sends the given line of text to the Socket, appending an end of
     * line character to the end.
     *
     * @param the line of text to send
     */
    private void sendLine(String line) {
	if (debug) {
	    System.out.println(line);
	}
	line = line.trim();
	if (line.length() > 0) {
	    writer.print(line);
	    writer.print('\n');
	    writer.flush();
	}
    }


    /**
     * Run the TTS protocol.
     */
    public void runTTSProtocol() {
	try {
	    String readyLine = readLine();
	    if (readyLine.equals("READY")) {
		if (!sendTTSRequest(FIRST_SENTENCE)) {
		    return;
		}
		System.out.print("Say       : ");
		String input;
		while ((input = systemInReader.readLine()) != null) {
		    if (input.length() > 0 && !sendTTSRequest(input)) {
			return;
		    }
		    System.out.print("Say       : ");
		}
	    }
	    sendLine("DONE");

	    audioPlayer.drain();
	    audioPlayer.close();

	    System.out.println("ALL DONE");

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }


    /**
     * Sends a TTS request on the given text.
     *
     * @param text the text to do TTS on
     *
     * @return <code>true</code> if the TTS transaction was successful
     *         <code>false</code> if an error occurred
     */
    private boolean sendTTSRequest(String text) {

	if (metrics) {
	    sendTime = System.currentTimeMillis();
	    firstByteReceived = false;
	}

	// send TTS request to server
	sendLine("TTS\n" +
		 String.valueOf(sampleRate) + "\n" +
		 text + "\n");

	// get response
	String numberSamplesStr = null;
	int numberSamples = 0;

	do {
	    try {
		numberSamplesStr = readLine();
		numberSamples = Integer.parseInt(numberSamplesStr);
				
		if (numberSamples == -2) { // error
		    System.err.println("Client.sendTTSRequest(): error!");
		    return false;
		}		    
		if (numberSamples > 0) {
		    System.out.println
			("Receiving : " + numberSamples + " samples");
		    receiveAndPlay(numberSamples);
		}
	    } catch (IOException ioe) {
		ioe.printStackTrace();
	    }
	}
	while (numberSamples > 0);

	if (metrics) {
	    System.out.println("FirstByte : " +
			       (receiveTime - sendTime) + " ms");
	}

	return true;
    }


    /**
     * Reads the given number of bytes from the socket, and plays them
     * with the AudioPlayer.
     *
     * @param numberSamples the number of bytes to read from the socket
     */
    private void receiveAndPlay(int numberSamples) {

	int bytesToRead;
	int bytesRemaining;

	bytesRemaining = numberSamples;

        audioPlayer.begin(0);

	while (bytesRemaining > 0) {
	    
	    // how many more bytes do we have to read?
	    if (bytesRemaining >= AUDIO_BUFFER_SIZE) {
		bytesToRead = AUDIO_BUFFER_SIZE;
	    } else {
		bytesToRead = bytesRemaining;
	    }
	    
	    try {
		// we want to fill the socketBuffer completely before playing
		int nRead = 0;
		do {
		    int read = dataReader.read
			(socketBuffer, nRead, bytesToRead);

		    if (metrics && !firstByteReceived) {
			receiveTime = System.currentTimeMillis();
		    }
		    nRead += read;
		    bytesToRead -= read;
		}
		while (bytesToRead > 0);
	       
		if (nRead < 0) {
		    System.err.println("error reading samples");
		} else {
		    bytesRemaining -= nRead;
		    
		    if (metrics && !firstByteReceived) {
			firstSoundTime = System.currentTimeMillis();
			firstByteReceived = true;
		    }
		    audioPlayer.write(socketBuffer, 0, nRead);
		}
	    } catch (IOException ioe) {
		ioe.printStackTrace();
	    }
	    
	    if (debug) {
		System.out.println("BytesRemaining: " + bytesRemaining);
	    }
	}

        audioPlayer.end();

	if (debug) {
	    System.out.println("finished");
	}
    }


    /**
     * Main program to run the client.
     */    
    public static void main(String[] argv) {
	Client client = new Client();
	client.runTTSProtocol();
	System.exit(0);
    }
}

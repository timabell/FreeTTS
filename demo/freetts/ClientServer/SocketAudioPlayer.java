/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */

import com.sun.speech.freetts.audio.AudioPlayer;
import com.sun.speech.freetts.util.Utilities;

import java.io.DataOutputStream;
import java.io.IOException;

import java.net.Socket;

import javax.sound.sampled.AudioFormat;


/**
 * Implements the AudioPlayer for the freetts Client/Server demo.
 * This SocketAudioPlayer basically sends synthesized wave bytes to the
 * client.
 */
public class SocketAudioPlayer implements AudioPlayer {

    private AudioFormat audioFormat;
    private Socket socket;
    private DataOutputStream dataOutputStream;
    private boolean debug = false;
    private int bytesToPlay = 0;
    private int bytesPlayed = 0;
    private boolean firstByteSent = false;
    private long firstByteTime = -1;


    /**
     * Constructs a SocketAudioPlayer that will send wave bytes to the
     * given Socket.
     *
     * @param socket the Socket to which synthesized wave bytes will be sent
     */
    public SocketAudioPlayer(Socket socket) {
	this.socket = socket;
	try {
	    this.dataOutputStream = new DataOutputStream
		(socket.getOutputStream());
            debug = Utilities.getBoolean("debug");
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	}
    }


    /**
     * Sets the audio format to use for the next set of outputs. Since
     * an audio player can be shared by a number of voices, and since
     * voices can have different AudioFormats (sample rates for
     * example), it is necessary to allow clients to dynamically set
     * the audio format for the player.
     *
     * @param format the audio format
     */
    public void setAudioFormat(AudioFormat format) {
	this.audioFormat = format;
    }


    /**
     * Retrieves the audio format for this player
     *
     * @return the current audio format
     *
     */
    public AudioFormat getAudioFormat() {
	return this.audioFormat;
    }


    /**
     * Pauses all audio output on this player. Play can be resumed
     * with a call to resume. Not implemented in this Player.
     */
    public void pause() {}


    /**
     * Resumes audio output on this player. Not implemented in this Player.
     */
    public void resume() {}


    /**
     * Prepares for another batch of output. Larger groups of output
     * (such as all output associated with a single FreeTTSSpeakable)
     * should be grouped between a reset/drain pair.
     */
    public void reset() {}


    /**
     * Flushes all the audio data to the Socket.
     *
     * @return <code>true</code> all the time
     */
    public boolean drain() {
	try {
	    dataOutputStream.flush();
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	}
	return true;
    }


    /**
     *  Starts the output of a set of data. Audio data for a single
     *  utterance should be grouped between begin/end pairs.
     *
     * @param size the size of data in bytes to be output before
     *    <code>end</code> is called.
     */
    public void begin(int size) {
	try {
	    bytesToPlay = size;
	    firstByteSent = false;
	    dataOutputStream.writeBytes(String.valueOf(size) + "\n");
	    dataOutputStream.flush();
	    if (debug) {
		System.out.println("begin: " + size);
	    }
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	}
    }


    /**
     * Starts the first sample timer (none in this player)
     */
    public void startFirstSampleTimer() {
    }


    /**
     *  Signals the end of a set of data. Audio data for a single 
     *  utterance should be groupd between <code> begin/end </code> pairs.
     *
     *  @return <code>true</code> if the audio was output properly, 
     *          <code> false</code> if the output was cancelled 
     *          or interrupted.
     *
     */
    public boolean end() {
	if (debug) {
	    System.out.println("end");
	}
	if (bytesPlayed < bytesToPlay) {
	    int bytesNotPlayed = bytesToPlay - bytesPlayed;
	    write(new byte[bytesNotPlayed], 0, bytesNotPlayed);
	}

	bytesToPlay = 0;
	bytesPlayed = 0;
	return true;
    }
    
    
    /**
     * Cancels all queued output. All 'write' calls until the next
     * reset will return false. Not implemented in this Player.
     *
     */
    public void cancel() {}

    
    /**
     * Waits for all audio playback to stop, and closes this AudioPlayer.
     * Not implemented in this Player.
     */
    public void close() {}


    /**
     * Returns the current volume. The volume is specified as a number
     * between 0.0 and 1.0, where 1.0 is the maximum volume and 0.0 is
     * the minimum volume. Not implemented in this Player.
     *
     * @return the current volume (between 0 and 1)
     */
    public float getVolume() {
	return -1;
    }


    /**
     * Sets the current volume. The volume is specified as a number
     * between 0.0 and 1.0, where 1.0 is the maximum volume and 0.0 is
     * the minimum volume. Not implemented in this Player.
     *
     * @param volume the new volume (between 0 and 1)
     */
    public void setVolume(float volume) {}


    /**
     * Gets the amount of audio played since the last resetTime.
     * Not implemented in this Player.
     *
     * @returns the amount of audio in milliseconds
     */
    public long getTime() {
	return -1;
    }


    /**
     * Resets the audio clock. Not implemented in this Player.
     */
    public void resetTime() {}
    

    /**
     * Writes the given bytes to the audio stream
     *
     * @param audioData audio data to write to the device
     *
     * @return <code>true</code> of the write completed successfully, 
     *          <code> false </code>if the write was cancelled.
     */
    public boolean write(byte[] audioData) {
	return write(audioData, 0, audioData.length);
    }


    /**
     * Writes the given bytes to the audio stream
     *
     * @param audioData audio data to write to the device
     * @param offset the offset into the buffer
     * @param size the number of bytes to write.
     *
     * @return <code>true</code> of the write completed successfully, 
     *          <code> false </code>if the write was cancelled.
     */
    public boolean write(byte[] audioData, int offset, int size) {
	try {
	    if (!firstByteSent) {
		firstByteTime = System.currentTimeMillis();
		firstByteSent = true;
	    }
	    
	    bytesPlayed += size;
	    dataOutputStream.write(audioData, offset, size);
	    dataOutputStream.flush();

	    if (debug) {
		System.out.println("sent " + size + " bytes " +
				   audioData[0] + " " + audioData[size/2]);
	    }
	    return true;
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	    return false;
	}
    }


    /**
     * Shows metrics for this audio player. Not implemented in this Player.
     */
    public void showMetrics() {}

    
    /**
     * Returns the first byte sent time in milliseconds, the last time it
     * was recorded.
     *
     * @return the last first byte sent time in milliseconds
     */
    public long getFirstByteSentTime() {
	return firstByteTime;
    }
}

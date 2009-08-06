/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package com.sun.speech.freetts.audio;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;

import com.sun.speech.freetts.util.Utilities;


/**
 * Provides an implementation of <code>AudioPlayer</code> that sends
 * all audio data to the given file. 
 */
public class RawFileAudioPlayer implements AudioPlayer {

    private AudioFormat audioFormat;
    private float volume;
    private BufferedOutputStream os;
    private String path;
    
    /**
     * Creates a default audio player for an AudioFileFormat of type
     * WAVE.  Reads the "com.sun.speech.freetts.AudioPlayer.baseName"
     * property for the base filename to use, and will produce files
     * of the form &lt;baseName>.raw.  The default value for the
     * base name is "freetts".
     */
    public RawFileAudioPlayer() throws IOException {
        this(Utilities.getProperty(
                 "com.sun.speech.freetts.AudioPlayer.baseName", "freetts")
             + ".raw");
    }
    
    /**
     * Constructs a NullAudioPlayer
     */
    public RawFileAudioPlayer(String path) throws IOException {
        this.path = path;
	os = new BufferedOutputStream(new FileOutputStream(path));
    }
    

    /**
     * Sets the audio format for this player
     *
     * @param format the audio format
     */
    public void setAudioFormat(AudioFormat format) {
	this.audioFormat = format;
    }

    /**
     * Retrieves the audio format for this player
     *
     * @return the current audio format.
     */
    public AudioFormat getAudioFormat() {
	return audioFormat;
    }

    /**
     * Cancels all queued output. Current 'write' call will return
     * false
     *
     */
    public void cancel() {
    }


    /**
     * Pauses the audio output
     */
    public void pause() {
    }


    /**
     * Prepares for another batch of output. Larger groups of output
     * (such as all output associated with a single FreeTTSSpeakable)
     * should be grouped between a reset/drain pair.
     */
    public void reset() {
    }


    /**
     * Resumes audio output
     */
    public void resume() {
    }
	



    /**
     * Waits for all audio playback to stop, and closes this AudioPlayer.
     */
    public void close() throws IOException {
        os.flush();
        os.close();
        System.out.println("Wrote synthesized speech to " + path);
    }
        

    /**
     * Returns the current volume.
     *
     * @return the current volume (between 0 and 1)
     */
    public float getVolume() {
	return volume;
    }	      

    /**
     * Sets the current volume.
     *
     * @param volume  the current volume (between 0 and 1)
     */
    public void setVolume(float volume) {
	this.volume = volume;
    }	      


    /**
     * {@inheritDoc}
     */
    public boolean write(byte[] audioData) throws IOException {
	return write(audioData, 0, audioData.length);
    }


    /**
     *  Starts the output of a set of data
     *
     * @param size the size of data between now and the end
     *
     */
    public void begin(int size) {
    }

    /**
     *  Marks the end of a set of data
     *
     */
    public boolean  end()  {
	return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean write(byte[] bytes, int offset, int size)
        throws IOException {
        os.write(bytes, offset, size);
        return true;
    }

    /**
     * Starts the first sample timer
     */
    public void startFirstSampleTimer() {
    }

    /**
     * Waits for all queued audio to be played
     *
     * @return <code>true</code> if the audio played to completion,
     *     	<code> false </code>if the audio was stopped
     */
    public boolean drain()  {
	return true;
    }

    /**
     * Gets the amount of played since the last resetTime
     * Currently not supported.
     *
     * @return the amount of audio in milliseconds
     */
    public long getTime()  {
	return -1L;
    }


    /**
     * Resets the audio clock
     */
    public void resetTime() {
    }

    /**
     * Shows metrics for this audio player
     */
    public void showMetrics() {
    }
}

/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package com.sun.speech.freetts.audio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import com.sun.speech.freetts.util.Utilities;

/**
 * Streams audio to a file. 
 *
 *
 */
public class SingleFileAudioPlayer implements AudioPlayer {
    /** Logger instance. */
    private static final Logger LOGGER =
        Logger.getLogger(SingleFileAudioPlayer.class.getName());

    private AudioFormat currentFormat = null;
    private String baseName;
    private byte[] outputData;
    private int curIndex = 0;
    private int totBytes = 0;
    private AudioFileFormat.Type outputType;
    private Vector<InputStream> outputList;


    /**
     * Constructs a FileAudioPlayer 
     *
     * @param baseName the base name of the audio file
     * @param type the type of audio output
     *
     */
    public SingleFileAudioPlayer(String baseName, AudioFileFormat.Type type) {
	this.baseName = baseName + "." + type.getExtension();
	this.outputType = type;

	outputList = new Vector<InputStream>();
    }

    /**
     * Creates a default audio player for an AudioFileFormat of type
     * WAVE.  Reads the "com.sun.speech.freetts.AudioPlayer.baseName"
     * property for the base filename to use, and will produce a file
     * of the form &lt;baseName>.wav.  The default value for the
     * base name is "freetts".
     */
    public SingleFileAudioPlayer() {
        this(Utilities.getProperty(
                 "com.sun.speech.freetts.AudioPlayer.baseName", "freetts"),
             AudioFileFormat.Type.WAVE);
    }

    /**
     * Sets the audio format for this player
     *
     * @param format the audio format
     *
     * @throws UnsupportedOperationException if the line cannot be opened with
     *     the given format
     */
    public synchronized void setAudioFormat(AudioFormat format) {
	currentFormat = format;
    }


    /**
     * Gets the audio format for this player
     *
     * @return format the audio format
     */
    public AudioFormat getAudioFormat() {
	return currentFormat;
    }


    /**
     * Pauses audio output
     */
    public void pause() {
    }

    /**
     * Resumes audio output
     */
    public synchronized void resume() {
    }
	


    /**
     * Cancels currently playing audio
     */
    public synchronized void cancel() {
    }

    /**
     * Prepares for another batch of output. Larger groups of output
     * (such as all output associated with a single FreeTTSSpeakable)
     * should be grouped between a reset/drain pair.
     */
    public synchronized void reset() {
    }


    /**
     * Starts the first sample timer
     */
    public void startFirstSampleTimer() {
    }

    /**
     * Closes this audio player
     */
    public synchronized void close() throws IOException {
	try {
	    File file = new File(baseName);
	    InputStream is = new SequenceInputStream(outputList.elements());
	    AudioInputStream ais = new AudioInputStream(is,
		    currentFormat, totBytes / currentFormat.getFrameSize());
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Avail " + ais.available());
                LOGGER.fine("totBytes " + totBytes);
                LOGGER.fine("FS " + currentFormat.getFrameSize());
            }
            LOGGER.info("Wrote synthesized speech to " + baseName);
	    AudioSystem.write(ais, outputType, file);
	} catch (IllegalArgumentException iae) {
	    throw new IOException("Can't write audio type " + outputType, iae);
	}
    }


    /**
     * Returns the current volume.
     *
     * @return the current volume (between 0 and 1)
     */
    public float getVolume() {
	return 1.0f;
    }	      

    /**
     * Sets the current volume.
     *
     * @param volume  the current volume (between 0 and 1)
     */
    public void setVolume(float volume) {
    }	      




    /**
     *  Starts the output of a set of data. Audio data for a single
     *  utterance should be grouped between begin/end pairs.
     *
     * @param size the size of data between now and the end
     */
    public void begin(int size) {
	outputData = new byte[size];
	curIndex = 0;
    }

    /**
     *  Marks the end of a set of data. Audio data for a single 
     *  utterance should be groupd between begin/end pairs.
     *
     *  @return true if the audio was output properly, false if the
     *      output was cancelled or interrupted.
     *
     */
    public boolean  end()  {
	outputList.add(new ByteArrayInputStream(outputData));
	totBytes += outputData.length;
	return true;
    }


    /**
     * Waits for all queued audio to be played
     *
     * @return true if the audio played to completion, false if
     *   the audio was stopped
     */
    public boolean drain()  {
	return true;
    }

    /**
     * Gets the amount of played since the last mark
     *
     * @return the amount of audio in milliseconds
     */
    public synchronized long getTime()  {
	return -1L;
    }


    /**
     * Resets the audio clock
     */
    public synchronized void resetTime() {
    }
    

    
    /**
     * Writes the given bytes to the audio stream
     *
     * @param audioData audio data to write to the device
     *
     * @return <code>true</code> of the write completed successfully, 
     *       	<code> false </code>if the write was cancelled.
     */
    public boolean write(byte[] audioData) {
	return write(audioData, 0, audioData.length);
    }
    
    /**
     * Writes the given bytes to the audio stream
     *
     * @param bytes audio data to write to the device
     * @param offset the offset into the buffer
     * @param size the size into the buffer
     *
     * @return <code>true</code> of the write completed successfully, 
     *       	<code> false </code>if the write was cancelled.
     */
    public boolean write(byte[] bytes, int offset, int size) {
	System.arraycopy(bytes, offset, outputData, curIndex, size);
	curIndex += size;
	return true;
    }

    /**
     * Returns the name of this audioplayer
     *
     * @return the name of the audio player
     */
    public String toString() {
	return "FileAudioPlayer";
    }

    /**
     * Shows metrics for this audio player
     */
    public void showMetrics() {
    }
}

/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package com.sun.speech.freetts.audio;

import com.sun.speech.freetts.util.BulkTimer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;

/**
 * Provides an implementation of <code>AudioPlayer</code> that creates
 * javax.sound.sampled audio clips and outputs them via the
 * javax.sound API.  The interface provides a highly reliable audio
 * output package. Since audio is batched and not sent to the audio
 * layer until an entire utterance has been processed, this player has
 * higher latency (50 msecs for a typical 4 second utterance).
 *
 * The system property:
 * <code>
	com.sun.speech.freetts.audio.AudioPlayer.debug;
 * </code> if set to <code>true</code> will cause this
 * class to emit debugging information (useful to a developer).
 */
public class JavaClipAudioPlayer implements AudioPlayer {
    
    private volatile boolean paused;
    private volatile boolean done = false;
    private volatile boolean cancelled = false;
    private volatile Clip currentClip;

    private float volume = 1.0f;  // the current volume
    private boolean debug = false;
    private BulkTimer timer = new BulkTimer();
    private AudioFormat defaultFormat = // default format is 8khz
	new AudioFormat(8000f, 16, 1, true, true);
    private AudioFormat currentFormat = defaultFormat;
    private boolean firstSample = true;
    private int curIndex = 0;
    private byte[] outputData;
    private LineListener lineListener = new JavaClipLineListener();

    /**
     * Constructs a default JavaClipAudioPlayer 
     */
    public JavaClipAudioPlayer() {
	debug = Boolean.getBoolean
	    ("com.sun.speech.freetts.audio.AudioPlayer.debug");
	setPaused(false);
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
     * Retrieves the audio format for this player
     *
     * @return format the audio format
     */
    public AudioFormat getAudioFormat() {
	return currentFormat;
    }



    /**
     * Pauses audio output.   All audio output is 
     * stopped. Output can be resumed at the
     * current point by calling <code>resume</code>. Output can be
     * aborted by calling <code> cancel </code>
     */
    public synchronized void pause() {
	if (!isPaused()) {
	    setPaused(true);
	    Clip clip = currentClip;
	    if (clip != null) {
	        clip.stop();
	    }
	}
    }

    /**
     * Resumes playing audio after a pause.
     *
     */
    public synchronized void resume() {
	if (isPaused()) {
	    setPaused(false);
	    Clip clip = currentClip;
	    if (clip != null) {
		clip.start();
	    }
	    notify();
	}
    }
	
    /**
     * Cancels all queued audio. Any 'write' in process will return
     * immediately false.
     */
    public synchronized void cancel() {
	cancelled = true;
	Clip clip = currentClip;
	if (clip != null) {
	    clip.close();
	}
    }

    /**
     * Prepares for another batch of output. Larger groups of output
     * (such as all output associated with a single FreeTTSSpeakable)
     * should be grouped between a reset/drain pair.
     */
    public synchronized void reset() {
	timer.start("speakableOut");
    }

    /**
     * Waits for all queued audio to be played
     *
     * @return <code>true</code> if the write completed successfully, 
     *       	<code> false </code>if the write was cancelled.
     */
    public boolean drain()  {
	timer.stop("speakableOut");
	return true;
    }

    /**
     * Closes this audio player
     */
    public synchronized void close() {
	done = true;
	notify();
    }
        

    /**
     * Returns the current volume.
     * @return the current volume (between 0 and 1)
     */
    public float getVolume() {
	return volume;
    }	      

    /**
     * Sets the current volume.
     * @param volume  the current volume (between 0 and 1)
     */
    public void setVolume(float volume) {
	if (volume > 1.0f) {
	    volume = 1.0f;
	}
	if (volume < 0.0f) {
	    volume = 0.0f;
	}
	this.volume = volume;
	Clip clip = currentClip;
	if (clip != null) {
	    setVolume(clip, volume);
	}
    }	      


    /**
     * Sets pause mode
     * @param state true if we are paused
     */
    private void setPaused(boolean state) {
	paused = state;
    }

    /**
     * Returns true if we are in pause mode
     *
     * @return <code> true </code>if paused; 
     *		otherwise returns <code>false</code>
     */
    private boolean isPaused() {
	return paused;
    }

    /**
     * Sets the volume on the given clip
     *
     * @param line the line to set the volume on
     * @param vol the volume (range 0 to 1)
     */
    private void setVolume(Clip clip, float vol) {
	if (clip.isControlSupported (FloatControl.Type.MASTER_GAIN)) {
	    FloatControl volumeControl = 
		(FloatControl) clip.getControl (FloatControl.Type.MASTER_GAIN);
	    float range = volumeControl.getMaximum() -
			  volumeControl.getMinimum();
	    volumeControl.setValue(vol * range + volumeControl.getMinimum());
	}
    }




    /**
     * Returns the current position in the output stream since the
     * last <code>resetTime</code> 
     *
     * Currently not supported.
     *
     * @return the position in the audio stream in milliseconds
     *
     */
    public synchronized long getTime()  {
        return -1L;
    }


    /**
     * Resets the time for this audio stream to zero
     */
    public synchronized void resetTime() {
    }
    

    /**
     *  Starts the output of a set of data. Audio data for a single
     *  utterance should be grouped between begin/end pairs.
     *
     * @param size the size of data between now and the end
     *
     */
    public void begin(int size) {
	cancelled = false;
	timer.start("utteranceOutput");
	curIndex = 0;
	outputData = new byte[size];
    }

    /**
     *  Marks the end a set of data. Audio data for a single utterance should
     *  be grouped between begin/end pairs.
     *
     *  @return <code>true</code> if the audio was output properly, 
     * 		<code>false </code> if the output was cancelled 
     *		or interrupted.
     */
    public synchronized boolean  end()  {
	boolean ok = true;

	while (!done && !cancelled && isPaused()) {
	    try { 
		wait();
	    } catch (InterruptedException ie) {
		return false;
	    }
	}

	if (done || cancelled) {
	    cancelled = false;
	    return false;
	}

	timer.start("clipGeneration");
	try {
	    DataLine.Info info = new DataLine.Info(Clip.class, currentFormat);
	    Clip clip = (Clip) AudioSystem.getLine(info);

	    clip.addLineListener(lineListener);
	    clip.open(currentFormat, outputData, 0, outputData.length);
	    setVolume(clip, volume);

	    if (currentClip != null) {
		throw new IllegalStateException("clip already set");
	    }
	    currentClip = clip;
	    clip.start();

	    try {
		while (currentClip != null) {
		    wait();	// drain does not work 
		}
	    } catch (InterruptedException ie) {
		ok = false;
	    }
	} catch (LineUnavailableException lue) {
	    System.err.println("Line unavailable");
	    System.err.println("Format is " + currentFormat);
	    ok = false;
	}

	timer.stop("clipGeneration");
	timer.stop("utteranceOutput");
	ok = !cancelled;
	cancelled = false;
	return ok;
    }

    
    /**
     * Writes the given bytes to the audio stream
     *
     * @param audioData audio data to write to the device
     *
     * @return <code>true</code> if the write completed successfully, 
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
     * @return <code>true</code> if the write completed successfully, 
     *       	<code> false </code>if the write was cancelled.
     */
    public boolean write(byte[] bytes, int offset, int size) {

	if (firstSample) {
	    firstSample = false;
	    timer.stop("firstAudio");
	}
	System.arraycopy(bytes, offset, outputData, curIndex, size);
	curIndex += size;
	return true;
    }


    /**
     * Returns the name of this audio player
     *
     * @return the name of the audio player
     */
    public String toString() {
	return "JavaClipAudioPlayer";
    }


    /**
     * Outputs the given msg if debugging is enabled for this
     * audio player.
     */
    private void debugPrint(String msg) {
	if (debug) {
	    System.out.println(toString() + ": " + msg);
	}
    }

    /**
     * Shows metrics for this audio player
     */
    public void showMetrics() {
	timer.show(toString());
    }

    /**
     * Starts the first sample timer
     */
    public void startFirstSampleTimer() {
	timer.start("firstAudio");
	firstSample = true;
    }


    /**
     * Provides a LineListener for this clas.
     */
    private class JavaClipLineListener implements LineListener {


	/**
	 * Implements update() method of LineListener interface. Responds
	 * to the line events as appropriate.
	 *
	 * @param event the LineEvent to handle
	 */
	public void update(LineEvent event) {
	    if (event.getType().equals(LineEvent.Type.START)) {
		debugPrint("Event  START");
	    } else if (event.getType().equals(LineEvent.Type.STOP)) {
		debugPrint("Event  STOP");
		// A line may be stopped for three reasons:
		//    the clip has finished playing
		//    the clip has been paused
		//    the clip has been cancelled (i.e. closed)
		// if the clip has stopped because it has finished
		// playing, then we should close the clip, otherwise leave
		// it alone
		synchronized(JavaClipAudioPlayer.this) {
		    if (!cancelled && !isPaused()) {
			currentClip.close();
		    }
		}
	    }  else if (event.getType().equals(LineEvent.Type.OPEN)) {
		debugPrint("Event OPEN");
	    }  else if (event.getType().equals(LineEvent.Type.CLOSE)) {
		// When a clip is closed we no longer need it, so 
		// set currentClip to null and notify anyone who may
		// be waiting on it.
		debugPrint("EVNT CLOSE");
		synchronized (JavaClipAudioPlayer.this) {
		    currentClip = null;
		    JavaClipAudioPlayer.this.notify();
		}
	    }
	}
    }
}

/**
 * Copyright 2003 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package com.sun.speech.freetts.jsapi;

import java.beans.PropertyVetoException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import javax.speech.EngineException;
import javax.speech.EngineStateError;
import javax.speech.synthesis.SynthesizerModeDesc;

import com.sun.speech.engine.BaseEngineProperties;
import com.sun.speech.engine.synthesis.BaseSynthesizer;
import com.sun.speech.engine.synthesis.BaseSynthesizerProperties;
import com.sun.speech.engine.synthesis.BaseSynthesizerQueueItem;
import com.sun.speech.engine.synthesis.BaseVoice;
import com.sun.speech.freetts.OutputQueue;
import com.sun.speech.freetts.audio.AudioPlayer;

/**
 * Provides  partial support for a JSAPI 1.0 synthesizer for the 
 * FreeTTS speech synthesis system.
 */
public class FreeTTSSynthesizer extends BaseSynthesizer {
    /** Logger instance. */
    private static final Logger LOGGER =
        Logger.getLogger(FreeTTSSynthesizer.class.getName());

    /**
     * Reference to output thread.
     */
    OutputHandler outputHandler;

    /**
     * The currently active voice for this synthesizer
     */
    private FreeTTSVoice curVoice;

    private AudioPlayer audio;

    /**
     * All voice output for this synthesizer goes through
     * this central utterance queue
     */
    private OutputQueue outputQueue;

    /**
     * Creates a new Synthesizer in the DEALLOCATED state.
     *
     * @param desc describes the allowed mode of operations for this
     * 		synthesizer.
     */
    public FreeTTSSynthesizer(FreeTTSSynthesizerModeDesc desc) {
        super(desc);
        outputHandler = new OutputHandler();

    }

    /**
     * Starts the output thread. The output thread is responsible for
     * taking items off of the queue and sending them to the audio
     * player.
     *
     * @throws EngineException if an allocation error occurs
     */
    protected void handleAllocate() throws EngineException {
        long states[];
	boolean ok = false;
	FreeTTSSynthesizerModeDesc desc = (FreeTTSSynthesizerModeDesc)
	    getEngineModeDesc();


	outputQueue = com.sun.speech.freetts.Voice.createOutputThread();

	if (desc.getVoices().length > 0) {
	    FreeTTSVoice freettsVoice = (FreeTTSVoice) desc.getVoices()[0];
	    ok = setCurrentVoice(freettsVoice);
	}



	if (ok) {
	    synchronized (engineStateLock) {
		long newState = ALLOCATED | RESUMED;
		newState |= (outputHandler.isQueueEmpty()
			     ? QUEUE_EMPTY
			     : QUEUE_NOT_EMPTY);
		states = setEngineState(CLEAR_ALL_STATE, newState);
	    }
	    outputHandler.start();
	    postEngineAllocated(states[0], states[1]);
	} else {
	    throw new EngineException("Can't allocate FreeTTS synthesizer");
	}
    }



    /**
     * Sets the given voice to be the current voice. If
     * the voice cannot be loaded, this call has no affect.
     *
     * @param voice the new voice.
     */
    private boolean setCurrentVoice(FreeTTSVoice voice) 
            throws EngineException {

	com.sun.speech.freetts.Voice freettsVoice = voice.getVoice();
	boolean ok = false;


	if (!freettsVoice.isLoaded()) {
	    freettsVoice.setOutputQueue(outputQueue);
	    freettsVoice.allocate();
            audio = freettsVoice.getAudioPlayer();
            if (audio == null) {
                audio = new com.sun.speech.freetts.audio.JavaClipAudioPlayer();
            }
            if (audio == null) {
                throw new EngineException("Can't get audio player");
            }
	    freettsVoice.setAudioPlayer(audio);
	}

	if (freettsVoice.isLoaded()) {
	    curVoice = voice;
	    ok = true;
	    // notify the world of potential property changes
	    FreeTTSSynthesizerProperties props =
		(FreeTTSSynthesizerProperties) getSynthesizerProperties();
	    props.checkForPropertyChanges();
	}
	return ok;
    }

    /**
     * Handles a deallocation request. Cancels all pending items,
     * terminates the output handler, and posts the state changes.
     *
     * @throws EngineException if a deallocation error occurs
     */
    protected void handleDeallocate() throws EngineException {
        long[] states = setEngineState(CLEAR_ALL_STATE, DEALLOCATED);
        outputHandler.cancelAllItems();
        outputHandler.terminate();

	// Close the audio. This should flush out any queued audio data

	if (audio != null) {
	    audio.close();
	}

        outputQueue.close();
        
        postEngineDeallocated(states[0], states[1]);
    }
    
    /**
     * Factory method to create a BaseSynthesizerQueueItem.
     *
     * @return a queue item appropriate for this synthesizer
     */
    protected BaseSynthesizerQueueItem createQueueItem() {
        return new FreeTTSSynthesizerQueueItem();
    }

    /**
     * Returns an enumeration of the queue.
     *
     * @return an enumeration of the contents of the queue. This
     * 		enumeration contains FreeTTSSynthesizerQueueItem objects
     *
     * @throws EngineStateError if the engine was not in the proper
     * 				state
     */
    public Enumeration enumerateQueue() throws EngineStateError {
        checkEngineState(DEALLOCATED | DEALLOCATING_RESOURCES);
        return outputHandler.enumerateQueue();
    }

    /**
     * Places an item on the speaking queue and send the queue update event.
     *
     * @param item	the item to place  in the queue
     */
    protected void appendQueue(BaseSynthesizerQueueItem item) {
        outputHandler.appendQueue((FreeTTSSynthesizerQueueItem) item);
    }

    /**
     * Cancels the item at the top of the queue.
     *
     * @throws EngineStateError if the synthesizer is not in the
     * 				proper state
     */
    public void cancel() throws EngineStateError {
        checkEngineState(DEALLOCATED | DEALLOCATING_RESOURCES);
        outputHandler.cancelItem();
    }

    /**
     * Cancels a specific object on the queue.
     * 
     * @param source the object to cancel
     *
     * @throws IllegalArgumentException if the source object is not
     * 					currently in the queue
     * @throws EngineStateError		the synthesizer is not in the
     * 					proper state
     */
    public void cancel(Object source)
        throws IllegalArgumentException, EngineStateError {
        checkEngineState(DEALLOCATED | DEALLOCATING_RESOURCES);
        outputHandler.cancelItem(source);
    }

    /**
     * Cancels all items on the output queue.
     *
     * @throws EngineStateError
     */
    public void cancelAll() throws EngineStateError {
        checkEngineState(DEALLOCATED | DEALLOCATING_RESOURCES);
        outputHandler.cancelAllItems();
    }

    /**
     * Pauses the output
     */
    protected void handlePause() {
	audio.pause();
    }    

    /**
     * Resumes the output
     */
    protected void handleResume() {
	audio.resume();
    }

    /**
     * Factory constructor for EngineProperties object.
     * Gets the default speaking voice from the SynthesizerModeDesc.
     * Takes the default prosody values (pitch, range, volume, rate)
     * from the default voice.
     * Override to set engine-specific defaults.
     */
    protected BaseEngineProperties createEngineProperties() {
        SynthesizerModeDesc desc = (SynthesizerModeDesc)engineModeDesc;
        FreeTTSVoice defaultVoice = (FreeTTSVoice)(desc.getVoices()[0]);
        return new FreeTTSSynthesizerProperties(defaultVoice,
			 defaultVoice.getPitch(),
			 defaultVoice.getPitchRange(),
			 defaultVoice.getSpeakingRate(),
			 defaultVoice.getVolume());
    }

    /**
     * Manages the FreeTTS synthesizer properties
     */
     class FreeTTSSynthesizerProperties extends BaseSynthesizerProperties {

	 /**
	  * Constructor 
	  * 
	  * @param defaultVoice the voice to use as the default for
	  * 			this synthesizer
	  * @param defaultPitch the default pitch in hertz
	  * @param defaultPitchRange the default range of pitch in
	  * 			hertz
	  * @param defaultSpeakingRate the default speaking rate in
	  * 			words per minute
	  * @param defaultVolume the default speaking volume 
	  *			(0.0 to 1.0)
	  */
	 FreeTTSSynthesizerProperties(
		 BaseVoice defaultVoice,
		 float defaultPitch,
		 float defaultPitchRange,
		 float defaultSpeakingRate,
		 float defaultVolume) {

	     super(defaultVoice, defaultPitch, defaultPitchRange, 
		     defaultSpeakingRate, defaultVolume);
	 }

	/**
	 * Resets the properties to their default values
	 */
	public void reset() {
	    super.reset();
	}

	/**
	 * Checks to see if any properties have changed and if so
	 * fires the proper events
	 */
	void checkForPropertyChanges() {
	    try {
		float pitch = getPitch();
		if (pitch != currentPitch) {
		    super.setPitch(pitch);
		}
		
		float pitchRange = getPitchRange();
		if (pitchRange != currentPitchRange) {
		    super.setPitchRange(pitchRange);
		}

		float volume = getVolume();
		if (volume != currentVolume) {
		    super.setVolume(volume);
		}

		float speakingRate = getSpeakingRate();
		if (speakingRate != currentSpeakingRate) {
		    super.setSpeakingRate(speakingRate);
		}

	    } catch (PropertyVetoException pve) {
		// the actual properties in the voices have
		// already changed to these new values so 
		// we should not expect a PropertyVetoException
	    }
	}

	/**
	 * Get the baseline pitch for synthesis
	 *
	 * @return the current pitch (in hertz)
	 */
	public float getPitch() {
	    com.sun.speech.freetts.Voice voice = curVoice.getVoice();
	    return voice.getPitch();
	}

	/**
	 * Sets the voice to a voice that matches the given voice
	 *
	 * @param voice the voice that matches it
	 */
	public void setVoice(javax.speech.synthesis.Voice voice) {
	    if (!curVoice.match(voice)) {
		// chase through the voice list and find the first match
		// and use that.  If no match, just ignore it.
		FreeTTSSynthesizerModeDesc desc =
		    (FreeTTSSynthesizerModeDesc) getEngineModeDesc();
		javax.speech.synthesis.Voice voices[]  = desc.getVoices();
		for (int i = 0; i < voices.length; i++) {
		    if (voices[i].match(voice)) {
                        try {
                            if (setCurrentVoice((FreeTTSVoice) voices[i])) {
                                try {
                                    super.setVoice(voice);
                                    break;
                                } catch (PropertyVetoException pve) {
                                    continue;
                                }
                            }
                        } catch (EngineException ee) {
                            System.err.println("Engine Exception: " +
                                    ee.getMessage());
                        }
		    }
		}
	    }
	}

	/**
	 * Set the baseline pitch for the current synthesis voice.
	 *
	 * @param hertz sets the current pitch
	 *
	 * @throws PropertyVetoException if the synthesizer rejects or
	 * 	limits the new value
	 */
	public void setPitch(float hertz) throws PropertyVetoException {
	    if (hertz != getPitch()) {
	    	com.sun.speech.freetts.Voice voice = curVoice.getVoice();
		voice.setPitch(hertz);
		super.setPitch(hertz);
	    }
	}


	/**
	 * Get the pitch range for synthesis.
	 *
	 * @return the current range of pitch in hertz
	 */
	public float getPitchRange() {
	    com.sun.speech.freetts.Voice voice = curVoice.getVoice();
	    return voice.getPitchRange();
	}

	/**
	 * Set the pitch range for the current synthesis voice.
	 *
	 * @throws PropertyVetoException if the synthesizer rejects or
	 * 	limits the new value
	 */
	public void setPitchRange(float hertz) throws PropertyVetoException {
	    if (hertz != getPitchRange()) {
		com.sun.speech.freetts.Voice voice = curVoice.getVoice();
		voice.setPitchRange(hertz);
		super.setPitchRange(hertz);
	    }
	}

	/**
	 * Gets the current target speaking rate.  
	 *
	 * @return the current speaking rate in words per minute
	 */
	public float getSpeakingRate() {
	    com.sun.speech.freetts.Voice voice = curVoice.getVoice();
	    return voice.getRate();
	}

	/**
	 * Set the target speaking rate.
	 *
	 * @param wpm sets the target speaking rate in 
	 *	words per minute
	 *
	 * @throws PropertyVetoException if the synthesizer rejects or
	 * 				limits the new value
	 */
	public void setSpeakingRate(float wpm) throws PropertyVetoException {
	    if (wpm != getSpeakingRate()) {
		com.sun.speech.freetts.Voice voice = curVoice.getVoice();
		voice.setRate(wpm);
		super.setSpeakingRate(wpm);
	    }
	}

	/**
	 * Gets the current volume.  
	 *
	 * @return the current volume setting (between 0 and 1.0)
	 */
	public float getVolume() {
	    com.sun.speech.freetts.Voice voice = curVoice.getVoice();
	    return voice.getVolume();
	}

	/**
	 * Sets the volume
	 *
	 * @param volume the new volume setting (between 0 and 1)
	 *
	 * @throws PropertyVetoException if the synthesizer rejects or
	 * 	limits the new value
	 */
	public void setVolume(float volume) throws PropertyVetoException {
	    if (volume > 1.0f)
		volume = 1.0f;
	    else if (volume < 0.0f)
		volume = 0.0f;
	
	    if (volume != getVolume()) {
		com.sun.speech.freetts.Voice voice = curVoice.getVoice();
		voice.setVolume(volume);
		super.setVolume(volume);
	    }
	}
     }


    /**
     * The OutputHandler is responsible for taking items off of the
     * input queue and sending them to the current voice.
     */
    class OutputHandler extends Thread {
        protected boolean done = false;
        
        /**
         * Internal speech output queue that will contain a set of 
         * FreeTTSSynthesizerQueueItems.
         *
         * @see BaseSynthesizerQueueItem
         */
        protected Vector queue;

        /**
         * Create a new OutputHandler for the given Synthesizer.
         */
        public OutputHandler() {
            queue = new Vector();
        }

        /**
         * shuts down this output handler
         */
        public synchronized void terminate() {
	    synchronized (queue) {
		done = true;
		queue.notify();
	    }
        }
        
        /**
         * Returns an enumeration of the queue
	 *
	 * @return the enumeration queue
         */
        public Enumeration enumerateQueue() {
            synchronized(queue) {
                return queue.elements();
            }
        }

        /**
         * Determines if the input queue is empty
	 *
	 * @return true if the queue is empty; otherwise false
         */
        public boolean isQueueEmpty() {
            synchronized(queue) {
                return queue.size() == 0;
            }
        }
        
        /**
         * Add an item to be spoken to the output queue. Fires the
	 * appropriate queue events
	 *
	 * @param item the item to add to the queue
         */
        public void appendQueue(FreeTTSSynthesizerQueueItem item) {
            boolean topOfQueueChanged;
            synchronized(queue) {
                topOfQueueChanged = (queue.size() == 0);
                queue.addElement(item);
                queue.notifyAll();
            }            
            if (topOfQueueChanged) {
                long[] states = setEngineState(QUEUE_EMPTY,
                                               QUEUE_NOT_EMPTY);
                postQueueUpdated(topOfQueueChanged, states[0], states[1]);
            }
        }

        /**
         * Cancel the current item
         */
        protected void cancelItem() {
	    FreeTTSSynthesizerQueueItem item = null;

	    synchronized(queue) {
	        audio.cancel();
		if (queue.size() != 0) {
		    item = (FreeTTSSynthesizerQueueItem) queue.remove(0);
		    if (item != null) {
			// item.postSpeakableCancelled();
			item.cancelled();
                        queueDrained();
		    }
		}
	    }
        }
        
        /**
         * Cancel all items in the queue
         */
        protected void cancelAllItems() {
	    FreeTTSSynthesizerQueueItem item = null;
	    Vector copy;

	    synchronized(queue) {
	        audio.cancel();
	    	copy = (Vector) queue.clone();
		queue.clear();
		queueDrained();
	    }
	    for (Iterator i = copy.iterator(); i.hasNext(); ) {
		item = (FreeTTSSynthesizerQueueItem) i.next();
		// item.postSpeakableCancelled();
                item.cancelled();
	    }
        }
        
            
        /**
         * Cancel the given item.
	 *
	 * @param source the item to cancel.
         */
        protected void cancelItem(Object source) {
	    FreeTTSSynthesizerQueueItem item = null;
	    synchronized(queue) {
		int index = queue.indexOf(source);
		if (index == 0) {
		    cancelItem();
		} else {
		    item = (FreeTTSSynthesizerQueueItem) queue.remove(index);
		    if (item != null) {
			// item.postSpeakableCancelled();
			item.cancelled();
                        queueDrained();
		    }
		}
	    }
        }

        /**
         * Gets the next item from the queue and outputs it
         */
        public void run() {
            FreeTTSSynthesizerQueueItem item;
            while (!done) {
                item = getQueueItem();
		if (item != null) {
		    outputItem(item);
		    removeQueueItem(item); 
		}
            }
        }

        /**
         * Return, but do not remove, the first item on the queue.
	 *
	 * @return a queue item
         */
        protected FreeTTSSynthesizerQueueItem getQueueItem() {
	    FreeTTSSynthesizerQueueItem item = null;
            synchronized(queue) {
                while (queue.size() == 0 && !done) {
                    try {
                        queue.wait();
                    }
                    catch (InterruptedException e) {
			LOGGER.severe("Unexpected interrupt");
                        // Ignore interrupts and we'll loop around
                    }
                }

		if (done) {
		    return null;
		}
                item = (FreeTTSSynthesizerQueueItem) queue.elementAt(0);
            }
	    item.postTopOfQueue();
	    return item;
        }

        /**
         * removes the given item, posting the appropriate
	 * events. The item may have already been removed (due to a
	 * cancel).
	 *
	 * @param item the item to remove 
         */
        protected void removeQueueItem(FreeTTSSynthesizerQueueItem item) {
	    boolean queueEmptied = false;
            synchronized(queue) {
		boolean found = queue.remove(item);
		if (found) {
		    queueDrained();
		}
            }
        }

	/**
	 * Should be called iff one or more items have been removed
	 * from the queue. Generates the appropriate state changes and
	 * events.
	 */
	private void queueDrained() {
	    if (queue.size() == 0) {
		long[] states = setEngineState(QUEUE_NOT_EMPTY, QUEUE_EMPTY);
		postQueueEmptied(states[0], states[1]);
	    } else { 
		long[] states = setEngineState(QUEUE_NOT_EMPTY,
					       QUEUE_NOT_EMPTY);
		postQueueUpdated(true, states[0], states[1]);
	    }
	}

        /**
         * Outputs the given queue item to the current voice
	 *
	 * @param item the item to output
         */
        protected void outputItem(FreeTTSSynthesizerQueueItem item) {
	    com.sun.speech.freetts.Voice voice = curVoice.getVoice();
	    voice.speak(item);
        }
    }
}

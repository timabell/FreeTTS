/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
import javax.swing.ListModel;
import javax.speech.synthesis.SynthesizerModeDesc;

/**
 * Defines the data model used by the GUI of the <code>Player</code>.
 * Defines ways to control volume, speaking rate, pitch and range, etc..
 * Also gives you information such as the list of <code>Synthesizers</code>, 
 * list of <code>Voices</code>, the play list, etc., that the user interface
 * will need. Also allows you to get and play different types of
 * <code>Playable</code> objects.
 */
public interface PlayerModel {

    /**
     * Performs text-to-speech on the given Playable.
     *
     * @param Playable the Playable to perform text-to-speech
     */
    public void play(Playable Playable);


    /**
     * Performs text-to-speech on the object at the given index of
     * the play list.
     *
     * @param index the index of the Playable object on the play list
     */
    public void play(int index);


    /**
     * Returns true if the player is paused.
     *
     * @return <code>true</code> if the player is paused,
     *         <code>false</code> otherwise
     */
    public boolean isPaused();

    
    /**
     * Pauses the player.
     */
    public void pause();
    

    /**
     * Resumes the player.
     */
    public void resume();
        

    /**
     * Stops the player if it is playing.
     */
    public void stop();


    /**
     * Cancels the currently playing item.
     */
    public void cancel();


    /**
     * Sets whether the Monitor is visible.
     *
     * @param visible true to set it to visible
     */
    public void setMonitorVisible(boolean visible);


    /**
     * Tells whether the monitor is visible.
     *
     * @return true if the monitor is visible, false otherwise
     */
    public boolean isMonitorVisible();


    /**
     * Creates the list of synthesizers.
     */
    public void createSynthesizers();
     

    /**
     * Returns the monitor of the synthesizer at the given index.
     *
     * @param index the position of the synthesizer in the synthesizer list
     *
     * @return the monitor of the specified synthesizer
     */
    public Monitor getMonitor(int index);


    /**
     * Returns the monitor of the current synthesizer.
     *
     * @return the monitor of the current synthesizer
     */
    public Monitor getMonitor();


    /**
     * Sets the current monitor.
     *
     * @param monitor the current monitor
     */
    public void setMonitor(Monitor monitor);


    /**
     * Sets the Synthesizer at the given index to use
     *
     * @param index index of the synthesizer in the list
     */
    public void setSynthesizer(int index);

    
    /**
     * Sets the Voice at the given index to use.
     *
     * @param index the index of the voice in the list
     */
    public void setVoice(int index);

        
    /**
     * Sets the list of voices using the given Synthesizer mode description.
     *
     * @param modeDesc the synthesizer mode description
     */
    public void setVoiceList(SynthesizerModeDesc modeDesc);


    /**
     * Returns the volume.
     *
     * @return the volume, or -1 if unknown, or an error occurred
     */
    public float getVolume();

    
    /**
     * Sets the volume.
     *
     * @param volume set the volume of the synthesizer
     *
     * @return true if new volume is set; false otherwise
     */
    public boolean setVolume(float volume);


    /**
     * Returns the speaking rate.
     *
     * @return the speaking rate, or -1 if unknown or an error occurred
     */
    public float getSpeakingRate();

    
    /**
     * Sets the speaking rate in the number of words per minute.
     *
     * @param wordsPerMin the speaking rate
     *
     * @return true if new speaking rate is set; false otherwise
     */
    public boolean setSpeakingRate(float wordsPerMin);


    /**
     * Returns the baseline pitch for the current synthesis voice.
     *
     * @return the baseline pitch for the current synthesis voice
     */
    public float getPitch();


    /**
     * Sets the baseline pitch for the current synthesis voice.
     *
     * @param pitch the baseline pitch
     *
     * @return true if new pitch is set; false otherwise
     */
    public boolean setPitch(float pitch);

    
    /**
     * Returns the pitch range for the current synthesis voice.
     *
     * @return the pitch range for the current synthesis voice
     */
    public float getRange();


    /**
     * Sets the pitch range for the current synthesis voice.
     *
     * @param range the pitch range
     *
     * @return true if new range is set; false otherwise
     */
    public boolean setRange(float range);

         
    /**
     * Returns the play list.
     *
     * @return the play list
     */
    public ListModel getPlayList();


    /**
     * Returns the list of voices of the current synthesizer
     *
     * @return the list of voices
     */
    public ListModel getVoiceList();

    
    /**
     * Returns the list synthesizers.
     *
     * @return the synthesizer list
     */
    public ListModel getSynthesizerList();
    

    /**
     * Returns the Playable object at the given index of the play list.
     *
     * @param index the index of the Playable object on the play list
     *
     * @return the Playable object
     */
    public Object getPlayableAt(int index);


    /**
     * Adds the given Playable object to the end of the play list.
     *
     * @param Playable the Playable object to add
     */
    public void addPlayable(Playable Playable);


    /**
     * Removes the Playable at the given position from the list
     *
     * @param index the index of the Playable to remove
     */
    public void removePlayableAt(int index);


    /**
     * Closes the PlayerModel
     */
    public void close();
}

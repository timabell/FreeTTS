/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

import javax.speech.AudioException;
import javax.speech.Central;
import javax.speech.Engine;
import javax.speech.EngineException;
import javax.speech.EngineList;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.SynthesizerProperties;
import javax.speech.synthesis.Voice;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.ListModel;

import com.sun.speech.engine.synthesis.SynthesizerMonitor;

/**
 * Implements the text-to-speech data model of the Player application, using
 * JSAPI. It should work with any JSAPI implementation.
 */
public class PlayerModelImpl implements PlayerModel {

    private Synthesizer synthesizer;
    private Monitor monitor;
    private boolean monitorVisible = false;
    private boolean paused = false;
    private boolean stopped = false;
    private boolean playingFile = false;
    private DefaultListModel playList;
    private DefaultComboBoxModel synthesizerList;
    private DefaultComboBoxModel voiceList;
    private float volume = -1;
    private static boolean debug = false;
    private Set loadedSynthesizers;

    
    /**
     * Constructs a default PlayerModelImpl.
     */
    public PlayerModelImpl() {
	playList = new DefaultListModel();
	synthesizerList = new DefaultComboBoxModel();
	voiceList = new DefaultComboBoxModel();
	loadedSynthesizers = new HashSet();
    }
    

    /**
     * Creates a FreeTTS synthesizer.
     */
    public void createSynthesizers() {
	try {
	    EngineList list = Central.availableSynthesizers(null); 
	    Enumeration e = list.elements();

	    while (e.hasMoreElements()) {
		MySynthesizerModeDesc myModeDesc =
		    new MySynthesizerModeDesc
		    ((SynthesizerModeDesc) e.nextElement(), this);
		debugPrint(myModeDesc.getEngineName() + " " +
			   myModeDesc.getLocale() + " " +
			   myModeDesc.getModeName() + " " +
			   myModeDesc.getRunning());
		synthesizerList.addElement(myModeDesc);
	    }
	    
	    if (synthesizerList.getSize() > 0) {
		setSynthesizer(0);
	    } else {
		System.err.println(noSynthesizerMessage());
	    }
            if (synthesizer == null) {
                System.err.println("PlayerModelImpl: Can't find synthesizer");
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a "no synthesizer" message, and asks 
     * the user to check if the "speech.properties" file is
     * at <code>user.home</code> or <code>java.home/lib</code>.
     *
     * @return a no synthesizer message
     */
    static private String noSynthesizerMessage() {
        String message =
            "No synthesizer created.  This may be the result of any\n" +
            "number of problems.  It's typically due to a missing\n" +
            "\"speech.properties\" file that should be at either of\n" +
            "these locations: \n\n";
        message += "user.home    : " + System.getProperty("user.home") + "\n";
        message += "java.home/lib: " + System.getProperty("java.home") +
	    File.separator + "lib\n\n" +
            "Another cause of this problem might be corrupt or missing\n" +
            "voice jar files in the freetts lib directory.  This problem\n" +
            "also sometimes arises when the freetts.jar file is corrupt\n" +
            "or missing.  Sorry about that.  Please check for these\n" +
            "various conditions and then try again.\n";
        return message;
    }
	    
    /**
     * Performs TTS on the given Playable.
     *
     * @param playable the Playable object to play
     */
    public void play(Playable playable) {
	if (playable != null) {
	    if (playable.getType() == PlayableType.TEXT) {
		play(playable.getText());
	    } else if (playable.getType() == PlayableType.JSML) {
		playJSML(playable.getText());
	    } else if (playable.getType() == PlayableType.TEXT_FILE ||
		       playable.getType() == PlayableType.JSML_FILE) {
		playFile(playable.getFile(), playable.getType());
	    } else if (playable.getType() == PlayableType.URL) {
		try {
		    playURL(new URL(playable.getName()));
		} catch (MalformedURLException mue) {
		    mue.printStackTrace();
		}
	    }
	}
    }


    /**
     * Performs TTS on the object at the given index of the play list.
     *
     * @param index index of the object in the playlist to play
     */
    public void play(int index) {
	if (0 <= index && index < playList.getSize()) {
	    Playable playable = (Playable) playList.getElementAt(index);
	    if (playable != null) {
		play(playable);
	    }
	}
    }


    /**
     * Performs text-to-speech on the given text.
     *
     * @param text the text to perform TTS
     */
    private void play(String text) {
	synthesizer.speakPlainText(text, null);
    }
    

    /**
     * Performs text-to-speech on the given JSML text.
     *
     * @param text the text to perform TTS
     */
    private void playJSML(String jsmlText) {
	try {
	    synthesizer.speak(jsmlText, null);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }


    /**
     * Plays the text in the given File.
     *
     * @param file the File to play
     * @param type the file type
     */
    private void playFile(File file, PlayableType type) {
	try {
	    FileInputStream fileStream = new FileInputStream(file);
	    playInputStream(fileStream, type);
	} catch (FileNotFoundException fnfe) {
	    fnfe.printStackTrace();
	}
    }
	

    /**
     * Plays the text in the given File.
     *
     * @param file the File to play
     * @param type the file type
     */
    private void playInputStream(InputStream inStream, PlayableType type) {
	playingFile = true;
	if (inStream != null) {
	    try {
		BufferedReader reader = new BufferedReader
		    (new InputStreamReader(inStream));
		String line = "";
		if (type == PlayableType.TEXT_FILE) {
		    while (!isStopped() && 
			   (line = reader.readLine()) != null) {
			if (line.length() > 0) {
			    play(line);
			}
		    }
		} else if (type == PlayableType.JSML_FILE) {
		    String fileText = "";
		    while ((line = reader.readLine()) != null) {
			fileText += line;
		    }
		    if (fileText != null && fileText.length() > 0) {
			playJSML(fileText);
		    }
		}
		stopped = false;
	    } catch (IOException ioe) {
		ioe.printStackTrace();
	    }
	}
	playingFile = false;
    }


    /**
     * Plays the contents of the given URL.
     *
     * @param url the URL to play
     */
    private void playURL(URL url) {
	try {
	    synthesizer.speak(url, null);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
	

    /**
     * Returns true if the player is paused.
     *
     * @return true if the player is paused, false otherwise
     */
    public synchronized boolean isPaused() {
	return paused;
    }
    

    /**
     * Pauses the player.
     */
    public synchronized void pause() {
	paused = true;
	synthesizer.pause();
    }
        

    /**
     * Resumes the player.
     */
    public synchronized void resume() {
	paused = false;
	try {
	    synthesizer.resume();
	} catch (AudioException ae) {
	    ae.printStackTrace();
	}	
    }
            

    /**
     * Stops the player if it is playing.
     */
    public synchronized void stop() {
	if (playingFile) {
	    stopped = true;
	}
	synthesizer.cancelAll();
    }


    /**
     * Cancels the currently playing item.
     */
    public void cancel() {
	synthesizer.cancel();
    }

    /**
     * Close this playable
     */
    public void close() {
	for (Iterator i = loadedSynthesizers.iterator(); i.hasNext();) {
	    Synthesizer synth = (Synthesizer) i.next();
	    try {
		synth.deallocate();
	    } catch (EngineException ee) {
		System.out.println("Trouble closing the synthesizer: " + ee);
	    }
	}
    }


    /**
     * Returns true if the Player is currently being stopped.
     *
     * @return true if the Player is currently being stopped; false otherwise
     */    
    private synchronized boolean isStopped() {
	return stopped;
    }
    

    /**
     * Sets whether the Monitor is visible
     *
     * @param visible true to set the Monitor as visible
     */
    public void setMonitorVisible(boolean visible) {
	monitorVisible = visible;
	if (monitor != null) {
	    monitor.setVisible(monitorVisible);
	}
    }
    

    /**
     * Tells whether the monitor is visible.
     *
     * @return true if the monitor is visible, false otherwise
     */
    public boolean isMonitorVisible() {
	return monitorVisible;
    }
            

    /**
     * Returns the monitor of the synthesizer at the given index.
     *
     * @param index the position of the synthesizer in the synthesizer list
     *
     * @return the monitor of the specified synthesizer
     */
    public Monitor getMonitor(int index) {
	MySynthesizerModeDesc myModeDesc = (MySynthesizerModeDesc)
	    synthesizerList.getElementAt(index);
	Monitor monitor = null;

	if (myModeDesc != null) {
	    monitor = myModeDesc.getMonitor();
	}
	return monitor;
    }


    /**
     * Returns the monitor of the current synthesizer.
     *
     * @return the monitor of the current synthesizer
     */
    public Monitor getMonitor() {
	return monitor;
    }


    /**
     * Sets the current monitor.
     *
     * @param monitor the current monitor
     */
    public void setMonitor(Monitor monitor) {
	this.monitor = monitor;
    }


    /**
     * Sets the Synthesizer at the given index to use
     *
     * @param index index of the synthesizer in the list
     */
    public void setSynthesizer(int index) {
	MySynthesizerModeDesc myModeDesc = (MySynthesizerModeDesc)
	    synthesizerList.getElementAt(index);
	if (myModeDesc != null) {
	    if (isMonitorVisible()) {
		if (monitor != null) {
		    monitor.setVisible(false);
		}
	    }
	    synthesizer = myModeDesc.getSynthesizer();
	    if (synthesizer == null) {
		synthesizer = myModeDesc.createSynthesizer();
		if (synthesizer == null) {
		    debugPrint("still null");
		} else {
		    debugPrint("created");
		}
	    } else {
		debugPrint("not null");
	    }
	    monitor = myModeDesc.getMonitor();
	    if (myModeDesc.isSynthesizerLoaded()) {
		setVoiceList(myModeDesc);
	    } else {
		myModeDesc.loadSynthesizer();
	    }

	    loadedSynthesizers.add(synthesizer);
            synthesizerList.setSelectedItem(myModeDesc);
	}
    }
    

    /**
     * Sets the Voice at the given to use.
     *
     * @param index the index of the voice
     */
    public void setVoice(int index) {
	try {
	    Voice voice = (Voice) voiceList.getElementAt(index);
	    if (voice != null) {
		float oldVolume = getVolume();
		float oldSpeakingRate = getSpeakingRate();
		synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
		synthesizer.getSynthesizerProperties().setVoice(voice);
		setVolume(oldVolume);
		setSpeakingRate(oldSpeakingRate);
                voiceList.setSelectedItem(voice);
	    }
	} catch (PropertyVetoException pve) {
	    pve.printStackTrace();
	} catch (InterruptedException ie) {
	    ie.printStackTrace();
	}
    }
    

    /**
     * Returns the volume, in the range of 0 to 10.
     *
     * @return the volume, or -1 if unknown, or an error occurred
     */
    public float getVolume() {
	try {
	    float adjustedVolume =
		synthesizer.getSynthesizerProperties().getVolume();
	    if (adjustedVolume < 0.5) {
		volume = 0;
	    } else {
		volume = (float) ((adjustedVolume - 0.5) * 20);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	} 
	return volume;
    }
    

    /**
     * Sets the volume, in the range of 0 to 10.
     *
     * @param volume the new volume
     *
     * @return true if new volume is set; false otherwise
     */
    public boolean setVolume(float volume) {
	try {
	    float adjustedVolume = (float) (volume/20 + 0.5);
	    if (synthesizer != null) {
		synthesizer.getSynthesizerProperties().setVolume
		    (adjustedVolume);
		this.volume = volume;
		return true;
	    } else {
		this.volume = volume;
		return false;
	    }
	} catch (PropertyVetoException pve) {
	    try {
		synthesizer.getSynthesizerProperties().setVolume(this.volume);
	    } catch (PropertyVetoException pe) {
		pe.printStackTrace();
	    }
	    return false;
	}
    }
                

    /**
     * Returns the speaking rate.
     *
     * @return the speaking rate, or -1 if unknown or an error occurred
     */
    public float getSpeakingRate() {
	if (synthesizer != null) {
	    return synthesizer.getSynthesizerProperties().getSpeakingRate();
	} else {
	    return -1;
	}
    }
	

    /**
     * Sets the speaking rate in terms of words per minute.
     *
     * @param wordsPerMin the new speaking rate
     *
     * @return the speaking rate, or -1 if unknown or an error occurred
     */
    public boolean setSpeakingRate(float wordsPerMin) {
	float oldSpeed = getSpeakingRate();
	SynthesizerProperties properties =
	    synthesizer.getSynthesizerProperties();
	try {
	    properties.setSpeakingRate(wordsPerMin);
	    return true;
	} catch (PropertyVetoException pve) {
	    try {
		properties.setSpeakingRate(oldSpeed);
	    } catch (PropertyVetoException pe) {
		pe.printStackTrace();
	    }
	    return false;
	}
    }	


    /**
     * Returns the baseline pitch for the current synthesis voice.
     *
     * @return the baseline pitch for the current synthesis voice
     */
    public float getPitch() {
	return synthesizer.getSynthesizerProperties().getPitch();
    }	


    /**
     * Sets the baseline pitch for the current synthesis voice.
     *
     * @param pitch the baseline pitch
     *
     * @return true if new pitch is set; false otherwise
     */
    public boolean setPitch(float pitch) {
	float oldPitch = getPitch();
	try {
	    synthesizer.getSynthesizerProperties().setPitch(pitch);
	    return true;
	} catch (PropertyVetoException pve) {
	    try {
		synthesizer.getSynthesizerProperties().setPitch(oldPitch);
	    } catch (PropertyVetoException pe) {
		pe.printStackTrace();
	    }
	    return false;
	}
    }
    

    /**
     * Returns the pitch range for the current synthesis voice.
     *
     * @return the pitch range for the current synthesis voice
     */
    public float getRange() {
	return synthesizer.getSynthesizerProperties().getPitchRange();
    }
    

    /**
     * Sets the pitch range for the current synthesis voice.
     *
     * @param range the pitch range
     *
     * @return true if new range is set; false otherwise
     */
    public boolean setRange(float range) {
	float oldRange = getRange();
	try {
	    synthesizer.getSynthesizerProperties().setPitchRange(range);
	    return true;
	} catch (PropertyVetoException pve) {
	    try {
		synthesizer.getSynthesizerProperties().setPitchRange(oldRange);
	    } catch (PropertyVetoException pe) {
		pe.printStackTrace();
	    }
	    return false;
	}
    }
         

    /**
     * Sets the list of voices using the given Synthesizer mode description.
     *
     * @param modeDesc the synthesizer mode description
     */
    public void setVoiceList(SynthesizerModeDesc modeDesc) {
	Voice[] voices = modeDesc.getVoices();
	voiceList.removeAllElements();
	for (int i = 0; i < voices.length; i++) {
	    voiceList.addElement(new MyVoice(voices[i].getName(),
					     voices[i].getGender(),
					     voices[i].getAge(),
					     voices[i].getStyle()));
	}
    }


    /**
     * Returns the play list.
     *
     * @return the play list
     */
    public ListModel getPlayList() {
	return playList;
    }


    /**
     * Returns the list of voices.
     *
     * @return the list of voices
     */
    public ListModel getVoiceList() {
	return voiceList;
    }


    /**
     * Returns the list synthesizers
     *
     * @return the synthesizer list
     */
    public ListModel getSynthesizerList() {
	return synthesizerList;
    }

    
    /**
     * Returns the Playable object at the given index of the play list.
     *
     * @return the Playable object
     */
    public Object getPlayableAt(int index) {
	return null;
    }
    

    /**
     * Adds the given Playable object to the end of the play list.
     *
     * @param playable the Playable object to add
     */
    public void addPlayable(Playable playable) {
	playList.addElement(playable);
    }


    /**
     * Removes the playable at the given position from the list
     *
     * @param index the index of the Playable to remove
     */
    public void removePlayableAt(int index) {
	if (index < playList.getSize()) {
	    playList.removeElementAt(index);
	}
    }


    /**
     * Prints debug statements.
     *
     * @param statement debug statements
     */
    public static void debugPrint(String statement) {
	if (debug) {
	    System.out.println(statement);
	}
    }
}


/**
 * A Voice that implements the <code>toString()</code> method so that
 * it returns the name of the person who owns this Voice.
 */
class MyVoice extends Voice {


    /**
     * Constructor provided with voice name, gender, age and style.
     *
     * @param name the name of the person who owns this Voice
     * @param gender the gender of the person
     * @param age the age of the person
     * @param style the style of the person
     */
    public MyVoice(String name, int gender, int age, String style) {
	super(name, gender, age, style);
    }


    /**
     * Returns the name of the person who owns this Voice.
     *
     * @param String the name of the person
     */
    public String toString() {
	return getName();
    }
}


/**
 * A SynthesizerModeDesc that implements the <code>toString()</code>
 * method so that it returns the name of the synthesizer.
 */
class MySynthesizerModeDesc extends SynthesizerModeDesc {

    private PlayerModel playerModel = null;
    private Synthesizer synthesizer = null;
    private Monitor monitor = null;
    private boolean synthesizerLoaded = false;
    

    /**
     * Constructs a MySynthesizerModeDesc with the attributes from
     * the given SynthesizerModeDesc.
     *
     * @param modeDesc the SynthesizerModeDesc to get attributes from
     */
    public MySynthesizerModeDesc(SynthesizerModeDesc modeDesc,
				 PlayerModel playerModel) {
	super(modeDesc.getEngineName(), modeDesc.getModeName(),
	      modeDesc.getLocale(), modeDesc.getRunning(), 
	      modeDesc.getVoices());
	this.playerModel = playerModel;
    }
    
    
    /**
     * Returns true if the synthesizer is already loaded.
     *
     * @return true if the synthesizer is already loaded
     */
    public synchronized boolean isSynthesizerLoaded() {
	if (synthesizer == null) {
	    return false;
	}
	return ((synthesizer.getEngineState() & Engine.ALLOCATED) != 0);
    }
    
    
    /**
     * Returns a Synthesizer that fits the description of this
     * MySynthesizerModeDesc. If the synthesize was never loaded,
     * it is loaded in a separate thread.
     *
     * @return a Synthesizer
     */
    public synchronized Synthesizer getSynthesizer() {
	debugPrint("MyModeDesc.getSynthesizer(): " + getEngineName());
	return synthesizer;
    }


    /**
     * Creates the Synthesizer and its Monitor.
     *
     * @return the created Synthesizer
     */
    public Synthesizer createSynthesizer() {
	try {
	    debugPrint("Creating " + getEngineName() + "...");
	    synthesizer = Central.createSynthesizer(this);
	    
	    if (synthesizer == null) {
		System.out.println("Central created null synthesizer");
	    } else {
		synthesizer.allocate();
		synthesizer.resume();
		monitor = new Monitor(synthesizer, getEngineName());
		debugPrint("...created monitor");
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	} 
	return synthesizer;
    }

    

    /**
     * Allocates the synthesizer if it has never been allocated. This
     * method should be called after method <code>createSynthesizer()</code>.
     * It spawns a new thread to allocate the synthesizer.
     */
    public Synthesizer loadSynthesizer() {
	try {
	    if (!synthesizerLoaded) {
		debugPrint("Loading " + getEngineName() + "...");
		synthesizerLoaded = true;
		SynthesizerLoader loader = new SynthesizerLoader
		    (synthesizer, this);
		loader.start();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return synthesizer;
    }


    /**
     * Returns the Monitor of this Synthesizer.
     *
     * @return the Monitor
     */
    public synchronized Monitor getMonitor() {
	if (monitor == null) {
	    createSynthesizer();
	}
	return monitor;
    }

    
    /**
     * Returns the PlayerModel.
     *
     * @return the PlayerModel
     */
    public PlayerModel getPlayerModel() {
	return playerModel;
    }

    
    /**
     * Returns the name of the Synthesizer.
     *
     * @return the name of the Synthesizer
     */
    public String toString() {
	return getEngineName();
    }

    
    /**
     * Prints debug statements.
     *
     * @param statement debug statements
     */
    private void debugPrint(String statement) {
	PlayerModelImpl.debugPrint(statement);
    }
}


/**
 * A Thread that loads the Synthesizer.
 */
class SynthesizerLoader extends Thread {
    private Synthesizer synthesizer;
    private MySynthesizerModeDesc modeDesc;
    private PlayerModel playerModel;
   
    
    /**
     * Constructs a SynthesizerLoaded which loads the given Synthesizer.
     *
     * @param synthesizer the Synthesizer to load
     * @param modeDesc the MySynthesizerModeDesc from which we can retrieve
     *    the PlayerModel
     */
    public SynthesizerLoader(Synthesizer synthesizer,
			     MySynthesizerModeDesc modeDesc) {
	this.synthesizer = synthesizer;
	this.modeDesc = modeDesc;
	this.playerModel = modeDesc.getPlayerModel();
    }
    

    /**
     * Implements the <code>run()</code> method of the Thread class.
     */
    public void run() {
	try {
	    System.out.println("allocating...");
	    synthesizer.allocate();
	    System.out.println("...allocated");
	    synthesizer.resume();
	    System.out.println("...resume");
	    playerModel.setVoiceList(modeDesc);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}

/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */

package com.sun.speech.freetts.jsapi;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.speech.Engine;
import javax.speech.EngineCreate;
import javax.speech.EngineException;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.Voice;

import com.sun.speech.engine.synthesis.BaseVoice;
import com.sun.speech.freetts.lexicon.Lexicon;
import com.sun.speech.freetts.audio.AudioPlayer;
import com.sun.speech.freetts.ValidationException;

/**
 * Represents a SynthesizerModeDesc for the
 * FreeTTSSynthesizer. A FreeTTSSynthesizerModeDesc adds 
 * a lexicon to the standard mode items.
 */
public class FreeTTSSynthesizerModeDesc extends SynthesizerModeDesc 
implements EngineCreate {

    private String lexiconName = "com.sun.speech.freetts.en.us.CMULexicon";
    private String audioPlayerName = null;
    
    /**
     * Creates a fully-specified descriptor.
     * Any of the features may be <code>null</code>.
     *
     * @param engineName  the name of the engine
     * @param modeName   the name of the mode
     * @param locale  the locale associated with this mode
     * @param lexiconName  the name of the lexicon
     * @param audioPlayerName  the name of the audio player class
     */
    public FreeTTSSynthesizerModeDesc( String engineName, String modeName,
	    Locale locale, String lexiconName, String audioPlayerName) {
        super(engineName, modeName, locale, Boolean.FALSE, null);
	this.lexiconName = lexiconName;
	this.audioPlayerName = audioPlayerName;
    }

    /**
     * Returns the lexicon associated with this mode
     *
     * @return the lexicon, or null if it can't be found
     */
    Lexicon getLexicon() {
	Lexicon lexicon = null;

	// try to contruct the lexicon from the name.
	// if for any reason we cannot construct it
	// we return null
	try { 
	    Class clazz = Class.forName(lexiconName);
	    lexicon = (Lexicon) clazz.newInstance();
	} catch (ClassNotFoundException cnfe) {
	} catch (IllegalAccessException iae) {
	} catch (InstantiationException ie) {
	}
	return lexicon;
    }

    /**
     * Returns the AudioPlayer associated with this mode
     *
     * @return the lexicon, or null if it can't be found
     */
    AudioPlayer getAudioPlayer() {
	AudioPlayer audioPlayer = null;

	// try to contruct the audioPlayer from the name.
	// if for any reason we cannot construct it
	// we return null
	try { 
	    Class clazz = Class.forName(audioPlayerName);
	    audioPlayer = (AudioPlayer) clazz.newInstance();
	} catch (ClassNotFoundException cnfe) {
	} catch (IllegalAccessException iae) {
	} catch (InstantiationException ie) {
	}
	return audioPlayer;
    }

    /**
     * Returns the valid voices in this synthesizer mode.
     *
     * @return an array of valid voices, if no valid voices, it will
     *    return an array of size 0
     */
    public Voice[] getVoices() {
        List voiceList = new LinkedList();
        Voice[] voices = super.getVoices();
        int count = 0;
        for (int i = 0; i < voices.length; i++) {
            FreeTTSVoice freettsVoice = (FreeTTSVoice) voices[i];
            try {
                freettsVoice.validate();
                voiceList.add(freettsVoice);
                count++;
            } catch (ValidationException ve) {
                // don't do anything here if a FreeTTSVoice is invalid
            }
        }
        Voice[] validVoices = new Voice[count];
        voiceList.toArray(validVoices);
        
        return validVoices;
    }
    
    /**
     * Returns true if this is a valid FreeTTSSynthesizerModeDesc.
     * It is valid if it contains at least one valid Voice.
     * Returns false otherwise.
     *
     * @throws ValidationException if this FreeTTSSynthesizerModeDesc
     *    is invalid
     */
    public void validate() throws ValidationException {
        Voice[] voices = super.getVoices();
        int invalidCount = 0;
        String validationMessage = "";

        for (int i = 0; i < voices.length; i++) {
            try {
                ((FreeTTSVoice) voices[i]).validate();
            } catch (ValidationException ve) {
                invalidCount++;
                validationMessage += (ve.getMessage() + "\n");
            }
        }
        if (invalidCount == voices.length) {
            throw new ValidationException
                (validationMessage + getModeName() + " has no valid voices.");
        }
    }

    /**
     * Constructs a FreeTTSSynthesizer with the properties of this mode
     * descriptor.
     * 
     * @return a synthesizer that mathes the mode
     *
     * @throws IllegalArgumentException  if the properties of this
     * 		descriptor do not match any known engine or mode
     * @throws EngineException if the engine could not be created
     * @throws SecurityException if the caller does not have
     * 		permission to use the speech engine
     */
    public Engine createEngine()
        throws IllegalArgumentException, EngineException, SecurityException {
        FreeTTSSynthesizer s = new FreeTTSSynthesizer(this);
        return s;
    }

}

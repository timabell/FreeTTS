/**
 * Copyright 2003 Sun Microsystems, Inc.
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

import com.sun.speech.freetts.ValidationException;

/**
 * Represents a SynthesizerModeDesc for the
 * FreeTTSSynthesizer. A FreeTTSSynthesizerModeDesc adds 
 * an audio player to the standard mode items.
 */
public class FreeTTSSynthesizerModeDesc extends SynthesizerModeDesc 
implements EngineCreate {

    /**
     * Creates a fully-specified descriptor.
     * Any of the features may be <code>null</code>.
     *
     * @param engineName  the name of the engine
     * @param modeName   the name of the mode
     * @param locale  the locale associated with this mode
     */
    public FreeTTSSynthesizerModeDesc( String engineName, String modeName,
	    Locale locale) {
        super(engineName, modeName, locale, Boolean.FALSE, null);
    }

    /**
     * Returns the valid voices in this synthesizer mode.
     *
     * @return an array of valid voices, if no valid voices, it will
     *    return an array of size 0
     */
    public javax.speech.synthesis.Voice[] getVoices() {
        List voiceList = new LinkedList();
        javax.speech.synthesis.Voice[] voices = super.getVoices();
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
        javax.speech.synthesis.Voice[] validVoices =
            new javax.speech.synthesis.Voice[count];
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
        javax.speech.synthesis.Voice[] voices = super.getVoices();
        int invalidCount = 0;
        StringBuilder validationMessage = new StringBuilder();

        for (int i = 0; i < voices.length; i++) {
            try {
                ((FreeTTSVoice) voices[i]).validate();
            } catch (ValidationException ve) {
                invalidCount++;
                validationMessage.append(ve.getMessage());
                validationMessage.append(System.getProperty("line.separator"));
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

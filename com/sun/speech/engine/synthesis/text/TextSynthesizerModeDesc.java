/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package com.sun.speech.engine.synthesis.text;

import java.util.Locale;

import javax.speech.Engine;
import javax.speech.EngineCreate;
import javax.speech.EngineException;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.Voice;

import com.sun.speech.engine.synthesis.BaseVoice;

/**
 * Describes the <code>TextSynthesizer</code>.   Builds up the
 * voice list and other data.
 */
public class TextSynthesizerModeDesc extends SynthesizerModeDesc 
    implements EngineCreate {
    
    /**
     * Class constructor.
     */
    public TextSynthesizerModeDesc() {
        super("Text Synthesizer",	        // engine name
              "simple text output",		// mode name
              Locale.getDefault(),
              Boolean.FALSE,			// running?
              null);				// voice[]

        // Add voices known to this synthesizer.
        addVoice(new BaseVoice("Mike-1", "Mike", Voice.GENDER_MALE, 
                               Voice.AGE_MIDDLE_ADULT, "standard",
                               120.0f, 50.0f, 150.0f, 1.0f));
        addVoice(new BaseVoice("Peter-2", "Peter", Voice.GENDER_MALE,  
                               Voice.AGE_YOUNGER_ADULT, "standard",
                               135.0f, 34.0f, 165.0f, 1.0f));
        addVoice(new BaseVoice("Paul-3", "Paul", Voice.GENDER_MALE,  
                               Voice.AGE_MIDDLE_ADULT, "standard",
                               90.0f, 30.0f, 120.0f, 1.0f));
        addVoice(new BaseVoice("Mary-4", "Mary", Voice.GENDER_FEMALE,  
                               Voice.AGE_OLDER_ADULT, "standard",
                               200.0f, 80.0f, 190.0f, 1.0f));
    }

    /**
     * Constructs a text synthesizer with the properties of this mode
     * desc.
     * 
     * @throws IllegalArgumentException
     * @throws EngineException
     * @throws SecurityException
     */
    public Engine createEngine()
        throws IllegalArgumentException, EngineException, SecurityException {
        TextSynthesizer s = new TextSynthesizer(this);
        if (s == null) {
            throw new EngineException();
        }
        return s;
    }
}

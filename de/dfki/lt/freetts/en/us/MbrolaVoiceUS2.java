/**
 * Copyright 2002 DFKI GmbH.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package de.dfki.lt.freetts.en.us;

import java.io.File;
import java.net.URL;

import com.sun.speech.freetts.UtteranceProcessor;

import de.dfki.lt.freetts.mbrola.MbrolaCaller;

import java.io.IOException;

/**
 * Defines an unlimited-domain diphone synthesis based voice using
 * the MBROLA synthesis.
 */
public class MbrolaVoiceUS2 extends MbrolaVoice {
        
    /**
     * Creates a simple voice
     */
    public MbrolaVoiceUS2() {
	this(false);
    }

    /**
     * Creates a simple voice
     *
     * @param createLexicon if <code>true</code> automatically load up
     * the default CMU lexicon; otherwise, don't load it.
     */
    public MbrolaVoiceUS2(boolean createLexicon) {
	super(createLexicon);
	setRate(150f);
	setPitch(115F);
	setPitchRange(12F);
    }

    //[[Providing the Mbrola classes via getUnitSelector() and
    // getUnitConcatenator() is just a hack allowing us to use
    // the current CMUVoice.java framework. It only means that
    // after the Durator and the ContourGenerator, the classes
    // process the utterance (Selector before Concatenator).]]
    /**
     * Returns the unit concatenator to be used by this voice.
     * This method constructs the command line with which the
     * MBROLA binary will be called, and initialises the
     * MbrolaCaller accordingly.
     * 
     * @return the unit conatenator
     * 
     * @throws IOException if an IO error occurs while getting
     *     processor
     */
    protected UtteranceProcessor getUnitConcatenator() throws IOException {
        String databaseDir = "us2";
        String databaseName = "us2";
	return new MbrolaCaller(getMbrolaCommand(databaseDir, databaseName));
    }

    /**
     * Converts this object to a string
     * 
     * @return a string representation of this object
     */
    public String toString() {
	return "MbrolaVoiceUS2";
    }
}


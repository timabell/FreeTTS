/**
 * Portions Copyright 2001 Sun Microsystems, Inc.
 * Portions Copyright 1999-2001 Language Technologies Institute, 
 * Carnegie Mellon University.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package com.sun.speech.freetts.en.us;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.Utterance;
import com.sun.speech.freetts.UtteranceProcessor;
import com.sun.speech.freetts.ProcessException;
import com.sun.speech.freetts.clunits.ClusterUnitSelector;
import com.sun.speech.freetts.clunits.ClusterUnitDatabase;
import com.sun.speech.freetts.clunits.ClusterUnitPitchmarkGenerator;
import com.sun.speech.freetts.relp.UnitConcatenator;
import java.io.IOException;

import com.sun.speech.freetts.Age;
import com.sun.speech.freetts.Gender;
import java.util.Locale;

/**
 * Defines limited domain synthesis voice that specializes
 * in telling the time (with an english accent).
 */
public class CMUClusterUnitVoice extends CMUVoice {
    /**
     * Creates a simple voice. By default, no Lexicon is loaded.
     */
    /*TODO
    public CMUClusterUnitVoice() {
	this(false);
    }
    */

    /**
     * Creates a simple voice
     *
     * @param createLexicon if <code>true</code> automatically load up
     * the default CMU lexicon; otherwise, don't load it.
     */
    //TODO
    public CMUClusterUnitVoice(boolean createLexicon, String name,
            Gender gender, Age age, String description, Locale locale) {
	super(createLexicon, name, gender, age, description, locale);
	setRate(150f);
	setPitch(100F);
	setPitchRange(12F);
    }

    /**
     * Sets up the FeatureSet for this Voice.
     *
     * @throws IOException if an I/O error occurs
     */
    protected void setupFeatureSet() throws IOException {
	super.setupFeatureSet();
	getFeatures().setString(FEATURE_JOIN_TYPE, "simple_join");
    }

    /**
     * Returns the unit selector to be used by this voice.
     * Derived voices typically override this to customize behaviors.
     * This voice uses  a cluster unit selector as the unit selector.
     * 
     * @return the post lexical processor
     * 
     * @throws IOException if an IO error occurs while getting
     *     processor
     */
    protected UtteranceProcessor getUnitSelector() throws IOException {
	String unitDatabaseName = getFeatures().getString(DATABASE_NAME);

	if (unitDatabaseName == null) {
            throw new Error("CMUClusterUnitVoice not defined with data file");
	}

	return new ClusterUnitSelector(
		getResource(unitDatabaseName));
    }

    /**
     * Returns the pitch mark generator to be used by this voice.
     * Derived voices typically override this to customize behaviors.
     * There is no default unit selector
     * 
     * @return the post lexical processor
     * 
     * @throws IOException if an IO error occurs while getting
     *     processor
     */
    protected UtteranceProcessor getPitchmarkGenerator() throws IOException {
	return new ClusterUnitPitchmarkGenerator();
    }

    /**
     * Returns the unit concatenator to be used by this voice.
     * Derived voices typically override this to customize behaviors.
     * There is no default unit selector
     * 
     * @return the post lexical processor
     * 
     * @throws IOException if an IO error occurs while getting
     *     processor
     */
    protected UtteranceProcessor getUnitConcatenator() throws IOException {
	return new UnitConcatenator();
    }

    
    /**
     * Converts this object to a string
     * 
     * @return a string representation of this object
     */
    public String toString() {
	return "CMUClusterUnitVoice";
    }
}

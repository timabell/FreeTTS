/**
 * Portions Copyright 2004 DFKI GmbH.
 * Portions Copyright 2001 Sun Microsystems, Inc.
 * Portions Copyright 1999-2001 Language Technologies Institute, 
 * Carnegie Mellon University.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package de.dfki.lt.freetts;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import com.sun.speech.freetts.Age;
import com.sun.speech.freetts.Gender;
import com.sun.speech.freetts.Tokenizer;
import com.sun.speech.freetts.UtteranceProcessor;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.clunits.ClusterUnitPitchmarkGenerator;
import com.sun.speech.freetts.clunits.ClusterUnitSelector;
import com.sun.speech.freetts.lexicon.Lexicon;
import com.sun.speech.freetts.relp.AudioOutput;
import com.sun.speech.freetts.relp.UnitConcatenator;

/**
 * A simple dummy voice as a starting point for non-US-English
 * cluster unit voices. All NLP stuff would need to be implemented
 * in order for this to become a full TTS voice.
 */
public class ClusterUnitVoice extends Voice {

	protected URL database;
	public ClusterUnitVoice(String name, Gender gender, Age age,
            String description, Locale locale, String domain,
            String organization, Lexicon lexicon, URL database) {
		
	//TODO: do something useful with the lexicon
	super(name, gender, age, description, locale,
                domain, organization);
	setRate(150f);
	setPitch(100F);
	setPitchRange(12F);
        this.database = database;
    }

	public Tokenizer getTokenizer() {
		return null;
	}


	protected void loader() throws IOException {
		
	}


	protected UtteranceProcessor getAudioOutput() throws IOException {
		return new AudioOutput();
	}
	
	   /**
     * Gets the url to the database that defines the unit data for this
     * voice.
     *
     * @return a url to the database
     */
    public URL getDatabase() {
        return database;
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
	return new ClusterUnitSelector(getDatabase());
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

}

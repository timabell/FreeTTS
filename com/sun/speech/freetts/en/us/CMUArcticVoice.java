package com.sun.speech.freetts.en.us;

import java.io.IOException;

import java.util.Locale;

import java.net.URL;

import com.sun.speech.freetts.Age;
import com.sun.speech.freetts.Gender;
import com.sun.speech.freetts.Item;
import com.sun.speech.freetts.UtteranceProcessor;
import com.sun.speech.freetts.PathExtractor;
import com.sun.speech.freetts.PathExtractorImpl;

import com.sun.speech.freetts.clunits.ClusterUnitSelector;

/**
 * Experimental class that selects units for the
 * <a href="http://festvox.org/cmu_arctic/">CMU ARCTIC voices</a>.
 */
public class CMUArcticVoice extends CMUClusterUnitVoice {
    
    /**
     * Creates a simple cluster unit voice for the ARCTIC voices
     *
     * @param name the name of the voice
     * @param gender the gender of the voice
     * @param age the age of the voice
     * @param description a human-readable string providing a
     * description that can be displayed for the users.
     * @param locale the locale of the voice
     * @param domain the domain of this voice.  For example,
     * @param organization the organization which created the voice
     * &quot;general&quot;, &quot;time&quot;, or
     * &quot;weather&quot;.
     * @param lexicon the lexicon to load
     * @param database the url to the database containing unit data
     * for this voice.
     */
    public CMUArcticVoice(String name, Gender gender, Age age,
            String description, Locale locale, String domain,
            String organization, CMULexicon lexicon, URL database) {
	super(name, gender, age, description, locale,
                domain, organization, lexicon, database);
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
	return new CMUArcticUnitSelector(getDatabase());
    }
}

/**
 * Selects a unit based on the algorithm used by the FestVox ARCTIC
 * voices.
 */
class CMUArcticUnitSelector extends ClusterUnitSelector {
    private static final String VOWELS = "aeiou";
    
    /**
     * Constructs an ArcticUnitSelector.
     *
     * @param url the URL for the unit database. If the URL path ends
     *     with a '.bin' it is assumed that the DB is a binary database,
     *     otherwise, its assumed that its a text database1
     *
     * @throws IOException if an error occurs while loading the
     *     database
     *
     */
    public CMUArcticUnitSelector(URL url) throws IOException {
        super(url);
    }

    /**
     * Sets the cluster unit name given the segment.
     *
     * @param seg the segment item that gets the name
     */
    protected void setUnitName(Item seg) {
	String cname = null;

	String segName = seg.getFeatures().getString("name");

        /* If we have a vowel, then the unit name is the segment name
         * plus a 0 or 1, depending upon the stress of the parent.
         * Otherwise, the unit name is the segment name plus "coda"
         * or "onset" based upon the seg_onsetcoda feature processor.
         */
        if (segName.equals("pau")) {
            cname = segName;
        } else if (VOWELS.indexOf(segName.charAt(0)) >= 0) {
            cname = segName + seg.findFeature("R:SylStructure.parent.stress");
        } else {
            cname = segName + seg.findFeature("seg_onsetcoda");
        }

	seg.getFeatures().setString("clunit_name", cname);
    }
}

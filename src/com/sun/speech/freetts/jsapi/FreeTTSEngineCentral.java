/**
 * Copyright 2003 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package com.sun.speech.freetts.jsapi;

import java.util.Locale;
import java.util.Vector;

import javax.speech.EngineCentral;
import javax.speech.EngineList;
import javax.speech.EngineModeDesc;

import com.sun.speech.freetts.ValidationException;
import com.sun.speech.freetts.Voice;


/**
 * Supports the EngineCentral JSAPI 1.0 interface for the
 * FreeTTSSynthesizer.  To use a FreeTTSSynthesizer, you should place 
 * a line into the speech.properties file as so:
 *
 * <pre>
 * FreeTTSSynthEngineCentral=com.sun.speech.freetts.jsapi.FreeTTSEngineCentral
 * </pre>
 *
 */

public class FreeTTSEngineCentral implements EngineCentral {
    private static final String ENGINE_NAME = "FreeTTS Synthesizer";

    /**
     * Creates a FreeTTSEngineCentral
     */
    public FreeTTSEngineCentral() throws Exception {
	// Note that the JSAPI layer currently is silent
	// about any exceptions thrown from here, so we are noisy here
    }

    /**
     * Returns a list containing references to all matching 
     * synthesizers.  The mapping of FreeTTS VoiceDirectories and
     * Voices to JSAPI Synthesizers and Voices is as follows:
     *
     * <p><ul>
     * <li>Each FreeTTS VoiceDirectory specifies the list of FreeTTS
     * Voices supported by that directory.  Each Voice in that
     * directory specifies its name (e.g., "kevin" "kevin16" "alan"),
     * domain (e.g., "general" or "time") and locale (e.g., Locale.US).
     * <li>For all FreeTTS Voices from all VoiceDirectories discovered
     * by the VoiceManager, this method will group the Voices
     * according to those that have both a common locale and domain
     * (e.g, all "general" domain voices for the US local will be
     * grouped together).
     * <li>For each group of voices that shares a common locale and
     * domain, this method generates a new JSAPI SynthesizerModeDesc
     * with the following attributes:
     *   <ul>
     *   <li>The engine name is of the form: "FreeTTS &lt;locale>
     *   &lt;domain> synthesizer"  For example, "FreeTTS en_us general
     *   synthesizer"
     *   <li>The locale is the locale shared by all the voices (e.g.,
     *   Locale.US)
     *   <li>The mode name is the domain shared by all the voices
     *   (e.g., "general").
     *   </ul>
     * <li>The JSAPI Voices for each resulting Synthesizer will have
     * the name of the FreeTTS Voice (e.g. "kevin" "kevin16").
     * </ul>
     *
     * @param require  an engine mode that describes the desired
     * 			synthesizer
     *
     * @return an engineList containing matching engines, or null if
     *		no matching engines are found
     */
    public EngineList createEngineList(EngineModeDesc require) {
	EngineList el = new EngineList();

        com.sun.speech.freetts.VoiceManager voiceManager =
            com.sun.speech.freetts.VoiceManager.getInstance();
        
        com.sun.speech.freetts.Voice[] voices = voiceManager.getVoices();

        // We want to get all combinations of domains and locales
        Vector domainLocaleVector = new Vector();
        for (int i = 0; i < voices.length; i++) {
            DomainLocale dl =
                new DomainLocale(voices[i].getDomain(), voices[i].getLocale());
            DomainLocale dlentry = (DomainLocale)
                getItem(domainLocaleVector, dl);
            if (dlentry == null) {
                domainLocaleVector.add(dl);
                dlentry = dl;
            }
            dlentry.addVoice(voices[i]);
        }

        // build list of SynthesizerModeDesc's for each domain/locale
        // combination
        for (int i = 0; i < domainLocaleVector.size(); i++) {
            DomainLocale dl = (DomainLocale) domainLocaleVector.get(i);

            FreeTTSSynthesizerModeDesc desc = new
                FreeTTSSynthesizerModeDesc("FreeTTS "
                        + dl.getLocale().toString() + " " + dl.getDomain()
                        + " synthesizer", dl.getDomain(), dl.getLocale());

            // iterate through the voices in a different order
            voices = dl.getVoices();
            for (int j = 0; j < voices.length; j++) {
                FreeTTSVoice jsapiVoice = new FreeTTSVoice(voices[j], null);
                desc.addVoice(jsapiVoice);
            }

            if (require == null || desc.match(require)) {
                try {
                    desc.validate();
                    el.addElement(desc);
                } catch (ValidationException ve) {
                    System.err.println(ve.getMessage());
                }
            }
        }

        if (el.size() == 0) {
            el = null;
        }
        return el;
    }

    /**
     * Gets an item out of a vector.
     * Warning: linear search
     *
     * @param vector the vector to search
     * @param o the object to look for using vector.get(i).equals(o)
     *
     * @return the item if it exists in the vector, else null
     */
    private Object getItem(Vector vector, Object o) {
        for (int i = 0; i < vector.size(); i++) {
            if (vector.get(i).equals(o)) {
                return vector.get(i);
            }
        }
        return null;
    }
}


/**
 * Used to be able to generate a list of voices based on unique
 * combinations of domain/locale pairs.
 */
class DomainLocale {
    private String domain;
    private Locale locale;
    private Vector<Voice> voices;

    /**
     * Constructor
     *
     * @param domain the domain to use
     * @param locale the locale to use
     */
    public DomainLocale(String domain, Locale locale) {
        this.domain = domain;
        this.locale = locale;
        this.voices = new Vector<Voice>();
    }

    /**
     * See if two DomainLocale objects are equal.
     * The voices are NOT compared.
     *
     * @param o, the object to compare to
     *
     * @return true if the domain and locale are both equal, else
     * false
     */
    public boolean equals(Object o) {
        if (! (o instanceof DomainLocale)) {
            return false;
        }
        return (domain.equals(((DomainLocale) o).getDomain())
                && locale.equals(((DomainLocale) o).getLocale()));
    }

    /**
     * Gets the domain.
     * @return the domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Gets the locale.
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Adds a voice to this instance.
     *
     * @param voice the voice to add
     */
    public void addVoice(com.sun.speech.freetts.Voice voice) {
        voices.add(voice);
    }

    /**
     * Gets the voices of this instance.
     *
     * @return all of the voices that have been added to this
     * instance.
     */
    public com.sun.speech.freetts.Voice[] getVoices() {
        com.sun.speech.freetts.Voice[] voiceArray =
            new com.sun.speech.freetts.Voice[voices.size()];
        return (com.sun.speech.freetts.Voice[]) voices.toArray(voiceArray);
    }
}

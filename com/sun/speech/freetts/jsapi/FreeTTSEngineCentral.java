/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package com.sun.speech.freetts.jsapi;

import javax.speech.EngineCentral;
import javax.speech.EngineModeDesc;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.EngineList;
import javax.speech.synthesis.Voice;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Locale;
import java.io.InputStream;
import java.io.IOException;

/**
 * Supports the EngineCentral JSAPI 1.0 interface for the
 * FreeTTSSynthesizer.  To use a FreeTTSSynthesizer, you should place 
 * a line into the speech.properties file as so:
 *
 * <pre>
 * FreeTTSSynthEngineCentral=com.sun.speech.freetts.jsapi.FreeTTSEngineCentral
 * </pre>
 *
 * Instances of this class load a 'synthesizer.properties' resource.
 * This file describes the various synthesizers, modes and associated
 * voices. Clients can retrieve a possibly filtered list of the
 * supported synthesizer modes via the <code>createEngineList</code>
 * call. The 'synthesizer.properties' file should be located in the
 * classpath at 
 * <pre>
       com.sun.speech.freetts.jsapi
 * </pre>
 *
 * The synthesizer.properties file has the following format:
 * <pre>
 #
 # FreeTTS Synthesizer Synthesizer Properties File
 #
 #
 # This is a synthesizer properties file used for creating FreeTTS
 # Synthesizers. This file is processed by FreeTTSEngineCentral and is
 # used to create the set of available FreeTTSSynthesizerModeDescs.
 #
 # Top level properties:
 #  synthesizers - the list of synthesizers. No default
 # 
 # For each listed synthesizer there should be one synthesizer as
 # described here:
 #
 # synthesizer properties are:
 #	synth.engineName - string description of the synthesizer
 #	synth.locale.language -  the ISO language
 #	synth.locale.country -  the ISO country
 #	synth.locale.variant -  (optional)
 #	synth.lexicon - the classname of the lexicon. No default
 #	synth.modes - the list of modes. No default
 #	synth.audioPlayer - the classname of the audio player.
 #	    Defaults to com.sun.speech.freetts.audio.JavaClipAudioPlayer
 #
 # For each listed mode there should be one mode as described here:
 #
 # mode properties are:
 #	synth.mode.modeName  - the name of the mode. defaults to name
 #				given in synth.modes line
 #	synth.mode.voices  	the list of voices. No default.
 #
 * For each listed voice there should be one voice as described here:
 #
 # Voice properties are
 #	synth.mode.voice.class - class file for voice. No default.
 #	synth.mode.voice.gender - Gender for voice. Default: GENDER_NEUTRAL
 #	synth.mode.voice.age - age for the voice. Default: AGE_NEUTRAL
 #	synth.mode.voice.pitch - pitch (in hertz) for the voice. Default 100
 #	synth.mode.voice.pitchRange - range (in hz) for the voice. Default 10
 #	synth.mode.voice.speakingRate - rate for the voice. Default: 150
 #	synth.mode.voice.volume - volume for the voice. Default: 1.0
 #	synth.mode.voice.dbName - the name of the unit database for the voice
 #

synthesizers = unlimited limited streaming

# -- unlimited synthesizer 
unlimited.engineName = Unlimited domain FreeTTS Speech Synthesizer from Sun Labs
unlimited.locale.language = en
unlimited.locale.country = US
unlimited.lexicon = com.sun.speech.freetts.en.us.CMULexicon
unlimited.modes = diphone

# ----- unlimited mode
unlimited.diphone.modeName = CMU Diphone
unlimited.diphone.voices = kevin kevin16

# A 8kHz voice
unlimited.diphone.kevin.class=com.sun.speech.freetts.en.us.CMUDiphoneVoice
unlimited.diphone.kevin.gender = GENDER_MALE
unlimited.diphone.kevin.age = AGE_YOUNG_ADULT
unlimited.diphone.kevin.dbName = cmu_kal/diphone_units.bin

# A 16kHz voice
unlimited.diphone.kevin16.class=com.sun.speech.freetts.en.us.CMUDiphoneVoice
unlimited.diphone.kevin16.gender = GENDER_MALE
unlimited.diphone.kevin16.age = AGE_YOUNG_ADULT
unlimited.diphone.kevin16.dbName = cmu_kal/diphone_units16.bin


# -- limited synthesizer
limited.engineName = Limited domain FreeTTS Speech Synthesizer from Sun Labs
limited.locale.language = en
limited.locale.country = US
limited.modes = clunits
limited.lexicon = com.sun.speech.freetts.en.us.CMULexicon

# ----- clunits  unlimited mode
limited.clunits.modeName = CMU 16Khz EN US time-domain
limited.clunits.voices = alan
limited.clunits.alan.class=com.sun.speech.freetts.en.us.CMUClusterUnitVoice
limited.clunits.alan.dbName=cmu_awb/cmu_time_awb.bin


# -- streaming synthesizer 
streaming.engineName = Streaming FreeTTS Speech Synthesizer from Sun Labs
streaming.locale.language = en
streaming.locale.country = US
streaming.lexicon = com.sun.speech.freetts.en.us.CMULexicon
streaming.audioPlayer = com.sun.speech.freetts.audio.JavaStreamingAudioPlayer
streaming.modes = diphone

# ----- streaming mode
# This is a test synthesizer that sends its output to
# the null audio player. 
streaming.diphone.modeName = CMU Diphone
streaming.diphone.voices = kevin kevin16

# A 8kHz voice
streaming.diphone.kevin.class=com.sun.speech.freetts.en.us.CMUDiphoneVoice
streaming.diphone.kevin.gender = GENDER_MALE
streaming.diphone.kevin.age = AGE_YOUNG_ADULT
streaming.diphone.kevin.dbName = cmu_kal/diphone_units.bin

# A 16kHz voice
streaming.diphone.kevin16.class=com.sun.speech.freetts.en.us.CMUDiphoneVoice
streaming.diphone.kevin16.gender = GENDER_MALE
streaming.diphone.kevin16.age = AGE_YOUNG_ADULT
streaming.diphone.kevin16.dbName = cmu_kal/diphone_units16.bin


# -- silent synthesizer 
silent.engineName = Silent FreeTTS Speech Synthesizer from Sun Labs
silent.locale.language = en
silent.locale.country = US
silent.lexicon = com.sun.speech.freetts.en.us.CMULexicon
silent.audioPlayer = com.sun.speech.freetts.audio.NullAudioPlayer
silent.modes = diphone

# ----- silent mode
# This is a test synthesizer that sends its output to
# the null audio player. 
silent.diphone.modeName = CMU Diphone
silent.diphone.voices = kevin kevin16

# A 8kHz voice
silent.diphone.kevin.class=com.sun.speech.freetts.en.us.CMUDiphoneVoice
silent.diphone.kevin.gender = GENDER_MALE
silent.diphone.kevin.age = AGE_YOUNG_ADULT
silent.diphone.kevin.dbName = cmu_kal/diphone_units.bin

# A 16kHz voice
silent.diphone.kevin16.class=com.sun.speech.freetts.en.us.CMUDiphoneVoice
silent.diphone.kevin16.gender = GENDER_MALE
silent.diphone.kevin16.age = AGE_YOUNG_ADULT
silent.diphone.kevin16.dbName = cmu_kal/diphone_units16.bin
   </pre>
 */

public class FreeTTSEngineCentral implements EngineCentral {
    private String engineName = "FreeTTS Synthesizer";
    private List descriptors = new ArrayList(); 

    /**
     * Creates a FreeTTSEngineCentral, loads the
     * FreeTTSSynthesizerModeDesc from the 'synthesizer.properties'
     * file.
     */
    public FreeTTSEngineCentral() throws Exception {
	// Note that the JSAPI layer currently is silent
	// about any exceptions thrown from here, so we are noisy here

	try {
	    loadDescriptorsFromProperties("synthesizer.properties");
	} catch (Exception e) {
	    System.err.println(
	    "FreeTTSEngineCentral: trouble loading properties");
	    e.printStackTrace();
	    throw e;
	}
    }

    /**
     * Load the FreeTTSModeDescriptors from the given properties
     * file
     *
     * @param source the name of the properties file
     */
    private void loadDescriptorsFromProperties(String source) {
	Class cls = FreeTTSEngineCentral.class;
	InputStream is = cls.getResourceAsStream("synthesizer.properties");
	Properties props = new Properties();
	try {
	    props.load(is);
	    engineName = props.getProperty("engineName", engineName);
	    String synths = props.getProperty("synthesizers");
	    StringTokenizer tok = new StringTokenizer(synths);

	    while (tok.hasMoreTokens()) {
		String synthName = tok.nextToken();
		loadSynthesizer(synthName, props);
	    }
	} catch (IOException ioe) {
	    // if we can't load the synthesizer.properties file
	    // we just end up with an empty descriptors list
	    // which is just fine.
	}
    }

    /**
     * Load a Synthesizer from the given properties
     *
     * @param synth the name of the synthesizer
     * @param props the property sheet to collect the mode from
     */
    private void loadSynthesizer( String synth, Properties props) {

	String engineName = props.getProperty(synth + ".engineName",
			this.engineName);

	String lexiconName = props.getProperty(synth + ".lexicon");
	Locale locale = getLocale(synth, props);
	String audioPlayerClass = props.getProperty(synth + ".audioPlayer",
		"com.sun.speech.freetts.audio.JavaClipAudioPlayer");

	// A synth must at least have a lexicon. If it doesn't
	// then there is no need to bother with the rest.

	if (lexiconName == null) {
	    return;
	}

	String modes = props.getProperty(synth + ".modes");
	StringTokenizer tok = new StringTokenizer(modes);

	while (tok.hasMoreTokens()) {
	    String modeName = tok.nextToken();
	    loadMode(synth, modeName, engineName, locale, lexiconName, 
		    audioPlayerClass, props);
	}
    }


    /**
     * Gets the locale from the property sheet
     *
     * @param synth the name of the synthesizer
     * @param props where to get the properties from 
     * 
     * @return the locale.
     */
    private Locale getLocale(String synth, Properties props) {
	Locale locale = Locale.getDefault();
	String language = props.getProperty(synth + ".locale.language"); 

	if (language != null) {
	    String country = props.getProperty(synth + ".locale.country"); 
	    if (country == null) {
		locale = new Locale(language);
	    } else {
	    	String variant = props.getProperty(synth + ".locale.variant"); 
		if (variant == null) {
		    locale = new Locale(language, country);
		} else {
		    locale = new Locale(language, country, variant);
		}
	    }
	} 
	return locale;
    }

    /**
     * Loads the mode from the properties file
     *
     * @param synth 	the synthesizer
     * @param mode 	the name of this mode
     * @param engineName the name of this engine
     * @param locale 	the locale for this mode
     * @param lexiconName the lexicon for this mode
     * @param audioPlayerClass the name of the audio player class
     * @param props the properties to load the mode from
     */
    private void loadMode(String synth, String mode, String engineName,
	    Locale locale, String lexiconName, 
	    String audioPlayerClass, Properties props) {

	String modeName = props.getProperty(
		synth + "." + mode + ".modeName", mode);
	SynthesizerModeDesc desc = new FreeTTSSynthesizerModeDesc(
		engineName, modeName, locale, lexiconName,
		audioPlayerClass);
	String voices = props.getProperty(synth + "." + mode + ".voices");
	StringTokenizer tok = new StringTokenizer(voices);

	while (tok.hasMoreTokens()) {
	    String voiceName = tok.nextToken();
	    Voice voice = loadVoice(synth, mode, voiceName, props);
	    if (voice != null) {
		desc.addVoice(voice);
	    }
	}

	// if a mode has no voices, we ignore it

	if (desc.getVoices().length > 0) {
	    descriptors.add(desc);
	}
    }


    /**
     * Loads the the voice from the property sheet
     *
     * @param synthName the name of the synthesizer
     * @param modeName the name of the mode
     * @param voiceName the name of the voice
     * @param props the property sheet to gather the mode from
     * 
     * @return 	the loaded voice or <code>null</code> if the voice was 
     *     Not loaded successfully
     */
    private Voice loadVoice(String synthName, String modeName, 
	    String voiceName, Properties props) {

	Voice voice = null;
	String propPrefix = synthName + "." + modeName + "." + voiceName + ".";
	
	String style = props.getProperty(propPrefix + "style", "standard");
	try {
	    float pitch = Float.parseFloat
                (props.getProperty(propPrefix + "pitch", "100"));
	    float pitchRange = Float.parseFloat
                (props.getProperty(propPrefix + "pitchRange", "10"));
	    float speakingRate = Float.parseFloat
                (props.getProperty(propPrefix + "speakingRate", "150"));
	    float volume = Float.parseFloat
                (props.getProperty(propPrefix + "volume", "1.0"));
	    String className = props.getProperty(propPrefix + "class");
	    String dbName = props.getProperty(propPrefix + "dbName");
	    int gender = stringToGender
                (props.getProperty(propPrefix + "gender", "GENDER_NEUTRAL"));
	    int age = stringToAge(props.getProperty(propPrefix + "age", 
                                                    "AGE_NEUTRAL"));
            String validatorName = props.getProperty
                (propPrefix + "validator");

	    if (className != null)  {
		voice = new FreeTTSVoice
                    (synthName + "." + modeName + "." + voiceName,
                     voiceName, gender, age,
                     style, pitch, pitchRange, speakingRate, 
                     volume, className, dbName, validatorName);
	    }

	} catch (NumberFormatException nfe) {
	    System.err.println("Error while parsing voice property data");
	}
	return voice;
    }


    /**
     * Given a string of the form "GENDER_MALE", return the
     * Voice constant associated with it.
     *
     * @param genderString the string form of the gender
     *
     * @return 	a Voice.GENDER_XXXX constant associated with the
     *     	string. If no match is found Voice.GENDER_NEUTRAL 
     *		is returned
     */
    private int stringToGender(String genderString) {
	int gender = Voice.GENDER_NEUTRAL;
	if (genderString != null) {
	    if (genderString.equals("GENDER_MALE")) {
		gender = Voice.GENDER_MALE;
	    } else if (genderString.equals("GENDER_FEMALE")) {
		gender = Voice.GENDER_FEMALE;
	    } else if (genderString.equals("GENDER_NEUTRAL")) {
		gender = Voice.GENDER_NEUTRAL;
	    } else if (genderString.equals("GENDER_DONT_CARE")) {
		gender = Voice.GENDER_DONT_CARE;
	    }
	}
	return gender;
    }

    /**
     * Given a string of the form "AGE_OLDER_ADULT", return the
     * Voice constant associated with it
     *
     * @param ageString the age string of the form "AGE_CHILD"
     *
     * @return 	a Voice constant for the age. If no match is found,
     * 		Voice.AGE_NEUTRAL is returned
     */
    private int stringToAge(String ageString) {
	int age = Voice.AGE_NEUTRAL;
	if (ageString != null) {
	    if (ageString.equals("AGE_CHILD")) {
		age = Voice.AGE_CHILD;
	    } else if (ageString.equals("AGE_DONT_CARE")) {
		age = Voice.AGE_DONT_CARE;
	    } else if (ageString.equals("AGE_MIDDLE_ADULT")) {
		age = Voice.AGE_MIDDLE_ADULT;
	    } else if (ageString.equals("AGE_NEUTRAL")) {
		age = Voice.AGE_NEUTRAL;
	    } else if (ageString.equals("AGE_OLDER_ADULT")) {
		age = Voice.AGE_OLDER_ADULT;
	    } else if (ageString.equals("AGE_TEENAGER")) {
		age = Voice.AGE_TEENAGER;
	    } else if (ageString.equals("AGE_YOUNGER_ADULT")) {
		age = Voice.AGE_YOUNGER_ADULT;
	    } 
	}
	return age;
    }

    /**
     * Returns a list containing references to all matching 
     *	synthesizers.
     *
     * @param required  an engine mode that describes the desired
     * 			synthesizer
     *
     * @return an engineList containing matching engines, or null if
     *		no matching engines are found
     */
    public EngineList createEngineList(EngineModeDesc require) {
	EngineList el = null;

	if (descriptors == null) {
	    return el;
	}

	for (Iterator i = descriptors.iterator(); i.hasNext();) {
	    FreeTTSSynthesizerModeDesc desc =
		(FreeTTSSynthesizerModeDesc) i.next();
	    if (require == null || (desc.match(require) && desc.isValid())) {
                if (el == null) {
                    el = new EngineList();
                }
                el.addElement(desc);
            }
	}
        return el;
    }
}

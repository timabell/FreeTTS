/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package com.sun.speech.freetts.jsapi;

import com.sun.speech.engine.synthesis.BaseVoice;
import com.sun.speech.freetts.Voice;

/**
 * Extends the BaseVoice class to encapsulate FreeTTSSynthesizer specific data.
 */
public class FreeTTSVoice extends BaseVoice {

    private String freettsVoiceClassName;
    private Voice freettsVoice;
    private String dbName = null;

    /**
     * Constructs a FreeTTSVoice
     *
     * @param id the id for the voice
     * @param gender the gender for the voice (e.g. GENDER_MALE)
     * @param gender the gender for the voice (e.g. Voice.GENDER_MALE)
     * @param age the age for the voice (e.g. Voice.AGE_YOUNGER_ADULT)
     * @param style style of the voice
     * @param pitch initial pitch of the voice in hertz
     * @param pitchRange initial pitch rate of the voice in hertz
     * @param speakingRate initial speaking rate of the voice in words
     * 				per minute
     * @param volume the initial volume of the voice
     * @param className the classname for the freetts voice
     * @param dbName the name of the database associated with this voice
     */
    public FreeTTSVoice(String id,
                     String name,
                     int gender,
                     int age,
                     String style,
                     float pitch,
                     float pitchRange,
                     float speakingRate,
                     float volume,
		     String className,
		     String dbName) {
        super(id, name, gender, age, style, pitch, pitchRange,
		speakingRate, volume);   
	this.freettsVoiceClassName = className;
	this.dbName = dbName;
    }

    /**
     * Gets the id for this voice.
     * Should be unique for a synthesizer.
     *
     * @return the voice id
     */
    public String getId() {
        return voiceId;
    }

    /**
     * Gets a string representation of the object
     *
     * @return the name of this voice
     */
    public String toString() {
	return getName();
    }


    /**
     * Gets a FreeTTS voice from this JSAPI voice
     *
     * @return a FreeTTS voice or null, if the voice cannot be found
     */
    synchronized Voice getFreeTTSVoice() {
	if (freettsVoice == null) {
	    try { 
		Class clazz = Class.forName(freettsVoiceClassName);
		freettsVoice = (Voice) clazz.newInstance();

		freettsVoice.setPitch(defaultPitch);
		freettsVoice.setPitchRange(defaultPitchRange);
		freettsVoice.setRate(defaultSpeakingRate);
		freettsVoice.setVolume(defaultVolume);
		if (dbName != null) {
		    freettsVoice.getFeatures().setString(
			    Voice.DATABASE_NAME, dbName);
		}

	    } catch (ClassNotFoundException cnfe) {
	    } catch (IllegalAccessException iae) {
	    } catch (InstantiationException ie) {
	    }
	}
	return freettsVoice;
    }

    /**
     * Sets the id for this voice.
     *
     * @param id the new id
     */
    public void setId(String id) {
        voiceId = id;
    }

    /**
     * Creates a copy of this <code>BaseVoice</code>.
     *
     * @return the cloned object
     */
    public Object clone() {
        return super.clone();
    }
}


package com.sun.speech.freetts.en.us.%VOICENAME%;

import com.sun.speech.freetts.en.us.CMUClusterUnitVoice;
import com.sun.speech.freetts.en.us.CMULexicon;
import com.sun.speech.freetts.VoiceDirectory;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.Gender;
import com.sun.speech.freetts.Age;
import java.util.Locale;


/**
 * This voice directory provides a US/English Cluster Unit voice imported
 * from FestVox.
 *
 */
public class %CLASSNAME% extends VoiceDirectory {
    /**
     * Gets the voices provided by this voice.
     *
     * @return an array of new Voice instances
     */
    public Voice[] getVoices() {
        // Change voice properties here
        Voice voice = new CMUClusterUnitVoice(false, "%NAME%", Gender.%GENDER%,
                Age.%AGE%, "%DESCRIPTION%",
                Locale.US);
        voice.getFeatures().setString(Voice.DATABASE_NAME,
                "%VOICENAME%/%VOICENAME%.bin");

        // Make sure this is the correct lexicon:
        voice.setLexicon(new CMULexicon("cmutimelex"));
        Voice[] voices = {voice};
        return voices;
    }

    /**
     * Print out information about this voice jarfile.
     */
    public static void main(String[] args) {
        System.out.println((new %CLASSNAME%()).toString());
    }
}

package de.dfki.lt.freetts.en.us;

import com.sun.speech.freetts.VoiceDirectory;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.Gender;
import com.sun.speech.freetts.Age;
import java.util.Locale;

import com.sun.speech.freetts.ValidationException;

//TODO documentation
public class MbrolaVoiceDirectory extends VoiceDirectory {
    
    public Voice[] getVoices() {
    
        Voice mbrola1 = new MbrolaVoice(true, "us1", "us1", 150f, 180F, 22F,
                "mbrola1", Gender.FEMALE, Age.YOUNGER_ADULT, "MBROLA Voice 1",
                Locale.US, "general", "mbrola");
        Voice mbrola2 = new MbrolaVoice(true, "us2", "us2", 150f, 115F, 12F,
                "mbrola2", Gender.MALE, Age.YOUNGER_ADULT, "MBROLA Voice 2",
                Locale.US, "general", "mbrola");
        Voice mbrola3 = new MbrolaVoice(true, "us3", "us3", 150f, 125F, 12F,
                "mbrola3", Gender.MALE, Age.YOUNGER_ADULT, "MBROLA Voice 3",
                Locale.US, "general", "mbrola");

        Voice[] voices = {mbrola1, mbrola2, mbrola3};

        for (int i = 0; i < voices.length; i++) {
            if (voices[i] == null) {
                throw new Error("Error instantiating MBROLA voice");
            }
            MbrolaVoiceValidator validator = new
                MbrolaVoiceValidator((MbrolaVoice) voices[i]);
            try {
                validator.validate();
            } catch (ValidationException ve) {
                throw new Error("Problem starting MBROLA voice: " + ve);
            }
        }

        return voices;
    }

    public static void main(String[] args) {
        System.out.println((new MbrolaVoiceDirectory()).toString());
    }
}

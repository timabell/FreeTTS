package com.sun.speech.freetts.en.us.cmu_us_kal;

import com.sun.speech.freetts.en.us.CMUDiphoneVoice;
import com.sun.speech.freetts.VoiceDirectory;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.Gender;
import com.sun.speech.freetts.Age;
import java.util.Locale;


//TODO documentation
public class KevinVoiceDirectory extends VoiceDirectory {
    public Voice[] getVoices() {
        //TODO different name?
        Voice kevin = new CMUDiphoneVoice(true, "kevin", Gender.MALE,
                Age.YOUNGER_ADULT, "default 8-bit diphone voice", Locale.US);
        Voice kevin16 = new CMUDiphoneVoice(true, "kevin16", Gender.MALE,
                Age.YOUNGER_ADULT, "default 16-bit diphone voice", Locale.US);

        kevin.getFeatures().setString(Voice.DATABASE_NAME,
                "cmu_us_kal/diphone_units.bin");
        kevin16.getFeatures().setString(Voice.DATABASE_NAME,
                "cmu_us_kal/diphone_units16.bin");

        Voice[] voices = {kevin, kevin16};
        return voices;
    }

    public static void main(String[] args) {
        System.out.println((new KevinVoiceDirectory()).toString());
    }
}

/**
 * Copyright 2003 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.JavaClipAudioPlayer;

/**
 * Simple program to demonstrate the use of the FreeTTS speech
 * synthesizer.  This simple program shows how to use FreeTTS
 * without requiring the Java Speech API (JSAPI).
 */
public class FreeTTSHelloWorld {

    /**
     * Example of how to list all the known voices.
     */
    public static void listAllVoices() {
        System.out.println();
        System.out.println("All voices available:");        
        VoiceManager voiceManager = VoiceManager.getInstance();
        Voice[] voices = voiceManager.getVoices();
        for (int i = 0; i < voices.length; i++) {
            System.out.println("    " + voices[i].getName()
                               + " (" + voices[i].getDomain() + " domain)");
        }
    }

    public static void main(String[] args) {

        listAllVoices();
        
        /* You can run "java -jar" on any of the voice jars that come
         * with FreeTTS to get a list of the names of the voices
         * contained in that file:
         *
         *    java -jar lib/cmu_us_kal.jar
         *    java -jar lib/cmu_time_awb.jar
         *    java -Dmbrola.base=/usr/local/mbrola -jar lib/mbrola.jar
         *
         * You can then use any one of the names as the argument to
         * this sample application.
         */
        String voiceName = (args.length > 0)
            ? args[0]
            : "kevin16";
        
        System.out.println();
        System.out.println("Using voice: " + voiceName);
        
        /* The VoiceManager manages all the voices for FreeTTS.
         */
        VoiceManager voiceManager = VoiceManager.getInstance();
        Voice helloVoice = voiceManager.getVoice(voiceName);
        
        /* Sets the AudioPlayer to the JavaClipAudioPlayer.
         * For more information on the various AudioPlayer
         * implementations available (e.g., saving to a file),
         * see the javadoc for AudioPlayer.  For an example
         * of streaming audio to a socket, see the
         * SocketAudioPlayer.java in demo/freetts/ClientServer.
         */
        helloVoice.setAudioPlayer(new JavaClipAudioPlayer());
        
        /* Allocates the resources for the voice.
         */
        helloVoice.allocate();
        
        /* Synthesize speech.
         */
        helloVoice.speak("Thank you for giving me a voice. "
                         + "I'm so glad to say hello to this world.");

        /* Clean up and leave.
         */
        helloVoice.deallocate();
        System.exit(0);
    }
}

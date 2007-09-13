/**
 * Copyright 2007 JVoiceXML group
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */

package org.jvoicexml.rtp.freetts.demo;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.media.MediaException;
import javax.media.rtp.SessionManagerException;

import org.jvoicexml.rtp.freetts.FreeTTSDataSource;
import org.jvoicexml.rtp.freetts.RtpAudioPlayer;
import org.jvoicexml.rtp.freetts.RtpServer;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.AudioPlayer;

/**
 * Simple program to use FreeTTS as an RTP data source.
 * @author Dirk Schnelle
 */
public class RtpServerDemo {

    /**
     * Example of how to list all the known voices.
     */
    public static void listAllVoices() {
        System.out.println();
        System.out.println("All voices available:");
        VoiceManager voiceManager = VoiceManager.getInstance();
        Voice[] voices = voiceManager.getVoices();
        for (int i = 0; i < voices.length; i++) {
            System.out.println("    " + voices[i].getName() + " ("
                    + voices[i].getDomain() + " domain)");
        }
    }

    /**
     * Starts the program
     * @param args none expected
     */
    public static void main(String[] args) {

        listAllVoices();

        String voiceName = (args.length > 0) ? args[0] : "kevin16";

        System.out.println();
        System.out.println("Using voice: " + voiceName);

        /*
         * The VoiceManager manages all the voices for FreeTTS.
         */
        VoiceManager voiceManager = VoiceManager.getInstance();
        Voice helloVoice = voiceManager.getVoice(voiceName);

        if (helloVoice == null) {
            System.err.println("Cannot find a voice named " + voiceName
                    + ".  Please specify a different voice.");
            System.exit(1);
        }

        RtpServer server = null;
        FreeTTSDataSource ds = new FreeTTSDataSource();
        AudioPlayer player = null;
        try {
            server = new RtpServer();
            server.addTarget(49150);
            player = new RtpAudioPlayer(ds);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SessionManagerException e) {
            e.printStackTrace();
        } catch (MediaException e) {
            e.printStackTrace();
        }

        /*
         * Allocates the resources for the voice.
         */
        helloVoice.allocate();

        /*
         * Synthesize speech.
         */
        helloVoice.setAudioPlayer(player);
        helloVoice.speak("This is the RTP test");
        try {
            server.initSendStream(ds);
            server.startSending();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MediaException e) {
            e.printStackTrace();
        }

        // Wait until all data is being delivered.
        ds.waitCompleted();
        ds.disconnect();
        
        /*
         * Clean up and leave.
         */
        helloVoice.deallocate();
        try {
            server.stopSending();
            server.dispose();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        System.exit(0);
    }
}

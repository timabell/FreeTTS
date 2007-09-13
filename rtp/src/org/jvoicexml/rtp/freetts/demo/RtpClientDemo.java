/**
 * Copyright 2007 JVoiceXML group
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */

package org.jvoicexml.rtp.freetts.demo;

import java.io.IOException;

import javax.media.ControllerClosedEvent;
import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.RealizeCompleteEvent;

/**
 * Simple program to play back an RTP audio stream.
 * 
 * @author Dirk Schnelle
 */
public class RtpClientDemo implements ControllerListener {
    public void controllerUpdate(ControllerEvent control) {
        Player player = (Player) control.getSourceController();
        // If player wasn't created successfully from controller, return
        if (player == null) {
            System.out.println("Player is null");
            return;
        }

        if (control instanceof RealizeCompleteEvent) {
            System.out.println("Starting player...");
            player.start();
        }

        if (control instanceof ControllerClosedEvent) {
            System.err.println("controller closed");
            System.exit(0);
        }

        if (control instanceof ControllerErrorEvent) {
            System.out.println("Error in ControllerErrorEvent: " + control);
            player.removeControllerListener(this);
            System.exit(0);
        }
    }

    /**
     * Starts the program
     * 
     * @param args
     *            none expected
     */
    public static void main(String[] args) {
        RtpClientDemo client = new RtpClientDemo();

        MediaLocator loc = new MediaLocator("rtp://127.0.0.1:49150/audio/1");
        Player player;
        try {
            player = javax.media.Manager.createPlayer(loc);
        } catch (NoPlayerException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        player.addControllerListener(client);

        player.realize();

        System.out.println("waiting for data...");
    }
}

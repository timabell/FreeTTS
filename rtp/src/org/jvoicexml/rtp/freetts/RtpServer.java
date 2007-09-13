/*
 * RTP demo for FreeTTS.
 *
 * Copyright (C) 2007 JVoiceXML group - http://jvoicexml.sourceforge.net
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 *
 */

package org.jvoicexml.rtp.freetts;

import java.io.IOException;
import java.net.InetAddress;

import javax.media.Manager;
import javax.media.MediaException;
import javax.media.Player;
import javax.media.Processor;
import javax.media.format.AudioFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.rtp.RTPManager;
import javax.media.rtp.SendStream;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.SessionManagerException;

/**
 * A general purpose RTP server based on JMF.
 * 
 * @author Dirk Schnelle
 */
public class RtpServer {
    /** Audio format. */
    public static final AudioFormat FORMAT_ULAR_RTP = new AudioFormat(
            AudioFormat.ULAW_RTP, 8000d, 8, 1, AudioFormat.LITTLE_ENDIAN,
            AudioFormat.SIGNED);

    /** The RTP manager. */
    private RTPManager rtpManager;

    /** The stream to send data. */
    private SendStream sendStream;

    /** The local IP address. */
    private SessionAddress localAddress;

    /** Waiting utility for processor events. */
    private ProcessorStateWaiter waiter;
    
    /**
     * Constructs a new object taking a free random port and this computer
     * as the local address. 
     * @throws IOException
     * @throws SessionManagerException
     * @throws MediaException
     */
    public RtpServer() throws IOException, SessionManagerException,
            MediaException {
        this(SessionAddress.ANY_PORT);
    }

    /**
     * Constructs a new object.
     * @param localPort local port number.
     * @throws IOException
     * @throws SessionManagerException
     * @throws MediaException
     */
    public RtpServer(int localPort) throws IOException,
            SessionManagerException, MediaException {
        rtpManager = RTPManager.newInstance();
        InetAddress localIp = InetAddress.getLocalHost();
        localAddress = new SessionAddress(localIp, localPort);
        rtpManager.initialize(localAddress);
        
        waiter = new ProcessorStateWaiter();
    }

    /**
     * Adds a remote JMF player on this computer.
     * @param remotePort port number of the JMF player.
     * @throws IOException
     * @throws SessionManagerException
     */
    public void addTarget(int remotePort) throws IOException,
            SessionManagerException {
        InetAddress ipAddress = InetAddress.getLocalHost();
        SessionAddress remoteAddress = new SessionAddress(ipAddress, remotePort);
        rtpManager.addTarget(remoteAddress);
    }

    /**
     * Adds a remote JMF player on the specified remote computer.
     * @param remoteHost name of the remote host.
     * @param remotePort port number of the JMF player.
     * @throws IOException
     * @throws SessionManagerException
     */
    public void addTarget(String remoteHost, int remotePort)
            throws IOException, SessionManagerException {
        InetAddress ipAddress = InetAddress.getByName(remoteHost);
        SessionAddress remoteAddress = new SessionAddress(ipAddress, remotePort);
        rtpManager.addTarget(remoteAddress);
    }

    /**
     * Initialize the send stream.
     * @param sendStreamSource datasource to send data from.
     * @throws IOException
     * @throws MediaException
     */
    public void initSendStream(DataSource sendStreamSource) throws IOException,
            MediaException {
        Processor proc = Manager.createProcessor(sendStreamSource);
        proc.configure();
        waiter.waitForState(proc, Processor.Configured);
        proc.setContentDescriptor(new ContentDescriptor(
                ContentDescriptor.RAW_RTP));
        proc.start();
        proc.getTrackControls()[0].setFormat(FORMAT_ULAR_RTP);
        waiter.waitForState(proc, Player.Started);
        sendStream = rtpManager.createSendStream(proc.getDataOutput(), 0);
    }

    /**
     * Start sending.
     * @throws IOException
     */
    public void startSending() throws IOException {
        sendStream.start();
    }

    /**
     * Stop sending.
     * @throws IOException
     */
    public void stopSending() throws IOException {
        sendStream.stop();
    }

    /**
     * Dispose.
     */
    public void dispose() {
        sendStream.close();
        rtpManager.removeTargets("Disconnected!");
        rtpManager.dispose();
    }
}

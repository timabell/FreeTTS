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

import javax.media.Time;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;

/**
 * A {@link javax.media.protocol.DataSource} for FreeTTS.
 * 
 * @author Dirk Schnelle
 */
public final class FreeTTSDataSource extends PullDataSource {
    /** The one and only source stream. */
    private FreeTTSPullSourceStream stream;

    /**
     * Constructs a new object.
     */
    public FreeTTSDataSource() {
        stream = new FreeTTSPullSourceStream();
    }

    public void waitCompleted() {
        stream.waitEndOfStream();

        // Wait some more time before terminating.
        // TODO replace this by a solution that uses the amount of data being
        // played back.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the completed
     */
    public boolean isCompleted() {
        return stream.endOfStream();
    }

    /**
     * {@inheritDoc}
     */
    public PullSourceStream[] getStreams() {
        PullSourceStream[] streams = new PullSourceStream[1];

        streams[0] = stream;

        return streams;
    }

    /**
     * {@inheritDoc}
     */
    public void connect() throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    public void disconnect() {
    }

    /**
     * {@inheritDoc}
     */
    public String getContentType() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Object getControl(final String arg0) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getControls() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Time getDuration() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void start() throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    public void stop() throws IOException {
    }
}

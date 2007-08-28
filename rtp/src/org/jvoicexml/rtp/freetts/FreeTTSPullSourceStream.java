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
import java.io.InputStream;

import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullSourceStream;
import javax.media.protocol.SourceStream;

/**
 * A {@link javax.media.protocol.SourceStream} to send the data coming
 * from FreeTTS. This is in fact a general purpose 
 * {@link javax.media.protocol.SourceStream} for any 
 * {@link java.io.InputStream}.
 * 
 * @author Dirk Schnelle
 */
public final class FreeTTSPullSourceStream implements PullSourceStream {
    /** No controls allowed. */
    private static final Object[] EMPTY_OBJECT_ARRAY = {};

    /** The input stream to read data from. */
    private InputStream in;
    
    /**
     * Sets the input stream.
     * @param input the input stream.
     */
    public void setInstream(InputStream input) {
        in = input;
    }

    /**
     * {@inheritDoc}
     */
    public int read(byte[] bytes, int start, int offset) throws IOException {
        if (in == null) {
            return 0;
        }
        return in.read(bytes, start, offset);
    }

    /**
     * {@inheritDoc}
     */
    public boolean willReadBlock() {
        try {
            return in.available() > 0;
        } catch (IOException e) {
           return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean endOfStream() {
        try {
            return in.available() == 0;
        } catch (IOException e) {
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    public ContentDescriptor getContentDescriptor() {
        return new ContentDescriptor(ContentDescriptor.RAW);
    }

    /**
     * {@inheritDoc}
     */
    public long getContentLength() {
        return SourceStream.LENGTH_UNKNOWN;
    }

    /**
     * {@inheritDoc}
     */
    public Object getControl(final String controlType) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getControls() {
        return EMPTY_OBJECT_ARRAY;
    }
}

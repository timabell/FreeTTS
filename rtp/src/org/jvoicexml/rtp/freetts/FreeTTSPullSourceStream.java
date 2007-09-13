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
    
    /** The number of bytes read so far. */
    private int num = 0;

    /** Maximum number of bytes to read. */
    private int max;
    
    private Integer waitLock = new Integer(0);
    
    /**
     * Sets the input stream.
     * @param input the input stream.
     */
    public void setInstream(InputStream input) {
        in = input;
        num = 0;
        try {
            max = in.available();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    public int read(byte[] bytes, int start, int length) throws IOException {
        if (in == null) {
            return 0;
        }
        
        int readBytes = in.read(bytes, start, length);

        num += length;
        if (num == max) {
            synchronized (waitLock) {
                waitLock.notifyAll();
            }
        }
        
        return readBytes;
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

    public void waitEndOfStream() {
        synchronized (waitLock) {
            try {
                waitLock.wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }
        }
    }
    /**
     * {@inheritDoc}
     */
    public boolean endOfStream() {
        return max == num;
    }

    /**
     * {@inheritDoc}
     */
    public ContentDescriptor getContentDescriptor() {
        return new ContentDescriptor(ContentDescriptor.RAW_RTP);
    }

    /**
     * {@inheritDoc}
     */
    public long getContentLength() {
        return max;
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

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

import javax.media.ControllerClosedEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Processor;

/**
 * A general purpose RTP server based on JMF.
 * 
 * @author Dirk Schnelle
 * @author Emil Ivov (http://www.emcho.com)
 * @version $Revision: 1.1 $
 * 
 * <p>
 * Based on the ProcessorUtility of the GJTAPI project.
 * </p>
 * 
 * <p>
 * Copyright &copy; 2003 LSIIT laboratory (http://lsiit.u-strasbg.fr)
 * </p>
 * <p>
 * Copyright &copy; 2007 JVoiceXML group - <a
 * href="http://jvoicexml.sourceforge.net"> http://jvoicexml.sourceforge.net/</a>
 * </p>
 * 
 * @since 0.6
 */
final class ProcessorStateWaiter implements ControllerListener {
    /** The locking object. */
    private final Integer stateLock = new Integer(0);

    /** <code>true</code> if the processor has been closed. */
    private boolean failed;

    /**
     * Constructs a new object.
     */
    public ProcessorStateWaiter() {
    }

    /**
     * {@inheritDoc}
     */
    public void controllerUpdate(final ControllerEvent event) {
        // If there was an error during configure or
        // realize, the processor will be closed
        if (event instanceof ControllerClosedEvent) {
            failed = true;
        }
        // All controller events, send a notification
        // to the waiting thread in waitForState method.
        if (event instanceof ControllerEvent) {
            synchronized (stateLock) {
                stateLock.notifyAll();
            }
        }
    }

    /**
     * Waits until the given state is reached by the processor.
     * 
     * @param processor
     *            the processor
     * @param state
     *            the state to reach
     * @return <code>true</code> if the state was reached.
     */
    synchronized boolean waitForState(final Processor processor, final int state) {
        processor.addControllerListener(this);
        failed = false;
        // Call the required method on the processor
        // Wait until we get an event that confirms the
        // success of the method, or a failure event.
        while ((processor.getState() != state) && !failed) {
            synchronized (stateLock) {
                try {
                    stateLock.wait();
                } catch (InterruptedException ie) {
                    return false;
                }
            }
        }

        processor.removeControllerListener(this);

        return !failed;
    }
}

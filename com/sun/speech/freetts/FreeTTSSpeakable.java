/**
 * Portions Copyright 2001 Sun Microsystems, Inc.
 * Portions Copyright 1999-2001 Language Technologies Institute, 
 * Carnegie Mellon University.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package com.sun.speech.freetts;

import org.w3c.dom.Document;
import java.io.InputStream;

/**
 * Represents something that should be spoken.
 */
public interface FreeTTSSpeakable {

    /**
     * Indicates that this speakable has been started.
     */
    public void started();

    /**
     * Indicates that this speakable has been completed.
     */
    public void completed();

    /**
     * Indicates that this speakable has been cancelled.
     */
    public void cancelled();

    /**
     * Returns <code>true</code> if this queue item has been 
     * processed.
     *
     * @return true if it has been processed
     */
    public boolean isCompleted();

    /**
     * Waits for this speakable item to be completed.
     *
     * @return true if the item was completed successfully, false if
     *   the speakable was cancelled or an error occurred.
     */
    public boolean waitCompleted();

   /**
    * Returns <code>true</code> if the item contains plain text
    * (not Java Speech Markup Language text).
    *
    * @return true if the item contains plain text
    */
    public boolean isPlainText();

   /**
    * Returns <code>true</code> if the item is an input stream.
    *
    * @return true if the item is an input stream
    */
    public boolean isStream();

   /**
    * Returns <code>true</code> if the item is a JSML document
    * (Java Speech Markup Language).
    *
    * @return true if the item is a document
    */
    public boolean isDocument();

    /**
     * Returns the text corresponding to this Playable.
     *
     * @return the Playable text
     */
    public String getText();

    /**
     * Gets the DOM document for this object.
     *
     * @return the DOM document for this object
     */
    public Document getDocument();

    /**
     * Gets the input stream
     *
     * @return the input stream
     */
    public InputStream getInputStream();
}

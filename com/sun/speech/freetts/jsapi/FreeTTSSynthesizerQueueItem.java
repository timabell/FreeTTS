/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package com.sun.speech.freetts.jsapi;

import javax.speech.Engine;
import javax.speech.synthesis.Speakable;
import javax.speech.synthesis.SpeakableEvent;

import com.sun.speech.engine.synthesis.BaseSynthesizer;
import com.sun.speech.engine.synthesis.BaseSynthesizerQueueItem;
import com.sun.speech.freetts.FreeTTSSpeakable;

import java.net.URL;
import java.io.IOException;
import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Represents an object on the speech output queue of a
 * <code>FreeTTSSynthesizer</code>.
 * Extends the BaseSynthesizerQueueItem by allowing access to the DOM
 * document.
 */

public class FreeTTSSynthesizerQueueItem extends BaseSynthesizerQueueItem 
		implements FreeTTSSpeakable {
    /**
     * Construct a queue item.
     */
    public FreeTTSSynthesizerQueueItem() {
        super();
    }

    /**
     * Gets the DOM document for this object.
     *
     * @return the DOM document for this object.
     */
    public Document getDocument() {
	return super.getDocument();
    }

   /**
    * Returns <code>true</code> if the item is an input stream
    *
    * @return true if the item is an input stream;
    *   otherwise, returns <code> false </code>
    */
    public boolean isStream() {
	return false;
    }

    /**
     * Gets the input stream
     *
     * @return the input stream
     */
    public InputStream getInputStream() {
	return null;
    }

   /**
    * Returns <code>true</code> if the item is a JSML document
    * (Java Speech Markup Language text).
    *
    * @return <code> true </code> if the item is a document; 
    *   otherwise, returns <code> false </code>
    */
    public boolean isDocument() {
	return super.getDocument() != null;
    }



}

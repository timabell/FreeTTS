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
package com.sun.speech.freetts.en.us;


/**
 * Defines limited domain synthesis voice that specializes
 * in telling the time (with an english accent).
 */
public class CMUTimeAWBVoice extends CMUClusterUnitVoice {
    /**
     * Creates a simple voice. By default, no Lexicon is loaded.
     */
    public CMUTimeAWBVoice() {
	super(false);
	setLexicon(new CMULexicon("cmutimelex"));
    }

    /**
     * Converts this object to a string
     * 
     * @return a string representation of this object
     */
    public String toString() {
	return "CMUTimeAWBVoice";
    }
}

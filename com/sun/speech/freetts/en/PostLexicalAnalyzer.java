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
package com.sun.speech.freetts.en;

import com.sun.speech.freetts.UtteranceProcessor;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.Utterance;
import com.sun.speech.freetts.Item;
import com.sun.speech.freetts.Relation;
import com.sun.speech.freetts.ProcessException;
import com.sun.speech.freetts.PathExtractorImpl;
import com.sun.speech.freetts.PathExtractor;


/**
 * Annotates the utterance with post lexical information.
 */
public class PostLexicalAnalyzer implements UtteranceProcessor {
    private static final PathExtractor wordPath =
	new PathExtractorImpl("R:SylStructure.parent.parent.name", true);

    /**
     * Constructs a PostLexicalAnalyzer
     */
     public PostLexicalAnalyzer () {
     }

    /**
     * Performs the post lexical processing.
     *
     * @param  utterance  the utterance to process
     *
     * @throws ProcessException if an error occurs while
     *         processing of the utterance
     */
    public void processUtterance(Utterance utterance) throws ProcessException {
	fixApostropheS(utterance);
    }

    /**
     * Fixes apostrophe s segments.
     *
     * @param utterance the utterance to fix
     */
    private void fixApostropheS(Utterance utterance) {
	Voice voice = utterance.getVoice();
	for (Item item = utterance.getRelation(Relation.SEGMENT).getHead();
		item != null;
		item = item.getNext()) {
	    if (wordPath.findFeature(item).equals("'s")) {

		String pname = item.getPrevious().toString();

		if (("fa".indexOf(
			    voice.getPhoneFeature(pname,"ctype")) != -1) &&
		    ("dbg".indexOf(
			    voice.getPhoneFeature(pname, "cplace")) == -1)) {
		    Item schwa = item.prependItem(null);
		    schwa.getFeatures().setString("name", "ax");
		    item.getItemAs(
                        Relation.SYLLABLE_STRUCTURE).prependItem(schwa);
		} else  if (voice.getPhoneFeature(pname, "cvox").equals("-")) {
		    item.getFeatures().setString("name", "s");

		}
	    }
	}
    }

    /**
     * Returns the string representation of the object
     *
     * @return the string representation of the object
     */
    public String toString() {
        return "PostLexicalAnalyzer";
    }
}


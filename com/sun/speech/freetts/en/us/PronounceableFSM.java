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

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.sun.speech.freetts.Item;
import com.sun.speech.freetts.Relation;
import com.sun.speech.freetts.UtteranceProcessor;
import com.sun.speech.freetts.Utterance;
import com.sun.speech.freetts.FeatureSet;
import com.sun.speech.freetts.ProcessException;
import com.sun.speech.freetts.util.Utilities;

/**
 * Implements a finite state machine that checks if a given string
 * is pronounceable. If it is pronounceable, the method
 * <code>accept()</code> will return true.
 */
public class PronounceableFSM {

    /**
     * The vocabulary size.
     */
    protected int vocabularySize;

    /**
     * The transitions of this FSM
     */
    protected int[] transitions;

    /**
     * Whether we should scan the input string from the front.
     */
    protected boolean scanFromFront;

    /**
     * Constructs a PronounceableFSM with the given 
     */
    public PronounceableFSM(int vocabularySize, int[] transitions,
			    boolean scanFromFront) {
	this.vocabularySize = vocabularySize;
	this.transitions = transitions;
	this.scanFromFront = scanFromFront;
    }

    /**
     * Causes this FSM to transition to the next state given
     * the current state and input symbol.
     *
     * @param state the current state
     * @param symbol the input symbol
     */
    private int transition(int state, int symbol) {
	for (int i = state; i < transitions.length; i++) {
	    if ((transitions[i] % vocabularySize) == symbol) {
		return (transitions[i] / vocabularySize);
	    }
	}
	return -1;
    }

    /**
     * Checks to see if this finite state machine accepts the given
     * input string.
     *
     * @param inputString the input string to be tested
     * @param startFromFront whether the reading of the input string
     * should start from the front or from the end (i.e. in reverse
     * order)
     *
     * @return true if this FSM accepts, false if it rejects
     */
    public boolean accept(String inputString) {
	int symbol;
	int state = transition(0, '#');
	int leftEnd = inputString.length() - 1;
	int start = (scanFromFront) ? 0 : leftEnd;
	
	for (int i = start; 0 <= i && i <= leftEnd; ) {
	    char c = inputString.charAt(i);
	    if (c == 'n' || c == 'm') {
		symbol = 'N';
	    } else if ("aeiouy".indexOf(c) != -1) {
		symbol = 'V';
	    } else {
		symbol = c;
	    }
	    state = transition(state, symbol);
	    if (state == -1) {
		return false;
	    } else if (symbol == 'V') {
		return true;
	    }
	    if (scanFromFront) {
		i++;
	    } else {
		i--;
	    }
	}
	return false;
    }
}







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

import com.sun.speech.freetts.FeatureSet;
import com.sun.speech.freetts.FeatureSetImpl;
import com.sun.speech.freetts.Item;
import com.sun.speech.freetts.Relation;
import com.sun.speech.freetts.Utterance;

public class WordRelation {

    private Relation relation;
    private TokenToWords tokenToWords;


    private WordRelation(Relation parentRelation, TokenToWords tokenToWords) {
	this.relation = parentRelation;
	this.tokenToWords = tokenToWords;
    }


    public static WordRelation createWordRelation(Utterance utterance,
						  TokenToWords tokenToWords) {
	Relation relation = utterance.createRelation(Relation.WORD);
	return new WordRelation(relation, tokenToWords);
    }


    /**
     * Adds a break as a feature to the last item in the list.
     */
    public void addBreak() {
	Item wordItem = (Item) relation.getTail();
	if (wordItem != null) {
	    FeatureSet featureSet = new FeatureSetImpl();
	    featureSet.setString("break", "1");
	}
    }


    public void addWord(String word) {
	Item tokenItem = tokenToWords.getTokenItem();
	assert (tokenItem != null);
	Item wordItem = tokenItem.createDaughter();
	FeatureSet featureSet = wordItem.getFeatures();
	featureSet.setString("name", word);
	relation.appendItem(wordItem);
    }


    public void setLastWord(String word) {
	Item lastItem = relation.getTail();
	FeatureSet featureSet = lastItem.getFeatures();
	featureSet.setString("name", word);
    }

    
    public Item getTail() {
	return relation.getTail();
    }
}

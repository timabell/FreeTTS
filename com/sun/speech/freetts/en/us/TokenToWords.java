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
import com.sun.speech.freetts.cart.CART;
import com.sun.speech.freetts.Item;
import com.sun.speech.freetts.Relation;
import com.sun.speech.freetts.UtteranceProcessor;
import com.sun.speech.freetts.Utterance;
import com.sun.speech.freetts.FeatureSet;
import com.sun.speech.freetts.ProcessException;
import com.sun.speech.freetts.util.Utilities;


/**
 * Converts the Tokens (in US English words) in an 
 * Utterance into a list of words. It puts the produced list back
 * into the Utterance. Usually, the tokens that gets expanded are numbers
 * like "23" (to "twenty" "three").
 * <p>
 * It translates the following code from flite:
 * <br>
 * <code>
 * lang/usenglish/us_text.c
 * </code>
 */
public class TokenToWords implements UtteranceProcessor {

    /** Regular expression for something that has a vowel */
    private static final String RX_HAS_VOWEL = ".*[aeiouAEIOU].*";    

    // a CART for classifying numbers
    private CART cart;
                        
    private static Pattern commaIntPattern;
    private static Pattern digitsPattern;
    private static Pattern doublePattern;
    private static Pattern ordinalPattern;
    private static Pattern alphabetPattern;
    private static Pattern hasVowelPattern;
    
    static {
	commaIntPattern = Pattern.compile(USEnglish.RX_COMMAINT);
	digitsPattern = Pattern.compile(USEnglish.RX_DIGITS);
	doublePattern = Pattern.compile(USEnglish.RX_DOUBLE);
	ordinalPattern = Pattern.compile(USEnglish.RX_ORDINAL_NUMBER);
	alphabetPattern = Pattern.compile(USEnglish.RX_ALPHABET);
	hasVowelPattern = Pattern.compile(RX_HAS_VOWEL);
    }

    private int commaIntCount = 0;
    private int digitsCount = 0;
    private int doubleCount = 0;
    private int ordinalCount = 0;
    private int alphabetCount = 0;
    private int hasVowelCount = 0;
    private int wordCount = 0;
    private int quoteCount = 0;
    
    /**
     * Print statistics (used for debugging)
     */
    public void printStats() {
	System.out.println("CommaInts: " + commaIntCount);
	System.out.println("digits: " + digitsCount);
	System.out.println("double: " + doubleCount);
	System.out.println("ordinal: " + ordinalCount);
	System.out.println("alphabet: " + alphabetCount);
	System.out.println("word: " + wordCount);
	System.out.println("quotes: " + quoteCount);
    }
    

    /**
     * Constructs a default USTokenWordProcessor. It uses the USEnglish
     * regular expression set (USEngRegExp) by default.
     *
     * @param usNumbersCART the cart to use to classify numbers
     */
    public TokenToWords(CART usNumbersCART) {
	this.cart = usNumbersCART;
    }

    /**
     *  process the utterance
     *
     * @param  utterance  the utterance contain the tokens
     *
     * @throws ProcessException if an IOException is thrown during the
     *         processing of the utterance
     */
    public void processUtterance(Utterance utterance) throws ProcessException {
	Relation tokenRelation;
	if ((tokenRelation = utterance.getRelation(Relation.TOKEN)) == null) {
	    throw new IllegalStateException
		("TokenToWords: Token relation does not exist");
	}
	
	Item tokenItem, wordItem;
	Relation wordRelation = utterance.createRelation(Relation.WORD);
	
	for (tokenItem = tokenRelation.getHead();
	     tokenItem != null;
	     tokenItem = tokenItem.getNext()) {

	    FeatureSet featureSet = tokenItem.getFeatures();
	    String tokenVal = featureSet.getString("name");
	    
	    // convert the token into a list of words
	    List wordList = new LinkedList();
	    tokenToWords(tokenItem, tokenVal, wordList);
	    
	    for (Iterator wordListIterator = wordList.iterator();
		 wordListIterator.hasNext();) {
		
		wordItem = tokenItem.createDaughter();
		
		String wordVal = (String) wordListIterator.next();
		FeatureSet featureSet2 = wordItem.getFeatures();
		featureSet2.setString("name", wordVal);

		// add the word to the WordRelation
		wordRelation.appendItem(wordItem);
	    }
	}
	// printStats();
    }
    

    /**
     * Converts the given Token into a list of words.
     *
     * @param  tokenItem  the Item that stores the token
     * @param  tokenVal  the String value of the token, which may or may not be
     *                   same as the one in tokenItem, called "name" in flite
     * @param  wordList  words are added to this list
     *
     * 
     * @return  a list of words
     */
    protected List tokenToWords(Item tokenItem, String tokenVal,
				List wordList) {

	String nsw = "";
	FeatureSet tokenFeatures = tokenItem.getFeatures();
	String aaa; // not sure what 'aaa' means
	int index;
	int tokenLength = tokenVal.length();
			
	if (tokenFeatures.isPresent("nsw")) {
	    nsw = tokenFeatures.getString("nsw");
	}

	if (matches(alphabetPattern, tokenVal) &&
	    matches(hasVowelPattern, tokenVal)) {
	    
	    // alphabetCount++;
	    
            /* just a word */
            wordList.add(tokenVal.toLowerCase());
	    return wordList;
	} else if (matches(commaIntPattern, tokenVal)) {

	    // commaIntCount++;
	    aaa = Utilities.deleteChar(tokenVal, ','); // remove all commas
	    tokenToWords(tokenItem, aaa, wordList);

	} else if (matches(digitsPattern, tokenVal)) {

	    // digitsCount++;
	    if (nsw.equals("nide")) {
		NumberExpander.expandID(tokenVal, wordList);
	    } else {
		String digitsType = (String) cart.interpret(tokenItem);
		if (digitsType.equals("ordinal")) {
		    NumberExpander.expandOrdinal(tokenVal, wordList);
		} else if (digitsType.equals("digits")) {
		    NumberExpander.expandDigits(tokenVal, wordList);
		} else if (digitsType.equals("year")) {
		    NumberExpander.expandID(tokenVal, wordList);
		} else {
		    NumberExpander.expandNumber(tokenVal, wordList);
		}
	    }
	} else if (matches(doublePattern, tokenVal)) {

	    // doubleCount++;
	    if (tokenVal != null && tokenVal.charAt(0) == '-') {
		wordList.add("minus");
		tokenToWords
		    (tokenItem, tokenVal.substring(1,tokenLength), wordList);
	    } else if ((index = tokenVal.indexOf('e')) != -1 ||
		       (index = tokenVal.indexOf('E')) != -1) {
		aaa = tokenVal.substring(0, index);
		tokenToWords(tokenItem, aaa, wordList);
		wordList.add("e");
		tokenToWords(tokenItem, aaa, wordList);
	    } else if ((index = tokenVal.indexOf('.')) != -1) {
		aaa = tokenVal.substring(0, index);
		NumberExpander.expandNumber(aaa, wordList);
		wordList.add("point");
		NumberExpander.expandDigits
		    (tokenVal.substring(index+1,tokenLength), wordList);
	    } else {
		/* I don't think you can get here. */
		NumberExpander.expandNumber(tokenVal, wordList);
	    }
	} else if (matches(ordinalPattern, tokenVal)) {

	    // ordinalCount++;
	    
	    /* explicit ordinals */
	    aaa = tokenVal.substring(0, tokenLength - 2);
	    NumberExpander.expandOrdinal(aaa, wordList);
	} else if (((index = tokenVal.indexOf("'s")) != -1 ||
		    (index = tokenVal.indexOf("'S")) != -1) &&
		   (tokenLength - index) == 2) {
	    /* apostrophe s */
	    aaa = tokenVal.substring(0, index);
	    tokenToWords(tokenItem, aaa, wordList);
	    wordList.add("'s");
	} else if ((index = tokenVal.indexOf('\'')) != -1) {

	    // quoteCount++;
	    	    
	    /* internal single quote deleted */
	    StringBuffer buffer = new StringBuffer(tokenVal);
	    buffer.deleteCharAt(index);
	    
	    tokenToWords(tokenItem, buffer.toString(), wordList);
	    
	} else if (tokenLength > 1 &&
		   matches(alphabetPattern, tokenVal) &&
		   !matches(hasVowelPattern, tokenVal) &&
		   tokenVal.indexOf('y') == -1 &&
		   tokenVal.indexOf('Y') == -1) {
	    /* unpronouncable list of alphas */
	    NumberExpander.expandLetters(tokenVal, wordList);

	} else if ((index = tokenVal.indexOf('-')) != -1) {
	    /* aaa-bbb */
	    aaa = tokenVal.substring(0, index);
	    String bbb = tokenVal.substring(index+1, tokenLength);
	    
	    tokenToWords(tokenItem, aaa, wordList);
	    tokenToWords(tokenItem, bbb, wordList);

	} else if (tokenLength > 1 &&
		   !matches(alphabetPattern, tokenVal)) {
	    /* its not just alphas */
	    for (index = 0; index < tokenLength; index++) {
		if (isTextSplitable(tokenVal, index)) {
		    break;
		}
	    }

	    aaa = tokenVal.substring(0, index+1);
	    String bbb = tokenVal.substring(index+1, tokenLength);
	    
	    FeatureSet featureSet = tokenItem.getFeatures();
	    featureSet.setString("nsw", "nide");
	    tokenToWords(tokenItem, aaa, wordList);
	    tokenToWords(tokenItem, bbb, wordList);
	} else { /* buckets of other stuff missing */

	    // wordCount++;
	    	    
	    /* just a word */
	    wordList.add(tokenVal.toLowerCase());
	}
	
	return wordList;
    }


    /**
     * Determines if the given input matches the given Pattern.
     *
     * @param pattern the pattern to match
     * @param input the string to test
     *
     * @return <code>true</code> if the input string matches the given Pattern;
     *         <code>false</code> otherwise
     */
    private static boolean matches(Pattern pattern, String input) {
	Matcher m = pattern.matcher(input);
	return m.matches();
    }
    
    /**
     * Determines if the character at the given position of the given
     * input text is splittable. A character is splittable if:
     * <p>
     * 1) the character and the following character are not letters
     *    in the English alphabet (A-Z and a-z)
     * <p>
     * 2) the character and the following character are not digits (0-9)
     * <p>
     * @param text the text containing the character of interest
     * @param index the index of the character of interest
     * 
     * @return true if the position of the given text is splittable
     *         false otherwise
     */ 
    private static boolean isTextSplitable(String text, int index) {

	char c0 = text.charAt(index);
	char c1 = text.charAt(index+1);
	
	if ((('a' <= c0 && c0 <= 'z') || ('A' <= c0 && c0 <= 'Z')) &&
	    (('a' <= c1 && c1 <= 'z') || ('A' <= c1 && c1 <= 'Z'))) {
	    return false;
	} else if (('0' <= c0 && c0 <= '9') && ('0' <= c1 && c1 <= '9')) {
	    return false;
	} else {
	    return true;
	}
    }

    /**
     * Converts this object to its String representation
     * 
     * @return the string representation of this object
     */
    public String toString() {
	return "TokenToWords";
    }
}





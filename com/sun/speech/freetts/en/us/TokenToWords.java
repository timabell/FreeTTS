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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.sun.speech.freetts.FeatureSet;
import com.sun.speech.freetts.FeatureSetImpl;
import com.sun.speech.freetts.Item;
import com.sun.speech.freetts.PathExtractor;
import com.sun.speech.freetts.PathExtractorImpl;
import com.sun.speech.freetts.ProcessException;
import com.sun.speech.freetts.Relation;
import com.sun.speech.freetts.Utterance;
import com.sun.speech.freetts.UtteranceProcessor;
import com.sun.speech.freetts.cart.CART;
import com.sun.speech.freetts.util.Utilities;


/**
 * Converts the Tokens (in US English words) in an 
 * Utterance into a list of words. It puts the produced list back
 * into the Utterance. Usually, the tokens that gets expanded are numbers
 * like "23" (to "twenty" "three").
 * <p> * It translates the following code from flite:
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
                        
    // Patterns for regular expression matching
    private static Pattern alphabetPattern;
    private static Pattern commaIntPattern;
    private static Pattern digits2DashPattern;
    private static Pattern digitsPattern;
    private static Pattern digitsSlashDigitsPattern;
    private static Pattern dottedAbbrevPattern;
    private static Pattern doublePattern;
    private static Pattern drStPattern;
    private static Pattern fourDigitsPattern;
    private static Pattern hasVowelPattern;
    private static Pattern illionPattern;
    private static Pattern numberTimePattern;
    private static Pattern numessPattern;
    private static Pattern ordinalPattern;
    private static Pattern romanNumbersPattern;
    private static Pattern sevenPhoneNumberPattern;
    private static Pattern threeDigitsPattern;
    private static Pattern usMoneyPattern;
    
    static {
	alphabetPattern = Pattern.compile(USEnglish.RX_ALPHABET);
	commaIntPattern = Pattern.compile(USEnglish.RX_COMMAINT);
	digits2DashPattern = Pattern.compile(USEnglish.RX_DIGITS2DASH);
	digitsPattern = Pattern.compile(USEnglish.RX_DIGITS);
	digitsSlashDigitsPattern = Pattern.compile(USEnglish.RX_DIGITSSLASHDIGITS);
	dottedAbbrevPattern = Pattern.compile(USEnglish.RX_DOTTED_ABBREV);
	doublePattern = Pattern.compile(USEnglish.RX_DOUBLE);
	drStPattern = Pattern.compile(USEnglish.RX_DRST);
	fourDigitsPattern = Pattern.compile(USEnglish.RX_FOUR_DIGIT);
	hasVowelPattern = Pattern.compile(USEnglish.RX_HAS_VOWEL);
	illionPattern = Pattern.compile(USEnglish.RX_ILLION);
	numberTimePattern = Pattern.compile(USEnglish.RX_NUMBER_TIME);
	numessPattern = Pattern.compile(USEnglish.RX_NUMESS);
	ordinalPattern = Pattern.compile(USEnglish.RX_ORDINAL_NUMBER);
	romanNumbersPattern = Pattern.compile(USEnglish.RX_ROMAN_NUMBER);
	sevenPhoneNumberPattern = Pattern.compile(USEnglish.RX_SEVEN_DIGIT_PHONE_NUMBER);
	threeDigitsPattern = Pattern.compile(USEnglish.RX_THREE_DIGIT);
	usMoneyPattern = Pattern.compile(USEnglish.RX_US_MONEY);
    }

    // King-like words 
    private static final String[] kingNames = {
	"louis", "henry", "charles", "philip", "george",
	"edward", "pius", "william", "richard", "ptolemy",
	"john", "paul", "peter", "nicholas", "frederick",
	"james", "alfonso", "ivan", "napoleon", "leo",
	"gregory", "catherine", "alexandria", "pierre", "elizabeth",
	"mary" };
    
    private static final String[] kingTitles = {
	"king", "queen", "pope", "duke", "tsar",
	"emperor", "shah", "caesar", "duchess", "tsarina",
	"empress", "baron", "baroness", "sultan", "count",
	"countess" };

    // Section-like words
    private static final String[] sectionTypes = {
	"section", "chapter", "part", "phrase", "verse",
	"scene", "act", "book", "volume", "chap",
	"war", "apollo", "trek", "fortran" };
    
    /**
     * Here we use a hashtable for constant time matching, instead of using
     * if (A.equals(B) || A.equals(C) || ...) to match Strings
     */
    private static Hashtable kingSectionLikeHash = new Hashtable();

    private static final String KING_NAMES = "kingNames";
    private static final String KING_TITLES = "kingTitles";
    private static final String SECTION_TYPES = "sectionTypes";

    // Hashtable initialization
    static {
	for (int i = 0; i < kingNames.length; i++) {
	    kingSectionLikeHash.put(kingNames[i], KING_NAMES);
	}
	for (int i = 0; i < kingTitles.length; i++) {
	    kingSectionLikeHash.put(kingTitles[i], KING_TITLES);
	}
	for (int i = 0; i < sectionTypes.length; i++) {
	    kingSectionLikeHash.put(sectionTypes[i], SECTION_TYPES);
	}
    }

    private static final String[] postrophes = {
	"'s", "'ll", "'ve", "'d" };

    // Finite state machines to check if a Token is pronounceable
    private PronounceableFSM prefixFSM = null;
    private PronounceableFSM suffixFSM = null;

    // List of US states abbreviations and their full names
    private static String[][] usStates =
    {
	{ "AL", "ambiguous", "alabama"  },
	{ "Al", "ambiguous", "alabama"  },
	{ "Ala", "", "alabama"  },
	{ "AK", "", "alaska"  },
	{ "Ak", "", "alaska"  },
	{ "AZ", "", "arizona"  },
	{ "Az", "", "arizona"  },
	{ "CA", "", "california"  },
	{ "Ca", "", "california"  },
	{ "Cal", "ambiguous", "california"  },
	{ "Calif", "", "california"  },
	{ "CO", "ambiguous", "colorado"  },
	{ "Co", "ambiguous", "colorado"  },
	{ "Colo", "", "colorado"  },
	{ "DC", "", "d" , "c" },
	{ "DE", "", "delaware"  },
	{ "De", "ambiguous", "delaware"  },
	{ "Del", "ambiguous", "delaware"  },
	{ "FL", "", "florida"  },
	{ "Fl", "ambiguous", "florida"  },
	{ "Fla", "", "florida"  },
	{ "GA", "", "georgia"  },
	{ "Ga", "", "georgia"  },
	{ "HI", "", "hawaii"  },
	{ "Hi", "ambiguous", "hawaii"  },
	{ "IA", "", "indiana"  },
	{ "Ia", "ambiguous", "indiana"  },
	{ "Ind", "ambiguous", "indiana"  },
	{ "ID", "ambiguous", "idaho"  },
	{ "IL", "ambiguous", "illinois"  },
	{ "Il", "ambiguous", "illinois"  },
	{ "ILL", "ambiguous", "illinois"  },
	{ "KS", "", "kansas"  },
	{ "Ks", "", "kansas"  },
	{ "Kans", "", "kansas"  },
	{ "KY", "ambiguous", "kentucky"  },
	{ "Ky", "ambiguous", "kentucky"  },
	{ "LA", "ambiguous", "louisiana"  },
	{ "La", "ambiguous", "louisiana"  },
	{ "Lou", "ambiguous", "louisiana"  },
	{ "Lous", "ambiguous", "louisiana"  },
	{ "MA", "ambiguous", "massachusetts"  },
	{ "Mass", "ambiguous", "massachusetts"  },
	{ "Ma", "ambiguous", "massachusetts"  },
	{ "MD", "ambiguous", "maryland"  },
	{ "Md", "ambiguous", "maryland"  },
	{ "ME", "ambiguous", "maine"  },
	{ "Me", "ambiguous", "maine"  },
	{ "MI", "", "michigan"  },
	{ "Mi", "ambiguous", "michigan"  },
	{ "Mich", "ambiguous", "michigan"  },
	{ "MN", "ambiguous", "minnestota"  },
	{ "Minn", "ambiguous", "minnestota"  },
	{ "MS", "ambiguous", "mississippi"  },
	{ "Miss", "ambiguous", "mississippi"  },
	{ "MT", "ambiguous", "montanna"  },
	{ "Mt", "ambiguous", "montanna"  },
	{ "MO", "ambiguous", "missouri"  },
	{ "Mo", "ambiguous", "missouri"  },
	{ "NC", "ambiguous", "north" , "carolina" },
	{ "ND", "ambiguous", "north" , "dakota" },
	{ "NE", "ambiguous", "nebraska"  },
	{ "Ne", "ambiguous", "nebraska"  },
	{ "Neb", "ambiguous", "nebraska"  },
	{ "NH", "ambiguous", "new" , "hampshire" },
	{ "NV", "", "nevada"  },
	{ "Nev", "", "nevada"  },
	{ "NY", "", "new" , "york" },
	{ "OH", "ambiguous", "ohio"  },
	{ "OK", "ambiguous", "oklahoma"  },
	{ "Okla", "", "oklahoma"  },
	{ "OR", "ambiguous", "oregon"  },
	{ "Or", "ambiguous", "oregon"  },
	{ "Ore", "ambiguous", "oregon"  },
	{ "PA", "ambiguous", "pennsylvania"  },
	{ "Pa", "ambiguous", "pennsylvania"  },
	{ "Penn", "ambiguous", "pennsylvania"  },
	{ "RI", "ambiguous", "rhode" , "island" },
	{ "SC", "ambiguous", "south" , "carlolina" },
	{ "SD", "ambiguous", "south" , "dakota" },
	{ "TN", "ambiguous", "tennesee"  },
	{ "Tn", "ambiguous", "tennesee"  },
	{ "Tenn", "ambiguous", "tennesee"  },
	{ "TX", "ambiguous", "texas"  },
	{ "Tx", "ambiguous", "texas"  },
	{ "Tex", "ambiguous", "texas"  },
	{ "UT", "ambiguous", "utah"  },
	{ "VA", "ambiguous", "virginia"  },
	{ "WA", "ambiguous", "washington"  },
	{ "Wa", "ambiguous", "washington"  },
	{ "Wash", "ambiguous", "washington"  },
	{ "WI", "ambiguous", "wisconsin"  },
	{ "Wi", "ambiguous", "wisconsin"  },
	{ "WV", "ambiguous", "west" , "virginia" },
	{ "WY", "ambiguous", "wyoming"  },
	{ "Wy", "ambiguous", "wyoming"  },
	{ "Wyo", "", "wyoming"  },
	{ "PR", "ambiguous", "puerto" , "rico" }
    };

    // Again hashtable for constant time searching
    private static Hashtable usStatesHash = new Hashtable();
    
    // initialize the Hashtable for usStates
    static {
	for (int i = 0; i < usStates.length; i++) {
	    usStatesHash.put(usStates[i][0], usStates[i]);
	}
    };

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
    public TokenToWords(CART usNumbersCART,
			PronounceableFSM prefixFSM,
			PronounceableFSM suffixFSM) {
	this.cart = usNumbersCART;
	this.prefixFSM = prefixFSM;
	this.suffixFSM = suffixFSM;
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
    }


    /**
     * Returns true if the given token matches part of a phone number
     *
     * @param tokenItem the token
     * @param tokenVal the string value of the token
     *
     * @return true or false
     */
    private boolean matchesPartPhoneNumber(Item tokenItem, String tokenVal) {

	String n_name = (String) tokenItem.findFeature("n.name");
	String n_n_name = (String) tokenItem.findFeature("n.n.name");
	String p_name = (String) tokenItem.findFeature("p.name");
	String p_p_name = (String) tokenItem.findFeature("p.p.name");

	boolean matches3DigitsP_name = matches(threeDigitsPattern, p_name);

	return ((matches(threeDigitsPattern, tokenVal) &&
		 ((!matches(digitsPattern, p_name)
		   && matches(threeDigitsPattern, n_name)
		   && matches(fourDigitsPattern, n_n_name)) ||
		  (matches(sevenPhoneNumberPattern, n_name)) ||
		  (!matches(digitsPattern, p_p_name)
		   && matches3DigitsP_name
		   && matches(fourDigitsPattern, n_name)))) ||
		(matches(fourDigitsPattern, tokenVal) &&
		 (!matches(digitsPattern, n_name)
		  && matches3DigitsP_name
		  && matches(threeDigitsPattern, p_p_name))));
    }
    

    /**
     * Returns true if the given string is in the given string array.
     *
     * @param value the string to check
     * @param stringArray the array to check
     *
     * @return true if the string is in the array, false otherwise
     */
    private static boolean inStringArray(String value, String[] stringArray) {
	for (int i = 0; i < stringArray.length; i++) {
	    if (stringArray[i].equals(value)) {
		return true;
	    }
	}
	return false;
    }


    /**
     * Adds a break as a feature to the last item in the list.
     *
     * @param wordList the list to add a break
     */
    private static void addBreak(List wordList) {
	int listLength = wordList.size();
	if (listLength > 0) {
	    Item wordItem = (Item) wordList.get(listLength - 1);
	    FeatureSet featureSet = new FeatureSetImpl();
	    featureSet.setString("break", "1");
	}
    }

    
    /**
     * Converts the given Token into a list of words.
     *
     * @param  tokenItem  the Item that stores the token
     * @param  tokenVal  the String value of the token, which may or may not be
     *                   same as the one in tokenItem, called "name" in flite
     * @param  wordList  words are added to this list
     *
     * @return  a list of words
     */
    protected List tokenToWords(Item tokenItem, String tokenVal,
				List wordList) {
	FeatureSet tokenFeatures = tokenItem.getFeatures();
	String itemName = tokenFeatures.getString("name");
	int tokenLength = tokenVal.length();
		
	if (tokenFeatures.isPresent("phones")) {
	    wordList.add("tokenVal");
	    return wordList;
	}

	if ((tokenVal.equals("a") || tokenVal.equals("A")) &&
	    !tokenVal.equals(itemName)) {
	    /* if A is a sub part of a token, then its ey not ah */
	    wordList.add("_a");

	} else if (matches(alphabetPattern, tokenVal)) {

	    if (matches(romanNumbersPattern, tokenVal)) {
		
		/* XVIII */
		romanToWords(tokenItem, tokenVal, wordList);
		
	    } else if (matches(drStPattern, tokenVal)) {
		
		/* St Andrew's St, Dr King Dr */
		drStToWords(tokenItem, tokenVal, wordList);
		
	    } else if (tokenVal.equals("Mr")) {
		
		tokenItem.getFeatures().setString("punc", "");
		wordList.add("mister");
		
	    } else if (tokenVal.equals("Mrs")) {
		
		tokenItem.getFeatures().setString("punc", "");
		wordList.add("missus");
		
	    } else if (tokenLength == 1
		       && isUppercaseLetter(tokenVal.charAt(0))
		       && ((String)tokenItem.findFeature("n.whitespace")).equals(" ")
		       && isUppercaseLetter
		       (((String) tokenItem.findFeature("n.name")).charAt(0))) {
		
		tokenFeatures.setString("punc", "");
		String aaa = tokenVal.toLowerCase();
		if (aaa.equals("a")) {
		    wordList.add("_a");
		} else {
		    wordList.add(aaa);
		}
	    } else if (isStateName(tokenItem, tokenVal, wordList)) {
		/*
		  The name of a US state
		  isStateName() has already added the full name of the
		  state, so we're all set.
		*/
	    } else if (tokenLength > 1 && !isPronounceable(tokenVal)) {
		/* Need common exception list */
		/* unpronouncable list of alphas */
		NumberExpander.expandLetters(tokenVal, wordList);
		
	    } else {
		/* just a word */
		wordList.add(tokenVal.toLowerCase());
	    }
	    
	} else if (matches(dottedAbbrevPattern, tokenVal)) {
	    
	    /* U.S.A. */
	    // remove all dots
	    String aaa = Utilities.deleteChar(tokenVal, '.');
	    NumberExpander.expandLetters(aaa, wordList);
	    
	} else if (matches(commaIntPattern, tokenVal)) {
	    
	    /* 99,999,999 */
	    String aaa = Utilities.deleteChar(tokenVal, ',');
	    NumberExpander.expandReal(aaa, wordList);
	    
	} else if (matches(sevenPhoneNumberPattern, tokenVal)) {
	    
	    /* 234-3434  telephone numbers */
	    int dashIndex = tokenVal.indexOf('-');
	    String aaa = tokenVal.substring(0, dashIndex);
	    String bbb = tokenVal.substring(dashIndex+1);
	    
	    NumberExpander.expandDigits(aaa, wordList);
	    addBreak(wordList);
	    NumberExpander.expandDigits(bbb, wordList);
	    
	} else if (matchesPartPhoneNumber(tokenItem, tokenVal)) {
	    
	    /* part of a telephone number */
	    String punctuation = (String) tokenItem.findFeature("punc");
	    if (punctuation.equals("")) {
		tokenItem.getFeatures().setString("punc", ",");
	    }
	    NumberExpander.expandDigits(tokenVal, wordList);
	    addBreak(wordList);
		
	} else if (matches(numberTimePattern, tokenVal)) {
	    
	    /* 12:35 */
	    int colonIndex = tokenVal.indexOf(':');
	    String aaa = tokenVal.substring(0, colonIndex);
	    String bbb = tokenVal.substring(colonIndex+1);
	    
	    NumberExpander.expandNumber(aaa, wordList);
	    if (!(bbb.equals("00"))) {
		NumberExpander.expandID(bbb, wordList);
	    }
	    
	} else if (matches(digits2DashPattern, tokenVal)) {
	    
	    /* 999-999-999 */
	    digitsDashToWords(tokenItem, tokenVal, wordList);
	    
	} else if (matches(digitsPattern, tokenVal)) {
	    
	    digitsToWords(tokenItem, tokenVal, wordList);
	    
	} else if (tokenLength == 1
		   && isUppercaseLetter(tokenVal.charAt(0))
		   && ((String)tokenItem.findFeature("n.whitespace")).equals(" ")
		   && isUppercaseLetter
		   (((String) tokenItem.findFeature("n.name")).charAt(0))) {
	    
	    tokenFeatures.setString("punc", "");
	    String aaa = tokenVal.toLowerCase();
	    if (aaa.equals("a")) {
		wordList.add("_a");
	    } else {
		wordList.add(aaa);
	    }
	} else if (matches(doublePattern, tokenVal)) {

	    NumberExpander.expandReal(tokenVal, wordList);

	} else if (matches(ordinalPattern, tokenVal)) {
	    
	    /* explicit ordinals */
	    String aaa = tokenVal.substring(0, tokenLength - 2);
	    NumberExpander.expandOrdinal(aaa, wordList);

	} else if (matches(illionPattern, tokenVal) &&
		   matches(usMoneyPattern, 
			   (String) tokenItem.findFeature("p.name"))) {

	    /* $ X -illion */
	    wordList.add(tokenVal);
	    wordList.add("dollars");	    

	} else if (matches(usMoneyPattern, tokenVal)) {

	    /* US money */
	    usMoneyToWords(tokenItem, tokenVal, wordList);

	} else if (tokenLength > 0
		   && tokenVal.charAt(tokenLength - 1) == '%') {
	    
	    /* Y% */
	    tokenToWords(tokenItem, tokenVal.substring(0, tokenLength - 1),
			 wordList);
	    wordList.add("per");
	    wordList.add("cent");

	} else if (matches(numessPattern, tokenVal)) {

	    /* 60s and 7s and 9s */
	    tokenToWords(tokenItem, tokenVal.substring(0, tokenLength - 1),
			 wordList);
	    wordList.add("'s");
	    
	} else if (tokenVal.indexOf('\'') != -1) {
	    
	    postropheToWords(tokenItem, tokenVal, wordList);
	    
	} else if (matches(digitsSlashDigitsPattern, tokenVal) &&
		   tokenVal.equals(itemName)) {

	    digitsSlashDigitsToWords(tokenItem, tokenVal, wordList);

	} else if (tokenVal.indexOf('-') != -1) {
	    
	    dashToWords(tokenItem, tokenVal, wordList);
	    
	} else if (tokenLength > 1 &&
		   !matches(alphabetPattern, tokenVal)) {
	    
	    notJustAlphasToWords(tokenItem, tokenVal, wordList);

	} else {
	    /* just a word */
	    wordList.add(tokenVal.toLowerCase());
	}   
	return wordList;
    }

	
    /**
     * Convert the given digit token with dashes (e.g. 999-999-999)
     * into a list of words.
     *
     * @param tokenItem the digit token item
     * @param tokenVal  the digit string
     * @param wordList  the list to add the words to
     *
     * @return a list of words
     */
    private List digitsDashToWords(Item tokenItem, String tokenVal,
				   List wordList) {
	int tokenLength = tokenVal.length();
	int a = 0;
	for (int p = 0; p < tokenLength; p++) {
	    if (tokenVal.charAt(p) == '-' || p == (tokenLength - 1)) {
		String aaa = tokenVal.substring(a, p);
		NumberExpander.expandDigits(aaa, wordList);
		addBreak(wordList);
		a = p+1;
	    }
	}
	return wordList;
    }

	
    /**
     * Convert the given digit token into a list of words.
     *
     * @param tokenItem the digit token item
     * @param tokenVal  the digit string
     * @param wordList  the list to add the words to
     *
     * @return a list of words
     */
    private List digitsToWords(Item tokenItem, String tokenVal,
				 List wordList) {
	FeatureSet featureSet = tokenItem.getFeatures();
	String nsw = "";
	if (featureSet.isPresent("nsw")) {
	    nsw = featureSet.getString("nsw");
	}

	if (nsw.equals("nide")) {
	    NumberExpander.expandID(tokenVal, wordList);
	} else {
	    String rName = featureSet.getString("name");
	    String digitsType = null;
	    
	    if (tokenVal.equals(rName)) {
		digitsType = (String) cart.interpret(tokenItem);
	    } else {
		featureSet.setString("name", tokenVal);
		digitsType = (String) cart.interpret(tokenItem);
		featureSet.setString("name", rName);
	    }
	    
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
	return wordList;
    }
    
    
    /**
     * Converts the given Roman numeral string into a list of words.
     *
     * @param tokenItem the roman token item
     * @param romanString the roman numeral string
     * @param wordList the list to add words to
     *
     * @return a list of words
     */
    private List romanToWords(Item tokenItem, String romanString,
				List wordList) {
	String punctuation = (String) tokenItem.findFeature("p.punc");
	
	if (punctuation.equals("")) {
	    /* no preceeding punctuation */
	    String n = String.valueOf(NumberExpander.expandRoman(romanString));
	
	    if (kingLike(tokenItem)) {
		wordList.add("the");
		NumberExpander.expandOrdinal(n, wordList);
	    } else if (sectionLike(tokenItem)) {
		NumberExpander.expandNumber(n, wordList);
	    } else {
		NumberExpander.expandLetters(romanString, wordList);
	    }
	} else {
	    NumberExpander.expandLetters(romanString, wordList);
	}

	return wordList;
    }
    

    /**
     * Returns true if the given key is in the kingSectionLikeHash
     * Hashtable, and the value is the same as the given value.
     *
     * @param key key to look for in the hashtable
     * @param value the value to match
     *
     * @return true if it matches, or false if it does not or if
     * the key is not mapped to any value in the hashtable.
     */
    private static boolean inKingSectionLikeHash(String key, String value) {
	String hashValue = (String) kingSectionLikeHash.get(key);
	if (hashValue != null) {
	    return (hashValue.equals(value));
	} else {
	    return false;
	}
    }



    /**
     * Returns true if the given token item contains a token that is
     * in a king-like context, e.g., "King" or "Louis".
     *
     * @param tokenItem the token item to check
     *
     * @return true or false
     */
    public static boolean kingLike(Item tokenItem) {
	String kingName = 
	    ((String) tokenItem.findFeature("p.name")).toLowerCase();
	if (inKingSectionLikeHash(kingName, KING_NAMES)) {
	    return true;
	} else {
	    String kingTitle =
		((String) tokenItem.findFeature("p.p.name")).toLowerCase();
	    return inKingSectionLikeHash(kingTitle, KING_TITLES);
	}
    }

    
    /**
     * Returns true if the given token item contains a token that is
     * in a section-like context, e.g., "chapter" or "act".
     *
     * @param tokenItem the token item to check
     *
     * @return true or false
     */
    public static boolean sectionLike(Item tokenItem) {
	String sectionType =
	    ((String) tokenItem.findFeature("p.name")).toLowerCase();
	return inKingSectionLikeHash(sectionType, SECTION_TYPES);
    }


    /**
     * Converts the given string containing "St" and "Dr" to a list
     * of words.
     *
     * @param tokenItem the token item
     * @param drStString the string with "St" and "Dr"
     * @param wordList the list to add words to
     *
     * @return a list of words
     */
    private List drStToWords(Item tokenItem, String drStString,
			     List wordList) {
	String street = null;
	String saint = null;
	char c0 = drStString.charAt(0);

	if (c0 == 's' || c0 == 'S') {
	    street = "street";
	    saint = "saint";
	} else {
	    street = "drive";
	    saint = "doctor";
	}
	
	FeatureSet featureSet = tokenItem.getFeatures();
	String punctuation = featureSet.getString("punc");

	String featPunctuation = (String) tokenItem.findFeature("punc");

	if (tokenItem.getNext() == null ||
	    punctuation.indexOf(',') != -1) {
	    wordList.add(street);
	} else if (featPunctuation.equals(",")) {
	    wordList.add(saint);
	} else {
	    String pName = (String) tokenItem.findFeature("p.name");
	    String nName = (String) tokenItem.findFeature("n.name");

	    char p0 = pName.charAt(0);
	    char n0 = nName.charAt(0);

	    if (isUppercaseLetter(p0) && isLowercaseLetter(n0)) {
		wordList.add(street);
	    } else if (NumberExpander.isDigit(p0) && isLowercaseLetter(n0)) {
		wordList.add(street);
	    } else if (isLowercaseLetter(p0) && isUppercaseLetter(n0)) {
		wordList.add(saint);
	    } else {
		String whitespace = (String) tokenItem.findFeature("n.whitespace");
		if (whitespace.equals(" ")) {
		    wordList.add(saint);
		} else {
		    wordList.add(street);
		}
	    }
	}

	if (punctuation != null && punctuation.equals(".")) {
	    featureSet.setString("punc", "");
	}

	return wordList;
    }
		

    /**
     * Converts US money string into a list of words.
     *
     * @param tokenItem the token item
     * @param tokenVal the US money string
     * @param wordList the list to add words to
     *
     * @return a list of words
     */
    private List usMoneyToWords(Item tokenItem, String tokenVal, 
				List wordList) {
	
	int dotIndex = tokenVal.indexOf('.');

	if (matches(illionPattern, 
		    (String) tokenItem.findFeature("n.name"))) {
	    NumberExpander.expandReal(tokenVal.substring(1), wordList);
	} else if (dotIndex == -1) {

	    String aaa = tokenVal.substring(1);
	    tokenToWords(tokenItem, aaa, wordList);

	    if (aaa.equals("1")) {
		wordList.add("dollar");
	    } else {
		wordList.add("dollars");
	    }
	} else if (dotIndex == (tokenVal.length() - 1) ||
		   (tokenVal.length() - dotIndex) > 3) {
	    /* simply read as mumble point mumble */
	    NumberExpander.expandReal(tokenVal.substring(1), wordList);
	    wordList.add("dollars");
	} else {
	    String aaa = tokenVal.substring(1, dotIndex);
	    aaa = Utilities.deleteChar(aaa, ',');
	    String bbb = tokenVal.substring(dotIndex+1);
	    
	    NumberExpander.expandNumber(aaa, wordList);

	    if (aaa.equals("1")) {
		wordList.add("dollar");
	    } else {
		wordList.add("dollars");
	    }

	    if (bbb.equals("00")) {
		// add nothing to the word list
	    } else {
		NumberExpander.expandNumber(bbb, wordList);
		if (bbb.equals("01")) {
		    wordList.add("cent");
		} else {
		    wordList.add("cents");
		}
	    }
	}

	return wordList;
    }	    


    /**
     * Convert the given apostrophed word into a list of words.
     *
     * @param tokenItem the token item
     * @param tokenVal the apostrophed word string
     * @param wordList the list to add words to
     *
     * @return a list of words
     */
    private List postropheToWords(Item tokenItem, String tokenVal,
				  List wordList) {
	int index = tokenVal.indexOf('\'');
	String bbb = tokenVal.substring(index).toLowerCase();

	if (inStringArray(bbb, postrophes)) {
	    String aaa = tokenVal.substring(0, index);
	    tokenToWords(tokenItem, aaa, wordList);
	    wordList.add(bbb);

	} else if (bbb.equals("'tve")) {
	    String aaa = tokenVal.substring(0, index-2);
	    tokenToWords(tokenItem, aaa, wordList);
	    wordList.add("'ve");

	} else {
	    /* internal single quote deleted */
	    StringBuffer buffer = new StringBuffer(tokenVal);
	    buffer.deleteCharAt(index);
	    tokenToWords(tokenItem, buffer.toString(), wordList);
	}
	return wordList;
    }


    /**
     * Convert the given digits/digits string into a list of words.
     *
     * @param tokenItem the token item
     * @param tokenVal the digits/digits string
     * @param wordList the list to add words to
     *
     * @return a list of words
     */
    private List digitsSlashDigitsToWords(Item tokenItem, String tokenVal,
					  List wordList) {
	/* might be fraction, or not */
	int index = tokenVal.indexOf('/');
	String aaa = tokenVal.substring(0, index);
	String bbb = tokenVal.substring(index+1);
	int a, b;
	
	// if the previous token is a number, add an "and"
	if (matches(digitsPattern, (String) tokenItem.findFeature("p.name"))
	    && tokenItem.getPrevious() != null) {
	    wordList.add("and");
	}

	if (aaa.equals("1") && bbb.equals("2")) {
	    wordList.add("a");
	    wordList.add("half");
	} else if ((a = Integer.parseInt(aaa)) < (b = Integer.parseInt(bbb))) {
	    NumberExpander.expandNumber(aaa, wordList);
	    NumberExpander.expandOrdinal(bbb, wordList);
	    if (a > 1) {
		wordList.add("'s");
	    }
	} else {
	    NumberExpander.expandNumber(aaa, wordList);
	    wordList.add("slash");
	    NumberExpander.expandNumber(bbb, wordList);
	}

	return wordList;
    }


    /**
     * Convert the given dashed string (e.g. "aaa-bbb") into a list of words.
     *
     * @param tokenItem the token item
     * @param tokenVal the dashed string
     * @param wordList the list to add words to
     *
     * @return a list of words
     */
    private List dashToWords(Item tokenItem, String tokenVal,
			     List wordList) {
	int index = tokenVal.indexOf('-');
	String aaa = tokenVal.substring(0, index);
	String bbb = tokenVal.substring(index+1, tokenVal.length());

	if (matches(digitsPattern, aaa) && matches(digitsPattern, bbb)) {
	    FeatureSet featureSet = tokenItem.getFeatures();
	    featureSet.setString("name", aaa);
	    tokenToWords(tokenItem, aaa, wordList);
	    wordList.add("to");
	    featureSet.setString("name", bbb);
	    tokenToWords(tokenItem, bbb, wordList);
	    featureSet.setString("name", tokenVal);
	} else {	    
	    tokenToWords(tokenItem, aaa, wordList);
	    tokenToWords(tokenItem, bbb, wordList);
	}

	return wordList;
    }


    /**
     * Convert the given string (which does not only consist of alphabet)
     * into a list of words.
     *
     * @param tokenItem the token item
     * @param tokenVal the string
     * @param wordList the list to add words to
     *
     * @return a list of words
     */
    private List notJustAlphasToWords(Item tokenItem, String tokenVal,
				      List wordList) {
	/* its not just alphas */
	int index = 0;
	int tokenLength = tokenVal.length();

	for (; index < tokenLength; index++) {
	    if (isTextSplitable(tokenVal, index)) {
		break;
	    }
	}
	
	String aaa = tokenVal.substring(0, index+1);
	String bbb = tokenVal.substring(index+1, tokenLength);
	
	FeatureSet featureSet = tokenItem.getFeatures();
	featureSet.setString("nsw", "nide");
	tokenToWords(tokenItem, aaa, wordList);
	tokenToWords(tokenItem, bbb, wordList);
	
	return wordList;
    }


    /**
     * Returns true if the given word is pronounceable.
     * This method is originally called us_aswd() in Flite 1.1.
     *
     * @param word the word to test
     *
     * @return true if the word is pronounceable, false otherwise
     */
    public boolean isPronounceable(String word) {
	String lowerCaseWord = word.toLowerCase();
	return (prefixFSM.accept(lowerCaseWord) &&
		suffixFSM.accept(lowerCaseWord));
    }


    /**
     * Returns true if the given token is the name of a US state.
     * If it is, it will add the name of the state to the given word list.
     *
     * @param tokenItem the token item
     * @param tokenVal the token string
     * @param wordList the list to add words to
     */
    private boolean isStateName(Item tokenItem, String tokenVal,
				List wordList) {
	String[] state = (String[]) usStatesHash.get(tokenVal);
	if (state != null) {
	    boolean doIt = false;
	    if (state.equals("ambiguous")) {
		String pName = (String) tokenItem.findFeature("p.name");
		String nName = (String) tokenItem.findFeature("n.name");
		int nNameLength = nName.length();
		FeatureSet featureSet = tokenItem.getFeatures();
		if ((isUppercaseLetter(pName.charAt(0))
		     && pName.length() > 2
		     && matches(alphabetPattern, pName)) &&
		    (isLowercaseLetter(nName.charAt(0))
		     || tokenItem.getNext() == null
		     || featureSet.getString("punc").equals(".")
		     || ((nNameLength == 5 || nNameLength == 10) &&
			 matches(digitsPattern, nName)))) {
		    doIt = true;
		} else {
		    doIt = false;
		}
	    } else {
		doIt = true;
	    }
	    if (doIt) {
		for (int j = 2; j < state.length; j++) {
		    if (state[j] != null) {
			wordList.add(state[j]);
		    }
		}
		return true;
	    }
	}
	return false;
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
	
	if (isLetter(c0) && isLetter(c1)) {
	    return false;
	} else if (NumberExpander.isDigit(c0) && NumberExpander.isDigit(c1)) {
	    return false;
	} else {
	    return true;
	}
    }


    /**
     * Returns true if the given character is a letter (a-z or A-Z).
     *
     * @param ch the character to test
     *
     * @return true or false
     */
    private static boolean isLetter(char ch) {
	return (('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z'));
    }


    /**
     * Returns true if the given character is an uppercase letter (A-Z).
     *
     * @param ch the character to test
     *
     * @return true or false
     */
    private static boolean isUppercaseLetter(char ch) {
	return ('A' <= ch && ch <= 'Z');
    }

    
    /**
     * Returns true if the given character is a lowercase letter (a-z).
     *
     * @param ch the character to test
     *
     * @return true or false
     */
    private static boolean isLowercaseLetter(char ch) {
	return ('a' <= ch && ch <= 'z');
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






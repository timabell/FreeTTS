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

import java.util.List;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileInputStream;

import java.net.URL;

import com.sun.speech.freetts.lexicon.LetterToSound;
import com.sun.speech.freetts.lexicon.LexiconImpl;
import com.sun.speech.freetts.util.BulkTimer;

/**
 * Provides a CMU lexicon-specific implementation of a Lexicon that is
 * stored in a text file.
 */
public class CMULexicon extends LexiconImpl {
    
    /**
     * Vowels
     */
    static final private String VOWELS = "aeiou";

    /**
     * Glides/Liquids
     */
    static final private String GLIDES_LIQUIDS = "wylr";

    /**
     * Nasals
     */
    static final private String NASALS = "nm";

    /**
     * Voiced Obstruents
     */
    static final private String VOICED_OBSTRUENTS = "bdgjlmnnnrvwyz";

    /**
     * Creates a CMULexicon based upon the given compiled and addenda
     * DBs and the given letter to sound rules
     *
     * @param compiledURL the compiled database is loaded from here
     * @param addendaURL the database addenda is loaded from here
     * @param letterToSoundURL the letter to sound rules are loaded
     * 		from here
     * @param binary if <code>true</code> the input data are loaded as
     * 		binary ; otherwise if <code>false</code> the input
     * 		data are loaded as text.
     *
     */
    public CMULexicon(URL compiledURL,
                       URL addendaURL,
                       URL  letterToSoundURL,
		       boolean binary) {
        setLexiconParameters(compiledURL, addendaURL, letterToSoundURL, binary);
    }

    /**
     * Creates the default CMU Lexicon which is a binary lexicon
     */
    public CMULexicon() {
	this("cmulex");
    }

    /**
     * Creates the CMU Lexicon which is a binary lexicon
     *
     * @param basename the basename for the lexicon.
     */
    public CMULexicon(String basename) {
	Class cls = CMULexicon.class;
	URL letterToSoundURL = cls.getResource(basename + "_lts.bin");
	URL compiledURL = cls.getResource(basename + "_compiled.bin");
	URL addendaURL = cls.getResource(basename + "_addenda.bin");
	setLexiconParameters(compiledURL, addendaURL, letterToSoundURL, true);
    }
    
    /**
     * Get the CMULexicon.
     *
     * @param useBinaryIO if true use binary IO to load DB
     *
     * @throws IOException if problems occurred while reading the data
     */ 
    static public CMULexicon getInstance( boolean useBinaryIO) 
						throws IOException {
	return getInstance("cmulex", useBinaryIO);
    }

    /**
     * Get the CMULexicon.
     *
     * @param useBinaryIO if true use binary IO to load DB
     *
     * @throws IOException if problems occurred while reading the data
     */ 
    static public CMULexicon getInstance( String basename, boolean useBinaryIO) 
						throws IOException {
	URL compiledURL;
	URL addendaURL;
	URL letterToSoundURL;

	System.out.println("Getting " + basename);
	LetterToSound letterToSound;
	Class cls = CMULexicon.class;
	CMULexicon lexicon;

	
	if (useBinaryIO) {
	    letterToSoundURL = cls.getResource(basename + "_lts.bin");
	    compiledURL = cls.getResource(basename + "_compiled.bin");
	    addendaURL = cls.getResource(basename + "_addenda.bin");
	}
	else {
	    letterToSoundURL = cls.getResource(basename + "_lts.txt");
	    compiledURL = cls.getResource(basename + "_compiled.txt");
	    addendaURL = cls.getResource(basename + "_addenda.txt");

	    System.out.println("lts is " + letterToSoundURL);
	    System.out.println("com is " + compiledURL);
	    System.out.println("ad is " + addendaURL);
	}
	lexicon = new CMULexicon(compiledURL, addendaURL,
		letterToSoundURL, useBinaryIO);
	lexicon.load();
        return lexicon;
    }

        
    /**
     * Determines if the currentPhone represents a new syllable
     * boundary.
     *
     * @param syllablePhones the phones in the current syllable so far
     * @param wordPhones the phones for the whole word
     * @param currentWordPhone the word phone in question
     *
     * @return <code>true</code> if the word phone in question is on a
     *     syllable boundary; otherwise <code>false</code>.
     */
    public boolean isSyllableBoundary(List syllablePhones,
                                      String[] wordPhones,
                                      int currentWordPhone) {
        if (currentWordPhone >= wordPhones.length) {
            return true;
        } else if (isSilence(wordPhones[currentWordPhone])) {
            return true;
        } else if (!hasVowel(wordPhones, currentWordPhone)) { // rest of word 
            return false;
        } else if (!hasVowel(syllablePhones)) { // current syllable
            return false;
        } else if (isVowel(wordPhones[currentWordPhone])) {
            return true;
        } else if (currentWordPhone == (wordPhones.length - 1)) {
            return false;
        } else {
            int p, n, nn;
            p = getSonority(
                (String) syllablePhones.get(syllablePhones.size() - 1));
            n = getSonority(wordPhones[currentWordPhone]);
            nn = getSonority(wordPhones[currentWordPhone + 1]);
            if ((p <= n) && (n <= nn)) {
                return true;
            } else {
                return false;
            }
        }
    }
    
    /**
     * Determines if the given phone represents a silent phone.
     *
     * @param phone the phone to test
     *
     * @return <code>true</code> if the phone represents a silent
     * 		phone; otherwise <code>false</code>. 
     */
    static protected boolean isSilence(String phone) {
        return phone.equals("pau");
    }

    /**
     * Determines if there is a vowel in the remainder of the array, 
     * starting at the given index.
     *
     * @param phones the set of phones to check
     * @param index start checking at this index
     *
     * @return <code>true</code> if a vowel is found; 
     *		otherwise <code>false</code>. 
     */
    static protected boolean hasVowel(String[] phones, int index) {
        for (int i = index; i < phones.length; i++) {
            if (isVowel(phones[i])) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Determines if there is a vowel in given list of phones.
     *
     * @param phones the list of phones
     *
     * @return <code>true</code> if a vowel is found; 
     *		otherwise <code>false</code>. 
     */
    static protected boolean hasVowel(List phones) {
        for (int i = 0; i < phones.size(); i++) {
            if (isVowel((String) phones.get(i))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Determines if the given phone is a vowel
     *
     * @param phone the phone to test
     *
     * @return <code>true</code> if phone is a vowel
     *		otherwise <code>false</code>. 
     */
    static protected boolean isVowel(String phone) {
        return VOWELS.indexOf(phone.substring(0,1)) != -1;
    }

    /**
     * Determines the sonority for the given phone.
     * 
     * @param phone the phone of interest
     * 
     * @return an integer that classifies phone transitions
     */
    static protected int getSonority(String phone) {
        if (isVowel(phone) || isSilence(phone)) {
            return 5;
        } else if (GLIDES_LIQUIDS.indexOf(phone.substring(0,1)) != -1) {
            return 4; 
        } else if (NASALS.indexOf(phone.substring(0,1)) != -1) {
            return 3;
        } else if (VOICED_OBSTRUENTS.indexOf(phone.substring(0,1)) != -1) {
            return 2;
        } else {
            return 1;
        }
    }    

    /**
     * Provides test code for the CMULexicon.
     * <br><b>Usage:</b><br>
     * <pre>
     *  com.sun.speech.freetts.en.us.CMULexicon [options]
     *
     * Where options is any combination of:
     *
     * -generate_binary
     * -compare
     * -showtimes
     *
     * </pre>
     */
    public static void main(String[] args) {
	LexiconImpl lex, lex2;
	boolean showTimes = false;
	String baseName = "cmulex";

	try {
	    if (args.length > 0) {
		BulkTimer.LOAD.start();
		for (int i = 0 ; i < args.length; i++) {
		    if (args[i].equals("-name") && i < args.length - 1) {
			baseName = args[++i];
		    } else if (args[i].equals("-generate_binary")) {

			 BulkTimer.LOAD.start("load_text");
			 lex = CMULexicon.getInstance(baseName, false);
			 BulkTimer.LOAD.stop("load_text");

			 BulkTimer.LOAD.start("dump_text");
			 lex.dumpBinary(baseName);
			 BulkTimer.LOAD.stop("dump_text");

		    } else if (args[i].equals("-compare")) {

			BulkTimer.LOAD.start("load_text");
			lex = CMULexicon.getInstance(baseName, false);
			BulkTimer.LOAD.stop("load_text");

			BulkTimer.LOAD.start("load_binary");
			lex2 = CMULexicon.getInstance(baseName, true);
			BulkTimer.LOAD.stop("load_binary");

			BulkTimer.LOAD.start("compare");
			lex.compare(lex2);
			BulkTimer.LOAD.stop("compare");
		    } else if (args[i].equals("-showtimes")) {
			showTimes = true;
		    } else {
			System.out.println("Unknown option " + args[i]);
		    }
		}
		BulkTimer.LOAD.stop();
		if (showTimes) {
		    BulkTimer.LOAD.show("CMULexicon loading and dumping");
		}
	    } else {
		System.out.println("Options: ");
		System.out.println("    -compare");
		System.out.println("    -generate_binary");
		System.out.println("    -showtimes");
	    }
	} catch (IOException ioe) {
	    System.err.println(ioe);
	}
    }
}

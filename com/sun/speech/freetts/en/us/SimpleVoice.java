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

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.Utterance;
import com.sun.speech.freetts.Item;
import com.sun.speech.freetts.Relation;
import com.sun.speech.freetts.UtteranceProcessor;
import com.sun.speech.freetts.ProcessException;
import com.sun.speech.freetts.Tokenizer;
import com.sun.speech.freetts.FeatureSet;
import com.sun.speech.freetts.PhoneDurations;
import com.sun.speech.freetts.PhoneDurationsImpl;
import com.sun.speech.freetts.PartOfSpeech;
import com.sun.speech.freetts.PartOfSpeechImpl;
import com.sun.speech.freetts.PhoneSet;
import com.sun.speech.freetts.PhoneSetImpl;
import com.sun.speech.freetts.Segmenter;
import com.sun.speech.freetts.util.BulkTimer;

import com.sun.speech.freetts.cart.CARTImpl;
import com.sun.speech.freetts.cart.Phraser;
import com.sun.speech.freetts.cart.Intonator;
import com.sun.speech.freetts.cart.Durator;
import com.sun.speech.freetts.relp.AudioOutput;

import com.sun.speech.freetts.en.us.TokenToWords;
import com.sun.speech.freetts.en.PartOfSpeechTagger;
import com.sun.speech.freetts.en.PauseGenerator;
import com.sun.speech.freetts.en.ContourGenerator;

import com.sun.speech.freetts.diphone.DiphoneUnitSelector;

import java.util.List;
import java.io.IOException;

/**
 * Provides a non-functional voice used for testing
 */
public class SimpleVoice extends Voice {
        
    private PhoneSet phoneSet;
    private String freettsPrefix = "com.sun.speech.freetts";

    private boolean useBinaryIO =
	System.getProperty(freettsPrefix+".useBinaryIO", "true").equals("true");

    private String silence =
	System.getProperty(freettsPrefix+".silence", "pau");

    private String joinType =
	System.getProperty(freettsPrefix+".joinType", "modified_lpc");
    

    /**
     * Creates a simple voice
     */
    public SimpleVoice() {
	this(false);
    }

    /**
     * Creates a simple voice
     *
     * @param createLexicon if <code>true</code> create the lexicon;
     *  
     */
    public SimpleVoice(boolean createLexicon) {
	if (createLexicon) {
	    setLexicon(new CMULexicon());
	}
	setRate(150f);
	setPitch(100F);
	setPitchRange(11F);
    }


    // overrides Voice.loader

    protected void loader() throws IOException {
	setupFeatureSet();
	setupUtteranceProcessors();
	setupFeatureProcessors();
    }


    /**
     * Sets up the FeatureSet for this Voice.
     *
     * @throws IOException if an I/O error occurs
     */
    protected void setupFeatureSet() throws IOException {
	
	BulkTimer.LOAD.start("FeatureSet");
        FeatureSet features = getFeatures();
	features.setString(FEATURE_SILENCE, silence);
	features.setString(FEATURE_JOIN_TYPE, joinType);
	BulkTimer.LOAD.stop("FeatureSet");
    }
    
    /**
     * Sets up the UtteranceProcessors for this Voice.  These are
     * order dependent, so don't change the order.
     *
     * @throws IOException if an I/O error occurs
     */
    protected void setupUtteranceProcessors() throws IOException {
	List processors = getUtteranceProcessors();

	BulkTimer.LOAD.start("CartLoading");
	CARTImpl numbersCart = new CARTImpl
	    (getResource("nums_cart.txt"));
	CARTImpl phrasingCart = new CARTImpl( 
            getResource("phrasing_cart.txt"));
        CARTImpl accentCart = new CARTImpl( 
            getResource("int_accent_cart.txt"));
        CARTImpl toneCart = new CARTImpl( 
            getResource("int_tone_cart.txt"));
        CARTImpl durzCart = new CARTImpl(
            getResource("durz_cart.txt"));
	BulkTimer.LOAD.stop("CartLoading");

	BulkTimer.LOAD.start("UtteranceProcessors");
        PhoneDurations phoneDurations = new PhoneDurationsImpl(
            getResource("dur_stat.txt"));
	PronounceableFSM prefixFSM = new PrefixFSM
	    (getResource("prefix_fsm.txt"));
	PronounceableFSM suffixFSM = new SuffixFSM
	    (getResource("suffix_fsm.txt"));
        
	processors.add(new TokenToWords(numbersCart, prefixFSM, suffixFSM));
	processors.add(new PartOfSpeechTagger());
	processors.add(new Phraser(phrasingCart));
	processors.add(new Segmenter());
	processors.add(new PauseGenerator());
	processors.add(new Intonator(accentCart, toneCart));
	processors.add(new SimpleVoicePostLexicalAnalyzer());
	processors.add(new Durator(durzCart, 150.0f, phoneDurations));
	processors.add(new ContourGenerator
	   (getResource("f0_lr_terms.txt"), 170.0f, 34.0f));

	processors.add(new DiphoneUnitSelector(
		    getResource(
		useBinaryIO ? "cmu_kal/diphone_units.bin" :
			      "cmu_kal/diphone_units.txt"
			)));
	BulkTimer.LOAD.stop("UtteranceProcessors");
    }


    /**
     * Sets up the FeatureProcessors for this Voice.
     *
     * @throws IOException if an I/O error occurs
     */
    protected void setupFeatureProcessors() throws IOException {
	BulkTimer.LOAD.start("FeatureProcessing");
        PartOfSpeech pos = new PartOfSpeechImpl( 
            getResource("part_of_speech.txt"),
	    "content");

        phoneSet  = new PhoneSetImpl( 
            getResource("phoneset.txt"));

	addFeatureProcessor("gpos", new FeatureProcessors.Gpos(pos));
	addFeatureProcessor("word_numsyls",new FeatureProcessors.WordNumSyls());
	addFeatureProcessor("ssyl_in", new FeatureProcessors.StressedSylIn());
	addFeatureProcessor("syl_in", new FeatureProcessors.SylIn());
	addFeatureProcessor("syl_out", new FeatureProcessors.SylOut());
	addFeatureProcessor("ssyl_out", new
		FeatureProcessors.StressedSylOut());
	addFeatureProcessor("syl_break", new FeatureProcessors.SylBreak());
	addFeatureProcessor("old_syl_break", new FeatureProcessors.SylBreak());
	addFeatureProcessor("num_digits", new FeatureProcessors.NumDigits());
	addFeatureProcessor("month_range", new FeatureProcessors.MonthRange());
	addFeatureProcessor("token_pos_guess", 
		new FeatureProcessors.TokenPosGuess());
	addFeatureProcessor("sub_phrases", new FeatureProcessors.SubPhrases());
	addFeatureProcessor("asyl_in", new FeatureProcessors.AccentedSylIn());
	addFeatureProcessor("last_accent", new FeatureProcessors.LastAccent());
	addFeatureProcessor("pos_in_syl", new FeatureProcessors.PosInSyl());
	addFeatureProcessor("position_type", new
		FeatureProcessors.PositionType());

	addFeatureProcessor("ph_cplace", new FeatureProcessors.PH_CPlace());
	addFeatureProcessor("ph_ctype", new FeatureProcessors.PH_CType());
	addFeatureProcessor("ph_cvox", new FeatureProcessors.PH_CVox());
	addFeatureProcessor("ph_vc", new FeatureProcessors.PH_VC());
	addFeatureProcessor("ph_vfront", new FeatureProcessors.PH_VFront());
	addFeatureProcessor("ph_vheight", new FeatureProcessors.PH_VHeight());
	addFeatureProcessor("ph_vlng", new FeatureProcessors.PH_VLength());
	addFeatureProcessor("ph_vrnd", new FeatureProcessors.PH_VRnd());

	addFeatureProcessor("seg_coda_fric", new
		FeatureProcessors.SegCodaFric());
	addFeatureProcessor("seg_onset_stop", new
		FeatureProcessors.SegOnsetStop());
	addFeatureProcessor("seg_onsetcoda", new
		FeatureProcessors.SegOnsetCoda());
	addFeatureProcessor("syl_codasize", new
		FeatureProcessors.SylCodaSize());
	addFeatureProcessor("syl_onsetsize", new
		FeatureProcessors.SylOnsetSize());
	addFeatureProcessor("accented", new FeatureProcessors.Accented());
	BulkTimer.LOAD.stop("FeatureProcessing");
    }

    /**
     * Returns the AudioOutput processor to be used by this voice.
     * Derived voices typically override this to customize behaviors.
     * 
     * @return the audio output processor
     * 
     * @throws IOException if an IO error occurs while getting
     *     processor
     */
    protected UtteranceProcessor getAudioOutput() throws IOException {
	return new AudioOutput();
    }
    

    /**
     * Given a phoneme and a feature name, return the feature
     *
     * @param phone the phoneme of interest
     * @param featureName the name of the feature of interest
     *
     * @return the feature with the given name
     */
    public String getPhoneFeature(String phone, String featureName) {
	return phoneSet.getPhoneFeature(phone, featureName);
    }

    /**
     * Gets a tokenizer for this voice.
     *
     * @return the tokenizer.
     */
    public Tokenizer getTokenizer() {
	Tokenizer tokenizer = new com.sun.speech.freetts.en.TokenizerImpl();
	tokenizer.setWhitespaceSymbols(USEnglish.WHITESPACE_SYMBOLS);
	tokenizer.setSingleCharSymbols(USEnglish.SINGLE_CHAR_SYMBOLS);
	tokenizer.setPrepunctuationSymbols(USEnglish.PREPUNCTUATION_SYMBOLS);
	tokenizer.setPostpunctuationSymbols(USEnglish.PUNCTUATION_SYMBOLS);
	return tokenizer;
    }


    /**
     * Converts this object ot its String representation
     *
     * @return the string representation of this object
     */
    public String toString() {
	return "SimpleVoice";
    }
}
/**
 * Annotates the utterance with post lexical information
 * 
 */
class SimpleVoicePostLexicalAnalyzer implements UtteranceProcessor {
    UtteranceProcessor englishPostLex =
        new com.sun.speech.freetts.en.PostLexicalAnalyzer();

    /**
     * performs the processing
     *
     * @param  utterance  the utterance to process/tokenize
     *
     * @throws ProcessException if an IOException is thrown during the
     *         processing of the utterance
     */
    public void processUtterance(Utterance utterance) throws ProcessException {
	fixPhoneme_AH(utterance);
	englishPostLex.processUtterance(utterance);
    }


    /**
     * Turns all AH phonemes into AA phonemes.
     * This should really be done in the index itself
     *
     * @param utterance the utterance to fix
     */
    private void fixPhoneme_AH(Utterance utterance) {
	for (Item item = utterance.getRelation(Relation.SEGMENT).getHead();
		item != null;
		item = item.getNext()) {
	    if (item.getFeatures().getString("name").equals("ah")) {
		item.getFeatures().setString("name", "aa");
	    }
	}
    }

    /**
     * Converts this object ot its String representation
     *
     * @return the string representation of this object
     */
    public String toString() {
        return "PostLexicalAnalyzer";
    }
}

/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package tests;
import junit.framework.*;
import java.util.*;
import java.io.*;
import com.sun.speech.freetts.*;

/**
 * Tests for the Utterance class
 * 
 * @version 1.0
 */
public class SegmenterTests extends TestCase {
    Voice voice;
    Utterance utterance;
    UtteranceProcessor wordSylSeg = new Segmenter();

    /**
     * JUnit style tests that test proper feature and relation
     * behavior.
     * 
     * @param  name the name of the test.
     */
    public SegmenterTests(String name) {
	super(name);
    }

    /**
     * given some text, create a word relation
     * and the syllable relations that go with it.
     *
     * @param text the text to process
     *
     * @return the utterance
     */
    public Utterance  getSyllables(String text) {
        VoiceManager voiceManager = VoiceManager.getInstance();
        voice = voiceManager.getVoice("kevin");
	voice.allocate();
	utterance = new Utterance(voice);
	Relation words = utterance.createRelation("Word");
	StringTokenizer tok = new StringTokenizer(text);

       while (tok.hasMoreTokens()) {
	   Item word = words.appendItem();
	   word.getFeatures().setString("name", tok.nextToken().toLowerCase());
       }
        try {
	    wordSylSeg.processUtterance(utterance);
	} catch (ProcessException pe) {
	    System.out.println("Error processing " + text);
	}
	return utterance;
    } 


    /**
     * Tests simple syllable and segment behavior
     */
    public void testHowNowBrownCow() {
	Utterance u = getSyllables("how now brown cowboy");
	Relation segment = u.getRelation("Segment");
	assertTrue("segment", segment != null);
	assertTrue("Syllable", u.getRelation("Syllable") != null);
	assertTrue("SylStructure", u.getRelation("SylStructure") != null);
    }

    /**
     * Tests to see if the segment names are created properly, as well
     * as the syllable structure is created properly.
     */
    public void testJanuary() {
	Utterance u = getSyllables("january first two thousand and one");
	Relation segment = u.getRelation("Segment");
	assertTrue("segment", segment != null);
	assertTrue("Syllable", u.getRelation("Syllable") != null);
	assertTrue("SylStructure", u.getRelation("SylStructure") != null);

	// tests the segment

	//assertTrue("segment size", segment.getItems().size() == 26);

	// spot check some segments
	Item i = segment.getHead();
	assertTrue("seg jh", i.toString().equals("jh"));
	i = i.getNext();
	assertTrue("seg ae", i.toString().equals("ae"));
	i = i.getNext();
	assertTrue("seg n",  i.toString().equals("n"));
	i = i.getNext();
	assertTrue("seg y",  i.toString().equals("y"));
	i = i.getNext();
	assertTrue("seg uw", i.toString().equals("uw"));
	i = i.getNext();
	assertTrue("seg eh", i.toString().equals("eh"));
	i = i.getNext();
	assertTrue("seg r",  i.toString().equals("r"));
	i = i.getNext();
	assertTrue("seg iy", i.toString().equals("iy"));
	i = i.getNext();
	assertTrue("seg f",  i.toString().equals("f"));
	i = i.getNext();
	assertTrue("seg er", i.toString().equals("er"));
	i = i.getNext();
	assertTrue("seg s",  i.toString().equals("s"));
	i = i.getNext();
	assertTrue("seg t",  i.toString().equals("t"));
	i = i.getNext();


	// spot check the SylStructure
	Relation sylStructure = u.getRelation("SylStructure");
	//assertTrue("sylStructure size", sylStructure.getItems().size() == 6);

	Item si = sylStructure.getHead();
	assertTrue("january", si.toString().equals("january"));
	si = si.getNext();
	assertTrue("first", si.toString().equals("first"));
	si = si.getNext();
	assertTrue("two", si.toString().equals("two"));
	si = si.getNext();
	assertTrue("thousand", si.toString().equals("thousand"));
	si = si.getNext();
	assertTrue("and", si.toString().equals("and"));
	si = si.getNext();
	assertTrue("one", si.toString().equals("one"));
	si = si.getNext();

	Item january = sylStructure.getHead();

	assertTrue("findItem", january.findItem("R:Word").
		toString().equals("january"));

	assertTrue("findItem", january.findItem("R:Word.n").
		toString().equals("first"));
	assertTrue("findItem", january.findItem("R:Word.n.n").
		toString().equals("two"));
	assertTrue("findItem", january.findItem("R:Word.n.n.p").
		toString().equals("first"));

	PrintWriter pw = new PrintWriter(System.out);
	january.findItem("daughter.daughter").dump(pw, 4, "dd");

	assertTrue("findItem",
		january.findItem("daughter.daughter").
		toString().equals("jh"));

	assertTrue("findItem",
		january.findItem("daughter.daughter.n").
		toString().equals("ae"));

	assertTrue("findItem",
		january.findItem("daughter.daughter.n.p").
		toString().equals("jh"));
	assertTrue("findItem",
		january.findItem("daughter.daughtern").
		toString().equals("n"));
	assertTrue("findItem",
		january.findItem("daughter.daughtern.parent.parent").
		toString().equals("january"));
	assertTrue("findItem",
		january.findItem(
	"daughter.daughtern.parent.parent.R:Word.R:SylStructure" +
	".daughter.daughter.n").toString().equals("ae"));

	assertTrue("findItem",
		january.findItem("daughter.daughtern").
		toString().equals("n"));

	assertTrue("findFeature", january.findFeature("daughter.stress").
		toString().equals("1"));

	Item firstSyllable =  january.getDaughter();
	/*
	assertTrue("num seg in syl",
		firstSyllable.getDaughters().size() == 3);
	*/
	Item l = firstSyllable.getDaughter();
	assertTrue("syl jh", l.toString().equals("jh"));
	l = l.getNext();
	assertTrue("syl ae", l.toString().equals("ae"));
	l = l.getNext();
	assertTrue("syl n", l.toString().equals("n"));
	l = l.getNext();

    }


    /**
     * Factory method that creates the test suite.
     * 
     * @return the test suite.
     */
    public static Test suite() {
	return new TestSuite(SegmenterTests.class);
    } 



    /**
     * Main entry point for this test suite.
     * 
     * @param  args    the command line arguments.
     */
    public static void main(String[] args) {
	// String inputText = "for score and seven years ago";
	String inputText = "january first two thousand and one";

	if (args.length > 0) {
	    inputText = args[0];
	}

	SegmenterTests wsst = new SegmenterTests("tests");
	Utterance t1 = wsst.getSyllables( inputText);
	t1.dump("t1");
    } 
}



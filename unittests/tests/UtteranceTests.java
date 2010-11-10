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
 * JUNIT Tests for the Utterance class
 * 
 * @version 1.0
 */
public class UtteranceTests extends TestCase {
    Voice voice;
    Utterance utterance;

    /**
     * Creates the set of UtteranceTests
     * 
     * @param  name the name of the test.
     */
    public UtteranceTests(String name) {
	super(name);
    }

    /**
     * Common code run before each test
     */
    protected void setUp() {
	voice = VoiceManager.getInstance().getVoice("kevin");
	utterance = new Utterance(voice);
	List processors = voice.getUtteranceProcessors();

	processors.add(new TestUtteranceProcessor("tokenizer"));
	processors.add(new TestUtteranceProcessor("tokentowords"));
	processors.add(new TestUtteranceProcessor("pauses"));
	processors.add(new TestUtteranceProcessor("intonation"));

	voice.getFeatures().setString("testFeature1", "testFeatureValue1");
	voice.getFeatures().setString("sillyText", "how now brown cowboy!");
	voice.getFeatures().setString("knock knock", "Who is there?");

	utterance.setString("uttFeature1", "this is utt feature 1");
	utterance.setString("whitespace", "_+_+_+_+_+_+_");
	utterance.setString("blackspace", "####@#@#@@");
	utterance.setString("inputText", "How now brown cow");

	Relation tokens = utterance.createRelation("tokens");
	StringTokenizer tok = new StringTokenizer(
		  "January 1st 2001 How now brown cow "
		+ " it's a far far better thing I do now than I've ever"
		+ "done before");

	while (tok.hasMoreTokens()) {
	    String token = tok.nextToken();
	    Item newItem = tokens.appendItem();
	    newItem.getFeatures().setString("name", token);
	    newItem.getFeatures().setString("punc", "");
	    newItem.getFeatures().setString("stress", "1");
	}

	Relation words = utterance.createRelation("Words");


	for (Item item = tokens.getHead(); 
			item != null; item = item.getNext()) {
	    if (item.getFeatures().getString("name").equals("2001")) {

		Item word1 = item.createDaughter();
		word1.getFeatures().setString("name", "two");
		words.appendItem(word1);

		Item word2 = item.createDaughter();
		word2.getFeatures().setString("name", "thousand");
		words.appendItem(word2);

		Item word3 = item.createDaughter();
		word3.getFeatures().setString("name", "one");
		words.appendItem(word3);

	    } else {
		Item word = item.createDaughter();
		word.getFeatures().setString("name",
			item.getFeatures().getString("name").toLowerCase());
		words.appendItem(word);
	    }
	}

	wordSylSeg(utterance);
    } 

    /**
     * Populates an utterance with the word/syl/seg relations
     *
     * @param u	the utterance
     */
    private void wordSylSeg(Utterance u) {
	Relation syl = u.createRelation("Syllable");
	Relation sylStructure = u.createRelation("SylStructure");
	Relation seg = u.createRelation("Segment");

	for (Item word = u.getRelation("Words").getHead();
			word != null; word = word.getNext()) {
	    Item ssword = sylStructure.appendItem(word);
	    List phones = lookup(word.getFeatures().getString("name"));
	    for (Iterator pi = phones.iterator(); pi.hasNext(); ) {
		Item segitem = seg.appendItem();
		segitem.getFeatures().setString("name", (String) pi.next());
		ssword.addDaughter(segitem);
	    }
	}
    }

    /**
     * Breaks a word into characters
     * 
     * @param word the word
     * 
     * @return list of single character strings
     */
    private List lookup(String word) {
	List l = new ArrayList();

	for (int i = 0; i < word.length(); i++) {
	    String ph = word.substring(i, i + 1);
	    l.add(ph);
	}
	return l;
    }

    /**
     * Test feature traversal behavior
     */
    public void testFeature1() {
	assertTrue("syl exist", utterance.getRelation("Syllable") != null);
	assertTrue("syls exist", utterance.getRelation("SylStructure") != null);
	assertTrue("segment exists", utterance.getRelation("Segment") != null);
	Item item = utterance.getRelation("Segment").getHead();
	item = item.getNext();
	item = item.getNext();
	item = item.getNext();

	// we should be at the 'u' in january
	assertTrue("name",
		item.findFeature("name").toString().equals("u"));
	assertTrue("n.name",
		item.findFeature("n.name").toString().equals("a"));
	assertTrue("p.name",
		item.findFeature("p.name").toString().equals("n"));
	assertTrue("R:SylStructure.parent", 
		item.findItem("R:SylStructure").toString().equals("u"));
	assertTrue("R:SylStructure.parent",
		item.findItem("R:SylStructure.parent").
		toString().equals("january"));
	assertTrue("R:SylStructure.parent.name",
		item.findFeature("R:SylStructure.parent.name").toString()
	        .equals("january"));
	assertTrue("R:SylStructure.parent.n",
		item.findItem("R:SylStructure.parent.n").toString()
	        .equals("1st"));

	Item token = utterance.getRelation("tokens").getHead();
	assertTrue("token head", token.toString().equals("January"));
	assertTrue("n.n.name", 
		token.findFeature("n.n.name").toString().equals("2001"));
	assertTrue("n.n.daughter.name", 
		token.findFeature("n.n.daughter.name").
		toString().equals("two"));
	assertTrue("n.n.daughter.n.name", 
		token.findFeature("n.n.daughter.n.name").
		toString().equals("thousand"));
	assertTrue("n.n.daughtern.name", 
		token.findFeature("n.n.daughtern.name").
		toString().equals("one"));
    }



    /**
     * Common code run after each test
     */
    protected void tearDown() {
	//utterance = null;
    } 


    /**
     * Tests to see that we succeed
     */
    public void testSuccess() {
	assertTrue("Should succeed", true);
    }

    /**
     * Tests to ensure that an utterance is created properly
     */
    public void testUtteranceCreation() {
	assertTrue("Utterance Created", utterance != null);
	assertTrue("retrieved proper voice", utterance.getVoice() == voice);
    }

    /**
     * Tests the utterance relations capabilities
     */
    public void testUtteranceRelations() {
	assertTrue("Utterance Created", utterance != null);
	Relation tokens = utterance.createRelation("tokens");
	assertTrue("retrieved token relation missing",
		utterance.getRelation("tokens") == tokens);
	assertTrue("token relation missing",
		utterance.hasRelation("tokens"));
	assertTrue("missing relation found",
		!utterance.hasRelation("missing_relation"));
	assertTrue("retrieved missing token relation found",
		utterance.getRelation("missing_relation") == null);
    }

    /**
     * Tests the utterance features capabilities
     */
    public void testUtteranceFeatures() {
	assertTrue("Utterance Created", utterance != null);
	assertTrue("Missing feature found", !utterance.isPresent("not_present"));
	utterance.setString("is_present", "here I am");
	assertTrue("presnt feature found", utterance.isPresent("is_present"));
	assertTrue("present feature retreived", 
		utterance.getString("is_present").equals("here I am"));
	assertTrue("missing voice feature found",
		!utterance.isPresent("voice_feature"));
	voice.getFeatures().setString("voice_feature", "is_set");
	assertTrue("voice feature missing",
		voice.getFeatures().isPresent("voice_feature"));
	assertTrue("voice feature missing",
		utterance.isPresent("voice_feature"));
	assertTrue("voice present feature retreived", 
		utterance.getString("voice_feature").equals("is_set"));

	utterance.setFloat("pi", (float) Math.PI);
	assertTrue("float get", utterance.getFloat("pi") == (float) Math.PI);

	utterance.setInt("one", 1);
	assertTrue("int get", utterance.getInt("one") == 1);
	Object o = new Object();
	utterance.setObject("object", o);
	assertTrue("object get", utterance.getObject("object") == o);

	try {
	    utterance.getFloat("one");
	    assertTrue("Cast exception mission", false);
	} catch (ClassCastException e) {
	    assertTrue("cast error OK", true);
	}
	utterance.remove("one");
	assertTrue("removed  feature found", !utterance.isPresent("one"));
    }


    /**
     * Tests the detailed relations capabilities
     */
    public void testRelations() {
	int index = 0;;
	int testSize = 10;
	Item[] items = new Item[testSize];
	Relation r = utterance.createRelation("itemTests");
	for (int i = 0; i < testSize; i++) {
	    items[i] = r.appendItem();
	}

	assertTrue("utterance OK", r.getUtterance() == utterance);
	assertTrue("Name ok", r.getName().equals("itemTests"));

	index = 0;
	for (Item item = r.getHead(); item != null; 
			item = item.getNext(), index++) {
	    assertTrue("Proper items", items[index] == item);
	}

	assertTrue("Items not equal", !items[0].getSharedContents().equals(
		    			items[1].getSharedContents()));
	Item dup  = r.appendItem();
	Item dup2 = r.appendItem(dup);
	assertTrue("Items should be equal", dup.getSharedContents().equals(
		    			    dup2.getSharedContents()));
    }

    /**
     * Tests the Item class capabilities
     */
    public void testItems() {
	Relation r = utterance.createRelation("tokens");
	Relation r2 = utterance.createRelation("words");
	Item parent  = r.appendItem();
	Item i2 = r.appendItem();
	Item dup2 = r.appendItem(i2);
	Item d1 = parent.createDaughter();
	Item d2 = parent.createDaughter();
	Item d3 = parent.createDaughter();

	assertTrue("dup equals",
		dup2.getSharedContents().equals(i2.getSharedContents()));
	assertTrue("parent check 1", d1.getParent() == parent);
	assertTrue("parent check 2", d2.getParent() == parent);
	assertTrue("parent check 3", d3.getParent() == parent);

	//assertTrue("daugher size", parent.getDaughters().size() == 3);
	assertTrue("first daughter", parent.getDaughter() == d1);
	assertTrue("second daughter", parent.getNthDaughter(1) == d2);
	assertTrue("Last daughter", parent.getLastDaughter() == d3);

	assertTrue("owner", parent.getOwnerRelation() == r);

	Item r2i1 = r2.appendItem();
	assertTrue("owner r2i1", r2i1.getOwnerRelation() == r2);

	Item r1i1 = r.appendItem(r2i1);
	assertTrue("r1,r2 equal",
		r1i1.getSharedContents().equals(r2i1.getSharedContents()));
	assertTrue("owner r2i1 reprise", r2i1.getOwnerRelation() == r2);
	assertTrue("owner r1i1 ", r1i1.getOwnerRelation() == r);
	assertTrue("no parent", r2i1.getParent() == null);
	assertTrue("r1i1 no daughters", !r1i1.hasDaughters());
	assertTrue("r1i1 utterance", r1i1.getUtterance() == utterance);

      // test the feature capability
	assertTrue("Missing feature found", 
		!r1i1.getFeatures().isPresent("not_present"));
	r1i1.getFeatures().setString("is_present", "here I am");
	assertTrue("presnt feature found", 
		r1i1.getFeatures().isPresent("is_present"));
	assertTrue("present feature retreived", 
		r1i1.getFeatures().getString("is_present").equals("here I am"));
	assertTrue("missing voice feature found",
		!r1i1.getFeatures().isPresent("voice_feature"));

	r1i1.getFeatures().setFloat("pi", (float) Math.PI);
	assertTrue("float get", 
		r1i1.getFeatures().getFloat("pi") == (float) Math.PI);
	assertTrue("float get r2", 
		r2i1.getFeatures().getFloat("pi") == (float) Math.PI);

	r1i1.getFeatures().setInt("one", 1);
	assertTrue("int get", r1i1.getFeatures().getInt("one") == 1);
	Object o = new Object();
	r1i1.getFeatures().setObject("object", o);
	assertTrue("object get", r1i1.getFeatures().getObject("object") == o);
	assertTrue("object get r2", r2i1.getFeatures().getObject("object") == o);

	try {
	    r1i1.getFeatures().getFloat("one");
	    assertTrue("Cast exception mission", false);
	} catch (ClassCastException e) {
	    assertTrue("cast error OK", true);
	}
	r1i1.getFeatures().remove("one");
	assertTrue("removed  feature found", !r1i1.getFeatures().isPresent("one"));
    }


    /**
     * Factory method that creates the test suite.
     * 
     * @return the test suite.
     */
    public static Test suite() {
	return new TestSuite(UtteranceTests.class);
    } 



    /**
     * Main entry point for this test suite.
     * 
     * @param  args    the command line arguments.
     */
    public static void main(String[] args) {
	junit.textui.TestRunner.run(suite());
    } 
}



/**
 * A test utterance processor
 */
class TestUtteranceProcessor implements UtteranceProcessor {
    String name;
    public TestUtteranceProcessor(String name) {
	this.name = name;
    }

    public void processUtterance(Utterance u) throws ProcessException {
	System.out.println("Processing " + name);
    }

    public String toString() {
	return name;
    }
}

  

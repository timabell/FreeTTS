/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package tests;

import java.io.IOException;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.sun.speech.freetts.PartOfSpeech;
import com.sun.speech.freetts.PartOfSpeechImpl;

/**
 * JUnit Tests for the PartOfSpeech class
 * 
 * @version 1.0
 */
public class PartOfSpeechTests extends TestCase {
    PartOfSpeech pos;

    /**
     * Creates the set of UtteranceTests
     * 
     * @param name
     *            the name of the test.
     */
    public PartOfSpeechTests(String name) {
        super(name);
    }

    /**
     * Common code run before each test
     */
    protected void setUp() {
        try {
            URL url = new URL("file:unittests/tests/part_of_speech.txt");
            pos = new PartOfSpeechImpl(url, "content");
        } catch (IOException ioe) {
            System.out.println("Can't open part_of_speech.txt");
        }
    }

    /**
     * test that checks for proer determination of part-of-speech
     */
    public void testPartOfSpeech() {
        assertTrue(pos.getPartOfSpeech("of").equals("in"));
        assertTrue(pos.getPartOfSpeech("from").equals("in"));
        assertTrue(pos.getPartOfSpeech("about").equals("in"));
        assertTrue(pos.getPartOfSpeech("up").equals("in"));
        assertTrue(pos.getPartOfSpeech("down").equals("in"));

        assertTrue(pos.getPartOfSpeech("each").equals("det"));
        assertTrue(pos.getPartOfSpeech("both").equals("det"));
        assertTrue(pos.getPartOfSpeech("no").equals("det"));
        assertTrue(pos.getPartOfSpeech("this").equals("det"));

        assertTrue(pos.getPartOfSpeech("will").equals("md"));
        assertTrue(pos.getPartOfSpeech("can").equals("md"));
        assertTrue(pos.getPartOfSpeech("ought").equals("md"));
        assertTrue(pos.getPartOfSpeech("might").equals("md"));

        assertTrue(pos.getPartOfSpeech("and").equals("cc"));
        assertTrue(pos.getPartOfSpeech("but").equals("cc"));
        assertTrue(pos.getPartOfSpeech("or").equals("cc"));
        assertTrue(pos.getPartOfSpeech("yet").equals("cc"));

        assertTrue(pos.getPartOfSpeech("who").equals("wp"));
        assertTrue(pos.getPartOfSpeech("what").equals("wp"));
        assertTrue(pos.getPartOfSpeech("where").equals("wp"));
        assertTrue(pos.getPartOfSpeech("when").equals("wp"));

        assertTrue(pos.getPartOfSpeech("her").equals("pps"));
        assertTrue(pos.getPartOfSpeech("his").equals("pps"));
        assertTrue(pos.getPartOfSpeech("our").equals("pps"));
        assertTrue(pos.getPartOfSpeech("mine").equals("pps"));

        assertTrue(pos.getPartOfSpeech("is").equals("aux"));
        assertTrue(pos.getPartOfSpeech("am").equals("aux"));
        assertTrue(pos.getPartOfSpeech("are").equals("aux"));
        assertTrue(pos.getPartOfSpeech("was").equals("aux"));
        assertTrue(pos.getPartOfSpeech("were").equals("aux"));
        assertTrue(pos.getPartOfSpeech("be").equals("aux"));

        assertTrue(pos.getPartOfSpeech(".").equals("punc"));
        assertTrue(pos.getPartOfSpeech(",").equals("punc"));
        assertTrue(pos.getPartOfSpeech(":").equals("punc"));
        assertTrue(pos.getPartOfSpeech(";").equals("punc"));
        assertTrue(pos.getPartOfSpeech("'").equals("punc"));
        assertTrue(pos.getPartOfSpeech("(").equals("punc"));
        assertTrue(pos.getPartOfSpeech("?").equals("punc"));
        assertTrue(pos.getPartOfSpeech(")").equals("punc"));

        assertTrue(pos.getPartOfSpeech("bear").equals("content"));
        assertTrue(pos.getPartOfSpeech("lamere").equals("content"));
        assertTrue(pos.getPartOfSpeech("walker").equals("content"));
        assertTrue(pos.getPartOfSpeech("kwok").equals("content"));
        assertTrue(pos.getPartOfSpeech("cumquat").equals("content"));
        assertTrue(pos.getPartOfSpeech("marshmellow").equals("content"));
        assertTrue(pos.getPartOfSpeech("tryptich").equals("content"));
    }

    /**
     * Common code run after each test
     */
    protected void tearDown() {
        // utterance = null;
    }

    /**
     * Factory method that creates the test suite.
     * 
     * @return the test suite.
     */
    public static Test suite() {
        return new TestSuite(PartOfSpeechTests.class);
    }

    /**
     * Main entry point for this test suite.
     * 
     * @param args
     *            the command line arguments.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}

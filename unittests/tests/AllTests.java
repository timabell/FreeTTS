/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package tests;

import junit.framework.*;
import com.sun.speech.freetts.*;

/**
 * Peforms all the tests for the freetts system. Adds all of the tests
 * to the test suite and runs them. 
 *
 *
 * FreeTTS tests use the JUnit unit test package. More information on
 * JUnit and how to use junit can be found at www.junit.org. We have
 * used JUnit 3.7 in this project.
 *
 * To run the text form of the test use "make tests".  To run the
 * tests within a Swing GUI use "make guitests"
 *
 * @version 1.0
 */
public class AllTests {

    /**
     * Main entry point for the tests.
     * @param  args    the arguments.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
	System.exit(0);
    }

    /**
     * Factory method that creates the suite of tests.
     * To add more tests, call 'suite.addTest' with the suite of tests to
     * add.
     * 
     * @return the test suite.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("All JUnit Tests");
        suite.addTest(UtteranceTests.suite());
        suite.addTest(UnitDatabaseTests.suite());
	suite.addTest(LetterToSoundTest.suite());
	suite.addTest(LexiconTest.suite());
	suite.addTest(SegmenterTests.suite());
	suite.addTest(PartOfSpeechTests.suite());
	return suite;
    }
}


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
import java.net.URL;
import com.sun.speech.freetts.*;
import com.sun.speech.freetts.diphone.DiphoneUnitDatabase;

/**
 * JUnit Tests for the DiphoneUnitDatabase test. 
 * 
 * @version 1.0
 */
public class UnitDatabaseTests extends TestCase {
    DiphoneUnitDatabase udb;
    private final static String BINARY_DB =
    "file:../bld/classes/com/sun/speech/freetts/en/us/cmu_us_kal/cmu_us_kal.bin";
    private final static String TEXT_DB =
    "file:../com/sun/speech/freetts/en/us/cmu_us_kal/cmu_us_kal.txt";

    /**
     * Creates the set of UtteranceTests
     * 
     * @param  name the name of the test.
     */
    public UnitDatabaseTests(String name) {
	super(name);
    }

    /**
     * Common code run before each test
     */
    protected void setUp() {
	try {
	    udb = new DiphoneUnitDatabase(new
		    URL(BINARY_DB), true);
	} catch (IOException ioe) {
	    System.out.println("Can't load db " + ioe);
	}
    } 


    /**
     * Checks to make sure that the  binary and text version of the DB
     * compare.
     */
    public void testIdentical() {
	DiphoneUnitDatabase udbTextVersion = null;
	try {
	    udbTextVersion = new DiphoneUnitDatabase(
		    new URL(TEXT_DB), false);

        } catch (IOException ioe) {
	    System.out.println("Can't load text db " + ioe);
	}
	assertTrue("db loaded", udb != null);
	assertTrue("txt db loaded", udbTextVersion != null);
	assertTrue("DBs identical", udb.compare(udbTextVersion));
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
     * Factory method that creates the test suite.
     * 
     * @return the test suite.
     */
    public static Test suite() {
	return new TestSuite(UnitDatabaseTests.class);
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



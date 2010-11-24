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
import com.sun.speech.freetts.lexicon.LetterToSound;
import com.sun.speech.freetts.lexicon.LetterToSoundImpl;

/**
 * Provides junit tests for the LetterToSound class
 * 
 * @version 1.0
 */
public class LetterToSoundTest extends TestCase {
    BufferedReader reader = null;
    LetterToSound lts = null;

    /**
     * Creates the set of LetterToSoundTest
     * 
     * @param  name the name of the test.
     */
    public LetterToSoundTest(String name) {
	super(name);
    }


    /**
     * Common code run before each test
     */
    protected void setUp() {
	try {
            lts = new LetterToSoundImpl( 
	     new URL("file:bld/classes/com/sun/speech/freetts/en/us/cmulex_lts.bin"), true);
            assertTrue("LTS Rules created", lts != null);
            InputStream in =
                LetterToSoundTest.class.getResourceAsStream("LTS.txt");
            reader = new BufferedReader(new InputStreamReader(in));
            assertTrue("Data File opened", reader != null);
        } catch (IOException e) {
                e.printStackTrace();
        }
    
    }
    
    /**
     * Common code run after each test
     */
    protected void tearDown() {
    } 


    /**
     * Tests to see that we succeed
     */
    public void testSuccess() {
	assertTrue("Should succeed", true);
    }


    /**
     * Tests that LTS generated match those from the standard results.
     */
    public void testLTS() {
        String word;
        int i;
        String flite_phones;
        String[] lts_phone_array;
        StringBuffer lts_phones;
	String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("***")) {
                    continue;
                }
                i = line.indexOf(' ');
                word = line.substring(0,i);
                flite_phones = line.substring(i+1);
                lts_phone_array = lts.getPhones(word, null);
                assertTrue("Phones returned for " + word + " is not null: ",
                           lts_phone_array != null);
                lts_phones = new StringBuffer("(");
                for (i = 0; i < lts_phone_array.length; i++) {
                    if (i != 0) {
                        lts_phones.append(" ");
                    }
                    lts_phones.append(lts_phone_array[i]);
                }
                lts_phones.append(")");
                assertTrue("Phones returned for " + word + " are identical "
                           + "(Our phones: " + lts_phones + ", "
                           + "Flite phones: " + flite_phones + "): ",
                           flite_phones.equals(lts_phones.toString()));
            }
        } catch (IOException e) {
            assertTrue("FILE IO problem: ", false);
        }
    }

    /*
     * Tests to see if the binary version of the database matches
     * that of the text database.
    public void testBinaryLoad() {
	try {
	    LetterToSoundImpl text = new LetterToSoundImpl(
		    new URL("file:../en/us/cmulex_lts.txt"),  false);
	    LetterToSoundImpl binary = new LetterToSoundImpl(
		    new URL("file:../en/us/cmulex_lts.bin"),  true);
	    
	    assertTrue("text binary compare", text.compare(binary));
	} catch (IOException ioe) {
	    fail("Can't load lts " + ioe);
	}
    }
            
    /**
     * Factory method that creates the test suite.
     * 
     * @return the test suite.
     */
    public static Test suite() {
	return new TestSuite(LetterToSoundTest.class);
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

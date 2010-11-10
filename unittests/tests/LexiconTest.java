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
import com.sun.speech.freetts.lexicon.Lexicon;
import com.sun.speech.freetts.en.us.CMULexicon;

/**
 * JUnit tests Tests for the LexiconTest class
 * 
 * @version 1.0
 */
public class LexiconTest extends TestCase {
    BufferedReader reader = null;
    Lexicon lex = null;
    
    /**
     * Creates the set of LexiconTest
     * 
     * @param  name the name of the test.
     */
    public LexiconTest(String name) {
	super(name);
    }


    /**
     * Common code run before each test
     */
    protected void setUp() {
	try {
            lex = CMULexicon.getInstance(true);
            assertTrue("Lexicon Created", lex != null);
            reader = new BufferedReader(new FileReader("LEX.txt"));
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
     * Tests that Lexicon matches those from the standard results.
     */
    public void testLexicon() {
        String word;
        String pos;
        int i;
        String flite_phones;
        String[] lex_phone_array;
        StringBuffer lex_phones;
	String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("***")) {
                    continue;
                }
                i = line.indexOf(' ');
                word = line.substring(0,i);
                line = line.substring(i+1);
                i = line.indexOf(' ');
                pos = line.substring(0,i);
                flite_phones = line.substring(i+1);
                lex_phone_array = lex.getPhones(word, pos);
                assertTrue("Phones returned for " + word + pos
                           + " is not null: ",
                           lex_phone_array != null);
                lex_phones = new StringBuffer("(");
                for (i = 0; i < lex_phone_array.length; i++) {
                    if (i != 0) {
                        lex_phones.append(" ");
                    }
                    lex_phones.append(lex_phone_array[i]);
                }
                lex_phones.append(")");
                assertTrue("Phones returned for " + word + pos
                           + " are identical "
                           + "(Our phones: " + lex_phones + ", "
                           + "Flite phones: " + flite_phones + "): ",
                           flite_phones.equals(lex_phones.toString()));
            }
        } catch (IOException e) {
            assertTrue("FILE IO problem: ", false);
        }
    }
            
    /**
     * Factory method that creates the test suite.
     * 
     * @return the test suite.
     */
    public static Test suite() {
	return new TestSuite(LexiconTest.class);
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

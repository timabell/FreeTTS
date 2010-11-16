package tests;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.sun.speech.freetts.cart.CARTImpl;
import com.sun.speech.freetts.en.us.CMUVoice;

/**
 * JUnit tests Tests to dump cart trees into dot files
 * 
 * @version 1.0
 */
public class CartDumping extends TestCase {

    public CartDumping(String name) {
        super(name);
    }

    protected void setUp() {
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
    public void testCartDumping() throws IOException {
        CARTImpl numbersCart = new CARTImpl(CMUVoice.class.getResource("nums_cart.txt"));
        CARTImpl phrasingCart = new CARTImpl(CMUVoice.class.getResource("phrasing_cart.txt"));
        CARTImpl accentCart = new CARTImpl(CMUVoice.class.getResource("int_accent_cart.txt"));
        CARTImpl toneCart = new CARTImpl(CMUVoice.class.getResource("int_tone_cart.txt"));
        CARTImpl durzCart = new CARTImpl(CMUVoice.class.getResource("durz_cart.txt"));

        /**
         * Dump the CART tree as a dot file.
         * 
         * The dot tool is part of the graphviz distribution at http://www.graphviz.org/.
         * If installed, call it as "dot -O -Tpdf *.dot" from the console to generate pdfs.
         * 
         */

        numbersCart.dumpDot(new PrintWriter(new FileOutputStream("unittests/tests/dotfiles/numbersCart.dot")));
        phrasingCart.dumpDot(new PrintWriter(new FileOutputStream("unittests/tests/dotfiles/phrasingCart.dot")));
        accentCart.dumpDot(new PrintWriter(new FileOutputStream("unittests/tests/dotfiles/accentCart.dot")));
        toneCart.dumpDot(new PrintWriter(new FileOutputStream("unittests/tests/dotfiles/toneCart.dot")));
        durzCart.dumpDot(new PrintWriter(new FileOutputStream("unittests/tests/dotfiles/durzCart.dot")));
        
    }
            
    /**
     * Factory method that creates the test suite.
     * 
     * @return the test suite.
     */
    public static Test suite() {
        return new TestSuite(CartDumping.class);
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

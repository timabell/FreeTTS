package com.sun.speech.freetts.cart;

import static org.junit.Assert.fail;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sun.speech.freetts.PhoneDurations;
import com.sun.speech.freetts.PhoneDurationsImpl;
import com.sun.speech.freetts.Token;
import com.sun.speech.freetts.Utterance;

/**
 * Test case for the Durator.
 * @author Dirk Schnelle-Walka
 */
public class DuratorTest {
    private Durator durator;
    
    /**
     * Set up the test environment.
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        URL urlCart = new URL("file:src/com/sun/speech/freetts/en/us/durz_cart.txt");
        CARTImpl cart = new CARTImpl(urlCart);
        URL urlPhones = new URL("file:src/com/sun/speech/freetts/en/us/dur_stat.txt");
        PhoneDurations durations = new PhoneDurationsImpl(urlPhones);
        durator = new Durator(cart, durations);
    }

    @Test
    public void testProcessUtterance() {
        List<Token> tokenList = new ArrayList<Token>();
        Utterance utterance = new Utterance(null, tokenList);
        fail("Not yet implemented");
    }

}

/**
 * 
 */
package com.sun.speech.freetts;

import java.net.URL;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for the PhoneDurations.
 * @author Dirk Schnelle-Walka
 *
 */
public class PhoneDurationsImplTest {
    private PhoneDurations durations;
    
    /**
     * Set up the test environment.
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        URL url = new URL("file:src/com/sun/speech/freetts/en/us/dur_stat.txt");
        durations = new PhoneDurationsImpl(url);
    }

    /**
     * Test method for {@link com.sun.speech.freetts.PhoneDurationsImpl#getPhoneDuration(java.lang.String)}.
     */
    @Test
    public void testGetPhoneDuration() {
        PhoneDuration duration = durations.getPhoneDuration("ey");
        Assert.assertEquals(0.165883f, duration.getMean());
        Assert.assertEquals(0.075700f, duration.getStandardDeviation());
    }

    /**
     * Test method for {@link com.sun.speech.freetts.PhoneDurationsImpl#getPhoneDuration(java.lang.String)}.
     */
    @Test
    public void testGetPhoneDurationUnknown() {
        Assert.assertNull(durations.getPhoneDuration("asdf"));
    }
}

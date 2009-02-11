/**
 * Portions Copyright 2001 Sun Microsystems, Inc.
 * Portions Copyright 1999-2001 Language Technologies Institute, 
 * Carnegie Mellon University.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package com.sun.speech.freetts;


/**
 * Maintains a list of phones with various features for those phones.
 */
public interface PhoneSet  {

    /**
     * Vowel or consonant:  + = vowel, - = consonant.
     */
    public final static String VC = "vc";  

    /**
     * Vowel length:  s = short, l = long, d = dipthong, a = schwa.
     */
    public final static String VLNG = "vlng";  

    /**
     * Vowel height:  1 = high,  2 = mid,  3 = low.
     */
    public final static String VHEIGHT = "vheight";  

    /**
     * Vowel frontness:  1 = front, 2 = mid, 3 = back.
     */
    public final static String VFRONT = "vfront";  

    /**
     * Lip rounding:  + = on, - = off.
     */
    public final static String VRND = "vrnd";  

    /**
     * Consonant type:  s = stop, f = fricative,  a = affricative,
     * n = nasal, l = liquid.
     */
    public final static String CTYPE = "ctype";  

    /**
     * Consonant cplace:  l = labial, a = alveolar, p = palatal,
     * b = labio_dental, d = dental, v = velar
     */
    public final static String CPLACE = "cplace";  

    /**
     * Consonant voicing:  + = on, - = off
     */
    public final static String CVOX = "cvox";  

    /**
     * Given a phoneme and a feature name, return the feature.
     *
     * @param phone the phoneme of interest
     * @param featureName the name of the feature of interest
     *
     * @return the feature with the given name
     */
    public String getPhoneFeature(String phone, String featureName);
}

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
package com.sun.speech.freetts.en.us;

import java.util.regex.*;


/**
 * Provides the definitions for US English whitespace, punctuations,
 * prepunctuation, and postpunctuation symbols. It also contains a set of
 * Regular Expressions for the US English language.
 * With regular expressions, it specifies what are whitespaces,
 * letters in the alphabet, uppercase and lowercase letters, alphanumeric
 * characters, identifiers, integers, doubles, digits, and 'comma and int'. 
 *
 * It translates the following code from flite:
 * src/regex/cst_regex.c
 * lang/usenglish/us_text.c
 */
public class USEnglish {

    /** default whitespace regular expression pattern */
    public static final String RX_DEFAULT_US_EN_WHITESPACE = "[ \n\t\r]+";
    /** default letter regular expression pattern */
    public static final String RX_DEFAULT_US_EN_ALPHABET = "[A-Za-z]+";
    /** default uppercase regular expression pattern */
    public static final String RX_DEFAULT_US_EN_UPPERCASE = "[A-Z]+";
    /** default lowercase regular expression pattern */
    public static final String RX_DEFAULT_US_EN_LOWERCASE = "[a-z]+";
    /** default alpha-numeric regular expression pattern */
    public static final String RX_DEFAULT_US_EN_ALPHANUMERIC = "[0-9A-Za-z]+";
    /** default identifier regular expression pattern */
    public static final String RX_DEFAULT_US_EN_IDENTIFIER = "[A-Za-z_][0-9A-Za-z_]+";
    /** default integer regular expression pattern */
    public static final String RX_DEFAULT_US_EN_INT = "-?[0-9]+";
    /** default double regular expression pattern */
    public static final String RX_DEFAULT_US_EN_DOUBLE =
    "-?(([0-9]+\\.[0-9]*)|([0-9]+)|(\\.[0-9]+))([eE][---+]?[0-9]+)?";
    /** default integer with commas  regular expression pattern */
    public static final String RX_DEFAULT_US_EN_COMMAINT =
    "[0-9][0-9]?[0-9]?,([0-9][0-9][0-9],)*[0-9][0-9][0-9](\\.[0-9]+)?";
    /** default digits regular expression pattern */
    public static final String RX_DEFAULT_US_EN_DIGITS = "[0-9][0-9]*";
    /** default dotted abbreviation  regular expression pattern */
    public static final String RX_DEFAULT_US_EN_DOTTED_ABBREV = "([A-Za-z]\\.)*[A-Za-z]";
    /** default ordinal number regular expression pattern */
    public static final String RX_DEFAULT_US_EN_ORDINAL_NUMBER =
    "[0-9][0-9,]*(th|TH|st|ST|nd|ND|rd|RD)";
    
    
    /** whitespace regular expression pattern */
    public static String RX_WHITESPACE = RX_DEFAULT_US_EN_WHITESPACE;
    /** letter  regular expression pattern */
    public static String RX_ALPHABET = RX_DEFAULT_US_EN_ALPHABET;
    /** uppercase  regular expression pattern */
    public static String RX_UPPERCASE = RX_DEFAULT_US_EN_UPPERCASE;
    /** lowercase  regular expression pattern */
    public static String RX_LOWERCASE = RX_DEFAULT_US_EN_LOWERCASE;
    /** alphanumeric  regular expression pattern */
    public static String RX_ALPHANUMERIC = RX_DEFAULT_US_EN_ALPHANUMERIC;
    /** identifier  regular expression pattern */
    public static String RX_IDENTIFIER = RX_DEFAULT_US_EN_IDENTIFIER;
    /** integer  regular expression pattern */
    public static String RX_INT = RX_DEFAULT_US_EN_INT;
    /** double  regular expression pattern */
    public static String RX_DOUBLE = RX_DEFAULT_US_EN_DOUBLE;
    /** comma separated integer  regular expression pattern */
    public static String RX_COMMAINT = RX_DEFAULT_US_EN_COMMAINT;
    /** digits regular expression pattern */
    public static String RX_DIGITS = RX_DEFAULT_US_EN_DIGITS;
    /** dotted abbreviation  regular expression pattern */
    public static String RX_DOTTED_ABBREV = RX_DEFAULT_US_EN_DOTTED_ABBREV;
    /** ordinal number regular expression pattern */
    public static String RX_ORDINAL_NUMBER = RX_DEFAULT_US_EN_ORDINAL_NUMBER;
    

    // the following symbols are from lang/usenglish/us_text.c

    /** punctuation regular expression pattern */
    public static final String PUNCTUATION_SYMBOLS = "\"'`.,:;!?(){}[]";
    /** pre-punctuation regular expression pattern */
    public static final String PREPUNCTUATION_SYMBOLS = "\"'`({[";
    /** single char symbols  regular expression pattern */
    public static final String SINGLE_CHAR_SYMBOLS = "";
    /** whitespace symbols  regular expression pattern */
    public static final String WHITESPACE_SYMBOLS = " \t\n\r";


    /**
     * Not constructable
     */
    private USEnglish() {}
}


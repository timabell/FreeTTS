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

import java.util.List;
import java.io.Reader;

/**
 * Chops a string or text file into Token instances.
 */
public interface Tokenizer {
    /**
     * Sets the text to be tokenized by this tokenizer.
     *
     * @param textToTokenize  the text to tokenize
     */
    public void setInputText(String textToTokenize);

    /**
     * Sets the input reader.
     *
     * @param  reader the input source
     */
    public void setInputReader(Reader reader);
    
    
    /**
     * Returns the next token.
     *
     * @return  the next token if it exists; otherwise null
     */
    public Token getNextToken();


    /**
     * Returns true if there are more tokens, false otherwise.
     *
     * @return true if there are more tokens; otherwise false
     */
    public boolean hasMoreTokens();

    /**
     * Returns true if there were errors while reading tokens.
     *
     * @return true if there were errors; otherwise false
     */
    public boolean hasErrors();

    /**
     * If hasErrors returns true, returns a description of the error
     * encountered.  Otherwise returns null.
     *
     * @return a description of the last error that occurred
     */
    public String getErrorDescription();

    /**
     * Sets the whitespace symbols of this Tokenizer to the given
     * symbols.
     * 
     * @param symbols the whitespace symbols
     */
    public void setWhitespaceSymbols(String symbols);

    /**
     * Sets the single character symbols of this Tokenizer to the given
     * symbols.
     *
     * @param symbols the single character symbols
     */
    public void setSingleCharSymbols(String symbols);

    /**
     * Sets the prepunctuation symbols of this Tokenizer to the given
     * symbols.
     *
     * @param symbols the prepunctuation symbols
     */
    public void setPrepunctuationSymbols(String symbols);

    /**
     * Sets the postpunctuation symbols of this Tokenizer to the given
     * symbols.
     *
     * @param symbols the postpunctuation symbols
     */
    public void setPostpunctuationSymbols(String symbols);

    /**
     * Determines if the current token should start a new sentence.
     *
     * @return true if a new sentence should be started
     */
    public boolean isBreak();
}

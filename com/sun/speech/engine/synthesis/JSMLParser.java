/**
 * Copyright 1998-2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package com.sun.speech.engine.synthesis;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.JSMLException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Parses a JSML 0.6 document and returns a DOM.
 */
public class JSMLParser {
    /**
     * The DOM.
     */
    Document document;
    
    /**
     * Creates a new JSMLParser for the given JSML
     * text.  Parses the text immediately and return any errors.
     * The resulting DOM Document can be retrieved via
     * <code>getDocument</code>.
     * The optional validate will do validation of the JSML text.
     * This is typically not used since JSML doesn't require validation.
     *
     * @param jsmlText the JSML text
     * @param validate if <code>true</code>, validate the JSML text
     *
     * @see #getDocument
     *
     * @throws JSMLException if the JSML text contains errors
     */
    public JSMLParser(String jsmlText, boolean validate)
        throws JSMLException {

        // Handle case where text does not include a root element
        //
        if (!(jsmlText.substring(0,2).equals("<?"))) {
            jsmlText = "<jsml>\n" + jsmlText + "</jsml>\n";
        }

        try {
            document = parse(new InputSource(new StringReader(jsmlText)),
                             validate);
        } catch (IOException e) {
            throw new JSMLException("JSMLParser: " + e.getMessage());
        }
    }

    /**
     * Creates a new JSMLParser for the given <code>URL</code>.
     * Parses the text immediately and returns any errors.
     * The resulting DOM Document can be retrieved via
     * <code>getDocument</code>.
     * The optional validate will do validation of the JSML text.
     * This is typically not used since JSML doesn't require validation.
     *
     * @param jsmlSource the URL containing JSML text
     * @param validate if <code>true</code>, validate the JSML text
     *
     * @see #getDocument
     *
     * @throws JSMLException if the JSML text contains errors
     * @throws IOException if problems encountered with URL
     */
    public JSMLParser(URL jsmlSource, boolean validate) 
        throws JSMLException, IOException {
        document = parse(new InputSource(jsmlSource.openStream()),
                         validate);
    }

    /**
     * Gets the document for this parser.
     *
     * @return a DOM
     */
    public Document getDocument() {
        return document;
    }
    
    /**
     * Parses the source and optionally validates it.
     *
     * @param source the JSML text
     * @param validate if <code>true</code>, validate the JSML text
     *
     * @throws JSMLException if the JSML text contains errors
     * @throws IOException if problems encountered with <code>source</code>
     */
    protected Document parse(InputSource source, boolean validate)
        throws JSMLException, IOException {
        
        // [[[TODO: Note that this could be much more efficient in
        // that the <code>DocumentBuilderFactory</code> and
        // <code>DocumentBuilder</code> could be saved for reuse.
        //
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        Document doc = null;
        
        dbf.setValidating(validate);
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(false);
        dbf.setCoalescing(true);
        dbf.setExpandEntityReferences(true);

        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new JSMLException(
                "JSMLParser: " + e.getMessage());
        }
        
        try {
            doc = db.parse(source);
        } catch (SAXException e) {
            throw new JSMLException("JSMLParser: " + e.getMessage());
        }
        return doc;
    }
}

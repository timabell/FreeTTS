/**
 * Copyright 2002 DFKI GmbH.
 * Portions Copyright 2002 Sun Microsystems, Inc.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package de.dfki.lt.freetts.en.us;

import com.sun.speech.freetts.util.Utilities;
import java.io.File;
import java.net.URL;

import com.sun.speech.freetts.Item;
import com.sun.speech.freetts.ProcessException;
import com.sun.speech.freetts.Relation;
import com.sun.speech.freetts.Utterance;
import com.sun.speech.freetts.UtteranceProcessor;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.en.us.CMUVoice;

import de.dfki.lt.freetts.mbrola.ParametersToMbrolaConverter;
import de.dfki.lt.freetts.mbrola.MbrolaCaller;
import de.dfki.lt.freetts.mbrola.MbrolaAudioOutput;

import java.io.IOException;

/**
 * Defines an unlimited-domain diphone synthesis based voice using
 * the MBROLA synthesis.
 */
public class MbrolaVoice extends CMUVoice {

    private String databaseDirectory; // where the voice database is
    private String database;          // name of the voice database


    /**
     * Creates an MbrolaVoice.
     *
     * @param createLexicon if <code>true</code> automatically load up
     *    the default CMU lexicon; otherwise, don't load it.
     * @param databaseDirectory the directory within the MBROLA directory
     *    where the voice database of this voice is located
     * @param database the name of the voice database of this voice
     */
    public MbrolaVoice(boolean createLexicon, String databaseDirectory, 
                       String database) {
	super(createLexicon);
        this.databaseDirectory = databaseDirectory;
        this.database = database;
    }

    //[[Providing the Mbrola classes via getUnitSelector() and
    // getUnitConcatenator() is just a hack allowing us to use
    // the current CMUVoice.java framework. It only means that
    // after the Durator and the ContourGenerator, the classes
    // process the utterance (Selector before Concatenator).]]
    /**
     * Returns the unit selector to be used by this voice.
     * Derived voices typically override this to customize behaviors.
     * 
     * @return the unit selector
     * 
     * @throws IOException if an IO error occurs while getting
     *     processor
     */
    protected UtteranceProcessor getUnitSelector() throws IOException {
        return new ParametersToMbrolaConverter();
    }

    /**
     * Returns the command line that invokes the MBROLA executable.
     * The command will be in the form of:
     *
     * <pre> {mbrolaExecutable} -e -I {mbrolaRenameTable} {mbrolaVoiceDB} 
     * - -.raw </pre>
     */
    protected String getMbrolaCommand() {

        // Construct the mbrola command in such a way that
        // mbrola reads from stdin and writes raw, headerless audio data
        // to stdout; translates CMU us radio to sampa phonetic symbols;
        // and only complains, but does not abort, when encountering an
        // unknown diphone:
        String cmd = getMbrolaBinary() + " -e -I " + getRenameTable() + " " + 
            getDatabase() + " - -.raw";
        
        return cmd;
    }

    /**
     * Returns the absolute name of the MBROLA directory.
     *
     * @return the absolute name of the MBROLA directory
     */
    public String getMbrolaBase() {
        return Utilities.getProperty("mbrola.base", ".");
    }

    /**
     * Returns the absolute file name of the MBROLA binary.
     *
     * @return the absolute file name of the MBROLA binary
     */
    public String getMbrolaBinary() {
        return getMbrolaBase() + File.separator + "mbrola";
    }

    /**
     * Returns the absolute file name of the MBROLA phonetic symbols
     * rename table.
     *
     * @return the absolute file name of the rename table
     */
    public String getRenameTable() {
        return getMbrolaBase() + File.separator + "us1" + 
            File.separator + "us1mrpa";
    }

    /**
     * Returns the absolute file name of the Voice database
     * this MbrolaVoice uses.
     *
     * @return the absolute file name of the Voice database
     */
    public String getDatabase() {
        return getMbrolaBase() + File.separator + 
            databaseDirectory + File.separator + database;
    }

    /**
     * Returns the unit concatenator to be used by this voice.
     * Derived voices typically override this to customize behaviors.
     * 
     * @return the unit conatenator
     * 
     * @throws IOException if an IO error occurs while getting
     *     processor
     */
    protected UtteranceProcessor getUnitConcatenator() throws IOException {
        return null;
    }

    /**
     * Returns the audio output used by this voice.
     *
     * @return the audio output used by this voice
     *
     * @throws IOException if an I/O error occurs
     */
    protected UtteranceProcessor getAudioOutput() throws IOException {
        return new MbrolaAudioOutput();
    }

    /**
     * Get a resource for this voice.  Resources for this voice are located in
     * the package <code>com.sun.speech.freetts.en.us</code>.
     */
    protected URL getResource(String resource) {
        return com.sun.speech.freetts.en.us.CMUVoice.class.
            getResource(resource);
    }

    /**
     * Converts this object to a string
     * 
     * @return a string representation of this object
     */
    public String toString() {
	return "MbrolaVoice";
    }
}


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

import com.sun.speech.freetts.Validator;

import java.io.File;

/**
 * Shows this MbrolaVoice is valid (or usable). It tests for 
 * the following:
 * 
 * <ol>
 * <li> Check that the "mbrola.base" System property is defined,
 *      and that directory exists.
 * <li> Check that the $(mbrola.base)/mbrola binary exists.
 * <li> Check that the transition table exists. It is assumed 
 *      to be at $(mbrola.base)/us1/us1mrpa.
 * <li> Check that its voice database exists.
 * </ol>
 */
public class MbrolaVoiceValidator implements Validator {

    private MbrolaVoice mbrolaVoice;

    public MbrolaVoiceValidator(MbrolaVoice mbrolaVoice) {
        this.mbrolaVoice = mbrolaVoice;
    }

    /**
     * Returns true if the MbrolaVoice tested is valid.
     */
    public boolean isValid() {
        String mbrolaBase = System.getProperty("mbrola.base");
        File mbrolaBinary = new File(mbrolaVoice.getMbrolaBinary());
        File mbrolaRenameTable = new File(mbrolaVoice.getRenameTable());
        File mbrolaVoiceDB = new File(mbrolaVoice.getDatabase());

        boolean valid = true;

        if (mbrolaBase == null || mbrolaBase.length() == 0) {
            System.err.println
                (toString() +
                 ": System property \"mbrola.base\" is undefined." +
                 "You might need to set the MBROLA_DIR environment variable.");
            valid = false;
        }
        if (!mbrolaBinary.exists()) {
            System.err.println(toString() + ": MBROLA binary does not exist");
            valid = false;
        }
        if (!mbrolaRenameTable.exists()) {
            System.err.println(toString() +
                               ": MBROLA rename table does not exist");
            valid = false;
        }
        if (!mbrolaVoiceDB.exists()) {
            System.err.println(toString() + ": voice database does not exist");
            valid = false;
        }
        if (!valid) {
            System.err.println(toString() + ": " + mbrolaVoice.toString() +
                               " is invalid");
        }
        return valid;
    }

    /**
     * Returns the name of this validator.
     *
     * @return the name of this Validator
     */
    public String toString() {
        return (mbrolaVoice.toString() + "Validator");
    }
}


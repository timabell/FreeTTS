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

import java.io.File;

/**
 * Validates that MbrolaVoiceUS2 is installed and available.
 */
public class MbrolaVoiceUS2Validator extends MbrolaVoiceValidator {

    public MbrolaVoiceUS2Validator() {
        super((new MbrolaVoiceUS2()));
    }
}

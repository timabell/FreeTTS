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
 * Implementors of this interface can be validated via the isValid
 * method.
 */
public interface Validator {

    /**
     * Returns true if the condition tested is valid.
     *
     * @return <code> true </code> if the condition tested is valid
     */
    public boolean isValid();
}

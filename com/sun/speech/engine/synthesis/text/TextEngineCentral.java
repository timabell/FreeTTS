/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package com.sun.speech.engine.synthesis.text;

import javax.speech.EngineCentral;
import javax.speech.EngineModeDesc;
import javax.speech.EngineList;

/**
 * Supports the JSAPI 1.0 <code>EngineCentral</code> interface for a
 * synthesizer that sends text to standard out.  Place the following
 * line into the <code>speech.properties</code> file so that a
 * <code>TextSynthesizer</code> can be created.
 *
 * <pre>
 * TextSynthEngineCentral=com.sun.speech.engine.synthesis.text.TextEngineCentral
 * </pre>
 */
public class TextEngineCentral implements EngineCentral {
    /**
     * Simple mode.
     */
    static private TextSynthesizerModeDesc textModeDesc = 
            new TextSynthesizerModeDesc();

    /**
     * Returns a list containing a single reference to a
     * <code>TextSynthesizerModeDesc</code>
     * if the required features match those of the
     * <code>TextSynthesizer</code>.
     *
     * @param require the required characteristics; <code>null</code>
     *   always matches       
     */
    public EngineList createEngineList(EngineModeDesc require) {
        if (require == null || textModeDesc.match(require)) {
            EngineList el = new EngineList();
            el.addElement(textModeDesc);
            return el;
        }
        return null;
    }
}

/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */

/**
 * Defines constants which represent different types of <code>Playable</code>
 * objects, which can be played by the <code>Player</code>.
 */
public class PlayableType {

    private String typeName;

    /**
     * The ASCII text type.
     */
    public static final PlayableType TEXT = new PlayableType("text");

    /**
     * The ASCII text file type.
     */
    public static final PlayableType TEXT_FILE = new PlayableType("text file");
        
    /**
     * The JSML file type.
     */
    public static final PlayableType JSML_FILE = new PlayableType("JSML file");

    /**
     * The JSML text type.
     */
    public static final PlayableType JSML = new PlayableType("JSML");

    /**
     * The URL type.
     */
    public static final PlayableType URL = new PlayableType("URL");
       
     
    /**
     * Constructs a PlayableType with the given name.
     *
     * @param typeName the PlayableType name
     */
    private PlayableType(String typeName) {
	this.typeName = typeName;
    }


    /**
     * Returns the name of the type.
     *
     * @return the name of the PlayableType
     */
    public String toString() {
	return typeName;
    }
}

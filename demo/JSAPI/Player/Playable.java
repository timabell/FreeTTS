/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
import java.io.File;

/**
 * An object that can be played by the Player. Contains type information,
 * and stores actual object that will be played.
 */
public class Playable {

    private PlayableType type;
    private Object data = null;
    private String name;
    
    /**
     * Creates a JSML text Playable object with the given text.
     *
     * @param jsmlText the JSML text of the Playable object
     *
     * @return a JSML text Playable object 
     */
    public static Playable createJSMLPlayable(String jsmlText) {
	return new Playable(PlayableType.JSML, jsmlText, jsmlText);
    }

    /**
     * Creates a JSML file Playable object with the given File.
     *
     * @param jsmlFile the JSML file
     *
     * @return a JSML file Playable object
     */
    public static Playable createJSMLFilePlayable(File jsmlFile) {
	return new Playable
	    (PlayableType.JSML_FILE, jsmlFile, jsmlFile.getName());
    }
    
    /**
     * Creates an ASCII text Playable object with the given text.
     *
     * @param text the ASCII text
     *
     * @return an ASCII text JSML file Playable object
     */
    public static Playable createTextPlayable(String text) {
	return new Playable(PlayableType.TEXT, text, text);
    }

    /**
     * Creates an ASCII file Playable object with the given text file.
     *
     * @param textFile the ASCII text file
     *
     * @return an ASCII text file Playable object
     */
    public static Playable createTextFilePlayable(File textFile) {
	return new Playable
	    (PlayableType.TEXT_FILE, textFile, textFile.getName());
    }

    /**
     * Creates a URL Playable object with the given URL.
     *
     * @param url the URL
     *
     * @return a URL Playable object
     */
    public static Playable createURLPlayable(String url) {
	return new Playable(PlayableType.URL, url, url.toString());
    }
    
    
    /**
     * Constructs a Playable object of the given type and data.
     *
     * @param type the Playable type
     * @param data the object containing the Playable data
     */
    private Playable(PlayableType type, Object data, String name) {
	this.type = type;
	this.data = data;
	this.name = name;
    }

    /**
     * Returns the Playable type.
     *
     * @return the PlayableType
     */
    public PlayableType getType() {
	return type;
    }
    
    /**
     * Returns the File corresponding to this Playable.
     * 
     * @return the Playable File
     */
    public File getFile() {
	if (type == PlayableType.TEXT_FILE ||
	    type == PlayableType.JSML_FILE) {
	    return (File) data;
	} else {
	    return null;
	}
    }

    /**
     * Returns the text corresponding to this Playable.
     *
     * @return the Playable text
     */
    public String getText() {
	if (type == PlayableType.JSML || type == PlayableType.TEXT) {
	    return (String) data;
	} else {
	    return null;
	}
    }

    /**
     * Returns the name of this Playable
     *
     * @return the name of this Playable
     */
    public String getName() {
	return name;
    }

    /**
     * Returns a String describing the type and name of this Playable, e.g.,
     * <p><code>[JSML file] example1.jsml</code>
     *
     * @return the type and name of this Playable
     */
    public String toString() {
	String typeName = "[" + type.toString() + "] ";
	return typeName + name;
    }
    
}

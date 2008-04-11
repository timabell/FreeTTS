/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package com.sun.speech.engine.synthesis.text;

import javax.speech.Engine;
import javax.speech.synthesis.Speakable;
import javax.speech.synthesis.SpeakableEvent;

import com.sun.speech.engine.synthesis.BaseSynthesizer;
import com.sun.speech.engine.synthesis.BaseSynthesizerQueueItem;

import java.net.URL;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Represents an object on the speech output queue of a
 * <code>TextSynthesizer</code>.
 */
public class TextSynthesizerQueueItem extends BaseSynthesizerQueueItem {
    static public final String JSML = "jsml";
    static public final String[] JSML_ATTRIBUTES = {
        "lang", "mark"
    };

    static public final String DIV = "div";
    static public final String[] DIV_ATTRIBUTES = {
        "type", "mark"
    };
    
    static public final String VOICE = "voice";
    static public final String[] VOICE_ATTRIBUTES = {
        "voice", "gender", "age", "variant", "name", "mark"
    };
    
    static public final String SAYAS = "sayas";
    static public final String[] SAYAS_ATTRIBUTES = {
        "class", "mark"
    };

    static public final String PHONEME = "phoneme";
    static public final String[] PHONEME_ATTRIBUTES = {
        "original", "mark"
    };

    static public final String EMPHASIS = "emphasis";
    static public final String[] EMPHASIS_ATTRIBUTES = {
        "level", "mark"
    };

    static public final String BREAK = "break";
    static public final String[] BREAK_ATTRIBUTES = {
        "size", "time", "mark"
    };

    static public final String PROSODY = "prosody";
    static public final String[] PROSODY_ATTRIBUTES = {
        "rate", "volume", "pitch", "range", "mark"
    };

    static public final String MARKER = "marker";
    static public final String[] MARKER_ATTRIBUTES = {
        "mark"
    };
    
    static public final String ENGINE = "engine";
    static public final String[] ENGINE_ATTRIBUTES = {
        "name", "data", "mark"
    };

    static public final String[] ELEMENTS = {
        JSML,
        DIV,
        VOICE,
        SAYAS,
        PHONEME,
        EMPHASIS,
        BREAK,
        PROSODY,
        MARKER,
        ENGINE
    };

    static public final String[][] ELEMENT_ATTRIBUTES = {
        JSML_ATTRIBUTES,
        DIV_ATTRIBUTES,
        VOICE_ATTRIBUTES,
        SAYAS_ATTRIBUTES,
        PHONEME_ATTRIBUTES,
        EMPHASIS_ATTRIBUTES,
        BREAK_ATTRIBUTES,
        PROSODY_ATTRIBUTES,
        MARKER_ATTRIBUTES,
        ENGINE_ATTRIBUTES
    };

    
    /*
     * Commands to be encoded in the text.
     */
    static public final String COMMAND_PREFIX = "/";
    static public final String COMMAND_SUFFIX = "/";
    static public final String DATA_PREFIX = "[";
    static public final String DATA_SUFFIX = "]";
    static public final String ELEMENT_START = "start";
    static public final String ELEMENT_END = "end";
    
    /**
     * Class constructor.
     */
    public TextSynthesizerQueueItem() {
        super();
    }

    /**
     * Gets the type of this queue item.
     *
     * @return a <code>String</code> for debug purposes
     */
    public String getTypeString() {
        if (isPlainText()) {
            return "Plain-text String";
        } else if (getSource() instanceof String) {
            return "JSML from String";
        } else if (getSource() instanceof Speakable) {
            return "JSML from Speakable";
        } else if (getSource() instanceof URL) {
            return "JSML from URL";
        } else {
            return "Unknown Output";
        }
    }

    /**
     * Appends the text for this node to the given StringBuffer.
     *
     * @param n the node to traverse in depth-first order
     * @param buf the buffer to append text to
     */
    protected void linearize(Node n, StringBuffer buf) {
        StringBuffer endText = processNode(n, buf);
        for (Node child = n.getFirstChild();
             child != null;
             child = child.getNextSibling()) {
            linearize(child, buf);
        }

        if (endText != null) {
            buf.append(endText);
        }
    }

    /**
     * Adds text for just this node, and returns any text that might
     * be needed to undo the effects of this node after it is
     * processed.
     *
     * @param n the node to traverse in depth-first order
     * @param buf the buffer to append text to
     *
     * @return a <code>String</code> containing text to undo the
     *   effects of the node
     */
    protected StringBuffer processNode(Node n, StringBuffer buf) {
        StringBuffer endText = null;
        
        int type = n.getNodeType();
        switch (type) {
            case Node.ATTRIBUTE_NODE:
                 break;
                 
            case Node.DOCUMENT_NODE:
                break;
                
            case Node.ELEMENT_NODE:
                endText = processElement((Element) n, buf);
                break;
                
            case Node.TEXT_NODE:
                buf.append(((Text) n).getData());
                break;

            // Pass processing instructions (e.g., <?blah?>
            // right on to the synthesizer.  These types of things
            // probably should not be used.  Instead the 'engine'
            // element is probably the best thing to do.
            //
            case Node.PROCESSING_INSTRUCTION_NODE:
                break;
                
            // The document type had better be JSML.
            //
            case Node.DOCUMENT_TYPE_NODE:
                break;

            // I think NOTATION nodes are only DTD's.
            //
            case Node.NOTATION_NODE:
                break;

            // Should not get COMMENTS because the JSMLParser
            // ignores them.
            //
            case Node.COMMENT_NODE:
                break;

            // Should not get CDATA because the JSMLParser is
            // coalescing.
            //    
            case Node.CDATA_SECTION_NODE:
                break;

            // Should not get ENTITY related notes because
            // entities are expanded by the JSMLParser
            //
            case Node.ENTITY_NODE:
            case Node.ENTITY_REFERENCE_NODE:
                break;

            // Should not get DOCUMENT_FRAGMENT nodes because I
            // [[[WDW]]] think they are only created via the API's
            // and cannot be defined via content.
            //
            case Node.DOCUMENT_FRAGMENT_NODE:
                break;

            default:
                break;
        }
        
        return endText;
    }

    /**
     * Adds any commands for this element and returns any text that might
     * be needed to undo the effects of this element after it is processed.
     *
     * @param element the element to traverse in depth-first order
     * @param buf the buffer to append text to
     *
     * @return a <code>String</code> containing text to undo the
     *   effects of the element
     */
    protected StringBuffer processElement(Element element, StringBuffer buf) {
        StringBuffer endText;
        StringBuffer attributeText = null;
        
        String elementName = element.getTagName();
        for (int i = 0; i < ELEMENTS.length; i++) {
            if (ELEMENTS[i].equals(elementName)) {
                attributeText = processAttributes(
                    element, ELEMENT_ATTRIBUTES[i]);
                break;
            }
        }

        buf.append(COMMAND_PREFIX + elementName + " " + ELEMENT_START);
        if (attributeText != null) {
            buf.append(attributeText);
        }
        buf.append(COMMAND_SUFFIX);

        endText = new StringBuffer(
            COMMAND_PREFIX + elementName + " " + ELEMENT_END);
        if (attributeText != null) {
            endText.append(attributeText);
        }
        endText.append(COMMAND_SUFFIX);

        return endText;
    }

    /**
     * Gets the list of attributes of the element and returns them in
     * a <code>StringBuffer</code>.
     *
     * @param element the element containing attributes (if any)
     * @param attributes the allowed attributes for
     *   <code>element</code>
     *
     * @return a buffer containing the attributes in text form
     */
    protected StringBuffer processAttributes(Element element,
                                             String[] attributes) {
        StringBuffer attributeText = new StringBuffer();
        for (int i = 0; i < attributes.length; i++) {
            if (element.hasAttribute(attributes[i])) {
                String data = element.getAttribute(attributes[i]);
                attributeText.append(
                    DATA_PREFIX + attributes[i] + "=" + data + DATA_SUFFIX);
            }
        }
        return attributeText;
    }
       
    /**
     * Gets the text form of this queue item.
     *
     * @return the text form of this queue item.
     */
    public String getEngineText() {
        if (isPlainText()) {
	    return text;
        } else {
            StringBuffer textBuffer = new StringBuffer();
            Document document = getDocument();
            linearize(document, textBuffer);
            return(textBuffer.toString());
        }
    }
}

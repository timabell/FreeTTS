/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
import java.io.File;
import java.util.Locale;

import javax.speech.Central;
import javax.speech.Engine;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.SynthesizerProperties;

/**
 * Simple program showing how to use FreeTTS in JSAPI.
 */
public class HelloWorld {
    
    public static void main(String[] argv) {
	try {
	    // Create a new SynthesizerModeDesc that will match the FreeTTS
	    // Synthesizer.
	    SynthesizerModeDesc desc = new SynthesizerModeDesc
		("Unlimited domain FreeTTS Speech Synthesizer from Sun Labs",
		 null,
		 Locale.US,
		 Boolean.FALSE,         // running?
		 null);                 // voice

	    Synthesizer synthesizer = Central.createSynthesizer(desc);

	    if (synthesizer == null) {
		String message = "Can't find synthesizer.\n" +
		    "Make sure that there is a \"speech.properties\" file " +
		    "at either of these locations: \n";
		message += "user.home    : " + 
		    System.getProperty("user.home") + "\n";
		message += "java.home/lib: " + System.getProperty("java.home")
		    + File.separator + "lib\n";
		
		System.err.println(message);
		System.exit(1);
	    }
	    
	    // get it ready to speak
	    synthesizer.allocate();
	    synthesizer.resume();
	    
	    // speak the "Hello world" string
	    synthesizer.speakPlainText("Hello, world!", null);
	    
	    // wait till speaking is done
	    synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
	    
	    // clean up
	    synthesizer.deallocate();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}

	System.exit(0);
    }
}

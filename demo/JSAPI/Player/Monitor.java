/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
import java.awt.Component;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.speech.synthesis.Synthesizer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import javax.swing.border.TitledBorder;

import com.sun.speech.engine.synthesis.SynthesizerMonitor;
import com.sun.speech.engine.EngineEventPanel;

/**
 * Implements the GUI for a SynthesizerMonitor.
 */
public class Monitor extends JPanel {
    
    private SynthesizerMonitor monitor;
    private Synthesizer synthesizer;
    private int width = 600;
    private int height = 300;


    /**
     * Constructs a Monitor for the given synthesizer
     *
     * @param synthesizer the synthesizer it monitors
     * @param synthesizerName name of the synthesizer
     */
    public Monitor(Synthesizer synthesizer, String synthesizerName) {
	TitledBorder titledBorder = new TitledBorder(synthesizerName);
	setBorder(titledBorder);
	setPreferredSize(new Dimension(width, height));
	setSize(width, height);
	this.synthesizer = synthesizer;
	this.monitor = new SynthesizerMonitor(synthesizer);
	createMonitorPanel();
    }


    /**
     * Populates this Monitor JPanel.
     */
    private void createMonitorPanel() {
	GridBagLayout gridbag = new GridBagLayout();
	setLayout(gridbag);

	GridBagConstraints c = new GridBagConstraints();
	c.anchor = GridBagConstraints.WEST;
	c.insets = new Insets(4,4,4,4);
	c.gridx = 0;
	c.gridy = 0;        
	c.weightx = 1.0;
	c.fill = GridBagConstraints.BOTH;

	Component sp = monitor.getStatePanel();
	gridbag.setConstraints(sp, c);
	add(sp);

	EngineEventPanel eventPanel = 
	    (EngineEventPanel) monitor.getEventPanel();

	c.gridy = 1;
	c.weighty = 1.0;
	gridbag.setConstraints(eventPanel, c);
	add(eventPanel);
	validate();
    }
}

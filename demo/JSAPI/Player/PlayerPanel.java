/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * Defines and contains all the user-interface Swing objects of the Player.
 * In terms of the model-view-controller (MVC) architecture, this implements
 * the "view" and "control" elements. Its interacts with the PlayerModel 
 * class, the "model" element.
 */
public class PlayerPanel extends JPanel {

    private PlayerModel playerModel;
    
    private int width = 600;
    private int height = 450;
    private int border = 30;

    private Color backgroundColor = Color.LIGHT_GRAY;
    private Color foregroundColor = Color.BLACK;
    private Color controlColor = new Color(250, 250, 250);
                            
    private JComboBox synthesizerComboBox;
    private JComboBox waveSynthesisComboBox;
    private JComboBox voiceComboBox;
        
    private JTextArea speakingTextArea;
    private JList speakablesList;
    private int speakablesListVisibleRows = 5;
    
    private JToggleButton pauseButton;
    private JButton playButton;
    private JButton cancelButton;
    private JButton stopButton;
    private JButton deleteButton;
    
    private int initialVolume = 10;
    private JSlider volumeSlider;
    private JSlider speedSlider;
    private JSlider pitchSlider;
    private JSlider rangeSlider;
    
    private JButton fileButton;
    private JTextArea textArea;
    private int textAreaRows = 2;
    private int textAreaColumns = 20;
    private JButton clearTextButton;
    private JButton speakTextButton;
    private JButton speakJSMLButton;

    private static char cancelMnemonic = 'A';
    private static char clearMnemonic = 'C';
    private static char deleteMnemonic = 'D';
    private static char pauseMnemonic = 'U';
    private static char pitchMnemonic = 'H';
    private static char playMnemonic = 'P';
    private static char playListMnemonic = 'L';
    private static char rangeMnemonic = 'R';
    private static char resumeMnemonic = 'E';
    private static char stopMnemonic = 'T';
    private static char speakMnemonic = 'S';
    private static char speakJSMLMnemonic = 'J';
    private static char synthesizerMnemonic = 'Y';
    private static char textMnemonic = 'X';
    private static char voiceMnemonic = 'O';
    private static char volumeMnemonic = 'V';
    private static char wordsPerMinMnemonic = 'W';
    
    
    /**
     * Constructs a PlayerPanel that interacts with the given PlayerModel.
     *
     * @param playerModel the PlayerModel that this PlayerPanel interacts
     *   with.
     */    
    public PlayerPanel(PlayerModel playerModel) {

	this.playerModel = playerModel;
	
	setSize(width, height);
	setAlignmentY((float) 0.5);
	setAlignmentX((float) 0.5);
	
	setLayout(new BorderLayout());
	add(createMainPanel(), BorderLayout.NORTH);
	add(createTextPanel(), BorderLayout.CENTER);	
    }


    /**
     * Creates the main JPanel that is the upper JPanel of the
     * user interface. It contains:
     * <ol>
     * <li> (on the left) volume and speaking rate control slides
     * <li> (center) the synthesizer/voice selection combo boxes,
     * the play list, and the buttons
     * <li> (on the right) pitch and range control slides
     * </ol>
     *
     * @return the upper JPanel of the application with all the controls
     *    and play list 
     */
    private JPanel createMainPanel() {
	JPanel centerPanel = new JPanel();

	centerPanel.setLayout(new BorderLayout());
	centerPanel.add(createLeftSliderPanel(), BorderLayout.WEST);
	centerPanel.add(createSpeakablesPanel(), BorderLayout.CENTER);
	centerPanel.add(createRightSliderPanel(), BorderLayout.EAST);
	
	return centerPanel;
    }


    /**
     * Creates the TitledBordered JPanel and the play list it contains.
     * This Panel is at the center of the Main Panel. The play list is
     * contained within a JScrollPane.
     *
     * @return a JPanel that containts the play list
     */
    private JPanel createSpeakablesPanel() {
	
       	ListModel playList = playerModel.getPlayList();
	speakablesList = new JList(playList);
	speakablesList.setVisibleRowCount(speakablesListVisibleRows);
	speakablesList.setSelectionMode
	    (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	speakablesList.setSelectedIndex(0);
	speakablesList.setDragEnabled(true);
	
	JLabel listTitle = new JLabel("Play List");
	listTitle.setDisplayedMnemonic(playListMnemonic);
	listTitle.setLabelFor(speakablesList);
	
	JScrollPane scrollPane = new JScrollPane(speakablesList);
	scrollPane.add(listTitle);

	JPanel centerPanel = new JPanel(new BorderLayout());
	centerPanel.add(listTitle, BorderLayout.NORTH);
	centerPanel.add(scrollPane, BorderLayout.CENTER);
	centerPanel.add(createControlsPanel(), BorderLayout.SOUTH);
			
	TitledBorder titledBorder = new TitledBorder("");
	titledBorder.setTitleColor(foregroundColor);
	titledBorder.setTitleJustification(TitledBorder.CENTER);
	titledBorder.setBorder(new EtchedBorder(Color.WHITE, Color.BLACK));
	centerPanel.setBorder(titledBorder);
			
	JPanel speakablesPanel = new JPanel(new BorderLayout());
	speakablesPanel.add(createSettingsPanel(), BorderLayout.NORTH);
	speakablesPanel.add(centerPanel, BorderLayout.CENTER);
	
	return speakablesPanel;
    }


    /**
     * Creates the settings JPanel and the synthesizer and voice
     * JComboBoxes it contains. This JPanel is inside the speakables Panel.
     *
     * @return the JPanel of synthesizer and voice JComboBoxes
     */
    private JPanel createSettingsPanel() {
	synthesizerComboBox = createComboBox
	    ((ComboBoxModel) playerModel.getSynthesizerList(),
	     "Synthesizer", "FreeTTS Synthesizer");
			
	voiceComboBox = createComboBox
	    ((ComboBoxModel) playerModel.getVoiceList(),
	     "Voice", "Voice");
	
	JLabel synthesizerLabel = new JLabel("Synthesizer:");
	synthesizerLabel.setDisplayedMnemonic(synthesizerMnemonic);
	synthesizerLabel.setLabelFor(synthesizerComboBox);

	JLabel voiceLabel = new JLabel("Voice:");
	voiceLabel.setDisplayedMnemonic(voiceMnemonic);
	voiceLabel.setLabelFor(voiceComboBox);
	
	JPanel leftPanel = new JPanel(new BorderLayout());
	leftPanel.add(synthesizerLabel, BorderLayout.NORTH);
	leftPanel.add(synthesizerComboBox, BorderLayout.CENTER);

	JPanel rightPanel = new JPanel(new BorderLayout());
	rightPanel.add(voiceLabel, BorderLayout.NORTH);
	rightPanel.add(voiceComboBox, BorderLayout.CENTER);

	JPanel settingsPanel = new JPanel();
	
	FlowLayout flowLayout = new FlowLayout();
	flowLayout.setAlignment(FlowLayout.CENTER);
	settingsPanel.setLayout(flowLayout);
	settingsPanel.add(leftPanel);
	settingsPanel.add(rightPanel);
		
	addComboBoxListeners();
		
	return settingsPanel;
    }


    /**
     * Creates a non-editable ComboBox with the given attributes.
     *
     * @param model the ComboBoxModel this ComboBox is based on
     * @param toolTipText the tooltip text
     * @param prototypeDisplayValue the String used to calculate the
     *    width of the ComboBox
     */
    public JComboBox createComboBox(ComboBoxModel model, String toolTipText,
				    String prototypeDisplayValue) {
	JComboBox comboBox = new JComboBox(model);
	comboBox.setToolTipText(toolTipText);
	comboBox.setPrototypeDisplayValue(prototypeDisplayValue);
	comboBox.setEditable(false);
	return comboBox;
    }


    /**
     * Adds listeners for the synthesizer and voices JComboBoxes
     */
    private void addComboBoxListeners() {
	synthesizerComboBox.addActionListener(new ActionListener() 
	    {
		public void actionPerformed(ActionEvent e) {
		    int selectedIndex = synthesizerComboBox.getSelectedIndex();
		    Monitor monitor = playerModel.getMonitor();
		    if (monitor != playerModel.getMonitor(selectedIndex)) {
			if (monitor != null) {
			    monitor.setVisible(false);
			}
			if (playerModel.isMonitorVisible()) {
			    monitor = playerModel.getMonitor(selectedIndex);
			    monitor.setVisible(true);
			    add(monitor, BorderLayout.SOUTH);
			}
			playerModel.setSynthesizer(selectedIndex);
		    }
		}
	    });
	voiceComboBox.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    Cursor oldCursor = getCursor();
		    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		    playerModel.setVoice(voiceComboBox.getSelectedIndex());
		    setCursor(oldCursor);
		    updateSliders();
		}
	    });
    }


    /**
     * Creates the JPanel and the buttons it contains.
     *
     * @return a JPanel that contains the buttons. 
     */
    private JPanel createControlsPanel() {		

	// create the buttons
	playButton = createJButton("Play", playMnemonic);
	cancelButton = createJButton("Cancel", cancelMnemonic);
	stopButton = createJButton("Stop", stopMnemonic);

	pauseButton = new JToggleButton("Pause");
	pauseButton.setToolTipText("Pause");
	pauseButton.setMnemonic(pauseMnemonic);
	setControlColors(pauseButton);

	JPanel buttonsPanel = new JPanel();
	buttonsPanel.add(pauseButton);
	buttonsPanel.add(playButton);
	buttonsPanel.add(cancelButton);
	buttonsPanel.add(stopButton);
		
	JPanel controlsPanel = new JPanel(new BorderLayout());
	controlsPanel.add(buttonsPanel, BorderLayout.CENTER);
	
	addControlsPanelActionListeners();

	return controlsPanel;
    }


    /**
     * Creates a JButton with the given label, which is also the tooltip
     * text, and the given mnemonic.
     *
     * @param label the button label, which is also the tooltip text
     * @param mnemonic mnemonic for the button
     *
     * @return a JButton
     */
    private JButton createJButton(String label, int mnemonic) {
	JButton button = new JButton(label);
	button.setToolTipText(label);
	button.setMnemonic(mnemonic);
	setControlColors(button);
	return button;
    }



    /**
     * Creates the JPanel where the volume and speaking rate JSliders are.
     */
    private JPanel createLeftSliderPanel() {
	// create the sliders
	playerModel.setVolume(initialVolume);

	volumeSlider = new JSlider(JSlider.VERTICAL, 0, 10, initialVolume);
	int speakingRate = (int) playerModel.getSpeakingRate();
	if (speakingRate == -1) {
	    speakingRate = 0;
	}
	speedSlider = new JSlider(JSlider.VERTICAL, 0, 400, 0);
		
	JPanel volumePanel = createSliderPanel
	    (volumeSlider, "Volume Control", 1, 5,
	     "Volume", volumeMnemonic);
	JPanel speedPanel = createSliderPanel
	    (speedSlider, "Speed Control", 50, 100,
	     "Words/min", wordsPerMinMnemonic);
		
	JPanel sliderPanel = new JPanel(new FlowLayout());
	sliderPanel.add(volumePanel);
	sliderPanel.add(speedPanel);
	addLeftSliderPanelListeners();
	
	return sliderPanel;
    }


    /**
     * Creates the JPanel where the pitch and range JSliders are.
     */
    private JPanel createRightSliderPanel() {
	// create the sliders
	pitchSlider = new JSlider(JSlider.VERTICAL, 50, 200, 50);
	rangeSlider = new JSlider(JSlider.VERTICAL, 0, 50, 0);

	JPanel pitchPanel = createSliderPanel
	    (pitchSlider, "Pitch Control", 25, 50, "Pitch/Hz", pitchMnemonic);
	JPanel rangePanel = createSliderPanel
	    (rangeSlider, "Range Control", 5, 10, "Range", rangeMnemonic);
	
	JPanel sliderPanel = new JPanel();
	sliderPanel.setLayout(new FlowLayout());
	sliderPanel.add(pitchPanel);
	sliderPanel.add(rangePanel);
	
	addRightSliderPanelListeners();
	
	return sliderPanel;
    }


    /**
     * Adds ActionListeners for the JSliders.
     */
    private void addLeftSliderPanelListeners() {
	volumeSlider.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent ce) {
		    if (playerModel.setVolume
			((float) volumeSlider.getValue()) == false) {
			volumeSlider.setValue((int) playerModel.getVolume());
		    }
		}
	    });
	speedSlider.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent ce) {
		    if (playerModel.setSpeakingRate
			((float) speedSlider.getValue()) == false) {
			speedSlider.setValue((int) playerModel.getSpeakingRate());
		    }
		}
	    });
    }


    /**
     * Adds ActionListeners for the JSliders.
     */
    private void addRightSliderPanelListeners() {
	pitchSlider.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
		    if (playerModel.setPitch
			((float) pitchSlider.getValue()) == false) {
			pitchSlider.setValue((int) playerModel.getPitch());
		    }
		}
	    });
	rangeSlider.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
		    if (playerModel.setRange
			((float) rangeSlider.getValue()) == false) {
			rangeSlider.setValue((int) playerModel.getRange());
		    }
		}
	    });
    }


    /**
     * Updates all the Sliders with values from the PlayerModel.
     */
    private void updateSliders() {
	int volume = (int) playerModel.getVolume();
	if (volume > -1) {
	    volumeSlider.setValue(volume);
	}
	int rate = (int) playerModel.getSpeakingRate();
	if (rate > -1) {
	    speedSlider.setValue(rate);
	}
	int pitch = (int) playerModel.getPitch();
	if (pitch > -1) {
	    pitchSlider.setValue(pitch);
	}
	int range = (int) playerModel.getRange();
	if (range > -1) {
	    rangeSlider.setValue(range);
	}
    }
    

    /**
     * Creates a JPanel that contains the given JSlider, with the
     * given attributes.
     *
     * @param slider the JSlider
     * @param toolTipText the text for the tooltip
     * @param minorTickSpacing the spacing between minor ticks
     * @param majorTickSpacing the spacing between major ticks
     * @param title the title of the JSlider
     */
    private JPanel createSliderPanel(JSlider slider, String toolTipText,
				     int minorTickSpacing,
				     int majorTickSpacing,
				     String title, char mnemonic) {
	JPanel sliderPanel = new JPanel(new BorderLayout());
		
	slider.setSize(getSize().width/2 - border,
		       slider.getSize().height);
	
	slider.putClientProperty("JSlider.isFilled", Boolean.TRUE);
	slider.setMinorTickSpacing(minorTickSpacing);
	slider.setMajorTickSpacing(majorTickSpacing);
	slider.setPaintTicks(true);
	slider.setPaintLabels(true);
	slider.setToolTipText(toolTipText);
	
	JLabel leftLabel = new JLabel(title);
	leftLabel.setForeground(foregroundColor);
	leftLabel.setDisplayedMnemonic(mnemonic);
	leftLabel.setLabelFor(slider);
	
	sliderPanel.add(leftLabel, BorderLayout.NORTH);
	sliderPanel.add(slider, BorderLayout.CENTER);
		
	return sliderPanel;
    }    


    /**
     * Adds ActionListeners to all the buttons
     */
    private void addControlsPanelActionListeners() {
	playButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    int[] selectedIndices =
			speakablesList.getSelectedIndices();
		    for (int i = 0; i < selectedIndices.length; i++) {
			if (selectedIndices[i] != -1) {
			    playerModel.play(selectedIndices[i]);
			}
		    }
		}
	    });
	pauseButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (playerModel.isPaused()) {
			playerModel.resume();
		    } else {
			playerModel.pause();
		    }
		}
	    });
	cancelButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    playerModel.cancel();
		}
	    });
	stopButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    playerModel.stop();
		    pauseButton.setEnabled(true);
		}
	    });
    }


    /**
     * Creates the text JPanel is where the user enters text to be read.
     *
     * @return the JPanel with the textArea for text input
     */
    private JPanel createTextPanel() {
	textArea = new JTextArea();
	textArea.requestFocusInWindow();
	textArea.setLineWrap(true);
	textArea.setWrapStyleWord(true);

	JScrollPane textScrollPane = new JScrollPane(textArea);

	speakTextButton = createJButton("Speak Text", speakMnemonic);
	speakJSMLButton = createJButton("Speak JSML", speakJSMLMnemonic);
	clearTextButton = createJButton("Clear", clearMnemonic);
					
	BorderLayout borderLayout = new BorderLayout();
	JPanel textPanel = new JPanel(borderLayout);
	textPanel.setSize(width - border * 2, textPanel.getSize().height);

	JPanel buttonsPanel = new JPanel();
	buttonsPanel.add(speakTextButton);
	buttonsPanel.add(speakJSMLButton);
	buttonsPanel.add(clearTextButton);
	
	TitledBorder titledBorder = new TitledBorder("Enter text:");
	JLabel titleLabel = new JLabel("Enter text:");
	titleLabel.setDisplayedMnemonic(textMnemonic);
	titleLabel.setLabelFor(textArea);
		
	EtchedBorder border = new EtchedBorder(Color.WHITE, Color.BLACK);
	textPanel.setBorder(border);
	textPanel.add(titleLabel, BorderLayout.NORTH);
	textPanel.add(textScrollPane, BorderLayout.CENTER);
	textPanel.add(buttonsPanel, BorderLayout.SOUTH);

	addTextPanelActionListeners();
		
	return textPanel;
    }


    /**
     * Adds ActionListeners to the buttons on the text panel.
     */
    private void addTextPanelActionListeners() {
	clearTextButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    textArea.setText("");
		}
	    });
	speakTextButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    String inputText = textArea.getText();
		    if (inputText.length() > 0) {
			Playable textPlayable =
			    Playable.createTextPlayable(inputText);
			playerModel.addPlayable(textPlayable);
			speakablesList.setSelectedValue(textPlayable, true);
			playerModel.play(textPlayable);
		    }
		}
	    });
	speakJSMLButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    String inputText = textArea.getText();
		    if (inputText.length() > 0) {
			Playable jsmlPlayable =
			    Playable.createJSMLPlayable(inputText);
			playerModel.addPlayable(jsmlPlayable);
			speakablesList.setSelectedValue(jsmlPlayable, true);
			playerModel.play(jsmlPlayable);
		    }
		}
	    });
    }


    /**
     * Sets the given control JComponent to the application defined color.
     *
     * @param component the JComponent to set color
     */
    private void setControlColors(JComponent component) {
	component.setBackground(controlColor);
    }


    /**
     * Returns the Playables JList.
     *
     * @return the Playables JList
     */
    public JList getPlayList() {
	return speakablesList;
    }
    
}

/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;

import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.Voice;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Player extends JFrame {

    private PlayerModel playerModel;
    private PlayerPanel playerPanel;
    private PlayerMenuBar playerMenuBar;

    private Font globalFont;
    private String globalFontFace = "Arial";
    
            
    /**
     * Constructs a Player with the given title.
     *
     * @param title the title of the Player
     */
    public Player(String title) {
	super(title);

	setDefaultLookAndFeelDecorated(true);
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    playerModel.close();
		    System.exit(0);
		}
	    });

	playerModel = new PlayerModelImpl();
	playerPanel = new PlayerPanel(playerModel);
	setSize(playerPanel.getSize());
	getContentPane().add(playerPanel, BorderLayout.CENTER);
    }


    /**
     * Returns the view model of this Player application.
     *
     * @return the view model
     */
    public PlayerPanel getView() {
	return playerPanel;
    }
    

    /**
     * Returns the data model of this Player application.
     *
     * @return the data model
     */
    public PlayerModel getModel() {
	return playerModel;
    }
    

    /**
     * Sets the menubar for this Player.
     *
     * @param menubar the menubar
     */
    public void setMenuBar(PlayerMenuBar menubar) {
	playerMenuBar = menubar;
	setJMenuBar(playerMenuBar);
    }


    /**
     * Show/hide the current monitor from the applcation frame.
     *
     * @param show true to show, false to hide
     */
    public void showMonitor(boolean show) {
	Monitor monitor = playerModel.getMonitor();
	int newHeight = getSize().height;
	int monitorHeight = monitor.getSize().height;

	if (show) {
	    playerPanel.add(monitor, BorderLayout.SOUTH);
	    newHeight += monitorHeight;
	} else {
	    playerPanel.remove(monitor);
	    newHeight -= monitorHeight;
	}

	Dimension newSize = new Dimension(getSize().width, newHeight);
	playerPanel.setSize(newSize);
	setSize(newSize);

	playerModel.setMonitorVisible(show);
        repaint();
    }


    /**
     * Changes the font size of all components in this Player.
     * 
     * @param change the change in font size
     */
    public void setGlobalFontSize(int fontSize) {
	if (globalFont == null) {
	    globalFont = getFont();
	}
	globalFont = new Font(globalFontFace, Font.BOLD, fontSize);
		
	UIManager.put("Button.font", globalFont);
	UIManager.put("ComboBox.font", globalFont);
	UIManager.put("Label.font", globalFont);
	UIManager.put("List.font", globalFont);
	UIManager.put("Menu.font", globalFont);
	UIManager.put("MenuItem.font", globalFont);
	UIManager.put("TextArea.font", globalFont);
	UIManager.put("ToggleButton.font", globalFont);
	UIManager.put("ToolTip.font", globalFont);

	setFont(globalFont);
	
	SwingUtilities.updateComponentTreeUI(this);
	repaint();
    }


    /**
     * A convenience method for setting the Look & Feel.
     *
     * @param lookAndFeel the Look & Feel specification
     */
    public void setLookAndFeel(Object lookAndFeel) {
	try {
	    if (lookAndFeel instanceof String) {
		UIManager.setLookAndFeel((String) lookAndFeel);
	    } else if (lookAndFeel instanceof LookAndFeel) {
		UIManager.setLookAndFeel((LookAndFeel) lookAndFeel);
	    }

	    SwingUtilities.updateComponentTreeUI(this);
	    repaint();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    /**
     * Sets the voice for the Player.  The main purpose of this
     * method is to set the Player to a nice voice on startup.
     */
    public void setVoice(String voiceName) {
        PlayerModel model = getModel();
        ListModel descList = model.getSynthesizerList();
        for (int i = 0; i < descList.getSize(); i++) {
            SynthesizerModeDesc desc = (SynthesizerModeDesc)
                descList.getElementAt(i);
            Voice[] voices = desc.getVoices();
            for (int j = 0; j < voices.length; j++) {
                if (voices[j].getName().equals(voiceName)) {
                    model.setSynthesizer(i);
                    model.setVoice(j);
                    break;
                }
            }
        }
    }
    

    /**
     * The main() method of the Player.
     */
    public static void main(String[] args) throws Exception {
	boolean showMonitor = false;
        String firstVoice = "kevin16";
        
	Player player = new Player("FreeTTS Player");

	for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-voice")) {
                if (++i < args.length) {
		    firstVoice = args[i];
                }
            } else if (args[i].equals("-fontsize")) {
		if (++i < args.length) {
		    player.setGlobalFontSize(Integer.parseInt(args[i]));
		}
	    } else if (args[i].equals("-monitor")) {
		showMonitor = true;
	    } else if (args[i].equals("-input")) {

		PlayerModel model = player.getModel();

		for (i++; i < args.length; i++) {
		    int start = args[i].indexOf(':');
		    if (start > -1) {
			String content = args[i].substring(start+1);
			System.out.println(content);
			if (args[i].startsWith("plaintext:")) {
			    model.addPlayable
				(Playable.createTextPlayable(content));
			} else if (args[i].startsWith("textfile:")) {
			    model.addPlayable
				(Playable.createTextFilePlayable
				 (new File(content)));
			} else if (args[i].startsWith("jsmltext:")) {
			    model.addPlayable
				(Playable.createJSMLPlayable(content));
			} else if (args[i].startsWith("jsmlfile:")) {
			    model.addPlayable
				(Playable.createJSMLFilePlayable
				 (new File(content)));
			}
		    }
		}
	    }
        }

	player.setMenuBar(new PlayerMenuBar(player));
	player.getModel().createSynthesizers();
	player.setVoice(firstVoice);
        
	player.show();

	if (showMonitor) {
	    player.showMonitor(showMonitor);
	}
    }
}

/**
 * Implements the menubar of the Player application.
 */
class PlayerMenuBar extends JMenuBar {
    private Player player;
    private PlayerModel playerModel;

    private JFileChooser fileChooser;
    
    private JMenuItem fileSpeakJSMLMenuItem;
    private JMenuItem fileSpeakTextMenuItem;
    private JMenuItem fileSpeakURLMenuItem;
    private JMenuItem fileExitMenuItem;

    private JMenuItem styleLFCrossPlatformMenuItem;
    private JMenuItem styleLFSystemMenuItem;
    private JMenuItem styleFontSizeLargerMenuItem;
    private JMenuItem styleFontSizeSmallerMenuItem;

    private JMenuItem monitorHideMenuItem;
    private JMenuItem monitorShowMenuItem;
                
    private static char crossPlatformMnemonic = 'C';
    private static char exitMnemonic = 'X';
    private static char fileMnemonic = 'F';
    private static char hideMonitorMnemonic = 'H';
    private static char jsmlMnemonic = 'J';
    private static char monitorMnemonic = 'M';
    private static char showMonitorMnemonic = 'S';
    private static char speakMnemonic = 'S';
    private static char styleMnemonic = 'E';
    private static char styleLFMnemonic = 'L';
    private static char systemMnemonic = 'S';
    private static char textMnemonic = 'T';
    private static char urlMnemonic = 'U';


    /**
     * Constructs a menubar with the given Player data model.
     *
     * @param playerModel the Player data model
     */
    public PlayerMenuBar(Player player) {
	super();
	this.player = player;
	this.playerModel = player.getModel();
	this.fileChooser = new JFileChooser();
	fileChooser.setCurrentDirectory
	    (new File(System.getProperty("user.dir")));

	add(createFileMenu());
	add(createStyleMenu());
	add(createMonitorMenu());
    }


    /**
     * Creates the File menu.
     *
     * @return the File menu
     */
    private JMenu createFileMenu() {
	JMenu fileMenu = new JMenu("File");
	fileMenu.setMnemonic(fileMnemonic);
	add(fileMenu);
	
	JMenu fileSpeakMenu = new JMenu("Speak");
	fileSpeakMenu.setMnemonic(speakMnemonic);
	
	fileSpeakJSMLMenuItem = new JMenuItem("JSML File ...");
	fileSpeakTextMenuItem = new JMenuItem("Text File ...");
	fileSpeakURLMenuItem = new JMenuItem("URL ...");
	fileSpeakJSMLMenuItem.setMnemonic(jsmlMnemonic);
	fileSpeakTextMenuItem.setMnemonic(textMnemonic);
	fileSpeakURLMenuItem.setMnemonic(urlMnemonic);
		
	fileSpeakMenu.add(fileSpeakJSMLMenuItem);
	fileSpeakMenu.add(fileSpeakTextMenuItem);
	fileSpeakMenu.add(fileSpeakURLMenuItem);
				
	fileExitMenuItem = new JMenuItem("Exit");
	fileExitMenuItem.setMnemonic(exitMnemonic);
	
	fileMenu.add(fileSpeakMenu);
	fileMenu.addSeparator();
	fileMenu.add(fileExitMenuItem);
	
	addFileMenuListeners();
	
	return fileMenu;
    }


    /**
     * Adds the given Playaable to the play list, and plays it.
     */
    private void playPlayable(Playable playable) {
	playerModel.addPlayable(playable);
	player.getView().getPlayList().setSelectedValue(playable, true);
	playerModel.play(playable);
    }


    /**
     * Add listenrs to the file menu.
     */
    private void addFileMenuListeners() {
	fileExitMenuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    playerModel.close();
		    System.exit(0);
		}
	    });
	fileSpeakJSMLMenuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    File file = chooseFile();
		    if (file != null) {
			playPlayable(Playable.createJSMLFilePlayable(file));
		    }
		}
	    });
	fileSpeakTextMenuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    File file = chooseFile();
		    if (file != null) {
			playPlayable(Playable.createTextFilePlayable(file));
		    }
		}
	    });
	fileSpeakURLMenuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    String url = JOptionPane.showInputDialog
			(getParent(), "Enter URL:", "Speak URL",
			 JOptionPane.QUESTION_MESSAGE);
		    if (url != null && url.length() > 0) {
			playPlayable(Playable.createURLPlayable(url));
		    }
		}
	    });
    }


    /**
     * Creates the Style menu.
     *
     * @return the Style menu
     */
    private JMenu createStyleMenu() {
	JMenu styleMenu = new JMenu("Style");
	styleMenu.setMnemonic(styleMnemonic);
	
	JMenu styleLFMenu = new JMenu("Look & Feel");
	styleLFMenu.setMnemonic(styleLFMnemonic);
	
	styleLFCrossPlatformMenuItem = new JMenuItem("Cross Platform");
	styleLFCrossPlatformMenuItem.setMnemonic(crossPlatformMnemonic);
	
	styleLFSystemMenuItem = new JMenuItem("System");
	styleLFSystemMenuItem.setMnemonic(systemMnemonic);
	
	styleLFMenu.add(styleLFCrossPlatformMenuItem);
	styleLFMenu.add(styleLFSystemMenuItem);
	
	styleMenu.add(styleLFMenu);
	
	addStyleMenuListeners();
	
	return styleMenu;
    }


    /**
     * Add listeners to the Style menu.
     */
    private void addStyleMenuListeners() {
	styleLFCrossPlatformMenuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    player.setLookAndFeel
			(UIManager.getCrossPlatformLookAndFeelClassName());
		}
	    });
	styleLFSystemMenuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    player.setLookAndFeel
			(UIManager.getSystemLookAndFeelClassName());
		}
	    });
    }


    /**
     * Creates the Style menu.
     *
     * @return the Style menu
     */
    private JMenu createMonitorMenu() {
	JMenu monitorMenu = new JMenu("Monitor");
	monitorMenu.setMnemonic(monitorMnemonic);
	
	monitorShowMenuItem = new JMenuItem("Show");
	monitorShowMenuItem.setMnemonic(showMonitorMnemonic);
	monitorShowMenuItem.setEnabled(!playerModel.isMonitorVisible());
		
	monitorHideMenuItem = new JMenuItem("Hide");
	monitorHideMenuItem.setMnemonic(hideMonitorMnemonic);
	monitorHideMenuItem.setEnabled(playerModel.isMonitorVisible());
		
	monitorMenu.add(monitorShowMenuItem);
	monitorMenu.add(monitorHideMenuItem);
		
	addMonitorMenuListeners();
	
	return monitorMenu;
    }


    /**
     * Add listeners to the monitor menu.
     */
    private void addMonitorMenuListeners() {
	monitorShowMenuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    monitorShowMenuItem.setEnabled(false);
		    monitorHideMenuItem.setEnabled(true);

		    player.showMonitor(true);
		}
	    });
	monitorHideMenuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    monitorShowMenuItem.setEnabled(true);
		    monitorHideMenuItem.setEnabled(false);
	
		    SwingUtilities.invokeLater
			(new Runnable() {
				public void run() {
				    player.showMonitor(false);
				}
			    });
		}
	    });
    }
    


    /**
     * Returns the JFrame which this menu bar belongs to.
     *
     * @return the JFrame which this menu bar belongs
     */
    private JFrame getFrame() {
	return (JFrame) player;
    }


    /**
     * Shows a JFileChooser screen and returns the chosen file.
     *
     * @return the chosen file
     */
    private File chooseFile() {
	int option = fileChooser.showOpenDialog(getParent());
	if (option == JFileChooser.APPROVE_OPTION) {
	    return fileChooser.getSelectedFile();
	} else {
	    return null;
	}
    }
}


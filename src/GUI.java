import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class GUI extends JFrame implements ActionListener {
	
	private JButton leftPanelNewButton;
    private JButton leftPanelStopButton;
    private JButton leftPanelContinueButton;
    
    private JButton leftPanelRemovePlayerButton;
    private JButton leftPanelResetPlayersButton;
    
    private JLabel leftPanelNumPlayersPlayingLabel;
    private JLabel leftPanelNumGamesPlayedLabel;
    private JLabel leftPanelRoundsMaximum;
    
    private MainAgent mainAgent;
    private JPanel rightPanel;
    private JTextArea rightPanelLoggingTextArea;
    private LoggingOutputStream loggingOutputStream;
    private JTable payoffTable;

    public GUI() {
        initUI();
    }

    public GUI(MainAgent agent) {
        mainAgent = agent;
        initUI();
        loggingOutputStream = new LoggingOutputStream(rightPanelLoggingTextArea);
    }

    public void log(String s) {
        Runnable appendLine = () -> {
            rightPanelLoggingTextArea.append('[' + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] - " + s);
            rightPanelLoggingTextArea.setCaretPosition(rightPanelLoggingTextArea.getDocument().getLength());
        };
        SwingUtilities.invokeLater(appendLine);
    }

    public OutputStream getLoggingOutputStream() {
        return loggingOutputStream;
    }

    public void logLine(String s) {
        log(s + "\n");
    }

    public void setPlayersUI(String[] players) {
    	DefaultTableModel model = (DefaultTableModel) payoffTable.getModel();
    	for (String s : players) {
    		model.addRow(new Object[] {s});
    	}
    }

    public void initUI() {
        setTitle("GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 400));
        setPreferredSize(new Dimension(1000, 600));
        setJMenuBar(createMainMenuBar());
        setContentPane(createMainContentPane());
        pack();
        setVisible(true);
    }

    private Container createMainContentPane() {
        JPanel pane = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.anchor = GridBagConstraints.FIRST_LINE_START;
        gc.gridy = 0;
        gc.weightx = 0.5;
        gc.weighty = 0.5;
        
        gc.insets = new Insets(0, 0, 0, 10);
        //LEFT PANEL
        gc.gridx = 0;
        gc.weightx = 0.5;
        pane.add(createLeftPanel(), gc);

        //CENTRAL PANEL
        gc.gridx = 1;
        gc.weightx = 10;
        pane.add(createCentralPanel(), gc);

        //RIGHT PANEL
        gc.gridx = 2;
        gc.weightx = 8;
        pane.add(createRightPanel(), gc);
        return pane;
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();

        leftPanelNewButton = new JButton("New");
        leftPanelNewButton.addActionListener(this);
        leftPanelNewButton.setToolTipText("Start a new tournament");
        leftPanelStopButton = new JButton("Stop");
        leftPanelStopButton.addActionListener(actionEvent -> mainAgent.stopGame());
        leftPanelStopButton.setToolTipText("Stop the execution of the current tournament");
        leftPanelContinueButton = new JButton("Continue");
        leftPanelContinueButton.addActionListener(actionEvent -> mainAgent.resumeGame());
        leftPanelContinueButton.setToolTipText("Continue the execution of the current tournament");
        enableGameButtons(true);

        leftPanelResetPlayersButton = new JButton("Reset players");
        leftPanelResetPlayersButton.addActionListener(actionEvent -> mainAgent.updatePlayers());
        leftPanelResetPlayersButton.setToolTipText("Reset all players");
        leftPanelRemovePlayerButton = new JButton("Remove player");
        leftPanelRemovePlayerButton.addActionListener(this);
        leftPanelRemovePlayerButton.setToolTipText("Remove an agent from the game");

        leftPanelNumGamesPlayedLabel = new JLabel("Games played : ");
        leftPanelNumGamesPlayedLabel.setToolTipText("Number of total games played in the current tournament");
        leftPanelNumPlayersPlayingLabel = new JLabel("Players playing : ");
        leftPanelNumPlayersPlayingLabel.setToolTipText("Number of current players in the tournament");
        leftPanelRoundsMaximum = new JLabel("Maximum rounds R : ");
        leftPanelRoundsMaximum.setToolTipText("Number of maximum rounds that can be played in one game");


        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.FIRST_LINE_START;
        gc.gridx = 0;
        gc.weightx = 0.5;
        gc.weighty = 0.5;

        gc.gridy = 1;
        leftPanel.add(leftPanelNewButton, gc);
        gc.gridy = 2;
        leftPanel.add(leftPanelStopButton, gc);
        gc.gridy = 3;
        leftPanel.add(leftPanelContinueButton, gc);
        gc.gridy = 4;
        leftPanel.add(leftPanelResetPlayersButton, gc);
        gc.gridy = 5;
        leftPanel.add(leftPanelRemovePlayerButton, gc);
        gc.gridy = 6;
        leftPanel.add(leftPanelNumGamesPlayedLabel, gc);
        gc.gridy = 7;
        leftPanel.add(leftPanelNumPlayersPlayingLabel, gc);
        gc.gridy = 8;
        leftPanel.add(leftPanelRoundsMaximum, gc);

        return leftPanel;
    }

    
    private JPanel createCentralPanel() {
        JPanel centralBottomSubpanel = new JPanel(new GridBagLayout());

        JLabel payoffLabel = new JLabel("Player results : \n");
        DefaultTableModel model = new DefaultTableModel(); 
        payoffTable = new JTable(model); 

        // Create columns 
        model.addColumn("Player"); 
        model.addColumn("Games played"); 
        model.addColumn("Avg payoff");
        model.addColumn("Cooperations");
        model.addColumn("Defections");
        model.addColumn("Games won");
        
        payoffTable.setFillsViewportHeight(true);
        payoffTable.setEnabled(false);
        
        JScrollPane player1ScrollPane = new JScrollPane(payoffTable);

        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = 0.5;
        gc.fill = GridBagConstraints.BOTH;
        gc.anchor = GridBagConstraints.FIRST_LINE_START;

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weighty = 0.5;
        centralBottomSubpanel.add(payoffLabel, gc);
        gc.gridy = 1;
        gc.gridx = 0;
        gc.weighty = 2;
        centralBottomSubpanel.add(player1ScrollPane, gc);

        return centralBottomSubpanel;
    }

    private JPanel createRightPanel() {
        rightPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weighty = 1d;
        c.weightx = 1d;

        rightPanelLoggingTextArea = new JTextArea("");
        rightPanelLoggingTextArea.setEditable(false);
        JScrollPane jScrollPane = new JScrollPane(rightPanelLoggingTextArea);
        rightPanel.add(jScrollPane, c);
        return rightPanel;
    }

    private JMenuBar createMainMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = new JMenu("File");
        JMenuItem exitFileMenu = new JMenuItem("Exit");
        exitFileMenu.setToolTipText("Exit application");
        exitFileMenu.addActionListener(this);

        JMenuItem newGameFileMenu = new JMenuItem("New Game");
        newGameFileMenu.setToolTipText("Start a new game");
        newGameFileMenu.addActionListener(this);

        menuFile.add(newGameFileMenu);
        menuFile.add(exitFileMenu);
        menuBar.add(menuFile);

        JMenu menuEdit = new JMenu("Edit");
        JMenuItem resetPlayerEditMenu = new JMenuItem("Reset Players");
        resetPlayerEditMenu.setToolTipText("Reset all players");
        resetPlayerEditMenu.addActionListener(actionEvent -> mainAgent.updatePlayers());

        JMenuItem parametersEditMenu = new JMenuItem("Remove player");
        parametersEditMenu.setToolTipText("Remove an agent from the game");
        parametersEditMenu.addActionListener(this);
        
        menuEdit.add(resetPlayerEditMenu);
        menuEdit.add(parametersEditMenu);
        menuBar.add(menuEdit);

        JMenu menuRun = new JMenu("Run");

        JMenuItem newRunMenu = new JMenuItem("New");
        newRunMenu.setToolTipText("Starts a new series of games");
        newRunMenu.addActionListener(this);

        JMenuItem stopRunMenu = new JMenuItem("Stop");
        stopRunMenu.setToolTipText("Stops the execution of the current round");
        stopRunMenu.addActionListener(actionEvent -> mainAgent.stopGame());

        JMenuItem continueRunMenu = new JMenuItem("Continue");
        continueRunMenu.setToolTipText("Resume the execution");
        continueRunMenu.addActionListener(actionEvent -> mainAgent.resumeGame());

        JMenuItem roundNumberRunMenu = new JMenuItem("Number Of rounds");
        roundNumberRunMenu.setToolTipText("Change the number of rounds");
        roundNumberRunMenu.addActionListener(this);

        menuRun.add(newRunMenu);
        menuRun.add(stopRunMenu);
        menuRun.add(continueRunMenu);
        menuRun.add(roundNumberRunMenu);
        menuBar.add(menuRun);

        JMenu menuWindow = new JMenu("Window");

        JCheckBoxMenuItem toggleVerboseWindowMenu = new JCheckBoxMenuItem("Verbose", true);
        toggleVerboseWindowMenu.addActionListener(actionEvent -> rightPanel.setVisible(toggleVerboseWindowMenu.getState()));

        menuWindow.add(toggleVerboseWindowMenu);
        menuBar.add(menuWindow);

        JMenu menuHelp = new JMenu("Help");
        
        JMenuItem aboutHelpMenu = new JMenuItem("About");
        aboutHelpMenu.setToolTipText("Info about the app");
        aboutHelpMenu.addActionListener(this);
        
        menuHelp.add(aboutHelpMenu);
        menuBar.add(menuHelp);

        return menuBar;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JButton) {
            JButton button = (JButton) e.getSource();
            logLine("Button " + button.getText());
            String item = button.getText();
            if (item.equals("New")) {
            	mainAgent.newGame();
            }
            else if (item.equals("Remove player")) {
            	String playerName = JOptionPane.showInputDialog(new Frame("Remove player"), "Enter local name of a player to remove");
            	mainAgent.deletePlayer(playerName);
            	sleep();
            	mainAgent.updatePlayers();
            } 
                 
        } else if (e.getSource() instanceof JMenuItem) {
            JMenuItem menuItem = (JMenuItem) e.getSource();
            logLine("Menu " + menuItem.getText());
            String item = menuItem.getText();
            if (item.equals("Exit")) {
            	System.exit(0);
            }
            else if (item.equals("New Game") || item.equals("New")) {
            	mainAgent.newGame();
            }
            else if (item.equals("Remove player")) {
            	String playerName = JOptionPane.showInputDialog(new Frame("Remove player"), "Enter local name of a player to remove");
            	mainAgent.deletePlayer(playerName);
            	sleep();
            	mainAgent.updatePlayers();
            }
            else if (item.equals("Number Of rounds")) {
            	String results = JOptionPane.showInputDialog(new Frame("Configure rounds"), "How many rounds?");
            	setRoundsMaximum(results);
            }
            else if (item.equals("About")) {
            	String about = "This application was developed by Sven Goluža as a part of 2020./21. PSI course in the University of Vigo";
            	JOptionPane.showMessageDialog(new JFrame(), about, "About",
        	            JOptionPane.INFORMATION_MESSAGE);
            }
        }	
    }
    
    
    public void enableGameButtons(boolean enable) {
    	leftPanelNewButton.setEnabled(enable);
    	leftPanelStopButton.setEnabled(!enable);
        leftPanelContinueButton.setEnabled(!enable);
    }
    
    public void enableResetButtons(boolean enable) {
    	leftPanelRemovePlayerButton.setEnabled(enable);
    	leftPanelResetPlayersButton.setEnabled(enable);
    }
    
    public void resetTable() {
    	DefaultTableModel model = (DefaultTableModel) payoffTable.getModel();
    	model.setRowCount(0);
    }
    
    public void removePlayerRow(String playerName) {
    	DefaultTableModel model = (DefaultTableModel) payoffTable.getModel();
    	int rows = model.getRowCount();
    	for (int i = 0; i < rows; i++) {
			if (model.getValueAt(i, 0).equals(playerName)) {
				model.removeRow(i);
				break;
			}
		}
    }
    
    public void updatePlayerTable(MainAgent.PlayerInformation player) {
    	DefaultTableModel model = (DefaultTableModel) payoffTable.getModel();
    	int rows = model.getRowCount();
    	int gamesPlayed = player.gamesPlayed;
    	player.calcFinalPayoff();
    	double payoff = player.finalPayoff;
    	int cooperations = player.cooperations;
    	int defections = player.defections;
    	int gamesWon = player.gamesWon;
    	
    	DecimalFormat format = new DecimalFormat("#.###");
		for (int i = 0; i < rows; i++) {
			if (model.getValueAt(i, 0).equals(player.aid.getLocalName())) {
				model.setValueAt(gamesPlayed, i, 1);
				model.setValueAt(format.format(payoff), i, 2);
				model.setValueAt(cooperations, i, 3);
				model.setValueAt(defections, i, 4);
				model.setValueAt(gamesWon, i, 5);
			}
		}
    }
    
    public void setGamesPlayed(String gamesPlayed) {
    	String text = leftPanelNumGamesPlayedLabel.getText();
    	text = text.replaceAll("[0-9]","");
    	leftPanelNumGamesPlayedLabel.setText(text + gamesPlayed);
    }
    
    // PARAMETERS - N and R
    public void setPlayersPlaying(String playersPlaying) {
    	String text = leftPanelNumPlayersPlayingLabel.getText();
    	text = text.replaceAll("[0-9]","");
    	leftPanelNumPlayersPlayingLabel.setText(text + playersPlaying);
    	mainAgent.setNumberOfPlayers(Integer.valueOf(playersPlaying.trim()));
    }
    
    public void setRoundsMaximum(String R) {
    	String text = leftPanelRoundsMaximum.getText();
    	text = text.replaceAll("[0-9]","");
    	leftPanelRoundsMaximum.setText(text + R);
    	mainAgent.setRounds(Integer.valueOf(R.trim()));
    }
    
    // show tournament winner
    public void showWinner(String playerName) {
    	JOptionPane.showMessageDialog(new JFrame(), "Winner : " + playerName, "Tournament winner",
	            JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void showWinner(String playerName, boolean winnerExists) {
    	if (!winnerExists) {
    		JOptionPane.showMessageDialog(new JFrame(), "There is no winner! Multiple players are equal", "Tournament winner",
    	            JOptionPane.INFORMATION_MESSAGE);
    		logLine("There is no winner! Multiple players are equal");
    	} else {
    		showWinner(playerName);
    		logLine(" WINNER IS : " + playerName);
    	}
    }
    
    private void sleep() {
    	// adding because otherwise main agent does not update players on time!!
    	try
    	{
    	    Thread.sleep(100);
    	}
    	catch(InterruptedException ex)
    	{
    	    Thread.currentThread().interrupt();
    	}
    }
    
    public class LoggingOutputStream extends OutputStream {
        private JTextArea textArea;

        public LoggingOutputStream(JTextArea jTextArea) {
            textArea = jTextArea;
        }

        @Override
        public void write(int i) throws IOException {
            textArea.append(String.valueOf((char) i));
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }
}

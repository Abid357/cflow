package frames;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import globals.DatabaseFacade;
import globals.LOGGER;
import globals._Settings;
import net.miginfocom.swing.MigLayout;

public class Main extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane, panelCards;
	private JToggleButton btnInventory, btnTransaction, btnSettings, btnUser, btnLogs, btnFunctions;
	private JButton btnLoad;
	private JButton btnSave;
	private JLabel lblWelcome;

	/**
	 * Create the frame.
	 */
	public Main() {
		setBounds(100, 100, 1200, 1000);
		contentPane = new JPanel();
		contentPane.setBackground(_Settings.backgroundColor);
		contentPane.setBorder(null);
		setContentPane(contentPane);
		setTitle("cFlow Main");
		contentPane.setLayout(new MigLayout("", "[10][150][10][150][10][550][grow]", "[10][40][40][10][grow]"));

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				if (DatabaseFacade.hasUnsavedData()) {
					MyOptionPane mop = new MyOptionPane("There are unsaved data. Are you sure you want to quit?",
							MyOptionPane.OPTION_DIALOG_BOX);
					int result = mop.getSelection();
					if (result == MyOptionPane.NEGATIVE)
						return;
				}
				_Settings.writeSetting();
				_Settings.userSignOut("cFlow Main");
				System.exit(0);
			}
		});

		cards.Logs cardLogs = new cards.Logs(this);
		cards.Transaction cardTransaction = new cards.Transaction(this);
		cards.Settings cardSettings = new cards.Settings();
		cards.Functions cardFunctions = new cards.Functions(this);
		cards.Inventory cardInventory = new cards.Inventory(this, cardTransaction, cardSettings);
		cards.User cardUser = new cards.User(this, cardTransaction);
		cardTransaction.setCard(cardInventory);
		cardTransaction.setCard(cardUser);

		btnLoad = new JButton("LOAD");
		btnLoad.setBackground(_Settings.backgroundColor);
		btnLoad.setForeground(_Settings.labelColor);
		btnLoad.setFont(new Font("Arial Black", Font.PLAIN, 21));
		btnLoad.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnLoad.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (DatabaseFacade.loadDatabases()) {
					cardInventory.updateTable();
					cardTransaction.updateOptions();
					cardTransaction.resetSelections();
					cardUser.updateOptions();
					cardUser.resetSelections();
					new MyOptionPane("All databases loaded!", MyOptionPane.CONFIRMATION_DIALOG_BOX);
				}
			}
		});
		contentPane.add(btnLoad, "cell 1 1 1 2,grow");

		btnSave = new JButton("SAVE");
		btnSave.setBackground(_Settings.backgroundColor);
		btnSave.setForeground(_Settings.labelColor);
		btnSave.setFont(new Font("Arial Black", Font.PLAIN, 21));
		btnSave.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnSave.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnSave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if (DatabaseFacade.saveDatabases()) {
					new MyOptionPane("All databases saved!", MyOptionPane.CONFIRMATION_DIALOG_BOX);
					LOGGER.Activity.log("Database", LOGGER.UPDATE, DatabaseFacade.dirtyCount + " file(s)");
					DatabaseFacade.dirtyCount = 0;
				}
			}
		});
		contentPane.add(btnSave, "cell 3 1 1 2,grow");

		String welcome = _Settings.userName;
		if (welcome == null)
			welcome = "User";
		lblWelcome = new JLabel("Welcome, " + welcome + "!");
		lblWelcome.setHorizontalAlignment(SwingConstants.CENTER);
		lblWelcome.setVerticalAlignment(SwingConstants.BOTTOM);
		lblWelcome.setBorder(null);
		lblWelcome.setFont(new Font("Century Gothic", Font.PLAIN, 25));
		lblWelcome.setBackground(null);
		lblWelcome.setForeground(_Settings.textFieldColor);
		contentPane.add(lblWelcome, "cell 5 2,grow");

		JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerSize(10);
		splitPane.setEnabled(false);
		splitPane.setUI(new BasicSplitPaneUI() {
			public BasicSplitPaneDivider createDefaultDivider() {
				return new BasicSplitPaneDivider(this) {
					/**
					*
					*/
					private static final long serialVersionUID = 1L;

					public void setBorder(Border b) {
					}

					@Override
					public void paint(Graphics g) {
						g.setColor(_Settings.backgroundColor);
						g.fillRect(0, 0, getSize().width, getSize().height);
						super.paint(g);
					}
				};
			}
		});
		splitPane.setBorder(null);
		contentPane.add(splitPane, "cell 0 4 7 1,grow");

		panelCards = new JPanel();
		splitPane.setRightComponent(panelCards);
		panelCards.setLayout(new CardLayout(0, 0));

		JPanel panelButtons = new JPanel();
		panelButtons.setBackground(_Settings.backgroundColor);
		panelButtons.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panelButtons.setPreferredSize(new Dimension(10, 25));
		splitPane.setLeftComponent(panelButtons);
		panelButtons.setLayout(
				new MigLayout("", "[100%]", "[16.66667%][16.66667%][16.66667%][16.66667%][16.66667%][16.66667%]"));

		btnInventory = new JToggleButton("INVENTORY");
		btnInventory.setSelected(true);
		btnInventory.setBackground(_Settings.backgroundColor);
		btnInventory.setForeground(_Settings.labelColor);
		btnInventory.setFont(new Font("Arial Black", Font.PLAIN, 17));
		btnInventory.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnInventory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				((CardLayout) panelCards.getLayout()).show(panelCards, btnInventory.getText());
			}
		});
		btnInventory.setPreferredSize(new Dimension(150, 25));
		btnInventory.setAlignmentX(Component.CENTER_ALIGNMENT);
		panelButtons.add(btnInventory, "cell 0 0,grow");
		panelCards.add(cardInventory, btnInventory.getText());

		btnTransaction = new JToggleButton("TRANSACTION");
		btnTransaction.setBackground(_Settings.backgroundColor);
		btnTransaction.setForeground(_Settings.labelColor);
		btnTransaction.setFont(new Font("Arial Black", Font.PLAIN, 17));
		btnTransaction.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnTransaction.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnTransaction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				((CardLayout) panelCards.getLayout()).show(panelCards, btnTransaction.getText());
			}
		});
		panelButtons.add(btnTransaction, "cell 0 1,grow");
		panelCards.add(cardTransaction, btnTransaction.getText());

		btnLogs = new JToggleButton("LOGS");
		btnLogs.setBackground(_Settings.backgroundColor);
		btnLogs.setForeground(_Settings.labelColor);
		btnLogs.setFont(new Font("Arial Black", Font.PLAIN, 17));
		btnLogs.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnLogs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				((CardLayout) panelCards.getLayout()).show(panelCards, btnLogs.getText());
				cardLogs.refresh();
			}
		});
		panelButtons.add(btnLogs, "cell 0 5,grow");
		panelCards.add(cardLogs, btnLogs.getText());

		btnUser = new JToggleButton("USER");
		btnUser.setBackground(_Settings.backgroundColor);
		btnUser.setForeground(_Settings.labelColor);
		btnUser.setFont(new Font("Arial Black", Font.PLAIN, 17));
		btnUser.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				((CardLayout) panelCards.getLayout()).show(panelCards, btnUser.getText());
			}
		});
		panelButtons.add(btnUser, "cell 0 2,grow");
		panelCards.add(cardUser, btnUser.getText());

		btnFunctions = new JToggleButton("FUNCTIONS");
		btnFunctions.setBackground(_Settings.backgroundColor);
		btnFunctions.setForeground(_Settings.labelColor);
		btnFunctions.setFont(new Font("Arial Black", Font.PLAIN, 17));
		btnFunctions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				((CardLayout) panelCards.getLayout()).show(panelCards, btnFunctions.getText());
			}
		});
		btnFunctions.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		panelButtons.add(btnFunctions, "cell 0 3,grow");
		panelCards.add(cardFunctions, btnFunctions.getText());

		btnSettings = new JToggleButton("SETTINGS");
		btnSettings.setBackground(_Settings.backgroundColor);
		btnSettings.setForeground(_Settings.labelColor);
		btnSettings.setFont(new Font("Arial Black", Font.PLAIN, 17));
		btnSettings.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				((CardLayout) panelCards.getLayout()).show(panelCards, btnSettings.getText());
			}
		});
		panelButtons.add(btnSettings, "cell 0 4,grow");
		panelCards.add(cardSettings, btnSettings.getText());

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(btnInventory);
		buttonGroup.add(btnTransaction);
		buttonGroup.add(btnLogs);
		buttonGroup.add(btnUser);
		buttonGroup.add(btnFunctions);
		buttonGroup.add(btnSettings);

		splitPane.setDividerLocation(200);

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
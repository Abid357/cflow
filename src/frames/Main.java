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
	private JToggleButton btnItem, btnStore, btnProduction, btnSale, btnLogs, btnPurchase;
	private JButton btnLoad;
	private JButton btnSave;
	private JLabel lblWelcome;
	public cards.Item cardItem;
	public cards.Store cardStore;
	public cards.Sale cardSale;
	public cards.Purchase cardPurchase;
	public cards.Production cardProduction;
	public cards.Logs cardLogs;

	/**
	 * Create the frame.
	 */
	public Main() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 1200, 1000);
		contentPane = new JPanel();
		contentPane.setBackground(_Settings.backgroundColor);
		contentPane.setBorder(null);
		setContentPane(contentPane);
		setTitle("cFlow Enterprise");
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
				_Settings.userSignOut("cFlow Enterprise");
				System.exit(0);
			}
		});

		cardItem = new cards.Item(this);
		cardStore = new cards.Store(this);
		cardSale = new cards.Sale(this);
		cardPurchase = new cards.Purchase(this);
		cardProduction = new cards.Production(this);
		cardLogs = new cards.Logs(this);

		btnLoad = new JButton("LOAD");
		btnLoad.setBackground(_Settings.backgroundColor);
		btnLoad.setForeground(_Settings.labelColor);
		btnLoad.setFont(new Font("Arial Black", Font.PLAIN, 21));
		btnLoad.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnLoad.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (DatabaseFacade.loadDatabases()) {
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

		btnItem = new JToggleButton("ITEM");
		btnItem.setSelected(true);
		btnItem.setBackground(_Settings.backgroundColor);
		btnItem.setForeground(_Settings.labelColor);
		btnItem.setFont(new Font("Arial Black", Font.PLAIN, 17));
		btnItem.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				((CardLayout) panelCards.getLayout()).show(panelCards, btnItem.getText());
			}
		});
		btnItem.setPreferredSize(new Dimension(150, 25));
		btnItem.setAlignmentX(Component.CENTER_ALIGNMENT);
		panelButtons.add(btnItem, "cell 0 0,grow");
		panelCards.add(cardItem, btnItem.getText());

		btnStore = new JToggleButton("STORE");
		btnStore.setBackground(_Settings.backgroundColor);
		btnStore.setForeground(_Settings.labelColor);
		btnStore.setFont(new Font("Arial Black", Font.PLAIN, 17));
		btnStore.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnStore.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnStore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				((CardLayout) panelCards.getLayout()).show(panelCards, btnStore.getText());
			}
		});
		panelButtons.add(btnStore, "cell 0 1,grow");
		panelCards.add(cardStore, btnStore.getText());

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

		btnSale = new JToggleButton("SALE");
		btnSale.setBackground(_Settings.backgroundColor);
		btnSale.setForeground(_Settings.labelColor);
		btnSale.setFont(new Font("Arial Black", Font.PLAIN, 17));
		btnSale.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnSale.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				((CardLayout) panelCards.getLayout()).show(panelCards, btnSale.getText());
			}
		});
		panelButtons.add(btnSale, "cell 0 2,grow");
		panelCards.add(cardSale, btnSale.getText());

		btnPurchase = new JToggleButton("PURCHASE");
		btnPurchase.setBackground(_Settings.backgroundColor);
		btnPurchase.setForeground(_Settings.labelColor);
		btnPurchase.setFont(new Font("Arial Black", Font.PLAIN, 17));
		btnPurchase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				((CardLayout) panelCards.getLayout()).show(panelCards, btnPurchase.getText());
			}
		});
		btnPurchase.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		panelButtons.add(btnPurchase, "cell 0 3,grow");
		panelCards.add(cardPurchase, btnPurchase.getText());

		btnProduction = new JToggleButton("PRODUCTION");
		btnProduction.setBackground(_Settings.backgroundColor);
		btnProduction.setForeground(_Settings.labelColor);
		btnProduction.setFont(new Font("Arial Black", Font.PLAIN, 17));
		btnProduction.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnProduction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				((CardLayout) panelCards.getLayout()).show(panelCards, btnProduction.getText());
			}
		});
		panelButtons.add(btnProduction, "cell 0 4,grow");
		panelCards.add(cardProduction, btnProduction.getText());

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(btnItem);
		buttonGroup.add(btnStore);
		buttonGroup.add(btnLogs);
		buttonGroup.add(btnSale);
		buttonGroup.add(btnPurchase);
		buttonGroup.add(btnProduction);

		splitPane.setDividerLocation(200);

		setLocationRelativeTo(null);
		setVisible(true);
	}
}
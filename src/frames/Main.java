package frames;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import cards.Default;
import cards.IReport;
import cards.InventoryReport1;
import cards.ItemReport1;
import cards.ItemReport2;
import cards.PurchaseReport1;
import cards.PurchaseReport2;
import cards.SalesReport1;
import cards.SalesReport2;
import cards.SalesReport3;
import cards.SalesReport4;
import cards.SalesReport5;
import cards.StockReport1;
import cards.StockReport2;
import cards.StockReport3;
import cards.TransactionReport1;
import cards.UserReport1;
import cards.UserReport2;
import globals._Settings;
import net.miginfocom.swing.MigLayout;

public class Main extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane, pnlCard, pnlSelections;
	private MyToggleButton btnTransaction, btnSale, btnItem, btnPurchase, btnStock, btnUser, btnProduction,
			btnInventory;
	private static ButtonGroup bg;
	private static List<MyToggleButton> btnlstTransaction, btnlstSale, btnlstItem, btnlstPurchase, btnlstStock,
			btnlstUser, btnlstProduction, btnlstInventory;
	private JButton btnGenerate;
	private static IReport visiblePanel;

	private static void initReports(JPanel pnlCard) {
		String htmlTags = "<html><center><p>";
		String htmlUntags = "</p></center></html>";

		// INVENTORY	
		MyToggleButton btn15 = new MyToggleButton(htmlTags + "Inventory List" + htmlUntags);
		btn15.setName("InventoryReport1");
		btnlstInventory.add(btn15);
		JPanel panel15 = new InventoryReport1();
		pnlCard.add(panel15, btn15.getName());
		btn15.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				visiblePanel = (IReport) panel15;
				((CardLayout) pnlCard.getLayout()).show(pnlCard, btn15.getName());
			}
		});
		bg.add(btn15);
		
		// TRANSACTION
		MyToggleButton btn16 = new MyToggleButton(htmlTags + "Transaction Statement" + htmlUntags);
		btn16.setName("TransactionReport1");
		btnlstTransaction.add(btn16);
		JPanel panel16 = new TransactionReport1();
		pnlCard.add(panel16, btn16.getName());
		btn16.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				visiblePanel = (IReport) panel16;
				((CardLayout) pnlCard.getLayout()).show(pnlCard, btn16.getName());
			}
		});
		bg.add(btn16);
		
		
		// USER
		MyToggleButton btn14 = new MyToggleButton(htmlTags + "User List" + htmlUntags);
		btn14.setName("UserReport2");
		btnlstUser.add(btn14);
		JPanel panel14 = new UserReport2();
		pnlCard.add(panel14, btn14.getName());
		btn14.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				visiblePanel = (IReport) panel14;
				((CardLayout) pnlCard.getLayout()).show(pnlCard, btn14.getName());
			}
		});
		bg.add(btn14);
		
		MyToggleButton btn13 = new MyToggleButton(htmlTags + "User Details" + htmlUntags);
		btn13.setName("UserReport1");
		btnlstUser.add(btn13);
		JPanel panel13 = new UserReport1();
		pnlCard.add(panel13, btn13.getName());
		btn13.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				visiblePanel = (IReport) panel13;
				((CardLayout) pnlCard.getLayout()).show(pnlCard, btn13.getName());
			}
		});
		bg.add(btn13);
		
		// SALE
		MyToggleButton btn2 = new MyToggleButton(htmlTags + "Daily Sales" + htmlUntags);
		btn2.setName("SalesReport2");
		btnlstSale.add(btn2);
		JPanel panel2 = new SalesReport2();
		pnlCard.add(panel2, btn2.getName());
		btn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				visiblePanel = (IReport) panel2;
				((CardLayout) pnlCard.getLayout()).show(pnlCard, btn2.getName());
			}
		});
		bg.add(btn2);

		MyToggleButton btn5 = new MyToggleButton(htmlTags + "Sales by Customer" + htmlUntags);
		btn5.setName("SalesReport3");
		btnlstSale.add(btn5);
		JPanel panel5 = new SalesReport3();
		pnlCard.add(panel5, btn5.getName());
		btn5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				visiblePanel = (IReport) panel5;
				((CardLayout) pnlCard.getLayout()).show(pnlCard, btn5.getName());
			}
		});
		bg.add(btn5);
		
		MyToggleButton btn1 = new MyToggleButton(htmlTags + "Total Sales" + htmlUntags);
		btn1.setName("SalesReport1");
		btnlstSale.add(btn1);
		JPanel panel1 = new SalesReport1();
		pnlCard.add(panel1, btn1.getName());
		btn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				visiblePanel = (IReport) panel1;
				((CardLayout) pnlCard.getLayout()).show(pnlCard, btn1.getName());
			}
		});
		bg.add(btn1);

		MyToggleButton btn9 = new MyToggleButton(htmlTags + "Total Profit" + htmlUntags);
		btn9.setName("SalesReport4");
		btnlstSale.add(btn9);
		JPanel panel9 = new SalesReport4();
		pnlCard.add(panel9, btn9.getName());
		btn9.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				visiblePanel = (IReport) panel9;
				((CardLayout) pnlCard.getLayout()).show(pnlCard, btn9.getName());
			}
		});
		bg.add(btn9);

		MyToggleButton btn6 = new MyToggleButton(htmlTags + "Total Stock Sold" + htmlUntags);
		btn6.setName("StockReport1");
		btnlstStock.add(btn6);
		btnlstSale.add(btn6);
		JPanel panel6 = new StockReport1();
		pnlCard.add(panel6, btn6.getName());
		btn6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				visiblePanel = (IReport) panel6;
				((CardLayout) pnlCard.getLayout()).show(pnlCard, btn6.getName());
			}
		});
		bg.add(btn6);

		MyToggleButton btn10 = new MyToggleButton(htmlTags + "Customer List" + htmlUntags);
		btn10.setName("SalesReport5");
		btnlstSale.add(btn10);
		JPanel panel10 = new SalesReport5();
		pnlCard.add(panel10, btn10.getName());
		btn10.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				visiblePanel = (IReport) panel10;
				((CardLayout) pnlCard.getLayout()).show(pnlCard, btn10.getName());
			}
		});
		bg.add(btn10);
		
		// PURCHASE
		MyToggleButton btn4 = new MyToggleButton(htmlTags + "Purchase by Supplier" + htmlUntags);
		btn4.setName("PurchaseReport2");
		btnlstPurchase.add(btn4);
		JPanel panel4 = new PurchaseReport2();
		pnlCard.add(panel4, btn4.getName());
		btn4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				visiblePanel = (IReport) panel4;
				((CardLayout) pnlCard.getLayout()).show(pnlCard, btn4.getName());
			}
		});
		bg.add(btn4);
		
		MyToggleButton btn3 = new MyToggleButton(htmlTags + "Total Purchase" + htmlUntags);
		btn3.setName("PurchaseReport1");
		btnlstPurchase.add(btn3);
		JPanel panel3 = new PurchaseReport1();
		pnlCard.add(panel3, btn3.getName());
		btn3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				visiblePanel = (IReport) panel3;
				((CardLayout) pnlCard.getLayout()).show(pnlCard, btn3.getName());
			}
		});
		bg.add(btn3);
		
		// PRODUCTION
		
		// STOCK
		MyToggleButton btn7 = new MyToggleButton(htmlTags + "Current Stock Overview" + htmlUntags);
		btn7.setName("StockReport2");
		btnlstStock.add(btn7);
		JPanel panel7 = new StockReport2();
		pnlCard.add(panel7, btn7.getName());
		btn7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				visiblePanel = (IReport) panel7;
				((CardLayout) pnlCard.getLayout()).show(pnlCard, btn7.getName());
			}
		});
		bg.add(btn7);

		MyToggleButton btn8 = new MyToggleButton(htmlTags + "Current Stock by Store" + htmlUntags);
		btn8.setName("StockReport3");
		btnlstStock.add(btn8);
		JPanel panel8 = new StockReport3();
		pnlCard.add(panel8, btn8.getName());
		btn8.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				visiblePanel = (IReport) panel8;
				((CardLayout) pnlCard.getLayout()).show(pnlCard, btn8.getName());
			}
		});
		bg.add(btn8);
		
		// ITEM
		MyToggleButton btn12 = new MyToggleButton(htmlTags + "Item List" + htmlUntags);
		btn12.setName("ItemReport2");
		btnlstItem.add(btn12);
		JPanel panel12 = new ItemReport2();
		pnlCard.add(panel12, btn12.getName());
		btn12.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				visiblePanel = (IReport) panel12;
				((CardLayout) pnlCard.getLayout()).show(pnlCard, btn12.getName());
			}
		});
		bg.add(btn12);
		
		MyToggleButton btn11 = new MyToggleButton(htmlTags + "Item Details" + htmlUntags);
		btn11.setName("ItemReport1");
		btnlstItem.add(btn11);
		JPanel panel11 = new ItemReport1();
		pnlCard.add(panel11, btn11.getName());
		btn11.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				visiblePanel = (IReport) panel11;
				((CardLayout) pnlCard.getLayout()).show(pnlCard, btn11.getName());
			}
		});
		bg.add(btn11);
	}

	/**
	 * Create the frame.
	 */
	public Main() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 900, 1000);
		contentPane = new JPanel();
		contentPane.setBackground(_Settings.backgroundColor);
		contentPane.setBorder(null);
		setContentPane(contentPane);
		setTitle("cFlow Report");
		contentPane.setLayout(new MigLayout("", "[10][200:200][10][150:150][10][grow][150][10]", "[grow][60]"));

		String welcome = _Settings.userName;
		if (welcome == null)
			welcome = "User";

		bg = new ButtonGroup();
		btnlstTransaction = new ArrayList<MyToggleButton>();
		btnlstSale = new ArrayList<MyToggleButton>();
		btnlstPurchase = new ArrayList<MyToggleButton>();
		btnlstStock = new ArrayList<MyToggleButton>();
		btnlstItem = new ArrayList<MyToggleButton>();
		btnlstUser = new ArrayList<MyToggleButton>();
		btnlstInventory = new ArrayList<MyToggleButton>();
		btnlstProduction = new ArrayList<MyToggleButton>();

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
		contentPane.add(splitPane, "cell 3 0 4 1,grow");

		pnlCard = new JPanel();
		splitPane.setRightComponent(pnlCard);
		pnlCard.setLayout(new CardLayout(0, 0));

		pnlCard.add(new Default(), "Default");
		initReports(pnlCard);

		JPanel pnlReports = new JPanel();
		pnlReports.setBackground(_Settings.backgroundColor);
		pnlReports.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		pnlReports.setPreferredSize(new Dimension(10, 25));
		splitPane.setLeftComponent(pnlReports);
		pnlReports.setLayout(new GridLayout(10, 0, 0, 0));

		pnlSelections = new JPanel();
		pnlSelections.setBackground(_Settings.backgroundColor);
		pnlSelections.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		pnlSelections.setPreferredSize(new Dimension(10, 25));
		contentPane.add(pnlSelections, "cell 1 0,grow");
		pnlSelections
				.setLayout(new MigLayout("", "[grow]", "[12.5%][12.5%][12.5%][12.5%][12.5%][12.5%][12.5%][12.5%]"));

		btnTransaction = new MyToggleButton("TRANSACTION");
		btnTransaction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pnlReports.removeAll();
				Iterator<MyToggleButton> iterator = btnlstTransaction.iterator();
				while (iterator.hasNext())
					pnlReports.add(iterator.next());
				bg.clearSelection();
				pnlReports.revalidate();
				pnlReports.repaint();
				((CardLayout) pnlCard.getLayout()).show(pnlCard, "Default");
			}
		});
		pnlSelections.add(btnTransaction, "cell 0 1,grow");

		btnInventory = new frames.MyToggleButton("INVENTORY");
		btnInventory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pnlReports.removeAll();
				Iterator<MyToggleButton> iterator = btnlstInventory.iterator();
				while (iterator.hasNext())
					pnlReports.add(iterator.next());
				bg.clearSelection();
				pnlReports.revalidate();
				pnlReports.repaint();
				((CardLayout) pnlCard.getLayout()).show(pnlCard, "Default");
			}
		});
		pnlSelections.add(btnInventory, "cell 0 0,grow");

		btnSale = new MyToggleButton("SALE");
		btnSale.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pnlReports.removeAll();
				Iterator<MyToggleButton> iterator = btnlstSale.iterator();
				while (iterator.hasNext())
					pnlReports.add(iterator.next());
				bg.clearSelection();
				pnlReports.revalidate();
				pnlReports.repaint();
				((CardLayout) pnlCard.getLayout()).show(pnlCard, "Default");
			}
		});
		pnlSelections.add(btnSale, "cell 0 3,grow");

		btnUser = new MyToggleButton("USER");
		btnUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pnlReports.removeAll();
				Iterator<MyToggleButton> iterator = btnlstUser.iterator();
				while (iterator.hasNext())
					pnlReports.add(iterator.next());
				bg.clearSelection();
				pnlReports.revalidate();
				pnlReports.repaint();
				((CardLayout) pnlCard.getLayout()).show(pnlCard, "Default");
			}
		});
		pnlSelections.add(btnUser, "cell 0 2,grow");

		btnPurchase = new MyToggleButton("PURCHASE");
		btnPurchase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pnlReports.removeAll();
				Iterator<MyToggleButton> iterator = btnlstPurchase.iterator();
				while (iterator.hasNext())
					pnlReports.add(iterator.next());
				bg.clearSelection();
				pnlReports.revalidate();
				pnlReports.repaint();
				((CardLayout) pnlCard.getLayout()).show(pnlCard, "Default");
			}
		});
		pnlSelections.add(btnPurchase, "cell 0 4,grow");

		btnStock = new MyToggleButton("STOCK");
		btnStock.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pnlReports.removeAll();
				Iterator<MyToggleButton> iterator = btnlstStock.iterator();
				while (iterator.hasNext())
					pnlReports.add(iterator.next());
				bg.clearSelection();
				pnlReports.revalidate();
				pnlReports.repaint();
				((CardLayout) pnlCard.getLayout()).show(pnlCard, "Default");
			}
		});
		pnlSelections.add(btnStock, "cell 0 6,grow");

		btnProduction = new MyToggleButton("PRODUCTION");
		btnProduction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pnlReports.removeAll();
				Iterator<MyToggleButton> iterator = btnlstProduction.iterator();
				while (iterator.hasNext())
					pnlReports.add(iterator.next());
				bg.clearSelection();
				pnlReports.revalidate();
				pnlReports.repaint();
				((CardLayout) pnlCard.getLayout()).show(pnlCard, "Default");
			}
		});
		pnlSelections.add(btnProduction, "cell 0 5,grow");

		btnItem = new MyToggleButton("ITEM");
		btnItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pnlReports.removeAll();
				Iterator<MyToggleButton> iterator = btnlstItem.iterator();
				while (iterator.hasNext())
					pnlReports.add(iterator.next());
				bg.clearSelection();
				pnlReports.revalidate();
				pnlReports.repaint();
				((CardLayout) pnlCard.getLayout()).show(pnlCard, "Default");
			}
		});
		pnlSelections.add(btnItem, "cell 0 7,grow");

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(btnTransaction);
		buttonGroup.add(btnSale);
		buttonGroup.add(btnPurchase);
		buttonGroup.add(btnStock);
		buttonGroup.add(btnItem);
		buttonGroup.add(btnUser);
		buttonGroup.add(btnProduction);
		buttonGroup.add(btnInventory);

		splitPane.setDividerLocation(200);

		btnGenerate = new JButton("GENERATE");
		btnGenerate.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnGenerate.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnGenerate.setBackground(_Settings.backgroundColor);
		btnGenerate.setForeground(_Settings.labelColor);
		btnGenerate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (visiblePanel == null)
					new MyOptionPane("Select a report for more options.", MyOptionPane.ERROR_MESSAGE);
				else if (!visiblePanel.isReady())
					new MyOptionPane("Report form has incomplete data.", MyOptionPane.INFORMATION_MESSAGE);
				else
					visiblePanel.generateReport();
			}
		});
		contentPane.add(btnGenerate, "cell 6 1,grow");

		setLocationRelativeTo(null);
		setVisible(true);
	}
}

class MyToggleButton extends JToggleButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MyToggleButton(String text) {
		super(text);
		setBackground(_Settings.backgroundColor);
		setForeground(_Settings.labelColor);
		setFont(new Font("Arial Black", Font.PLAIN, 17));
		setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		setAlignmentX(Component.CENTER_ALIGNMENT);
	}
}
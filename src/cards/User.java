package cards;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;

import cores.Organization;
import cores.Person;
import cores.UserObject;
import databases.UserCategories;
import databases.UserObjects;
import frames.Main;
import globals.DatabaseFacade;
import globals.LOGGER;
import globals.ReportFacade;
import globals.ValueFormatter;
import globals._Settings;
import helpers.MyComboBoxRenderer;
import helpers.SortedComboBoxModel;
import net.miginfocom.swing.MigLayout;

public class User extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Main mainFrame;
	private JTable tblUser;
	private DefaultTableModel dtm;
	private String[] personHeaders = { "FIRST NAME", "LAST NAME", "BALANCE", "PHONE", "EMAIL", "NATIONALITY" };
	private String[] orgHeaders = { "NAME", "CONTACT", "BALANCE", "PHONE", "EMAIL", "LOCATION" };
	private JComboBox<String> cbType, cbCategory;
	private cards.Transaction cardTransaction;

	public void updateOptions() {
		cbCategory.removeAllItems();
		UserCategories categories = (UserCategories) DatabaseFacade.getDatabase("UserCategories");
		List<String> categoryList = categories.getList();
		cbCategory.addItem("ANY");
		for (String category : categoryList)
			cbCategory.addItem(category);
		cbCategory.setSelectedIndex(0);
	}

	public void resetSelections() {
		cbType.setSelectedIndex(0);
		cbCategory.setSelectedIndex(0);
		updateTable();
	}

	public void updateTable() {
		dtm.setRowCount(0);
		String selectedType = (String) cbType.getSelectedItem();
		UserObjects db = (UserObjects) DatabaseFacade.getDatabase("UserObjects");
		List<cores.UserObject> userObjects = new ArrayList<UserObject>();
		if (selectedType.equals("PERSON")) {
			dtm.setColumnIdentifiers(personHeaders);
			List<Person> personList = db.getPersonList();
			for (Person person : personList)
				userObjects.add(person);
		} else {
			dtm.setColumnIdentifiers(orgHeaders);
			List<Organization> organizationList = db.getOrganizationList();
			for (Organization organization : organizationList)
				userObjects.add(organization);
		}
		String selectedCategory = (String) cbCategory.getSelectedItem();
		if (!selectedCategory.equals("ANY")) {
			List<cores.UserObject> categoryUserObjects = new ArrayList<cores.UserObject>();
			for (cores.UserObject userObject : userObjects) {
				List<String> userCategories = userObject.getCategories();
				for (String category : userCategories)
					if (category.equals(selectedCategory))
						categoryUserObjects.add(userObject);
			}
			userObjects = categoryUserObjects;
		}
		if (selectedType.equals("PERSON"))
			for (cores.UserObject userObject : userObjects) {
				Person person = (Person) userObject;
				String balance = ValueFormatter.formatMoney(person.getBalance());
				if (person.getBalance() > 0)
					balance = "+" + balance;
				dtm.addRow(new Object[] { person.getFirstName(), person.getLastName(),
						balance, person.getPhone(), person.getEmail(),
						person.getNationality() });
			}
		else {
			for (cores.UserObject userObject : userObjects) {
				Organization organization = (Organization) userObject;
				String balance = ValueFormatter.formatMoney(organization.getBalance());
				if (organization.getBalance() > 0)
					balance = "+" + balance;
				dtm.addRow(new Object[] { organization.getName(), organization.getContactName(),
						balance, organization.getPhone(),
						organization.getEmail(), organization.getLocation() });
			}
		}
	}

	/**
	 * Create the panel.
	 */
	public User(Main frame, cards.Transaction card1) {
		this.mainFrame = frame;
		setBackground(_Settings.backgroundColor);
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(new MigLayout("", "[150][10][150][10][150][10][150][grow]", "[50][10][20][30][10][grow][10][50]"));

		cardTransaction = card1;

		JButton btnAddCategory = new JButton("ADD CATEGORY");
		btnAddCategory.setBackground(_Settings.backgroundColor);
		btnAddCategory.setForeground(_Settings.labelColor);
		btnAddCategory.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnAddCategory.setFont(new Font("Arial", Font.BOLD, 14));
		btnAddCategory.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub

				frames.UserCategory frameUC = new frames.UserCategory(mainFrame, null);
				frameUC.addWindowListener(new WindowAdapter() {

					@Override
					public void windowClosed(WindowEvent e) {
						// TODO Auto-generated method stub

						updateOptions();
						updateTable();
						cardTransaction.updateOptions();
					}
				});
			}
		});

		JButton btnAddUser = new JButton("ADD USER");
		btnAddUser.setBackground(_Settings.backgroundColor);
		btnAddUser.setForeground(_Settings.labelColor);
		btnAddUser.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnAddUser.setFont(new Font("Arial", Font.BOLD, 14));
		btnAddUser.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub

				frames.UserObject frameUserObject = new frames.UserObject(mainFrame, null);
				frameUserObject.addWindowListener(new WindowAdapter() {

					@Override
					public void windowClosed(WindowEvent e) {
						// TODO Auto-generated method stub

						updateOptions();
						updateTable();
						cardTransaction.updateOptions();
					}
				});
			}
		});
		add(btnAddUser, "cell 0 0,grow");
		add(btnAddCategory, "cell 2 0,grow");

		JButton btnPrintList = new JButton("PRINT LIST");
		btnPrintList.setBackground(_Settings.backgroundColor);
		btnPrintList.setForeground(_Settings.labelColor);
		btnPrintList.setFont(new Font("Arial", Font.BOLD, 14));
		btnPrintList.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnPrintList.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (ReportFacade.printUserList((String) cbType.getSelectedItem(),
						(String) cbCategory.getSelectedItem()))
					LOGGER.Activity.log("User List", LOGGER.CREATE);
			}
		});
		add(btnPrintList, "cell 4 0,grow");

		JLabel lblUser = new JLabel("User Type");
		lblUser.setFont(new Font("Arial", Font.BOLD, 15));
		lblUser.setHorizontalAlignment(SwingConstants.CENTER);
		lblUser.setBorder(null);
		lblUser.setBackground(null);
		lblUser.setForeground(_Settings.labelColor);
		add(lblUser, "cell 0 2,grow");

		JLabel lblCategory = new JLabel("User Category");
		lblCategory.setHorizontalAlignment(SwingConstants.CENTER);
		lblCategory.setFont(new Font("Arial", Font.BOLD, 15));
		lblCategory.setBorder(null);
		lblCategory.setBackground(null);
		lblCategory.setForeground(_Settings.labelColor);
		add(lblCategory, "cell 2 2,grow");

		UserCategories categories = (UserCategories) DatabaseFacade.getDatabase("UserCategories");
		SortedComboBoxModel<String> scbmCategory = new SortedComboBoxModel<String>();
		MyComboBoxRenderer mcbrCategory = new MyComboBoxRenderer();
		List<String> ttCategory = new ArrayList<String>();
		List<String> categoryList = categories.getList();
		cbCategory = new JComboBox<String>(scbmCategory);
		cbCategory.setRenderer(mcbrCategory);
		cbCategory.setFont(new Font("Century Gothic", Font.BOLD, 14));
		cbCategory.setBorder(null);
		cbCategory.setForeground(Color.DARK_GRAY);
		cbCategory.addItem("ANY");
		for (String category : categoryList) {
			scbmCategory.addElement(category);
			ttCategory.add(category);
		}
		ttCategory.sort(null);
		ttCategory.add(0, "ANY");
		mcbrCategory.setTooltips(ttCategory);
		cbCategory.setSelectedIndex(0);
		cbCategory.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if ((e.getStateChange() == ItemEvent.SELECTED)) {
					updateTable();
				}
			}
		});
		
				JButton btnRefresh = new JButton("REFRESH");
				btnRefresh.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						resetSelections();
					}
				});
				btnRefresh.setBackground(_Settings.backgroundColor);
				btnRefresh.setForeground(_Settings.labelColor);
				btnRefresh.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
				btnRefresh.setFont(new Font("Arial", Font.BOLD, 14));
				add(btnRefresh, "cell 4 2 1 2,grow");
		add(cbCategory, "cell 2 3,grow");

		cbType = new JComboBox<String>();
		cbType.setFont(new Font("Century Gothic", Font.BOLD, 14));
		cbType.setBorder(null);
		cbType.setForeground(Color.DARK_GRAY);
		cbType.addItem("PERSON");
		cbType.addItem("ORGANIZATION");
		cbType.setSelectedIndex(0);
		cbType.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if ((e.getStateChange() == ItemEvent.SELECTED)) {
					updateTable();
				}
			}
		});
		add(cbType, "cell 0 3,grow");

		JScrollPane spUser = new JScrollPane();
		spUser.setBackground(null);
		spUser.setOpaque(false);
		spUser.getViewport().setOpaque(false);
		add(spUser, "cell 0 5 8 1,grow");

		dtm = new DefaultTableModel() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

		};
		dtm.setColumnIdentifiers(personHeaders);
		tblUser = new JTable(dtm);
		tblUser.setGridColor(_Settings.labelColor);
		tblUser.getTableHeader().setForeground(_Settings.labelColor);
		tblUser.getTableHeader().setBackground(_Settings.backgroundColor);
		tblUser.getTableHeader().setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		tblUser.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
		tblUser.setFont(new Font("Century Gothic", Font.BOLD, 16));
		tblUser.setBackground(_Settings.backgroundColor);
		tblUser.setForeground(_Settings.textFieldColor);
		tblUser.setRowHeight(30);
		tblUser.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				if (e.getClickCount() == 2) {
					int row = tblUser.getSelectedRow();
					if (row >= 0) {
						String name1 = (String) tblUser.getValueAt(row, 0);
						String name2 = (String) tblUser.getValueAt(row, 1);
						UserObjects db = (UserObjects) DatabaseFacade.getDatabase("UserObjects");
						cores.UserObject userObject = db.get(db.find(name1, name2));

						frames.UserObject frameUserObject = new frames.UserObject(mainFrame, userObject);
						frameUserObject.addWindowListener(new WindowAdapter() {

							@Override
							public void windowClosed(WindowEvent arg0) {
								// TODO Auto-generated method stub

								updateTable();
							}
						});
					}
				}
			}
		});
		spUser.setViewportView(tblUser);
		updateTable();
	}

}

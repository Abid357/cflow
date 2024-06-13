package frames;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cores.Organization;
import cores.Person;
import databases.Database;
import databases.UserCategories;
import databases.UserObjects;
import globals.DatabaseFacade;
import globals.LOGGER;
import globals.ValueFormatter;
import globals._Settings;
import net.miginfocom.swing.MigLayout;

public class UserObject extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtName1;
	private JTextField txtName2;
	private JTextField txtPhoneCode;
	private JTextField txtPhone;
	private JTextField txtEmail;
	private JTextArea txtAddress;
	private JButton btnSave;
	private JButton btnClose;
	private JComboBox<String> cbNationality;
	private JComboBox<String> cbLocation;
	private JList<String> listCategory;
	private JTextField txtStatus;
	private JToggleButton tglbtnOrganization;
	private JToggleButton tglbtnPerson;
	private JLabel lblName1;
	private JLabel lblName2;
	private boolean isName1Empty = true, isPhoneEmpty = true, isAmountValid = true, isCategorySelected, isDuplicateKey,
			isPhoneValid;
	private cores.UserObject userObject;
	private JLabel lblBalance;
	private JTextField txtBalance;

	private void reset() {
		txtBalance.setText("0.00");
		txtName1.setText("");
		txtName2.setText("");
		txtPhone.setText("");
		txtPhone.setEnabled(false);
		txtPhoneCode.setText("");
		txtEmail.setText("");
		txtAddress.setText("");
		txtStatus.setText("");
		btnSave.setEnabled(false);
		isName1Empty = isAmountValid = isPhoneEmpty = true;
		isCategorySelected = isDuplicateKey = isPhoneValid = false;
		cbNationality.setSelectedIndex(-1);
		cbLocation.setSelectedIndex(-1);
		listCategory.clearSelection();
	}

	private boolean isAmountValid() {
		return Pattern.compile("(\\-?[0-9]{1,3}(\\,)?)*(\\.[0-9]*)?").matcher(txtBalance.getText()).matches();
	}

	private boolean isPhoneValid() {
		return Pattern.compile("\\d{5,15}").matcher(txtPhone.getText()).matches();
	}

	private boolean isDuplicateKey() {
		Database<?> db = DatabaseFacade.getDatabase("UserObjects");
		int index = db.find(txtName1.getText(), txtName2.getText());
		if (index == -1) {
			txtStatus.setText("");
			return false;
		} else {
			txtStatus.setText("Record already exists!");
			return true;
		}
	}

	private void populatePhoneNumber(String location, String phone) {
		int index = -1;
		for (int i = 0; i < _Settings.countries.size(); i++)
			if (_Settings.countries.get(i).equals(location))
				index = i;
		if (index != -1) {
			String phoneCode = _Settings.phoneCodes.get(index);
			String trimmedPhone = phone.split(phoneCode)[1];
			txtPhone.setText(trimmedPhone);
			txtPhoneCode.setText("+" + phoneCode);
		}
	}

	private void populateCategorySelections(List<String> categories) {
		List<String> listElements = new ArrayList<String>();
		for (int i = 0; i < listCategory.getModel().getSize(); i++)
			listElements.add(listCategory.getModel().getElementAt(i));
		List<Integer> indices = new ArrayList<Integer>();
		for (String category : categories)
			for (int i = 0; i < listElements.size(); i++)
				if (listElements.get(i).equals(category))
					indices.add(i);
		int[] ret = new int[indices.size()];
		Iterator<Integer> iterator = indices.iterator();
		for (int i = 0; i < ret.length; i++)
			ret[i] = iterator.next().intValue();
		listCategory.setSelectedIndices(ret);
	}

	private void populateFrameWith(Person person) {
		tglbtnOrganization.setEnabled(false);
		txtName1.setText(person.getFirstName());
		txtName2.setText(person.getLastName());
		txtEmail.setText(person.getEmail());
		txtAddress.setText(person.getAddress());
		cbNationality.setSelectedItem(person.getNationality());
		cbLocation.setSelectedItem(person.getLocation());
		txtBalance.setText(ValueFormatter.formatMoney(person.getBalance()));
		populateCategorySelections(person.getCategories());
		populatePhoneNumber(person.getLocation(), person.getPhone());
		isName1Empty = false;
		isPhoneEmpty = false;
		isPhoneValid = true;
		isAmountValid = true;
		isCategorySelected = true;
		validateButton();
	}

	private void populateFrameWith(Organization organization) {
		tglbtnPerson.setEnabled(false);
		tglbtnOrganization.setSelected(true);
		lblName1.setText("Name");
		lblName2.setText("Contact Name");
		cbNationality.setEnabled(false);
		txtName1.setText(organization.getName());
		txtName2.setText(organization.getContactName());
		txtEmail.setText(organization.getEmail());
		txtAddress.setText(organization.getAddress());
		cbLocation.setSelectedItem(organization.getLocation());
		txtBalance.setText(ValueFormatter.formatMoney(organization.getBalance()));
		populateCategorySelections(organization.getCategories());
		populatePhoneNumber(organization.getLocation(), organization.getPhone());
		isName1Empty = false;
		isPhoneEmpty = false;
		isPhoneValid = true;
		isAmountValid = true;
		isCategorySelected = true;
		validateButton();
	}

	private boolean isFieldEmpty(JTextField field) {
		if (field.getText().equals("")) {
			return true;
		} else {
			return false;
		}
	}

	private void validateButton() {
		if (isName1Empty)
			txtStatus.setText("Name cannot be empty!");
		else if (!isAmountValid)
			txtStatus.setText("Invalid amount value!");
		else if (isPhoneEmpty)
			txtStatus.setText("Phone cannot be empty!");
		else if (!isPhoneValid)
			txtStatus.setText("Invalid phone number!");
		else if (!isCategorySelected)
			txtStatus.setText("Select at least 1 category!");
		else if (isDuplicateKey)
			txtStatus.setText("Record already exists!");
		else
			txtStatus.setText("");
		if (!isName1Empty && !isPhoneEmpty && isCategorySelected && isAmountValid && isPhoneValid && !isDuplicateKey)
			btnSave.setEnabled(true);
		else
			btnSave.setEnabled(false);
	}

	/**
	 * Create the frame.
	 */
	@SuppressWarnings("unchecked")
	public UserObject(Window owner, cores.UserObject userObject) {
		super(owner, "User", Dialog.ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		setBounds(100, 100, 850, 700);
		contentPane = new JPanel();
		contentPane.setBackground(_Settings.backgroundColor);
		contentPane.setBorder(null);
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("",
				"[10,grow][120,grow][10][20,grow][10][150,grow][60,grow][20,grow][10][80,grow][20,grow][20,grow][10][60,grow][40][10][150][10]",
				"[60,grow][10][40][10][40][10][40][10][40][20][10][20][20][10][40][10][20][10][60,grow]"));

		this.userObject = userObject;

		lblName1 = new JLabel("First Name*");
		lblName1.setBorder(null);
		lblName1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblName1.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblName1.setForeground(_Settings.labelColor);
		contentPane.add(lblName1, "cell 0 2 2 1,grow");

		txtName1 = new JTextField();
		txtName1.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtName1.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtName1.setBackground(null);
		txtName1.setForeground(_Settings.textFieldColor);
		txtName1.setColumns(10);
		txtName1.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				if (userObject == null)
					isDuplicateKey = isDuplicateKey();
				isName1Empty = isFieldEmpty(txtName1);
				validateButton();
			}

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub

			}
		});
		contentPane.add(txtName1, "cell 3 2 5 1,grow");

		lblName2 = new JLabel("Last Name*");
		lblName2.setBorder(null);
		lblName2.setHorizontalAlignment(SwingConstants.RIGHT);
		lblName2.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblName2.setForeground(_Settings.labelColor);
		contentPane.add(lblName2, "cell 9 2 3 1,grow");

		txtName2 = new JTextField();
		txtName2.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtName2.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtName2.setBackground(null);
		txtName2.setForeground(_Settings.textFieldColor);
		txtName2.setColumns(10);
		txtName2.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				if (userObject == null)
					isDuplicateKey = isDuplicateKey();
				validateButton();
			}

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub

			}
		});
		contentPane.add(txtName2, "cell 13 2 4 1,grow");

		JLabel lblNationality = new JLabel("Nationality");
		lblNationality.setBorder(null);
		lblNationality.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNationality.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblNationality.setForeground(_Settings.labelColor);
		contentPane.add(lblNationality, "cell 0 4 2 1,grow");

		cbNationality = new JComboBox<String>();
		cbNationality.setFont(new Font("Century Gothic", Font.BOLD, 17));
		cbNationality.setForeground(Color.DARK_GRAY);
		for (String country : _Settings.countries)
			cbNationality.addItem(country);
		cbNationality.setSelectedIndex(-1);
		contentPane.add(cbNationality, "cell 3 4 5 1,grow");

		JLabel lblAddress = new JLabel("Address");
		lblAddress.setBorder(null);
		lblAddress.setHorizontalAlignment(SwingConstants.RIGHT);
		lblAddress.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblAddress.setForeground(_Settings.labelColor);
		contentPane.add(lblAddress, "cell 0 8 2 1,grow");

		txtAddress = new JTextArea();
		txtAddress.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtAddress.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtAddress.setBackground(_Settings.backgroundColor);
		txtAddress.setForeground(_Settings.textFieldColor);
		txtAddress.setLineWrap(true);

		JScrollPane spAddress = new JScrollPane(txtAddress);
		spAddress.setBorder(null);
		contentPane.add(spAddress, "cell 3 8 5 2,grow");

		JLabel lblEmail = new JLabel("Email");
		lblEmail.setBorder(null);
		lblEmail.setHorizontalAlignment(SwingConstants.RIGHT);
		lblEmail.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblEmail.setForeground(_Settings.labelColor);
		contentPane.add(lblEmail, "cell 9 8 3 1,grow");

		txtEmail = new JTextField();
		txtEmail.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtEmail.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtEmail.setBackground(null);
		txtEmail.setForeground(_Settings.textFieldColor);
		contentPane.add(txtEmail, "cell 13 8 4 1,grow");

		txtPhoneCode = new JTextField();
		txtPhoneCode.setEditable(false);
		txtPhoneCode.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtPhoneCode.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtPhoneCode.setBackground(null);
		txtPhoneCode.setForeground(_Settings.textFieldColor);
		txtPhoneCode.setColumns(10);
		contentPane.add(txtPhoneCode, "cell 13 6,grow");

		JLabel lblPhone = new JLabel("Phone*");
		lblPhone.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPhone.setForeground(_Settings.labelColor);
		lblPhone.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblPhone.setBorder(null);
		contentPane.add(lblPhone, "cell 9 6 3 1,grow");

		JLabel lblLocation = new JLabel("Location");
		lblLocation.setHorizontalAlignment(SwingConstants.RIGHT);
		lblLocation.setForeground(_Settings.labelColor);
		lblLocation.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblLocation.setBorder(null);
		contentPane.add(lblLocation, "cell 0 6 2 1,grow");

		cbLocation = new JComboBox<String>();
		for (String country : _Settings.countries)
			cbLocation.addItem(country);
		cbLocation.setFont(new Font("Century Gothic", Font.BOLD, 17));
		cbLocation.setForeground(Color.DARK_GRAY);
		cbLocation.setSelectedIndex(-1);
		cbLocation.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String country = (String) e.getItem();
					int index = _Settings.countries.indexOf(country);
					txtPhoneCode.setText("+" + _Settings.phoneCodes.get(index));
					txtPhone.setEnabled(true);
					txtPhone.setText("");
				}
			}
		});
		contentPane.add(cbLocation, "cell 3 6 5 1,grow");

		txtPhone = new JTextField();
		txtPhone.setEnabled(false);
		txtPhone.setForeground(_Settings.textFieldColor);
		txtPhone.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtPhone.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtPhone.setBackground(null);
		txtPhone.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				isPhoneEmpty = isFieldEmpty(txtPhone);
				isPhoneValid = isPhoneValid();
				validateButton();
			}

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub

			}
		});
		contentPane.add(txtPhone, "cell 14 6 3 1,grow");

		tglbtnPerson = new JToggleButton("PERSON");
		tglbtnPerson.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		tglbtnPerson.setFont(new Font("Arial Black", Font.PLAIN, 19));
		tglbtnPerson.setBackground(_Settings.backgroundColor);
		tglbtnPerson.setForeground(_Settings.labelColor);
		tglbtnPerson.setSelected(true);
		tglbtnPerson.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				lblName1.setText("First Name*");
				lblName2.setText("Last Name*");
				cbNationality.setEnabled(true);
			}
		});
		contentPane.add(tglbtnPerson, "cell 5 0 3 1,grow");

		tglbtnOrganization = new JToggleButton("ORGANIZATION");
		tglbtnOrganization.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		tglbtnOrganization.setFont(new Font("Arial Black", Font.PLAIN, 19));
		tglbtnOrganization.setBackground(_Settings.backgroundColor);
		tglbtnOrganization.setForeground(_Settings.labelColor);
		tglbtnOrganization.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				lblName1.setText("Name*");
				lblName2.setText("Contact Name*");
				cbNationality.setEnabled(false);
			}
		});
		contentPane.add(tglbtnOrganization, "cell 9 0 6 1,grow");

		ButtonGroup bg = new ButtonGroup();
		bg.add(tglbtnPerson);
		bg.add(tglbtnOrganization);

		JButton btnReset = new JButton("RESET");
		btnReset.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnReset.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnReset.setBackground(_Settings.backgroundColor);
		btnReset.setForeground(_Settings.labelColor);
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});

		JLabel lblCategoryHint = new JLabel("Hold Ctrl for multiple selections");
		lblCategoryHint.setFont(new Font("Arial Black", Font.ITALIC, 15));
		lblCategoryHint.setBorder(null);
		lblCategoryHint.setForeground(_Settings.labelColor);
		contentPane.add(lblCategoryHint, "cell 11 11 6 3,growx,aligny center");

		lblBalance = new JLabel("Initial Balance");
		lblBalance.setHorizontalAlignment(SwingConstants.RIGHT);
		lblBalance.setForeground(_Settings.labelColor);
		lblBalance.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblBalance.setBorder(null);
		contentPane.add(lblBalance, "cell 9 14 3 1,grow");

		txtBalance = new JTextField();
		txtBalance.setText("0.00");
		txtBalance.setHorizontalAlignment(SwingConstants.CENTER);
		txtBalance.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtBalance.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtBalance.setBackground(null);
		txtBalance.setForeground(_Settings.textFieldColor);
		txtBalance.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				isAmountValid = isAmountValid();
				if (isAmountValid) {
					double amount = ValueFormatter.parseMoney(txtBalance.getText());
					txtBalance.setText(ValueFormatter.formatMoney(amount));
				}
				validateButton();
			}

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				txtBalance.selectAll();
			}
		});
		contentPane.add(txtBalance, "cell 13 14 3 1,grow");

		txtStatus = new JTextField();
		txtStatus.setEditable(false);
		txtStatus.setBackground(null);
		txtStatus.setForeground(_Settings.labelColor);
		txtStatus.setBorder(null);
		txtStatus.setHorizontalAlignment(SwingConstants.RIGHT);
		txtStatus.setFont(new Font("Arial Black", Font.PLAIN, 15));
		txtStatus.setColumns(10);
		contentPane.add(txtStatus, "cell 11 16 6 1,growx,aligny bottom");
		contentPane.add(btnReset, "cell 1 18 3 1,aligny center,grow");

		JLabel lblCategory = new JLabel("<html><div style='text-align: center;'>User</div><div style='text-align: center;'>Category</div></html>");
		lblCategory.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCategory.setForeground(_Settings.labelColor);
		lblCategory.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblCategory.setBorder(null);
		contentPane.add(lblCategory, "cell 1 11 1 2,alignx right,growy");

		listCategory = new JList<String>();
		listCategory.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		listCategory.setFont(new Font("Century Gothic", Font.BOLD, 21));
		listCategory.setBackground(_Settings.backgroundColor);
		listCategory.setForeground(_Settings.textFieldColor);
		UserCategories userCategories = (UserCategories) DatabaseFacade.getDatabase("UserCategories");
		DefaultListModel<String> dlm = new DefaultListModel<String>();
		for (String category : userCategories.getList())
			dlm.addElement(category);
		listCategory.setModel(dlm);
		listCategory.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				ListSelectionModel lsm = ((JList<String>) e.getSource()).getSelectionModel();
				if (lsm.isSelectionEmpty())
					isCategorySelected = false;
				else
					isCategorySelected = true;
				validateButton();
			}
		});

		JScrollPane spCategory = new JScrollPane(listCategory);
		spCategory.setBorder(null);
		contentPane.add(spCategory, "cell 3 11 5 6,grow");

		JButton btnAdd = new JButton("ADD");
		btnAdd.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnAdd.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnAdd.setBackground(_Settings.backgroundColor);
		btnAdd.setForeground(_Settings.labelColor);
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new frames.UserCategory(owner, (DefaultListModel<String>) listCategory.getModel());
			}
		});
		contentPane.add(btnAdd, "cell 9 11 1 3,grow");

		btnSave = new JButton("SAVE");
		btnSave.setEnabled(false);
		btnSave.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnSave.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnSave.setBackground(_Settings.backgroundColor);
		btnSave.setForeground(_Settings.labelColor);
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name1 = txtName1.getText().trim();
				String name2 = txtName2.getText().trim();
				String phone = txtPhoneCode.getText() + txtPhone.getText().trim();
				String email = txtEmail.getText().trim();
				String address = txtAddress.getText().trim();
				List<String> categories = listCategory.getSelectedValuesList();
				String location = (String) cbLocation.getSelectedItem();
				String nationality = "";
				double balance = ValueFormatter.parseMoney(txtBalance.getText());
				if (cbNationality.getSelectedIndex() != -1)
					nationality = (String) cbNationality.getSelectedItem();
				UserObjects userObjects = (UserObjects) DatabaseFacade.getDatabase("UserObjects");
				if (userObject == null) {
					if (tglbtnPerson.isSelected()) {
						if (userObjects.add(name1, name2, nationality, location, phone, email, address, balance,
								categories)) {
							reset();
							txtStatus.setText("Record saved!");
							LOGGER.Activity.log("User", LOGGER.CREATE,
									ValueFormatter.formatUserObject(userObjects.get(userObjects.getList().size() - 1)));
						} else
							txtStatus.setText("An error occurred!");
					} else {
						if (userObjects.add(name1, name2, location, phone, email, address, balance, categories)) {
							reset();
							txtStatus.setText("Record saved!");
							LOGGER.Activity.log("User", LOGGER.CREATE,
									ValueFormatter.formatUserObject(userObjects.get(userObjects.getList().size() - 1)));
						} else
							txtStatus.setText("An error occurred!");
					}
				} else {
					if (userObject instanceof Person) {
						Person person = (Person) userObject;
						String oldUserObject = ValueFormatter.formatUserObject(person);
						person.setFirstName(name1);
						person.setLastName(name2);
						txtStatus.setText("Record updated!");
						LOGGER.Activity.log("User", LOGGER.UPDATE,
								oldUserObject + " -> " + ValueFormatter.formatUserObject(userObject));
					} else {
						Organization organization = (Organization) userObject;
						String oldUserObject = ValueFormatter.formatUserObject(organization);
						organization.setName(name1);
						organization.setContactName(name2);
						txtStatus.setText("Record updated!");
						LOGGER.Activity.log("User", LOGGER.UPDATE,
								oldUserObject + " -> " + ValueFormatter.formatUserObject(userObject));
					}
				}
			}
		});
		contentPane.add(btnSave, "cell 16 18,aligny center,grow");

		btnClose = new JButton("CLOSE");
		btnClose.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnClose.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnClose.setBackground(_Settings.backgroundColor);
		btnClose.setForeground(_Settings.labelColor);
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		contentPane.add(btnClose, "cell 10 18 5 1,aligny center,grow");

		if (this.userObject != null)
			if (this.userObject.getClass().getSimpleName().equals("Person"))
				populateFrameWith((Person) this.userObject);
			else
				populateFrameWith((Organization) this.userObject);

		getRootPane().setDefaultButton(btnSave);
		getRootPane().registerKeyboardAction(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				btnClose.doClick();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		pack();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}

}

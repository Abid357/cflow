package frames;

import java.awt.Dialog;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import cores.Material;
import cores.Product;
import cores.Stock;
import databases.Items;
import globals.DatabaseFacade;
import globals.ItemFacade;
import globals.LOGGER;
import globals.ValueFormatter;
import globals._Settings;
import helpers.ItemType;
import net.miginfocom.swing.MigLayout;

public class Item extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JLabel lblName;
	private boolean isNameEmpty = true, isUnitEmpty, isUnitValid = true, isConversionValid = true;
	private cores.Item item;
	private JButton btnNew, btnSave;
	private JTextField txtName, txtStatus;
	private JLabel lblPrice;
	private JTextField txtPrice;
	private JScrollPane spItem;
	private JTable tblItem;
	private JButton btnAdd;
	private JButton btnDelete;
	private DefaultTableModel dtm;
	private List<Stock> items;
	private JButton btnClose;
	private JLabel lblId;
	private JTextField txtId;
	private JToggleButton tglbtnProduct;
	private JToggleButton tglbtnMaterial;
	private JToggleButton tglbtnService;
	private JLabel lblUnit;
	private JTextField txtUnit;
	private JLabel lblPriceUnit;
	private JLabel lblConversion;
	private JTextField txtConversion;
	private JLabel lblConversionUnit;
	private JLabel lblPc;
	private JLabel lblEquals;
	private JTextField txtConversionUnit;
	private JLabel lblDetails;
	private JScrollPane spDetails;
	private JTextArea txtDetails;
	private JLabel lblBarcode;
	private JTextField txtBarcode;
	private JTextField txtNote;
	private Items db;
	private String[] columnHeaders = { "ID", "NAME", "TYPE", "QUANTITY", "UNIT", "PRICE/UNIT", "VALUE" };

	public void setStatus(String status) {
		txtStatus.setText(status);
	}

	public cores.Item getItem() {
		return item;
	}

	public void setItem(cores.Item item) {
		this.item = item;
	}

	public void setItems(List<Stock> items) {
		this.items = items;
	}

	public List<Stock> getItems() {
		return items;
	}

	public JDialog getThis() {
		return this;
	}

	private boolean isValidNumeric(JTextField txt) {
		return Pattern.compile("([0-9]{1,3}(\\,)?)*(\\.[0-9]*)?").matcher(txt.getText()).matches();
	}

	private void validateButton() {
		if (isNameEmpty)
			txtStatus.setText("Enter an item name!");
		else if (isUnitEmpty)
			txtStatus.setText("Enter a unit!");
		else if (!isUnitValid)
			txtStatus.setText("Invalid unit!");
		else if (!isConversionValid)
			txtStatus.setText("Invalid conversion!");
		else
			txtStatus.setText("");
		btnSave.setEnabled(!isNameEmpty && !isUnitEmpty && isUnitValid && isConversionValid);
	}

	private void updateTable() {
		dtm.setRowCount(0);
		for (Stock stock : items) {
			cores.Item item = stock.getItem();
			dtm.addRow(new Object[] { ValueFormatter.formatItem(item), item.getName(), item.getType().toString(),
					ValueFormatter.formatQuantity(stock.getQuantity()), item.getUnit(),
					ValueFormatter.formatMoney(item.getPrice()),
					ValueFormatter.formatMoney(stock.getPrice() * stock.getQuantity()) });
		}
	}

	private boolean isUnitValid() {
		return Pattern.compile("[a-zA-z]+").matcher(txtUnit.getText()).matches();
	}

	private void populateFrame() {
		if (item != null) {
			setFormEnabled(false);
			txtName.setText(item.getName());
			txtUnit.setText(item.getUnit());
			txtPrice.setText(ValueFormatter.formatMoney(item.getPrice()));
			txtConversion.setText(ValueFormatter.formatQuantity(item.getConversion()));
			txtDetails.setText(item.getDetails());
			isConversionValid = isUnitValid = true;
			isNameEmpty = isUnitEmpty = false;
			if (item.getType().equals(ItemType.PRODUCT)) {
				tglbtnProduct.setSelected(true);
				txtBarcode.setText(((Product) item).getBarcode());
				items = ((Product) item).getComponents();
				txtId.setText("P-" + item.getId());
			} else if (item.getType().equals(ItemType.MATERIAL)) {
				tglbtnMaterial.setSelected(true);
				items = ((Material) item).getComponents();
				txtId.setText("M-" + item.getId());
			} else {
				tglbtnService.setSelected(true);
				txtId.setText("S-" + item.getId());
			}
			txtStatus.setText("");
			updateTable();
		}
	}

	private void setFormEnabled(boolean isEnabled) {
		btnSave.setEnabled(isEnabled);
		txtName.setEnabled(isEnabled);
		txtPrice.setEnabled(isEnabled);
		txtUnit.setEnabled(isEnabled);
		txtConversion.setEnabled(isEnabled);
		tblItem.setEnabled(isEnabled);
		btnAdd.setEnabled(isEnabled);
		btnDelete.setEnabled(isEnabled);
		tglbtnProduct.setEnabled(isEnabled);
		tglbtnMaterial.setEnabled(isEnabled);
		tglbtnService.setEnabled(isEnabled);
		txtBarcode.setEnabled(isEnabled);
		txtDetails.setEnabled(isEnabled);
	}

	private void reset() {
		txtName.setText("");
		txtPrice.setText("0.00");
		txtDetails.setText("");
		tglbtnProduct.setSelected(true);
		tglbtnMaterial.setSelected(false);
		tglbtnService.setSelected(false);
		txtId.setText("P-" + (db.maxID() + 1));
		txtUnit.setText("pc");
		txtConversionUnit.setText("pc");
		txtConversion.setText("1");
		lblPriceUnit.setText("/ pc");
		items = new ArrayList<Stock>();
		updateTable();
		isNameEmpty = isConversionValid = isUnitValid = true;
		isUnitEmpty = false;
		txtStatus.setText("");
		btnSave.setEnabled(false);
	}

	private double calculateNetAsset() {
		double sum = 0;
		for (int i = 0; i < tblItem.getRowCount(); i++)
			sum += ValueFormatter.parseMoney((String) dtm.getValueAt(i, 6));
		return sum;
	}

	/**
	 * Create the frame.
	 */
	public Item(Window owner, cores.Item item) {
		super(owner, "Item - Product, Material or Service", Dialog.ModalityType.DOCUMENT_MODAL);
		setTitle("Item");
		setResizable(false);
		setBounds(100, 100, 850, 700);
		contentPane = new JPanel();
		contentPane.setBackground(_Settings.backgroundColor);
		contentPane.setBorder(null);
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[10][60,grow][10][50,grow][10][20][180][10][60][10][110,grow][10][30,grow][50][10][50][10][10][40,grow][10][30][50][10][60][10][80][10]", "[10][20][30][30][10][40][10][40,grow][10][40][10][:250:250,grow][10][60][10][20,grow][10][60][10]"));

		this.item = item;
		items = new ArrayList<Stock>();


		UIManager.put("ComboBox.disabledForeground", _Settings.labelColor);
		UIManager.put("TextField.inactiveForeground", _Settings.labelColor);
		UIManager.put("TextArea.inactiveForeground", _Settings.labelColor);

		lblId = new JLabel("Item ID");
		lblId.setHorizontalAlignment(SwingConstants.CENTER);
		lblId.setForeground(_Settings.labelColor);
		lblId.setFont(new Font("Arial Black", Font.PLAIN, 11));
		lblId.setBorder(null);
		contentPane.add(lblId, "cell 1 0 3 1,grow");

		btnNew = new JButton("NEW");
		btnNew.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnNew.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnNew.setBackground(_Settings.backgroundColor);
		btnNew.setForeground(_Settings.labelColor);
		btnNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setFormEnabled(true);
				reset();
			}
		});

		tglbtnProduct = new JToggleButton("PRODUCT");
		tglbtnProduct.setSelected(true);
		tglbtnProduct.setForeground(_Settings.labelColor);
		tglbtnProduct.setFont(new Font("Arial Black", Font.PLAIN, 19));
		tglbtnProduct.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		tglbtnProduct.setBackground(_Settings.backgroundColor);
		tglbtnProduct.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				txtBarcode.setEnabled(true);
				tblItem.setEnabled(true);
				btnAdd.setEnabled(true);
				btnDelete.setEnabled(true);
				txtUnit.setEnabled(true);
				txtConversion.setEnabled(true);
				txtId.setText("P-" + (db.maxID() + 1));
			}
		});
		contentPane.add(tglbtnProduct, "cell 5 2 2 2,grow");

		tglbtnMaterial = new JToggleButton("MATERIAL");
		tglbtnMaterial.setForeground(_Settings.labelColor);
		tglbtnMaterial.setFont(new Font("Arial Black", Font.PLAIN, 19));
		tglbtnMaterial.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		tglbtnMaterial.setBackground(_Settings.backgroundColor);
		tglbtnMaterial.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				txtBarcode.setEnabled(false);
				txtBarcode.setText("");
				tblItem.setEnabled(true);
				btnAdd.setEnabled(true);
				btnDelete.setEnabled(true);
				txtUnit.setEnabled(true);
				txtConversion.setEnabled(true);
				txtId.setText("M-" + (db.maxID() + 1));
			}
		});
		contentPane.add(tglbtnMaterial, "cell 8 2 3 2,grow");

		tglbtnService = new JToggleButton("SERVICE");
		tglbtnService.setForeground(_Settings.labelColor);
		tglbtnService.setFont(new Font("Arial Black", Font.PLAIN, 19));
		tglbtnService.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		tglbtnService.setBackground(_Settings.backgroundColor);
		tglbtnService.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				txtBarcode.setEnabled(false);
				txtBarcode.setText("");
				tblItem.setEnabled(false);
				btnAdd.setEnabled(false);
				btnDelete.setEnabled(false);
				txtUnit.setText("pc");
				txtConversion.setText("1");
				txtUnit.setEnabled(false);
				txtConversion.setEnabled(false);
				txtId.setText("S-" + (db.maxID() + 1));
			}
		});
		contentPane.add(tglbtnService, "cell 12 2 7 2,grow");

		ButtonGroup bg = new ButtonGroup();
		bg.add(tglbtnProduct);
		bg.add(tglbtnMaterial);
		bg.add(tglbtnService);

		db = (Items) DatabaseFacade.getDatabase("Items");
		txtId = new JTextField("P-" + (db.maxID() + 1));
		txtId.setHorizontalAlignment(SwingConstants.CENTER);
		txtId.setForeground(_Settings.textFieldColor);
		txtId.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtId.setEditable(false);
		txtId.setColumns(5);
		txtId.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtId.setBackground(_Settings.backgroundColor);
		contentPane.add(txtId, "cell 1 1 3 3,grow");

		lblName = new JLabel("Name");
		lblName.setHorizontalAlignment(SwingConstants.RIGHT);
		lblName.setForeground(_Settings.labelColor);
		lblName.setBackground(null);
		lblName.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblName.setBorder(null);
		contentPane.add(lblName, "cell 1 5,grow");

		txtName = new JTextField();
		txtName.setForeground(_Settings.textFieldColor);
		txtName.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtName.setColumns(1);
		txtName.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtName.setBackground(_Settings.backgroundColor);
		txtName.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				isNameEmpty = txtName.getText().isEmpty();
				validateButton();
			}

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub

			}
		});
		contentPane.add(txtName, "cell 3 5 4 1,grow");

		lblUnit = new JLabel("Unit");
		lblUnit.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUnit.setForeground(_Settings.labelColor);
		lblUnit.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblUnit.setBorder(null);
		lblUnit.setBackground(_Settings.backgroundColor);
		contentPane.add(lblUnit, "cell 8 5,grow");

		txtUnit = new JTextField();
		txtUnit.setHorizontalAlignment(SwingConstants.CENTER);
		txtUnit.setText("pc");
		txtUnit.setForeground(_Settings.textFieldColor);
		txtUnit.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtUnit.setColumns(1);
		txtUnit.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtUnit.setBackground(_Settings.backgroundColor);
		txtUnit.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				isUnitEmpty = txtUnit.getText().isEmpty();
				if (!isUnitEmpty) {
					isUnitValid = isUnitValid();
					lblPriceUnit.setText("/ " + txtUnit.getText());
					txtConversionUnit.setText(txtUnit.getText());
				}
				validateButton();
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
		contentPane.add(txtUnit, "cell 10 5 4 1,grow");

		lblPrice = new JLabel("Price");
		lblPrice.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPrice.setForeground(_Settings.labelColor);
		lblPrice.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblPrice.setBorder(null);
		lblPrice.setBackground(_Settings.backgroundColor);
		contentPane.add(lblPrice, "cell 15 5 2 1,grow");

		spItem = new JScrollPane();
		spItem.setBackground(null);
		spItem.setOpaque(false);
		spItem.getViewport().setOpaque(false);

		txtPrice = new JTextField();
		txtPrice.setText("0.00");
		txtPrice.setHorizontalAlignment(SwingConstants.CENTER);
		txtPrice.setForeground(_Settings.textFieldColor);
		txtPrice.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtPrice.setColumns(1);
		txtPrice.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtPrice.setBackground(_Settings.backgroundColor);
		txtPrice.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				if (!isValidNumeric(txtPrice)) {
					txtPrice.setText("0.00");
					new MyOptionPane("Price must be positive number!", MyOptionPane.INFORMATION_MESSAGE);
					return;
				} else {
					double price = ValueFormatter.parseMoney(txtPrice.getText());
					txtPrice.setText(ValueFormatter.formatMoney(price));
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				txtPrice.selectAll();
			}
		});
		contentPane.add(txtPrice, "cell 18 5 6 1,grow");

		lblPriceUnit = new JLabel("/ pc");
		lblPriceUnit.setHorizontalAlignment(SwingConstants.LEFT);
		lblPriceUnit.setForeground(_Settings.labelColor);
		lblPriceUnit.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblPriceUnit.setBorder(null);
		lblPriceUnit.setBackground(_Settings.backgroundColor);
		contentPane.add(lblPriceUnit, "cell 24 5 2 1,grow");

		lblDetails = new JLabel("Details");
		lblDetails.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDetails.setForeground(_Settings.labelColor);
		lblDetails.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblDetails.setBorder(null);
		lblDetails.setBackground(_Settings.backgroundColor);
		contentPane.add(lblDetails, "cell 0 7 2 1,grow");

		txtDetails = new JTextArea();
		txtDetails.setLineWrap(true);
		txtDetails.setForeground(_Settings.textFieldColor);
		txtDetails.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtDetails.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtDetails.setBackground(_Settings.backgroundColor);

		spDetails = new JScrollPane(txtDetails);
		spDetails.setBorder(null);
		contentPane.add(spDetails, "cell 3 7 6 3,grow");

		lblConversion = new JLabel("Conversion");
		lblConversion.setHorizontalAlignment(SwingConstants.RIGHT);
		lblConversion.setForeground(_Settings.labelColor);
		lblConversion.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblConversion.setBorder(null);
		lblConversion.setBackground(_Settings.backgroundColor);
		contentPane.add(lblConversion, "cell 10 7,grow");

		lblConversionUnit = new JLabel("1");
		lblConversionUnit.setHorizontalAlignment(SwingConstants.CENTER);
		lblConversionUnit.setForeground(_Settings.labelColor);
		lblConversionUnit.setFont(new Font("Century Gothic", Font.BOLD, 21));
		lblConversionUnit.setBorder(null);
		lblConversionUnit.setBackground(_Settings.backgroundColor);
		contentPane.add(lblConversionUnit, "cell 12 7,grow");

		txtConversionUnit = new JTextField();
		txtConversionUnit.setText("pc");
		txtConversionUnit.setHorizontalAlignment(SwingConstants.CENTER);
		txtConversionUnit.setForeground(_Settings.textFieldColor);
		txtConversionUnit.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtConversionUnit.setEditable(false);
		txtConversionUnit.setColumns(1);
		txtConversionUnit.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtConversionUnit.setBackground(_Settings.backgroundColor);
		contentPane.add(txtConversionUnit, "cell 13 7 6 1,grow");

		lblEquals = new JLabel("=");
		lblEquals.setHorizontalAlignment(SwingConstants.CENTER);
		lblEquals.setForeground(_Settings.labelColor);
		lblEquals.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblEquals.setBorder(null);
		lblEquals.setBackground(_Settings.backgroundColor);
		contentPane.add(lblEquals, "cell 20 7,grow");

		txtConversion = new JTextField();
		txtConversion.setText("1");
		txtConversion.setHorizontalAlignment(SwingConstants.CENTER);
		txtConversion.setForeground(_Settings.textFieldColor);
		txtConversion.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtConversion.setColumns(1);
		txtConversion.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtConversion.setBackground(_Settings.backgroundColor);
		txtConversion.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				if (!isValidNumeric(txtConversion)) {
					txtConversion.setText("1");
					new MyOptionPane("Conversion must be positive number!", MyOptionPane.INFORMATION_MESSAGE);
					return;
				} else {
					double conversion = ValueFormatter.parseQuantity(txtConversion.getText());
					isConversionValid = conversion > 0;
					txtConversion.setText(ValueFormatter.formatQuantity(conversion));
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				txtConversion.selectAll();
			}
		});
		contentPane.add(txtConversion, "cell 21 7 3 1,grow");

		lblPc = new JLabel("pc");
		lblPc.setHorizontalAlignment(SwingConstants.LEFT);
		lblPc.setForeground(_Settings.labelColor);
		lblPc.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblPc.setBorder(null);
		lblPc.setBackground(_Settings.backgroundColor);
		contentPane.add(lblPc, "cell 24 7 2 1,grow");

		lblBarcode = new JLabel("Barcode");
		lblBarcode.setHorizontalAlignment(SwingConstants.RIGHT);
		lblBarcode.setForeground(_Settings.labelColor);
		lblBarcode.setFont(new Font("Arial Black", Font.PLAIN, 17));
		lblBarcode.setBorder(null);
		lblBarcode.setBackground(_Settings.backgroundColor);
		contentPane.add(lblBarcode, "cell 10 9,grow");

		txtBarcode = new JTextField();
		txtBarcode.setHorizontalAlignment(SwingConstants.CENTER);
		txtBarcode.setForeground(_Settings.textFieldColor);
		txtBarcode.setFont(new Font("Century Gothic", Font.BOLD, 21));
		txtBarcode.setColumns(1);
		txtBarcode.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtBarcode.setBackground(_Settings.backgroundColor);
		contentPane.add(txtBarcode, "cell 12 9 12 1,grow");
		contentPane.add(spItem, "cell 1 11 25 1,grow");

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
		dtm.setColumnIdentifiers(columnHeaders);
		dtm.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent arg0) {
				// TODO Auto-generated method stub
				double value = calculateNetAsset();
				txtPrice.setText(ValueFormatter.formatMoney(value));
			}
		});

		tblItem = new JTable(dtm);
		tblItem.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblItem.setGridColor(_Settings.labelColor);
		tblItem.getTableHeader().setForeground(_Settings.labelColor);
		tblItem.getTableHeader().setBackground(_Settings.backgroundColor);
		tblItem.getTableHeader().setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		tblItem.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
		tblItem.setFont(new Font("Century Gothic", Font.BOLD, 16));
		tblItem.setBackground(_Settings.backgroundColor);
		tblItem.setForeground(_Settings.textFieldColor);
		tblItem.setRowHeight(30);
		spItem.setViewportView(tblItem);

		btnAdd = new JButton("ADD");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frames.Stock stock = new frames.Stock(getThis(), null, (Item) getThis());
				stock.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						items = stock.getItems();
						updateTable();
					}

				});
			}
		});

		btnDelete = new JButton("DELETE");
		btnDelete.setForeground(_Settings.labelColor);
		btnDelete.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnDelete.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnDelete.setBackground(_Settings.backgroundColor);
		btnDelete.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int row = tblItem.getSelectedRow();
				if (row != -1) {
					String stringId = (String) dtm.getValueAt(row, 0);
					ItemFacade.removeStockById(items,ValueFormatter.parseItem(stringId).getId());
					updateTable();
					txtStatus.setText("Item deleted!");
					LOGGER.Activity.log("Component", LOGGER.DELETE, stringId, "unfinished item");
				} else
					txtStatus.setText("Select an item to delete!");
			}
		});

		txtNote = new JTextField();
		txtNote.setText("*pc is the smallest unit");
		txtNote.setForeground(_Settings.labelColor);
		txtNote.setFont(new Font("Century Gothic", Font.BOLD | Font.ITALIC, 17));
		txtNote.setEditable(false);
		txtNote.setColumns(10);
		txtNote.setBorder(null);
		txtNote.setBackground(_Settings.backgroundColor);
		contentPane.add(txtNote, "cell 1 13 10 1,growx,aligny top");

		contentPane.add(btnDelete, "cell 16 13 6 1,grow");
		btnAdd.setForeground(_Settings.labelColor);
		btnAdd.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnAdd.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnAdd.setBackground(_Settings.backgroundColor);
		contentPane.add(btnAdd, "cell 23 13 3 1,grow");

		getRootPane().setDefaultButton(btnAdd);

		txtStatus = new JTextField();
		txtStatus.setHorizontalAlignment(SwingConstants.RIGHT);
		txtStatus.setForeground(_Settings.labelColor);
		txtStatus.setFont(new Font("Arial Black", Font.PLAIN, 15));
		txtStatus.setEditable(false);
		txtStatus.setColumns(10);
		txtStatus.setBorder(null);
		txtStatus.setBackground(_Settings.backgroundColor);
		contentPane.add(txtStatus, "cell 15 15 11 1,grow");
		contentPane.add(btnNew, "cell 1 17 5 1,grow");

		btnClose = new JButton("CLOSE");
		btnClose.setForeground(_Settings.labelColor);
		btnClose.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnClose.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnClose.setBackground(_Settings.backgroundColor);
		btnClose.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				dispose();
			}
		});
		contentPane.add(btnClose, "cell 16 17 6 1,grow");

		btnSave = new JButton("SAVE");
		btnSave.setEnabled(false);
		btnSave.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnSave.setFont(new Font("Arial Black", Font.PLAIN, 19));
		btnSave.setBackground(_Settings.backgroundColor);
		btnSave.setForeground(_Settings.labelColor);
		btnSave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int id = Integer.parseInt(txtId.getText().substring(txtId.getText().indexOf("-") + 1));
				String name = txtName.getText().trim();
				double price = ValueFormatter.parseMoney(txtPrice.getText());
				String unit = txtUnit.getText();
				double conversion = ValueFormatter.parseQuantity(txtConversion.getText());
				String details = txtDetails.getText().trim().replaceAll("\n", " ");
				details = details.replaceAll(",", ";");

				if (tglbtnProduct.isSelected())
					db.add(id, ItemType.PRODUCT, name, price, unit, conversion, details, txtBarcode.getText(), items);
				else if (tglbtnMaterial.isSelected())
					db.add(id, ItemType.MATERIAL, name, price, unit, conversion, details, items);
				else
					db.add(id, ItemType.SERVICE, name, price, details);

				txtStatus.setText("Item saved!");
				LOGGER.Activity.log("Item", LOGGER.CREATE, ValueFormatter.formatItem(db.get(db.getList().size() - 1)));
				setFormEnabled(false);
				((Main) owner).cardItem.calculateStock();
				((Main) owner).cardItem.updateTable();
				((Main) owner).cardStore.updateTable();
			}
		});
		contentPane.add(btnSave, "cell 23 17 3 1,grow");

		populateFrame();

		pack();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}

}

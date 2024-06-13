package frames;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import cores.Inventory;
import cores.PurchaseTransaction;
import cores.SaleTransaction;
import cores.StoreInventory;
import cores.TransactionCategory;
import cores.UserObject;
import databases.Database;
import databases.Transactions;
import databases.UserCategories;
import globals.DatabaseFacade;
import globals.LOGGER;
import globals.RecordFacade;
import globals.ReportFacade;
import globals.ValueFormatter;
import globals._Settings;
import helpers.MyComboBoxRenderer;
import helpers.SortedComboBoxModel;
import helpers.TransactionType;
import net.miginfocom.swing.MigLayout;

public class Transaction extends JDialog {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField txtId, txtAmount, txtTax, txtTotal, txtStatus;
    private JLabel lblId, lblAmount, lblTax, lblCredit, lblDebit, lblUserCategory, lblUser, lblCashInventory, lblDate,
            lblRemark, lblCategory, lblTotal;
    private boolean isDateEmpty = true, isUserSelected, isInventorySelected = _Settings.defaultInventory != null,
            isCategorySelected, isAmountValid = true, isTaxValid = true;
    private Object object;
    private cores.Transaction transaction;
    private JSlider slider;
    private JComboBox<String> cbUserCategory, cbUser, cbCashInventory, cbCategory;
    private JDateChooser dateChooser;
    private Date date;
    private JScrollPane spRemark;
    private JButton btnPrint, btnSave, btnClose;
    private JTextArea txtRemark;
    private ItemListener ilCategory;

    /**
     * Create the frame.
     */
    @SuppressWarnings("unchecked")
    public Transaction(Window owner, Object object, TransactionType type) {
        super(owner, "Transaction", Dialog.ModalityType.DOCUMENT_MODAL);
        setResizable(false);
        setBounds(100, 100, 850, 700);
        contentPane = new JPanel();
        contentPane.setBackground(_Settings.backgroundColor);
        contentPane.setBorder(null);
        setContentPane(contentPane);
        contentPane.setLayout(new MigLayout("", "[10][60][30][10][20][10][20][20][10][40][10][210,grow][10][90][10][40,grow][10,grow][50,grow][110,grow][10]", "[10][10][40][40][10][40][10][30][10][10][40][10][40,grow][10][50][20][10][60,grow][10]"));

        this.object = object;

        UIManager.put("ComboBox.disabledForeground", _Settings.labelColor);
        UIManager.put("TextField.inactiveForeground", _Settings.labelColor);
        UIManager.put("TextArea.inactiveForeground", _Settings.labelColor);

        ilCategory = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if ((e.getStateChange() == ItemEvent.SELECTED))
                    isCategorySelected = true;
                    validateButton();
            }
        };

        lblId = new JLabel("Transaction ID");
        lblId.setBorder(null);
        lblId.setHorizontalAlignment(SwingConstants.CENTER);
        lblId.setFont(new Font("Arial Black", Font.PLAIN, 11));
        lblId.setForeground(_Settings.labelColor);
        contentPane.add(lblId, "cell 1 1 4 1,grow");

        txtId = new JTextField();
        txtId.setText(Integer.toString(((Transactions) DatabaseFacade.getDatabase("Transactions")).maxID() + 1));
        txtId.setEditable(false);
        txtId.setHorizontalAlignment(SwingConstants.CENTER);
        txtId.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        txtId.setFont(new Font("Century Gothic", Font.BOLD, 21));
        txtId.setBackground(null);
        txtId.setForeground(_Settings.labelColor);
        txtId.setColumns(5);
        contentPane.add(txtId, "cell 1 2 4 2,grow");

        lblAmount = new JLabel("Amount");
        lblAmount.setHorizontalAlignment(SwingConstants.RIGHT);
        lblAmount.setBorder(null);
        lblAmount.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblAmount.setForeground(_Settings.labelColor);
        lblAmount.setBackground(null);
        contentPane.add(lblAmount, "cell 6 3 4 1,grow");

        txtAmount = new JTextField();
        txtAmount.setText("0.00");
        txtAmount.setHorizontalAlignment(SwingConstants.CENTER);
        txtAmount.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        txtAmount.setFont(new Font("Century Gothic", Font.BOLD, 21));
        txtAmount.setBackground(null);
        txtAmount.setForeground(_Settings.textFieldColor);
        txtAmount.setColumns(10);
        txtAmount.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                // TODO Auto-generated method stub
                isAmountValid = isAmountValid();
                if (isAmountValid && isTaxValid) {
                    double amount = ValueFormatter.parseMoney(txtAmount.getText());
                    double tax = ValueFormatter.parseRate(txtTax.getText());
                    txtAmount.setText(ValueFormatter.formatMoney(amount));
                    calculateTotal(amount, tax);
                }
                validateButton();
            }

            @Override
            public void focusGained(FocusEvent e) {
                // TODO Auto-generated method stub
                txtAmount.selectAll();
            }
        });
        contentPane.add(txtAmount, "cell 11 3,grow");

        lblTax = new JLabel("VAT / Tax (AED)");
        lblTax.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTax.setForeground(_Settings.labelColor);
        lblTax.setBackground(null);
        lblTax.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblTax.setBorder(null);
        contentPane.add(lblTax, "cell 13 3,grow");

        txtTax = new JTextField();
        txtTax.setText("0.00");
        txtTax.setHorizontalAlignment(SwingConstants.CENTER);
        txtTax.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        txtTax.setFont(new Font("Century Gothic", Font.BOLD, 21));
        txtTax.setBackground(null);
        txtTax.setForeground(_Settings.textFieldColor);
        txtTax.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                // TODO Auto-generated method stub
                isTaxValid = isTaxValid();
                if (isTaxValid && isAmountValid) {
                    double amount = ValueFormatter.parseMoney(txtAmount.getText());
                    double tax = ValueFormatter.parseRate(txtTax.getText());
                    txtTax.setText(ValueFormatter.formatRate(tax));
                    calculateTotal(amount, tax);
                }
                validateButton();
            }

            @Override
            public void focusGained(FocusEvent e) {
                // TODO Auto-generated method stub
                txtTax.selectAll();
            }
        });
        contentPane.add(txtTax, "cell 15 3 3 1,grow");

        lblCredit = new JLabel("Credit");
        lblCredit.setHorizontalAlignment(SwingConstants.LEFT);
        lblCredit.setForeground(_Settings.labelColor);
        lblCredit.setBackground(null);
        lblCredit.setFont(new Font("Arial Black", Font.PLAIN, 15));
        lblCredit.setBorder(null);
        contentPane.add(lblCredit, "cell 1 5,grow");

        lblDebit = new JLabel("Debit");
        lblDebit.setHorizontalAlignment(SwingConstants.RIGHT);
        lblDebit.setForeground(_Settings.labelColor);
        lblDebit.setBackground(null);
        lblDebit.setFont(new Font("Arial Black", Font.PLAIN, 15));
        lblDebit.setBorder(null);
        contentPane.add(lblDebit, "cell 2 5 3 1,grow");

        lblUserCategory = new JLabel("<html><div style='text-align: center;'>User</div><div style='text-align: center;'>Category</div></html>");
        lblUserCategory.setHorizontalAlignment(SwingConstants.RIGHT);
        lblUserCategory.setForeground(_Settings.labelColor);
        lblUserCategory.setBackground(null);
        lblUserCategory.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblUserCategory.setBorder(null);
        contentPane.add(lblUserCategory, "cell 6 5 4 1,alignx right,growy");

        SortedComboBoxModel<String> scbmUser = new SortedComboBoxModel<String>();
        MyComboBoxRenderer mcbrUser = new MyComboBoxRenderer();
        cbUser = new JComboBox<String>(scbmUser);
        cbUser.setRenderer(mcbrUser);
        cbUser.setMaximumSize(new Dimension(210, 32767));
        cbUser.setFont(new Font("Century Gothic", Font.BOLD, 17));
        cbUser.setForeground(Color.DARK_GRAY);
        cbUser.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                // TODO Auto-generated method stub
                if ((e.getStateChange() == ItemEvent.SELECTED)) {
                    isUserSelected = true;
                    validateButton();
                }
            }
        });
        contentPane.add(cbUser, "cell 15 5 4 1,grow");

        List<String> userCategories = (List<String>) DatabaseFacade.getDatabase("UserCategories").getList();
        SortedComboBoxModel<String> scbmUserCategory = new SortedComboBoxModel<String>();
        MyComboBoxRenderer mcbrUserCategory = new MyComboBoxRenderer();
        List<String> ttUserCategory = new ArrayList<String>();
        for (String category : userCategories) {
            scbmUserCategory.addElement(category);
            ttUserCategory.add(category);
        }
        ttUserCategory.sort(null);
        mcbrUserCategory.setTooltips(ttUserCategory);
        cbUserCategory = new JComboBox<String>(scbmUserCategory);
        cbUserCategory.setRenderer(mcbrUserCategory);
        cbUserCategory.setMaximumSize(new Dimension(210, 32767));
        cbUserCategory.setFont(new Font("Century Gothic", Font.BOLD, 17));
        cbUserCategory.setForeground(Color.DARK_GRAY);
        cbUserCategory.setSelectedIndex(-1);
        cbUserCategory.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                // TODO Auto-generated method stub
                if ((e.getStateChange() == ItemEvent.SELECTED)) {
                    String category = (String) cbUserCategory.getSelectedItem();
                    UserCategories db = (UserCategories) DatabaseFacade.getDatabase("UserCategories");
                    List<UserObject> userObjects = db.get(category);
                    List<String> ttUser = new ArrayList<String>();
                    cbUser.removeAllItems();
                    for (UserObject userObject : userObjects) {
                        String userString = ValueFormatter.formatUserObject(userObject);
                        cbUser.addItem(userString);
                        ttUser.add(userString);
                    }
                    cbUser.setSelectedIndex(-1);
                    ttUser.sort(null);
                    mcbrUser.setTooltips(ttUser);
                    isUserSelected = false;
                    validateButton();
                }
            }
        });
        contentPane.add(cbUserCategory, "cell 11 5,grow");

        lblUser = new JLabel("User");
        lblUser.setHorizontalAlignment(SwingConstants.RIGHT);
        lblUser.setForeground(_Settings.labelColor);
        lblUser.setBackground(null);
        lblUser.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblUser.setBorder(null);
        contentPane.add(lblUser, "cell 13 5,grow");

        slider = new JSlider();
        slider.setBorder(null);
        slider.setPaintTicks(true);
        slider.setPreferredSize(new Dimension(120, 26));
        slider.setValue(2);
        slider.setMinimum(1);
        slider.setMaximum(3);
        slider.setBackground(null);
        slider.setForeground(_Settings.labelColor);
        slider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent arg0) {
                // TODO Auto-generated method stub
                validateButton();
            }
        });
        contentPane.add(slider, "cell 1 6 4 2,grow");

        lblCashInventory = new JLabel("<html><div style='text-align: center;'>Cash</div><div style='text-align: center;'>Inventory</div></html>");
        lblCashInventory.setHorizontalAlignment(SwingConstants.RIGHT);
        lblCashInventory.setBackground(null);
        lblCashInventory.setForeground(_Settings.labelColor);
        lblCashInventory.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblCashInventory.setBorder(null);
        contentPane.add(lblCashInventory, "cell 6 7 4 2,grow");

        List<Inventory> inventories = (List<Inventory>) DatabaseFacade.getDatabase("Inventories").getList();
        SortedComboBoxModel<String> scbmCashInventory = new SortedComboBoxModel<String>();
        MyComboBoxRenderer mcbrCashInventory = new MyComboBoxRenderer();
        List<String> ttCashInventory = new ArrayList<String>();
        for (Inventory inventory : inventories)
            if (!(inventory instanceof StoreInventory))
                scbmCashInventory.addElement(ValueFormatter.formatInventory(inventory));
        cbCashInventory = new JComboBox<String>(scbmCashInventory);
        cbCashInventory.setRenderer(mcbrCashInventory);
        cbCashInventory.setMaximumSize(new Dimension(210, 32767));
        cbCashInventory.setFont(new Font("Century Gothic", Font.BOLD, 17));
        cbCashInventory.setForeground(Color.DARK_GRAY);
        cbCashInventory.setSelectedItem(_Settings.defaultInventory);
        for (int i = 0; i < cbCashInventory.getItemCount(); i++) {
            Inventory inventory = ValueFormatter.parseInventory(cbCashInventory.getItemAt(i));
            String inventoryString = inventory.getName();
            if (!inventory.getAccountNo().isEmpty())
                inventoryString += " [" + inventory.getAccountNo() + "]";
            ttCashInventory.add(inventoryString);
        }
        mcbrCashInventory.setTooltips(ttCashInventory);
        cbCashInventory.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                // TODO Auto-generated method stub
                if ((e.getStateChange() == ItemEvent.SELECTED)) {
                    isInventorySelected = true;
                    validateButton();
                }
            }
        });
        contentPane.add(cbCashInventory, "cell 11 7 1 2,grow");

        lblCategory = new JLabel("<html><div style='text-align: center'>Transaction</div><div style='text-align: center'>Category</div></html>");
        lblCategory.setHorizontalAlignment(SwingConstants.CENTER);
        lblCategory.setBackground(null);
        lblCategory.setForeground(_Settings.labelColor);
        lblCategory.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblCategory.setBorder(null);
        contentPane.add(lblCategory, "cell 13 7 1 2,alignx right,growy");

        List<TransactionCategory> categories = (List<TransactionCategory>) DatabaseFacade
                .getDatabase("TransactionCategories").getList();
        SortedComboBoxModel<String> scbmCategory = new SortedComboBoxModel<String>();
        MyComboBoxRenderer mcbrCategory = new MyComboBoxRenderer();
        List<String> ttCategory = new ArrayList<String>();
        for (TransactionCategory category : categories) {
//			if (!category.getName().equals("Sale") && !category.getName().equals("Purchase")
//					&& !category.getName().equals("Delivery") && !category.getName().equals("Production")) {
            scbmCategory.addElement(category.getName());
            ttCategory.add(category.getName());
//			}
        }
        ttCategory.sort(null);
        mcbrCategory.setTooltips(ttCategory);
        cbCategory = new JComboBox<String>(scbmCategory);
        cbCategory.setRenderer(mcbrCategory);
        cbCategory.setMaximumSize(new Dimension(210, 50));
        cbCategory.setFont(new Font("Century Gothic", Font.BOLD, 17));
        cbCategory.setForeground(Color.DARK_GRAY);
        cbCategory.setSelectedIndex(-1);
        cbCategory.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                // TODO Auto-generated method stub
                if ((e.getStateChange() == ItemEvent.SELECTED)) {
                    String categoryString = (String) ((JComboBox<String>) e.getSource()).getSelectedItem();
                    Database<?> db = DatabaseFacade.getDatabase("TransactionCategories");
                    List<TransactionCategory> categories = (List<TransactionCategory>) db.getList();
                    TransactionCategory selectedCategory = null;
                    for (TransactionCategory category : categories)
                        if (category.getName().equals(categoryString))
                            selectedCategory = category;
                    if (selectedCategory.isCreditable() && selectedCategory.isDebitable()) {
                        slider.setEnabled(true);
                        lblCredit.setText("Credit");
                        lblDebit.setText("Debit");
                    } else if (!selectedCategory.isCreditable()) {
                        slider.setEnabled(false);
                        slider.setValue(3);
                        lblCredit.setText("");
                        lblDebit.setText("Debit");
                    } else {
                        slider.setEnabled(false);
                        slider.setValue(1);
                        lblCredit.setText("Credit");
                        lblDebit.setText("");
                    }
                    isCategorySelected = true;
                    validateButton();
                }
            }
        });
        contentPane.add(cbCategory, "cell 15 7 4 2,grow");

        lblRemark = new JLabel("Description");
        lblRemark.setHorizontalAlignment(SwingConstants.RIGHT);
        lblRemark.setBackground(null);
        lblRemark.setForeground(_Settings.labelColor);
        lblRemark.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblRemark.setBorder(null);
        contentPane.add(lblRemark, "cell 1 10 2 1,grow");

        txtRemark = new JTextArea();
        txtRemark.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        txtRemark.setFont(new Font("Century Gothic", Font.BOLD, 21));
        txtRemark.setBackground(_Settings.backgroundColor);
        txtRemark.setForeground(_Settings.textFieldColor);
        txtRemark.setLineWrap(true);

        spRemark = new JScrollPane(txtRemark);
        spRemark.setBorder(null);
        contentPane.add(spRemark, "cell 4 10 8 3,grow");

        lblDate = new JLabel("Date");
        lblDate.setHorizontalAlignment(SwingConstants.RIGHT);
        lblDate.setForeground(_Settings.labelColor);
        lblDate.setBackground(null);
        lblDate.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblDate.setBorder(null);
        contentPane.add(lblDate, "cell 13 10,grow");

        dateChooser = new JDateChooser();
        dateChooser.setFont(new Font("Century Gothic", Font.BOLD, 17));
        dateChooser.setBorder(null);
        dateChooser.setDateFormatString("dd-MMM-yyyy");
        JTextFieldDateEditor dateEditor = (JTextFieldDateEditor) dateChooser.getComponent(1);
        dateEditor.setHorizontalAlignment(JTextField.CENTER);
        dateEditor.setEnabled(false);
        dateEditor.setDisabledTextColor(Color.DARK_GRAY);
        dateChooser.addPropertyChangeListener("date", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent e) {
                // TODO Auto-generated method stub
                date = (Date) e.getNewValue();
                date = ValueFormatter.setTimeToZero(date);
                Date today = ValueFormatter.setTimeToZero(new Date());
                isDateEmpty = false;
                if (date.after(today))
                    new MyOptionPane(
                            "Selected date is later than today.\n" + "Today: " + ValueFormatter.formatDate(today) + "\n"
                                    + "Selection:" + ValueFormatter.formatDate(date),
                            MyOptionPane.CONFIRMATION_DIALOG_BOX);
                validateButton();
            }
        });
        contentPane.add(dateChooser, "cell 15 10 4 1,grow");

        lblTotal = new JLabel("Total");
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTotal.setForeground(_Settings.labelColor);
        lblTotal.setBackground(null);
        lblTotal.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblTotal.setBorder(null);
        contentPane.add(lblTotal, "cell 4 14 4 2,grow");

        txtTotal = new JTextField();
        txtTotal.setText("0.00 AED");
        txtTotal.setHorizontalAlignment(SwingConstants.CENTER);
        txtTotal.setForeground(_Settings.labelColor);
        txtTotal.setBackground(null);
        txtTotal.setFont(new Font("Century Gothic", Font.BOLD, 25));
        txtTotal.setEditable(false);
        txtTotal.setColumns(5);
        txtTotal.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        contentPane.add(txtTotal, "cell 9 14 3 2,grow");

        txtStatus = new JTextField();
        txtStatus.setEditable(false);
        txtStatus.setBackground(null);
        txtStatus.setForeground(_Settings.labelColor);
        txtStatus.setBorder(null);
        txtStatus.setHorizontalAlignment(SwingConstants.RIGHT);
        txtStatus.setFont(new Font("Arial Black", Font.PLAIN, 15));
        txtStatus.setColumns(10);
        contentPane.add(txtStatus, "cell 13 15 6 1,grow");

        btnPrint = new JButton("PRINT");
        btnPrint.setEnabled(false);
        btnPrint.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
        btnPrint.setFont(new Font("Arial Black", Font.PLAIN, 19));
        btnPrint.setBackground(_Settings.backgroundColor);
        btnPrint.setForeground(_Settings.labelColor);
        btnPrint.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              ReportFacade.printTransactionSingle(transaction);
            }
        });
        contentPane.add(btnPrint, "cell 1 17 6 1,grow");

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
        contentPane.add(btnClose, "cell 12 17 4 1,grow");

        btnSave = new JButton("SAVE");
        btnSave.setEnabled(false);
        btnSave.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
        btnSave.setFont(new Font("Arial Black", Font.PLAIN, 19));
        btnSave.setBackground(_Settings.backgroundColor);
        btnSave.setForeground(_Settings.labelColor);
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int id = Integer.parseInt(txtId.getText());
                double amount = ValueFormatter.parseMoney(txtAmount.getText());
                double tax = ValueFormatter.parseRate(txtTax.getText());
                boolean isCredit = false; // debit by default
                if (slider.getValue() == 1)
                    isCredit = true;
                UserObject userObject = ValueFormatter.parseUserObject((String) cbUser.getSelectedItem());
                Inventory inventory = ValueFormatter.parseInventory((String) cbCashInventory.getSelectedItem());
                String category = (String) cbCategory.getSelectedItem();
                String remark = ValueFormatter.formatTextSafely(txtRemark.getText());

                switch (type) {
                    case PRODUCTION_COST:
                        if (addTransaction(id, amount, tax, isCredit, userObject, inventory, date, remark, category)) {
                            setFormEnabled(false);
                            List<cores.Transaction> other = (List<cores.Transaction>) object;
                            Transactions db = (Transactions) DatabaseFacade.getDatabase("Transactions");
                            other.add(db.get(db.find(db.maxID())));
                            txtStatus.setText("Record saved!");
                            LOGGER.Activity.log("Transaction", LOGGER.ADD,
                                    ValueFormatter.formatBalanceId(id, cores.Transaction.class), "unfinished production");
                        }
                        break;
                    case DELIVERY_COST:
                        if (addTransaction(id, amount, tax, isCredit, userObject, inventory, date, remark, category)) {
                            setFormEnabled(false);
                            List<cores.Transaction> costs = (List<cores.Transaction>) object;
                            Transactions db = (Transactions) DatabaseFacade.getDatabase("Transactions");
                            costs.add(db.get(db.find(db.maxID())));
                            txtStatus.setText("Record saved!");
                            LOGGER.Activity.log("Transaction", LOGGER.ADD,
                                    ValueFormatter.formatBalanceId(id, cores.Transaction.class), "unfinished delivery");
                        }
                        break;
                    case PURCHASE_COST:
                        if (addTransaction(id, amount, tax, isCredit, userObject, inventory, date, remark, category)) {
                            setFormEnabled(false);
                            List<cores.Transaction> other = (List<cores.Transaction>) object;
                            Transactions db = (Transactions) DatabaseFacade.getDatabase("Transactions");
                            other.add(db.get(db.find(db.maxID())));
                            txtStatus.setText("Record saved!");
                            LOGGER.Activity.log("Transaction", LOGGER.ADD,
                                    ValueFormatter.formatBalanceId(id, cores.Transaction.class), "unfinished purchase");
                        }
                        break;
                    case SALE_COST:
                        if (addTransaction(id, amount, tax, isCredit, userObject, inventory, date, remark, category)) {
                            setFormEnabled(false);
                            List<cores.Transaction> other = (List<cores.Transaction>) object;
                            Transactions db = (Transactions) DatabaseFacade.getDatabase("Transactions");
                            other.add(db.get(db.find(db.maxID())));
                            txtStatus.setText("Record saved!");
                            LOGGER.Activity.log("Transaction", LOGGER.ADD,
                                    ValueFormatter.formatBalanceId(id, cores.Transaction.class), "unfinished sale");
                        }
                        break;
                    case PURCHASE:
                        if (addTransaction(id, amount, tax, isCredit, userObject, inventory, date, remark, category)) {
                            setFormEnabled(false);
                            PurchaseTransaction pt = (PurchaseTransaction) object;
                            Transactions db = (Transactions) DatabaseFacade.getDatabase("Transactions");
                            pt.getPayments().add(db.get(db.find(db.maxID())));
                            txtStatus.setText("Record saved!");
                            LOGGER.Activity.log("Transaction", LOGGER.ADD,
                                    ValueFormatter.formatBalanceId(id, cores.Transaction.class),
                                    "purchase " + ValueFormatter.formatBalanceId(pt.getId(), PurchaseTransaction.class));
                        }
                        break;
                    case SALE:
                        if (addTransaction(id, amount, tax, isCredit, userObject, inventory, date, remark, category)) {
                            setFormEnabled(false);
                            SaleTransaction st = (SaleTransaction) object;
                            Transactions db = (Transactions) DatabaseFacade.getDatabase("Transactions");
                            st.getPayments().add(db.get(db.find(db.maxID())));
                            txtStatus.setText("Record saved!");
                            LOGGER.Activity.log("Transaction", LOGGER.ADD,
                                    ValueFormatter.formatBalanceId(id, cores.Transaction.class),
                                    "sale " + ValueFormatter.formatBalanceId(st.getId(), SaleTransaction.class));
                        }
                        break;
                    case NORMAL:
                        if (addTransaction(id, amount, tax, isCredit, userObject, inventory, date, remark, category)) {
                            reset();
                            txtStatus.setText("Record saved!");
                            LOGGER.Activity.log("Transaction", LOGGER.ADD,
                                    ValueFormatter.formatBalanceId(id, cores.Transaction.class));
                            txtId.setText(Integer
                                    .toString(((Transactions) DatabaseFacade.getDatabase("Transactions")).maxID() + 1));
                        } else
                            txtStatus.setText("An error occurred!");
                        break;
                    default:
                        break;
                }
                btnPrint.setEnabled(true);
				btnSave.setEnabled(false);
            }
        });
        contentPane.add(btnSave, "cell 17 17 2 1,grow");

        populateFrame(type);

        getRootPane().setDefaultButton(btnSave);

        pack();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public Object getTransaction() {
        return object;
    }

    private void reset() {
        txtAmount.setText("0.00");
        txtTax.setText("0.00");
        txtTotal.setText("0.00 AED");
        txtRemark.setText("");
        slider.setEnabled(true);
        lblDebit.setText("Debit");
        lblCredit.setText("Credit");
        txtStatus.setText("");
        cbCashInventory.setSelectedItem(_Settings.defaultInventory);
        isInventorySelected = _Settings.defaultInventory != null;
        isAmountValid = isTaxValid = isUserSelected = isCategorySelected = true;
        btnSave.setEnabled(false);
    }

    private boolean addTransaction(int id, double amount, double tax, boolean isCredit, UserObject userObject,
                                   Inventory inventory, Date date, String remark, String category) {
        Transactions transactions = (Transactions) DatabaseFacade.getDatabase("Transactions");
        String balanceId = ValueFormatter.formatBalanceId(id, cores.Transaction.class);
        if (transactions.add(id, amount, tax, isCredit, userObject, inventory, date, remark, category)) {
            RecordFacade.record(inventory, amount, tax, isCredit, userObject);
            RecordFacade.addInventoryRecord(inventory, amount, tax, isCredit, balanceId, date);
            RecordFacade.addUserRecord(userObject, amount, tax, !isCredit, balanceId, date);
            transaction = transactions.get(0);
            return true;
        } else
            return false;
    }

    private void calculateTotal(double amount, double tax) {
        txtTotal.setText(ValueFormatter.formatMoney(tax + amount) + " AED");
    }

    private boolean isAmountValid() {
        return Pattern.compile("([0-9]{1,3}(\\,)?)*(\\.[0-9]*)?").matcher(txtAmount.getText()).matches();
    }

    private boolean isTaxValid() {
        return Pattern.compile("([0-9]{1,3}(\\,)?)*(\\.[0-9]*)?").matcher(txtAmount.getText()).matches();
    }

    private void setFormEnabled(boolean isEnabled) {
        slider.setEnabled(isEnabled);
        cbUser.setEnabled(isEnabled);
        cbUserCategory.setEnabled(isEnabled);
        cbCashInventory.setEnabled(isEnabled);
        cbCategory.setEnabled(isEnabled);
        dateChooser.setEnabled(isEnabled);
        txtAmount.setEnabled(isEnabled);
        txtTax.setEnabled(isEnabled);
        txtRemark.setEnabled(isEnabled);
    }

    private void populateFrame(TransactionType type) {
        if (object != null) {
            setFormEnabled(false);
            switch (type) {
                case DELIVERY_COST:
                case PURCHASE_COST:
                case SALE_COST:
                    cbCategory.removeItemListener(cbCategory.getItemListeners()[0]);
                    cbCategory.addItemListener(ilCategory);
                    cbCategory.setEnabled(true);
                    cbUserCategory.setEnabled(true);
                    cbUser.setEnabled(true);
                    cbCashInventory.setEnabled(true);
                    slider.setValue(3);
                    slider.setEnabled(false);
                    lblCredit.setText("");
                    lblDebit.setText("Debit");
                    txtAmount.setEnabled(true);
                    txtTax.setEnabled(true);
                    txtRemark.setEnabled(true);
                    dateChooser.setEnabled(true);
                    isAmountValid = isTaxValid =  true;
                    isDateEmpty  =isCategorySelected = isInventorySelected = isUserSelected = false;
                    break;
                case PRODUCTION_COST:
                    cbCategory.removeItemListener(cbCategory.getItemListeners()[0]);
                    cbCategory.addItemListener(ilCategory);
                    cbCategory.setEnabled(true);
                    cbUserCategory.setEnabled(true);
                    cbUser.setEnabled(true);
                    cbCashInventory.setEnabled(true);
                    txtRemark.setText(ValueFormatter.formatProductionRemark());
                    slider.setValue(3);
                    slider.setEnabled(false);
                    lblCredit.setText("");
                    lblDebit.setText("Debit");
                    txtAmount.setEnabled(true);
                    txtTax.setEnabled(true);
                    txtRemark.setEnabled(true);
                    dateChooser.setEnabled(true);
                    isAmountValid = isTaxValid = true;
                    isDateEmpty = isInventorySelected = isUserSelected = false;
                    break;
                case PURCHASE:
                    PurchaseTransaction pt = (PurchaseTransaction) object;
                    cbCategory.removeItemListener(cbCategory.getItemListeners()[0]);
                    cbCategory.addItemListener(ilCategory);
                    cbCategory.setEnabled(true);
                    cbUser.addItem(ValueFormatter.formatUserObject(pt.getSupplier()));
                    cbUser.setSelectedIndex(0);
                    cbCashInventory.setEnabled(true);
                    txtRemark.setText(ValueFormatter.formatPurchaseRemark(pt));
                    slider.setValue(3);
                    slider.setEnabled(false);
                    lblCredit.setText("");
                    lblDebit.setText("Debit");
                    txtAmount.setEnabled(true);
                    txtTax.setEnabled(true);
                    txtRemark.setEnabled(true);
                    dateChooser.setEnabled(true);
                    dateChooser.setDate(pt.getDate());
                    date = pt.getDate();
                    isAmountValid = isTaxValid = isUserSelected  = true;
                    isInventorySelected = isDateEmpty = false;
                    break;
                case SALE:
                    SaleTransaction st = (SaleTransaction) object;
                    cbCategory.removeItemListener(cbCategory.getItemListeners()[0]);
                    cbCategory.addItemListener(ilCategory);
                    cbCategory.setEnabled(true);
                    cbUser.addItem(ValueFormatter.formatUserObject(st.getCustomer()));
                    cbUser.setSelectedIndex(0);
                    cbCashInventory.setEnabled(true);
                    txtRemark.setText(ValueFormatter.formatSaleRemark(st));
                    slider.setValue(1);
                    slider.setEnabled(false);
                    lblCredit.setText("Credit");
                    lblDebit.setText("");
                    txtAmount.setEnabled(true);
                    txtTax.setEnabled(true);
                    txtRemark.setEnabled(true);
                    dateChooser.setEnabled(true);
                    dateChooser.setDate(st.getDate());
                    date = st.getDate();
                    isAmountValid = isTaxValid = isUserSelected = true;
                    isInventorySelected = isDateEmpty = false;
                    break;
                case NORMAL:
                    cores.Transaction t = (cores.Transaction) object;
                    transaction = t;
                    txtId.setText(Integer.toString(t.getId()));
                    txtAmount.setText(ValueFormatter.formatMoney(t.getAmount()));
                    txtTax.setText(ValueFormatter.formatRate(t.getTax()));
                    if (t.isCredit())
                        slider.setValue(1);
                    else
                        slider.setValue(3);
                    cbUser.addItem(ValueFormatter.formatUserObject(t.getUserObject()));
                    cbUser.setSelectedIndex(0);
                    cbCashInventory.setSelectedItem(ValueFormatter.formatInventory(t.getInventory()));
                    cbCategory.setSelectedItem(t.getCategory());
                    txtRemark.setText(t.getRemark());
                    dateChooser.getDateEditor().setDate(t.getDate());
                    calculateTotal(t.getAmount(), t.getTax());
                    isAmountValid = isTaxValid = isUserSelected = isInventorySelected = isCategorySelected = true;
                    isDateEmpty = false;
                    contentPane.remove(btnSave);
                    btnPrint.setEnabled(true);
                    break;
                default:
                    break;
            }
            validateButton();
        }
    }

    private void validateButton() {
        if (!isAmountValid)
            txtStatus.setText("Invalid amount value!");
        else if (!isTaxValid)
            txtStatus.setText("Invalid tax value!");
        else if (slider.getValue() == 2)
            txtStatus.setText("Select credit or debit!");
        else if (!isUserSelected)
            txtStatus.setText("Select a user!");
        else if (!isInventorySelected)
            txtStatus.setText("Select a cash inventory!");
        else if (!isCategorySelected)
            txtStatus.setText("Select a transaction category!");
        else if (isDateEmpty)
            txtStatus.setText("Select a date!");
        else
            txtStatus.setText("");
        if (isAmountValid && isTaxValid && isUserSelected && isInventorySelected && isCategorySelected && !isDateEmpty
                && slider.getValue() != 2)
            btnSave.setEnabled(true);
        else
            btnSave.setEnabled(false);
    }

}

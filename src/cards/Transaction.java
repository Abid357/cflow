package cards;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import cores.Inventory;
import cores.PurchaseTransaction;
import cores.SaleTransaction;
import cores.TransactionCategory;
import cores.UserObject;
import cores.UserRecord;
import databases.Inventories;
import databases.PurchaseTransactions;
import databases.SaleTransactions;
import databases.TransactionCategories;
import databases.Transactions;
import databases.UserObjects;
import databases.UserRecords;
import frames.Main;
import frames.MyOptionPane;
import globals.DatabaseFacade;
import globals.LOGGER;
import globals.RecordFacade;
import globals.ReportFacade;
import globals.ValueFormatter;
import globals._Settings;
import helpers.MyComboBoxRenderer;
import helpers.SortedComboBoxModel;
import helpers.TransactionStatus;
import helpers.TransactionType;
import helpers.TransactionWrapper;
import net.miginfocom.swing.MigLayout;

public class Transaction extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static String SORT_BY_DATE = "SORT BY: DATE";
    private static String SORT_BY_ID = "SORT BY: ID";

    private Main mainFrame;
    private JTable tblTransaction;
    private JDateChooser dcFrom, dcTo;
    private Date toDate, fromDate;
    private DefaultTableModel dtm;
    private String[] columnHeaders = {"ID", "USER", "CREDIT", "DEBIT", "BALANCE", "DATE", "INVENTORY", "DESCRIPTION",
            "CATEGORY"};
    private JComboBox<String> cbUser, cbCategory, cbInventory;
    private MyComboBoxRenderer mcbrUser, mcbrCategory, mcbrInventory;
    private cards.Inventory cardInventory;
    private cards.User cardUser;
    private JButton btnSort;

    /**
     * Create the panel.
     */
    public Transaction(Main frame) {
        this.mainFrame = frame;
        setBackground(_Settings.backgroundColor);
        setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        setLayout(new MigLayout("", "[150][10][150][10][150][10][150][10][150][10][150][grow][150][150]", "[50][20][20][30][10][grow][10][50]"));

        JButton btnAddTCategory = new JButton("ADD CATEGORY");
        btnAddTCategory.setBackground(_Settings.backgroundColor);
        btnAddTCategory.setForeground(_Settings.labelColor);
        btnAddTCategory.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
        btnAddTCategory.setFont(new Font("Arial", Font.BOLD, 14));
        btnAddTCategory.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                frames.TransactionCategory frameTC = new frames.TransactionCategory(mainFrame);
                frameTC.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosed(WindowEvent e) {
                        // TODO Auto-generated method stub
                        updateOptions();
                        updateTable();
                    }
                });
            }
        });

        JButton btnAddTransaction = new JButton("ADD TRANSACTION");
        btnAddTransaction.setBackground(_Settings.backgroundColor);
        btnAddTransaction.setForeground(_Settings.labelColor);
        btnAddTransaction.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
        btnAddTransaction.setFont(new Font("Arial", Font.BOLD, 14));
        btnAddTransaction.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                frames.Transaction frameTransaction = new frames.Transaction(mainFrame, null, TransactionType.NORMAL);
                frameTransaction.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosed(WindowEvent e) {
                        // TODO Auto-generated method stub
                        cardInventory.setTotalLiquidAsset();
                        cardUser.updateTable();
                        updateTable();
                    }
                });
            }
        });
        add(btnAddTransaction, "cell 0 0,grow");
        add(btnAddTCategory, "cell 2 0,grow");

        JButton btnPrintStatement = new JButton("PRINT STATEMENT");
        btnPrintStatement.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                ReportFacade.printTransactionStatement(mainFrame, tblTransaction, fromDate, toDate, columnHeaders,
                        !((String) cbUser.getSelectedItem()).equals("ANY"));
            }
        });
        btnPrintStatement.setBackground(_Settings.backgroundColor);
        btnPrintStatement.setForeground(_Settings.labelColor);
        btnPrintStatement.setFont(new Font("Arial", Font.BOLD, 14));
        btnPrintStatement.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
        add(btnPrintStatement, "cell 4 0,grow");
        
                btnSort = new JButton("SORT BY: DATE");
                btnSort.setBackground(_Settings.backgroundColor);
                btnSort.setForeground(_Settings.labelColor);
                btnSort.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
                btnSort.setFont(new Font("Arial", Font.BOLD, 14));
                btnSort.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // TODO Auto-generated method stub
                        if (btnSort.getText().equals(SORT_BY_DATE))
                            btnSort.setText(SORT_BY_ID);
                        else
                            btnSort.setText(SORT_BY_DATE);
                        updateTable();
                    }
                });
                add(btnSort, "cell 6 0,grow");

        JLabel lblUser = new JLabel("User");
        lblUser.setMinimumSize(new Dimension(22, 20));
        lblUser.setFont(new Font("Arial", Font.BOLD, 15));
        lblUser.setHorizontalAlignment(SwingConstants.CENTER);
        lblUser.setBorder(null);
        lblUser.setBackground(null);
        lblUser.setForeground(_Settings.labelColor);
        add(lblUser, "cell 0 2,grow");

        JLabel lblFrom = new JLabel("From");
        lblFrom.setMinimumSize(new Dimension(24, 20));
        lblFrom.setHorizontalAlignment(SwingConstants.CENTER);
        lblFrom.setFont(new Font("Arial", Font.BOLD, 15));
        lblFrom.setBorder(null);
        lblFrom.setBackground(null);
        lblFrom.setForeground(_Settings.labelColor);
        add(lblFrom, "cell 2 2,grow");

        JLabel lblTo = new JLabel("To");
        lblTo.setMinimumSize(new Dimension(12, 20));
        lblTo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTo.setFont(new Font("Arial", Font.BOLD, 15));
        lblTo.setBorder(null);
        lblTo.setBackground(null);
        lblTo.setForeground(_Settings.labelColor);
        add(lblTo, "cell 4 2,grow");

        JLabel lblInventory = new JLabel("Inventory");
        lblInventory.setMinimumSize(new Dimension(48, 20));
        lblInventory.setHorizontalAlignment(SwingConstants.CENTER);
        lblInventory.setFont(new Font("Arial", Font.BOLD, 15));
        lblInventory.setBorder(null);
        lblInventory.setBackground(null);
        lblInventory.setForeground(_Settings.labelColor);
        add(lblInventory, "cell 6 2,grow");

        JLabel lblCategory = new JLabel("<html><center><p>Transaction Category</p></center></html>");
        lblCategory.setMinimumSize(new Dimension(45, 20));
        lblCategory.setHorizontalAlignment(SwingConstants.CENTER);
        lblCategory.setFont(new Font("Arial", Font.BOLD, 15));
        lblCategory.setBorder(null);
        lblCategory.setBackground(null);
        lblCategory.setForeground(_Settings.labelColor);
        add(lblCategory, "cell 8 1 1 2,growx,aligny bottom");

        JButton btnRefresh = new JButton("REFRESH");
        btnRefresh.setMinimumSize(new Dimension(77, 50));
        btnRefresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                resetSelections();
            }
        });
        btnRefresh.setBackground(_Settings.backgroundColor);
        btnRefresh.setForeground(_Settings.labelColor);
        btnRefresh.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 14));
        add(btnRefresh, "cell 10 2 1 2,grow");

        mcbrUser = new MyComboBoxRenderer();
        cbUser = new JComboBox<String>(new SortedComboBoxModel<String>());
        cbUser.setMinimumSize(new Dimension(28, 30));
        cbUser.setRenderer(mcbrUser);
        cbUser.setMaximumSize(new Dimension(150, 32767));
        cbUser.setFont(new Font("Century Gothic", Font.BOLD, 14));
        cbUser.setBorder(null);
        cbUser.setForeground(Color.DARK_GRAY);
        add(cbUser, "cell 0 3,grow");

        dcFrom = new JDateChooser();
        dcFrom.setMinimumSize(new Dimension(27, 30));
        dcFrom.setFont(new Font("Century Gothic", Font.BOLD, 14));
        dcFrom.setBorder(null);
        dcFrom.setDateFormatString("dd-MMM-yyyy");
        JTextFieldDateEditor deFrom = (JTextFieldDateEditor) dcFrom.getComponent(1);
        deFrom.setHorizontalAlignment(JTextField.CENTER);
        deFrom.setEnabled(false);
        deFrom.setDisabledTextColor(Color.DARK_GRAY);
        dcFrom.addPropertyChangeListener("date", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent e) {
                // TODO Auto-generated method stub
                fromDate = (Date) e.getNewValue();
                fromDate = ValueFormatter.setTimeToZero(fromDate);
                dcTo.setMinSelectableDate(fromDate);
                updateTable();
            }
        });
        add(dcFrom, "cell 2 3,grow");

        dcTo = new JDateChooser();
        dcTo.setMinimumSize(new Dimension(27, 3));
        dcTo.setFont(new Font("Century Gothic", Font.BOLD, 14));
        dcTo.setBorder(null);
        dcTo.setDateFormatString("dd-MMM-yyyy");
        JTextFieldDateEditor deTo = (JTextFieldDateEditor) dcTo.getComponent(1);
        deTo.setHorizontalAlignment(JTextField.CENTER);
        deTo.setEnabled(false);
        deTo.setDisabledTextColor(Color.DARK_GRAY);
        dcTo.addPropertyChangeListener("date", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent e) {
                // TODO Auto-generated method stub
                toDate = (Date) e.getNewValue();
                toDate = ValueFormatter.setTimeToZero(toDate);
                dcFrom.setMaxSelectableDate(toDate);
                updateTable();
            }
        });
        add(dcTo, "cell 4 3,grow");

        mcbrInventory = new MyComboBoxRenderer();
        SortedComboBoxModel<String> scbmInventory = new SortedComboBoxModel<String>();
        cbInventory = new JComboBox<String>(scbmInventory);
        cbInventory.setMinimumSize(new Dimension(28, 3));
        cbInventory.setMaximumSize(new Dimension(150, 32767));
        cbInventory.setRenderer(mcbrInventory);
        cbInventory.setFont(new Font("Century Gothic", Font.BOLD, 14));
        cbInventory.setBorder(null);
        cbInventory.setForeground(Color.DARK_GRAY);
        add(cbInventory, "cell 6 3,grow");

        mcbrCategory = new MyComboBoxRenderer();
        SortedComboBoxModel<String> scbmCategory = new SortedComboBoxModel<String>();
        cbCategory = new JComboBox<String>(scbmCategory);
        cbCategory.setMinimumSize(new Dimension(28, 30));
        cbCategory.setMaximumSize(new Dimension(150, 32767));
        cbCategory.setRenderer(mcbrCategory);
        cbCategory.setFont(new Font("Century Gothic", Font.BOLD, 14));
        cbCategory.setBorder(null);
        cbCategory.setForeground(Color.DARK_GRAY);
        add(cbCategory, "cell 8 3,grow");

        JScrollPane spTransaction = new JScrollPane();
        spTransaction.setBackground(null);
        spTransaction.setOpaque(false);
        spTransaction.getViewport().setOpaque(false);
        add(spTransaction, "cell 0 5 14 1,grow");

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
        tblTransaction = new JTable(dtm);
        tblTransaction.setGridColor(_Settings.labelColor);
        tblTransaction.getTableHeader().setForeground(_Settings.labelColor);
        tblTransaction.getTableHeader().setBackground(_Settings.backgroundColor);
        tblTransaction.getTableHeader().setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
        tblTransaction.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
        tblTransaction.setFont(new Font("Century Gothic", Font.BOLD, 16));
        tblTransaction.setBackground(_Settings.backgroundColor);
        tblTransaction.setForeground(_Settings.textFieldColor);
        tblTransaction.setRowHeight(30);
        tblTransaction.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub
                if (e.getClickCount() == 2) {
                    int row = tblTransaction.getSelectedRow();
                    if (row >= 0) {
                        String id = (String) tblTransaction.getValueAt(row, 0);
                        if (id.startsWith("TR")) {
                            int idNo = Integer.parseInt(id.substring(id.indexOf("-") + 1));
                            Transactions db = (Transactions) DatabaseFacade.getDatabase("Transactions");
                            cores.Transaction transaction = db.get(db.find(idNo));
                            frames.Transaction frameTransaction = new frames.Transaction(mainFrame, transaction,
                                    TransactionType.NORMAL);
                            frameTransaction.addWindowListener(new WindowAdapter() {

                                @Override
                                public void windowClosed(WindowEvent arg0) {
                                    // TODO Auto-generated method stub
                                    updateTable();
                                }
                            });
                        }
                    }
                }
            }
        });
        spTransaction.setViewportView(tblTransaction);
        
                JButton btnDelete = new JButton("DELETE");
                btnDelete.setBackground(_Settings.backgroundColor);
                btnDelete.setForeground(_Settings.labelColor);
                btnDelete.setFont(new Font("Arial", Font.BOLD, 13));
                btnDelete.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
                btnDelete.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // TODO Auto-generated method stub
                        int index = tblTransaction.getSelectedRow();
                        if (index != -1) {
                            String stringId = (String) tblTransaction.getValueAt(index, 0);
                            if (stringId.startsWith("TR")) {
                                int id = Integer.parseInt(stringId.substring(stringId.indexOf("TR-") + 3));
                                RecordFacade.removeTransaction(id);
                                RecordFacade.removeTransactionFromOthers(id);
                                LOGGER.Activity.log("Transaction", LOGGER.DELETE, stringId);
                                updateTable();
                                cardUser.updateTable();
                            } else
                                new MyOptionPane("This type of transaction cannot be deleted from here.",
                                        MyOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });
                
                        add(btnDelete, "cell 12 7,grow");

        updateOptions();
        cbUser.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                // TODO Auto-generated method stub
                if ((e.getStateChange() == ItemEvent.SELECTED)) {
                    updateTable();
                }
            }
        });
        cbInventory.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                // TODO Auto-generated method stub
                if ((e.getStateChange() == ItemEvent.SELECTED)) {
                    updateTable();
                }
            }
        });
        cbCategory.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                // TODO Auto-generated method stub
                if ((e.getStateChange() == ItemEvent.SELECTED)) {
                    updateTable();
                }
            }
        });
        updateTable();
    }

    public void updateOptions() {
        cbUser.removeAllItems();
        cbUser.addItem("ANY");
        List<String> ttUser = new ArrayList<String>();
        UserObjects userObjects = (UserObjects) DatabaseFacade.getDatabase("UserObjects");
        List<UserObject> userObjectList = userObjects.getList();
        for (UserObject userObject : userObjectList) {
            String userString = ValueFormatter.formatUserObject(userObject);
            cbUser.addItem(userString);
            ttUser.add(userString);
        }
        ttUser.sort(null);
        ttUser.add(0, "ANY");
        mcbrUser.setTooltips(ttUser);
        cbUser.setSelectedIndex(0);

        cbInventory.removeAllItems();
        cbInventory.addItem("ANY");
        List<String> ttInventory = new ArrayList<String>();
        Inventories inventories = (Inventories) DatabaseFacade.getDatabase("Inventories");
        List<Inventory> inventoryList = inventories.getList();
        for (Inventory inventory : inventoryList)
            cbInventory.addItem(ValueFormatter.formatInventory(inventory));
        for (int i = 1; i < cbInventory.getItemCount(); i++) {
            Inventory inventory = ValueFormatter.parseInventory(cbInventory.getItemAt(i));
            String inventoryString = inventory.getName();
            if (!inventory.getAccountNo().isEmpty())
                inventoryString += " [" + inventory.getAccountNo() + "]";
            ttInventory.add(inventoryString);
        }
        ttInventory.add(0, "ANY");
        mcbrInventory.setTooltips(ttInventory);
        cbInventory.setSelectedIndex(0);

        cbCategory.removeAllItems();
        cbCategory.addItem("ANY");
        List<String> ttCategory = new ArrayList<String>();
        TransactionCategories categories = (TransactionCategories) DatabaseFacade.getDatabase("TransactionCategories");
        List<TransactionCategory> categoryList = categories.getList();
        for (TransactionCategory category : categoryList) {
            cbCategory.addItem(category.getName());
            ttCategory.add(category.getName());
        }
        ttCategory.sort(null);
        ttCategory.add(0, "ANY");
        mcbrCategory.setTooltips(ttCategory);
        cbCategory.setSelectedIndex(0);
    }

    public void resetSelections() {
        cbUser.setSelectedIndex(0);
        cbInventory.setSelectedIndex(0);
        cbCategory.setSelectedIndex(0);
        fromDate = null;
        toDate = null;
        dcFrom.setDate(null);
        dcTo.setDate(null);
        updateTable();
    }

    private boolean isUndelivered(TransactionWrapper transaction) {
        String type = transaction.getId().substring(0, 2);
        int id = Integer.parseInt(transaction.getId().substring(3, transaction.getId().length()));
        switch (type) {
            case "ST": {
                SaleTransactions db = (SaleTransactions) DatabaseFacade.getDatabase("SaleTransactions");
                SaleTransaction st = db.get(db.find(id));
                return st.getItemStatus().equals(TransactionStatus.UNDELIVERED);
            }
            case "PT": {
                PurchaseTransactions db = (PurchaseTransactions) DatabaseFacade.getDatabase("PurchaseTransactions");
                PurchaseTransaction pt = db.get(db.find(id));
                return pt.getItemStatus().equals(TransactionStatus.UNDELIVERED);
            }
            default:
                return true;
        }
    }

    private void updateTable() {
        dtm.setRowCount(0);
        List<TransactionWrapper> transactions = new ArrayList<TransactionWrapper>();
        Transactions dbTransaction = (Transactions) DatabaseFacade.getDatabase("Transactions");
        List<cores.Transaction> lstTransaction = dbTransaction.getList();
        SaleTransactions dbSales = (SaleTransactions) DatabaseFacade.getDatabase("SaleTransactions");
        List<SaleTransaction> lstSales = dbSales.getList();
        PurchaseTransactions dbPurchases = (PurchaseTransactions) DatabaseFacade.getDatabase("PurchaseTransactions");
        List<PurchaseTransaction> lstPurchases = dbPurchases.getList();

        for (cores.Transaction t : lstTransaction)
            transactions.add(new TransactionWrapper(t));
        for (SaleTransaction st : lstSales)
            transactions.add(new TransactionWrapper(st));
        for (PurchaseTransaction pt : lstPurchases)
            transactions.add(new TransactionWrapper(pt));

        String selectedUser = (String) cbUser.getSelectedItem();
        if (!selectedUser.equals("ANY")) {
            UserObject userObject = ValueFormatter.parseUserObject(selectedUser);
            List<TransactionWrapper> userTransactions = new ArrayList<TransactionWrapper>();
            for (TransactionWrapper transaction : transactions)
                if (transaction.getUserObject().equals(userObject))
                    userTransactions.add(transaction);
            transactions = userTransactions;
        }
        if (fromDate != null) {
            List<TransactionWrapper> datedTransactions = new ArrayList<TransactionWrapper>();
            for (TransactionWrapper transaction : transactions)
                if (transaction.getDate().after(fromDate) || transaction.getDate().equals(fromDate))
                    datedTransactions.add(transaction);
            transactions = datedTransactions;
        }
        if (toDate != null) {
            List<TransactionWrapper> datedTransactions = new ArrayList<TransactionWrapper>();
            for (TransactionWrapper transaction : transactions)
                if (transaction.getDate().before(toDate) || transaction.getDate().equals(toDate))
                    datedTransactions.add(transaction);
            transactions = datedTransactions;
        }
        String selectedInventory = (String) cbInventory.getSelectedItem();
        if (!selectedInventory.equals("ANY")) {
            List<TransactionWrapper> inventoryTransactions = new ArrayList<TransactionWrapper>();
            Inventory inventory = ValueFormatter.parseInventory(selectedInventory);
            for (TransactionWrapper transaction : transactions)
                if (transaction.getInventory() != null && transaction.getInventory().equals(inventory))
                    inventoryTransactions.add(transaction);
            transactions = inventoryTransactions;
        }
        String selectedCategory = (String) cbCategory.getSelectedItem();
        if (!selectedCategory.equals("ANY")) {
            List<TransactionWrapper> categoryTransactions = new ArrayList<TransactionWrapper>();
            for (TransactionWrapper transaction : transactions)
                if (transaction.getCategory().equals(selectedCategory))
                    categoryTransactions.add(transaction);
            transactions = categoryTransactions;
        }

        if (btnSort.getText().equals(SORT_BY_DATE))
            Collections.sort(transactions, new Comparator<TransactionWrapper>() {
                public int compare(TransactionWrapper t1, TransactionWrapper t2) {
                    return t2.getId().compareTo(t1.getId());
                }
            });
        else
            Collections.sort(transactions, new Comparator<TransactionWrapper>() {
                public int compare(TransactionWrapper t1, TransactionWrapper t2) {
                    return t2.getDate().compareTo(t1.getDate());
                }
            });

        UserRecords dbUserRecords = (UserRecords) DatabaseFacade.getDatabase("UserRecords");
        int count = 0;
        for (int i = 0; i < transactions.size() && count <= 1000; i++) {
            TransactionWrapper transaction = transactions.get(i);
            if (transaction.getId().startsWith("ST") || transaction.getId().startsWith("PT"))
                if (isUndelivered(transaction))
                    continue;
            String credit = "", debit = "";
            if (transaction.isCredit()) {
                credit = "+" + ValueFormatter
                        .formatMoneyNicely(RecordFacade.calculateAmountWithTax(transaction.getAmount(), transaction.getTax()));
            } else
                debit =
                        "-" + ValueFormatter.formatMoneyNicely(RecordFacade.calculateAmountWithTax(transaction.getAmount()
                                , transaction.getTax()));
            UserRecord userRecord = dbUserRecords
                    .get(dbUserRecords.find(transaction.getId(), transaction.getUserObject()));
            String balance = ValueFormatter.formatMoneyNicely(userRecord.getBalance());
            if (userRecord.getBalance() > 0)
                balance = "+" + balance;
            dtm.addRow(new Object[]{transaction.getId(), ValueFormatter.formatUserObject(transaction.getUserObject()),
                    credit, debit, balance, ValueFormatter.formatDate(transaction.getDate()),
                    ValueFormatter.formatInventory(transaction.getInventory()), transaction.getRemark(),
                    transaction.getCategory()});
            ++count;
        }
    }

    public void setCard(cards.Inventory card) {
        cardInventory = card;
    }

    public void setCard(cards.User card) {
        cardUser = card;
    }
}

package cards;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import cores.Inventory;
import cores.TransactionCategory;
import cores.UserObject;
import databases.Inventories;
import databases.TransactionCategories;
import databases.UserCategories;
import globals.DatabaseFacade;
import globals.ReportFacade;
import globals.ValueFormatter;
import globals._Settings;
import helpers.MyComboBoxRenderer;
import helpers.SortedComboBoxModel;
import net.miginfocom.swing.MigLayout;

public class TransactionReport1 extends JPanel implements IReport {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private JTextField txtTitle;
    private Date toDate, fromDate;
    private JDateChooser dcFrom, dcTo;
    private JComboBox<String> cbCategory;
    private UserObject user;
    private Inventory inventory;
    private String transactionCategory;
    private String userCategory;
    private JPanel pnlTitle;
    private JPanel pnlDates;
    private JPanel pnlCustomer;
    private JPanel pnlColumns;
    private List<JCheckBox> chkbxColumns;
    private List<JLabel> lblColumns;
    private List<String> columns;
    private JLabel lblUser;
    private JComboBox<String> cbUser;
    private JPanel pnlInventory;
    private JLabel lblInventory;
    private JComboBox<String> cbInventory;
    private JPanel pnlCategory;
    private JLabel lblCategory2;
    private JComboBox<String> cbCategory2;
    private String[] columnHeaders = {"ID", "USER", "CREDIT", "DEBIT", "BALANCE", "DATE", "INVENTORY", "DESCRIPTION",
            "CATEGORY"};

    /**
     * Create the panel.
     */
    public TransactionReport1() {
        setBackground(_Settings.backgroundColor);
        setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        setLayout(new MigLayout("", "[grow]", "[][][][][][250]"));

        new ArrayList<JCheckBox>();

        pnlTitle = new JPanel();
        pnlTitle.setBackground(_Settings.backgroundColor);
        pnlTitle.setForeground(_Settings.labelColor);
        pnlTitle.setBorder(new TitledBorder(null, "Enter a title", TitledBorder.LEADING, TitledBorder.TOP, null,
                _Settings.textFieldColor));
        add(pnlTitle, "cell 0 0,grow");
        pnlTitle.setLayout(new MigLayout("", "[75][10][275]", "[50]"));

        JLabel lblTitle = new JLabel("Title:");
        pnlTitle.add(lblTitle, "cell 0 0,alignx right,growy");
        lblTitle.setBackground(_Settings.backgroundColor);
        lblTitle.setForeground(_Settings.labelColor);
        lblTitle.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTitle.setFont(new Font("Arial Black", Font.BOLD, 17));
        lblTitle.setBorder(null);

        txtTitle = new JTextField();
        pnlTitle.add(txtTitle, "cell 2 0,grow");
        txtTitle.setBackground(_Settings.backgroundColor);
        txtTitle.setForeground(_Settings.textFieldColor);
        txtTitle.setDisabledTextColor(_Settings.textFieldColor);
        txtTitle.setHorizontalAlignment(SwingConstants.CENTER);
        txtTitle.setFont(new Font("Century Gothic", Font.BOLD, 17));
        txtTitle.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        txtTitle.setColumns(10);

        pnlDates = new JPanel();
        pnlDates.setBackground(_Settings.backgroundColor);
        pnlDates.setForeground(_Settings.labelColor);
        pnlDates.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Select date range",
                TitledBorder.LEADING, TitledBorder.TOP, null, _Settings.textFieldColor));
        add(pnlDates, "cell 0 1,grow");
        pnlDates.setLayout(new MigLayout("", "[75,fill][10][275]", "[50][50]"));

        JLabel lblFrom = new JLabel("From:");
        pnlDates.add(lblFrom, "cell 0 0,alignx right,growy");
        lblFrom.setBackground(_Settings.backgroundColor);
        lblFrom.setForeground(_Settings.labelColor);
        lblFrom.setHorizontalAlignment(SwingConstants.RIGHT);
        lblFrom.setFont(new Font("Arial Black", Font.BOLD, 17));
        lblFrom.setBorder(null);

        dcFrom = new JDateChooser();
        pnlDates.add(dcFrom, "cell 2 0,grow");
        dcFrom.setFont(new Font("Century Gothic", Font.BOLD, 17));
        dcFrom.setBorder(null);
        dcFrom.setDateFormatString("dd-MMM-yyyy");

        JLabel lblTo = new JLabel("To:");
        pnlDates.add(lblTo, "cell 0 1,alignx right,growy");
        lblTo.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTo.setBackground(_Settings.backgroundColor);
        lblTo.setForeground(_Settings.labelColor);
        lblTo.setFont(new Font("Arial Black", Font.BOLD, 17));
        lblTo.setBorder(null);

        dcTo = new JDateChooser();
        pnlDates.add(dcTo, "cell 2 1,grow");
        dcTo.setFont(new Font("Century Gothic", Font.BOLD, 17));
        dcTo.setBorder(null);
        dcTo.setDateFormatString("dd-MMM-yyyy");
        JTextFieldDateEditor deTo = (JTextFieldDateEditor) dcTo.getComponent(1);
        dcTo.addPropertyChangeListener("date", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent e) {
                // TODO Auto-generated method stub
                toDate = (Date) e.getNewValue();
                toDate = ValueFormatter.setTimeToZero(toDate);
                dcFrom.setMaxSelectableDate(toDate);
            }
        });
        JTextFieldDateEditor deFrom = (JTextFieldDateEditor) dcFrom.getComponent(1);
        dcFrom.addPropertyChangeListener("date", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent e) {
                // TODO Auto-generated method stub
                fromDate = (Date) e.getNewValue();
                fromDate = ValueFormatter.setTimeToZero(fromDate);
                dcTo.setMinSelectableDate(fromDate);
            }
        });
        deFrom.setHorizontalAlignment(JTextField.CENTER);
        deFrom.setEnabled(false);
        deFrom.setDisabledTextColor(Color.DARK_GRAY);
        deTo.setHorizontalAlignment(JTextField.CENTER);
        deTo.setEnabled(false);
        deTo.setDisabledTextColor(Color.DARK_GRAY);

        pnlCustomer = new JPanel();
        pnlCustomer.setBackground(_Settings.backgroundColor);
        pnlCustomer.setForeground(_Settings.labelColor);
        pnlCustomer.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Select a user",
                TitledBorder.LEADING, TitledBorder.TOP, null, _Settings.textFieldColor));
        add(pnlCustomer, "cell 0 2,grow");
        pnlCustomer.setLayout(new MigLayout("", "[110][10][240]", "[50][50]"));

        JLabel lblCategory = new JLabel("Category:");
        pnlCustomer.add(lblCategory, "cell 0 0,alignx right,growy");
        lblCategory.setHorizontalAlignment(SwingConstants.RIGHT);
        lblCategory.setBackground(_Settings.backgroundColor);
        lblCategory.setForeground(_Settings.labelColor);
        lblCategory.setFont(new Font("Arial Black", Font.BOLD, 17));
        lblCategory.setBorder(null);

        UserCategories dbCategory = (UserCategories) DatabaseFacade.getDatabase("UserCategories");
        List<String> lstCategories = dbCategory.getList();
        Collections.sort(lstCategories);

        MyComboBoxRenderer mcbrCategory = new MyComboBoxRenderer();
        List<String> ttCategory = new ArrayList<String>();
        ttCategory.add("ANY");
        SortedComboBoxModel<String> scbmCategory = new SortedComboBoxModel<String>();
        scbmCategory.addElement("ANY");
        Iterator<String> iterator = lstCategories.iterator();
        while (iterator.hasNext()) {
            String customerString = iterator.next();
            scbmCategory.addElement(customerString);
            ttCategory.add(customerString);
        }
        ttCategory.set(0, "ANY");
        mcbrCategory.setTooltips(ttCategory);

        cbCategory = new JComboBox<String>(scbmCategory);
        pnlCustomer.add(cbCategory, "cell 2 0,grow");
        cbCategory.setMaximumSize(new Dimension(240, 50));
        cbCategory.setRenderer(mcbrCategory);
        cbCategory.setForeground(Color.DARK_GRAY);
        cbCategory.setFont(new Font("Century Gothic", Font.BOLD, 17));
        cbCategory.setBorder(null);

        SortedComboBoxModel<String> scbmUser = new SortedComboBoxModel<String>();
        MyComboBoxRenderer mcbrUser = new MyComboBoxRenderer();
        cbUser = new JComboBox<String>(scbmUser);
        cbUser.setRenderer(mcbrUser);
        cbCategory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String category = (String) cbCategory.getSelectedItem();
                scbmUser.removeAllElements();
                if (category.equals("ANY")) {
                    cbUser.addItem("ANY");
                    cbUser.setSelectedIndex(0);
                    userCategory = null;
                } else {
                    List<UserObject> lstUsers = dbCategory.get(category);

                    List<String> ttUser = new ArrayList<String>();
                    SortedSet<String> list = new TreeSet<String>();
                    lstUsers.forEach((user) -> list.add(ValueFormatter.formatUserObject(user)));
                    Iterator<String> iterator = list.iterator();
                    scbmUser.addElement("ANY");
                    while (iterator.hasNext()) {
                        String customerString = iterator.next();
                        scbmUser.addElement(customerString);
                        ttUser.add(customerString);
                    }
                    mcbrUser.setTooltips(ttUser);
                    cbUser.setSelectedIndex(0);

                    userCategory = category;
                }
                cbUser.setModel(scbmUser);
            }
        });
        cbCategory.setSelectedIndex(0);

        lblUser = new JLabel("User:");
        lblUser.setHorizontalAlignment(SwingConstants.RIGHT);
        lblUser.setFont(new Font("Arial Black", Font.BOLD, 17));
        lblUser.setBorder(null);
        lblUser.setBackground(_Settings.backgroundColor);
        lblUser.setForeground(_Settings.labelColor);
        pnlCustomer.add(lblUser, "cell 0 1,alignx right,growy");

        cbUser.setMaximumSize(new Dimension(240, 50));
        cbUser.setForeground(Color.DARK_GRAY);
        cbUser.setFont(new Font("Century Gothic", Font.BOLD, 17));
        cbUser.setBorder(null);
        cbUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selection = (String) cbUser.getSelectedItem();
                if (selection == null)
                    return;
                else if (selection.equals("ANY"))
                    user = null;
                else
                    user = ValueFormatter.parseUserObject(selection);
            }
        });
        pnlCustomer.add(cbUser, "cell 2 1,grow");

        pnlInventory = new JPanel();
        pnlInventory.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Select an inventory", TitledBorder.LEADING, TitledBorder.TOP, null, _Settings.textFieldColor));
        pnlInventory.setBackground(_Settings.backgroundColor);
        pnlInventory.setForeground(_Settings.labelColor);
        add(pnlInventory, "cell 0 3,grow");
        pnlInventory.setLayout(new MigLayout("", "[110][10][240]", "[50]"));

        lblInventory = new JLabel("Inventory:");
        lblInventory.setHorizontalAlignment(SwingConstants.RIGHT);
        lblInventory.setBackground(_Settings.backgroundColor);
        lblInventory.setForeground(_Settings.labelColor);
        lblInventory.setFont(new Font("Arial Black", Font.BOLD, 17));
        lblInventory.setBorder(null);
        pnlInventory.add(lblInventory, "cell 0 0,alignx trailing,growy");

        Inventories dbInventory = (Inventories) DatabaseFacade.getDatabase("Inventories");
        List<Inventory> lstInventory = dbInventory.getList();
        MyComboBoxRenderer mcbrInventory = new MyComboBoxRenderer();
        List<String> ttInventory = new ArrayList<String>();
        ttInventory.add("ANY");
        SortedComboBoxModel<String> scbmInventory = new SortedComboBoxModel<String>();
        scbmInventory.addElement("ANY");
        Iterator<Inventory> iteratorInventory = lstInventory.iterator();
        while (iteratorInventory.hasNext()) {
            String inventoryString = ValueFormatter.formatInventory(iteratorInventory.next());
            scbmInventory.addElement(inventoryString);
            ttInventory.add(inventoryString);
        }
        ttInventory.set(0, "ANY");
        mcbrInventory.setTooltips(ttCategory);

        cbInventory = new JComboBox<String>(scbmInventory);
        cbInventory.setRenderer(mcbrInventory);
        cbInventory.setMaximumSize(new Dimension(240, 50));
        cbInventory.setForeground(Color.DARK_GRAY);
        cbInventory.setFont(new Font("Century Gothic", Font.BOLD, 17));
        cbInventory.setBorder(null);
        cbInventory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selection = (String) cbInventory.getSelectedItem();
                if (selection == null)
                    return;
                else if (selection.equals("ANY"))
                    inventory = null;
                else
                    inventory = ValueFormatter.parseInventory(selection);
            }
        });
        cbInventory.setSelectedIndex(0);
        pnlInventory.add(cbInventory, "cell 2 0,grow");

        pnlCategory = new JPanel();
        pnlCategory.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Select a transaction category", TitledBorder.LEADING, TitledBorder.TOP, null, _Settings.textFieldColor));
        pnlCategory.setBackground(_Settings.backgroundColor);
        pnlCategory.setForeground(_Settings.labelColor);
        add(pnlCategory, "cell 0 4,grow");
        pnlCategory.setLayout(new MigLayout("", "[110][10][240]", "[50]"));

        lblCategory2 = new JLabel("Category:");
        lblCategory2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblCategory2.setFont(new Font("Arial Black", Font.BOLD, 17));
        lblCategory2.setBorder(null);
        lblCategory2.setBackground(_Settings.backgroundColor);
        lblCategory2.setForeground(_Settings.labelColor);
        pnlCategory.add(lblCategory2, "cell 0 0,alignx right,growy");

        TransactionCategories dbCategories2 = (TransactionCategories) DatabaseFacade.getDatabase(
                "TransactionCategories");
        List<TransactionCategory> lstCategories2 = dbCategories2.getList();
        MyComboBoxRenderer mcbrCategory2 = new MyComboBoxRenderer();
        List<String> ttCategory2 = new ArrayList<String>();
        ttCategory2.add("ANY");
        SortedComboBoxModel<String> scbmCategory2 = new SortedComboBoxModel<String>();
        scbmCategory2.addElement("ANY");
        Iterator<TransactionCategory> iterator2 = lstCategories2.iterator();
        while (iterator2.hasNext()) {
            String category = iterator2.next().getName();
            scbmCategory2.addElement(category);
            ttCategory2.add(category);
        }
        ttCategory2.set(0, "ANY");
        mcbrCategory2.setTooltips(ttCategory);

        cbCategory2 = new JComboBox<String>(scbmCategory2);
        cbCategory2.setRenderer(mcbrCategory2);
        cbCategory2.setMaximumSize(new Dimension(240, 50));
        cbCategory2.setForeground(Color.DARK_GRAY);
        cbCategory2.setFont(new Font("Century Gothic", Font.BOLD, 17));
        cbCategory2.setBorder(null);
        cbCategory2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selection = (String) cbCategory2.getSelectedItem();
                if (selection == null)
                    return;
                else if (selection.equals("ANY"))
                    transactionCategory = null;
                else
                    transactionCategory = selection;
            }
        });
        cbCategory2.setSelectedIndex(0);
        pnlCategory.add(cbCategory2, "cell 2 0,grow");

        pnlColumns = new JPanel();
        pnlColumns.setForeground(_Settings.labelColor);
        pnlColumns.setBackground(_Settings.backgroundColor);
        pnlColumns.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Select one or more table columns in desired order", TitledBorder.LEADING, TitledBorder.TOP, null, _Settings.textFieldColor));

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(pnlColumns);
        add(scrollPane, "cell 0 5,grow");
        String rows = "";
        for (int i = 0; i < columnHeaders.length; i++)
            rows += "[30]";
        pnlColumns.setLayout(new MigLayout("", "[grow][15%]", rows));

        chkbxColumns = new ArrayList<JCheckBox>();
        lblColumns = new ArrayList<JLabel>();
        columns = new ArrayList<String>();
        for (int i = 0; i < columnHeaders.length; i++) {
            JCheckBox chkbx = new JCheckBox(columnHeaders[i]);
            chkbx.setFont(new Font("Century Gothic", Font.ITALIC, 17));
            chkbx.setBackground(_Settings.backgroundColor);
            chkbx.setForeground(_Settings.labelColor);
            chkbx.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    // TODO Auto-generated method stub
                    JCheckBox source = (JCheckBox) arg0.getSource();
                    for (int i = 0; i < chkbxColumns.size(); i++)
                        if (chkbxColumns.get(i).equals(source))
                            if (source.isSelected()) {
                                columns.add(source.getText());
                                lblColumns.get(i).setText(Integer.toString(columns.size()));
                                break;
                            } else {
                                columns.remove(source.getText());
                                lblColumns.get(i).setText("");
                                for (int j = 0; j < columns.size(); j++)
                                    for (int k = 0; k < chkbxColumns.size(); k++)
                                        if (chkbxColumns.get(k).getText().equals(columns.get(j)))
                                            lblColumns.get(k).setText(Integer.toString(j + 1));
                                break;
                            }
                }
            });
            pnlColumns.add(chkbx, "cell 0 " + i + ",grow");
            chkbxColumns.add(chkbx);
            JLabel lbl = new JLabel();
            lbl.setFont(new Font("Century Gothic", Font.ITALIC, 17));
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            lbl.setBackground(_Settings.backgroundColor);
            lbl.setForeground(_Settings.labelColor);
            pnlColumns.add(lbl, "cell 1 " + i + ",grow");
            lblColumns.add(lbl);
        }
    }

    @Override
    public boolean isReady() {
        // TODO Auto-generated method stub
        boolean atleastOneTrue = false;
        for (JCheckBox chkbx : chkbxColumns)
            if (chkbx.isSelected()) {
                atleastOneTrue = true;
                break;
            }
        return !txtTitle.getText().isEmpty() && fromDate != null && toDate != null && atleastOneTrue;
    }

    @Override
    public void generateReport() {
        // TODO Auto-generated method stub
        ReportFacade.generateTransactionReport1(txtTitle.getText().trim(), fromDate, toDate, userCategory, user, inventory, transactionCategory, columnHeaders, columns);
    }
}

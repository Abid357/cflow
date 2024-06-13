package frames;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import cores.Item;
import cores.Material;
import cores.Product;
import cores.Service;
import cores.Stock;
import cores.Store;
import cores.Transaction;
import databases.Items;
import databases.Productions;
import databases.Stores;
import globals.DatabaseFacade;
import globals.ItemFacade;
import globals.LOGGER;
import globals.RecordFacade;
import globals.ValueFormatter;
import globals._Settings;
import helpers.ItemType;
import helpers.MyComboBoxRenderer;
import helpers.SortedComboBoxModel;
import helpers.TransactionStatus;
import helpers.TransactionType;
import net.miginfocom.swing.MigLayout;

public class Production extends JDialog {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final String STOCK_UNAVAILABLE = "STOCK UNAVAILABLE";
    boolean containsZero, isProductSelected, isDateEmpty = true, allMaterialsSelected, isProductStoreSelected;
    private JPanel tabMain, tabMaterial, tabProduct;
    private JTabbedPane tabs;
    private JTextField txtId;
    private List<Transaction> costs;
    private JLabel lblId;
    private JLabel lblProduct;
    private JLabel lblstartDate;
    private JComboBox<String> cbProduct;
    private JDateChooser dateChooser;
    private JScrollPane spMain;
    private int count = 0;
    private JButton btnIncrement;
    private JButton btnDecrement;
    private List<JComboBox<String>> cbMaterials;
    private List<JTextField> quantitys;
    private List<JLabel> quantitys2;
    private List<JLabel> prices2;
    private JLabel lblProductUnit;
    private JComboBox<String> cbProductUnit;
    private Product selectedProduct;
    private JLabel lblProductQuantity;
    private JTextField txtProductQuantity;
    private JButton btnUpdate;
    private JButton btnComplete;
    private JButton btnPrint;
    private JButton btnClose;
    private JButton btnSave;
    private JTextField txtStatus;
    private JLabel lblStatus;
    private JLabel lblStatusValue;
    private JScrollPane spMaterial;
    private JLabel lblPrompt;
    private JButton btnOther;
    private JLabel lblUnitPrice;
    private JTextField txtUnitPrice;
    private JLabel lblTotalCost;
    private JTextField txtTotalCost;
    private JLabel lblPrompt2;
    private JLabel lblProduct2;
    private JLabel lblQuantity3;
    private JLabel lblUnit3;
    private JLabel lblProductStorage;
    private JLabel lblProduct2Text;
    private JLabel lblQuantityText;
    private JLabel lblUnitText;
    private JComboBox<String> cbProductStorage;
    private List<JComboBox<String>> cbMaterialStorages;
    private JLabel lblCosts;
    private JTextField txtCosts;
    private Date date;
    private ItemListener ilProduct;
    private ChangeListener clTab;
    private JLabel lblUnitInTab2;
    private double otherCost;
    private cores.Production production;

    /**
     * Create the frame.
     */
    public Production(Window owner, cores.Production production) {
        super(owner, "Production", Dialog.ModalityType.DOCUMENT_MODAL);
        setTitle("Production");
        setResizable(false);
        setBounds(100, 100, 690, 103);

        quantitys = new ArrayList<JTextField>();
        quantitys2 = new ArrayList<JLabel>();
        prices2 = new ArrayList<JLabel>();
        cbMaterials = new ArrayList<JComboBox<String>>();
        costs = new ArrayList<Transaction>();
        cbMaterialStorages = new ArrayList<JComboBox<String>>();
        otherCost = 0;

        UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", false);
        UIManager.put("ComboBox.disabledForeground", _Settings.labelColor);
        UIManager.put("TextField.inactiveForeground", _Settings.labelColor);
        UIManager.put("TextArea.inactiveForeground", _Settings.labelColor);

        tabMain = new JPanel();
        tabMain.setBackground(_Settings.backgroundColor);
        tabMain.setBorder(null);
        tabMain.setLayout(new MigLayout("",
                "[10][60][90][10][40][30][80][grow][10][140][30][10][80][10][20][10][40][50][10][50,grow][10]",
                "[10][40][10][40][10][40][10][400][20][20][10][60][10][20][10][60][10]"));

        tabMaterial = new JPanel();
        tabMaterial.setBackground(_Settings.backgroundColor);
        tabMaterial.setBorder(null);

        tabProduct = new JPanel();
        tabProduct.setMaximumSize(new Dimension(400, 60));
        tabProduct.setBackground(_Settings.backgroundColor);
        tabProduct.setBorder(null);

        // TAB 1 - MAIN

        tabs = new JTabbedPane();
        tabs.setBackground(_Settings.backgroundColor);
        tabs.setForeground(_Settings.labelColor);
        tabs.setBorder(null);
        tabs.setFont(new Font("Arial", Font.BOLD, 17));
        tabs.add("Main", tabMain);

        lblId = new JLabel("Production ID");
        lblId.setHorizontalAlignment(SwingConstants.CENTER);
        lblId.setForeground(_Settings.labelColor);
        lblId.setFont(new Font("Arial Black", Font.PLAIN, 11));
        lblId.setBorder(null);
        tabMain.add(lblId, "cell 1 0 4 1,grow");

        txtId = new JTextField();
        txtId.setHorizontalAlignment(SwingConstants.CENTER);
        txtId.setForeground(_Settings.labelColor);
        txtId.setFont(new Font("Century Gothic", Font.BOLD, 21));
        txtId.setEditable(false);
        txtId.setColumns(5);
        txtId.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        txtId.setBackground(_Settings.backgroundColor);
        tabMain.add(txtId, "cell 1 1 4 3,grow");

        lblProduct = new JLabel("Finished Product");
        lblProduct.setHorizontalAlignment(SwingConstants.RIGHT);
        lblProduct.setForeground(_Settings.labelColor);
        lblProduct.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblProduct.setBorder(null);
        lblProduct.setBackground(_Settings.backgroundColor);
        tabMain.add(lblProduct, "cell 6 1 2 1,grow");

        Items dbItems = (Items) DatabaseFacade.getDatabase("Items");
        List<Product> products = dbItems.getProductList();
        SortedComboBoxModel<String> scbmProduct = new SortedComboBoxModel<String>(true);
        MyComboBoxRenderer mcbrProduct = new MyComboBoxRenderer();
        List<String> ttProduct = new ArrayList<String>();
        for (Product product : products) {
            String productString = ValueFormatter.formatItemForComboBox(product);
            scbmProduct.addElement(productString);
            ttProduct.add(productString);
        }

        Collections.sort(ttProduct, new Comparator<String>() {

            @Override
            public int compare(String arg0, String arg1) {
                // TODO Auto-generated method stub
                String string0 = arg0.substring(arg0.indexOf(" ") + 1);
                String string1 = arg1.substring(arg1.indexOf(" ") + 1);
                return string0.compareTo(string1);
            }
        });
        mcbrProduct.setTooltips(ttProduct);

        cbProduct = new JComboBox<String>(scbmProduct);
        cbProduct.setRenderer(mcbrProduct);
        cbProduct.setFont(new Font("Century Gothic", Font.BOLD, 17));
        cbProduct.setForeground(Color.DARK_GRAY);
        cbProduct.setSelectedIndex(-1);
        ilProduct = new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                // TODO Auto-generated method stub
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    selectedProduct = (Product) ValueFormatter
                            .parseItemFromComboBox((String) cbProduct.getSelectedItem());

                    cbProductUnit.removeAllItems();
                    String unit = selectedProduct.getUnit();
                    cbProductUnit.addItem(unit); // check if product has no unit other than pc
                    if (!unit.equals("pc"))
                        cbProductUnit.addItem("pc");
                    cbProductUnit.setSelectedItem(unit);

                    // remove existing material selections
                    while (count != 0) {
                        btnDecrement.doClick();
                    }
                    List<Stock> stocks = selectedProduct.getComponents();
                    if (stocks != null && !stocks.isEmpty()) {
                        for (Stock stock : stocks) {
                            btnIncrement.doClick();
                            JComboBox<String> cb = cbMaterials.get(cbMaterials.size() - 1);
                            cb.setSelectedItem(ValueFormatter.formatItemForComboBox(stock.getItem()));
                        }
                    }
                    btnUpdate.doClick();

                    isProductSelected = true;
                    cbProductStorage.setSelectedIndex(-1);
                    isProductStoreSelected = false;
                    validateButton();
                }
            }
        };
        cbProduct.addItemListener(ilProduct);
        tabMain.add(cbProduct, "cell 9 1 11 1,grow");

        lblstartDate = new JLabel("Start Date");
        lblstartDate.setHorizontalAlignment(SwingConstants.RIGHT);
        lblstartDate.setForeground(_Settings.labelColor);
        lblstartDate.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblstartDate.setBorder(null);
        lblstartDate.setBackground(_Settings.backgroundColor);
        tabMain.add(lblstartDate, "cell 6 3 2 1,grow");

        dateChooser = new JDateChooser();
        dateChooser.setMaximumSize(new Dimension(210, 2147483647));
        dateChooser.setFont(new Font("Century Gothic", Font.BOLD, 17));
        dateChooser.setBorder(null);
        dateChooser.setDateFormatString("dd-MMM-yyyy");
        JTextFieldDateEditor dateEditor = (JTextFieldDateEditor) dateChooser.getComponent(1);
        dateChooser.addPropertyChangeListener("date", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent e) {
                // TODO Auto-generated method stub
                date = (Date) e.getNewValue();
                isDateEmpty = false;
                validateButton();
            }
        });
        dateEditor.setHorizontalAlignment(JTextField.CENTER);
        dateEditor.setEnabled(false);
        dateEditor.setDisabledTextColor(Color.DARK_GRAY);
        tabMain.add(dateChooser, "cell 9 3 2 1,grow");

        lblProductUnit = new JLabel("Unit");
        lblProductUnit.setHorizontalAlignment(SwingConstants.RIGHT);
        lblProductUnit.setForeground(_Settings.labelColor);
        lblProductUnit.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblProductUnit.setBorder(null);
        lblProductUnit.setBackground(_Settings.backgroundColor);
        tabMain.add(lblProductUnit, "cell 12 3,grow");

        cbProductUnit = new JComboBox<String>();
        cbProductUnit.setFont(new Font("Century Gothic", Font.BOLD, 17));
        cbProductUnit.setForeground(Color.DARK_GRAY);
        cbProductUnit.setSelectedIndex(-1);
        tabMain.add(cbProductUnit, "cell 14 3 6 1,grow");

        lblProductQuantity = new JLabel("Quantity");
        lblProductQuantity.setHorizontalAlignment(SwingConstants.RIGHT);
        lblProductQuantity.setForeground(_Settings.labelColor);
        lblProductQuantity.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblProductQuantity.setBorder(null);
        lblProductQuantity.setBackground(_Settings.backgroundColor);
        tabMain.add(lblProductQuantity, "cell 6 5 2 1,grow");

        txtProductQuantity = new JTextField();
        txtProductQuantity.setText("1");
        txtProductQuantity.setHorizontalAlignment(SwingConstants.CENTER);
        txtProductQuantity.setForeground(_Settings.textFieldColor);
        txtProductQuantity.setFont(new Font("Century Gothic", Font.BOLD, 17));
        txtProductQuantity.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        txtProductQuantity.setBackground(_Settings.backgroundColor);
        txtProductQuantity.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent evt) {
                // TODO Auto-generated method stub
                try {
                    Double.parseDouble(txtProductQuantity.getText());
                } catch (NumberFormatException e) {
                    new MyOptionPane("Quantity must be a number!", MyOptionPane.INFORMATION_MESSAGE);
                    txtProductQuantity.setText("1");
                }
                validateButton();
            }

            @Override
            public void focusGained(FocusEvent e) {
                // TODO Auto-generated method stub
                txtProductQuantity.selectAll();
            }
        });
        tabMain.add(txtProductQuantity, "cell 9 5 2 1,grow");

        btnUpdate = new JButton("UPDATE");
        btnUpdate.setForeground(_Settings.labelColor);
        btnUpdate.setFont(new Font("Arial", Font.BOLD, 14));
        btnUpdate.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
        btnUpdate.setBackground(_Settings.backgroundColor);
        btnUpdate.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                if (selectedProduct != null) {

                    double quantity;
                    try {
                        quantity = ValueFormatter.parseQuantity(txtProductQuantity.getText());
                    } catch (NumberFormatException e1) {
                        return;
                    }

                    List<Stock> stocks = selectedProduct.getComponents();

                    String unitString = (String) cbProductUnit.getSelectedItem();
                    if (!unitString.equals("pc")) // no need for conversion
                        for (int i = 0; i < count; i++) {
                            String itemString = (String) cbMaterials.get(i).getSelectedItem();
                            if (itemString == null)
                                continue;
                            Item item =
                                    ValueFormatter.parseItemFromComboBox(itemString);
                            JTextField txt = quantitys.get(i);
                            int j = 0;
                            for (; j < stocks.size(); j++)
                                if (stocks.get(j).getItem().equals(item))
                                    break;
                            if (j == stocks.size())
                                txt.setText(ValueFormatter.formatQuantity(quantity));
                            else
                                txt.setText(ValueFormatter.formatQuantity(quantity * stocks.get(j).getQuantity()));
                        }
                    else {
                        for (int i = 0; i < count; i++) {
                            String itemString = (String) cbMaterials.get(i).getSelectedItem();
                            if (itemString == null)
                                continue;
                            Item item =
                                    ValueFormatter.parseItemFromComboBox(itemString);
                            JTextField txt = quantitys.get(i);
                            int j = 0;
                            for (; j < stocks.size(); j++)
                                if (stocks.get(j).getItem().equals(item))
                                    break;
                            if (j == stocks.size())
                                txt.setText(ValueFormatter.formatQuantity(quantity));
                            else
                                txt.setText(ValueFormatter.formatQuantity(
                                        quantity * (stocks.get(i).getQuantity() / selectedProduct.getConversion())));
                        }
                    }

                }
            }
        });
        tabMain.add(btnUpdate, "cell 12 5 2 1,grow");

        JPanel tab1Pane = new JPanel();
        tab1Pane.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        tab1Pane.setBackground(_Settings.backgroundColor);
        GridBagLayout gbl = new GridBagLayout();
        gbl.rowHeights = new int[]{40, 40, 40, 40, 40, 40, 40, 40, 40, 40};
        tab1Pane.setLayout(gbl);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
        panel.setBackground(_Settings.backgroundColor);
        panel.setBorder(null);

        JLabel lblNo = new JLabel("No.");
        lblNo.setPreferredSize(new Dimension(100, 40));
        lblNo.setHorizontalAlignment(SwingConstants.CENTER);
        lblNo.setForeground(_Settings.labelColor);
        lblNo.setBackground(_Settings.backgroundColor);
        lblNo.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblNo.setBorder(new LineBorder(Color.WHITE));
        panel.add(lblNo);

        JLabel lblMaterial = new JLabel("Material");
        lblMaterial.setPreferredSize(new Dimension(680, 40));
        lblMaterial.setHorizontalAlignment(SwingConstants.CENTER);
        lblMaterial.setForeground(_Settings.labelColor);
        lblMaterial.setBackground(_Settings.backgroundColor);
        lblMaterial.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblMaterial.setBorder(new LineBorder(Color.WHITE));
        panel.add(lblMaterial);

        JLabel lblQuantity = new JLabel("Quantity");
        lblQuantity.setPreferredSize(new Dimension(120, 40));
        lblQuantity.setHorizontalAlignment(SwingConstants.CENTER);
        lblQuantity.setForeground(_Settings.labelColor);
        lblQuantity.setBackground(_Settings.backgroundColor);
        lblQuantity.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblQuantity.setBorder(new LineBorder(Color.WHITE));
        panel.add(lblQuantity);

        JLabel lblUnit = new JLabel("Unit");
        lblUnit.setPreferredSize(new Dimension(120, 40));
        lblUnit.setHorizontalAlignment(SwingConstants.CENTER);
        lblUnit.setForeground(_Settings.labelColor);
        lblUnit.setBackground(_Settings.backgroundColor);
        lblUnit.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblUnit.setBorder(new LineBorder(Color.WHITE));
        panel.add(lblUnit);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        tab1Pane.add(panel, gbc);

        spMain = new JScrollPane(tab1Pane);
        tabMain.add(spMain, "cell 1 7 19 1,grow");

        btnDecrement = new JButton("-");
        btnDecrement.setMinimumSize(new Dimension(37, 20));
        btnDecrement.setEnabled(false);
        btnDecrement.setForeground(_Settings.labelColor);
        btnDecrement.setFont(new Font("Arial Black", Font.PLAIN, 19));
        btnDecrement.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
        btnDecrement.setBackground(_Settings.backgroundColor);
        btnDecrement.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                tab1Pane.remove(count--);
                removeMaterial();
                if (count == 0)
                    btnDecrement.setEnabled(false);
                revalidate();
                repaint();
            }
        });
        tabMain.add(btnDecrement, "cell 17 8 1 2,grow");

        btnIncrement = new JButton("+");
        btnIncrement.setMinimumSize(new Dimension(41, 20));
        btnIncrement.setForeground(_Settings.labelColor);
        btnIncrement.setFont(new Font("Arial Black", Font.PLAIN, 19));
        btnIncrement.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
        btnIncrement.setBackground(_Settings.backgroundColor);
        btnIncrement.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = count + 1;
                tab1Pane.add(addMaterialInTab1(), gbc);
                btnDecrement.setEnabled(true);
                revalidate();
                repaint();
            }
        });
        tabMain.add(btnIncrement, "cell 19 8 1 2,grow");

        lblStatus = new JLabel("Status:");
        lblStatus.setHorizontalAlignment(SwingConstants.LEFT);
        lblStatus.setForeground(_Settings.labelColor);
        lblStatus.setFont(new Font("Arial Black", Font.PLAIN, 14));
        lblStatus.setBorder(null);
        lblStatus.setBackground(_Settings.backgroundColor);
        tabMain.add(lblStatus, "cell 1 9,grow");

        lblStatusValue = new JLabel();
        lblStatusValue.setHorizontalAlignment(SwingConstants.LEFT);
        lblStatusValue.setForeground(_Settings.textFieldColor);
        lblStatusValue.setFont(new Font("Century Gothic", Font.BOLD | Font.ITALIC, 14));
        lblStatusValue.setBorder(null);
        lblStatusValue.setBackground(_Settings.backgroundColor);
        tabMain.add(lblStatusValue, "cell 2 9 3 1,grow");

        btnComplete = new JButton("COMPLETE");
        btnComplete.setMinimumSize(new Dimension(73, 23));
        btnComplete.setPreferredSize(new Dimension(73, 23));
        btnComplete.setForeground(_Settings.labelColor);
        btnComplete.setFont(new Font("Arial Black", Font.PLAIN, 19));
        btnComplete.setEnabled(false);
        btnComplete.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
        btnComplete.setBackground(_Settings.backgroundColor);
        btnComplete.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                cores.Production production = getProduction();
                if (completeProduction()) {
                    String message = "Select completion date:\n";
                    MyOptionPane mop = new MyOptionPane(message, MyOptionPane.DATE_DIALOG_BOX,
                            production.getStartDate());
                    if (mop.getSelection() == MyOptionPane.POSITIVE && mop.getDate() != null) {
                        String balanceId = ValueFormatter.formatBalanceId(production.getId(), cores.Production.class);
                        production.setEndDate(mop.getDate());

                        // update production object variables i.e. materials, storages, costs etc.
                        List<Store> materialStores = new ArrayList<Store>();
                        List<Stock> materials = production.getMaterials();
                        for (int i = 0; i < cbMaterialStorages.size(); i++) {
                            materialStores.add(
                                    ValueFormatter.parseStore((String) cbMaterialStorages.get(i).getSelectedItem()));
                            materials.get(i).setPrice(ValueFormatter.parseMoney(prices2.get(i).getText()));
                        }
                        production.setMaterialStores(materialStores);
                        production.getProduct().setPrice(calculateUnitPrice(calculateMaterialCost() + otherCost));

                        // update inventory records for each material store
                        Set<String> stringSet = new HashSet<String>();
                        for (int i = 0; i < cbMaterialStorages.size(); i++)
                            stringSet.add((String) cbMaterialStorages.get(i).getSelectedItem());
                        List<String> stringArray = new ArrayList<String>(stringSet);

                        for (int i = 0; i < stringArray.size(); i++) {
                            double total = 0.0;
                            Store store = null;
                            for (int j = 0; j < materials.size(); j++) {
                                String storeString = (String) cbMaterialStorages.get(j).getSelectedItem();
                                if (stringArray.get(i).equals(storeString)) {
                                    store = ValueFormatter.parseStore(storeString);
                                    Stock material = materials.get(j);
                                    if (ItemFacade.unstock(store, material.getItem(), material.getQuantity()))
                                        if (!material.getItem().getType().equals(ItemType.SERVICE)) {
                                            total += material.getPrice() * material.getQuantity();
                                        }
                                }
                            }
                            if (total != 0.0) {
                                RecordFacade.record(store.getInventory(), total, 0.0, false, null);
                                RecordFacade.addInventoryRecord(store.getInventory(), total, 0.0, false, balanceId,
                                        production.getEndDate());
                            }
                        }

                        // update inventory records for product store
                        Store store = production.getProductStore();
                        Stock product = production.getProduct();
                        double total = product.getPrice() * product.getQuantity();
                        if (ItemFacade.stock(store, product.getItem(), product.getQuantity(), product.getPrice())) {
                            RecordFacade.record(store.getInventory(), total, 0.0, true, null);
                            RecordFacade.addInventoryRecord(store.getInventory(), total, 0.0, true, balanceId,
                                    production.getEndDate());
                        }

                        production.setStatus(TransactionStatus.COMPLETE);
                        txtStatus.setText("Production completed!");
                        lblStatusValue.setText(TransactionStatus.COMPLETE.toString());
                        LOGGER.Activity.log("Production", LOGGER.COMPLETE, production.getAlternativeId());
                        setFormEnabled(false);
                        ((Main) owner).cardItem.calculateStock();
                        ((Main) owner).cardItem.updateTable();
                        ((Main) owner).cardStore.updateTable();
                        ((Main) owner).cardProduction.updateTable();
                        new MyOptionPane("Production completed!", MyOptionPane.INFORMATION_MESSAGE);
                    } else
                        new MyOptionPane("Production could not be completed!", MyOptionPane.ERROR_DIALOG_BOX);
                } else
                    new MyOptionPane("Production could not be completed!", MyOptionPane.ERROR_DIALOG_BOX);
            }
        });
        tabMain.add(btnComplete, "cell 1 11 2 2,grow");

        btnOther = new JButton("OTHER");
        btnOther.setPreferredSize(new Dimension(73, 23));
        btnOther.setMinimumSize(new Dimension(73, 23));
        btnOther.setForeground(_Settings.labelColor);
        btnOther.setFont(new Font("Arial Black", Font.PLAIN, 19));
        btnOther.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
        btnOther.setBackground(_Settings.backgroundColor);
        btnOther.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                frames.Transaction frame = new frames.Transaction(getThis(), getCosts(),
                        TransactionType.PRODUCTION_COST);
                frame.addWindowListener(new WindowAdapter() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void windowClosed(WindowEvent e) {
                        // TODO Auto-generated method stub
                        costs = (List<Transaction>) frame.getTransaction();
                        otherCost = 0.0;
                        if (!costs.isEmpty()) {
                            for (Transaction t : costs)
                                otherCost += t.getAmount() + t.getTax();
                            txtCosts.setText(ValueFormatter.formatMoneyNicely(otherCost));
                            double oldCost = ValueFormatter.parseMoney(txtCosts.getText());
                            if (otherCost != oldCost)
                                txtStatus.setText("Transaction added!");
                        }
                    }
                });
            }
        });
        tabMain.add(btnOther, "cell 4 11 3 2,grow");

        lblCosts = new JLabel("Other Costs");
        lblCosts.setHorizontalAlignment(SwingConstants.RIGHT);
        lblCosts.setForeground(_Settings.labelColor);
        lblCosts.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblCosts.setBorder(null);
        lblCosts.setBackground(_Settings.backgroundColor);
        tabMain.add(lblCosts, "cell 10 11 3 1,grow");

        txtCosts = new JTextField();
        txtCosts.setText("0.00");
        txtCosts.setHorizontalAlignment(SwingConstants.CENTER);
        txtCosts.setForeground(_Settings.textFieldColor);
        txtCosts.setFont(new Font("Century Gothic", Font.BOLD, 21));
        txtCosts.setEditable(false);
        txtCosts.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        txtCosts.setBackground(_Settings.backgroundColor);
        tabMain.add(txtCosts, "cell 14 11 6 1,grow");

        txtStatus = new JTextField();
        txtStatus.setEditable(false);
        txtStatus.setBackground(null);
        txtStatus.setForeground(_Settings.labelColor);
        txtStatus.setBorder(null);
        txtStatus.setHorizontalAlignment(SwingConstants.RIGHT);
        txtStatus.setFont(new Font("Arial Black", Font.PLAIN, 15));
        txtStatus.setColumns(10);
        tabMain.add(txtStatus, "cell 10 13 10 1,grow");

        btnPrint = new JButton("PRINT");
        btnPrint.setEnabled(false);
        btnPrint.setForeground(_Settings.labelColor);
        btnPrint.setFont(new Font("Arial Black", Font.PLAIN, 19));
        btnPrint.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
        btnPrint.setBackground(_Settings.backgroundColor);
        tabMain.add(btnPrint, "cell 1 15 2 1,grow");

        btnClose = new JButton("CLOSE");
        btnClose.setForeground(_Settings.labelColor);
        btnClose.setFont(new Font("Arial Black", Font.PLAIN, 19));
        btnClose.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
        btnClose.setBackground(_Settings.backgroundColor);
        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                dispose();
            }
        });
        tabMain.add(btnClose, "cell 10 15 5 1,grow");

        btnSave = new JButton("SAVE");
        btnSave.setEnabled(false);
        btnSave.setForeground(_Settings.labelColor);
        btnSave.setFont(new Font("Arial Black", Font.PLAIN, 19));
        btnSave.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
        btnSave.setBackground(_Settings.backgroundColor);
        btnSave.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                tabs.setSelectedIndex(1); // quickly switch between tabs to update tab 2 components
                tabs.setSelectedIndex(0);
                validateButton();
                if (!btnSave.isEnabled())
                    return;

                Productions dbProductions = (Productions) DatabaseFacade.getDatabase("Productions");
                int id = dbProductions.maxID() + 1;
                double quantityProduct = ValueFormatter.parseQuantity(txtProductQuantity.getText());
                double unitPriceProduct = calculateUnitPrice(calculateMaterialCost() + otherCost);
                String productUnit = (String) cbProductUnit.getSelectedItem();
                Stock product = new Stock(selectedProduct, quantityProduct, unitPriceProduct);
                Store productStore = ValueFormatter.parseStore((String) cbProductStorage.getSelectedItem());
                List<Stock> materials = new ArrayList<Stock>();
                List<Store> materialStores = new ArrayList<Store>();
                for (int i = 0; i < count; i++) {
                    Item material = ValueFormatter.parseItemFromComboBox((String) cbMaterials.get(i).getSelectedItem());
                    double quantityMaterial = ValueFormatter.parseQuantity(quantitys.get(i).getText());
                    String priceString = prices2.get(i).getText();
                    double unitPriceMaterial = -1.0;
                    if (!priceString.equals("-"))
                        unitPriceMaterial = ValueFormatter.parseMoney(priceString);
                    Stock stockMaterial = new Stock(material, quantityMaterial, unitPriceMaterial);
                    materials.add(stockMaterial);
                    String storeString = (String) cbMaterialStorages.get(i).getSelectedItem();
                    if (storeString.equals(STOCK_UNAVAILABLE))
                        materialStores.add(null);
                    else {
                        Store materialStore = ValueFormatter.parseStore(storeString);
                        materialStores.add(materialStore);
                    }
                }
                setProduction(new cores.Production(id, date, null, product, productStore, productUnit, materials,
                        materialStores, costs, TransactionStatus.ONGOING));
                getProduction().createAlternativeId();

                if (addProduction()) {
                    txtStatus.setText("Record saved!");
                    LOGGER.Activity.log("Production", LOGGER.ADD, getProduction().getAlternativeId());
                    lblStatusValue.setText(getProduction().getStatus().toString());
                    btnComplete.setEnabled(true);
                    btnPrint.setEnabled(true);
                    txtId.setText(getProduction().getAlternativeId());
                    setFormEnabled(false);
                    ((Main) owner).cardProduction.updateTable();
                } else
                    txtStatus.setText("An error occurred.");

            }
        });
        tabMain.add(btnSave, "cell 16 15 4 1,grow");

        // TAB 2 - MATERIAL

        tabs.add("Material", tabMaterial);
        tabMaterial.setLayout(
                new MigLayout("", "[10][grow][180][10][180][10][180][10][180][10][110][10]", "[40][700][10][50][10]"));

        lblPrompt = new JLabel("Select stores from which raw materials will be taken for production.");
        lblPrompt.setForeground(_Settings.labelColor);
        lblPrompt.setFont(new Font("Century Gothic", Font.BOLD | Font.ITALIC, 17));
        lblPrompt.setBorder(null);
        lblPrompt.setBackground(_Settings.backgroundColor);
        tabMaterial.add(lblPrompt, "cell 1 0 5 1,grow");

        JPanel tab2Pane = new JPanel();
        tab2Pane.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        tab2Pane.setBackground(_Settings.backgroundColor);
        GridBagLayout gbl2 = new GridBagLayout();
        gbl2.rowHeights = new int[]{60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60};
        tab2Pane.setLayout(gbl2);

        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
        panel2.setBackground(_Settings.backgroundColor);
        panel2.setBorder(null);

        JLabel lblNo2 = new JLabel("No.");
        lblNo2.setPreferredSize(new Dimension(60, 30));
        lblNo2.setHorizontalAlignment(SwingConstants.CENTER);
        lblNo2.setForeground(_Settings.labelColor);
        lblNo2.setBackground(_Settings.backgroundColor);
        lblNo2.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblNo2.setBorder(new LineBorder(Color.WHITE));
        panel2.add(lblNo2);

        JLabel lblMaterial2 = new JLabel("Material");
        lblMaterial2.setPreferredSize(new Dimension(250, 30));
        lblMaterial2.setHorizontalAlignment(SwingConstants.CENTER);
        lblMaterial2.setForeground(_Settings.labelColor);
        lblMaterial2.setBackground(_Settings.backgroundColor);
        lblMaterial2.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblMaterial2.setBorder(new LineBorder(Color.WHITE));
        panel2.add(lblMaterial2);

        JLabel lblQuantity2 = new JLabel("Quantity");
        lblQuantity2.setPreferredSize(new Dimension(120, 30));
        lblQuantity2.setHorizontalAlignment(SwingConstants.CENTER);
        lblQuantity2.setForeground(_Settings.labelColor);
        lblQuantity2.setBackground(_Settings.backgroundColor);
        lblQuantity2.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblQuantity2.setBorder(new LineBorder(Color.WHITE));
        panel2.add(lblQuantity2);

        JLabel lblUnit2 = new JLabel("Unit");
        lblUnit2.setPreferredSize(new Dimension(120, 30));
        lblUnit2.setHorizontalAlignment(SwingConstants.CENTER);
        lblUnit2.setForeground(_Settings.labelColor);
        lblUnit2.setBackground(_Settings.backgroundColor);
        lblUnit2.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblUnit2.setBorder(new LineBorder(Color.WHITE));
        panel2.add(lblUnit2);

        JLabel lblPrice2 = new JLabel("Unit Price");
        lblPrice2.setPreferredSize(new Dimension(120, 30));
        lblPrice2.setHorizontalAlignment(SwingConstants.CENTER);
        lblPrice2.setForeground(_Settings.labelColor);
        lblPrice2.setBackground(_Settings.backgroundColor);
        lblPrice2.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblPrice2.setBorder(new LineBorder(Color.WHITE));
        panel2.add(lblPrice2);

        JLabel lblMaterialStorage = new JLabel("Storage");
        lblMaterialStorage.setPreferredSize(new Dimension(350, 30));
        lblMaterialStorage.setHorizontalAlignment(SwingConstants.CENTER);
        lblMaterialStorage.setForeground(_Settings.labelColor);
        lblMaterialStorage.setBackground(_Settings.backgroundColor);
        lblMaterialStorage.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblMaterialStorage.setBorder(new LineBorder(Color.WHITE));
        panel2.add(lblMaterialStorage);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 0;
        gbc2.gridy = 0;
        tab2Pane.add(panel2, gbc2);

        spMaterial = new JScrollPane(tab2Pane);
        tabMaterial.add(spMaterial, "cell 1 1 10 1,grow");

        lblTotalCost = new JLabel("Total Material Cost");
        lblTotalCost.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTotalCost.setForeground(_Settings.labelColor);
        lblTotalCost.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblTotalCost.setBorder(null);
        lblTotalCost.setBackground(_Settings.backgroundColor);
        tabMaterial.add(lblTotalCost, "cell 2 3,grow");

        txtTotalCost = new JTextField();
        txtTotalCost.setHorizontalAlignment(SwingConstants.CENTER);
        txtTotalCost.setForeground(_Settings.textFieldColor);
        txtTotalCost.setFont(new Font("Century Gothic", Font.BOLD, 17));
        txtTotalCost.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        txtTotalCost.setBackground(_Settings.backgroundColor);
        tabMaterial.add(txtTotalCost, "cell 4 3,grow");

        lblUnitPrice = new JLabel("Unit Product Price");
        lblUnitPrice.setHorizontalAlignment(SwingConstants.RIGHT);
        lblUnitPrice.setForeground(_Settings.labelColor);
        lblUnitPrice.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblUnitPrice.setBorder(null);
        lblUnitPrice.setBackground(_Settings.backgroundColor);
        tabMaterial.add(lblUnitPrice, "cell 6 3,grow");

        txtUnitPrice = new JTextField();
        txtUnitPrice.setHorizontalAlignment(SwingConstants.CENTER);
        txtUnitPrice.setForeground(_Settings.textFieldColor);
        txtUnitPrice.setFont(new Font("Century Gothic", Font.BOLD, 17));
        txtUnitPrice.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        txtUnitPrice.setBackground(_Settings.backgroundColor);
        tabMaterial.add(txtUnitPrice, "cell 8 3,grow");

        lblUnitInTab2 = new JLabel("");
        lblUnitInTab2.setHorizontalAlignment(SwingConstants.LEFT);
        lblUnitInTab2.setForeground(_Settings.labelColor);
        lblUnitInTab2.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblUnitInTab2.setBorder(null);
        lblUnitInTab2.setBackground(_Settings.backgroundColor);
        tabMaterial.add(lblUnitInTab2, "cell 10 3,grow");

        tabs.add("Product", tabProduct);
        tabProduct.setLayout(new MigLayout("", "[10][grow][120][120][350][10]", "[40][10][30][10][60][650][10]"));

        lblPrompt2 = new JLabel("Select store in which finished products will be placed after production.");
        lblPrompt2.setForeground(_Settings.labelColor);
        lblPrompt2.setFont(new Font("Century Gothic", Font.BOLD | Font.ITALIC, 17));
        lblPrompt2.setBorder(null);
        lblPrompt2.setBackground(_Settings.backgroundColor);
        tabProduct.add(lblPrompt2, "cell 1 0 4 1,grow");

        lblProduct2 = new JLabel("Product");
        lblProduct2.setPreferredSize(new Dimension(250, 30));
        lblProduct2.setHorizontalAlignment(SwingConstants.CENTER);
        lblProduct2.setForeground(_Settings.labelColor);
        lblProduct2.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblProduct2.setBorder(new LineBorder(Color.WHITE));
        lblProduct2.setBackground(_Settings.backgroundColor);
        tabProduct.add(lblProduct2, "cell 1 2,grow");

        lblQuantity3 = new JLabel("Quantity");
        lblQuantity3.setPreferredSize(new Dimension(120, 30));
        lblQuantity3.setHorizontalAlignment(SwingConstants.CENTER);
        lblQuantity3.setForeground(_Settings.labelColor);
        lblQuantity3.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblQuantity3.setBorder(new LineBorder(Color.WHITE));
        lblQuantity3.setBackground(_Settings.backgroundColor);
        tabProduct.add(lblQuantity3, "cell 2 2,grow");

        lblUnit3 = new JLabel("Unit");
        lblUnit3.setPreferredSize(new Dimension(120, 30));
        lblUnit3.setHorizontalAlignment(SwingConstants.CENTER);
        lblUnit3.setForeground(_Settings.labelColor);
        lblUnit3.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblUnit3.setBorder(new LineBorder(Color.WHITE));
        lblUnit3.setBackground(_Settings.backgroundColor);
        tabProduct.add(lblUnit3, "cell 3 2,grow");

        lblProductStorage = new JLabel("Storage");
        lblProductStorage.setPreferredSize(new Dimension(350, 30));
        lblProductStorage.setHorizontalAlignment(SwingConstants.CENTER);
        lblProductStorage.setForeground(_Settings.labelColor);
        lblProductStorage.setFont(new Font("Arial Black", Font.PLAIN, 17));
        lblProductStorage.setBorder(new LineBorder(Color.WHITE));
        lblProductStorage.setBackground(_Settings.backgroundColor);
        tabProduct.add(lblProductStorage, "cell 4 2,grow");

        lblProduct2Text = new JLabel();
        lblProduct2Text.setPreferredSize(new Dimension(60, 30));
        lblProduct2Text.setHorizontalAlignment(SwingConstants.LEFT);
        lblProduct2Text.setForeground(_Settings.textFieldColor);
        lblProduct2Text.setFont(new Font("Century Gothic", Font.BOLD, 17));
        lblProduct2Text.setBorder(null);
        lblProduct2Text.setBackground(_Settings.backgroundColor);
        tabProduct.add(lblProduct2Text, "cell 1 4,grow");

        lblQuantityText = new JLabel();
        lblQuantityText.setPreferredSize(new Dimension(60, 30));
        lblQuantityText.setHorizontalAlignment(SwingConstants.CENTER);
        lblQuantityText.setForeground(_Settings.textFieldColor);
        lblQuantityText.setFont(new Font("Century Gothic", Font.BOLD, 17));
        lblQuantityText.setBorder(null);
        lblQuantityText.setBackground(_Settings.backgroundColor);
        tabProduct.add(lblQuantityText, "cell 2 4,grow");

        lblUnitText = new JLabel();
        lblUnitText.setPreferredSize(new Dimension(60, 30));
        lblUnitText.setHorizontalAlignment(SwingConstants.CENTER);
        lblUnitText.setForeground(_Settings.textFieldColor);
        lblUnitText.setFont(new Font("Century Gothic", Font.BOLD, 17));
        lblUnitText.setBorder(null);
        lblUnitText.setBackground(_Settings.backgroundColor);
        tabProduct.add(lblUnitText, "cell 3 4,grow");

        Stores dbStore = (Stores) DatabaseFacade.getDatabase("Stores");
        List<Store> lstStore = dbStore.getList();
        SortedComboBoxModel<String> scbmStore = new SortedComboBoxModel<String>();
        MyComboBoxRenderer mcbrStore = new MyComboBoxRenderer();
        List<String> ttStore = new ArrayList<String>();
        for (Store store : lstStore) {
            String storeString = ValueFormatter.formatStore(store);
            scbmStore.addElement(storeString);
            ttStore.add(storeString);
        }
        mcbrStore.setTooltips(ttStore);

        cbProductStorage = new JComboBox<String>(scbmStore);
        cbProductStorage.setRenderer(mcbrStore);
        cbProductStorage.setVisible(false);
        cbProductStorage.setMaximumSize(new Dimension(350, 60));
        cbProductStorage.setSelectedIndex(-1);
        cbProductStorage.setForeground(Color.DARK_GRAY);
        cbProductStorage.setFont(new Font("Century Gothic", Font.BOLD, 17));
        cbProductStorage.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                // TODO Auto-generated method stub
                if ((e.getStateChange() == ItemEvent.SELECTED)) {
                    isProductStoreSelected = true;
                    validateButton();
                }
            }
        });
        tabProduct.add(cbProductStorage, "cell 4 4,grow");

        clTab = new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent arg0) {
                // TODO Auto-generated method stub

                // Material tab
                if (tabs.getSelectedIndex() == 1) {
                    int size = tab2Pane.getComponentCount();
                    for (int i = size - 1; i > 0; i--) {
                        tab2Pane.remove(i);
                        quantitys2.remove(i - 1);
                        prices2.remove(i - 1);
                        cbMaterialStorages.remove(i - 1);
                    }

                    for (int i = 0; i < cbMaterials.size(); i++) {
                        GridBagConstraints gbc = new GridBagConstraints();
                        gbc.gridx = 0;
                        gbc.gridy = i + 1;
                        JPanel panel = addMaterialInTab2(i);
                        if (panel != null)
                            tab2Pane.add(panel, gbc);
                    }

                    updateTotalCostAndUnitPrice();
                    revalidate();
                    repaint();
                } else if (tabs.getSelectedIndex() == 2) {
                    addProductInTab3();
                }
            }
        };
        tabs.addChangeListener(clTab);

        if (production != null) {
            this.production = production;
            populateFrame();
        }

        setContentPane(tabs);
        pack();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public cores.Production getProduction() {
        return production;
    }

    public void setProduction(cores.Production production) {
        this.production = production;
    }

    public boolean completeProduction() {
        boolean unavailableStock = false;
        for (int i = 0; i < cbMaterialStorages.size(); i++) {
            String storeString = (String) cbMaterialStorages.get(i).getSelectedItem();
            Item material = ValueFormatter.parseItemFromComboBox((String) cbMaterials.get(i).getSelectedItem());
            if (storeString.equals(STOCK_UNAVAILABLE)) {
                new MyOptionPane("Stock is unavailable for: " + material.getName(), MyOptionPane.INFORMATION_MESSAGE,
                        "Info");
                unavailableStock = true;
            }
        }
        if (unavailableStock)
            return false;

        boolean insufficientStock = false;
        for (int i = 0; i < cbMaterialStorages.size(); i++) {
            String storeString = (String) cbMaterialStorages.get(i).getSelectedItem();
            Store store = ValueFormatter.parseStore(storeString);
            Item material = ValueFormatter.parseItemFromComboBox((String) cbMaterials.get(i).getSelectedItem());
            if (material.getType().equals(ItemType.SERVICE))
                continue;
            double quantity = ValueFormatter.parseQuantity(quantitys.get(i).getText());
            Stock stock = ItemFacade.findStockById(store.getItems(), material.getId());
            if (quantity > stock.getQuantity()) {
                new MyOptionPane(
                        "Stock is insufficient for: " + material.getName() + "\nAt store: " + store.getName()
                                + "\nRequired quantity: " + ValueFormatter.formatQuantity(quantity)
                                + "\nAvailable stock: " + ValueFormatter.formatQuantity(stock.getQuantity()),
                        MyOptionPane.INFORMATION_MESSAGE, "Info");
                insufficientStock = true;
            }
        }
        return !insufficientStock;
    }

    public List<Transaction> getCosts() {
        return costs;
    }

    private void populateFrame() {
        Stock product = production.getProduct();
        txtId.setText(production.getAlternativeId());

        cbProduct.removeItemListener(ilProduct);
        cbProduct.removeAllItems();
        cbProduct.addItem(ValueFormatter.formatItemForComboBox(product.getItem()));
        cbProduct.setSelectedIndex(0);

        cbProductStorage.removeAllItems();
        cbProductStorage.addItem(ValueFormatter.formatStore(production.getProductStore()));
        cbProductStorage.setSelectedIndex(0);

        dateChooser.setDate(production.getStartDate());

        txtProductQuantity.setText(ValueFormatter.formatQuantity(product.getQuantity()));

        cbProductUnit.removeAllItems();
        cbProductUnit.addItem(production.getProductUnit());
        cbProductUnit.setSelectedIndex(0);

        for (int i = 0; i < production.getMaterials().size(); i++) {
            btnIncrement.doClick();
        }
        for (int i = 0; i < production.getMaterials().size(); i++) {
            cbMaterials.get(i).removeAllItems();
            cbMaterials.get(i)
                    .addItem(ValueFormatter.formatItemForComboBox(production.getMaterials().get(i).getItem()));
            cbMaterials.get(i).setSelectedIndex(0);
            quantitys.get(i).setText(ValueFormatter.formatQuantity(production.getMaterials().get(i).getQuantity()));
        }

        lblStatusValue.setText(production.getStatus().toString());

        isProductSelected = isProductStoreSelected = allMaterialsSelected = true;
        isDateEmpty = containsZero = false;

        for (Transaction transaction : production.getCosts())
            otherCost += transaction.getAmount() + transaction.getTax();
        txtCosts.setText(ValueFormatter.formatMoneyNicely(otherCost));

        // TAB 2

        tabs.setSelectedIndex(1);
        tabs.setSelectedIndex(0);

        cbProductStorage.removeAllItems();
        cbProductStorage.addItem(ValueFormatter.formatStore(production.getProductStore()));
        cbProductStorage.setSelectedIndex(0);
        cbProductStorage.setVisible(true);
        lblProduct2Text.setText(production.getProduct().getItem().getName());
        lblQuantityText.setText(txtProductQuantity.getText());
        lblUnitText.setText(production.getProductUnit());

        txtStatus.setText("");

        btnPrint.setEnabled(true);

        setFormEnabled(false);
    }

    private void setFormEnabled(boolean isEnabled) {
        tabs.removeChangeListener(clTab);

        for (JTextField txt : quantitys)
            txt.setEnabled(isEnabled);
        for (JComboBox<String> cb : cbMaterials)
            cb.setEnabled(isEnabled);

        cbProduct.setEnabled(isEnabled);
        cbProductStorage.setEnabled(isEnabled);
        cbProductUnit.setEnabled(isEnabled);
        dateChooser.setEnabled(isEnabled);
        txtProductQuantity.setEnabled(isEnabled);
        btnSave.setEnabled(isEnabled);
        btnOther.setEnabled(isEnabled);
        btnUpdate.setEnabled(isEnabled);
        btnIncrement.setEnabled(isEnabled);
        btnDecrement.setEnabled(isEnabled);

        if (lblStatusValue.getText().equals(TransactionStatus.COMPLETE.toString())) {
            btnComplete.setEnabled(false);
            for (JComboBox<String> cb : cbMaterialStorages)
                cb.setEnabled(isEnabled);
        } else
            btnComplete.setEnabled(true);
    }

    private boolean addProduction() {
        Productions dbProductions = (Productions) DatabaseFacade.getDatabase("Productions");
        return dbProductions.add(production.getId(), production.getAlternativeId(), production.getStartDate(),
                production.getEndDate(), production.getProduct(), production.getProductStore(),
                production.getProductUnit(), production.getMaterials(), production.getMaterialStores(),
                production.getCosts(), production.getStatus());
    }

    private boolean checkForZeroAmounts() {
        if (ValueFormatter.parseQuantity(txtProductQuantity.getText()) == 0)
            return true;
        for (JTextField txtQuantity : quantitys)
            if (!txtQuantity.getText().isEmpty())
                if (ValueFormatter.parseQuantity(txtQuantity.getText()) == 0)
                    return true;
        return false;
    }

    private boolean checkForAllMaterialsSelection() {
        for (JComboBox<String> cb : cbMaterials)
            if (cb.getSelectedIndex() == -1)
                return false;
        return true;
    }

    private void validateButton() {
        containsZero = checkForZeroAmounts();
        allMaterialsSelected = checkForAllMaterialsSelection();
        if (!isProductSelected)
            txtStatus.setText("Select a product!");
        else if (isDateEmpty)
            txtStatus.setText("Select a date!");
        else if (!allMaterialsSelected)
            txtStatus.setText("One or more materials are not selected!");
        else if (containsZero)
            txtStatus.setText("Quantity value(s) cannot be zero!");
        else if (!isProductStoreSelected)
            txtStatus.setText("Select a store (in Product tab)!");
        else
            txtStatus.setText("");
        btnSave.setEnabled(
                isProductSelected && isProductStoreSelected && allMaterialsSelected && !isDateEmpty && !containsZero);
    }

    private double calculateMaterialCost() {
        double materialCost = 0.0;
        for (int i = 0; i < quantitys2.size(); i++) {
            if (prices2.get(i).getText().equals("-"))
                continue;
            else {
                double quantity = ValueFormatter.parseQuantity(quantitys2.get(i).getText());
                double price = ValueFormatter.parseMoney(prices2.get(i).getText());
                materialCost += (quantity * price);
            }
        }
        return materialCost;
    }

    private double calculateUnitPrice(double totalCost) {
        double quantity = ValueFormatter.parseQuantity(txtProductQuantity.getText());
        return totalCost / quantity;
    }

    private void updateTotalCostAndUnitPrice() {
        double materialCost = calculateMaterialCost();
        txtTotalCost.setText(ValueFormatter.formatMoneyNicely(materialCost));
        txtUnitPrice.setText(ValueFormatter.formatMoneyNicely(calculateUnitPrice(materialCost + otherCost)));
        lblUnitInTab2.setText("/" + cbProductUnit.getSelectedItem());
    }

    private void addProductInTab3() {
        if (selectedProduct != null) {
            lblProduct2Text.setText("");
            lblUnitText.setText("");
            lblQuantityText.setText("");

            if (!txtProductQuantity.getText().isEmpty())
                lblQuantityText.setText(txtProductQuantity.getText());
            lblProduct2Text.setText("<html>" + selectedProduct.getName() + "</html>");
            lblUnitText.setText((String) cbProductUnit.getSelectedItem());
            cbProductStorage.setVisible(true);
        }
    }

    private JPanel addMaterialInTab2(int index) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
        panel.setBackground(_Settings.backgroundColor);
        panel.setBorder(null);

        JLabel lblNo = new JLabel(Integer.toString(index + 1));
        lblNo.setPreferredSize(new Dimension(60, 60));
        lblNo.setHorizontalAlignment(SwingConstants.CENTER);
        lblNo.setForeground(_Settings.textFieldColor);
        lblNo.setBackground(_Settings.backgroundColor);
        lblNo.setFont(new Font("Century Gothic", Font.BOLD, 17));
        panel.add(lblNo);

        if (cbMaterials.get(index).getSelectedIndex() == -1)
            return null;
        Item selectedItem = ValueFormatter.parseItemFromComboBox((String) cbMaterials.get(index).getSelectedItem());

        JLabel lblMaterial = new JLabel("<html>" + selectedItem.getName() + "</html>");
        lblMaterial.setPreferredSize(new Dimension(250, 60));
        lblMaterial.setHorizontalAlignment(SwingConstants.LEFT);
        lblMaterial.setForeground(_Settings.textFieldColor);
        lblMaterial.setBackground(_Settings.backgroundColor);
        lblMaterial.setFont(new Font("Century Gothic", Font.BOLD, 17));
        panel.add(lblMaterial);

        JLabel lblQuantity = new JLabel(quantitys.get(index).getText());
        lblQuantity.setPreferredSize(new Dimension(120, 60));
        lblQuantity.setHorizontalAlignment(SwingConstants.CENTER);
        lblQuantity.setForeground(_Settings.textFieldColor);
        lblQuantity.setBackground(_Settings.backgroundColor);
        lblQuantity.setFont(new Font("Century Gothic", Font.BOLD, 17));
        panel.add(lblQuantity);
        quantitys2.add(lblQuantity);

        JLabel lblUnit = new JLabel(selectedItem.getUnit());
        lblUnit.setPreferredSize(new Dimension(120, 60));
        lblUnit.setHorizontalAlignment(SwingConstants.CENTER);
        lblUnit.setForeground(_Settings.textFieldColor);
        lblUnit.setBackground(_Settings.backgroundColor);
        lblUnit.setFont(new Font("Century Gothic", Font.BOLD, 17));
        panel.add(lblUnit);

        List<Store> stores = new ArrayList<Store>();
        List<Stock> stocks = new ArrayList<Stock>();

        Stores dbStore = (Stores) DatabaseFacade.getDatabase("Stores");
        List<Store> lstStore = dbStore.getList();
        for (Store store : lstStore) {
            List<Stock> lstStock = store.getItems();
            if (lstStock != null)
                for (Stock stock : lstStock) {
                    if (stock.getItem().equals(selectedItem)) {
                        stores.add(store);
                        stocks.add(stock);
                        break;
                    }
                }
        }

        JLabel lblPrice = new JLabel("-");
        if (!stocks.isEmpty())
            lblPrice.setText(ValueFormatter.formatMoney(stocks.get(0).getPrice()));
        else if (lblStatusValue.getText().equals(TransactionStatus.COMPLETE.toString()))
            lblPrice.setText(ValueFormatter.formatMoney(production.getMaterials().get(index).getPrice()));
        lblPrice.setPreferredSize(new Dimension(120, 60));
        lblPrice.setHorizontalAlignment(SwingConstants.CENTER);
        lblPrice.setForeground(_Settings.textFieldColor);
        lblPrice.setBackground(_Settings.backgroundColor);
        lblPrice.setFont(new Font("Century Gothic", Font.BOLD, 17));
        panel.add(lblPrice);
        prices2.add(lblPrice);

        JComboBox<String> cb = new JComboBox<String>();
        cb.setPreferredSize(new Dimension(330, 60));
        cb.setForeground(Color.DARK_GRAY);
        cb.setFont(new Font("Century Gothic", Font.BOLD, 17));

        SortedComboBoxModel<String> scbmStore = new SortedComboBoxModel<String>();
        MyComboBoxRenderer mcbrStore = new MyComboBoxRenderer();
        List<String> ttStore = new ArrayList<String>();

        if (lblStatusValue.getText().equals(TransactionStatus.COMPLETE.toString())) {
            String storeString = ValueFormatter.formatStore(production.getMaterialStores().get(index));
            scbmStore.addElement(storeString);
            ttStore.add(storeString);
        } else if (stores.isEmpty()) {
            String storeString = STOCK_UNAVAILABLE;
            scbmStore.addElement(storeString);
            ttStore.add(storeString);
        } else
            for (Store store : stores) {
                String storeString = ValueFormatter.formatStore(store);
                scbmStore.addElement(storeString);
                ttStore.add(storeString);
            }

        mcbrStore.setTooltips(ttStore);
        cb.setModel(scbmStore);
        cb.setRenderer(mcbrStore);
        cb.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                // TODO Auto-generated method stub
                if (e.getStateChange() == ItemEvent.SELECTED && !stocks.isEmpty()) {
                    lblPrice.setText(ValueFormatter.formatMoneyNicely(stocks.get(cb.getSelectedIndex()).getPrice()));
                    updateTotalCostAndUnitPrice();
                }
            }
        });
        cb.setSelectedIndex(0);
        cbMaterialStorages.add(cb);
        panel.add(cb);

        JLabel lblEmpty = new JLabel();
        lblEmpty.setPreferredSize(new Dimension(20, 60));
        lblEmpty.setBackground(_Settings.backgroundColor);
        panel.add(lblEmpty);

        return panel;
    }

    private JPanel addMaterialInTab1() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
        panel.setBackground(_Settings.backgroundColor);
        panel.setBorder(null);

        JLabel lbl = new JLabel(Integer.toString(++count));
        lbl.setPreferredSize(new Dimension(100, 40));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setForeground(_Settings.labelColor);
        lbl.setBackground(_Settings.backgroundColor);
        lbl.setFont(new Font("Century Gothic", Font.BOLD, 17));
        panel.add(lbl);

        Items dbItems = (Items) DatabaseFacade.getDatabase("Items");
        List<Material> materials = dbItems.getMaterialList();
        List<Service> services = dbItems.getServiceList();
        SortedComboBoxModel<String> scbmItem = new SortedComboBoxModel<String>(true);
        MyComboBoxRenderer mcbrItem = new MyComboBoxRenderer();
        List<String> ttItem = new ArrayList<String>();
        for (Material material : materials) {
            String materialString = ValueFormatter.formatItemForComboBox(material);
            scbmItem.addElement(materialString);
            ttItem.add(materialString);
        }
        for (Service service : services) {
            String serviceString = ValueFormatter.formatItemForComboBox(service);
            scbmItem.addElement(serviceString);
            ttItem.add(serviceString);
        }
        Collections.sort(ttItem, new Comparator<String>() {

            @Override
            public int compare(String arg0, String arg1) {
                // TODO Auto-generated method stub
                String string0 = arg0.substring(arg0.indexOf(" ") + 1);
                String string1 = arg1.substring(arg1.indexOf(" ") + 1);
                return string0.compareTo(string1);
            }
        });
        mcbrItem.setTooltips(ttItem);

        JLabel lbl2 = new JLabel();
        JComboBox<String> cb = new JComboBox<String>(scbmItem);
        cb.setRenderer(mcbrItem);
        cb.setPreferredSize(new Dimension(680, 40));
        cb.setForeground(Color.DARK_GRAY);
        cb.setFont(new Font("Century Gothic", Font.BOLD, 17));
        cb.setSelectedIndex(-1);
        cb.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                // TODO Auto-generated method stub
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Item item = ValueFormatter.parseItemFromComboBox((String) cb.getSelectedItem());
                    lbl2.setText(item.getUnit());
                    validateButton();
                }
            }
        });
        cbMaterials.add(cb);
        panel.add(cb);

        JTextField txt = new JTextField("1");
        txt.setBorder(null);
        txt.setPreferredSize(new Dimension(120, 40));
        txt.setHorizontalAlignment(SwingConstants.CENTER);
        txt.setForeground(_Settings.textFieldColor);
        txt.setBackground(_Settings.backgroundColor);
        txt.setFont(new Font("Century Gothic", Font.BOLD, 17));
        txt.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent arg0) {
                // TODO Auto-generated method stub
                try {
                    ValueFormatter.parseQuantity(txt.getText());
                } catch (NumberFormatException e) {
                    new MyOptionPane("Quantity must be a number!", MyOptionPane.INFORMATION_MESSAGE);
                    txt.setText("0");
                }
                validateButton();
            }

            @Override
            public void focusGained(FocusEvent arg0) {
                // TODO Auto-generated method stub
                txt.selectAll();
            }
        });
        quantitys.add(txt);
        panel.add(txt);

        lbl2.setPreferredSize(new Dimension(120, 40));
        lbl2.setHorizontalAlignment(SwingConstants.CENTER);
        lbl2.setForeground(_Settings.labelColor);
        lbl2.setBackground(_Settings.backgroundColor);
        lbl2.setFont(new Font("Century Gothic", Font.BOLD, 17));
        panel.add(lbl2);

        return panel;
    }

    private void removeMaterial() {
        cbMaterials.remove(count);
        quantitys.remove(count);
    }

    public JDialog getThis() {
        return this;
    }

}

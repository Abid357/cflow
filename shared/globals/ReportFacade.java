package globals;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import cores.Inventory;
import cores.Material;
import cores.Organization;
import cores.Person;
import cores.Product;
import cores.PurchaseTransaction;
import cores.SaleTransaction;
import cores.Service;
import cores.Stock;
import cores.Store;
import cores.StoreInventory;
import cores.Transaction;
import cores.UserObject;
import cores.UserRecord;
import databases.Inventories;
import databases.Items;
import databases.PurchaseTransactions;
import databases.SaleTransactions;
import databases.Stores;
import databases.Transactions;
import databases.UserObjects;
import databases.UserRecords;
import frames.Main;
import frames.MyOptionPane;
import frames.ReportOptions;
import helpers.ItemType;
import helpers.TransactionStatus;
import helpers.TransactionWrapper;

public class ReportFacade {

    public static void generateStockReport1(String title) {
        SaleTransactions dbSales = (SaleTransactions) DatabaseFacade.getDatabase("SaleTransactions");
        List<SaleTransaction> lstSales = dbSales.getList();
        List<Hashtable<String, Double>> hashes = new ArrayList<Hashtable<String, Double>>();
        for (int i = 0; i < 12; i++)
            hashes.add(new Hashtable<String, Double>());
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < lstSales.size(); i++) {
            if (lstSales.get(i).getItemStatus().equals(TransactionStatus.DELIVERED)) {
                cal.setTime(lstSales.get(i).getDate());
                int month = cal.get(Calendar.MONTH);
                for (Stock stock : lstSales.get(i).getItems()) {
                    String key = ValueFormatter.formatItem(stock.getItem());
                    for (int j = 0; j < 12; j++) { // check other hash tables or months for same customer
                        if (!hashes.get(j).containsKey(key))
                            hashes.get(j).put(key, 0.0);
                    }
                    double oldValue = hashes.get(month).get(key);
                    hashes.get(month).put(key, oldValue + stock.getQuantity());
                }
            }
        }

        SortedSet<String> list = new TreeSet<String>();
        Set<String> set = hashes.get(0).keySet();
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext())
            list.add(iterator.next());

        double[][] totals = new double[list.size()][12];

        iterator = list.iterator();
        int item = 0;
        while (iterator.hasNext()) {
            String key = iterator.next();
            for (int month = 0; month < hashes.size(); month++)
                totals[item][month] = hashes.get(month).get(key);
            item++;
        }
        PRINTER.printStockReport1(title, list, totals);
    }

    public static void generateStockReport2(String title) {
        Stores dbStores = (Stores) DatabaseFacade.getDatabase("Stores");
        List<Store> lstStores = dbStores.getList();
        Collections.sort(lstStores, new Comparator<Store>() {
            public int compare(Store s1, Store s2) {
                return ValueFormatter.formatStore(s2).compareTo(ValueFormatter.formatStore(s1));
            }
        });
        PRINTER.printStockReport2(title, lstStores);
    }

    public static void generateStockReport3(String title, Store store) {
        PRINTER.printStockReport3(title, store);
    }

    public static void generatePurchaseReport1(String title) {
        PurchaseTransactions dbPurchases = (PurchaseTransactions) DatabaseFacade.getDatabase("PurchaseTransactions");
        List<PurchaseTransaction> lstPurchases = dbPurchases.getList();
        List<Hashtable<String, Double>> hashes = new ArrayList<Hashtable<String, Double>>();
        for (int i = 0; i < 12; i++)
            hashes.add(new Hashtable<String, Double>());
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < lstPurchases.size(); i++) {
            cal.setTime(lstPurchases.get(i).getDate());
            int month = cal.get(Calendar.MONTH);
            String key = ValueFormatter.formatUserObject(lstPurchases.get(i).getSupplier());
            for (int j = 0; j < 12; j++) { // check other hash tables or months for same supplier
                if (!hashes.get(j).containsKey(key))
                    hashes.get(j).put(key, 0.0);
            }
            double oldValue = hashes.get(month).get(key);
            hashes.get(month).put(key, oldValue + lstPurchases.get(i).getTotal());
        }

        SortedSet<String> list = new TreeSet<String>();
        Set<String> set = hashes.get(0).keySet();
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext())
            list.add(iterator.next());

        double[][] totals = new double[list.size()][12];

        iterator = list.iterator();
        int customer = 0;
        while (iterator.hasNext()) {
            String key = iterator.next();
            for (int month = 0; month < hashes.size(); month++)
                totals[customer][month] = hashes.get(month).get(key);
            customer++;
        }
        PRINTER.printPurchaseReport1(title, list, totals);
    }

    public static void generatePurchaseReport2(String title, Date fromDate, Date toDate, UserObject customer,
                                               List<JCheckBox> checkboxes) {
        PurchaseTransactions dbPurchases = (PurchaseTransactions) DatabaseFacade.getDatabase("PurchaseTransactions");
        List<PurchaseTransaction> lstPurchases = dbPurchases.getList();
        List<PurchaseTransaction> datedCustomerPurchases = new ArrayList<PurchaseTransaction>();
        for (PurchaseTransaction purchase : lstPurchases) {
            Date date = purchase.getDate();
            if (date.equals(fromDate) || (date.after(fromDate) && date.before(toDate)) || date.equals(toDate))
                if (purchase.getSupplier().equals(customer))
                    datedCustomerPurchases.add(purchase);
        }
        Collections.sort(datedCustomerPurchases, new Comparator<PurchaseTransaction>() {
            public int compare(PurchaseTransaction t1, PurchaseTransaction t2) {
                return t2.getDate().compareTo(t1.getDate());
            }
        });
        PRINTER.printPurchaseReport2(title, fromDate, toDate, datedCustomerPurchases, checkboxes.get(0).isSelected(),
                checkboxes.get(1).isSelected(), checkboxes.get(2).isSelected(), checkboxes.get(3).isSelected(),
                checkboxes.get(4).isSelected());
    }

    public static void generateTransactionReport1(String title, Date fromDate, Date toDate, String userCategory, UserObject user, Inventory inventory, String transactionCategory, String[] columnHeaders, List<String> columns) {
        List<TransactionWrapper> transactions = new ArrayList<TransactionWrapper>();
        Transactions dbTransaction = (Transactions) DatabaseFacade.getDatabase("Transactions");
        List<Transaction> lstTransaction = dbTransaction.getList();
        SaleTransactions dbSales = (SaleTransactions) DatabaseFacade.getDatabase("SaleTransactions");
        List<SaleTransaction> lstSales = dbSales.getList();
        PurchaseTransactions dbPurchases = (PurchaseTransactions) DatabaseFacade.getDatabase("PurchaseTransactions");
        List<PurchaseTransaction> lstPurchases = dbPurchases.getList();

        for (Transaction t : lstTransaction)
            transactions.add(new TransactionWrapper(t));
        for (SaleTransaction st : lstSales)
            if (st.getItemStatus().equals(TransactionStatus.DELIVERED))
                transactions.add(new TransactionWrapper(st));
        for (PurchaseTransaction pt : lstPurchases)
            if (pt.getItemStatus().equals(TransactionStatus.DELIVERED))
                transactions.add(new TransactionWrapper(pt));

        if (user != null) {
            List<TransactionWrapper> userTransactions = new ArrayList<TransactionWrapper>();
            for (TransactionWrapper transaction : transactions)
                if (transaction.getUserObject().equals(user))
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
        if (inventory != null) {
            List<TransactionWrapper> inventoryTransactions = new ArrayList<TransactionWrapper>();
            for (TransactionWrapper transaction : transactions)
                if (transaction.getInventory() != null && transaction.getInventory().equals(inventory))
                    inventoryTransactions.add(transaction);
            transactions = inventoryTransactions;
        }
        if (userCategory != null) {
            List<TransactionWrapper> categoryTransactions = new ArrayList<TransactionWrapper>();
            for (TransactionWrapper transaction : transactions) {
                List<String> categories = transaction.getUserObject().getCategories();
                for (String category : categories)
                    if (category.equals(userCategory))
                        categoryTransactions.add(transaction);
            }
            transactions = categoryTransactions;
        }
        if (transactionCategory != null) {
            List<TransactionWrapper> categoryTransactions = new ArrayList<TransactionWrapper>();
            for (TransactionWrapper transaction : transactions)
                if (transaction.getCategory().equals(transactionCategory))
                    categoryTransactions.add(transaction);
            transactions = categoryTransactions;
        }

        DefaultTableModel dtm = new DefaultTableModel() {

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
        JTable table = new JTable(dtm);

        UserRecords dbUserRecords = (UserRecords) DatabaseFacade.getDatabase("UserRecords");
        for (int i = 0; i < transactions.size(); i++) {
            TransactionWrapper transaction = transactions.get(i);
            String credit = "", debit = "";
            String string = ValueFormatter
                    .formatMoneyNicely(RecordFacade.calculateAmountWithTax(transaction.getAmount(), transaction.getTax()));
            if (transaction.isCredit())
                credit = "+" + string;
            else
                debit = "-" + string;
            UserRecord userRecord = dbUserRecords
                    .get(dbUserRecords.find(transaction.getId(), transaction.getUserObject()));
            String balance = ValueFormatter.formatMoneyNicely(userRecord.getBalance());
            dtm.addRow(new Object[]{transaction.getId(), ValueFormatter.formatUserObject(transaction.getUserObject()),
                    credit, debit, balance, ValueFormatter.formatDate(transaction.getDate()),
                    ValueFormatter.formatInventory(transaction.getInventory()), transaction.getRemark(),
                    transaction.getCategory()});
        }

        PRINTER.printTransactionReport1(table, title, fromDate, toDate, columns, user != null);
    }

    public static void generateSalesReport1(String title, int year) {
        SaleTransactions dbSales = (SaleTransactions) DatabaseFacade.getDatabase("SaleTransactions");
        List<SaleTransaction> lstSales = dbSales.getList();
        if (lstSales.isEmpty())
        	new MyOptionPane("There is no data to generate the report.", MyOptionPane.ERROR_DIALOG_BOX);
        List<Hashtable<String, Double>> hashes = new ArrayList<Hashtable<String, Double>>();
        for (int i = 0; i < 12; i++)
            hashes.add(new Hashtable<String, Double>());
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < lstSales.size(); i++) {
            cal.setTime(lstSales.get(i).getDate());
            int month = cal.get(Calendar.MONTH);
            if (year != cal.get(Calendar.YEAR))
            	continue;
            String key = ValueFormatter.formatUserObject(lstSales.get(i).getCustomer());
            for (int j = 0; j < 12; j++) { // check other hash tables or months for same customer
                if (!hashes.get(j).containsKey(key))
                    hashes.get(j).put(key, 0.0);
            }
            double oldValue = hashes.get(month).get(key);
            hashes.get(month).put(key, oldValue + lstSales.get(i).getTotal());
        }

        SortedSet<String> list = new TreeSet<String>();
        Set<String> set = hashes.get(0).keySet();
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext())
            list.add(iterator.next());

        double[][] totals = new double[list.size()][12];

        iterator = list.iterator();
        int customer = 0;
        while (iterator.hasNext()) {
            String key = iterator.next();
            for (int month = 0; month < hashes.size(); month++)
                totals[customer][month] = hashes.get(month).get(key);
            customer++;
        }
        PRINTER.printSalesReport1(title, list, totals);
    }

    public static void generateSalesReport2(String title, Date fromDate, Date toDate) {
        SaleTransactions dbSales = (SaleTransactions) DatabaseFacade.getDatabase("SaleTransactions");
        List<SaleTransaction> lstSales = dbSales.getList();
        List<SaleTransaction> datedSales = new ArrayList<SaleTransaction>();
        for (SaleTransaction sale : lstSales) {
            Date date = sale.getDate();
            if (date.equals(fromDate) || (date.after(fromDate) && date.before(toDate)) || date.equals(toDate))
                datedSales.add(sale);
        }
        Collections.sort(datedSales, new Comparator<SaleTransaction>() {
            public int compare(SaleTransaction t1, SaleTransaction t2) {
                return t2.getDate().compareTo(t1.getDate());
            }
        });
        PRINTER.printSalesReport2(title, fromDate, toDate, datedSales);
    }

    public static void generateSalesReport3(String title, Date fromDate, Date toDate, UserObject customer,
                                            List<JCheckBox> checkboxes) {
        SaleTransactions dbSales = (SaleTransactions) DatabaseFacade.getDatabase("SaleTransactions");
        List<SaleTransaction> lstSales = dbSales.getList();
        List<SaleTransaction> datedCustomerSales = new ArrayList<SaleTransaction>();
        for (SaleTransaction sale : lstSales) {
            Date date = sale.getDate();
            if (date.equals(fromDate) || (date.after(fromDate) && date.before(toDate)) || date.equals(toDate))
                if (sale.getCustomer().equals(customer))
                    datedCustomerSales.add(sale);
        }
        Collections.sort(datedCustomerSales, new Comparator<SaleTransaction>() {
            public int compare(SaleTransaction t1, SaleTransaction t2) {
                return t2.getDate().compareTo(t1.getDate());
            }
        });
        PRINTER.printSalesReport3(title, fromDate, toDate, datedCustomerSales, checkboxes.get(0).isSelected(),
                checkboxes.get(1).isSelected(), checkboxes.get(2).isSelected(), checkboxes.get(3).isSelected(),
                checkboxes.get(4).isSelected());
    }

	private static double calculateProfit(SaleTransaction sale) {
        double profit = 0.0;
        List<Stock> lstStock = sale.getItems();
        for (Stock stock : lstStock) {
            Double costPrice = null;
            if (stock.getOriginalPrice() != null)
                costPrice = new Double(stock.getOriginalPrice().doubleValue());
            else // look for the item in store it was purchased from
            {
                List<Stock> lstStoreItems = sale.getStore().getItems();
                for (Stock storeStock : lstStoreItems)
                    if (storeStock.getItem().equals(stock.getItem()))
                        costPrice = new Double(storeStock.getPrice());
                if (costPrice == null) { // if item not found in store anymore then get default price from item itself
                    costPrice = new Double(stock.getItem().getPrice());
                }
            }
            profit += ((stock.getPrice() - costPrice) * stock.getQuantity());
        }
        return profit;
    }

    public static void generateSalesReport4(String title, int year) {
        SaleTransactions dbSales = (SaleTransactions) DatabaseFacade.getDatabase("SaleTransactions");
        List<SaleTransaction> lstSales = dbSales.getList();
        if (lstSales.isEmpty())
        	new MyOptionPane("There is no data to generate the report.", MyOptionPane.ERROR_DIALOG_BOX);
        List<Hashtable<String, Double>> hashes = new ArrayList<Hashtable<String, Double>>();
        for (int i = 0; i < 12; i++)
            hashes.add(new Hashtable<String, Double>());
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < lstSales.size(); i++) {
            cal.setTime(lstSales.get(i).getDate());
            int month = cal.get(Calendar.MONTH);
            if (year != cal.get(Calendar.YEAR))
            	continue;
            String key = ValueFormatter.formatUserObject(lstSales.get(i).getCustomer());
            for (int j = 0; j < 12; j++) { // check other hash tables or months for same customer
                if (!hashes.get(j).containsKey(key))
                    hashes.get(j).put(key, 0.0);
            }
            double oldValue = hashes.get(month).get(key);
            hashes.get(month).put(key, oldValue + calculateProfit(lstSales.get(i)));
        }

        SortedSet<String> list = new TreeSet<String>();
        Set<String> set = hashes.get(0).keySet();
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext())
            list.add(iterator.next());

        double[][] totals = new double[list.size()][12];

        iterator = list.iterator();
        int customer = 0;
        while (iterator.hasNext()) {
            String key = iterator.next();
            for (int month = 0; month < hashes.size(); month++)
                totals[customer][month] = hashes.get(month).get(key);
            customer++;
        }
        PRINTER.printSalesReport1(title, list, totals);
    }

    public static void generateSalesReport5(String title, List<JCheckBox> checkboxes) {
        SaleTransactions dbSales = (SaleTransactions) DatabaseFacade.getDatabase("SaleTransactions");
        List<SaleTransaction> lstSales = dbSales.getList();
        SortedSet<UserObject> set = new TreeSet<UserObject>(new Comparator<UserObject>() {

            @Override
            public int compare(UserObject o1, UserObject o2) {
                // TODO Auto-generated method stub
                return ValueFormatter.formatUserObject(o1).compareTo(ValueFormatter.formatUserObject(o2));
            }
        });
        for (SaleTransaction sale : lstSales)
            set.add(sale.getCustomer());

        Transactions dbTransaction = (Transactions) DatabaseFacade.getDatabase("Transactions");

        List<UserObject> list = new ArrayList<UserObject>(set);
        List<Date> startDates = new ArrayList<Date>();
        List<Date> lastDates = new ArrayList<Date>();
        List<Double> totalSales = new ArrayList<Double>();
        List<Double> paid = new ArrayList<Double>();
        List<Double> outstanding = new ArrayList<Double>();
        for (UserObject customer : list) {
            List<Date> allDates = new ArrayList<Date>();
            double salesValue = 0.0;
            double paidValue = 0.0;
            double outstandingValue = 0.0;
            for (SaleTransaction sale : lstSales) {
                if (sale.getCustomer().equals(customer)) {
                    allDates.add(sale.getDate());
                    salesValue += sale.getTotal();

                    double paidValueOfThisSale = 0.0;
                    List<Transaction> payments = sale.getPayments();
                    for (Transaction transaction : payments)
                        paidValueOfThisSale += dbTransaction.get(dbTransaction.find(transaction.getId())).getAmount();
                    paidValue += paidValueOfThisSale;

                    if (!sale.getPaymentStatus().equals(TransactionStatus.PAID))
                        outstandingValue += (sale.getTotal() - paidValueOfThisSale);
                }
            }
            startDates.add(Collections.min(allDates, new Comparator<Date>() {

                @Override
                public int compare(Date o1, Date o2) {
                    // TODO Auto-generated method stub
                    return o1.compareTo(o2);
                }
            }));
            lastDates.add(Collections.max(allDates, new Comparator<Date>() {

                @Override
                public int compare(Date o1, Date o2) {
                    // TODO Auto-generated method stub
                    return o1.compareTo(o2);
                }
            }));
            totalSales.add(new Double(salesValue));
            paid.add(new Double(paidValue));
            outstanding.add(new Double(outstandingValue));
        }

        if (!checkboxes.get(0).isSelected())
            startDates = null;
        if (!checkboxes.get(1).isSelected())
            lastDates = null;
        if (!checkboxes.get(2).isSelected())
            totalSales = null;
        if (!checkboxes.get(3).isSelected())
            paid = null;
        if (!checkboxes.get(4).isSelected())
            outstanding = null;

        PRINTER.printSalesReport5(title, list, startDates, lastDates, totalSales, paid, outstanding);
    }

    public static void printTransactionSingle(Transaction transaction){
        PRINTER.printTransactionReport2("Transaction", transaction, true, true);
    }

    public static void printTransactionStatement(Main mainFrame, JTable tblTransaction, Date fromDate, Date toDate,
                                                 String[] columnHeaders, boolean printOutstandingBalance) {
        ReportOptions reportOptions = new ReportOptions(mainFrame, columnHeaders);
        reportOptions.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent arg0) {
                // TODO Auto-generated method stub

                if (reportOptions.isConfirmed()) {
                    List<String> list = reportOptions.getColumns();
                    if (PRINTER.printTransactionReport1(tblTransaction, reportOptions.getTitle(), fromDate, toDate,
                            list, printOutstandingBalance))
                        LOGGER.Activity.log("Transaction Statement", LOGGER.CREATE);
                }
            }
        });
    }

    public static boolean printItemList(String type) {
        Items db = (Items) DatabaseFacade.getDatabase("Items");
        List<Product> products = db.getProductList();
        List<Material> materials = db.getMaterialList();
        List<Service> services = db.getServiceList();

        if (type.equals(ItemType.PRODUCT.toString()))
            return PRINTER.printItemReport2("Product List", products, null, null, true);
        else if (type.equals(ItemType.MATERIAL.toString()))
            return PRINTER.printItemReport2("Material List", null, materials, null, true);
        else if (type.equals(ItemType.SERVICE.toString()))
            return PRINTER.printItemReport2("Service List", null, null, services, true);
        else
            return PRINTER.printItemReport2("Item List", products, materials, services, true);
    }

    public static boolean printInventoryList() {
        Inventories db = (Inventories) DatabaseFacade.getDatabase("Inventories");
        List<Inventory> banks = db.getBankInventoryList();
        List<Inventory> personals = db.getPersonalInventoryList();
        List<StoreInventory> stores = db.getStoreInventoryList();

        Collections.sort(banks, new Comparator<Inventory>() {

            @Override
            public int compare(Inventory o1, Inventory o2) {
                // TODO Auto-generated method stub
                return o1.getName().compareTo(o2.getName());
            }
        });
        Collections.sort(personals, new Comparator<Inventory>() {

            @Override
            public int compare(Inventory o1, Inventory o2) {
                // TODO Auto-generated method stub
                return o1.getName().compareTo(o2.getName());
            }
        });
        Collections.sort(stores, new Comparator<StoreInventory>() {

            @Override
            public int compare(StoreInventory o1, StoreInventory o2) {
                // TODO Auto-generated method stub
                return o1.getName().compareTo(o2.getName());
            }
        });
        return PRINTER.printInventoryReport1("Inventory List", banks, personals, stores, true, false, false);
    }

    public static boolean printUserList(String type, String category) {
        UserObjects db = (UserObjects) DatabaseFacade.getDatabase("UserObjects");
        List<Person> persons = db.getPersonList();
        List<Organization> organizations = db.getOrganizationList();

        if (type.equals("PERSON"))
            return PRINTER.printUserReport2("User List", persons, null, category, true);
        else
            return PRINTER.printUserReport2("User List", null, organizations, category, true);
    }

    public static void generateItemReport1(String title, List<JCheckBox> checkboxes) {
        Items db = (Items) DatabaseFacade.getDatabase("Items");
        List<Product> products = null;
        List<Material> materials = null;
        List<Service> services = null;
        if (checkboxes.get(0).isSelected())
            products = db.getProductList();
        if (checkboxes.get(1).isSelected())
            materials = db.getMaterialList();
        if (checkboxes.get(2).isSelected())
            services = db.getServiceList();
        PRINTER.printItemReport1(title, products, materials, services);
    }

    public static void generateItemReport2(String title, List<JCheckBox> checkboxes) {
        Items db = (Items) DatabaseFacade.getDatabase("Items");
        List<Product> products = null;
        List<Material> materials = null;
        List<Service> services = null;
        if (checkboxes.get(0).isSelected())
            products = db.getProductList();
        if (checkboxes.get(1).isSelected())
            materials = db.getMaterialList();
        if (checkboxes.get(2).isSelected())
            services = db.getServiceList();
        PRINTER.printItemReport2(title, products, materials, services, checkboxes.get(3).isSelected());
    }

    public static void generateUserReport1(String title, List<JCheckBox> checkboxes, JComboBox<String> combobox) {
        UserObjects db = (UserObjects) DatabaseFacade.getDatabase("UserObjects");
        List<Person> persons = null;
        List<Organization> organizations = null;
        if (checkboxes.get(0).isSelected())
            persons = db.getPersonList();
        if (checkboxes.get(1).isSelected())
            organizations = db.getOrganizationList();

        String category = (String) combobox.getSelectedItem();
        if (!category.equals("ANY")) {
            if (persons != null) {
                List<Person> list = new ArrayList<Person>();
                for (Person person : persons)
                    for (String string : person.getCategories())
                        if (string.equals(category))
                            list.add(person);
                persons = list;
            }
            if (organizations != null) {
                List<Organization> list = new ArrayList<Organization>();
                for (Organization organization : organizations)
                    for (String string : organization.getCategories())
                        if (string.equals(category))
                            list.add(organization);
                organizations = list;
            }
        }
        PRINTER.printUserReport1(title, persons, organizations, category, checkboxes.get(2).isSelected());
    }

    public static void generateUserReport2(String title, List<JCheckBox> checkboxes, JComboBox<String> combobox) {
        UserObjects db = (UserObjects) DatabaseFacade.getDatabase("UserObjects");
        List<Person> persons = null;
        List<Organization> organizations = null;
        if (checkboxes.get(0).isSelected())
            persons = db.getPersonList();
        if (checkboxes.get(1).isSelected())
            organizations = db.getOrganizationList();

        String category = (String) combobox.getSelectedItem();
        if (!category.equals("ANY")) {
            if (persons != null) {
                List<Person> list = new ArrayList<Person>();
                for (Person person : persons)
                    for (String string : person.getCategories())
                        if (string.equals(category))
                            list.add(person);
                persons = list;
            }
            if (organizations != null) {
                List<Organization> list = new ArrayList<Organization>();
                for (Organization organization : organizations)
                    for (String string : organization.getCategories())
                        if (string.equals(category))
                            list.add(organization);
                organizations = list;
            }
        }
        PRINTER.printUserReport2(title, persons, organizations, category, checkboxes.get(2).isSelected());
    }

    public static void generateInventoryReport1(String title, List<JCheckBox> checkboxes) {
        Inventories db = (Inventories) DatabaseFacade.getDatabase("Inventories");
        List<Inventory> banks = null;
        List<Inventory> personals = null;
        List<StoreInventory> stores = null;
        if (checkboxes.get(0).isSelected())
            banks = db.getBankInventoryList();
        if (checkboxes.get(1).isSelected())
            personals = db.getPersonalInventoryList();
        if (checkboxes.get(2).isSelected())
            stores = db.getStoreInventoryList();
        PRINTER.printInventoryReport1(title, banks, personals, stores, checkboxes.get(3).isSelected(),
                checkboxes.get(4).isSelected(), checkboxes.get(5).isSelected());
    }
}

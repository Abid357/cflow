package globals;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfCell;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import cores.ITransaction;
import cores.Inventory;
import cores.Item;
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
import databases.InventoryRecords;
import frames.MyOptionPane;
import helpers.ItemType;

public class PRINTER {

    private static String fullPath;

    private static String getTotals(List<String> list, JTable table, boolean printOutstandingBalance) {
        String phrase = "";
        DefaultTableModel dtm = (DefaultTableModel) table.getModel();

        double totalCredit = 0.0;
        if (list.contains("CREDIT")) {
            phrase += "          Total Credit: ";
            int colIndex = table.getColumnModel().getColumnIndex("CREDIT");
            for (int i = 0; i < table.getRowCount(); i++) {
                String creditString = (String) dtm.getValueAt(i, colIndex);
                if (!creditString.isEmpty())
                    totalCredit += ValueFormatter.parseMoney(creditString);
            }
            phrase += ValueFormatter.formatMoney(totalCredit);
        }

        double totalDebit = 0.0;
        if (list.contains("DEBIT")) {
            phrase += "           Total Debit: ";
            int colIndex = table.getColumnModel().getColumnIndex("DEBIT");
            for (int i = 0; i < table.getRowCount(); i++) {
                String debitString = (String) dtm.getValueAt(i, colIndex);
                if (!debitString.isEmpty())
                    totalDebit += ValueFormatter.parseMoney(debitString);
            }
            phrase += ValueFormatter.formatMoney(totalDebit * -1);
        }

        if (list.contains("BALANCE") && printOutstandingBalance) {
            String balanceString = "N/A";
            if (table.getRowCount() != 0) {
                List<Date> dates = new ArrayList<Date>();
                int colIndex = table.getColumnModel().getColumnIndex("DATE");
                for (int i = 0; i < table.getRowCount(); i++) {
                    String dateString = (String) dtm.getValueAt(i, colIndex);
                    if (!dateString.isEmpty())
                        dates.add(ValueFormatter.parseDate(dateString));
                }
                Date recentDate = Collections.max(dates);
                int rowIndex = dates.indexOf(recentDate);
                colIndex = table.getColumnModel().getColumnIndex("BALANCE");
                balanceString = (String) dtm.getValueAt(rowIndex, colIndex);
            }
            phrase += "          Outstanding Balance: " + balanceString;
        }
        return phrase;
    }

    private static boolean selectDestination(String title) {
        String directory = System.getProperty("user.home") + "/Downloads/" + title;
        JFileChooser destination = new JFileChooser();
        destination.setSelectedFile(new File(directory));
        destination.setMultiSelectionEnabled(false);
        int choice = destination.showSaveDialog(null);
        if (choice == JFileChooser.APPROVE_OPTION) {
            directory = destination.getCurrentDirectory().toString();
            String fileName = destination.getSelectedFile().getName();
            fullPath = directory + "\\" + fileName + ".pdf";
            return true;
        }
        return false;
    }

    public static double[] calculateNetValues(Inventory inventory) {
        InventoryRecords dbRecord = (InventoryRecords) DatabaseFacade.getDatabase("InventoryRecords");
        double netCredit = 0.0, netDebit = 0.0;
        int startIndex = dbRecord.getInventoryStartIndex(ValueFormatter.formatInventory(inventory));
        if (startIndex != -1) {
            int endIndex = dbRecord.getInventoryEndIndex(ValueFormatter.formatInventory(inventory));
            ITransaction t = ValueFormatter.parseBalanceId(dbRecord.get(startIndex).getId());
            if (RecordFacade.isRecordCredit(t, inventory))
                netCredit += t.getAmount();
            else
                netDebit += t.getAmount();
            // startIndex++;
            while (startIndex < endIndex) {
                double currentValue = dbRecord.get(startIndex).getBalance();
                double nextValue = dbRecord.get(startIndex + 1).getBalance();
                if (currentValue < nextValue)
                    netCredit += (nextValue - currentValue);
                else
                    netDebit += (currentValue - nextValue);
                startIndex++;
            }
        }
        return new double[]{netCredit, netDebit};
    }

    public static boolean printInventoryReport1(String title, List<Inventory> bankInventories,
                                                List<Inventory> personalInventories, List<StoreInventory> storeInventories, boolean printBalance,
                                                boolean printNetCredit, boolean printNetDebit) {
        Document document = new Document();
        document.setMargins(30f, 30f, 140f, 70f);
        fullPath = null;
        if (selectDestination(title)) {
            PdfWriter writer = null;
            try {
                writer = PdfWriter.getInstance(document, new FileOutputStream(fullPath));
            } catch (FileNotFoundException | DocumentException e1) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e1);
            }
            writer.setPageEvent(new MyFooter(title, null, null));
            document.open();

            List<String> headers = null;

            double liquidAsset = 0.0;

            // TABLE OF BANK INVENTORIES
            PdfPTable pdfTableBank = null;
            if (bankInventories != null) {
                headers = new ArrayList<String>();
                headers.add("Name");
                headers.add("Account No.");
                if (printNetCredit)
                    headers.add("Net Credit");
                if (printNetDebit)
                    headers.add("Net Debit");
                if (printBalance)
                    headers.add("Current Balance");

                int[] widths;
                if (headers.size() == 2)
                    widths = new int[]{3, 1};
                else if (headers.size() == 3)
                    widths = new int[]{2, 1, 1};
                else if (headers.size() == 4)
                    widths = new int[]{2, 1, 1, 1};
                else
                    widths = new int[]{2, 1, 1, 1, 1};

                pdfTableBank = new PdfPTable(headers.size());
                try {
                    pdfTableBank.setWidths(widths);
                } catch (DocumentException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
                pdfTableBank.setTotalWidth(PageSize.A4.getWidth() - 40);
                pdfTableBank.setLockedWidth(true);

                MyPhrase phrase = new MyPhrase("BANK", Font.BOLD, true);
                phrase.getCell().setColspan(headers.size());
                phrase.getCell().setBorder(PdfPCell.NO_BORDER);
                phrase.getCell().setBackgroundColor(Color.DARK_GRAY);
                phrase.getFont().setColor(Color.WHITE);
                pdfTableBank.addCell(phrase.getCell());

                for (int i = 0; i < headers.size(); i++)
                    pdfTableBank.addCell(new MyPhrase(headers.get(i), Font.BOLD + Font.ITALIC, true).getCell());
                pdfTableBank.setHeaderRows(2);

                for (Inventory inventory : bankInventories) {
                    pdfTableBank.addCell(new MyPhrase(inventory.getName(), Font.PLAIN).getCell());
                    pdfTableBank.addCell(new MyPhrase(inventory.getAccountNo(), Font.PLAIN).getCell());
                    double[] netValues = calculateNetValues(inventory);
                    if (printNetCredit)
                        pdfTableBank.addCell(
                                new MyPhrase(ValueFormatter.formatMoneyNicely(netValues[0]), Font.PLAIN).getCell());
                    if (printNetDebit)
                        pdfTableBank.addCell(
                                new MyPhrase(ValueFormatter.formatMoneyNicely(netValues[1]), Font.PLAIN).getCell());
                    if (printBalance)
                        pdfTableBank.addCell(
                                new MyPhrase(ValueFormatter.formatMoneyNicely(inventory.getBalance()), Font.PLAIN)
                                        .getCell());
                    liquidAsset += inventory.getBalance();
                }
            }

            // TABLE OF PERSONAL INVENTORIES
            PdfPTable pdfTablePersonal = null;
            if (personalInventories != null) {
                headers = new ArrayList<String>();
                headers.add("Name");
                if (printNetCredit)
                    headers.add("Net Credit");
                if (printNetDebit)
                    headers.add("Net Debit");
                if (printBalance)
                    headers.add("Current Balance");

                int[] widths;
                if (headers.size() == 1)
                    widths = new int[]{1};
                else if (headers.size() == 2)
                    widths = new int[]{3, 1};
                else if (headers.size() == 3)
                    widths = new int[]{2, 1, 1};
                else
                    widths = new int[]{2, 1, 1, 1};

                pdfTablePersonal = new PdfPTable(headers.size());
                try {
                    pdfTablePersonal.setWidths(widths);
                } catch (DocumentException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
                pdfTablePersonal.setTotalWidth(PageSize.A4.getWidth() - 40);
                pdfTablePersonal.setLockedWidth(true);

                MyPhrase phrase = new MyPhrase("PERSONAL", Font.BOLD, true);
                phrase.getCell().setColspan(headers.size());
                phrase.getCell().setBorder(PdfPCell.NO_BORDER);
                phrase.getCell().setBackgroundColor(Color.DARK_GRAY);
                phrase.getFont().setColor(Color.WHITE);
                pdfTablePersonal.addCell(phrase.getCell());

                for (int i = 0; i < headers.size(); i++)
                    pdfTablePersonal.addCell(new MyPhrase(headers.get(i), Font.BOLD + Font.ITALIC, true).getCell());
                pdfTablePersonal.setHeaderRows(2);

                for (Inventory inventory : personalInventories) {
                    pdfTablePersonal.addCell(new MyPhrase(inventory.getName(), Font.PLAIN).getCell());
                    double[] netValues = calculateNetValues(inventory);
                    if (printNetCredit)
                        pdfTablePersonal.addCell(
                                new MyPhrase(ValueFormatter.formatMoneyNicely(netValues[0]), Font.PLAIN).getCell());
                    if (printNetDebit)
                        pdfTablePersonal.addCell(
                                new MyPhrase(ValueFormatter.formatMoneyNicely(netValues[1]), Font.PLAIN).getCell());
                    if (printBalance)
                        pdfTablePersonal.addCell(
                                new MyPhrase(ValueFormatter.formatMoneyNicely(inventory.getBalance()), Font.PLAIN)
                                        .getCell());
                    liquidAsset += inventory.getBalance();
                }
            }

            if ((bankInventories != null || personalInventories != null) && printBalance) {
                MyPhrase phrase = new MyPhrase("Liquid Asset:", Font.BOLD, false);
                if (headers.size() != 1)
                    phrase.getCell().setColspan(headers.size() - 1);
                phrase.getCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                phrase.getCell().setBorder(PdfPCell.NO_BORDER);

                MyPhrase phrase2 = new MyPhrase(ValueFormatter.formatMoneyNicely(liquidAsset), Font.BOLD, false);
                phrase2.getCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                phrase2.getCell().setBorder(PdfPCell.NO_BORDER);

                if (pdfTablePersonal == null) {
                    pdfTableBank.addCell(phrase.getCell());
                    pdfTableBank.addCell(phrase2.getCell());
                } else {
                    pdfTablePersonal.addCell(phrase.getCell());
                    pdfTablePersonal.addCell(phrase2.getCell());
                }
            }

            double solidAsset = 0.0;

            // TABLE OF STORE INVENTORIES
            PdfPTable pdfTableStore = null;
            if (storeInventories != null) {
                headers = new ArrayList<String>();
                headers.add("Name");
                if (printNetCredit)
                    headers.add("Net Credit");
                if (printNetDebit)
                    headers.add("Net Debit");
                if (printBalance)
                    headers.add("Current Asset");

                int[] widths;
                if (headers.size() == 1)
                    widths = new int[]{1};
                else if (headers.size() == 2)
                    widths = new int[]{3, 1};
                else if (headers.size() == 3)
                    widths = new int[]{2, 1, 1};
                else
                    widths = new int[]{2, 1, 1, 1};

                pdfTableStore = new PdfPTable(headers.size());
                try {
                    pdfTableStore.setWidths(widths);
                } catch (DocumentException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
                pdfTableStore.setTotalWidth(PageSize.A4.getWidth() - 40);
                pdfTableStore.setLockedWidth(true);

                MyPhrase phrase = new MyPhrase("STORE", Font.BOLD, true);
                phrase.getCell().setColspan(headers.size());
                phrase.getCell().setBorder(PdfPCell.NO_BORDER);
                phrase.getCell().setBackgroundColor(Color.DARK_GRAY);
                phrase.getFont().setColor(Color.WHITE);
                pdfTableStore.addCell(phrase.getCell());

                for (int i = 0; i < headers.size(); i++)
                    pdfTableStore.addCell(new MyPhrase(headers.get(i), Font.BOLD + Font.ITALIC, true).getCell());
                pdfTableStore.setHeaderRows(2);

                for (StoreInventory inventory : storeInventories) {
                    pdfTableStore.addCell(new MyPhrase(inventory.getName(), Font.PLAIN).getCell());
                    double[] netValues = calculateNetValues(inventory);
                    if (printNetCredit)
                        pdfTableStore.addCell(
                                new MyPhrase(ValueFormatter.formatMoneyNicely(netValues[0]), Font.PLAIN).getCell());
                    if (printNetDebit)
                        pdfTableStore.addCell(
                                new MyPhrase(ValueFormatter.formatMoneyNicely(netValues[1]), Font.PLAIN).getCell());
                    if (printBalance)
                        pdfTableStore.addCell(
                                new MyPhrase(ValueFormatter.formatMoneyNicely(inventory.getBalance()), Font.PLAIN)
                                        .getCell());
                    solidAsset += inventory.getBalance();
                }
            }

            if (storeInventories != null && printBalance) {
                MyPhrase phrase = new MyPhrase("Solid Asset:", Font.BOLD, false);
                if (headers.size() != 1)
                    phrase.getCell().setColspan(headers.size() - 1);
                phrase.getCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                phrase.getCell().setBorder(PdfPCell.NO_BORDER);

                MyPhrase phrase2 = new MyPhrase(ValueFormatter.formatMoneyNicely(solidAsset), Font.BOLD, false);
                phrase2.getCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                phrase2.getCell().setBorder(PdfPCell.NO_BORDER);

                pdfTableStore.addCell(phrase.getCell());
                pdfTableStore.addCell(phrase2.getCell());
            }

            try {
                if (pdfTableBank != null)
                    document.add(pdfTableBank);
                if (pdfTablePersonal != null)
                    document.add(pdfTablePersonal);
                if (pdfTableStore != null)
                    document.add(pdfTableStore);
            } catch (DocumentException e1) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e1);
            }
            document.close();

            if (fullPath != null) {
                File file = new File(fullPath);
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
            }
            return true;
        } else {
            new MyOptionPane("You cancelled the operation.", MyOptionPane.INFORMATION_MESSAGE);
            return false;
        }
    }

    public static boolean printTransactionReport1(JTable table, String title, Date fromDate, Date toDate,
                                                  List<String> list, boolean printOutstandingBalance) {
        Document document = new Document();
        document.setMargins(30f, 30f, 140f, 70f);
        fullPath = null;
        if (selectDestination(title)) {
            PdfWriter writer = null;
            try {
                writer = PdfWriter.getInstance(document, new FileOutputStream(fullPath));
            } catch (FileNotFoundException | DocumentException e1) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e1);
            }
            writer.setPageEvent(new MyFooter(title, fromDate, toDate));
            document.open();

            PdfPTable pdfTable = new PdfPTable(list.size());
            pdfTable.setTotalWidth(PageSize.A4.getWidth() - 40);
            pdfTable.setLockedWidth(true);

            for (int i = 0; i < list.size(); i++) {
                pdfTable.addCell(new MyPhrase(list.get(i), Font.BOLD + Font.ITALIC, true).getCell());
            }
            pdfTable.setHeaderRows(1);

            // based on string list, clean up table and sort
            List<Integer> indices = new ArrayList<Integer>();
            TableColumnModel tcm = table.getColumnModel();
            for (int i = 0; i < list.size(); i++)
                indices.add(tcm.getColumnIndex(list.get(i)));

            DefaultTableModel dtm = (DefaultTableModel) table.getModel();
            for (int i = 0; i < table.getRowCount(); i++)
                for (int j = 0; j < indices.size(); j++) {
                    pdfTable.addCell(
                            new MyPhrase(dtm.getValueAt(i, indices.get(j)).toString(), Font.PLAIN, false).getCell());
                }
            MyPhrase phrase = new MyPhrase(getTotals(list, table, printOutstandingBalance), Font.BOLD, false);
            phrase.getCell().setColspan(indices.size());
            phrase.getCell().setBorder(PdfCell.NO_BORDER);
            pdfTable.addCell(phrase.getCell());
            pdfTable.completeRow();
            try {
                document.add(pdfTable);
            } catch (DocumentException e1) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e1);
            }
            document.close();

            if (fullPath != null) {
                File file = new File(fullPath);
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
            }
            return true;
        } else {
            new MyOptionPane("You cancelled the operation.", MyOptionPane.INFORMATION_MESSAGE);
            return false;
        }
    }

    public static boolean printTransactionReport2(String title, Transaction transaction,
                                                  boolean printUserDetails, boolean printInventoryDetails) {
        Document document = new Document();
        document.setMargins(30f, 30f, 140f, 70f);
        fullPath = null;
        if (selectDestination(title)) {
            PdfWriter writer = null;
            try {
                writer = PdfWriter.getInstance(document, new FileOutputStream(fullPath));
            } catch (FileNotFoundException | DocumentException e1) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e1);
            }
            writer.setPageEvent(new MyFooter(title, null, null));
            document.open();

            PdfPTable pdfTable = new PdfPTable(5);
            pdfTable.setTotalWidth(PageSize.A4.getWidth() - 40);
            pdfTable.setLockedWidth(true);

            if (transaction != null) {
                Paragraph p = new Paragraph();
                String paidOrReceived = "paid";
                if (!transaction.isCredit())
                    paidOrReceived = "received";

                String personalOrBank = "bank";
                if (transaction.getInventory().getAccountNo() == null || transaction.getInventory().getAccountNo().isEmpty())
                    personalOrBank = "personal";

                double total = RecordFacade.calculateAmountWithTax(transaction.getAmount(), transaction.getTax());
               total = ValueFormatter.addSign(total, transaction.isCredit());

                String string =
                        ValueFormatter.formatUserObject(transaction.getUserObject()) + " has " + paidOrReceived + " " + total + " AED via " + personalOrBank + " inventory.";
                MyChunk summary = new MyChunk(string, 12, Font.ITALIC);
                p.add(summary);

                PdfPCell cell = new PdfPCell();
                cell.addElement(p);
                cell.setColspan(5);
                cell.setBorder(0);
                pdfTable.addCell(cell);

                p = new Paragraph(new MyChunk(ValueFormatter.formatBalanceId(transaction.getId(),
                        Transaction.class), 15, Font.BOLD));
                cell = new PdfPCell();
                cell.addElement(p);
                cell.setColspan(2);
                cell.setBorder(0);
                pdfTable.addCell(cell);

                p = new Paragraph();
                p.setAlignment(Paragraph.ALIGN_RIGHT);
                MyChunk dateKey = new MyChunk("Date: ", 12,
                        Font.BOLD);
                p.add(dateKey);
                MyChunk dateValue = new MyChunk(ValueFormatter.formatDate(transaction.getDate()), 12, Font.PLAIN);
                p.add(dateValue);

                cell = new PdfPCell();
                cell.addElement(p);
                cell.setColspan(3);
                cell.setBorder(0);
                pdfTable.addCell(cell);

                p = new Paragraph();
                p.setAlignment(Paragraph.ALIGN_RIGHT);
                MyChunk keys = new MyChunk("Subtotal:\nTax/VAT:", 12, Font.PLAIN);
                p.add(keys);

                cell = new PdfPCell();
                cell.addElement(p);
                cell.setColspan(2);
                cell.setBorder(0);
                pdfTable.addCell(cell);

                p = new Paragraph();
                p.setAlignment(Paragraph.ALIGN_CENTER);
                MyChunk values =
                        new MyChunk(ValueFormatter.formatMoneyNicely(transaction.getAmount()) + "\n" + ValueFormatter.formatMoneyNicely(transaction.getTax()), 12, Font.PLAIN);
                p.add(values);

                cell = new PdfPCell();
                cell.addElement(p);
                cell.setBorder(0);
                pdfTable.addCell(cell);

                p = new Paragraph();
                p.setAlignment(Paragraph.ALIGN_LEFT);
                MyChunk aeds =
                        new MyChunk("AED\nAED", 12,
                                Font.PLAIN);
                p.add(aeds);

                cell = new PdfPCell();
                cell.addElement(p);
                cell.setColspan(2);
                cell.setBorder(0);
                pdfTable.addCell(cell);

                p = new Paragraph();
                p.setAlignment(Paragraph.ALIGN_RIGHT);
                MyChunk totalKey = new MyChunk("Total:", 12, Font.BOLD);
                p.add(totalKey);

                cell = new PdfPCell();
                cell.addElement(p);
                cell.setColspan(2);
                cell.setBorder(0);
                pdfTable.addCell(cell);

                p = new Paragraph();
                p.setAlignment(Paragraph.ALIGN_CENTER);
                MyChunk totalValue =
                        new MyChunk(ValueFormatter.formatMoneyNicely(total), 12, Font.BOLD);
                p.add(totalValue);

                cell = new PdfPCell();
                cell.addElement(p);
                cell.setBorder(0);
                pdfTable.addCell(cell);

                p = new Paragraph();
                p.setAlignment(Paragraph.ALIGN_LEFT);
                MyChunk aed =
                        new MyChunk("AED", 12,
                                Font.BOLD);
                p.add(aed);

                cell = new PdfPCell();
                cell.addElement(p);
                cell.setColspan(2);
                cell.setBorder(0);
                pdfTable.addCell(cell);

                MyPhrase inWordsKey = new MyPhrase("In Words:", 12, Font.BOLD, false);
                cell = inWordsKey.getCell();
                cell.setBorder(0);
                pdfTable.addCell(cell);

                MyPhrase inWordsValue = new MyPhrase(ValueFormatter.convertNumberToWords(total), 12, Font.ITALIC, false);
                cell = inWordsValue.getCell();
                cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                cell.setColspan(3);
                cell.setBorder(0);
                pdfTable.addCell(cell);
            }

            PdfPTable pdfTableUser = new PdfPTable(1);
            pdfTableUser.setTotalWidth(PageSize.A4.getWidth() - 40);
            pdfTableUser.setLockedWidth(true);

            if (printUserDetails) {
                Paragraph p = new Paragraph();
                p.setAlignment(Paragraph.ALIGN_CENTER);
                MyChunk header = new MyChunk("User Details", 12, Font.BOLD);
                p.add(header);

                PdfPCell cell = new PdfPCell();
                cell.addElement(p);

                p = new Paragraph();
                String fullName = "";
                String companyName = "";
                if (transaction.getUserObject() instanceof Person){
                    Person person = (Person) transaction.getUserObject();
                    fullName = person.getFirstName() + " " + person.getLastName();
                }
                else {
                    Organization organization = (Organization) transaction.getUserObject();
                    fullName = organization.getContactName();
                    companyName = organization.getName();
                }

                MyChunk name = new MyChunk("Name: ", fullName);
                name.updateFontSize(12);
                p.add(name.getKey());
                p.add(name.getValue());

                MyChunk company = new MyChunk("\nCompany: ", companyName);
                company.updateFontSize(12);
                p.add(company.getKey());
                p.add(company.getValue());

                MyChunk email = new MyChunk("\nEmail: ", transaction.getUserObject().getEmail());
                email.updateFontSize(12);
                p.add(email.getKey());
                p.add(email.getValue());

                MyChunk phone = new MyChunk("\nPhone: ", transaction.getUserObject().getPhone());
                phone.updateFontSize(12);
                p.add(phone.getKey());
                p.add(phone.getValue());

                cell.addElement(p);
                cell.setPaddingBottom(10);
                pdfTableUser.addCell(cell);
            }

            PdfPTable pdfTableInventory = new PdfPTable(1);
            pdfTableInventory.setTotalWidth(PageSize.A4.getWidth() - 40);
            pdfTableInventory.setLockedWidth(true);

            if (printInventoryDetails) {
                Paragraph p = new Paragraph();
                p.setAlignment(Paragraph.ALIGN_CENTER);
                MyChunk header = new MyChunk("Inventory Details", 12, Font.BOLD);
                p.add(header);

                Inventory inventory = transaction.getInventory();
                PdfPCell cell = new PdfPCell();
                cell.addElement(p);

                p = new Paragraph();
                String inventoryType = "Personal";
                if (inventory.getAccountNo() != null && !inventory.getAccountNo().isEmpty())
                    inventoryType = "Bank";
                MyChunk type = new MyChunk("Type: ", inventoryType);
                type.updateFontSize(12);
                p.add(type.getKey());
                p.add(type.getValue());

                MyChunk name = new MyChunk("\nName: ", inventory.getName());
                name.updateFontSize(12);
                p.add(name.getKey());
                p.add(name.getValue());

                MyChunk accountNo = new MyChunk("\nAccount#: ", inventory.getAccountNo());
                accountNo.updateFontSize(12);
                p.add(accountNo.getKey());
                p.add(accountNo.getValue());

                cell.addElement(p);
                cell.setPaddingBottom(10);
                pdfTableInventory.addCell(cell);
            }


            try {
                document.add(pdfTable);
                if (printUserDetails) {
                    document.add(new Paragraph("\n"));
                    document.add(pdfTableUser);
                }
                if (printInventoryDetails) {
                    document.add(new Paragraph("\n"));
                    document.add(pdfTableInventory);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            document.close();

            if (fullPath != null) {
                File file = new File(fullPath);
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
            }
        }
        return true;
    }

    public static void printStockReport1(String title, SortedSet<String> set, double[][] totals) {
        Document document = new Document(PageSize.A4.rotate());
        document.setMargins(30f, 30f, 140f, 70f);
        fullPath = null;
        if (selectDestination(title)) {
            PdfWriter writer = null;
            try {
                writer = PdfWriter.getInstance(document, new FileOutputStream(fullPath));
            } catch (FileNotFoundException | DocumentException e1) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e1);
            }
            writer.setPageEvent(new MyFooter(title, null, null));
            document.open();

            Map<Integer, String> map = new HashMap<Integer, String>();
            map.put(0, "Jan");
            map.put(1, "Feb");
            map.put(2, "Mar");
            map.put(3, "Apr");
            map.put(4, "May");
            map.put(5, "Jun");
            map.put(6, "Jul");
            map.put(7, "Aug");
            map.put(8, "Sep");
            map.put(9, "Oct");
            map.put(10, "Nov");
            map.put(11, "Dec");

            PdfPTable pdfTable = new PdfPTable(map.size() + 2);
            pdfTable.setTotalWidth(PageSize.A4.getHeight() - 40);
            pdfTable.setLockedWidth(true);

            pdfTable.addCell(new MyPhrase("Month / Item", Font.BOLD + Font.ITALIC, true).getCell());
            for (int i = 0; i < map.size(); i++)
                pdfTable.addCell(new MyPhrase(map.get(i), Font.BOLD + Font.ITALIC, true).getCell());
            pdfTable.addCell(new MyPhrase("Total", Font.BOLD + Font.ITALIC, true).getCell());
            try {
                pdfTable.setWidths(new int[]{2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1});
            } catch (DocumentException e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            pdfTable.setHeaderRows(1);

            double[] rowTotals = rowSum(totals);
            String[] list = set.toArray(new String[0]);
            for (int i = 0; i < list.length; i++) {
                Item item = ValueFormatter.parseItem(list[i]);
                MyPhrase phrase = new MyPhrase(item.getName() + " (" + item.getUnit() + ")", Font.ITALIC, false);
                phrase.getCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                pdfTable.addCell(phrase.getCell());
                for (int j = 0; j < 12; j++)
                    pdfTable.addCell(
                            new MyPhrase(ValueFormatter.formatQuantity(totals[i][j]), Font.PLAIN, false).getCell());
                pdfTable.addCell(new MyPhrase(ValueFormatter.formatQuantity(rowTotals[i]), Font.BOLD, false).getCell());
            }

            pdfTable.completeRow();
            try {
                document.add(pdfTable);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            document.close();

            if (fullPath != null) {
                File file = new File(fullPath);
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
            }
        }
    }

    public static void printStockReport2(String title, List<Store> list) {
        Document document = new Document();
        document.setMargins(30f, 30f, 140f, 70f);
        fullPath = null;
        if (selectDestination(title)) {
            PdfWriter writer = null;
            try {
                writer = PdfWriter.getInstance(document, new FileOutputStream(fullPath));
            } catch (FileNotFoundException | DocumentException e1) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e1);
            }
            writer.setPageEvent(new MyFooter(title, null, null));
            document.open();

            List<String> headers = Arrays.asList("Store", "Owner", "Items", "Quantity", "Unit Price", "Total Price",
                    "Total Asset");
            PdfPTable pdfTable = new PdfPTable(headers.size());
            try {
                pdfTable.setWidths(new int[]{1, 1, 2, 1, 1, 1, 1});
            } catch (DocumentException e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            pdfTable.setTotalWidth(PageSize.A4.getWidth() - 40);
            pdfTable.setLockedWidth(true);
            for (int i = 0; i < headers.size(); i++)
                pdfTable.addCell(new MyPhrase(headers.get(i), Font.BOLD + Font.ITALIC, true).getCell());
            pdfTable.setHeaderRows(1);

            double totalSolidAsset = 0.0;
            for (int i = 0; i < list.size(); i++) {
                double totalAssetPerStore = 0.0;
                int extraRows = 0;
                List<Stock> lstStock = list.get(i).getItems();
                List<Stock> lstProduct = new ArrayList<Stock>();
                List<Stock> lstMaterial = new ArrayList<Stock>();
                Collections.sort(lstStock, new Comparator<Stock>() {

                    @Override
                    public int compare(Stock o1, Stock o2) {
                        // TODO Auto-generated method stub
                        return o1.getItem().getName().compareTo(o2.getItem().getName());
                    }
                });
                for (Stock stock : lstStock) {
                    totalAssetPerStore += (stock.getPrice() * stock.getQuantity());
                    if (stock.getItem().getType().equals(ItemType.PRODUCT))
                        lstProduct.add(stock);
                    else if (stock.getItem().getType().equals(ItemType.MATERIAL))
                        lstMaterial.add(stock);
                }

                if (!lstProduct.isEmpty())
                    ++extraRows;
                if (!lstMaterial.isEmpty())
                    ++extraRows;

                int rowspan = lstProduct.size() + lstMaterial.size() + extraRows;

                MyPhraseForStock phrase = new MyPhraseForStock(list.get(i).getName(), Font.PLAIN);
                phrase.getCell().setRowspan(rowspan); // +2 for grayed rows PRODUCT and
                // MATERIAL
                pdfTable.addCell(phrase.getCell());
                phrase = new MyPhraseForStock(ValueFormatter.formatUserObject(list.get(i).getOwner()), Font.PLAIN);
                phrase.getCell().setRowspan(rowspan);
                pdfTable.addCell(phrase.getCell());

                boolean isLastRowPrinted = false;

                if (!lstProduct.isEmpty()) {
                    phrase = new MyPhraseForStock("PRODUCT", Font.BOLD);
                    phrase.getCell().setColspan(4);
                    phrase.getCell().setBackgroundColor(Color.LIGHT_GRAY);
                    phrase.getFont().setColor(Color.WHITE);
                    pdfTable.addCell(phrase.getCell());
                    for (Stock stock : lstProduct) {
                        if (!isLastRowPrinted) {
                            phrase = new MyPhraseForStock(ValueFormatter.formatMoneyNicely(totalAssetPerStore),
                                    Font.BOLD);
                            phrase.getCell().setRowspan(rowspan);
                            pdfTable.addCell(phrase.getCell());
                            isLastRowPrinted = true;
                        }
                        pdfTable.addCell(new MyPhraseForStock(stock.getItem().getName(), Font.PLAIN).getCell());
                        pdfTable.addCell(
                                new MyPhraseForStock(ValueFormatter.formatQuantity(stock.getQuantity()), Font.PLAIN)
                                        .getCell());
                        pdfTable.addCell(new MyPhraseForStock(ValueFormatter.formatRate(stock.getPrice()), Font.PLAIN)
                                .getCell());
                        pdfTable.addCell(new MyPhraseForStock(
                                ValueFormatter.formatMoneyNicely(stock.getPrice() * stock.getQuantity()), Font.PLAIN)
                                .getCell());
                    }
                }

                if (!lstMaterial.isEmpty()) {
                    phrase = new MyPhraseForStock("MATERIAL", Font.BOLD);
                    phrase.getCell().setColspan(4);
                    phrase.getCell().setBackgroundColor(Color.LIGHT_GRAY);
                    phrase.getFont().setColor(Color.WHITE);
                    pdfTable.addCell(phrase.getCell());
                    for (Stock stock : lstMaterial) {
                        if (!isLastRowPrinted) {
                            phrase = new MyPhraseForStock(ValueFormatter.formatMoneyNicely(totalAssetPerStore),
                                    Font.BOLD);
                            phrase.getCell().setRowspan(rowspan);
                            pdfTable.addCell(phrase.getCell());
                            isLastRowPrinted = true;
                        }
                        pdfTable.addCell(new MyPhraseForStock(stock.getItem().getName(), Font.PLAIN).getCell());
                        pdfTable.addCell(
                                new MyPhraseForStock(ValueFormatter.formatQuantity(stock.getQuantity()), Font.PLAIN)
                                        .getCell());
                        pdfTable.addCell(new MyPhraseForStock(ValueFormatter.formatRate(stock.getPrice()), Font.PLAIN)
                                .getCell());
                        pdfTable.addCell(new MyPhraseForStock(
                                ValueFormatter.formatMoneyNicely(stock.getPrice() * stock.getQuantity()), Font.PLAIN)
                                .getCell());
                    }
                }

                totalSolidAsset += totalAssetPerStore;
            }

            MyPhrase phrase = new MyPhrase("Total Solid Asset:", Font.BOLD, false);
            phrase.getCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            phrase.getCell().setColspan(headers.size() - 1);
            phrase.getCell().setBorder(PdfPCell.TOP);
            pdfTable.addCell(phrase.getCell());

            phrase = new MyPhrase(ValueFormatter.formatMoneyNicely(totalSolidAsset), Font.BOLD, false);
            phrase.getCell().setBorder(PdfPCell.TOP);
            pdfTable.addCell(phrase.getCell());
            pdfTable.completeRow();
            try {
                document.add(pdfTable);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            document.close();

            if (fullPath != null) {
                File file = new File(fullPath);
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
            }
        }
    }

    public static void printStockReport3(String title, Store store) {
        Document document = new Document();
        document.setMargins(30f, 30f, 140f, 70f);
        fullPath = null;
        if (selectDestination(title)) {
            PdfWriter writer = null;
            try {
                writer = PdfWriter.getInstance(document, new FileOutputStream(fullPath));
            } catch (FileNotFoundException | DocumentException e1) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e1);
            }
            writer.setPageEvent(new MyFooter(title, null, null));
            document.open();

            List<String> headers = Arrays.asList("Item", "Quantity", "Unit Price", "Total Price");

            // TABLE OF PRODUCTS
            PdfPTable pdfTableProduct = new PdfPTable(headers.size());
            try {
                pdfTableProduct.setWidths(new int[]{2, 1, 1, 1});
            } catch (DocumentException e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            pdfTableProduct.setTotalWidth(PageSize.A4.getWidth() - 40);
            pdfTableProduct.setLockedWidth(true);
            MyPhrase phrase = new MyPhrase("PRODUCT", Font.BOLD, true);
            phrase.getCell().setColspan(headers.size());
            phrase.getCell().setBorder(PdfPCell.NO_BORDER);
            phrase.getCell().setBackgroundColor(Color.DARK_GRAY);
            phrase.getFont().setColor(Color.WHITE);
            pdfTableProduct.addCell(phrase.getCell());

            for (int i = 0; i < headers.size(); i++)
                pdfTableProduct.addCell(new MyPhrase(headers.get(i), Font.BOLD + Font.ITALIC, true).getCell());
            pdfTableProduct.setHeaderRows(2);

            // TABLE OF MATERIALS
            PdfPTable pdfTableMaterial = new PdfPTable(headers.size());
            try {
                pdfTableMaterial.setWidths(new int[]{2, 1, 1, 1});
            } catch (DocumentException e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            pdfTableMaterial.setTotalWidth(PageSize.A4.getWidth() - 40);
            pdfTableMaterial.setLockedWidth(true);

            phrase = new MyPhrase("MATERIAL", Font.BOLD, true);
            phrase.getCell().setColspan(headers.size());
            phrase.getCell().setBorder(PdfPCell.NO_BORDER);
            phrase.getCell().setBackgroundColor(Color.DARK_GRAY);
            phrase.getFont().setColor(Color.WHITE);
            pdfTableMaterial.addCell(phrase.getCell());

            for (int i = 0; i < headers.size(); i++)
                pdfTableMaterial.addCell(new MyPhrase(headers.get(i), Font.BOLD + Font.ITALIC, true).getCell());
            pdfTableMaterial.setHeaderRows(2);

            double totalSolidAsset = 0.0;
            double totalProductAsset = 0.0;
            double totalMaterialAsset = 0.0;
            List<Stock> lstStock = store.getItems();
            Collections.sort(lstStock, new Comparator<Stock>() {

                @Override
                public int compare(Stock o1, Stock o2) {
                    // TODO Auto-generated method stub
                    return o1.getItem().getName().compareTo(o2.getItem().getName());
                }
            });
            for (Stock stock : lstStock) {
                if (stock.getItem().getType().equals(ItemType.PRODUCT)) {
                    pdfTableProduct.addCell(new MyPhraseForStock(stock.getItem().getName(), Font.PLAIN));
                    pdfTableProduct.addCell(
                            new MyPhraseForStock(ValueFormatter.formatQuantity(stock.getQuantity()), Font.PLAIN)
                                    .getCell());
                    pdfTableProduct.addCell(
                            new MyPhraseForStock(ValueFormatter.formatRate(stock.getPrice()), Font.PLAIN).getCell());
                    double total = stock.getQuantity() * stock.getPrice();
                    pdfTableProduct.addCell(
                            new MyPhraseForStock(ValueFormatter.formatMoneyNicely(total), Font.PLAIN).getCell());
                    totalProductAsset += total;
                    totalSolidAsset += total;
                } else if (stock.getItem().getType().equals(ItemType.MATERIAL)) {
                    pdfTableMaterial.addCell(new MyPhraseForStock(stock.getItem().getName(), Font.PLAIN));
                    pdfTableMaterial.addCell(
                            new MyPhraseForStock(ValueFormatter.formatQuantity(stock.getQuantity()), Font.PLAIN)
                                    .getCell());
                    pdfTableMaterial.addCell(
                            new MyPhraseForStock(ValueFormatter.formatRate(stock.getPrice()), Font.PLAIN).getCell());
                    double total = stock.getQuantity() * stock.getPrice();
                    pdfTableMaterial.addCell(
                            new MyPhraseForStock(ValueFormatter.formatMoneyNicely(total), Font.PLAIN).getCell());
                    totalMaterialAsset += total;
                    totalSolidAsset += total;
                }
            }

            phrase = new MyPhrase("Total Product Asset:", Font.BOLD, false);
            phrase.getCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            phrase.getCell().setColspan(headers.size() - 1);
            phrase.getCell().setBorder(PdfPCell.TOP);
            pdfTableProduct.addCell(phrase.getCell());

            phrase = new MyPhrase(ValueFormatter.formatMoneyNicely(totalProductAsset), Font.BOLD, false);
            phrase.getCell().setBorder(PdfPCell.TOP);
            pdfTableProduct.addCell(phrase.getCell());

            phrase = new MyPhrase("Total Material Asset:", Font.BOLD, false);
            phrase.getCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            phrase.getCell().setColspan(headers.size() - 1);
            phrase.getCell().setBorder(PdfPCell.TOP);
            pdfTableMaterial.addCell(phrase.getCell());

            phrase = new MyPhrase(ValueFormatter.formatMoneyNicely(totalMaterialAsset), Font.BOLD, false);
            phrase.getCell().setBorder(PdfPCell.TOP);
            pdfTableMaterial.addCell(phrase.getCell());

            phrase = new MyPhrase("Total Solid Asset:", Font.BOLD, false);
            phrase.getCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            phrase.getCell().setColspan(headers.size() - 1);
            phrase.getCell().setBorder(PdfPCell.TOP);
            pdfTableMaterial.addCell(phrase.getCell());

            phrase = new MyPhrase(ValueFormatter.formatMoneyNicely(totalSolidAsset), Font.BOLD, false);
            phrase.getCell().setBorder(PdfPCell.TOP);
            pdfTableMaterial.addCell(phrase.getCell());

            pdfTableProduct.completeRow();
            pdfTableMaterial.completeRow();
            try {
                document.add(pdfTableProduct);
                document.add(pdfTableMaterial);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            document.close();

            if (fullPath != null) {
                File file = new File(fullPath);
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
            }
        }
    }

    private static double[] rowSum(double[][] totals) {
        double[] sum = new double[totals.length];
        for (int i = 0; i < totals.length; i++) {
            sum[i] = 0;
            for (int j = 0; j < totals[0].length; j++)
                sum[i] += totals[i][j];
        }
        return sum;
    }

    private static double[] colSum(double[][] totals) {
        double[] sum = new double[totals[0].length];
        for (int i = 0; i < totals[0].length; i++) {
            sum[i] = 0;
            for (int j = 0; j < totals.length; j++)
                sum[i] += totals[j][i];
        }
        return sum;
    }

    public static void printPurchaseReport1(String title, SortedSet<String> set, double[][] totals) {
        Document document = new Document(PageSize.A4.rotate());
        document.setMargins(30f, 30f, 140f, 70f);
        fullPath = null;
        if (selectDestination(title)) {
            PdfWriter writer = null;
            try {
                writer = PdfWriter.getInstance(document, new FileOutputStream(fullPath));
            } catch (FileNotFoundException | DocumentException e1) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e1);
            }
            writer.setPageEvent(new MyFooter(title, null, null));
            document.open();

            Map<Integer, String> map = new HashMap<Integer, String>();
            map.put(0, "Jan");
            map.put(1, "Feb");
            map.put(2, "Mar");
            map.put(3, "Apr");
            map.put(4, "May");
            map.put(5, "Jun");
            map.put(6, "Jul");
            map.put(7, "Aug");
            map.put(8, "Sep");
            map.put(9, "Oct");
            map.put(10, "Nov");
            map.put(11, "Dec");

            PdfPTable pdfTable = new PdfPTable(map.size() + 2);
            pdfTable.setTotalWidth(PageSize.A4.getHeight() - 40);
            pdfTable.setLockedWidth(true);

            pdfTable.addCell(new MyPhrase("Month / Supplier", Font.BOLD + Font.ITALIC, true).getCell());
            for (int i = 0; i < map.size(); i++)
                pdfTable.addCell(new MyPhrase(map.get(i), Font.BOLD + Font.ITALIC, true).getCell());
            pdfTable.addCell(new MyPhrase("Supplier Totals", Font.BOLD + Font.ITALIC, true).getCell());
            pdfTable.setHeaderRows(1);

            double[] rowTotals = rowSum(totals);
            double[] colTotals = colSum(totals);
            String[] list = set.toArray(new String[0]);
            for (int i = 0; i < list.length; i++) {
                MyPhrase phrase = new MyPhrase(list[i], Font.ITALIC, false);
                phrase.getCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                pdfTable.addCell(phrase.getCell());
                for (int j = 0; j < 12; j++)
                    pdfTable.addCell(
                            new MyPhrase(ValueFormatter.formatMoneyNicely(totals[i][j]), Font.PLAIN, false).getCell());
                pdfTable.addCell(
                        new MyPhrase(ValueFormatter.formatMoneyNicely(rowTotals[i]), Font.BOLD, false).getCell());
            }

            // last row for column totals
            pdfTable.addCell(new MyPhrase("Month Totals", Font.BOLD + Font.ITALIC, false).getCell());
            for (int i = 0; i < colTotals.length; i++)
                pdfTable.addCell(
                        new MyPhrase(ValueFormatter.formatMoneyNicely(colTotals[i]), Font.BOLD, false).getCell());

            /*
             * perform sum ROW-wise or COLUMN-wise
             */
            double sumColTotals = 0.0;
            for (int i = 0; i < colTotals.length; i++)
                sumColTotals += colTotals[i];
            pdfTable.addCell(new MyPhrase(ValueFormatter.formatMoneyNicely(sumColTotals), Font.BOLD, false).getCell());
            pdfTable.completeRow();
            try {
                document.add(pdfTable);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            document.close();

            if (fullPath != null) {
                File file = new File(fullPath);
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
            }
        }
    }

    public static void printPurchaseReport2(String title, Date fromDate, Date toDate, List<PurchaseTransaction> list,
                                            boolean printQuantity, boolean printUnit, boolean printPrice, boolean printStore, boolean printAmount) {
        Document document = new Document();
        document.setMargins(30f, 30f, 140f, 70f);
        fullPath = null;
        if (selectDestination(title)) {
            PdfWriter writer = null;
            try {
                writer = PdfWriter.getInstance(document, new FileOutputStream(fullPath));
            } catch (FileNotFoundException | DocumentException e1) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e1);
            }
            writer.setPageEvent(new MyFooter(title, fromDate, toDate));
            document.open();

            List<String> headers = new ArrayList<String>();
            headers.add("Date");
            headers.add("Item(s)");
            if (printQuantity)
                headers.add("Quantity");
            if (printUnit)
                headers.add("Unit");
            if (printPrice)
                headers.add("Unit Price");
            if (printStore)
                headers.add("Store");
            if (printAmount)
                headers.add("Amount");

            int[] widths;
            if (headers.size() == 2)
                widths = new int[]{1, 3};
            else if (headers.size() == 3)
                widths = new int[]{1, 2, 1};
            else if (headers.size() == 4)
                widths = new int[]{1, 2, 1, 1};
            else if (headers.size() == 5)
                widths = new int[]{1, 2, 1, 1, 1};
            else if (headers.size() == 6)
                widths = new int[]{1, 2, 1, 1, 1, 1};
            else
                widths = new int[]{2, 3, 1, 1, 1, 2, 1};

            PdfPTable pdfTable = new PdfPTable(headers.size());
            try {
                pdfTable.setWidths(widths);
            } catch (DocumentException e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            pdfTable.setTotalWidth(PageSize.A4.getWidth() - 40);
            pdfTable.setLockedWidth(true);

            for (int i = 0; i < headers.size(); i++)
                pdfTable.addCell(new MyPhrase(headers.get(i), Font.BOLD + Font.ITALIC, true).getCell());
            pdfTable.setHeaderRows(1);

            double total = 0.0;
            for (int i = 0; i < list.size(); i++) {
                int rowSpan = list.get(i).getItems().size();
                PdfPCell cell = new MyPhrase(ValueFormatter.formatDate(list.get(i).getDate()), Font.PLAIN, false)
                        .getCell();
                cell.setRowspan(rowSpan);
                pdfTable.addCell(cell);

                double amount = 0.0;
                boolean amountAdded = false;
                if (printAmount)
                    for (Stock stock : list.get(i).getItems())
                        amount += (stock.getQuantity() * stock.getPrice());

                for (Stock stock : list.get(i).getItems()) {
                    PdfPCell cellStock = new MyPhrase(stock.getItem().getName(), Font.ITALIC, false).getCell();
                    cellStock.setHorizontalAlignment(Element.ALIGN_LEFT);
                    pdfTable.addCell(cellStock);

                    if (printQuantity)
                        pdfTable.addCell(
                                new MyPhrase(ValueFormatter.formatQuantity(stock.getQuantity()), Font.PLAIN, false)
                                        .getCell());

                    if (printUnit)
                        pdfTable.addCell(new MyPhrase(stock.getItem().getUnit(), Font.PLAIN, false).getCell());

                    if (printPrice)
                        pdfTable.addCell(
                                new MyPhrase(ValueFormatter.formatMoneyNicely(stock.getPrice()), Font.PLAIN, false)
                                        .getCell());

                    if (printStore)
                        pdfTable.addCell(
                                new MyPhrase(ValueFormatter.formatStore(list.get(i).getStore()), Font.PLAIN, false)
                                        .getCell());

                    if (printAmount && !amountAdded) {
                        PdfPCell cellAmount = new MyPhrase(ValueFormatter.formatMoneyNicely(amount), Font.PLAIN, false)
                                .getCell();
                        cellAmount.setRowspan(rowSpan);
                        pdfTable.addCell(cellAmount);
                        amountAdded = true;
                    }

                }

                total += list.get(i).getTotal();
            }

            MyPhrase phrase = new MyPhrase("Total Purchases:", Font.BOLD, false);
            phrase.getCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            phrase.getCell().setColspan(headers.size() - 1);
            phrase.getCell().setBorder(PdfPCell.TOP);
            pdfTable.addCell(phrase.getCell());

            phrase = new MyPhrase(ValueFormatter.formatMoneyNicely(total), Font.BOLD, false);
            phrase.getCell().setBorder(PdfPCell.TOP);
            pdfTable.addCell(phrase.getCell());
            pdfTable.completeRow();
            try {
                document.add(pdfTable);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            document.close();

            if (fullPath != null) {
                File file = new File(fullPath);
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
            }
        }
    }

    public static void printSalesReport1(String title, SortedSet<String> set, double[][] totals) {
        Document document = new Document(PageSize.A4.rotate());
        document.setMargins(30f, 30f, 140f, 70f);
        fullPath = null;
        if (selectDestination(title)) {
            PdfWriter writer = null;
            try {
                writer = PdfWriter.getInstance(document, new FileOutputStream(fullPath));
            } catch (FileNotFoundException | DocumentException e1) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e1);
            }
            writer.setPageEvent(new MyFooter(title, null, null));
            document.open();

            Map<Integer, String> map = new HashMap<Integer, String>();
            map.put(0, "Jan");
            map.put(1, "Feb");
            map.put(2, "Mar");
            map.put(3, "Apr");
            map.put(4, "May");
            map.put(5, "Jun");
            map.put(6, "Jul");
            map.put(7, "Aug");
            map.put(8, "Sep");
            map.put(9, "Oct");
            map.put(10, "Nov");
            map.put(11, "Dec");

            PdfPTable pdfTable = new PdfPTable(map.size() + 2);
            pdfTable.setTotalWidth(PageSize.A4.getHeight() - 40);
            pdfTable.setLockedWidth(true);

            pdfTable.addCell(new MyPhrase("Month / Customer", Font.BOLD + Font.ITALIC, true).getCell());
            for (int i = 0; i < map.size(); i++)
                pdfTable.addCell(new MyPhrase(map.get(i), Font.BOLD + Font.ITALIC, true).getCell());
            pdfTable.addCell(new MyPhrase("Customer Totals", Font.BOLD + Font.ITALIC, true).getCell());
            pdfTable.setHeaderRows(1);

            double[] rowTotals = rowSum(totals);
            double[] colTotals = colSum(totals);
            String[] list = set.toArray(new String[0]);
            for (int i = 0; i < list.length; i++) {
                MyPhrase phrase = new MyPhrase(list[i], Font.ITALIC, false);
                phrase.getCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                pdfTable.addCell(phrase.getCell());
                for (int j = 0; j < 12; j++)
                    pdfTable.addCell(
                            new MyPhrase(ValueFormatter.formatMoneyNicely(totals[i][j]), Font.PLAIN, false).getCell());
                pdfTable.addCell(
                        new MyPhrase(ValueFormatter.formatMoneyNicely(rowTotals[i]), Font.BOLD, false).getCell());
            }

            // last row for column totals
            pdfTable.addCell(new MyPhrase("Month Totals", Font.BOLD + Font.ITALIC, false).getCell());
            for (int i = 0; i < colTotals.length; i++)
                pdfTable.addCell(
                        new MyPhrase(ValueFormatter.formatMoneyNicely(colTotals[i]), Font.BOLD, false).getCell());

            /*
             * perform sum ROW-wise or COLUMN-wise
             */
            double sumColTotals = 0.0;
            for (int i = 0; i < colTotals.length; i++)
                sumColTotals += colTotals[i];
            pdfTable.addCell(new MyPhrase(ValueFormatter.formatMoneyNicely(sumColTotals), Font.BOLD, false).getCell());
            pdfTable.completeRow();
            try {
                document.add(pdfTable);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            document.close();

            if (fullPath != null) {
                File file = new File(fullPath);
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
            }
        }
    }

    public static void printSalesReport2(String title, Date fromDate, Date toDate, List<SaleTransaction> list) {
        Document document = new Document();
        document.setMargins(30f, 30f, 140f, 70f);
        fullPath = null;
        if (selectDestination(title)) {
            PdfWriter writer = null;
            try {
                writer = PdfWriter.getInstance(document, new FileOutputStream(fullPath));
            } catch (FileNotFoundException | DocumentException e1) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e1);
            }
            writer.setPageEvent(new MyFooter(title, fromDate, toDate));
            document.open();

            List<String> headers = Arrays.asList("Date", "Customer", "Items", "Store", "Amount");
            PdfPTable pdfTable = new PdfPTable(headers.size());
            try {
                pdfTable.setWidths(new int[]{1, 1, 2, 1, 1});
            } catch (DocumentException e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            pdfTable.setTotalWidth(PageSize.A4.getWidth() - 40);
            pdfTable.setLockedWidth(true);

            for (int i = 0; i < headers.size(); i++)
                pdfTable.addCell(new MyPhrase(headers.get(i), Font.BOLD + Font.ITALIC, true).getCell());
            pdfTable.setHeaderRows(1);

            double total = 0.0;
            for (int i = 0; i < list.size(); i++) {
                pdfTable.addCell(
                        new MyPhrase(ValueFormatter.formatDate(list.get(i).getDate()), Font.PLAIN, false).getCell());
                pdfTable.addCell(
                        new MyPhrase(ValueFormatter.formatUserObject(list.get(i).getCustomer()), Font.PLAIN, false)
                                .getCell());
                String stockString = "";
                for (Stock stock : list.get(i).getItems()) {
                    stockString += stock.getItem().getName() + " (" + ValueFormatter.formatQuantity(stock.getQuantity())
                            + " " + stock.getItem().getUnit() + "),\n";
                }
                stockString = stockString.substring(0, stockString.length() - 2);
                MyPhrase phrase = new MyPhrase(stockString, Font.PLAIN, false);
                phrase.getCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                pdfTable.addCell(phrase.getCell());
                pdfTable.addCell(
                        new MyPhrase(ValueFormatter.formatStore(list.get(i).getStore()), Font.PLAIN, false).getCell());
                pdfTable.addCell(
                        new MyPhrase(ValueFormatter.formatMoneyNicely(list.get(i).getTotal()), Font.PLAIN, false)
                                .getCell());
                total += list.get(i).getTotal();
            }

            MyPhrase phrase = new MyPhrase("Total Sales:", Font.BOLD, false);
            phrase.getCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            phrase.getCell().setColspan(headers.size() - 1);
            phrase.getCell().setBorder(PdfPCell.TOP);
            pdfTable.addCell(phrase.getCell());

            phrase = new MyPhrase(ValueFormatter.formatMoneyNicely(total), Font.BOLD, false);
            phrase.getCell().setBorder(PdfPCell.TOP);
            pdfTable.addCell(phrase.getCell());
            pdfTable.completeRow();
            try {
                document.add(pdfTable);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            document.close();

            if (fullPath != null) {
                File file = new File(fullPath);
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
            }
        }
    }

    public static void printSalesReport3(String title, Date fromDate, Date toDate, List<SaleTransaction> list,
                                         boolean printQuantity, boolean printUnit, boolean printPrice, boolean printStore, boolean printAmount) {
        Document document = new Document();
        document.setMargins(30f, 30f, 140f, 70f);
        fullPath = null;
        if (selectDestination(title)) {
            PdfWriter writer = null;
            try {
                writer = PdfWriter.getInstance(document, new FileOutputStream(fullPath));
            } catch (FileNotFoundException | DocumentException e1) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e1);
            }
            writer.setPageEvent(new MyFooter(title, fromDate, toDate));
            document.open();

            List<String> headers = new ArrayList<String>();
            headers.add("Date");
            headers.add("Item(s)");
            if (printQuantity)
                headers.add("Quantity");
            if (printUnit)
                headers.add("Unit");
            if (printPrice)
                headers.add("Unit Price");
            if (printStore)
                headers.add("Store");
            if (printAmount)
                headers.add("Amount");

            int[] widths;
            if (headers.size() == 2)
                widths = new int[]{1, 3};
            else if (headers.size() == 3)
                widths = new int[]{1, 2, 1};
            else if (headers.size() == 4)
                widths = new int[]{1, 2, 1, 1};
            else if (headers.size() == 5)
                widths = new int[]{1, 2, 1, 1, 1};
            else if (headers.size() == 6)
                widths = new int[]{1, 2, 1, 1, 1, 1};
            else
                widths = new int[]{2, 3, 1, 1, 1, 2, 1};

            PdfPTable pdfTable = new PdfPTable(headers.size());
            try {
                pdfTable.setWidths(widths);
            } catch (DocumentException e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            pdfTable.setTotalWidth(PageSize.A4.getWidth() - 40);
            pdfTable.setLockedWidth(true);

            for (int i = 0; i < headers.size(); i++)
                pdfTable.addCell(new MyPhrase(headers.get(i), Font.BOLD + Font.ITALIC, true).getCell());
            pdfTable.setHeaderRows(1);

            double total = 0.0;
            for (int i = 0; i < list.size(); i++) {
                int rowSpan = list.get(i).getItems().size();
                PdfPCell cell = new MyPhrase(ValueFormatter.formatDate(list.get(i).getDate()), Font.PLAIN, false)
                        .getCell();
                cell.setRowspan(rowSpan);
                pdfTable.addCell(cell);

                double amount = 0.0;
                boolean amountAdded = false;
                if (printAmount)
                    for (Stock stock : list.get(i).getItems())
                        amount += (stock.getQuantity() * stock.getPrice());

                for (Stock stock : list.get(i).getItems()) {
                    PdfPCell cellStock = new MyPhrase(stock.getItem().getName(), Font.ITALIC, false).getCell();
                    cellStock.setHorizontalAlignment(Element.ALIGN_LEFT);
                    pdfTable.addCell(cellStock);

                    if (printQuantity)
                        pdfTable.addCell(
                                new MyPhrase(ValueFormatter.formatQuantity(stock.getQuantity()), Font.PLAIN, false)
                                        .getCell());

                    if (printUnit)
                        pdfTable.addCell(new MyPhrase(stock.getItem().getUnit(), Font.PLAIN, false).getCell());

                    if (printPrice)
                        pdfTable.addCell(
                                new MyPhrase(ValueFormatter.formatMoneyNicely(stock.getPrice()), Font.PLAIN, false)
                                        .getCell());

                    if (printStore)
                        pdfTable.addCell(
                                new MyPhrase(ValueFormatter.formatStore(list.get(i).getStore()), Font.PLAIN, false)
                                        .getCell());

                    if (printAmount && !amountAdded) {
                        PdfPCell cellAmount = new MyPhrase(ValueFormatter.formatMoneyNicely(amount), Font.PLAIN, false)
                                .getCell();
                        cellAmount.setRowspan(rowSpan);
                        pdfTable.addCell(cellAmount);
                        amountAdded = true;
                    }

                }

                total += list.get(i).getTotal();
            }

            MyPhrase phrase = new MyPhrase("Total Sales:", Font.BOLD, false);
            phrase.getCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            phrase.getCell().setColspan(headers.size() - 1);
            phrase.getCell().setBorder(PdfPCell.TOP);
            pdfTable.addCell(phrase.getCell());

            phrase = new MyPhrase(ValueFormatter.formatMoneyNicely(total), Font.BOLD, false);
            phrase.getCell().setBorder(PdfPCell.TOP);
            pdfTable.addCell(phrase.getCell());
            pdfTable.completeRow();
            try {
                document.add(pdfTable);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            document.close();

            if (fullPath != null) {
                File file = new File(fullPath);
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
            }
        }
    }

    public static void printSalesReport5(String title, List<UserObject> sales, List<Date> startDates,
                                         List<Date> lastDates, List<Double> totalSales, List<Double> totalPaids, List<Double> totalOutstandings) {
        Document document = new Document();
        document.setMargins(30f, 30f, 140f, 70f);
        fullPath = null;
        if (selectDestination(title)) {
            PdfWriter writer = null;
            try {
                writer = PdfWriter.getInstance(document, new FileOutputStream(fullPath));
            } catch (FileNotFoundException | DocumentException e1) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e1);
            }
            writer.setPageEvent(new MyFooter(title, null, null));
            document.open();

            List<String> headers = new ArrayList<String>();
            headers.add("Name");
            headers.add("Address");
            headers.add("Contact");
            if (startDates != null)
                headers.add("Sale Start Date");
            if (lastDates != null)
                headers.add("Last Sale Date");
            if (totalSales != null)
                headers.add("Total Sale");
            if (totalPaids != null)
                headers.add("Total Paid");
            if (totalOutstandings != null)
                headers.add("Outstanding Balance");

            int[] widths;
            if (headers.size() == 3)
                widths = new int[]{1, 1, 1};
            else if (headers.size() == 4)
                widths = new int[]{2, 1, 1, 1};
            else if (headers.size() == 5)
                widths = new int[]{2, 1, 1, 1, 1};
            else if (headers.size() == 6)
                widths = new int[]{2, 1, 1, 1, 1, 1};
            else if (headers.size() == 7)
                widths = new int[]{2, 1, 1, 1, 1, 1, 1};
            else
                widths = new int[]{2, 1, 1, 1, 1, 1, 1, 1};

            // PERSON
            PdfPTable pdfTablePerson = new PdfPTable(headers.size());
            try {
                pdfTablePerson.setWidths(widths);
            } catch (DocumentException e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            pdfTablePerson.setTotalWidth(PageSize.A4.getWidth() - 40);
            pdfTablePerson.setLockedWidth(true);

            MyPhrase phrase = new MyPhrase("INDIVIDUAL", Font.BOLD, true);
            phrase.getCell().setBackgroundColor(Color.DARK_GRAY);
            phrase.getFont().setColor(Color.WHITE);
            phrase.getCell().setBorder(PdfPCell.NO_BORDER);
            phrase.getCell().setColspan(headers.size());
            pdfTablePerson.addCell(phrase.getCell());
            for (int i = 0; i < headers.size(); i++)
                pdfTablePerson.addCell(new MyPhrase(headers.get(i), Font.BOLD + Font.ITALIC, true).getCell());
            pdfTablePerson.setHeaderRows(2);

            // ORGANIZATION
            PdfPTable pdfTableOrganization = new PdfPTable(headers.size());
            try {
                pdfTableOrganization.setWidths(widths);
            } catch (DocumentException e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            pdfTableOrganization.setTotalWidth(PageSize.A4.getWidth() - 40);
            pdfTableOrganization.setLockedWidth(true);

            phrase = new MyPhrase("ORGANIZATION", Font.BOLD, true);
            phrase.getCell().setBackgroundColor(Color.DARK_GRAY);
            phrase.getFont().setColor(Color.WHITE);
            phrase.getCell().setBorder(PdfPCell.NO_BORDER);
            phrase.getCell().setColspan(headers.size());
            pdfTableOrganization.addCell(phrase.getCell());
            for (int i = 0; i < headers.size(); i++)
                pdfTableOrganization.addCell(new MyPhrase(headers.get(i), Font.BOLD + Font.ITALIC, true).getCell());
            pdfTableOrganization.setHeaderRows(2);

            int fontSize = 8;
            if (headers.size() > 4)
                fontSize = 7;

            for (int i = 0; i < sales.size(); i++) {
                UserObject customer = sales.get(i);
                PdfPTable table = null;
                if (customer instanceof Person)
                    table = pdfTablePerson;
                else
                    table = pdfTableOrganization;

                phrase = new MyPhrase(ValueFormatter.formatUserObject(customer), fontSize, Font.ITALIC, false);
                phrase.getCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(phrase.getCell());

                table.addCell(new MyPhrase(customer.getAddress(), fontSize, Font.PLAIN, false).getCell());

                String contact = customer.getPhone();
                if (customer.getEmail() != null)
                    contact += "\n" + customer.getEmail();
                table.addCell(new MyPhrase(contact, fontSize, Font.PLAIN, false).getCell());

                if (startDates != null)
                    table.addCell(
                            new MyPhrase(ValueFormatter.formatDate(startDates.get(i)), fontSize, Font.PLAIN, false)
                                    .getCell());

                if (lastDates != null)
                    table.addCell(new MyPhrase(ValueFormatter.formatDate(lastDates.get(i)), fontSize, Font.PLAIN, false)
                            .getCell());

                if (totalSales != null)
                    table.addCell(new MyPhrase(ValueFormatter.formatMoneyNicely(totalSales.get(i)), fontSize,
                            Font.PLAIN, false).getCell());

                if (totalPaids != null)
                    table.addCell(new MyPhrase(ValueFormatter.formatMoneyNicely(totalPaids.get(i)), fontSize,
                            Font.PLAIN, false).getCell());

                if (totalOutstandings != null)
                    table.addCell(new MyPhrase(ValueFormatter.formatMoneyNicely(totalOutstandings.get(i)), fontSize,
                            Font.PLAIN, false).getCell());
            }

            pdfTablePerson.completeRow();
            pdfTableOrganization.completeRow();
            try {
                document.add(pdfTablePerson);
                document.add(pdfTableOrganization);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            document.close();

            if (fullPath != null) {
                File file = new File(fullPath);
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
            }
        }
    }

//    public static boolean printSalesReport6(String title, SaleTransaction sale){
//        Document document = new Document();
//        document.setMargins(30f, 30f, 140f, 70f);
//        fullPath = null;
//        if (selectDestination(title)) {
//            PdfWriter writer = null;
//            try {
//                writer = PdfWriter.getInstance(document, new FileOutputStream(fullPath));
//            } catch (FileNotFoundException | DocumentException e1) {
//                // TODO Auto-generated catch block
//                LOGGER.Error.log(e1);
//            }
//            writer.setPageEvent(new MyFooter(title, null, null));
//            document.open();
//
//            PdfPTable pdfTable = new PdfPTable(5);
//            pdfTable.setTotalWidth(PageSize.A4.getWidth() - 40);
//            pdfTable.setLockedWidth(true);
//
//            if (sale != null) {
//
//                Paragraph p = new Paragraph(new MyChunk(ValueFormatter.formatBalanceId(transaction.getId(),
//                        Transaction.class), 15, Font.BOLD));
//                PdfPCell cell = new PdfPCell();
//                cell.addElement(p);
//                cell.setColspan(2);
//                cell.setBorder(0);
//                pdfTable.addCell(cell);
//
//                p = new Paragraph();
//                p.setAlignment(Paragraph.ALIGN_RIGHT);
//                MyChunk dateKey = new MyChunk("Date: ", 12,
//                        Font.BOLD);
//                p.add(dateKey);
//                MyChunk dateValue = new MyChunk(ValueFormatter.formatDate(transaction.getDate()), 12, Font.PLAIN);
//                p.add(dateValue);
//
//                cell = new PdfPCell();
//                cell.addElement(p);
//                cell.setColspan(3);
//                cell.setBorder(0);
//                pdfTable.addCell(cell);
//
//                p = new Paragraph();
//                p.setAlignment(Paragraph.ALIGN_RIGHT);
//                MyChunk keys = new MyChunk("Subtotal:\nTax/VAT:", 12, Font.PLAIN);
//                p.add(keys);
//
//                cell = new PdfPCell();
//                cell.addElement(p);
//                cell.setColspan(2);
//                cell.setBorder(0);
//                pdfTable.addCell(cell);
//
//                p = new Paragraph();
//                p.setAlignment(Paragraph.ALIGN_CENTER);
//                MyChunk values =
//                        new MyChunk(ValueFormatter.formatMoneyNicely(transaction.getAmount()) + "\n" + ValueFormatter.formatMoneyNicely(transaction.getTax()), 12, Font.PLAIN);
//                p.add(values);
//
//                cell = new PdfPCell();
//                cell.addElement(p);
//                cell.setBorder(0);
//                pdfTable.addCell(cell);
//
//                p = new Paragraph();
//                p.setAlignment(Paragraph.ALIGN_LEFT);
//                MyChunk aeds =
//                        new MyChunk("AED\nAED", 12,
//                                Font.PLAIN);
//                p.add(aeds);
//
//                cell = new PdfPCell();
//                cell.addElement(p);
//                cell.setColspan(2);
//                cell.setBorder(0);
//                pdfTable.addCell(cell);
//
//                p = new Paragraph();
//                p.setAlignment(Paragraph.ALIGN_RIGHT);
//                MyChunk totalKey = new MyChunk("Total:", 12, Font.BOLD);
//                p.add(totalKey);
//
//                cell = new PdfPCell();
//                cell.addElement(p);
//                cell.setColspan(2);
//                cell.setBorder(0);
//                pdfTable.addCell(cell);
//
//                p = new Paragraph();
//                p.setAlignment(Paragraph.ALIGN_CENTER);
//                MyChunk totalValue =
//                        new MyChunk(ValueFormatter.formatMoneyNicely(total), 12, Font.BOLD);
//                p.add(totalValue);
//
//                cell = new PdfPCell();
//                cell.addElement(p);
//                cell.setBorder(0);
//                pdfTable.addCell(cell);
//
//                p = new Paragraph();
//                p.setAlignment(Paragraph.ALIGN_LEFT);
//                MyChunk aed =
//                        new MyChunk("AED", 12,
//                                Font.BOLD);
//                p.add(aed);
//
//                cell = new PdfPCell();
//                cell.addElement(p);
//                cell.setColspan(2);
//                cell.setBorder(0);
//                pdfTable.addCell(cell);
//
//                MyPhrase inWordsKey = new MyPhrase("In Words:", 12, Font.BOLD, false);
//                cell = inWordsKey.getCell();
//                cell.setBorder(0);
//                pdfTable.addCell(cell);
//
//                MyPhrase inWordsValue = new MyPhrase(ValueFormatter.convertNumberToWords(total), 12, Font.ITALIC, false);
//                cell = inWordsValue.getCell();
//                cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
//                cell.setColspan(3);
//                cell.setBorder(0);
//                pdfTable.addCell(cell);
//            }
//
//            PdfPTable pdfTableUser = new PdfPTable(1);
//            pdfTableUser.setTotalWidth(PageSize.A4.getWidth() - 40);
//            pdfTableUser.setLockedWidth(true);
//
//            if (printUserDetails) {
//                Paragraph p = new Paragraph();
//                p.setAlignment(Paragraph.ALIGN_CENTER);
//                MyChunk header = new MyChunk("User Details", 12, Font.BOLD);
//                p.add(header);
//
//                PdfPCell cell = new PdfPCell();
//                cell.addElement(p);
//
//                p = new Paragraph();
//                String fullName = "";
//                String companyName = "";
//                if (transaction.getUserObject() instanceof Person){
//                    Person person = (Person) transaction.getUserObject();
//                    fullName = person.getFirstName() + " " + person.getLastName();
//                }
//                else {
//                    Organization organization = (Organization) transaction.getUserObject();
//                    fullName = organization.getContactName();
//                    companyName = organization.getName();
//                }
//
//                MyChunk name = new MyChunk("Name: ", fullName);
//                name.updateFontSize(12);
//                p.add(name.getKey());
//                p.add(name.getValue());
//
//                MyChunk company = new MyChunk("\nCompany: ", companyName);
//                company.updateFontSize(12);
//                p.add(company.getKey());
//                p.add(company.getValue());
//
//                MyChunk email = new MyChunk("\nEmail: ", transaction.getUserObject().getEmail());
//                email.updateFontSize(12);
//                p.add(email.getKey());
//                p.add(email.getValue());
//
//                MyChunk phone = new MyChunk("\nPhone: ", transaction.getUserObject().getPhone());
//                phone.updateFontSize(12);
//                p.add(phone.getKey());
//                p.add(phone.getValue());
//
//                cell.addElement(p);
//                cell.setPaddingBottom(10);
//                pdfTableUser.addCell(cell);
//            }
//
//            PdfPTable pdfTableInventory = new PdfPTable(1);
//            pdfTableInventory.setTotalWidth(PageSize.A4.getWidth() - 40);
//            pdfTableInventory.setLockedWidth(true);
//
//            if (printInventoryDetails) {
//                Paragraph p = new Paragraph();
//                p.setAlignment(Paragraph.ALIGN_CENTER);
//                MyChunk header = new MyChunk("Inventory Details", 12, Font.BOLD);
//                p.add(header);
//
//                Inventory inventory = transaction.getInventory();
//                PdfPCell cell = new PdfPCell();
//                cell.addElement(p);
//
//                p = new Paragraph();
//                String inventoryType = "Personal";
//                if (inventory.getAccountNo() != null && !inventory.getAccountNo().isEmpty())
//                    inventoryType = "Bank";
//                MyChunk type = new MyChunk("Type: ", inventoryType);
//                type.updateFontSize(12);
//                p.add(type.getKey());
//                p.add(type.getValue());
//
//                MyChunk name = new MyChunk("\nName: ", inventory.getName());
//                name.updateFontSize(12);
//                p.add(name.getKey());
//                p.add(name.getValue());
//
//                MyChunk accountNo = new MyChunk("\nAccount#: ", inventory.getAccountNo());
//                accountNo.updateFontSize(12);
//                p.add(accountNo.getKey());
//                p.add(accountNo.getValue());
//
//                cell.addElement(p);
//                cell.setPaddingBottom(10);
//                pdfTableInventory.addCell(cell);
//            }
//
//
//            try {
//                document.add(pdfTable);
//                if (printUserDetails) {
//                    document.add(new Paragraph("\n"));
//                    document.add(pdfTableUser);
//                }
//                if (printInventoryDetails) {
//                    document.add(new Paragraph("\n"));
//                    document.add(pdfTableInventory);
//                }
//            } catch (Exception e) {
//                // TODO Auto-generated catch block
//                LOGGER.Error.log(e);
//            }
//            document.close();
//
//            if (fullPath != null) {
//                File file = new File(fullPath);
//                try {
//                    Desktop.getDesktop().open(file);
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    LOGGER.Error.log(e);
//                }
//            }
//        }
//        return true;
//    }

    public static void printUserReport1(String title, List<Person> persons, List<Organization> organizations,
                                        String category, boolean printBalance) {
        Document document = new Document();
        document.setMargins(30f, 30f, 140f, 70f);
        fullPath = null;
        if (selectDestination(title)) {
            PdfWriter writer = null;
            try {
                writer = PdfWriter.getInstance(document, new FileOutputStream(fullPath));
            } catch (FileNotFoundException | DocumentException e1) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e1);
            }
            writer.setPageEvent(new MyFooter(title, null, null));
            document.open();

            PdfPTable pdfTable = new PdfPTable(2);
            pdfTable.setWidthPercentage(50);
            pdfTable.setTotalWidth(PageSize.A4.getWidth() - 40);
            pdfTable.setLockedWidth(true);

            if (persons != null)
                for (Person person : persons) {
                    Paragraph p = new Paragraph();

                    if (category.equals("ANY")) {
                        String strings = "";
                        for (String string : person.getCategories())
                            strings += string + " | ";
                        strings = strings.substring(0, strings.length() - 3) + "\n";

                        MyChunk categories = new MyChunk(strings, 12, Font.ITALIC + Font.BOLD);
                        p.add(categories);
                    }

                    MyChunk firstName = new MyChunk("First name: ", person.getFirstName());
                    p.add(firstName.getKey());
                    p.add(firstName.getValue());

                    MyChunk lastName = new MyChunk("\nLast name: ", person.getLastName());
                    p.add(lastName.getKey());
                    p.add(lastName.getValue());

                    MyChunk nationality = new MyChunk("\nNationality: ", person.getNationality());
                    p.add(nationality.getKey());
                    p.add(nationality.getValue());

                    MyChunk location = new MyChunk("\nLocation: ", person.getLocation());
                    p.add(location.getKey());
                    p.add(location.getValue());

                    MyChunk address = new MyChunk("\nAddress: ", person.getAddress());
                    p.add(address.getKey());
                    p.add(address.getValue());

                    MyChunk phone = new MyChunk("\nPhone: ", person.getPhone());
                    p.add(phone.getKey());
                    p.add(phone.getValue());

                    MyChunk email = new MyChunk("\nEmail: ", person.getEmail());
                    p.add(email.getKey());
                    p.add(email.getValue());

                    if (printBalance) {
                        MyChunk balance = new MyChunk("\nBalance: ",
                                ValueFormatter.formatMoneyNicely(person.getBalance()));
                        p.add(balance.getKey());
                        p.add(balance.getValue());
                    }

                    PdfPCell cell = new PdfPCell();
                    cell.addElement(p);
                    cell.setPaddingBottom(10);
                    pdfTable.addCell(cell);
                }

            if (organizations != null)
                for (Organization organization : organizations) {
                    Paragraph p = new Paragraph();

                    if (category.equals("ANY")) {
                        String strings = "";
                        for (String string : organization.getCategories())
                            strings += string + " | ";
                        strings = strings.substring(0, strings.length() - 3) + "\n";

                        MyChunk categories = new MyChunk(strings, 12, Font.ITALIC + Font.BOLD);
                        p.add(categories);
                    }

                    MyChunk name = new MyChunk("Organization: ", organization.getName());
                    p.add(name.getKey());
                    p.add(name.getValue());

                    MyChunk contactName = new MyChunk("\nContact Person: ", organization.getContactName());
                    p.add(contactName.getKey());
                    p.add(contactName.getValue());

                    MyChunk location = new MyChunk("\nLocation: ", organization.getLocation());
                    p.add(location.getKey());
                    p.add(location.getValue());

                    MyChunk address = new MyChunk("\nAddress: ", organization.getAddress());
                    p.add(address.getKey());
                    p.add(address.getValue());

                    MyChunk phone = new MyChunk("\nPhone: ", organization.getPhone());
                    p.add(phone.getKey());
                    p.add(phone.getValue());

                    MyChunk email = new MyChunk("\nEmail: ", organization.getEmail());
                    p.add(email.getKey());
                    p.add(email.getValue());

                    if (printBalance) {
                        MyChunk balance = new MyChunk("\nBalance: ",
                                ValueFormatter.formatMoneyNicely(organization.getBalance()));
                        p.add(balance.getKey());
                        p.add(balance.getValue());
                    }

                    PdfPCell cell = new PdfPCell();
                    cell.addElement(p);
                    cell.setPaddingBottom(10);
                    pdfTable.addCell(cell);
                }
            pdfTable.completeRow();

            try {
                document.add(pdfTable);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            document.close();

            if (fullPath != null) {
                File file = new File(fullPath);
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
            }
        }
    }

    public static boolean printUserReport2(String title, List<Person> persons, List<Organization> organizations,
                                           String category, boolean printBalance) {
        Document document = new Document();
        document.setMargins(30f, 30f, 140f, 70f);
        fullPath = null;
        if (selectDestination(title)) {
            PdfWriter writer = null;
            try {
                writer = PdfWriter.getInstance(document, new FileOutputStream(fullPath));
            } catch (FileNotFoundException | DocumentException e1) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e1);
            }
            writer.setPageEvent(new MyFooter(title, null, null));
            document.open();

            List<String> headers = new ArrayList<String>();
            headers.add("User");
            if (category.equals("ANY"))
                headers.add("Category");
            headers.add("Location");
            headers.add("Phone");
            headers.add("Email");
            if (printBalance)
                headers.add("Balance");

            int[] widths;
            if (headers.size() == 4)
                widths = new int[]{2, 1, 1, 1};
            else if (headers.size() == 5)
                widths = new int[]{2, 1, 1, 1, 1};
            else
                widths = new int[]{2, 1, 1, 1, 1, 1};

            // TABLE OF PERSONS
            PdfPTable pdfTablePerson = null;
            if (persons != null) {
                pdfTablePerson = new PdfPTable(headers.size());
                try {
                    pdfTablePerson.setWidths(widths);
                } catch (DocumentException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
                pdfTablePerson.setTotalWidth(PageSize.A4.getWidth() - 40);
                pdfTablePerson.setLockedWidth(true);

                MyPhrase phrase = new MyPhrase("PERSON", Font.BOLD, true);
                phrase.getCell().setColspan(headers.size());
                phrase.getCell().setBorder(PdfPCell.NO_BORDER);
                phrase.getCell().setBackgroundColor(Color.DARK_GRAY);
                phrase.getFont().setColor(Color.WHITE);
                pdfTablePerson.addCell(phrase.getCell());

                for (int i = 0; i < headers.size(); i++)
                    pdfTablePerson.addCell(new MyPhrase(headers.get(i), Font.BOLD + Font.ITALIC, true).getCell());
                pdfTablePerson.setHeaderRows(2);

                for (Person person : persons) {
                    pdfTablePerson.addCell(new MyPhrase(ValueFormatter.formatUserObject(person), Font.PLAIN).getCell());
                    if (category.equals("ANY")) {
                        String strings = "";
                        for (String string : person.getCategories())
                            strings += string + " | ";
                        strings = strings.substring(0, strings.length() - 3);
                        pdfTablePerson.addCell(new MyPhrase(strings, Font.ITALIC).getCell());
                    }
                    pdfTablePerson.addCell(new MyPhrase(person.getLocation(), Font.PLAIN).getCell());
                    pdfTablePerson.addCell(new MyPhrase(person.getPhone(), Font.PLAIN).getCell());
                    pdfTablePerson.addCell(new MyPhrase(person.getEmail(), Font.PLAIN).getCell());
                    if (printBalance)
                        pdfTablePerson
                                .addCell(new MyPhrase(ValueFormatter.formatMoneyNicely(person.getBalance()), Font.PLAIN)
                                        .getCell());
                }
            }

            // TABLE OF ORGANIZATIONS
            PdfPTable pdfTableOrganization = null;
            if (organizations != null) {
                pdfTableOrganization = new PdfPTable(headers.size());
                try {
                    pdfTableOrganization.setWidths(widths);
                } catch (DocumentException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
                pdfTableOrganization.setTotalWidth(PageSize.A4.getWidth() - 40);
                pdfTableOrganization.setLockedWidth(true);

                MyPhrase phrase = new MyPhrase("ORGANIZATION", Font.BOLD, true);
                phrase.getCell().setColspan(headers.size());
                phrase.getCell().setBorder(PdfPCell.NO_BORDER);
                phrase.getCell().setBackgroundColor(Color.DARK_GRAY);
                phrase.getFont().setColor(Color.WHITE);
                pdfTableOrganization.addCell(phrase.getCell());

                for (int i = 0; i < headers.size(); i++)
                    pdfTableOrganization.addCell(new MyPhrase(headers.get(i), Font.BOLD + Font.ITALIC, true).getCell());
                pdfTableOrganization.setHeaderRows(2);

                for (Organization organization : organizations) {
                    pdfTableOrganization
                            .addCell(new MyPhrase(ValueFormatter.formatUserObject(organization), Font.PLAIN).getCell());
                    if (category.equals("ANY")) {
                        String strings = "";
                        for (String string : organization.getCategories())
                            strings += string + " | ";
                        strings = strings.substring(0, strings.length() - 3);
                        pdfTableOrganization.addCell(new MyPhrase(strings, Font.ITALIC).getCell());
                    }
                    pdfTableOrganization.addCell(new MyPhrase(organization.getLocation(), Font.PLAIN).getCell());
                    pdfTableOrganization.addCell(new MyPhrase(organization.getPhone(), Font.PLAIN).getCell());
                    pdfTableOrganization.addCell(new MyPhrase(organization.getEmail(), Font.PLAIN).getCell());
                    if (printBalance)
                        pdfTableOrganization.addCell(
                                new MyPhrase(ValueFormatter.formatMoneyNicely(organization.getBalance()), Font.PLAIN)
                                        .getCell());
                }
            }

            try {
                if (pdfTablePerson != null)
                    document.add(pdfTablePerson);
                if (pdfTableOrganization != null)
                    document.add(pdfTableOrganization);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            document.close();

            if (fullPath != null) {
                File file = new File(fullPath);
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
            }
            return true;
        } else {
            new MyOptionPane("You cancelled the operation.", MyOptionPane.INFORMATION_MESSAGE);
            return false;
        }
    }

    public static void printItemReport1(String title, List<Product> products, List<Material> materials,
                                        List<Service> services) {
        Document document = new Document();
        document.setMargins(30f, 30f, 140f, 70f);
        fullPath = null;
        if (selectDestination(title)) {
            PdfWriter writer = null;
            try {
                writer = PdfWriter.getInstance(document, new FileOutputStream(fullPath));
            } catch (FileNotFoundException | DocumentException e1) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e1);
            }
            writer.setPageEvent(new MyFooter(title, null, null));
            document.open();

            PdfPTable pdfTable = new PdfPTable(2);
            pdfTable.setWidthPercentage(50);
            pdfTable.setTotalWidth(PageSize.A4.getWidth() - 40);
            pdfTable.setLockedWidth(true);

            if (products != null)
                for (Product product : products) {
                    Paragraph p = new Paragraph();

                    MyChunk name = new MyChunk(product.getName(), 12, Font.ITALIC + Font.BOLD);
                    p.add(name);

                    MyChunk id = new MyChunk("\nItem ID: ", ValueFormatter.formatItem(product));
                    p.add(id.getKey());
                    p.add(id.getValue());

                    MyChunk type = new MyChunk("\nType: ", ItemType.PRODUCT.toString());
                    p.add(type.getKey());
                    p.add(type.getValue());

                    MyChunk unit = new MyChunk("\nUnit: ", product.getUnit());
                    p.add(unit.getKey());
                    p.add(unit.getValue());

                    MyChunk price = new MyChunk("\nPrice (default): ",
                            ValueFormatter.formatMoneyNicely(product.getPrice()));
                    p.add(price.getKey());
                    p.add(price.getValue());

                    String barcodeString = "N/A";
                    if (product.getBarcode() != null && !product.getBarcode().isEmpty())
                        barcodeString = product.getBarcode();
                    MyChunk barcode = new MyChunk("\nBarcode: ", barcodeString);
                    p.add(barcode.getKey());
                    p.add(barcode.getValue());

                    String descriptionString = "N/A";
                    if (product.getDetails() != null && !product.getDetails().isEmpty())
                        descriptionString = product.getDetails();
                    MyChunk description = new MyChunk("\nDescription: ", descriptionString);
                    p.add(description.getKey());
                    p.add(description.getValue());

                    PdfPCell cell = new PdfPCell();
                    cell.addElement(p);
                    cell.setPaddingBottom(10);
                    pdfTable.addCell(cell);
                }

            if (materials != null)
                for (Material material : materials) {
                    Paragraph p = new Paragraph();

                    MyChunk name = new MyChunk(material.getName(), 12, Font.ITALIC + Font.BOLD);
                    p.add(name);

                    MyChunk id = new MyChunk("\nItem ID: ", ValueFormatter.formatItem(material));
                    p.add(id.getKey());
                    p.add(id.getValue());

                    MyChunk type = new MyChunk("\nType: ", ItemType.MATERIAL.toString());
                    p.add(type.getKey());
                    p.add(type.getValue());

                    MyChunk unit = new MyChunk("\nUnit: ", material.getUnit());
                    p.add(unit.getKey());
                    p.add(unit.getValue());

                    MyChunk price = new MyChunk("\nPrice (default): ",
                            ValueFormatter.formatMoneyNicely(material.getPrice()));
                    p.add(price.getKey());
                    p.add(price.getValue());

                    String descriptionString = "N/A";
                    if (material.getDetails() != null && !material.getDetails().isEmpty())
                        descriptionString = material.getDetails();
                    MyChunk description = new MyChunk("\nDescription: ", descriptionString);
                    p.add(description.getKey());
                    p.add(description.getValue());

                    PdfPCell cell = new PdfPCell();
                    cell.addElement(p);
                    cell.setPaddingBottom(10);
                    pdfTable.addCell(cell);
                }

            if (services != null)
                for (Service service : services) {
                    Paragraph p = new Paragraph();

                    MyChunk name = new MyChunk(service.getName(), 12, Font.ITALIC + Font.BOLD);
                    p.add(name);

                    MyChunk id = new MyChunk("\nItem ID: ", ValueFormatter.formatItem(service));
                    p.add(id.getKey());
                    p.add(id.getValue());

                    MyChunk type = new MyChunk("\nType: ", ItemType.SERVICE.toString());
                    p.add(type.getKey());
                    p.add(type.getValue());

                    MyChunk price = new MyChunk("\nPrice (default): ",
                            ValueFormatter.formatMoneyNicely(service.getPrice()));
                    p.add(price.getKey());
                    p.add(price.getValue());

                    String descriptionString = "N/A";
                    if (service.getDetails() != null && !service.getDetails().isEmpty())
                        descriptionString = service.getDetails();
                    MyChunk description = new MyChunk("\nDescription: ", descriptionString);
                    p.add(description.getKey());
                    p.add(description.getValue());

                    PdfPCell cell = new PdfPCell();
                    cell.addElement(p);
                    cell.setPaddingBottom(10);
                    pdfTable.addCell(cell);
                }
            pdfTable.completeRow();

            try {
                document.add(pdfTable);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            document.close();

            if (fullPath != null) {
                File file = new File(fullPath);
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
            }
        }
    }

    public static boolean printItemReport2(String title, List<Product> products, List<Material> materials,
                                           List<Service> services, boolean printPrice) {
        Document document = new Document();
        document.setMargins(30f, 30f, 140f, 70f);
        fullPath = null;
        if (selectDestination(title)) {
            PdfWriter writer = null;
            try {
                writer = PdfWriter.getInstance(document, new FileOutputStream(fullPath));
            } catch (FileNotFoundException | DocumentException e1) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e1);
            }
            writer.setPageEvent(new MyFooter(title, null, null));
            document.open();

            List<String> headers = null;
            int[] widths;
            if (printPrice) {
                headers = Arrays.asList("Item ID", "Name", "Unit", "Price");
                widths = new int[]{1, 2, 1, 1};
            } else {
                headers = Arrays.asList("Item ID", "Name", "Unit");
                widths = new int[]{1, 2, 1};
            }

            // TABLE OF PRODUCTS
            PdfPTable pdfTableProduct = null;
            if (products != null) {
                pdfTableProduct = new PdfPTable(headers.size());
                try {
                    pdfTableProduct.setWidths(widths);
                } catch (DocumentException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
                pdfTableProduct.setTotalWidth(PageSize.A4.getWidth() - 40);
                pdfTableProduct.setLockedWidth(true);

                MyPhrase phrase = new MyPhrase("PRODUCT", Font.BOLD, true);
                phrase.getCell().setColspan(headers.size());
                phrase.getCell().setBorder(PdfPCell.NO_BORDER);
                phrase.getCell().setBackgroundColor(Color.DARK_GRAY);
                phrase.getFont().setColor(Color.WHITE);
                pdfTableProduct.addCell(phrase.getCell());

                for (int i = 0; i < headers.size(); i++)
                    pdfTableProduct.addCell(new MyPhrase(headers.get(i), Font.BOLD + Font.ITALIC, true).getCell());
                pdfTableProduct.setHeaderRows(2);

                for (Product product : products) {
                    pdfTableProduct
                            .addCell(new MyPhraseForStock(ValueFormatter.formatItem(product), Font.PLAIN).getCell());
                    pdfTableProduct.addCell(new MyPhraseForStock(product.getName(), Font.ITALIC).getCell());
                    pdfTableProduct.addCell(new MyPhraseForStock(product.getUnit(), Font.PLAIN).getCell());
                    if (printPrice)
                        pdfTableProduct.addCell(
                                new MyPhraseForStock(ValueFormatter.formatMoneyNicely(product.getPrice()), Font.PLAIN)
                                        .getCell());
                }
            }

            // TABLE OF MATERIALS
            PdfPTable pdfTableMaterial = null;
            if (materials != null) {
                pdfTableMaterial = new PdfPTable(headers.size());
                try {
                    pdfTableMaterial.setWidths(widths);
                } catch (DocumentException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
                pdfTableMaterial.setTotalWidth(PageSize.A4.getWidth() - 40);
                pdfTableMaterial.setLockedWidth(true);

                MyPhrase phrase = new MyPhrase("MATERIAL", Font.BOLD, true);
                phrase.getCell().setColspan(headers.size());
                phrase.getCell().setBorder(PdfPCell.NO_BORDER);
                phrase.getCell().setBackgroundColor(Color.DARK_GRAY);
                phrase.getFont().setColor(Color.WHITE);
                pdfTableMaterial.addCell(phrase.getCell());

                for (int i = 0; i < headers.size(); i++)
                    pdfTableMaterial.addCell(new MyPhrase(headers.get(i), Font.BOLD + Font.ITALIC, true).getCell());
                pdfTableMaterial.setHeaderRows(2);

                for (Material product : materials) {
                    pdfTableMaterial
                            .addCell(new MyPhraseForStock(ValueFormatter.formatItem(product), Font.PLAIN).getCell());
                    pdfTableMaterial.addCell(new MyPhraseForStock(product.getName(), Font.ITALIC).getCell());
                    pdfTableMaterial.addCell(new MyPhraseForStock(product.getUnit(), Font.PLAIN).getCell());
                    if (printPrice)
                        pdfTableMaterial.addCell(
                                new MyPhraseForStock(ValueFormatter.formatMoneyNicely(product.getPrice()), Font.PLAIN)
                                        .getCell());
                }
            }

            // TABLE OF SERVICES
            PdfPTable pdfTableService = null;
            if (services != null) {
                pdfTableService = new PdfPTable(headers.size());
                try {
                    pdfTableService.setWidths(widths);
                } catch (DocumentException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
                pdfTableService.setTotalWidth(PageSize.A4.getWidth() - 40);
                pdfTableService.setLockedWidth(true);

                MyPhrase phrase = new MyPhrase("SERVICE", Font.BOLD, true);
                phrase.getCell().setColspan(headers.size());
                phrase.getCell().setBorder(PdfPCell.NO_BORDER);
                phrase.getCell().setBackgroundColor(Color.DARK_GRAY);
                phrase.getFont().setColor(Color.WHITE);
                pdfTableService.addCell(phrase.getCell());

                for (int i = 0; i < headers.size(); i++)
                    pdfTableService.addCell(new MyPhrase(headers.get(i), Font.BOLD + Font.ITALIC, true).getCell());
                pdfTableService.setHeaderRows(2);

                for (Service service : services) {
                    pdfTableService
                            .addCell(new MyPhraseForStock(ValueFormatter.formatItem(service), Font.PLAIN).getCell());
                    pdfTableService.addCell(new MyPhraseForStock(service.getName(), Font.ITALIC).getCell());
                    pdfTableService.addCell(new MyPhraseForStock(service.getUnit(), Font.PLAIN).getCell());
                    if (printPrice)
                        pdfTableService.addCell(
                                new MyPhraseForStock(ValueFormatter.formatMoneyNicely(service.getPrice()), Font.PLAIN)
                                        .getCell());
                }
            }
            try {
                if (pdfTableProduct != null)
                    document.add(pdfTableProduct);
                if (pdfTableMaterial != null)
                    document.add(pdfTableMaterial);
                if (pdfTableService != null)
                    document.add(pdfTableService);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                LOGGER.Error.log(e);
            }
            document.close();

            if (fullPath != null) {
                File file = new File(fullPath);
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    LOGGER.Error.log(e);
                }
            }
            return true;
        } else {
            new MyOptionPane("You cancelled the operation.", MyOptionPane.INFORMATION_MESSAGE);
            return false;
        }
    }
}

class MyFooter extends PdfPageEventHelper {
    com.lowagie.text.Font hfont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 20, Font.BOLD);
    com.lowagie.text.Font ffont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8, Font.PLAIN);
    com.lowagie.text.Font dfont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, Font.PLAIN);
    private String title;
    private Date fromDate;
    private Date toDate;
    private PdfTemplate total;

    public MyFooter(String title, Date fromDate, Date toDate) {
        this.title = title;
        this.fromDate = fromDate;
        this.toDate = toDate;

        if (this.title.length() >= 15) {
            hfont.setSize(12);
            dfont.setSize(8);
        }
    }

    public void onOpenDocument(PdfWriter writer, Document document) {
        total = writer.getDirectContent().createTemplate(30, 12);
        PdfContentByte cb = writer.getDirectContent();
        Phrase header = new Phrase(title, hfont);
        Phrase date = new Phrase("Printed: " + ValueFormatter.formatDate(new Date()), dfont);
        String fromDateString = "N/A";
        if (fromDate != null)
            fromDateString = ValueFormatter.formatDate(fromDate);
        String toDateString = "N/A";
        if (toDate != null)
            toDateString = ValueFormatter.formatDate(toDate);
        Phrase dates = new Phrase("From: " + fromDateString + "   To: " + toDateString, dfont);

        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, header,
                (document.right() - document.left()) / 2 + document.leftMargin(), document.top() + 10, 0);
        ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, date, (document.right()), document.top() + 10, 0);
        if (fromDate != null || toDate != null)
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, dates, (document.left()), document.top() + 10, 0);
    }

    public void onEndPage(PdfWriter writer, Document document) {
        PdfPTable table = new PdfPTable(2);
        try {
            table.getDefaultCell().setFixedHeight(10);
            PdfPCell cell = new PdfPCell();
            cell.setBorder(PdfCell.NO_BORDER);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setPhrase(new Phrase(String.format("Page %d of", writer.getPageNumber()), ffont));
            table.addCell(cell);

            cell = new PdfPCell(Image.getInstance(total));
            cell.setBorder(PdfCell.NO_BORDER);
            table.addCell(cell);
            table.setTotalWidth(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin());
            table.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin() - 15,
                    writer.getDirectContent());
        } catch (DocumentException de) {
            throw new ExceptionConverter(de);
        }
    }

    public void onCloseDocument(PdfWriter writer, Document document) {
        ColumnText.showTextAligned(total, Element.ALIGN_LEFT,
                new Phrase(String.valueOf(writer.getPageNumber() - 1), ffont), 2, 2, 0);
    }
}

class MyChunk extends Chunk {

    private static final int DEFAULT_FONT_SIZE = 8;
    private Chunk key;
    private Chunk value;

    public MyChunk(String string, int fontSize, int FONT_TYPE) {
        super(string);
        setFont(FontFactory.getFont(FontFactory.HELVETICA, fontSize, FONT_TYPE, new Color(0, 0, 0)));
    }

    public MyChunk(String string, int FONT_TYPE) {
        this(string, DEFAULT_FONT_SIZE, FONT_TYPE);
    }

    public MyChunk(String key, String value) {
        this.key = new Chunk(key);
        this.key.setFont(FontFactory.getFont(FontFactory.HELVETICA, DEFAULT_FONT_SIZE, Font.BOLD, new Color(0, 0, 0)));
        this.value = new Chunk(value);
        this.value
                .setFont(FontFactory.getFont(FontFactory.HELVETICA, DEFAULT_FONT_SIZE, Font.PLAIN, new Color(0, 0, 0)));
    }

    public void updateFontSize(int size){
        if (key != null)
            key.setFont(FontFactory.getFont(FontFactory.HELVETICA, size, Font.BOLD, new Color(0, 0, 0)));
        if (value != null)
            value.setFont(FontFactory.getFont(FontFactory.HELVETICA, size, Font.PLAIN, new Color(0, 0, 0)));
    }

    public Chunk getKey() {
        return key;
    }

    public Chunk getValue() {
        return value;
    }
}

class MyPhrase extends Phrase {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private PdfPCell cell;

    public MyPhrase(String text, int FONT_TYPE, boolean isHeader) {
        this(text, 8, FONT_TYPE, isHeader);
    }

    public MyPhrase(String text, int FONT_TYPE) {
        this(text, 8, FONT_TYPE, false);
    }

    public MyPhrase(String text, int fontSize, int FONT_TYPE, boolean isHeader) {
        super(text, FontFactory.getFont(FontFactory.HELVETICA, fontSize, FONT_TYPE, new Color(0, 0, 0)));
        cell = new PdfPCell(this);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setNoWrap(false);
        if (!isHeader)
            cell.setMinimumHeight(40f);
    }

    public PdfPCell getCell() {
        return cell;
    }
}

class MyPhraseForStock extends Phrase {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private PdfPCell cell;

    public MyPhraseForStock(String text, int FONT_TYPE) {
        super(text, FontFactory.getFont(FontFactory.HELVETICA, 8, FONT_TYPE, new Color(0, 0, 0)));
        cell = new PdfPCell(this);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setNoWrap(false);
        cell.setMinimumHeight(20f);
    }

    public PdfPCell getCell() {
        return cell;
    }
}
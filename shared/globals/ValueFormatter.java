package globals;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import cores.DeliveryTransaction;
import cores.ITransaction;
import cores.Inventory;
import cores.Item;
import cores.Organization;
import cores.Person;
import cores.PurchaseTransaction;
import cores.SaleTransaction;
import cores.Stock;
import cores.Store;
import cores.StoreInventory;
import cores.UserObject;
import databases.Database;
import databases.Inventories;
import databases.Items;
import databases.Stores;
import helpers.NumberWordConverter;

public class ValueFormatter {

    public static ITransaction parseBalanceId(String balanceId) {
        String idTokens[] = balanceId.split("-");
        String type = idTokens[0];
        int no = Integer.parseInt(idTokens[1]);
        Database<?> db = null;
        switch (type) {
            case "TR":
                db = DatabaseFacade.getDatabase("Transactions");
                break;
            case "IT":
                db = DatabaseFacade.getDatabase("IntraTransfers");
                break;
            case "ST":
                db = DatabaseFacade.getDatabase("SaleTransactions");
                break;
            case "PT":
                db = DatabaseFacade.getDatabase("PurchaseTransactions");
                break;
            case "DT":
                db = DatabaseFacade.getDatabase("DeliveryTransactions");
                break;
            case "PR":
                db = DatabaseFacade.getDatabase("Productions");
                break;
            case "RA":
                db = DatabaseFacade.getDatabase("RecordAdjustments");
                break;
        }
        return (ITransaction) db.get(db.find(no));
    }

    public static <T> String formatBalanceId(int value, Class<T> type) {
        String idType = null;
        switch (type.getSimpleName()) {
            case "Transaction":
                idType = "TR";
                break;
            case "IntraTransfer":
                idType = "IT";
                break;
            case "SaleTransaction":
                idType = "ST";
                break;
            case "PurchaseTransaction":
                idType = "PT";
                break;
            case "DeliveryTransaction":
                idType = "DT";
                break;
            case "RecordAdjustment":
                idType = "RA";
                break;
            case "Production":
                idType = "PR";
                break;
        }
        return idType + "-" + String.format("%04d", value);
    }

    public static String formatTextSafely(String value) {
        if (!value.isEmpty()) {
            value = value.trim().replaceAll("\n", " ");
            return value.replaceAll(",", ";");
        } else
            return value;
    }

    public static double parseQuantity(String value) {
        DecimalFormat df = new DecimalFormat("##,###,###,##0.######");
        try {
            return df.parse(value).doubleValue();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            LOGGER.Error.log(e);
            return 0;
        }
    }

    public static String formatQuantity(double value) {
        DecimalFormat df = new DecimalFormat("##,###,###,##0.######");
        return df.format(value);
    }

    public static double parseMoney(String value) {
        DecimalFormat df = new DecimalFormat("##,###,###,##0.00####");
        try {
            return df.parse(value).doubleValue();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            LOGGER.Error.log(e);
            return 0;
        }
    }

    public static String formatMoney(double value) {
        DecimalFormat df = new DecimalFormat("##,###,###,##0.00####");
        return df.format(value);
    }

    public static String formatMoneyNicely(double value) {
        DecimalFormat df = new DecimalFormat("##,###,###,##0.00");
        return df.format(value);
    }

    public static String formatMoneySafely(double value) {
        DecimalFormat df = new DecimalFormat("##########0.00####");
        return df.format(value);
    }

    public static Date parseDate(String value) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {
            return sdf.parse(value);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            LOGGER.Error.log(e);
            return null;
        }
    }

    public static String formatDate(Date value) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        return sdf.format(value);
    }

    public static double parseRate(String value) {
        DecimalFormat df = new DecimalFormat("#,##0.00####");
        try {
            return df.parse(value).floatValue();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            LOGGER.Error.log(e);
            return 0;
        }
    }

    public static String formatRate(double value) {
        DecimalFormat df = new DecimalFormat("#,##0.00####");
        return df.format(value);
    }

    public static Inventory parseInventory(String value) {
        Inventories db = (Inventories) DatabaseFacade.getDatabase("Inventories");
        if (value.contains("/"))
            return (StoreInventory) db.get(db.find(value)); // store inventory
        else if (value.contains("[") && value.contains("]")) { // bank inventory
            String bankInitials = value.substring(0, value.indexOf("[") - 1);
            String last4Digits = value.substring(value.indexOf("[") + 2, value.length() - 1);
            return (Inventory) db.get(db.find(bankInitials, last4Digits));
        } else
            return (Inventory) db.get(db.find(value));
    }

    public static String formatInventory(Inventory value) {
        if (value != null)
            if (value.getAccountNo().equals("")) // non-bank inventory
                return value.getName();
            else {
                String bankInitials = "";
                for (int i = 0; i < value.getName().length(); i++)
                    if (Character.isUpperCase(value.getName().charAt(i)))
                        bankInitials += value.getName().charAt(i);
                String last4Digits = value.getAccountNo().substring(value.getAccountNo().length() - 4);
                return bankInitials + " [-" + last4Digits + "]";
            }
        else
            return "";
    }

    public static UserObject parseUserObject(String value) {
        if (value.contains("[") && value.contains("]")) {
            String name = value.substring(0, value.indexOf("[") - 1);
            String contactName = value.substring(value.indexOf("[") + 1, value.length() - 1);
            Database<?> db = DatabaseFacade.getDatabase("UserObjects");
            return (UserObject) db.get(db.find(name, contactName));
        } else {
            String firstName = value.substring(0, value.indexOf(" "));
            String lastName = value.substring(value.indexOf(" ") + 1, value.length());
            Database<?> db = DatabaseFacade.getDatabase("UserObjects");
            return (UserObject) db.get(db.find(firstName, lastName));
        }
    }

    public static String formatUserObject(UserObject value) {
        if (value instanceof Person)
            return ((Person) value).getFirstName() + " " + ((Person) value).getLastName();
        else {
            return ((Organization) value).getName() + " [" + ((Organization) value).getContactName() + "]";
        }
    }

    public static Item parseItemFromComboBox(String value) {
        String itemCode = value.substring(0, value.indexOf(" "));
        return parseItem(itemCode);
    }

    public static String formatStockForComboBox(Stock value) {
        return formatItem(value.getItem()) + " " + value.getItem().getName() + " (" + formatMoney(value.getPrice())
                + "/" + value.getItem().getUnit() + ")";
    }

    public static String formatItemForComboBox(Item value) {
        return formatItem(value) + " " + value.getName() + " (" + formatMoney(value.getPrice()) + "/" + value.getUnit()
                + ")";
    }

    public static Item parseItem(String value) {
        int id = Integer.parseInt(value.substring(value.indexOf("-") + 1));
        Items db = (Items) DatabaseFacade.getDatabase("Items");
        return db.get(db.find(id));
    }

    public static String formatItem(Item value) {
        return value.getType().toString().charAt(0) + "-" + value.getId();
    }

    public static Store parseStore(String value) {
        String name = value.substring(0, value.indexOf("(") - 1);
        String ownerString = value.substring(value.indexOf("(") + 1, value.indexOf(")"));
        UserObject owner = parseUserObject(ownerString);
        Stores db = (Stores) DatabaseFacade.getDatabase("Stores");
        return db.get(db.find(name, owner));
    }

    public static String formatStore(Store value) {
        return value.getName() + " (" + formatUserObject(value.getOwner()) + ")";
    }

    public static String formatSaleRemark(SaleTransaction value) {
        return formatBalanceId(value.getId(), SaleTransaction.class) + " Sale of " + formatMoneySafely(value.getTotal())
                + " AED by " + formatUserObject(value.getCustomer()) + " at " + formatStore(value.getStore());
    }

    public static String formatPurchaseRemark(PurchaseTransaction value) {
        return formatBalanceId(value.getId(), PurchaseTransaction.class) + " Purchase of "
                + formatMoneySafely(value.getTotal()) + " AED by " + formatUserObject(value.getSupplier()) + " at "
                + formatStore(value.getStore());
    }

    public static String formatDeliveryRemark(DeliveryTransaction value) {
        return formatBalanceId(value.getId(), DeliveryTransaction.class) + " Delivery from "
                + formatStore(value.getFromStore()) + " to " + formatStore(value.getToStore());
    }

    public static String formatProductionRemark() {
        return "Expense in Production";
    }

    public static Date setTimeToZero(Date date) {
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        } else
            return null;
    }

    public static String convertNumberToWords(double value) {
        NumberWordConverter nwc = new NumberWordConverter();
        String result = nwc.getMoneyIntoWords(value);
        return convertToSentenceCase(result);
    }

    public static double addSign(double value, boolean isCredit) {
        if ((value >= 0 && !isCredit) || (value < 0 && isCredit))
            return value * -1;
        else
            return value;
    }

    public static String convertToSentenceCase(String inputString) {
        if (StringUtils.isBlank(inputString)) {
            return "";
        }

        if (StringUtils.length(inputString) == 1) {
            return inputString.toUpperCase();
        }

        StringBuffer resultPlaceHolder = new StringBuffer(inputString.length());

        Stream.of(inputString.split(" ")).forEach(stringPart ->
        {
            if (stringPart.length() > 1)
                resultPlaceHolder.append(stringPart.substring(0, 1)
                        .toUpperCase())
                        .append(stringPart.substring(1)
                                .toLowerCase());
            else
                resultPlaceHolder.append(stringPart.toUpperCase());

            resultPlaceHolder.append(" ");
        });
        return StringUtils.trim(resultPlaceHolder.toString());
    }
}

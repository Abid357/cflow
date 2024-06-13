package globals;

import java.util.Date;
import java.util.List;

import cores.DeliveryTransaction;
import cores.ITransaction;
import cores.IntraTransfer;
import cores.Inventory;
import cores.RecordAdjustment;
import cores.Transaction;
import cores.UserObject;
import databases.DeliveryTransactions;
import databases.Inventories;
import databases.InventoryRecords;
import databases.PurchaseTransactions;
import databases.RecordAdjustments;
import databases.SaleTransactions;
import databases.Transactions;
import databases.UserObjects;
import databases.UserRecords;

public class RecordFacade {

	public static double calculateAmountWithTax(double amount, double tax) {
		return amount + tax;
	}

	public static boolean removeTransaction(int id) {
		Transactions transactions = (Transactions) DatabaseFacade.getDatabase("Transactions");
		cores.Transaction transaction = transactions.get(transactions.find(id));
		String balanceId = ValueFormatter.formatBalanceId(id, cores.Transaction.class);
		
		// reverse isCredit() method during removal process
		RecordFacade.record(transaction.getInventory(), transaction.getAmount(), transaction.getTax(),
				!transaction.isCredit(), transaction.getUserObject()); 
		RecordFacade.removeInventoryRecord(transaction.getInventory(), transaction.getAmount(), transaction.getTax(),
				transaction.isCredit(), balanceId);
		RecordFacade.removeUserRecord(transaction.getUserObject(), transaction.getAmount(), transaction.getTax(),
				!transaction.isCredit(), balanceId);
		return transactions.remove(id);
	}

	public static boolean removeTransactionFromOthers(int id) {
		boolean isRemoved = false;
		List<Transaction> list;
		SaleTransactions dbSale = (SaleTransactions) DatabaseFacade.getDatabase("SaleTransactions");
		for (int i = 0; i < dbSale.getList().size() && !isRemoved; i++) {
			list = dbSale.get(i).getCosts();
			for (int j = 0; j < list.size(); j++)
				if (list.get(j).getId() == id) {
					double oldTotal = dbSale.get(i).getTotal();
					double newAmount = calculateAmountWithTax(list.get(j).getAmount(), list.get(j).getTax());
					dbSale.get(i).setTotal(oldTotal - newAmount);
					isRemoved = list.remove(list.get(j));
					break;
				}
			if (!isRemoved) {
                list = dbSale.get(i).getPayments();
                for (int j = 0; j < list.size(); j++)
                    if (list.get(j).getId() == id) {
                        isRemoved = list.remove(list.get(j));
                        break;
                    }
            }
		}
		if (isRemoved) {
            dbSale.setDirty(true);
            return true;
        }

		PurchaseTransactions dbPurchase = (PurchaseTransactions) DatabaseFacade.getDatabase("PurchaseTransactions");
		for (int i = 0; i < dbPurchase.getList().size() && !isRemoved; i++) {
			list = dbPurchase.get(i).getCosts();
			for (int j = 0; j < list.size(); j++)
				if (list.get(j).getId() == id) {
                    double oldTotal = dbPurchase.get(i).getTotal();
                    double newAmount = calculateAmountWithTax(list.get(j).getAmount(), list.get(j).getTax());
                    dbPurchase.get(i).setTotal(oldTotal - newAmount);
					isRemoved = list.remove(list.get(j));
					break;
				}
            if (!isRemoved) {
                list = dbPurchase.get(i).getPayments();
                for (int j = 0; j < list.size() && !isRemoved; j++)
                    if (list.get(j).getId() == id) {
                        isRemoved = list.remove(list.get(j));
                        break;
                    }
            }
		}
		if (isRemoved) {
            dbPurchase.setDirty(true);
            return true;
        }

		DeliveryTransactions dbDelivery = (DeliveryTransactions) DatabaseFacade.getDatabase("DeliveryTransactions");
		for (int i = 0; i < dbDelivery.getList().size() && !isRemoved; i++) {
			list = dbDelivery.get(i).getCosts();
			for (int j = 0; j < list.size() && !isRemoved; j++)
				if (list.get(j).getId() == id) {
                    double oldTotal = dbDelivery.get(i).getTotal();
                    double newAmount = calculateAmountWithTax(list.get(j).getAmount(), list.get(j).getTax());
                    dbDelivery.get(i).setTotal(oldTotal - newAmount);
					isRemoved = list.remove(list.get(j));
					break;
				}
		}
        if (isRemoved) {
            dbDelivery.setDirty(true);
            return true;
        }
		return false;
	}

	public static void adjustRecord(double amount, boolean isCredit, UserObject user, Inventory inventory,
			String remark) {
		RecordAdjustments db = (RecordAdjustments) DatabaseFacade.getDatabase("RecordAdjustments");
		int id = db.maxID() + 1;
		Date date = new Date();
		db.add(id, amount, isCredit, user, inventory, remark, date);
		db.setDirty(true);
		record(inventory, amount, 0.00, isCredit, user);
		String balanceId = ValueFormatter.formatBalanceId(id, RecordAdjustment.class);
		if (user != null)
			addUserRecord(user, amount, 0.00, isCredit, balanceId, date);
		if (inventory != null)
			addInventoryRecord(inventory, amount, 0.00, isCredit, balanceId, date);
	}

	public static void removeInventoryRecord(Inventory inventory, double amount, double tax, boolean isCredit,
			String balanceId) {
		InventoryRecords db = (InventoryRecords) DatabaseFacade.getDatabase("InventoryRecords");
		int startIndex = db.getInventoryStartIndex(ValueFormatter.formatInventory(inventory));
		if (startIndex == -1)
			return;
		else {
			int endIndex = db.getInventoryEndIndex(ValueFormatter.formatInventory(inventory));
			int i = db.find(balanceId, inventory);
			db.remove(balanceId, inventory);
			double total = ValueFormatter.addSign(calculateAmountWithTax(amount, tax), isCredit);

			// update subsequent user records
			endIndex--;
			for (; i <= endIndex; i++) {
				double oldBalance = db.get(i).getBalance();
				db.get(i).setBalance(oldBalance - total);
			}
		}
		db.setDirty(true);
	}

	public static boolean isRecordCredit(ITransaction t, Inventory inventory) {
		if (t instanceof IntraTransfer) {
			IntraTransfer intraTransfer = (IntraTransfer) t;
			if (intraTransfer.getFromInventory().equals(inventory))
				return false;
			else if (intraTransfer.getToInventory().equals(inventory))
				return true;
		} else if (t instanceof DeliveryTransaction) {
			DeliveryTransaction delivery = (DeliveryTransaction) t;
			if (delivery.getFromStore().getInventory().equals(inventory))
				return false;
			else if (delivery.getToStore().getInventory().equals(inventory))
				return true;
		}
		return t.isCredit();
	}

	public static void addInventoryRecord(Inventory inventory, double amount, double tax, boolean isCredit,
			String balanceId, Date date) {
		InventoryRecords db = (InventoryRecords) DatabaseFacade.getDatabase("InventoryRecords");
		int startIndex = db.getInventoryStartIndex(ValueFormatter.formatInventory(inventory));
		if (startIndex == -1) {
			db.add(balanceId, inventory, inventory.getBalance());
		} else {
			date = ValueFormatter.setTimeToZero(date);
			int endIndex = db.getInventoryEndIndex(ValueFormatter.formatInventory(inventory));
			int i;
			for (i = endIndex; i >= startIndex; i--) {
				Date recordDate = ValueFormatter.parseBalanceId(db.get(i).getId()).getDate();
				if (recordDate.equals(date) || recordDate.before(date))
					break;
			}

			double oldBalance, total;
			// check if record has to be inserted at the very beginning
			if (i < startIndex) {
				ITransaction t = ValueFormatter.parseBalanceId(db.get(startIndex).getId());
				double recordAmount = t.getAmount();
				if (RecordFacade.isRecordCredit(t, db.get(startIndex).getInventory()))
					recordAmount *= -1;
				oldBalance = db.get(startIndex).getBalance() + recordAmount;
			} else
				oldBalance = db.get(i).getBalance();
			total = ValueFormatter.addSign(calculateAmountWithTax(amount, tax), isCredit);
			db.add(balanceId, inventory, total + oldBalance, ++i);

			// update subsequent inventory records
			endIndex++;
			for (++i; i <= endIndex; i++) {
				oldBalance = db.get(i).getBalance();
				db.get(i).setBalance(total + oldBalance);
			}
		}
		db.setDirty(true);
	}

	public static void removeUserRecord(UserObject userObject, double amount, double tax, boolean isCredit,
			String balanceId) {
		UserRecords db = (UserRecords) DatabaseFacade.getDatabase("UserRecords");
		int startIndex = db.getUserStartIndex(ValueFormatter.formatUserObject(userObject));
		if (startIndex == -1)
			return;
		else {
			int endIndex = db.getUserEndIndex(ValueFormatter.formatUserObject(userObject));
			int i = db.find(balanceId, userObject);
			db.remove(balanceId, userObject);
			double total = ValueFormatter.addSign(calculateAmountWithTax(amount, tax), isCredit);

			// update subsequent inventory records
			endIndex--;
			for (; i <= endIndex; i++) {
				double oldBalance = db.get(i).getBalance();
				db.get(i).setBalance(oldBalance - total);
			}
		}
		db.setDirty(true);
	}

	public static void addUserRecord(UserObject userObject, double amount, double tax, boolean isCredit,
			String balanceId, Date date) {
		UserRecords db = (UserRecords) DatabaseFacade.getDatabase("UserRecords");
		int startIndex = db.getUserStartIndex(ValueFormatter.formatUserObject(userObject));
		if (startIndex == -1) {
			db.add(balanceId, userObject, userObject.getBalance());
		} else {
			date = ValueFormatter.setTimeToZero(date);
			int endIndex = db.getUserEndIndex(ValueFormatter.formatUserObject(userObject));
			int i;
			for (i = endIndex; i >= startIndex; i--) {
				Date recordDate = ValueFormatter.parseBalanceId(db.get(i).getId()).getDate();
				if (recordDate.equals(date) || recordDate.before(date))
					break;
			}

			double oldBalance, total;
			// check if record has to be inserted at the very beginning
			if (i < startIndex) {
				oldBalance = db.get(startIndex).getBalance() + ValueFormatter.parseBalanceId(balanceId).getAmount();
			} else
				oldBalance = db.get(i).getBalance();
			total = ValueFormatter.addSign(calculateAmountWithTax(amount, tax), isCredit);
			db.add(balanceId, userObject, total + oldBalance, ++i);

			// update subsequent user records
			endIndex++;
			for (++i; i <= endIndex; i++) {
				oldBalance = db.get(i).getBalance();
				db.get(i).setBalance(total + oldBalance);
			}
		}
		db.setDirty(true);
	}

	public static void record(Inventory inventory, double amount, double tax, boolean isCredit, UserObject userObject) {
		double total = ValueFormatter.addSign(calculateAmountWithTax(amount, tax), isCredit);
		if (inventory != null) {
			double oldBalance = inventory.getBalance();
			inventory.setBalance(oldBalance + total);
			Inventories inventories = (Inventories) DatabaseFacade.getDatabase("Inventories");
			inventories.setDirty(true);
		}

		if (userObject != null) {
			double oldBalance = userObject.getBalance();
			userObject.setBalance(oldBalance - total);
			UserObjects userObjects = (UserObjects) DatabaseFacade.getDatabase("UserObjects");
			userObjects.setDirty(true);
		}
	}
}

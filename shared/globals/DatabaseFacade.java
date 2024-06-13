package globals;

import java.util.LinkedHashMap;
import java.util.Map;

import cores.DeliveryTransaction;
import cores.IntraTransfer;
import cores.Inventory;
import cores.InventoryRecord;
import cores.Item;
import cores.Production;
import cores.PurchaseTransaction;
import cores.RecordAdjustment;
import cores.SaleTransaction;
import cores.Store;
import cores.Transaction;
import cores.TransactionCategory;
import cores.UserObject;
import cores.UserRecord;
import databases.Database;
import databases.DeliveryTransactions;
import databases.IntraTransfers;
import databases.Inventories;
import databases.InventoryRecords;
import databases.Items;
import databases.Productions;
import databases.PurchaseTransactions;
import databases.RecordAdjustments;
import databases.SaleTransactions;
import databases.Stores;
import databases.TransactionCategories;
import databases.Transactions;
import databases.UserCategories;
import databases.UserObjects;
import databases.UserRecords;

public class DatabaseFacade {

	private static Map<String, Database<?>> map;
	public static int dirtyCount = 0;

	public static void init() {
		Database<UserObject> userObjects = new UserObjects();
		Database<String> userCategories = new UserCategories();
		Database<Inventory> inventories = new Inventories();
		Database<Transaction> transactions = new Transactions();
		Database<TransactionCategory> transactionCategories = new TransactionCategories();
		Database<IntraTransfer> intraTransfers = new IntraTransfers();
		Database<InventoryRecord> inventoryRecords = new InventoryRecords();
		Database<UserRecord> userRecords = new UserRecords();
		Database<Item> items = new Items();
		Database<Store> stores = new Stores();
		Database<SaleTransaction> sales = new SaleTransactions();
		Database<DeliveryTransaction> deliveries = new DeliveryTransactions();
		Database<PurchaseTransaction> purchases = new PurchaseTransactions();
		Database<RecordAdjustment> adjustments = new RecordAdjustments();
		Database<Production> productions = new Productions();

		new DatabaseFacade(userObjects, userCategories, inventories, transactions, transactionCategories,
				intraTransfers, inventoryRecords, userRecords, items, stores, sales, purchases, deliveries,
				adjustments, productions);
	}

	public DatabaseFacade(Database<?>... databases) {
		map = new LinkedHashMap<String, Database<?>>();
		for (Database<?> database : databases)
			map.put(database.getClass().getSimpleName(), database);
	}

	public static Database<?> getDatabase(String database) {
		return map.get(database);
	}

	public static void addDatabase(Database<?> database) {
		map.put(database.getClass().getSimpleName(), database);
	}

	public static boolean loadDatabases() {
		try {
			for (Database<?> database : map.values())
				if (!database.loadList())
					return false;
			return true;
		} catch (Exception e) {
			LOGGER.Error.log(e);
			return false;
		}
	}

	public static boolean hasUnsavedData() {
		try {
			for (Database<?> database : map.values())
				if (database.isDirty())
					return true;
			return false;
		} catch (Exception e) {
			LOGGER.Error.log(e);
			return false;
		}
	}

	public static boolean saveDatabases() {
		try {
			for (Database<?> database : map.values())
				if (database.isDirty())
					if (!database.saveList())
						return false;
					else
						++dirtyCount;
			return true;
		} catch (Exception e) {
			LOGGER.Error.log(e);
			return false;
		}
	}
}

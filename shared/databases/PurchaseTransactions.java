package databases;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cores.PurchaseTransaction;
import cores.Stock;
import cores.Store;
import cores.Transaction;
import cores.UserObject;
import helpers.TransactionStatus;

public class PurchaseTransactions implements Database<PurchaseTransaction> {
	private List<PurchaseTransaction> list;
	private boolean isDirty;
	private handlers.PurchaseTransactions db;

	public PurchaseTransactions() {
		list = new ArrayList<PurchaseTransaction>();
		db = new handlers.PurchaseTransactions();
	}

	public int maxID() {
		int max = 0;
		if (!list.isEmpty()) {
			max = list.get(0).getId();
			for (int i = 1; i < list.size(); i++)
				if (list.get(i).getId() > max)
					max = list.get(i).getId();
		}
		return max;
	}

	@Override
	public boolean isDirty() {
		return isDirty;
	}

	@Override
	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}

	@Override
	public int find(Object... objects) {
		int id = (int) objects[0];
		for (int i = 0; i < list.size(); i++)
			if (list.get(i).getId() == id)
				return i;
		return -1;
	}

	@Override
	public boolean remove(Object... objects) {
		int id = (int) objects[0];
		for (PurchaseTransaction transaction : list)
			if (transaction.getId() == id)
				return list.remove(transaction);
		return false;
	}

	@Override
	public boolean add(Object... objects) {
		isDirty = true;
		int id = (int) objects[0];
		Date date = (Date) objects[1];
		UserObject customer = (UserObject) objects[2];
		Store store = (Store) objects[3];
		@SuppressWarnings("unchecked")
		List<Stock> items = (List<Stock>) objects[4];
		double discount = (double) objects[5];
		double vat = (double) objects[6];
		double total = (double) objects[7];
		@SuppressWarnings("unchecked")
		List<Transaction> payments = (List<Transaction>) objects[8];
		@SuppressWarnings("unchecked")
		List<Transaction> costs = (List<Transaction>) objects[9];
		TransactionStatus itemStatus = (TransactionStatus) objects[10];
		TransactionStatus paymentStatus = (TransactionStatus) objects[11];
		String lpoNo = (String) objects[12];
		String piNo = (String) objects[13];
		String invoiceNo = (String) objects[14];
		list.add(0,new PurchaseTransaction(id, date, customer, store, items, discount, vat, total, payments, costs,
				itemStatus, paymentStatus, lpoNo, piNo, invoiceNo));
		return true;
	}

	@Override
	public PurchaseTransaction get(int index) {
		if (index < 0 || index >= list.size())
			return null;
		else
			return list.get(index);
	}

	@Override
	public boolean loadList() {
		// TODO Auto-generated method stub
		isDirty = false;
		list = db.load();
		if (list != null)
			return true;
		else {
			list = new ArrayList<PurchaseTransaction>();
			return false;
		}
	}

	@Override
	public boolean saveList() {
		// TODO Auto-generated method stub
		if (isDirty) {
			isDirty = false;
			return db.save(list);
		} else
			return false;
	}

	@Override
	public List<PurchaseTransaction> getList() {
		return list;
	}
}

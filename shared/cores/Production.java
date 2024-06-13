package cores;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import databases.Productions;
import globals.DatabaseFacade;
import globals.ValueFormatter;
import helpers.TransactionStatus;

public class Production implements ITransaction, Comparable<Production> {
	private int id;
	private String alternativeId; //alternative ID: PR-[productID]-[date]-[serialnumber]
	private Date startDate;
	private Date endDate;
	private Stock product;
	private Store productStore;
	private String productUnit;
	private List<Stock> materials;
	private List<Store> materialStores;
	private List<Transaction> costs;
	private TransactionStatus status;


	public Production(int id, String alternativeId, Date startDate, Date endDate, Stock product, Store productStore, String productUnit,
			List<Stock> materials, List<Store> materialStores, List<Transaction> costs, TransactionStatus status) {
		super();
		this.id = id;
		this.alternativeId = alternativeId;
		this.startDate = startDate;
		this.endDate = endDate;
		this.product = product;
		this.productStore = productStore;
		this.productUnit = productUnit;
		this.materials = materials;
		this.materialStores = materialStores;
		this.costs = costs;
		this.status = status;
	}

	
	public Production(int id, Date startDate, Date endDate, Stock product, Store productStore, String productUnit,
			List<Stock> materials, List<Store> materialStores, List<Transaction> costs, TransactionStatus status) {
		this(id, null, startDate, endDate, product, productStore, productUnit, materials, materialStores, costs, status);
	}

	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public double getAmount() {
		// TODO Auto-generated method stub
		double materialCost = 0.0;
		for (Stock stock : materials)
			materialCost += (stock.getPrice() * stock.getQuantity());
		double otherCost = 0.0;
		for (Transaction transaction : costs)
			otherCost += transaction.getAmount() + (transaction.getAmount() * transaction.getTax());
		return materialCost + otherCost;
	}

	public void setAlternativeId(String alternativeId) {
		this.alternativeId = alternativeId;
	}
	
	public String getAlternativeId() {
		return alternativeId;
	}
	
	public void createAlternativeId() {
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
		DecimalFormat df = new DecimalFormat("##00");
		int productId = product.getItem().getId();
		int serial = 1;
		Productions db = (Productions) DatabaseFacade.getDatabase("Productions");
		List<Production> lst = db.getList();
		for (Production production : lst) {
			startDate = ValueFormatter.setTimeToZero(startDate);
			Date productionDate = ValueFormatter.setTimeToZero(production.getStartDate());
			if (production.getProduct().getItem().getId() == productId && productionDate.equals(startDate))
				++serial;
		}
		alternativeId =  "PR-" + productId + "-" + sdf.format(startDate) + "-" + df.format(serial);
	}

	@Override
	public Date getDate() {
		// TODO Auto-generated method stub
		return getStartDate();
	}

	@Override
	public boolean isCredit() {
		// TODO Auto-generated method stub
		return false;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getProductUnit() {
		return productUnit;
	}

	public void setProductUnit(String productUnit) {
		this.productUnit = productUnit;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Stock getProduct() {
		return product;
	}

	public void setProduct(Stock product) {
		this.product = product;
	}

	public Store getProductStore() {
		return productStore;
	}

	public void setProductStore(Store productStore) {
		this.productStore = productStore;
	}

	public List<Stock> getMaterials() {
		return materials;
	}

	public void setMaterials(List<Stock> materials) {
		this.materials = materials;
	}

	public List<Store> getMaterialStores() {
		return materialStores;
	}

	public void setMaterialStores(List<Store> materialStores) {
		this.materialStores = materialStores;
	}

	public List<Transaction> getCosts() {
		return costs;
	}

	public void setCosts(List<Transaction> costs) {
		this.costs = costs;
	}

	public TransactionStatus getStatus() {
		return status;
	}

	public void setStatus(TransactionStatus status) {
		this.status = status;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		String itemString = "[";
		for (int i = 0; i < materials.size(); i++)
			itemString += materials.get(i) + "/";
		if (materials.size() != 0)
			itemString = itemString.substring(0, itemString.length() - 1);
		itemString += "]";
		String costIds = "[";
		for (int i = 0; i < costs.size(); i++)
			costIds += costs.get(i).getId() + "/";
		if (costs.size() != 0)
			costIds = costIds.substring(0, costIds.length() - 1);
		costIds += "]";
		String storeString = "[";
		for (int i = 0; i < materialStores.size(); i++)
			if (materialStores.get(i) == null)
				storeString += "null/";
			else
				storeString += ValueFormatter.formatStore(materialStores.get(i)) + "/";
		if (materialStores.size() != 0)
			storeString = storeString.substring(0, storeString.length() - 1);
		storeString += "]";
		String dateString = "null";
		if (endDate != null)
			dateString = ValueFormatter.formatDate(endDate);
		return id + "," + alternativeId + "," + ValueFormatter.formatDate(startDate) + "," + dateString + "," + product + ","
				+ ValueFormatter.formatStore(productStore) + "," + productUnit + "," + itemString + "," + storeString
				+ "," + costIds + "," + status;
	}

	@Override
	public int compareTo(Production production) {
		int comparedId = production.getId();
		return comparedId - this.id;
	}

	public static List<Store> parseStores(String storeString) {
		List<Store> stores = new ArrayList<Store>();
		String decapsulatedString = storeString.substring(1, storeString.length() - 1);
		if (decapsulatedString.isEmpty())
			return stores;
		String[] splitItems = decapsulatedString.split("/");
		for (String eachItem : splitItems)
			if (eachItem.equals("null"))
				stores.add(null);
			else
				stores.add(ValueFormatter.parseStore(eachItem));
		return stores;
	}

	public static Production parse(String record[]) {
		int id = Integer.parseInt(record[0]);
		String alternativeId = record[1];
		Date startDate = ValueFormatter.parseDate(record[2]);
		Date endDate = null;
		if (!record[3].equals("null"))
			ValueFormatter.parseDate(record[3]);
		Stock product = Stock.parse(record[4].split(";"));
		Store productStore = ValueFormatter.parseStore(record[5]);
		String productUnit = record[6];
		List<Stock> materials = Stock.parseItems(record[7]);
		List<Store> materialStores = parseStores(record[8]);
		List<Transaction> costs = Transaction.parseTransactions(record[9]);
		TransactionStatus status = TransactionStatus.valueOf(record[10]);
		return new Production(id, alternativeId, startDate, endDate, product, productStore, productUnit, materials, materialStores,
				costs, status);
	}

}

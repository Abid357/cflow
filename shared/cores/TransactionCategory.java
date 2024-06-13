package cores;

public class TransactionCategory {

	private String name;
	private boolean isCreditable;
	private boolean isDebitable;

	public TransactionCategory(String name, boolean isCreditable, boolean isDebitable) {
		super();
		this.name = name;
		this.isCreditable = isCreditable;
		this.isDebitable = isDebitable;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isCreditable() {
		return isCreditable;
	}

	public void setCreditable(boolean isCreditable) {
		this.isCreditable = isCreditable;
	}

	public boolean isDebitable() {
		return isDebitable;
	}

	public void setDebitable(boolean isDebitable) {
		this.isDebitable = isDebitable;
	}

	@Override
	public String toString() {
		return name + "," + isCreditable + "," + isDebitable;
	}
}

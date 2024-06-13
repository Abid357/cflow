package cores;

import java.util.Date;

public interface ITransaction {
	int getId();
	double getAmount();
	Date getDate();
	boolean isCredit();
}

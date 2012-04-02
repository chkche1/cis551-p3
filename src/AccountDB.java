import java.util.HashMap;
import java.io.*;

public class AccountDB implements Serializable {
	public static final long serialVersionUID = 331000;
	public static final String acctFile = "acct.db";

	public static AccountDB load() throws IOException {
		return (AccountDB)Disk.load(acctFile);
	}

	public void save() {
		try {
			Disk.save(this, acctFile);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private HashMap<String,Account> db;
	private int nextAcctNum;

	public AccountDB() {
		db = new HashMap<String,Account>();
		nextAcctNum = 0;
	}

	public String getNextAcctNum() {
		return Integer.toString(nextAcctNum);
	}

	public void addAccount(Account a) 
	throws AccountException
	{
		String acct = a.getNumber();
		if (!acct.equals(Integer.toString(nextAcctNum))) {
			throw new AccountException("Invalid account number.");
		}
		nextAcctNum = nextAcctNum + 1;
		if (db.containsKey(acct)) {
			throw new AccountException("Account already in database.");
		}
		db.put(acct, a);
	}

	public Account getAccount(String acctNumber) 
	throws AccountException
	{
		Object obj = db.get(acctNumber);
		if (obj == null) {
			throw new AccountException("Account not found.");
		}
		return (Account)obj;
	}

	// Destructively 
	public void updateAccount(Account a) 
	throws AccountException
	{
		if (!db.containsKey(a.getNumber())) {
			throw new AccountException("Account not found.");
		}
		db.put(a.getNumber(), a);
	}
}

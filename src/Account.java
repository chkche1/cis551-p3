import java.security.PublicKey;
import java.io.Serializable;

public class Account implements Serializable {
	public static final long serialVersionUID = 331005;
	private String owner;
	private String acctNumber;
	private double balance;
	public PublicKey kPub;

	public Account(double b, String o, String n, PublicKey k) {
		this.balance = b;
		this.owner = o;
		this.acctNumber = n;
		this.kPub = k;
	}

	public double getBalance() {
		return balance;
	}

	public String getOwner() {
		return owner;
	}

	public String getNumber() {
		return acctNumber;
	}

	public PublicKey getKey() {
		return kPub;
	}

	public void deposit(double amt) {
		balance = balance + amt;
	}

	public void withdraw(double amt) throws TransException {
		if (balance >= amt) {
			balance = balance - amt;
		} else {
			throw new TransException("Withdrawal amount exceeds balance.");
		}
	}
}

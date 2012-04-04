import java.io.Serializable;
import java.util.Date;


public class Transaction implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public MessageType type;
	public double amount;
	byte[] amt;
	byte[] bal;
	byte[] ts;
	public double balance;
	public Date timestamp;
	
	public Transaction(MessageType t, double amt, double b, Date ts){
		this.type = t;
		this.amount =amt;
		this.balance = b;
		this.timestamp = ts;
	}
	
	public Transaction(MessageType t, byte[] a, byte[] b, byte[] d){
		this.type = t;
		this.amt = a;
		this.bal =b;
		this.ts = d;
	}
}

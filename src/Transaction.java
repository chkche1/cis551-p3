import java.io.Serializable;
import java.util.Date;


public class Transaction implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public MessageType type;
	public double amount;
	public double balance;
	public Date timestamp;
	
	public Transaction(MessageType t, double amt, double b, Date ts){
		this.type = t;
		this.amount =amt;
		this.balance = b;
		this.timestamp = ts;
	}
	
}

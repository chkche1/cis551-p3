import java.io.Serializable;
import java.security.PrivateKey;

public class ATMCard implements Serializable{
	public static final long serialVersionUID = 33100l;
	private String acctNum;
	private PrivateKey kPriv;
	private String PIN;

	public ATMCard(String acctNum, PrivateKey kPriv, String PIN) {
		this.acctNum = acctNum;
		this.kPriv = kPriv;
		this.PIN = PIN;
	}

	public String getAcctNum() {
		return acctNum;
	}

	public PrivateKey getKey(String PIN) 
	throws AccountException
	{
		if (this.PIN.equals(PIN)) {
			return kPriv;
		} else {
			throw new AccountException("Incorrect Password");
		}
	}
}

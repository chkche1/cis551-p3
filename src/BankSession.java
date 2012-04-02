import java.net.*;
import java.io.*;
import java.security.*;

public class BankSession implements Session, Runnable {
	private Socket s;
	private ObjectOutputStream os;
	private ObjectInputStream is;

	private AccountDB accts;
	private Crypto crypto;
	private PrivateKey kPrivBank;
	private PublicKey  kPubBank;

	// These fields are initialized during authentication
	private Key kSession;
	private Account currAcct;
	private String atmID;

	// Add additional fields you need here
	private boolean init_flag;

	BankSession(Socket s, AccountDB a, KeyPair p)
	throws IOException
	{
		this.s = s;
		OutputStream out =  s.getOutputStream();
		this.os = new ObjectOutputStream(out);
		InputStream in = s.getInputStream();
		this.is = new ObjectInputStream(in);
		this.accts = a;
		this.kPrivBank = p.getPrivate();
		this.kPubBank = p.getPublic();
		this.crypto = new Crypto();
		this.init_flag = false;
	}

	public void run() {
		try {
			if (authenticateUser()) {
				while (doTransaction()) {
					// loop
				}
			}
			is.close();
			os.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Interacts with an ATMclient to 
	// (1) Authenticate the user
	// (2) If the user is valid, establish session key and any
	//     additional information needed for the protocol.
	// (3) Maintain a log of whether the login attempt succeeded
	// (4) Returns true if the user authentication succeeds, false otherwise
	public boolean authenticateUser() {
		boolean authOutcome = false;
		
		// waiting for first message.
		ProtocolMessage msg;
		
		try {
			while( (msg=(ProtocolMessage)is.readObject()) != null){
				int result = processMessage(msg);
				if(result==0){
					break;
				}else if(result ==1){
					authOutcome = true;
					break;
				}
				
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return authOutcome;
	}
	
	// 0 - error state, auth failed
	// 1 - success
	// 2 - continue processing more messages
	private int processMessage(ProtocolMessage pm){
		int result = 2;
		switch(pm.getMessageType()){
		case INIT:
			result = processInit(pm);
			break;
		case CHAL:
			result = processChal(pm);
			break;
		case RESP:
			result = processResp(pm);
			break;
		default:
			break;
		}
		
		return result;
	}
	
	// should send a challenge to ATM and return 2. Otherwise, return 0
	private int processInit(ProtocolMessage pm){
		if(!init_flag) return 0;
		
		
		
		
		
		return 2;
	}
	private int processChal(ProtocolMessage pm){
		return 0;
	}
	
	private int processResp(ProtocolMessage pm){
		return 0;
	}
	
	
	// Interacts with an ATMclient to 
	// (1) Perform a transaction 
	// (2) or end transactions if end-of-session message is received
	// (3) Maintain a log of the information exchanged with the client
	public boolean doTransaction() {

		// replace this code to carry out a bank transaction
		return false;
	}
}


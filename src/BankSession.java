import java.net.*;
import java.io.*;
import java.security.*;
import java.util.Random;

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
	private Random rand;
	private boolean init_flag;
	private Integer challenge;

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
		this.rand = new Random();
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
				
			}// end of while
			
			
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
		
		// Generate a random #, encrypt it with public key, and send it out
		challenge = rand.nextInt();
		
		try {
			byte[] byte_msg = crypto.encryptRSA(challenge, kPubBank);
			ProtocolMessage p = new ProtocolMessage(MessageType.CHAL, byte_msg);
			os.writeObject(byte_msg);
		} catch (KeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return 2;
	}
	
	// return 1 in case of success. return 0 otherwise.
	private int processChal(ProtocolMessage pm){
		// decrypt and send back response
		try {
			Integer atmChallenge = (Integer) crypto.decryptRSA(pm.getMessage(), kPrivBank);
			byte[] ans = crypto.encryptRSA(atmChallenge, kPubBank);
			ProtocolMessage rsp = new ProtocolMessage(MessageType.RESP, ans);
		} catch (KeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}
	
	private int processResp(ProtocolMessage pm){
		// decrypt and verify answer
		Integer atmAnswer;
		try {
			atmAnswer = (Integer) crypto.decryptRSA(pm.getMessage(), kPrivBank);
			if(!atmAnswer.equals(challenge)){
				return 0;
			}
		} catch (KeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 2;
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


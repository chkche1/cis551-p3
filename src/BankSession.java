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

	private PublicKey kPubAcct;

	// These fields are initialized during authentication
	private Key kSession;
	private Account currAcct;
	private String atmID;

	// Add additional fields you need here
	private Random rand;
	private boolean init_flag;
	private boolean isAuth;
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
		this.isAuth = false;
		this.rand = new Random();
	}

	public void run() {
		boolean temp;
		try {
			if (authenticateUser()) {
				while ((temp = doTransaction())) {
					// loop
					System.out.println("loop");
				}
				System.out.println(temp);
			}
			//is.close();
			//os.close();
			System.out.println("last line of run");
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("end");
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
			while(true){
				msg = (ProtocolMessage)is.readObject();
				if(msg==null){
					System.out.println("null!");
					continue;
				}
				//System.out.println("not null");
				int result = processMessage(msg);
				if(result==0){
					init_flag = false;
					break;
				}else if(result ==1){
					authOutcome = true;
					break;
				}

			}// end of while

		}catch(EOFException e){
			System.out.println("EOFException");
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("BankSession authOutcome: "+authOutcome);
		return authOutcome;
	}

	// 0 - error state, auth failed
	// 1 - success
	// 2 - continue processing more messages
	private int processMessage(ProtocolMessage pm){
		int result = 2;
		System.out.println("hello");

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
		case SESS:
			System.out.println("get to SESS");
			result = processSess(pm);
			break;
		default:
			break;
		}
		System.out.println("msg type: "+pm.getMessageType());
		//System.out.println("result: "+result);
		return result;
	}

	// should send a challenge to ATM and return 2. Otherwise, return 0
	private int processInit(ProtocolMessage pm){
		System.out.println("in BankSession INIT");

		init_flag = true;

		// lookup account in acct.db
		try {
			Integer aNum = (Integer)crypto.decryptRSA(pm.getMessage(), kPrivBank);
			Account acct = accts.getAccount(""+aNum);
			kPubAcct = acct.getKey();
			System.out.println("account number: "+aNum);
		} catch (AccountException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (KeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Generate a random #, encrypt it with public key, and send it out
		challenge = rand.nextInt();
		System.out.println("challenge: "+challenge);

		try {
			byte[] byte_msg = crypto.encryptRSA(challenge, kPubAcct);
			ProtocolMessage p = new ProtocolMessage(MessageType.CHAL, byte_msg);
			os.writeObject(p);
			os.flush();
		} catch (KeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		System.out.println("===============================");

		return 2;
	}

	// return 1 in case of success. return 0 otherwise.
	private int processChal(ProtocolMessage pm){
		System.out.println("in BankSession CHAL");

		if(!init_flag){
			System.out.println("===============================");

			return 0;
		}

		// decrypt and send back response
		try {
			Integer atmChallenge = (Integer) crypto.decryptRSA(pm.getMessage(), kPrivBank);
			System.out.println("atmChallenge: "+atmChallenge);
			byte[] ans = crypto.encryptRSA(atmChallenge, kPubAcct);
			ProtocolMessage rsp = new ProtocolMessage(MessageType.RESP, ans);
			os.writeObject(rsp);
			os.flush();
		} catch (KeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("===============================");

		return 0;
	}

	private int processResp(ProtocolMessage pm){
		System.out.println("in BankSession RESP");

		if(!init_flag) {
			System.out.println("===============================");
			return 0;
		}


		// decrypt and verify answer
		Integer atmAnswer;
		try {
			atmAnswer = (Integer) crypto.decryptRSA(pm.getMessage(), kPrivBank);
			if(!atmAnswer.equals(challenge)){
				System.out.println("===============================");
				return 0;
			}
		} catch (KeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		isAuth = true;
		System.out.println("===============================");
		return 2;
	}

	private int processSess(ProtocolMessage pm){
		System.out.println("in BankSession SESS");

		if(!init_flag || !isAuth) {
			System.out.println("===============================");
			return 0;
		}

		try {
			Integer id = (Integer)crypto.decryptRSA(pm.getMessage(), kPrivBank);
			atmID = id+"";
			kSession = crypto.makeAESKey();
			byte[] encrypted_sess;
			encrypted_sess = crypto.encryptRSA(kSession, kPubAcct);
			ProtocolMessage p = new ProtocolMessage(MessageType.SESS,encrypted_sess);
			os.writeObject(p);
			os.flush();
		} catch (KeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("===============================");

		return 1;
	}


	// Interacts with an ATMclient to 
	// (1) Perform a transaction 
	// (2) or end transactions if end-of-session message is received
	// (3) Maintain a log of the information exchanged with the client

	// states: 0(error), 1(success), 2(continue)
	public boolean doTransaction() {
		boolean more = true;
		ProtocolMessage msg;
		try {
			while(true){
				msg = (ProtocolMessage)is.readObject();
				if(msg==null){
					System.out.println("null!");
					continue;
				}
				int result = processTransaction(msg);
				if(result==0){
					break;
				}else if(result ==1){
					more = false;
					break;
				}

			}// end of while
		}catch(EOFException e){
			System.out.println("EOFException");
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		// replace this code to carry out a bank transaction
		return more;
	}

	private int processTransaction(ProtocolMessage pm){
		System.out.println("in processTransaction");
		MessageType type = pm.getMessageType();
		int result = 2;
		switch(type){
		case TRAN_B:
			getBalance();
			break;
		case TRAN_W:
			withdraw(pm);
			break;
		case TRAN_D:
			deposit(pm);
			break;
		case TRAN_Q:
			result = 1;
		default:
			result = 0;
		} // end of switch
		return result;

	}

	private void getBalance(){
		System.out.println("in getBalance");
		Double bal = currAcct.getBalance();
		try {
			byte[] sig = crypto.sign(bal.toString().getBytes(), kPrivBank);
			byte[] encrypted = crypto.encryptAES(bal.toString().getBytes(), kSession);
			ProtocolMessage p = new ProtocolMessage(MessageType.TRAN_B, encrypted, sig);
			os.writeObject(p);
			os.flush();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}// end of getBalance

	private void withdraw(ProtocolMessage pm){
		System.out.println("in withdraw");
		Double amount;
		try {
			String s = (String) crypto.decryptAES(pm.getMessage(), kSession);
			amount = new Double(s);
			currAcct.withdraw(amount);
			Double bal = currAcct.getBalance();
			String t = bal.toString();
			byte[] encrypted = crypto.encryptAES(t, kSession);
			ProtocolMessage p = new ProtocolMessage(MessageType.TRAN_W, encrypted);
			os.writeObject(p);
			os.flush();
			
		} catch (KeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}// end of withdraw

	private void deposit(ProtocolMessage pm){
		System.out.println("in deposit");
		Double amount;
		try {
			String s = (String) crypto.decryptAES(pm.getMessage(), kSession);
			amount = new Double(s);
			currAcct.deposit(amount);
			Double bal = currAcct.getBalance();
			String t = bal.toString();
			byte[] encrypted = crypto.encryptAES(t, kSession);
			ProtocolMessage p = new ProtocolMessage(MessageType.TRAN_D, encrypted);
			os.writeObject(p);
			os.flush();
			
		} catch (KeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}// end of deposit
	
}
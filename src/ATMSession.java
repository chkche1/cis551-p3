import java.io.*;
import java.security.*;
import java.net.*;
import java.util.Random;


public class ATMSession implements Session {
	private Socket s;
	private ObjectOutputStream os;
	private ObjectInputStream is;
	private BufferedReader textIn;

	private String ID; // ATM ID
	private ATMCard card;
	private PublicKey kBank;
	private PrivateKey kUser;
	private Crypto crypto;

	// This field is initialized during authentication
	private Key kSession;
	private Random rand;
	private Integer challenge;

	// Additional fields here

	ATMSession(Socket s, String ID, ATMCard card, PublicKey kBank) {
		this.s = s;
		this.ID = ID;
		this.card = card;
		this.kBank = kBank;
		this.crypto = new Crypto();
		this.rand = new Random();
		try {
			textIn = new BufferedReader(new InputStreamReader(System.in));
			OutputStream out =  s.getOutputStream();
			this.os = new ObjectOutputStream(out);
			InputStream in = s.getInputStream();
			this.is = new ObjectInputStream(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// This method authenticates the user and establishes a session key.
	public boolean authenticateUser(){
		System.out.println("Please enter your PIN: ");

		// First, the smartcard checks the user's pin to get the 
		// user's private key.
		try {
			String pin = textIn.readLine();
			kUser = card.getKey(pin);
		} catch (Exception e) {
			return false;
		}

		/* Implement the client half of the authentication protocol here */
		boolean authOutcome = false;

		// Send the init msg to bank
		// Read loop. Interactive from this point on
		try {
			System.out.println("ATM send INIT");
			System.out.println("card acct num: "+card.getAcctNum());
			byte[] encrypted_acctNum = crypto.encryptRSA(new Integer(card.getAcctNum()), kBank);
			ProtocolMessage msg = new ProtocolMessage(MessageType.INIT, encrypted_acctNum);
			os.writeObject(msg);
			os.flush();
			System.out.println("===============================");

			//while((msg = (ProtocolMessage) is.readObject()) != null){
			while(true){
				
				//if(is.available()!=0){
					//System.out.println("has data to read");
					msg = (ProtocolMessage) is.readObject();
					if(msg==null){
						System.out.println("null!");
						continue;
					}
					//System.out.println("not null");

					int result = processMessage(msg);
					if(result==0){
						break;
					}else if(result == 1){
						authOutcome = true;
						
						// authenticated. sens SESS msg to establish session key with Bank
						sendSESSMessage();
						break;
					}
				//}

			} // end of while

		}catch (EOFException e){
			System.out.println("End of file reached");
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return authOutcome;
	}
	
	private void sendSESSMessage(){
		System.out.println("ATM initializing SESS msg");
		// Initialize a SESS ProtocolMessage
		try {
			byte[] encrypted_id = crypto.encryptRSA(new Integer(ID), kBank);
			ProtocolMessage p = new ProtocolMessage(MessageType.SESS, encrypted_id);
			
			os.writeObject(p);
			os.flush();
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (KeyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
		case SESS:
			result = processSess(pm);
			break;
		default:
			break;
		}

		return result;
	}

	// return 0 or 2
	private int processInit(ProtocolMessage pm){
		System.out.println("in ATMSession INIT");
		System.out.println("===============================");

		// an ATM is not supposed to receive INIT messages
		return 0;
	}

	// return 0 or 2
	private int processChal(ProtocolMessage pm){
		System.out.println("in ATMSession CHAL");


		// Decrypt it and send back the answer
		try {
			Integer bankChallenge = (Integer)crypto.decryptRSA(pm.getMessage(), kUser);
			System.out.println("bankChallange: "+bankChallenge);
			byte[] ans = crypto.encryptRSA(bankChallenge, kBank);
			ProtocolMessage rsp = new ProtocolMessage(MessageType.RESP, ans);
			os.writeObject(rsp);
			os.flush();
		} catch (KeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("KeyException");
			System.out.println("===============================");
			return 0;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Send out challenge
		challenge = rand.nextInt();
		System.out.println("challenge: "+challenge);
		
		byte[] byte_msg;
		try {
			byte_msg = crypto.encryptRSA(challenge, kBank);
			ProtocolMessage c = new ProtocolMessage(MessageType.CHAL, byte_msg);
			os.writeObject(c);
			os.flush();
		} catch (KeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("KeyException");
			System.out.println("===============================");
			return 0;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("===============================");

		return 2;
	}

	// return 0 or 1
	private int processResp(ProtocolMessage pm){
		System.out.println("in ATMSession RESP");

		// decrypt and verify answer
		Integer bankAnswer;
		try {
			bankAnswer = (Integer) crypto.decryptRSA(pm.getMessage(), kUser);
			if(!bankAnswer.equals(challenge)){
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
		System.out.println("===============================");
		return 1;
	}

	private int processSess(ProtocolMessage pm){
		System.out.println("in ATMSession SESS");

		try {
			kSession = (Key)crypto.decryptRSA(pm.getMessage(), kUser);

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

	void printMenu() {
		System.out.println("*****************************");
		System.out.println("(1) Deposit");
		System.out.println("(2) Withdraw");
		System.out.println("(3) Get Balance");
		System.out.println("(4) Quit\n");
		System.out.print  ("Please enter your selection: ");
	}

	int getSelection() {
		try {
			String s = textIn.readLine();
			int i = Integer.parseInt(s, 10);
			return i;
		} catch (IOException e) {
			return -1;
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	double getDouble() {
		try {
			String s = textIn.readLine();
			double d = Double.parseDouble(s);
			return d;
		} catch (IOException e) {
			return 0.0;
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}

	void endSession() {
		try {
			os.close();
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	void doDeposit() {
		System.out.println("in doDeposit");
		System.out.println("How much do you want to deposit?");
		Double d = getDouble();
		byte[] dep;
		try {
			dep = crypto.encryptAES(d, kSession);
			String tmp = ""+d;
			byte[] sig = crypto.sign(tmp.getBytes(), kUser);
			ProtocolMessage pm = new ProtocolMessage(MessageType.TRAN_D, dep, sig);
			os.writeObject(pm);
			os.flush();
		} catch (SignatureException e){ 
			e.printStackTrace();
		}catch (KeyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// wait to read from bank
		while(true){

			try {
				ProtocolMessage msg = (ProtocolMessage)is.readObject();
				byte[] encrypted = msg.getMessage();
				String bal = new String((byte[])crypto.decryptAES(encrypted, kSession));
				System.out.println("Deposit is complete. Your balance is: "+bal);
			
			} catch (KeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}// end of while
		
	}

	void doWithdrawal() {
		System.out.println("in doWithdrawal");
		System.out.println("How much do you want to withdraw?");
		Double d = getDouble();
		byte[] dep;
		try {
			dep = crypto.encryptAES(d, kSession);
			String tmp = ""+d;
			byte[] sig = crypto.sign(tmp.getBytes(), kUser);
			ProtocolMessage pm = new ProtocolMessage(MessageType.TRAN_W, dep, sig);
			os.writeObject(pm);
			os.flush();
		} catch (SignatureException e){ 
			e.printStackTrace();
		}catch (KeyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// wait to read from bank
		while(true){

			try {
				ProtocolMessage msg = (ProtocolMessage)is.readObject();
				byte[] encrypted = msg.getMessage();
				String bal = new String((byte[])crypto.decryptAES(encrypted, kSession));
				System.out.println("Withdrawal is complete. Your balance is: "+bal);
			
			} catch (KeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}// end of while
		
	}

	void doBalance() {
		System.out.println("in doBalance");
		try {
			ProtocolMessage pm = new ProtocolMessage(MessageType.TRAN_B);
			os.writeObject(pm);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// wait to read from bank
		while(true){
			try {
				ProtocolMessage msg = (ProtocolMessage)is.readObject();
				byte[] encrypted = msg.getMessage();
				String bal = new String((byte[])crypto.decryptAES(encrypted, kSession));
				byte[] sig = msg.getSignature();
				System.out.println("Your balance is: "+bal);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} // end of while
	} 

	public boolean doTransaction() {
		printMenu();
		int x = getSelection();
		switch(x) {
		case 1 : doDeposit(); break;
		case 2 : doWithdrawal(); break;
		case 3 : doBalance(); break;
		case 4 : {endSession(); return false;}
		default: {System.out.println("Invalid choice.  Please try again.");}
		}
		return true;
	} 
}

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
		ProtocolMessage msg = new ProtocolMessage(MessageType.INIT);
		try {
			os.writeObject(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Read loop. Interactive from this point on
		try {
			while((msg = (ProtocolMessage) is.readObject()) != null){
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
	
	// return 0 or 2
	private int processInit(ProtocolMessage pm){
		// an ATM is not supposed to receive INIT messages
		return 0;
	}
	
	// return 0 or 2
	private int processChal(ProtocolMessage pm){
		
		// Decrypt it and send back the answer
		try {
			Integer bankChallenge = (Integer)crypto.decryptRSA(pm.getMessage(), kUser);
			byte[] ans = crypto.encryptRSA(bankChallenge, kBank);
			ProtocolMessage rsp = new ProtocolMessage(MessageType.RESP, ans);
		} catch (KeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Send out challenge
		challenge = rand.nextInt();
		byte[] byte_msg;
		try {
			byte_msg = crypto.encryptRSA(challenge, kBank);
			ProtocolMessage c = new ProtocolMessage(MessageType.CHAL, byte_msg);
			os.writeObject(c);
		} catch (KeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 2;
	}
	
	// return 0 or 1
	private int processResp(ProtocolMessage pm){
		// decrypt and verify answer
		Integer bankAnswer;
		try {
			bankAnswer = (Integer) crypto.decryptRSA(pm.getMessage(), kUser);
			if(!bankAnswer.equals(challenge)){
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
	}

	void doDeposit() {
	}

	void doWithdrawal() {
	}

	void doBalance() {
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

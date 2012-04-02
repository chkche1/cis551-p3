import java.security.*;
import java.io.*;
import java.net.*;

public class ATMClient {
	private static String ID = null;

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: java ATMClient <ATM #> " +
			"<BankServer>");
			System.exit(0);
		} 
		ID  = args[0]; // Initialize ATM ID number
		try {
			PublicKey kBank = (PublicKey)Disk.load(BankServer.pubKeyFile);
			while (true) {
				Socket s = new Socket(args[1], 2100);	    
				ATMCard card = null;
				// Get the ATMCard from the user
				try {
					card = getCard();
				} catch (IOException e) {
					System.out.println("Bad Card");
					continue;
				}
				ATMSession session = new ATMSession(s, ID, card, kBank);
				if (session.authenticateUser()) {
					while (session.doTransaction()) {
						// loop
						
						// TO-DO
						
						
					}
					System.out.println("Goodbye!");
					System.out.println("*****************************");
				} else {
					System.out.println("Authentication failed. " +
					"Please try again.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static ATMCard getCard() throws IOException {
		System.out.println("*****************************");
		System.out.println("           ATM #" + ID);
		System.out.print  ("\nPlease insert card: ");
		InputStreamReader is = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(is);

		String cardFile = br.readLine();
		return (ATMCard)Disk.load(cardFile);
	}
}

import java.io.*;
import java.security.*;

public class MakeAccounts {
	public static void main(String[] args) {
		InputStreamReader is;
		BufferedReader br = null;
		AccountDB accts = null;
		SecureRandom sr;
		Crypto crypto = new Crypto();

		try {
			is = new InputStreamReader(System.in);
			br = new BufferedReader(is);
			sr = new SecureRandom();
		} catch (Exception e) {
			System.out.println("Initialization Failed");
			e.printStackTrace();
			System.exit(1);
		}

		try {
			accts = AccountDB.load();
		} catch (Exception e) {
			System.out.print("File: " + AccountDB.acctFile + " not found. " +
			"Create it? (y/n): ");

			String ans = "n";
			try {
				ans = br.readLine();
			} catch (IOException ioe) {
				ioe.printStackTrace();
				System.exit(1); 
			}
			if (ans.equals("y")) {
				System.out.println("Creating new database");
				accts = new AccountDB();
			} else {
				System.out.println("Bye");
				System.exit(0);
			}
		}
		String ans = "n";
		do {
			try {
				System.out.println("----- Creating Account -----");
				System.out.print  ("Account owner: ");
				String owner = br.readLine();
				System.out.print  ("Please enter a pin number: ");
				String pin = br.readLine();
				System.out.print  ("Please enter ATM card filename: ");
				String filename = br.readLine();
				System.out.print  ("Starting Balance: ");
				double balance = Double.parseDouble(br.readLine());
				System.out.print  ("Creating account. Please wait");

				String acctNum = accts.getNextAcctNum();
				System.out.print(".");
				KeyPair kp = crypto.makeRSAKeyPair();
				System.out.print(".");

				Account a = new Account(balance, owner, acctNum, kp.getPublic());
				System.out.print(".");
				ATMCard card = new ATMCard(acctNum, kp.getPrivate(), pin);
				System.out.print(".");
				accts.addAccount(a);
				System.out.print(".");

				Disk.save(card, filename);
				System.out.println(". Done");
				System.out.print("Create another account? (y/n) ");
				ans = br.readLine();

			} catch (Exception e) {
				System.out.println("\nAccount creation failed");
				e.printStackTrace();
			}
		} while (ans.equals("y"));
		accts.save();
	}
}

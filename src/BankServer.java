import java.security.*;
import java.io.*;
import java.net.*;

public class BankServer {

	public static final String pubKeyFile = "bank.pub";
	public static final String keyPairFile = "bank.key";
	public static final String logFile = "bank.log";
	private static KeyPair pair = null;
	public static Log log = null;

	static {
		try {
			pair = (KeyPair)Disk.load(keyPairFile);
			log = new Log(logFile, pair.getPublic());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}


	public static void main(String[] args) {
		try {
			AccountDB accts = AccountDB.load();
			ServerSocket serverS = new ServerSocket(2100);
			System.out.println("--------------------------");
			System.out.println("  Bank Server is Running  ");
			System.out.println("--------------------------");
			while (true) {
				try {
					Socket s = serverS.accept();
					BankSession session = new BankSession(s, accts, pair);
					new Thread(session).start();
				} catch (IOException e) {
					log.write(e);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}

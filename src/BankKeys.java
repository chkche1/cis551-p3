import java.security.*;

// This program creates the bank.pub and bank.key files
public class BankKeys {
	private static Crypto crypto = new Crypto();

	public static void main(String[] args) {
		try {
			KeyPair kp = crypto.makeRSAKeyPair();
			Disk.save(kp, BankServer.keyPairFile);
			Disk.save(kp.getPublic(), BankServer.pubKeyFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

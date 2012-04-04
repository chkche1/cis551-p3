import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class LogViewer {
	public static final String pubKeyFile = "bank.pub";
	public static final String keyPairFile = "bank.key";
	public static final String logFile = "bank.log";
	private static KeyPair pair = null;
	public static Log log = null;

	static {
		try {
			pair = (KeyPair)Disk.load(keyPairFile);
			//log = new Log(logFile, pair.getPublic());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	private Crypto crypto;
	private PrivateKey kPrivBank;
	private PublicKey  kPubBank;
	private String file;

	public LogViewer(String file){
		this.file = file;
		this.crypto = new Crypto();
		this.kPrivBank = pair.getPrivate();
		this.kPubBank = pair.getPublic();	
	}

	public void read(){
		System.out.println("$$$$$Log File: "+file+"$$$$$\n");

		try {
			LinkedList<Object> l = (LinkedList<Object>)Disk.load(file);

			System.out.println("size: "+l.size());
			for(Object o : l){
				if(o instanceof List) System.out.println("o is a list");
					Transaction t = (Transaction) o;
					Double a;
					Double b;
					Date d;

					try {
						a = (Double) crypto.decryptRSA(t.amt, kPrivBank);
						b = (Double) crypto.decryptRSA(t.bal, kPrivBank);
						d= (Date) crypto.decryptRSA(t.ts, kPrivBank);
						System.out.println("===============================");
						System.out.println("Transaction Type: "+t.type);
						System.out.println("Transaction Amount: "+a);
						System.out.println("Transaction Balance: "+b);
						System.out.println("Issued At: "+d);
						System.out.println("===============================\n");
					} catch (KeyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
			} // end of for

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		System.out.println("$$$$$$$$$$$$$$$$\n");

	}// end of read

	public static void main(String[] args){
		LogViewer lv = new LogViewer("0.log");
		lv.read();
	}

}

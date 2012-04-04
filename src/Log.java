import java.security.KeyException;
import java.security.PublicKey;
import java.security.Key;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Log {

	private String file;
	private Crypto crypto;
	private PublicKey kPub;
	// You may add more state here.
	LinkedList<Transaction> trans;

	public Log(String file, PublicKey key) 
	{
		try {
			this.crypto = new Crypto();
			this.file = file;
			this.kPub = key;
			
			// Initialize the log file. Create file if it does not exist
			File f = new File(file);
			if(!f.exists()){
				f.createNewFile();
				this.trans = new LinkedList<Transaction>();
				System.out.println(file+" created.");
			}else{
				try{
					this.trans = (LinkedList<Transaction>) Disk.load(this.file);
				}catch(Exception e){
					e.printStackTrace();
					this.trans = new LinkedList<Transaction>();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void write(Serializable obj) {
		System.out.println(obj.toString());
		
		Double d = ((Transaction)obj).amount;
		byte[] a = null;
		try {
			a = crypto.encryptRSA(d, kPub);
		} catch (KeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Double d2 = ((Transaction)obj).balance;
		byte[] b = null;
		try {
			b = crypto.encryptRSA(d2, kPub);
		} catch (KeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Date d3 = ((Transaction)obj).timestamp;
		byte[] date = null;
		try {
			date = crypto.encryptRSA(d3, kPub);
		} catch (KeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Transaction t = new Transaction(((Transaction)obj).type, a, b, date);
		trans.add(t);

	} // end of write

	public void toDisk(){
		try {
			Disk.save((Serializable)trans, file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/*
	public void read(){
		System.out.println("$$$$$Log File: "+file+"$$$$$\n");

		try {
			List<Object> l = (List<Object>)Disk.read(file);

			for(Object o : l){
				
				Transaction t = (Transaction)o;
				
				
				
				System.out.println("===============================");
				System.out.println("Transaction Type: "+((Transaction)t).type);
				System.out.println("Transaction Amount: "+((Transaction)t).amount);
				System.out.println("Transaction Balance: "+((Transaction)t).balance);
				System.out.println("Issued At: "+((Transaction)t).timestamp.toString());
				System.out.println("===============================\n");
			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		System.out.println("$$$$$$$$$$$$$$$$\n");

	}// end of read
*/

} // end of Log

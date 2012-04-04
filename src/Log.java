import java.security.PublicKey;
import java.security.Key;
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
				System.out.println(file+" created.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void write(Serializable obj) {
		System.out.println(obj.toString());
		System.out.println("written to "+file);

		// Add code to store to the log file
		try {
			Disk.append(obj, file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	} // end of write

	public void read(){
		System.out.println("$$$$$Log File: "+file+"$$$$$\n");

		try {
			List<Object> l = Disk.read(file);
			for(Object t : l){
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


} // end of Log

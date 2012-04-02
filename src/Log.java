import java.security.PublicKey;
import java.security.Key;
import java.io.IOException;
import java.io.Serializable;

public class Log {

    private String file;
    private Crypto crypto;

    // You may add more state here.

    public Log(String file, PublicKey key) 
    {
	try {
	    this.crypto = new Crypto();
	    this.file = file;

	    // Initialize the log file
	    // ...

	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }

    public void write(Serializable obj) {
	System.out.println(obj.toString());

	// Add code to store to the log file
	// ...
    }

}

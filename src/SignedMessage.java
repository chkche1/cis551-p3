import java.io.*;
import java.security.*;

public class SignedMessage implements Message {
    public static final long serialVersionUID = 331004;
    byte[] msg;
    byte[] signature;

    public SignedMessage(Serializable msg, PrivateKey kPriv, Crypto crypto) 
	throws SignatureException, KeyException, IOException
    {
	this.msg = crypto.objToBytes(msg);
	this.signature = crypto.sign(this.msg, kPriv);
    }

    public Object getObject() throws IOException, ClassNotFoundException {
	ByteArrayInputStream bis = new ByteArrayInputStream(msg);
	ObjectInputStream ois = new ObjectInputStream(bis);
	return ois.readObject();
    }
}

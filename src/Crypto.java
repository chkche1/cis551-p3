import java.io.*;
import java.security.*;
import javax.crypto.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Crypto {

	public  boolean DEBUG = true;

	public  static SecureRandom rand = new SecureRandom();

	private  void debug(String s) {
		if (DEBUG) {
			System.out.println(s);
		}
	}

	public Crypto() {
		java.security.Security.addProvider(new BouncyCastleProvider());
	}


	public synchronized Key makeAESKey() {
		try {
			KeyGenerator rkg = KeyGenerator.getInstance("AES","SunJCE");
			rkg.init(128,rand);
			return rkg.generateKey();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	public synchronized KeyPair makeRSAKeyPair() {
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA","BC");
			kpg.initialize(2048,rand);
			return kpg.generateKeyPair();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	public synchronized  byte[] objToBytes(Serializable obj) 
	throws IOException
	{
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(buf);
		oos.writeObject(obj);
		return buf.toByteArray();
	}


	/* NOTE: The RSA encryptiong method will work only on objects of size <= 256 bytes */
	public synchronized  byte[] encryptRSA(Serializable obj, PublicKey kPub) 
	throws KeyException, IOException
	{
		try {
			Cipher RSACipher = Cipher.getInstance("RSA/NONE/PKCS1Padding","BC");
			return encrypt(RSACipher, obj, kPub);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	public synchronized  byte[] encryptAES(Serializable obj, Key k) 
	throws KeyException, IOException
	{
		try {
			Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding", "SunJCE");
			return encrypt(c,obj,k);
			/*
	    c.init(Cipher.ENCRYPT_MODE, k);
	    ByteArrayOutputStream bOut = new ByteArrayOutputStream();
	    CipherOutputStream cOut = new CipherOutputStream(bOut, c);
	    cOut.write(objToBytes(obj));
	    cOut.close();
	    return bOut.toByteArray();
			 */
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	public synchronized  Object decryptAES(byte[] msgE, Key k) 
	throws KeyException, IOException
	{
		try {
			Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding","SunJCE");
			return decrypt(c, msgE, k);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}


	public synchronized  Object decryptRSA(byte[] msgE, PrivateKey kPriv)
	throws KeyException, IOException
	{
		try {
			Cipher RSACipher = Cipher.getInstance("RSA/NONE/PKCS1Padding","BC");
			return decrypt(RSACipher, msgE, kPriv);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}


	private synchronized byte[] encrypt(Cipher cipher,
			Serializable obj,
			Key kPub)
	{
		try {
			debug("Initializing cipher for encryption.");
			cipher.init(Cipher.ENCRYPT_MODE,kPub);
			ByteArrayOutputStream plainbuf = new ByteArrayOutputStream();
			ObjectOutputStream plainobj = new ObjectOutputStream(plainbuf);
			plainobj.writeObject(obj);
			debug("Serialized size = " + plainbuf.toByteArray().length);	    

			ByteArrayOutputStream ciphertext = new ByteArrayOutputStream();
			CipherOutputStream cipherbuf = new CipherOutputStream(ciphertext, cipher);
			cipherbuf.write(plainbuf.toByteArray());
			cipherbuf.close();
			byte[] output = ciphertext.toByteArray();
			debug("output.length = " + output.length);
			return output;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}


	private synchronized  Object decrypt(Cipher cipher,
			byte[] msgE, 
			Key kPriv) 
	{
		try {
			debug("Initializing cipher for decryption.");
			cipher.init(Cipher.DECRYPT_MODE,kPriv);

			debug("msgE.length = " + msgE.length);	    
			ByteArrayInputStream ciphertext = new ByteArrayInputStream(msgE);
			CipherInputStream plaintext = new CipherInputStream(ciphertext, cipher);
			debug("plaintext.available() = " + plaintext.available());
			ObjectInputStream ois = new ObjectInputStream(plaintext);
			return ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}


	public synchronized  byte[] sign(byte[] msg, PrivateKey kPriv) 
	throws SignatureException, KeyException, IOException
	{
		// Initialize the signature object for signing
		debug("Initializing signature.");
		try {
			Signature RSASig = Signature.getInstance("SHA256withRSA","BC");
			debug("Using algorithm: " + RSASig.getAlgorithm());
			RSASig.initSign(kPriv);
			RSASig.update(msg);
			return RSASig.sign();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	public synchronized  boolean verify(byte[] msg, byte[] sig, PublicKey kPub) 
	throws SignatureException, KeyException
	{
		// Initialize the signature object for verifying
		debug("Initializing signature.");
		try {
			Signature RSASig = Signature.getInstance("SHA256withRSA", "BC");
			RSASig.initVerify(kPub);
			RSASig.update(msg);
			return RSASig.verify(sig);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return false;
	}

}

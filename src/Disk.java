import java.io.*;

public class Disk {

	public static Object load(String objFile) 
	throws IOException 
	{
		FileInputStream fis = new FileInputStream(objFile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		Object obj = null;
		try {
			obj = ois.readObject();
		} catch (ClassNotFoundException e) {
			System.out.println("load failed: " + e);
			System.exit(1);
		}
		ois.close();
		fis.close();
		return obj;
	}


	public static void save(Serializable obj, String objFile) 
	throws IOException
	{
		FileOutputStream fos = new FileOutputStream(objFile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(obj);
		oos.close();
		fos.close();
	}

	public static void append(Serializable obj, String objFile) 
	throws IOException
	{
		FileOutputStream fos = new FileOutputStream(objFile, true);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(obj);
		oos.close();
		fos.close();
	}


}

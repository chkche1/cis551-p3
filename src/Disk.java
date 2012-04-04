import java.io.*;
import java.util.LinkedList;
import java.util.List;

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

	public static List<Object> read(String objFile)throws IOException{
		FileInputStream fis = new FileInputStream(objFile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		LinkedList<Object> li = new LinkedList<Object>();
		try {
			//while(true){
				li.add(ois.readObject());
				li.add(ois.readObject());
			//}
		}catch(EOFException e){ 
			System.out.println("reached end");
		}catch (ClassNotFoundException e) {
			System.out.println("load failed: " + e);
			System.exit(1);
		}
		ois.close();
		fis.close();
		return li;
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
		oos.flush();
		oos.close();
		fos.close();
	}


}

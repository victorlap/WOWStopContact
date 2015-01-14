package nl.utwente.wsc.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import nl.utwente.wsc.models.WSc;

public final class FileUtils {
	
	private final static String WSC_LIST_PATH = "wsc_array_list.tmp";


	public static void saveToFile(ArrayList<WSc> list) throws IOException {
    	FileOutputStream fileOut = new FileOutputStream(WSC_LIST_PATH);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(list);
		out.close();
    }
    
    public static ArrayList<WSc> getWSCListFromFile() throws IOException, ClassNotFoundException {
    	FileInputStream fileIn = new FileInputStream(WSC_LIST_PATH);
    	ObjectInputStream in = new ObjectInputStream(fileIn);
    	ArrayList<WSc> returnlist = (ArrayList<WSc>) in.readObject();
    	in.close();
    	return returnlist;
    }

}

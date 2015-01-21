package nl.utwente.wsc.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import nl.utwente.wsc.models.ColorType;
import nl.utwente.wsc.models.WSc;
import android.content.Context;

public final class FileUtils {
	
	private final static String WSC_LIST_PATH = "wsc_array_list.tmp";

	public static boolean hasWscList(Context c) {
		return c.getFileStreamPath(WSC_LIST_PATH).exists();
	}

	public static void saveToFile(Context c, List<WSc> list) throws IOException {
		for (WSc wsc : list) {
			wsc.setConnected(false);
			wsc.setTurnedOn(false);
			wsc.setBusy(false);
			wsc.setColor(ColorType.NONE);
		}
		FileOutputStream fileOut = c.openFileOutput(WSC_LIST_PATH, Context.MODE_PRIVATE);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(list);
		out.close();
    }
    
    public static List<WSc> getWSCListFromFile(Context c) throws IOException, ClassNotFoundException {
		FileInputStream fileIn = c.openFileInput(WSC_LIST_PATH);
    	ObjectInputStream in = new ObjectInputStream(fileIn);
    	List<WSc> returnlist = (List<WSc>) in.readObject();
    	in.close();
    	return returnlist;
    }

}

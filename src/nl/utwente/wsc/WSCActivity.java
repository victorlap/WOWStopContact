package nl.utwente.wsc;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import nl.utwente.wsc.models.WSc;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

public class WSCActivity extends ListActivity {
	
	private final static String WSC_LIST_PATH = "wsc_array_list";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ArrayList<WSc> list = null;
		try {
			list = getWSCListFromFile();
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ListAdapter adapter = new ArrayAdapter<WSc>(this, android.R.layout.simple_list_item_1, list);
		
		setListAdapter(adapter);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.wsc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	switch(item.getItemId()) {
	    	case R.id.action_add_wsc:
	    		return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
    
    private void saveToFile(ArrayList<WSc> list) throws IOException {
    	FileOutputStream fileOut = openFileOutput(WSC_LIST_PATH, MODE_PRIVATE);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(list);
		out.close();
    }
    
    private ArrayList<WSc> getWSCListFromFile() throws StreamCorruptedException, IOException, ClassNotFoundException {
    	//FileInputStream fis = openFileInput(WSC_LIST_PATH);
    	//ObjectInputStream ois = new ObjectInputStream(fis);
    	//ArrayList<WSc> returnlist = (ArrayList<WSc>) ois.readObject();
    	//ois.close();
    	ArrayList<WSc> list = new ArrayList<WSc>();
    	list.add(new WSc("Televisie", "192.168.0.1", 1337));
    	list.add(new WSc("Bureaulamp", "192.168.0.54", 1337));
    	return list;
    }

}

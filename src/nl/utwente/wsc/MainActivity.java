package nl.utwente.wsc;

import java.io.IOException;
import java.util.List;

import nl.utwente.wsc.models.WSc;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


public class MainActivity extends ListActivity {
	
	private static final String TAG = "MainActivity";
	
	public static final int DEFAULT_PORTNUMBER = 7331;
	
	private SocketClientManager manager;
	List<WSc> list;
	
	private ArrayAdapter<WSc> adapter;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startSocketManager();
        list = manager.getDevices();
        adapter = new WscAdapter(this, list, manager);

        setListAdapter(adapter);
    }
		
	@Override
    public void onBackPressed() {
    	super.onBackPressed();
    	// TODO pause client update list
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	stopSocketManager();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	switch(item.getItemId()) {
	    	case R.id.action_add_wsc:
	    		showAddWscDialog();
	    		return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }

    private void startSocketManager() {
    	try {
			manager = new SocketClientManager(this);
		} catch (IOException e) {
			e.printStackTrace();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {}
			System.exit(20);
		}
    }
    
    private void stopSocketManager() {
    	manager.stop();
    }
    
    public void updateList() {
		list = manager.getDevices();
		runOnUiThread(new Runnable() {
    	    public void run() {
    	        adapter.notifyDataSetChanged();
    	    }
    	});
    }
    
    private void showAddWscDialog() {
    	AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Add WSc");
        
    	LinearLayout layout = new LinearLayout(this);
    	layout.setOrientation(LinearLayout.VERTICAL);

    	final EditText nameBox = new EditText(this);
    	nameBox.setHint("Name");
    	nameBox.requestFocus();
    	layout.addView(nameBox);

    	final EditText hostnameBox = new EditText(this);
    	hostnameBox.setHint("Hostname");
    	layout.addView(hostnameBox);
    	
    	final EditText portBox = new EditText(this);
    	portBox.setHint("Port");
    	portBox.setText(String.valueOf(DEFAULT_PORTNUMBER));
    	portBox.setInputType(EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
    	layout.addView(portBox);

    	dialog.setView(layout);
    	
    	dialog.setPositiveButton("Add", new OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String name = nameBox.getText().toString();
				String host = hostnameBox.getText().toString();
				
				int port;
				try {
					port = Integer.parseInt(portBox.getText().toString());
				} catch (NumberFormatException e) {
					port = DEFAULT_PORTNUMBER;
				}
				if(name != null && host != null) {
					
					WSc wsc = new WSc(nameBox.getText().toString(), hostnameBox.getText().toString(), Integer.parseInt(portBox.getText().toString()));
					manager.addDevice(wsc);
					updateList();
					dialog.dismiss();
				} else {
					Toast.makeText(getApplication(), "Please fill in correct values", Toast.LENGTH_SHORT).show();
				}
			}
		});
    	
    	dialog.setNegativeButton("Cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
    	
    	dialog.show();
    }
      
    public void toastMessage(final Context context, final String message, final boolean displayLong) {
    	runOnUiThread(new Runnable() {
    	    public void run() {
    	        Toast.makeText(context, message, displayLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    	    }
    	});
    }
}

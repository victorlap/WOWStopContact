package nl.utwente.wsc;

import java.io.IOException;

import nl.utwente.wsc.models.WSc;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.Toast;


public class MainActivity extends ListActivity {
	
	private static final String TAG = "WSc";
	
	public static final String EXTRA_INETADDRESS = "extra_inetaddress";
	public static final String EXTRA_PORTNUMBER = "extra_portnumber";
	public static final int DEFAULT_PORTNUMBER = 7331;
	
	private SocketClientManager manager;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startSocketManager();
        //ListAdapter adapter = new ArrayAdapter<WSc>(this, android.R.layout.simple_list_item_1, list);
		
		//setListAdapter(adapter);
    }
		
	@Override
    public void onBackPressed() {
    	super.onBackPressed();
    	// pause client update list
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
    
    private void showAddWscDialog() {
    	AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Add WSc");
        
    	LinearLayout layout = new LinearLayout(this);
    	layout.setOrientation(LinearLayout.VERTICAL);

    	final EditText nameBox = new EditText(this);
    	nameBox.setHint("Name");
    	layout.addView(nameBox);

    	final EditText hostnameBox = new EditText(this);
    	hostnameBox.setHint("Hostname");
    	layout.addView(hostnameBox);
    	
    	final EditText portBox = new EditText(this);
    	portBox.setHint("Port");
    	layout.addView(portBox);

    	dialog.setView(layout);
    	
    	dialog.setPositiveButton("Add", new OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				manager.addDevice(new WSc(nameBox.getText().toString(), 
						hostnameBox.getText().toString(), 
						Integer.parseInt(portBox.getText().toString())));
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

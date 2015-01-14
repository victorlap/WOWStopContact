package nl.utwente.wsc;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import nl.utwente.wsc.communication.OnSocManagerTaskCompleted;
import nl.utwente.wsc.communication.SocketClient;
import nl.utwente.wsc.communication.ValueType;
import nl.utwente.wsc.models.WSc;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
	
	private OnSocManagerTaskCompleted callback;
	
	private String mInetAddress;
	private int mPortNumber;
	
	//private List<SocManagerClient> outlets; TODO should be like this
	private List<SocketClient> outlets;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupCallbackListener();
        startSocketManager();
        
        ArrayList<WSc> list = new ArrayList<WSc>(manager.getAll);
        ListAdapter adapter = new ArrayAdapter<WSc>(this, android.R.layout.simple_list_item_1, list);
		
		setListAdapter(adapter);
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
    
    private void setupCallbackListener() {
    	final MainActivity ref = this;
        callback = new OnSocManagerTaskCompleted() {			      	
        	@Override
			public void doneTask(InetAddress address, ValueType type, Object value) {
				try {
					if (type.equals(ValueType.CONNECTING)) {	
						toastMessage(ref, "Succes! (" + InetAddress.getByName(mInetAddress) + 
								":" + mPortNumber, false);
						//TODO enable 
					}
				} catch (IOException e) {
					Log.e(TAG, e.getLocalizedMessage());
				}
				
			}
		};		
	}
    
    /*private void stopSocketManager() {
    	if (outlet != null) {
			outlet.disconnect();
			outlet = null;
			toastMessage(this, "Stopped client succesfully", false);
    	}
    }*/
    
    private void startSocketManager() {
    	/*if (outlet != null) {
    		return;
    	}*/
    	Intent i = getIntent();
    	mInetAddress = "130.89.236.220";//i.getStringExtra(EXTRA_INETADDRESS);
    	mPortNumber = 7331;//i.getIntExtra(EXTRA_PORTNUMBER, DEFAULT_PORTNUMBER);
    	final MainActivity ref = this;
    	/*Thread thread = new Thread(new Runnable() {		
			@Override
			public void run() {
		    	try {
					outlet = new SocketClient(ref, callback);
					outlet.connect(InetAddress.getByName(mInetAddress), mPortNumber, 10000);
		        } catch (UnknownHostException e) {
		        	toastMessage(ref, "No WSc could be found on this hostname", false);
					Log.e(TAG, e.getMessage());			
		        } catch (IOException e) {
		        	toastMessage(ref, "Something went wrong, please try again later or check the hostname and port", false);
					Log.e(TAG, e.getMessage());			
				}		
			}
		});
    	thread.setDaemon(true);
    	thread.start();*/
    }
    
    private void toastMessage(final Context context, final String message, final boolean displayLong) {
    	runOnUiThread(new Runnable() {
    	    public void run() {
    	        Toast.makeText(context, message, displayLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
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
				// TODO
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
}

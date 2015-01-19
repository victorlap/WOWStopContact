package nl.utwente.wsc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import nl.utwente.wsc.SocketClientManager.SCMCallback;
import nl.utwente.wsc.models.WSc;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class MainActivity extends ListActivity implements SCMCallback {
	
	private static final String TAG = "MainActivity";
	
	public static final int    DEFAULT_PORTNUMBER = 7331;
	public static final String EXTRA_WSC = "extra_wsc";
	public static String BASE_IP;
	
	private SocketClientManager manager;
	List<WSc> list;
	
	private ToggleButton toggle_devices;
	
	private WscAdapter adapter;
	
	private boolean wasPaused;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //BASE_IP = getBaseIP();        
        startSocketManager();
        list = manager.getDevices();
        adapter = new WscAdapter(this, list, manager, this);

        setListAdapter(adapter);
        
        toggle_devices = (ToggleButton) findViewById(R.id.allDevicesToggle);
        toggle_devices.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toggle_devices.setChecked(false);
				toggle_devices.setEnabled(false);
				manager.setDevicesState(false);
			}
		});
        
        buildGraph();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	if (wasPaused) {
    		startSocketManager();
    	}
        //BASE_IP = getBaseIP();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	wasPaused = true;
    	stopSocketManager();
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
    
    private String getBaseIP() {
    	String[] parts;
		try {
			parts = InetAddress.getLocalHost().toString().split("\\.");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return "";
		}
    	return parts[0] + "." + parts[1] + ".";
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
    	manager.getDevicesValues();
    	switch(item.getItemId()) {
    		case R.id.action_refresh:
    			startSocketManager();
    			return true;
	    	case R.id.action_add_wsc:
	    		showAddWscDialog();
	    		return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }

    private void startSocketManager() {
    	try {
			manager = new SocketClientManager(this, this);
			manager.connect();
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
    	    	adapter.updateList(manager.getDevices());
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
				
				int port = -1;
				try {
					port = Integer.parseInt(portBox.getText().toString());
				} catch (NumberFormatException e) {}
				if(port != -1 && name != null && !name.equals("") && host != null && !host.equals("")) {
					
					WSc wsc = new WSc(nameBox.getText().toString(), hostnameBox.getText().toString(), port);
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
    
    public void setToggleDevices(boolean enabled) {
    	toggle_devices.setEnabled(enabled);
    }
    
    public void buildGraph() {
    	GraphView graph = (GraphView) findViewById(R.id.graph);
    	LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
    	          new DataPoint(0, 1),
    	          new DataPoint(1, 5),
    	          new DataPoint(2, 3),
    	          new DataPoint(3, 2),
    	          new DataPoint(4, 6)
    	});
    	series.setDrawBackground(true);
    	graph.addSeries(series);
    }
}

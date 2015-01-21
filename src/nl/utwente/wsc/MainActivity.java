package nl.utwente.wsc;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import nl.utwente.wsc.SocketClientManager.SCMCallback;
import nl.utwente.wsc.communication.SocketClient;
import nl.utwente.wsc.models.WSc;
import nl.utwente.wsc.utils.Tools;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;


public class MainActivity extends ListActivity implements SCMCallback {
	
	private boolean devMode = false;

	public static final int    DEFAULT_PORTNUMBER = 7331;
	public static final String EXTRA_WSC = "extra_wsc";

	public static String BASE_IP;

	private SocketClientManager manager;
	List<WSc> list;

	private ToggleButton toggle_devices;

	private WscAdapter adapter;

	private boolean wasPaused;
	private boolean deactivated = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//BASE_IP = getBaseIP();        
		startSocketManager();
		if (Tools.updated != null) {
			manager.updateDevice(Tools.updated);
			manager.save();
			Tools.updated = null;
		}
		if (Tools.removed != null) {
			manager.removeDevice(Tools.removed.getHostname(), false);
			manager.save();
			Tools.removed = null;
		}		
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
				updateList();
			}
		});

		buildGraph();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (wasPaused) {
        	manager.resume();
    		refreshDevices();
		}
		//BASE_IP = getBaseIP();
	}

	@Override
	protected void onPause() {
		super.onPause();
    	manager.pauze();
		wasPaused = true;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
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
    	//TODOmanager.getDevicesValues();
		switch(item.getItemId()) {
		case R.id.action_refresh:
    			refreshDevices();
			return true;
		case R.id.action_add_wsc:
			showAddWscDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
    }
    
    private void refreshDevices() {
		for (Entry<WSc, SocketClient> entry : manager.getEntries()) {
			entry.getKey().setBusy(true);
			updateList();    	
			if (entry.getKey().isConnected()) {
				entry.getValue().socketIsOn();
				entry.getValue().getSocketColor();
			} else {
				try {
					entry.getValue().connect(entry.getKey());
				} catch (UnknownHostException e) {}
			}			
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
		dialog.setTitle("Add Device");

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);

		final EditText nameBox = new EditText(this);
		nameBox.setHint("Name");
		nameBox.requestFocus();
		layout.addView(nameBox);

		final EditText hostnameBox = new EditText(this);
		hostnameBox.setHint("IP-address");
		layout.addView(hostnameBox);

		EditText tempPortBox = null;
		if (devMode) {
			tempPortBox = new EditText(this);
			tempPortBox.setHint("Port");
			tempPortBox.setText(String.valueOf(DEFAULT_PORTNUMBER));
			tempPortBox.setInputType(EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
			layout.addView(tempPortBox);
		}
		final EditText portBox = tempPortBox;
		dialog.setView(layout);

		dialog.setPositiveButton("Add", new OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String name = nameBox.getText().toString();
				String host = hostnameBox.getText().toString();
				
				int port;
				if (devMode) {
					port = -1;
					try {
						port = Integer.parseInt(portBox.getText().toString());
					} catch (NumberFormatException e) {}
				} else {
					port = DEFAULT_PORTNUMBER;
				}			
				
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
		final GraphView graph = (GraphView) findViewById(R.id.graph);
		final LinearLayout box = (LinearLayout) findViewById(R.id.hoverbox);
		box.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				box.setVisibility(View.GONE);
			}
		});
		graph.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (box.getVisibility() == View.GONE) {
					box.setVisibility(View.VISIBLE);
				} else {
					box.setVisibility(View.GONE);
				}
			}
		});
		int n = manager.getDevices().size();
		Tools.buildGraph(10*n, 50*n, this, graph, true);
		buildText(n);
	}

	private void buildText(int n) {
		TextView tv1 = (TextView) findViewById(R.id.textView1);
		TextView tv2 = (TextView) findViewById(R.id.textView2);
		TextView tv3 = (TextView) findViewById(R.id.textView3);
		TextView tv4 = (TextView) findViewById(R.id.textView4);

		Random r = new Random();
		DecimalFormat f = new DecimalFormat("#");
		if(n > 0) {
			tv1.setText("Powered on for: "+ f.format(rand(n*2, n*12, r)) +" hours");
			tv2.setText("Current power draw: "+ f.format(rand(n*10, n*50, r)) +"W");
		} else {
			tv1.setText("There are no devices currently on");
			tv2.setText("Current power draw: None");
		}
		double w = rand(0, n*1, r);
		f = new DecimalFormat("#.#");
		DecimalFormat eu = new DecimalFormat("0.00");
		tv3.setText("Daily power draw: "+ f.format(w) +"kWh ~ €"+ eu.format(w*0.23));
		f = new DecimalFormat("#");
		tv4.setText("Yearly estimate: "+ f.format(w*365) +"kWh ~ €"+ eu.format(w*365*0.23));

	}

	private double rand(int min, int max, Random random) {
		return min + (max - min) * random.nextDouble();
	}
}
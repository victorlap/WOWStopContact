package nl.utwente.wsc;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

import org.joda.time.DateTime;

import nl.utwente.wsc.SocketClientManager.SCMCallback;
import nl.utwente.wsc.models.WSc;
import nl.utwente.wsc.utils.Tools;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;

public class WscActivity extends ActionBarActivity implements SCMCallback {

	private WSc wsc;
	private SocketClientManager manager;
	private boolean createdMenu = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wsc);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		
		Intent i = getIntent();
		wsc = (WSc) i.getSerializableExtra(MainActivity.EXTRA_WSC);

		setTitle(wsc.getName());
		
		GraphView graph = (GraphView) findViewById(R.id.graph);
		graph.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				ProgressBar pb = (ProgressBar) findViewById(R.id.progress);
				pb.setVisibility(View.VISIBLE);
				buildGraph();				
			}
		});
		startSocketManager();
		if (!wsc.isConnected()) {
			buildGraph();
		}
		createdMenu = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.wsc, menu);
		if (!createdMenu) {
			createdMenu = true;
			new Handler().post(new Runnable() {
				@Override
				public void run() {
					ToggleButton tb = (ToggleButton) findViewById(R.id.switchForActionBar);
					if(tb != null) {
						tb.setOnClickListener(new OnClickListener() {					
							@Override
							public void onClick(View v) {
								toggle_wsc();
							}
						});
					}
				}
			});
		}

		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId()) {
		case R.id.action_edit_wsc:
			showEditWscDialog();
			return true;
		case R.id.action_remove_wsc:
			remove();
			return true;
		case android.R.id.home:
			manager.pauze();
			NavUtils.navigateUpFromSameTask(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void toggle_wsc() {
		runOnUiThread(new Runnable() {
			public void run() {
				ToggleButton tb = (ToggleButton) findViewById(R.id.switchForActionBar);
				ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressForActionBar);
				tb.setEnabled(false);	
				progressBar.setVisibility(View.VISIBLE);
				wsc.setBusy(true);
				manager.setDeviceState(wsc, tb.isChecked());	
				invalidateOptionsMenu();
			}
		});
	}

	public void startSocketManager() {
		if(manager == null) {
			try {
				manager = new SocketClientManager(this, this);
				manager.connect(wsc);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		manager.resume();
	}

	@Override 
	protected void onPause() {
		manager.pauze();
		super.onPause();
	}
	
	@Override
	public void onBackPressed() {
		manager.pauze();
		super.onBackPressed();
	}

	@Override
	protected void onStop() {
		manager.stop();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		manager.stop();
		super.onDestroy();
	}

	private void remove() {
		manager.removeDevice(wsc.getHostname(), true);
		manager.stop();
		Tools.removed = wsc;
		Toast.makeText(this, "WSc "+ wsc.getName() +" removed", Toast.LENGTH_SHORT).show();
		manager.pauze();
		NavUtils.navigateUpFromSameTask(this);
	}

	private void showEditWscDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("Edit device");

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);

		final EditText nameBox = new EditText(this);
		nameBox.setText(wsc.getName());
		nameBox.setHint("Name");
		nameBox.requestFocus();
		layout.addView(nameBox);

		dialog.setView(layout);

		dialog.setPositiveButton("Edit", new android.content.DialogInterface.OnClickListener() {		
			@Override
			public void onClick(android.content.DialogInterface dialog, int which) {
				String name = nameBox.getText().toString();

				if(name != null && !name.equals("")) {
					wsc.setName(name);
					setTitle(wsc.getName());
					manager.updateDevice(wsc);
					manager.save();
					Tools.updated = wsc;
					dialog.dismiss();
				} else {
					Toast.makeText(getApplication(), "Please fill in a correct name", Toast.LENGTH_SHORT).show();
				}
			}
		});

		dialog.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {

			@Override
			public void onClick(android.content.DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}
	
	public void buildGraph() {
		GraphView graph = (GraphView) findViewById(R.id.graph);		
		if (MainActivity.FAKE_DATA) {
			Tools.buildGraphRandom(10, 50, this, graph, true);
			buildFakeText();
		} else {
			Tools.buildGraphReal(this, graph, wsc);
			buildRealText(wsc);
		}
		ProgressBar pb = (ProgressBar) findViewById(R.id.progress);
		pb.setVisibility(View.GONE);
	}
	
	private void buildRealText(WSc wscs) {	
		TextView tv1 = (TextView) findViewById(R.id.textView1);
		TextView tv2 = (TextView) findViewById(R.id.textView2);
		TextView tv3 = (TextView) findViewById(R.id.textView3);
		TextView tv4 = (TextView) findViewById(R.id.textView4);
		
		DateTime now = DateTime.now();
		double poweredOnMilis = wsc.getPowerOnTime();
		double currentPowerDraw = wsc.getCurrentPowerDraw();
		double dailyUsage = wsc.getDailyUsage(now);
		double yearlyEstimate = wsc.getYearlyEstimate(now);

		DecimalFormat f = new DecimalFormat("#.##");
		if(wsc.isConnected() && wsc.isTurnedOn()) {
			tv1.setText("Powered on for: " + ((poweredOnMilis < 60000) ? ((int)(poweredOnMilis / 1000) + " secs.") : (poweredOnMilis < 3600000) ? ((int)(poweredOnMilis / 60000) + " min.") : ((int)(poweredOnMilis / 3600000) + " hours")));
			tv2.setText("Current power draw: "+ f.format(currentPowerDraw) + " watt");
		} else {
			if (!wsc.isConnected()) {
				tv1.setText("The device is currently not connected");
				tv2.setText("Current power draw: unknown");
			} else {
				tv1.setText("The device is currently powered off");
				tv2.setText("Current power draw: 0.00 watt");
			}
		}
		f = new DecimalFormat("0.000");
		DecimalFormat eu = new DecimalFormat("0.00");
		tv3.setText("Daily power draw: "+ f.format(dailyUsage) +" kWh ~ €"+ eu.format(dailyUsage*0.23));
		f = new DecimalFormat("#");
		tv4.setText("Yearly estimate: "+ f.format(yearlyEstimate) +" kWh ~ €"+ eu.format(yearlyEstimate*0.23));
	}
	
	private void buildFakeText() {
		TextView tv1 = (TextView) findViewById(R.id.textView1);
		TextView tv2 = (TextView) findViewById(R.id.textView2);
		TextView tv3 = (TextView) findViewById(R.id.textView3);
		TextView tv4 = (TextView) findViewById(R.id.textView4);
		
		Random r = new Random();
		DecimalFormat f = new DecimalFormat("#");
		if(wsc.isTurnedOn()) {
			tv1.setText("Powered on for: "+ f.format(rand(2, 12, r)) +" hours");
			tv2.setText("Current power draw: "+ f.format(rand(10, 50, r)) +"W");
		} else {
			tv1.setText("Not currently powered on");
			tv2.setText("Current power draw: 0.00 watt");
		}
		double w = rand(0, 1, r);
		f = new DecimalFormat("#.#");
		DecimalFormat eu = new DecimalFormat("0.00");
		tv3.setText("Daily power draw: "+ f.format(w) +"kWh ~ €"+ eu.format(w*0.23));
		f = new DecimalFormat("#");
		tv4.setText("Yearly estimate: "+ f.format(w*365) +"kWh ~ €"+ eu.format(w*365*0.23));

	}
	
	private double rand(int min, int max, Random random) {
		return min + (max - min) * random.nextDouble();
	}
	
	@Override
	public void updateList(final boolean updateGraph) {
		final WSc wscRef = wsc; 
		if (wsc == null) {
			return;
		}
		runOnUiThread(new Runnable() {		
			@Override
			public void run() {
				if (updateGraph) {
					buildGraph();
				}
				ToggleButton tb = (ToggleButton) findViewById(R.id.switchForActionBar);
				ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressForActionBar);
				try {
					tb.setEnabled(wscRef.isConnected() && !wscRef.isBusy());
					tb.setChecked(wscRef.isTurnedOn());
					progressBar.setVisibility(wscRef.isBusy() ? View.VISIBLE : View.GONE);
					invalidateOptionsMenu();
				} catch (Exception e) {
					@SuppressWarnings("unused")
					int i = 0;
				}
			}
		});
	}

	@Override
	public void toastMessage(final Context context, final String message,
			final boolean displayLong) {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(context, message, displayLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
			}
		});
	}

}

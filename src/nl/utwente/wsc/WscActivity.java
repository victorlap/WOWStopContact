package nl.utwente.wsc;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;

import nl.utwente.wsc.SocketClientManager.SCMCallback;
import nl.utwente.wsc.models.WSc;
import nl.utwente.wsc.utils.Tools;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;

public class WscActivity extends Activity implements SCMCallback {

	private WSc wsc;
	private SocketClientManager manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wsc);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		Intent i = getIntent();
		wsc = (WSc) i.getSerializableExtra(MainActivity.EXTRA_WSC);

		setTitle(wsc.getName());

		startSocketManager();

		buildGraph();

		buildText();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.wsc, menu);

		new Handler().post(new Runnable() {
			@Override
			public void run() {
				updateList();
			}
		});

		return true;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(android.R.anim.fade_out, android.R.anim.fade_in);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId()) {
		case R.id.action_toggle_wsc:
			toggle_wsc();
			return true;
		case R.id.action_edit_wsc:
			showEditWscDialog();
			return true;
		case R.id.action_remove_wsc:
			remove();
			return true;
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			overridePendingTransition(android.R.anim.fade_out, android.R.anim.fade_in);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void toggle_wsc() {
		ToggleButton tb = (ToggleButton) findViewById(R.id.switchForActionBar);
		ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressForActionBar);
		tb.setEnabled(false);	
		progressBar.setVisibility(View.VISIBLE);
		wsc.setBusy(true);
		manager.setDeviceState(wsc, tb.isChecked());	
		invalidateOptionsMenu();
	}

	public void startSocketManager() {
		if(manager == null) {
			try {
				manager = new SocketClientManager(this, this);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		startSocketManager();
	}

	@Override 
	protected void onPause() {
		manager.save();
		super.onPause();
	}

	@Override
	protected void onStop() {
		manager.save();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		manager.save();
		super.onDestroy();
	}

	private void remove() {
		manager.removeDevice(wsc.getHostname());
		Toast.makeText(this, "WSc "+ wsc.getName() +" removed", Toast.LENGTH_SHORT).show();
		manager.save();
		finish();
		overridePendingTransition(android.R.anim.fade_out, android.R.anim.fade_in);
	}

	private void showEditWscDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("Edit WSc");

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);

		final EditText nameBox = new EditText(this);
		nameBox.setText(wsc.getName());
		nameBox.setHint("Name");
		nameBox.requestFocus();
		layout.addView(nameBox);

		dialog.setView(layout);

		dialog.setPositiveButton("Edit", new OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String name = nameBox.getText().toString();

				if(name != null && !name.equals("")) {

					wsc.setName(name);
					setTitle(wsc.getName());
					manager.updateDevice(wsc);
					dialog.dismiss();
				} else {
					Toast.makeText(getApplication(), "Please fill in a correct name", Toast.LENGTH_SHORT).show();
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

	private void buildGraph() {
			ProgressBar pb = (ProgressBar) findViewById(R.id.progress);
			pb.setVisibility(View.GONE);
			GraphView graph = (GraphView) findViewById(R.id.graph);
			Tools.buildGraph(10, 50, this, graph);
	}
	
	private void buildText() {
		TextView tv1 = (TextView) findViewById(R.id.textView1);
		TextView tv2 = (TextView) findViewById(R.id.textView2);
		TextView tv3 = (TextView) findViewById(R.id.textView3);
		TextView tv4 = (TextView) findViewById(R.id.textView4);
		
		Random r = new Random();
		DecimalFormat f = new DecimalFormat("#");
		if(wsc.isTurnedOn()) {
			tv1.setText("Powerd on for: "+ f.format(rand(2, 12, r)) +" hours");
			tv2.setText("Current power draw: "+ f.format(rand(10, 50, r)) +"W");
		} else {
			tv1.setText("Not currently powered on");
			tv2.setText("Current power draw: None");
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
	public void updateList() {
		ToggleButton tb = (ToggleButton) findViewById(R.id.switchForActionBar);
		ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressForActionBar);	
		tb.setEnabled(wsc.isConnected() && !wsc.isBusy());
		tb.setChecked(wsc.isTurnedOn());
		progressBar.setVisibility(wsc.isBusy() ? View.VISIBLE : View.GONE);
		invalidateOptionsMenu();
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

package nl.utwente.wsc;

import nl.utwente.wsc.models.WSc;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class WscActivity extends Activity {
	
	private WSc wsc;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wsc);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        Intent i = getIntent();
        wsc = (WSc) i.getSerializableExtra(MainActivity.EXTRA_WSC);
        
        setTitle(wsc.getName());
        
        buildGraph();
        
    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.wsc, menu);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
              set_toggle();
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
	
	private void set_toggle() {
		ToggleButton tb = (ToggleButton) findViewById(R.id.switchForActionBar);
		tb.setEnabled(wsc.isConnected() && wsc.isTurnedOn());
	}

	private void toggle_wsc() {
		// TODO Auto-generated method stub
		
	}

	private void remove() {
		// TODO Auto-generated method stub
		
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
					// TODO manager.updateDevice(wsc);
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
	
	public void buildGraph() {
    	GraphView graph = (GraphView) findViewById(R.id.graph);
    	LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
    	          new DataPoint(0, 6),
    	          new DataPoint(1, 2),
    	          new DataPoint(2, 3),
    	          new DataPoint(3, 5),
    	          new DataPoint(4, 1)
    	});
    	series.setDrawBackground(true);
    	graph.addSeries(series);
    }

}

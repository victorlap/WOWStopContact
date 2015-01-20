package nl.utwente.wsc;

import java.util.List;

import nl.utwente.wsc.models.WSc;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class WscAdapter extends ArrayAdapter<WSc> {
	
	private List<WSc> objects;
	private SocketClientManager manager;
	private final MainActivity mainActivity;

	public WscAdapter(Context context, List<WSc> objects, SocketClientManager manager, MainActivity mainActivity) {
		super(context, R.layout.listitem_wsc, objects);
		this.objects = objects;
		this.manager = manager;
		this.mainActivity = mainActivity;
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// first check to see if the view is null. if so, we have to inflate it.
		// to inflate it basically means to render, or show, the view.
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.listitem_wsc, null, false);
		}
		
		final WSc wsc = objects.get(position);
		
		if(wsc != null) {
			
			final TextView name = (TextView) convertView.findViewById(R.id.wsc_name);
			name.setText(wsc.toString());
			name.setTextColor(!wsc.isConnected() ? Color.GRAY : Color.BLACK);
			name.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (!wsc.isBusy()) {
						Intent i = new Intent(mainActivity, WscActivity.class);
						i.putExtra(MainActivity.EXTRA_WSC, wsc);
						mainActivity.startActivity(i);
					}
				}
			});
			
			final ToggleButton toggleButton = (ToggleButton) convertView.findViewById(R.id.toggleButton);
			toggleButton.setChecked(wsc.isTurnedOn());
			toggleButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					boolean turnOn = toggleButton.isChecked();
					manager.setDeviceState(wsc, turnOn);
					wsc.setTurnedOn(turnOn);
					mainActivity.updateList();
				}
			});
			
			final ImageView powerImage = (ImageView) convertView.findViewById(R.id.powerImage);
			if (!wsc.isTurnedOn()) {
				powerImage.setImageResource(R.drawable.ic_color_none);
			} else {
				switch(wsc.getColor()) {
					case RED:
						powerImage.setImageResource(R.drawable.ic_color_red);
						break;
					case ORANGE:
						powerImage.setImageResource(R.drawable.ic_color_orange);
						break;
					case GREEN:
						powerImage.setImageResource(R.drawable.ic_color_green);
						break;
					case BLUE:
						powerImage.setImageResource(R.drawable.ic_color_blue);
						break;
					case NONE:
						powerImage.setImageResource(R.drawable.ic_color_none);
						break;
				}
			}
			
			final ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);			
			if(wsc.isBusy()) {
				progressBar.setVisibility(View.VISIBLE);
				toggleButton.setEnabled(false);
			} else {
				toggleButton.setEnabled(wsc.isConnected());
				progressBar.setVisibility(View.GONE);
			}
			
		}
		
		return convertView;

	}

	public void updateList(List<WSc> devices) {
		boolean oneDeviceOn = false;
		for(WSc wsc : devices) {
			if(wsc.isConnected() && wsc.isTurnedOn()) {
				oneDeviceOn = true;
			}
		}
		mainActivity.setToggleDevices(oneDeviceOn);
		clear();
		addAll(devices);
		notifyDataSetChanged();
	}

}

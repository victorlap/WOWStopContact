package nl.utwente.wsc;

import java.util.List;

import nl.utwente.wsc.models.WSc;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class WscAdapter extends ArrayAdapter<WSc> {
	
	private List<WSc> objects;
	private SocketClientManager manager;

	public WscAdapter(Context context, List<WSc> objects, SocketClientManager manager) {
		super(context, R.layout.listitem_wsc, objects);
		this.objects = objects;
		this.manager = manager;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// first check to see if the view is null. if so, we have to inflate it.
		// to inflate it basically means to render, or show, the view.
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.listitem_wsc, null);
		}
		
		final TextView name = (TextView) convertView.findViewById(R.id.wsc_name);
		final ImageView powerImage = (ImageView) convertView.findViewById(R.id.powerImage);
		final ToggleButton toggleButton = (ToggleButton) convertView.findViewById(R.id.toggleButton);
		
		final WSc wsc = objects.get(position);
		
		if(wsc != null) {
			
			name.setText(wsc.toString());
			
			toggleButton.setChecked(wsc.isTurnedOn());
			toggleButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					wsc.setTurnedOn(toggleButton.isChecked());
					manager.setDeviceState(wsc, toggleButton.isChecked());
					updateWscColor(wsc, powerImage);
				}
			});
			
			updateWscColor(wsc, powerImage);
			
			toggleButton.setEnabled(wsc.isConnected());
			
		}
		
		return convertView;

	}
	
	private void updateWscColor(WSc wsc, ImageView powerImage) {
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
			case NONE:
				powerImage.setImageResource(R.drawable.ic_color_none);
				break;
		}
	}

}

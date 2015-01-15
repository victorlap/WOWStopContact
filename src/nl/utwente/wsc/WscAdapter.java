package nl.utwente.wsc;

import java.util.List;

import nl.utwente.wsc.models.WSc;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class WscAdapter extends ArrayAdapter<WSc> {
	
	public List<WSc> objects;

	public WscAdapter(Context context, List<WSc> objects) {
		super(context, R.layout.listitem_wsc, objects);
		this.objects = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// first check to see if the view is null. if so, we have to inflate it.
		// to inflate it basically means to render, or show, the view.
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.listitem_wsc, null);
		}
		
		TextView name = (TextView) convertView.findViewById(R.id.wsc_name);
		ImageView powerImage = (ImageView) convertView.findViewById(R.id.powerImage);
		ToggleButton toggleButton = (ToggleButton) convertView.findViewById(R.id.toggleButton);
		
		WSc wsc = objects.get(position);
		
		if(wsc != null) {
			
			name.setText(wsc.toString());
			toggleButton.setChecked(wsc.isTurnedOn());
			
		}
		
		return convertView;

	}

}

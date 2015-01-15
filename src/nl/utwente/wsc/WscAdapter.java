package nl.utwente.wsc;

import java.util.List;

import nl.utwente.wsc.models.WSc;
import android.content.Context;
import android.widget.ArrayAdapter;

public class WscAdapter extends ArrayAdapter<WSc> {

	public WscAdapter(Context context, List<WSc> objects) {
		super(context, android.R.layout.simple_list_item_1, objects);
		// TODO Auto-generated constructor stub
	}

}

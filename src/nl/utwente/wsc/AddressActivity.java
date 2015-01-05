package nl.utwente.wsc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddressActivity extends Activity {
	
	private EditText mInetAddressText;
	private EditText mPortnumberText;
	private Button   mInetAddressButton;
	private Context  mContext = this;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);
        
        mInetAddressText = (EditText) findViewById(R.id.inetaddress);
        mPortnumberText = (EditText) findViewById(R.id.portnumber);
        mInetAddressButton = (Button) findViewById(R.id.button_inetaddress);
        
        setDefaultText();
        
        mInetAddressButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int portnumber = MainActivity.DEFAULT_PORTNUMBER;
				try {
					portnumber = Integer.parseInt(mPortnumberText.getText().toString());
				} catch(NumberFormatException e) {
					Toast.makeText(mContext, "Not a valid port number", Toast.LENGTH_SHORT).show();
					Log.e("WSc", e.getMessage());
					return;
				}
				
				Intent intent = new Intent(mContext, MainActivity.class);
				intent.putExtra(MainActivity.EXTRA_INETADDRESS, mInetAddressText.getText().toString());
				intent.putExtra(MainActivity.EXTRA_PORTNUMBER,  portnumber);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intent);
				finish();
			}
		});
    }
	
	private void setDefaultText() {
		String inetaddress;
		Intent i = getIntent();
		if((inetaddress = i.getStringExtra(MainActivity.EXTRA_INETADDRESS)) != null) {
			mInetAddressText.setText(inetaddress);
		}
		int portnumber = i.getIntExtra(MainActivity.EXTRA_PORTNUMBER, MainActivity.DEFAULT_PORTNUMBER);
		mPortnumberText.setText(String.valueOf(portnumber));
	}

}

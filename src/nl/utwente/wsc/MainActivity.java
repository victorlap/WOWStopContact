package nl.utwente.wsc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import nl.utwente.wsc.communication.SocManagerClient;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends Activity {
	
	public static final String EXTRA_INETADDRESS = "extra_inetaddress";
	public static final String EXTRA_PORTNUMBER = "extra_portnumber";
	public static final int DEFAULT_PORTNUMBER = 7331;
	
	private String mInetAddress;
	private int mPortNumber;
	
	private SocManagerClient mSocketManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        try {
        	startSocketManager();
        } catch (UnknownHostException e) {
        	Toast.makeText(this, "No WSc could be found on this hostname", Toast.LENGTH_SHORT).show();
			Log.e("WSc", e.getMessage());
			
			backToAddress();
        } catch (IOException e) {
        	Toast.makeText(this, "Something went wrong, please try again later or check the hostname and port", Toast.LENGTH_SHORT).show();
			Log.e("WSc", e.getMessage());
			
			backToAddress();
		}
    }
    
    private void backToAddress() {
    	Intent intent = new Intent(this, AddressActivity.class);
		intent.putExtra(MainActivity.EXTRA_INETADDRESS, mInetAddress);
		intent.putExtra(MainActivity.EXTRA_PORTNUMBER, mPortNumber);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
		finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	switch(item.getItemId()) {
	    	case R.id.action_settings:
	    		return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
    
    private void startSocketManager() throws UnknownHostException, IOException {
    	Intent i = getIntent();
    	mInetAddress = i.getStringExtra(EXTRA_INETADDRESS);
    	mPortNumber = i.getIntExtra(EXTRA_PORTNUMBER, DEFAULT_PORTNUMBER);
    	mSocketManager = new SocManagerClient(InetAddress.getByName(mInetAddress), mPortNumber, 20);
    }
}

package nl.utwente.wsc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import nl.utwente.wsc.communication.SocManagerClient;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


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
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	switch(item.getItemId()) {
	    	case R.id.action_view_wscs:
	    		Intent intent = new Intent(this, WSCActivity.class);
	    		startActivity(intent);
	    		return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
    
    private void startSocketManager() throws UnknownHostException, IOException {
    	Intent i = getIntent();
    	mInetAddress = i.getStringExtra(EXTRA_INETADDRESS);
    	mPortNumber = i.getIntExtra(EXTRA_PORTNUMBER, DEFAULT_PORTNUMBER);
    	mSocketManager = new SocManagerClient(InetAddress.getByName("192.168.1.2"), 1337, 20);
    	mSocketManager.start();
    }
}

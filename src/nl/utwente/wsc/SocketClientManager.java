package nl.utwente.wsc;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import nl.utwente.wsc.communication.ColorType;
import nl.utwente.wsc.communication.OnSocManagerTaskCompleted;
import nl.utwente.wsc.communication.SocketClient;
import nl.utwente.wsc.communication.ValueType;
import nl.utwente.wsc.models.WSc;
import nl.utwente.wsc.utils.FileUtils;
import android.database.Observable;
import android.util.Log;

public class SocketClientManager extends Observable<String> implements OnSocManagerTaskCompleted {
    public static final String TAG = "SocketClientManager";
	
	private static final String CERTIFICATE = "server.crt";	
    private static SSLContext SSLC;
	
	LinkedHashMap<WSc, SocketClient> clientList = new LinkedHashMap<WSc, SocketClient>();
	private MainActivity mainActivity;

	public SocketClientManager(MainActivity c) throws IOException {
		mainActivity = c;
		getSSLContext(mainActivity.getAssets().open(CERTIFICATE));
		if(FileUtils.hasWscList()) {
			try {
				for(WSc wsc : FileUtils.getWSCListFromFile(mainActivity)) {
					SocketClient sClient = new SocketClient(mainActivity, SSLC, this);
					sClient.connect(wsc.getHostname(), wsc.getPort(), 10000);
					clientList.put(wsc, new SocketClient(mainActivity, SSLC, this));
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	    addDevice(new WSc("Mark server", "130.89.232.73", 7331)); //TODO remove
	}

	/**
	 * Place where all callbacks will end up and from here they will be 
	 * send to all observers.
	 */
	@Override
	public void doneTask(String address, ValueType type, Object value) {
		if (type.equals(ValueType.IS_ON)) {		
		} else if (type.equals(ValueType.TURN_OFF)) {
		} else if (type.equals(ValueType.TURN_ON)) {
		} else if (type.equals(ValueType.VALUES_POWER)) {
		} else if (type.equals(ValueType.VALUES_COLOR)) {
		} else if (type.equals(ValueType.CONNECTING)) {
			if (value.equals(false)) {
				//TODO connecting does not have to succeed to be able to add the device
				mainActivity.toastMessage(mainActivity, "Adding device failed: " + address, true);
				removeDevice(address);
			} else {
				mainActivity.toastMessage(mainActivity, "Adding device succes: " + address, true);
				Thread set = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {}
						setDevicesState(true);						
					}
				});
				set.start();
			}
		} else if (type.equals(ValueType.DISCONNECTING)) {
		} else if (type.equals(ValueType.CONN_DEAD)) {
			
		}
		
	}
	
	public List<WSc> getDevices() {
		return new ArrayList<WSc>(clientList.keySet());
	}

	public boolean addDevice(WSc wsc) {
		SocketClient client = new SocketClient(mainActivity, SSLC, this);
		try {
			client.connect(wsc);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		clientList.put(wsc, client);
		return true;
	}
	public void removeDevice(WSc wsc) {
		clientList.get(wsc).disconnect();
		clientList.remove(wsc);	
		mainActivity.updateList();
	}
	
	public void removeDevice(String address) {
		removeDevice(getKey(address));
	}
	
	public void removeDevice(int index) {
		removeDevice(getKey(index));
	}
	
	/**
	 * Gets the state (on or off) of every connected WSc.<br>
	 * Array can contain nulls when the device(s) do(es) not respond.
	 * 
	 * @return the array of states
	 */
	public Boolean[] getDevicesState() {
		Boolean[] states = new Boolean[clientList.size()];
		boolean succes = true;
		int counter = 0;
		for (SocketClient client : clientList.values()) {	
			try {
				states[counter] = client.socketIsOn();
			} catch (IOException e) {
				e.printStackTrace();
			}
			counter++;
		}
		return states;
	}
	
	/**
	 * Sets the state of all connected WSc.
	 * 
	 * @param turnOn whether to turn them on or off
	 * @return whether this has succeeded on all devices
	 */
	public boolean setDevicesState(boolean turnOn) {
		boolean succes = true;
		for (SocketClient client : clientList.values()) {
			try {
				if (turnOn) {
					client.turnOnSocket();
				} else {
					client.turnOffSocket();
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return succes;
	}
	
	/**
	 * Sets the state of a connected WSc.
	 * 
	 * @param turnOn whether to turn it on or off
	 * @return whether this has succeeded
	 */	
	public boolean setDeviceState(int index, boolean turnOn) {
		try {
			if (turnOn) {
				clientList.get(getKey(index)).turnOnSocket();
			} else {
				clientList.get(getKey(index)).turnOffSocket();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Gets the current color of every connected WSc.<br>
	 * Array can contain nulls when the device(s) do(es) not respond.
	 * 
	 * @return the array of color types
	 */
	public ColorType[] getDevicesColor() {
		ColorType[] colors = new ColorType[clientList.size()];
//		boolean succes = true;
//		int counter = 0;
//		for (SocketClient client : clientList.values()) {	
//			try {
//				colors[counter] = client.getSocketColor();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			counter++;
//		}
		return colors;
	}
	
	private WSc getKey(int index) {
		Iterator<WSc> it = clientList.keySet().iterator();
		for (int i = 0; i < index; i++) {
			if (it.hasNext()) {
				it.next();
			} else {
				return null;
			}
		}
		return it.next();
	}
	
	private WSc getKey(String hostName) {
		Iterator<WSc> it = clientList.keySet().iterator();
		while (it.hasNext()) {
			WSc device = it.next();
			if (device.getHostname().equals(hostName)) {
				return device;
			}
		} 
		return null;
	}

	public void stop() {
		for(SocketClient client : clientList.values()) {
			client.disconnect();
		}
		try {
			FileUtils.saveToFile(mainActivity, new ArrayList<WSc>(clientList.keySet()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    private SSLContext getSSLContext(InputStream certificate) {
    	if (SSLC != null) {
    		return SSLC;
    	}
    	// Load CA from a .crt file
    	CertificateFactory cf = null;
		try {
			cf = CertificateFactory.getInstance("X.509");
		} catch (CertificateException e) {
			Log.e(TAG, "Cannot load certificate factory for X.509: " + e);
			System.exit(10);
		}
		Certificate ca = null;
    	try {
    	    ca = cf.generateCertificate(certificate);
    	    //System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
    	} catch (CertificateException e) {
			Log.e(TAG, "Cannot generate certificate: " + e);
			System.exit(12);
		} finally {
    	    try {
				certificate.close();
			} catch (IOException e) {
				Log.e(TAG, "Cannot close certificate file: " + e);
				System.exit(13);				
			}
    	}
		try {
	    	// Create a KeyStore containing our trusted CAs
	    	String keyStoreType = KeyStore.getDefaultType();
	    	KeyStore keyStore = null;
			keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(null, null);
			keyStore.setCertificateEntry("WSc", ca);
	    	// Create a TrustManager that trusts the CAs in our KeyStore
	    	String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
	    	TrustManagerFactory tm = TrustManagerFactory.getInstance(tmfAlgorithm);
	    	tm.init(keyStore);
	    	// Create an SSLContext that uses our TrustManager
	    	SSLC = SSLContext.getInstance("TLS");
	    	SSLC.init(null, tm.getTrustManagers(), null);
		} catch (KeyStoreException e) {
			Log.e(TAG, "Cannot generate keystore: " + e);
			System.exit(14);	
		} catch (CertificateException e) {
			Log.e(TAG, "Cannot use certificate: " + e);
			System.exit(15);	
		} catch (NoSuchAlgorithmException e) {
			// will not happen
			System.exit(16);
		} catch (IOException e) {
			// will not happen
			System.exit(17);
		} catch (KeyManagementException e) {
			// will not happen
			System.exit(18);
		}   
		return SSLC;
    }

}

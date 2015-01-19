package nl.utwente.wsc;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import nl.utwente.wsc.communication.OnSocManagerTaskCompleted;
import nl.utwente.wsc.communication.SocketClient;
import nl.utwente.wsc.communication.ValueType;
import nl.utwente.wsc.models.WSc;
import nl.utwente.wsc.utils.FileUtils;

import org.joda.time.DateTime;

import android.content.Context;
import android.database.Observable;
import android.util.Log;

public class SocketClientManager extends Observable<String> implements OnSocManagerTaskCompleted {

	public interface SCMCallback {
		public void toastMessage(final Context context, final String message, final boolean displayLong);
		public void updateList();
	}

	public static final String TAG = "SocketClientManager";

	private static final String CERTIFICATE = "server.crt";	
	private static SSLContext SSLC;

	LinkedHashMap<WSc, SocketClient> clientList = new LinkedHashMap<WSc, SocketClient>();
	private SCMCallback callback;
	private Context context;

	public SocketClientManager(Context context, SCMCallback callback) throws IOException{
		this.context = context;
		this.callback = callback;
		getSSLContext(context.getAssets().open(CERTIFICATE));
		if(FileUtils.hasWscList(context)) {
			try {
				for(WSc wsc : FileUtils.getWSCListFromFile(context)) {
					SocketClient sClient = new SocketClient(context, SSLC, this);
					if (clientList.get(wsc) == null) {
						clientList.put(wsc, sClient);
					}
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void connect() {
		for(Entry<WSc, SocketClient> e : clientList.entrySet()) {
			WSc w = e.getKey();
			w.setBusy(true);
			e.getValue().connect(w.getHostname(), w.getPort());
		}
	}

	/**
	 * Place where all callbacks will end up and from here they will be 
	 * send to all observers.
	 */
	@Override
	public void doneTask(String address, ValueType type, Object value) {
		WSc wsc = getKey(address);
		boolean active = true, succes = value.equals(true);
		if (type.equals(ValueType.IS_ON)) {	
			wsc.setTurnedOn(succes);
		} else if (type.equals(ValueType.TURN_OFF)) {
			if (succes) {
				wsc.setTurnedOn(false);
			} else {
				// Could not turn it off
			}			
		} else if (type.equals(ValueType.TURN_ON)) {
			if (succes) {
				wsc.setTurnedOn(true);
			} else {
				// Could not turn it on
			}
		} else if (type.equals(ValueType.VALUES_POWER)) {
			if(!value.equals("-1")) {		
				// Asume object is large string with format:
				// 4865448654,230;7896546451,290;12354,200;�
				wsc.setHistory((HashMap<DateTime, Integer>) value);
			} else {
				// Problem
			}
		} else if (type.equals(ValueType.VALUES_COLOR)) {
			// TODO
		} else if (type.equals(ValueType.CONNECTING)) {
			if (succes) {
				wsc.setConnected(true);
				try {
					clientList.get(wsc).socketIsOn();
					clientList.get(wsc).getPowerValues();
				} catch (IOException e) {
					// Problem
				}
			} else { 
				// problem
			}
		} else if (type.equals(ValueType.DISCONNECTING)) {
			if (succes) {
				wsc.setConnected(false);
			} else { 
				// problem
			}			
		} else if (type.equals(ValueType.CONN_DEAD)) {
			clientList.remove(wsc);
			callback.updateList();
			active = false;
		} else {
			return;
		}
		toastDeviceUpdate(wsc, type.toFriendlyString(), active, succes);
		wsc.setBusy(false);
		callback.updateList();
	}

	private void toastDeviceUpdate(WSc wsc, String action, boolean active, boolean succes) {
		callback.toastMessage(context, (active ? action + " device \"" + wsc.getName() + "\"" : 
			"Device \"" + wsc.getName() + "\"" + action) 
			+ (active && succes ? "succes" : " failed"), false);
	}

	public List<WSc> getDevices() {
		return new ArrayList<WSc>(clientList.keySet());
	}

	public void updateDevice(WSc updated) {
		WSc wsc = getKey(updated.getHostname());
		wsc.setName(updated.getName());
	}

	public boolean addDevice(WSc wsc) {
		wsc.setBusy(true);
		SocketClient client = new SocketClient(context, SSLC, this);
		clientList.put(wsc, client);
		try {
			client.connect(wsc);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public void removeDevice(WSc wsc) {
		clientList.get(wsc).disconnect();
		clientList.remove(wsc);	
		callback.updateList();
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
		for (WSc wsc : clientList.keySet()) {
			if(!setDeviceState(wsc, turnOn)) {
				succes = false;
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
		return setDeviceState(getKey(index), turnOn);
	}

	public boolean setDeviceState(WSc wsc, boolean turnOn ) {

		try {
			if (turnOn && !wsc.isTurnedOn()) {
				clientList.get(getKey(wsc.getHostname())).turnOnSocket();
				wsc.setBusy(true);
			} else if(!turnOn && wsc.isTurnedOn()) {
				clientList.get(getKey(wsc.getHostname())).turnOffSocket();
				wsc.setBusy(true);
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
	public boolean getDevicesColor() {	
		boolean succes = true;
		for (SocketClient client : clientList.values()) {	
			try {
				client.getSocketColor();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return succes;
	}

	/**
	 * Gets the current color of every connected WSc.<br>
	 * Array can contain nulls when the device(s) do(es) not respond.
	 * 
	 * @return the array of color types
	 */
	public boolean getDevicesValues() {		
		boolean succes = true;
		for (SocketClient client : clientList.values()) {	
			try {
				client.getPowerValues();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return succes;
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
			if(client.alive()) {
				client.disconnect();		
			}
		}
		save();
	}

	public void save() {
		try {
			FileUtils.saveToFile(context, new ArrayList<WSc>(clientList.keySet()));
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

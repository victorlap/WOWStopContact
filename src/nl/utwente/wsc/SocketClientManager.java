package nl.utwente.wsc;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import nl.utwente.wsc.communication.ColorType;
import nl.utwente.wsc.communication.OnSocManagerTaskCompleted;
import nl.utwente.wsc.communication.SocketClient;
import nl.utwente.wsc.communication.ValueType;
import nl.utwente.wsc.models.WSc;
import nl.utwente.wsc.utils.FileUtils;
import android.content.Context;
import android.graphics.Color;

public class SocketClientManager implements OnSocManagerTaskCompleted {
	
	LinkedHashMap<WSc, SocketClient> clientList = new LinkedHashMap<WSc, SocketClient>();

	public SocketClientManager(Context c) {
		if(FileUtils.hasWscList()) {
			try {
				for(WSc wsc : FileUtils.getWSCListFromFile()) {
					SocketClient sClient = new SocketClient(c, this);
					sClient.connect(InetAddress.getByName(wsc.getHostname()), wsc.getPort(), 10000);
					clientList.put(wsc, new SocketClient(c, this));
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

	@Override
	public void doneTask(InetAddress address, ValueType type, Object value) {
		// TODO Auto-generated method stub
		
	}
	
	public List<WSc> getDevices() {
		return new ArrayList<WSc>(clientList.keySet());
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

	public void stop() {
		for(SocketClient client : clientList.values()) {
			client.disconnect();
		}
		
	}

}

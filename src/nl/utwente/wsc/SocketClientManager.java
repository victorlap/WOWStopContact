package nl.utwente.wsc;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

import nl.utwente.wsc.communication.OnSocManagerTaskCompleted;
import nl.utwente.wsc.communication.SocketClient;
import nl.utwente.wsc.communication.ValueType;
import nl.utwente.wsc.models.WSc;
import nl.utwente.wsc.utils.FileUtils;
import android.content.Context;

public class SocketClientManager implements OnSocManagerTaskCompleted {
	
	HashMap<WSc, SocketClient> clientList = new HashMap<WSc, SocketClient>();

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
	
	public boolean setDevicesState(boolean turnOn) {
		boolean succes = true;
		for(WSc wsc : clientList.keySet()) {
			if (!setDeviceState(wsc, turnOn)) {
				succes = false;
			}
		}
		return succes;
	}
	
	public boolean setDeviceState(WSc wsc, boolean turnOn) {
		try {
			if (turnOn) {
				clientList.get(wsc).turnOnSocket();
			} else {
				clientList.get(wsc).turnOffSocket();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void stop() {
		// TODO
		
	}

}

package nl.utwente.wsc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Set;

import nl.utwente.wsc.communication.OnSocManagerTaskCompleted;
import nl.utwente.wsc.communication.SocketClient;
import nl.utwente.wsc.communication.ValueType;
import nl.utwente.wsc.models.WSc;
import nl.utwente.wsc.utils.FileUtils;
import android.content.Context;

public class SocketClientManager implements OnSocManagerTaskCompleted {
	
	private HashMap<WSc, SocketClient> clientList = new HashMap<WSc, SocketClient>();
	private Context context;

	public SocketClientManager(Context c) {
		
		this.context = c;
		if(FileUtils.hasWscList()) {
			try {
				for(WSc wsc : FileUtils.getWSCListFromFile()) {
					addWsc(wsc);
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
	
	public Set<WSc> getAll() {
		return clientList.keySet();
	}
	
	public void addWsc(WSc wsc) throws IOException {
			SocketClient sClient = new SocketClient(context, this);
			sClient.connect(InetAddress.getByName(wsc.getHostname()), wsc.getPort(), 10000);
			clientList.put(wsc, sClient);
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

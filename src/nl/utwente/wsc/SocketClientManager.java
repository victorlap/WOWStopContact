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
	
	HashMap<WSc, SocketClient> clientlist = new HashMap<WSc, SocketClient>();

	public SocketClientManager(Context c) {
		if(FileUtils.hasWscList()) {
			try {
				for(WSc wsc : FileUtils.getWSCListFromFile()) {
					SocketClient sClient = new SocketClient(c, this);
					sClient.connect(InetAddress.getByName(wsc.getHostname()), wsc.getPort(), 10000);
					clientlist.put(wsc, new SocketClient(c, this));
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
	public void doneTask(ValueType type, Object value) {
		// TODO Auto-generated method stub
		
	}
	
	public void turnOnAllDevices() {
		for(WSc wsc : clientlist.keySet()) {
			SocketClient client = clientlist.get(wsc);
			try {
				client.turnOnSocket();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}

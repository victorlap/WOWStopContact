package nl.utwente.wsc.communication;

import java.net.InetAddress;

public interface OnSocManagerTaskCompleted {
	    
	public void doneTask(String address, ValueType type, Object value);
}

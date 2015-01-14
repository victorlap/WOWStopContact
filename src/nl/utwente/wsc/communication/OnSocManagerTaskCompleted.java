package nl.utwente.wsc.communication;

import java.net.InetAddress;

public interface OnSocManagerTaskCompleted {
	    
	public void doneTask(InetAddress address, ValueType type, Object value);
}

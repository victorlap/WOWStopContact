package nl.utwente.wsc.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import org.joda.time.DateTime;

import com.jjoe64.graphview.series.DataPoint;


public class WSc implements Serializable {

	private static final long serialVersionUID = -8500471167788498880L;

	private String name;
	private String hostname;
	private int port;
	
	private transient boolean connected;
	private transient boolean turnedOn;
	private transient ColorType color;
	private transient boolean busy;
	private HashMap<DateTime, Integer> history;
	
	public WSc(String name, String hostname, int port) {
		this.name = name;
		this.hostname = hostname;
		this.port = port;
		this.color = ColorType.NONE;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setTurnedOn(boolean turnedOn) {
		this.turnedOn = turnedOn;
	}
	
	public boolean isTurnedOn() {
		return turnedOn;
	}
	
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public void setBusy(boolean busy) {
		this.busy = busy;
	}
	
	public boolean isBusy() {
		return busy;
	}
	
	public void setColor(ColorType color) {
		this.color = color;
	}
	
	public ColorType getColor() {
		return color == null ? ColorType.NONE : color;
	}
	
	public boolean hasHistory() {
		return history != null;
	}
	
	public void setHistory(HashMap<DateTime, Integer> history) {
		this.history = history;
	}
	
	public DataPoint[] getHistory() {
		DataPoint[] dps = new DataPoint[history.size()];
		//DataPoint[] dps = new DataPoint[100];
		int i = 0;
		for(DateTime datetime : history.keySet()) {
			dps[i++] = new DataPoint(datetime.toDate(), history.get(datetime));
		}
		return dps;
	}
	
	@Override
	public String toString() {
		return getName() + " " + getHostname() + ":" + getPort();
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == this) {
			return true;
		}
		if(other instanceof WSc) {
			WSc w = (WSc) other;
			if(getName() != w.getName()) return false; 
			if(getHostname() != w.getHostname()) return false;
			if(getPort() != w.getPort()) return false;
			return true;
		}
		return false;
	}

}

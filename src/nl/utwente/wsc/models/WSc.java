package nl.utwente.wsc.models;

import java.io.Serializable;
import java.util.Date;

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
	private DataPoint[] history;
	
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
	
	/**
	 * Set a history in the form of a <code>String[]</code>
	 * the string array should contain strings in the form of 12354648884,230;
	 * @param history
	 */
	public void setHistory(String[] history) {
		DataPoint[] dps = new DataPoint[history.length];
		int i = -1;
		for(String unit : history) {
			String[] units = unit.split(",");
			dps[i++] = new DataPoint(new DateTime(Long.parseLong(units[0])).toDate(), Double.parseDouble(units[1]));
		}
		setHistory(dps);
	}
	
	/**
	 * Set a history in the form of a <code>DataPoint[]</code>
	 * the DataPointArray should be in the form of ({@link Date}, {@link String})
	 * @param history
	 */
	public void setHistory(DataPoint[] history) {
		this.history = history;
	}
	
	public DataPoint[] getHistory() {
		return history;
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

package nl.utwente.wsc.models;

import java.io.Serializable;
import java.util.Random;


public class WSc implements Serializable {

	private static final long serialVersionUID = -8500471167788498880L;

	private String name;
	private String hostname;
	private int port;
	
	private transient boolean connected;
	private transient boolean turnedOn;
	private transient ColorType color;
	private transient boolean busy;
	
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
	
	public void setTurnedOn(boolean turned_on) {
		this.turnedOn = turned_on;
		if(turned_on) {
			setColor(ColorType.GREEN);
		} else {
			setColor(ColorType.NONE);
		}
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
		if (color == null) {
			color = ColorType.NONE;
		}
		return color;
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

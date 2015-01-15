package nl.utwente.wsc.models;

import java.io.Serializable;
import java.util.Random;


public class WSc implements Serializable {

	private static final long serialVersionUID = -8500471167788498880L;

	private String name;
	private String hostname;
	private int port;
	private boolean turned_on;
	private ColorType color;
	
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
		this.turned_on = turned_on;
		if(turned_on) {
			setColor(ColorType.GREEN);
		} else {
			setColor(ColorType.NONE);
		}
	}
	
	public boolean isTurnedOn() {
		return turned_on;
	}
	
	public void setColor(ColorType color) {
		this.color = color;
	}
	
	public ColorType getColor() {
		return color;
	}
	
	@Override
	public String toString() {
		return getName() + " " + getHostname() + ":" + getPort();
	}

}

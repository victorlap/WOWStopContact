package nl.utwente.wsc.models;

import java.io.Serializable;
import java.util.Random;


public class WSc implements Serializable {

	private static final long serialVersionUID = -8500471167788498880L;

	private String name;
	private String hostname;
	private int port;
	private boolean turned_on;
	
	public WSc(String name, String hostname, int port) {
		this.name = name;
		this.hostname = hostname;
		this.port = port;
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
	}
	
	public boolean isTurnedOn() {
		return turned_on;
	}
	
	@Override
	public String toString() {
		return getName() + " " + getHostname() + ":" + getPort();
	}

}

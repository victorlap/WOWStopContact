package nl.utwente.wsc.models;


public class WSc {
	
	private String name;
	private String hostname;
	private int port;
	
	public WSc(String name, String hostname, int port) {
		this.name = name;
		this.hostname = hostname;
		this.port = port;
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
	
	@Override
	public String toString() {
		return getName() + " " + getHostname() + ":" + getPort();
	}

}

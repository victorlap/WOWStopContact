package nl.utwente.wsc.communication;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import nl.utwente.wsc.exceptions.InvalidPacketException;
import nl.utwente.wsc.models.ColorType;
import nl.utwente.wsc.models.WSc;
import nl.utwente.wsc.utils.Timer;
import nl.utwente.wsc.utils.Tools;

import org.joda.time.DateTime;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Client socket implementation.
 *
 * @author rvemous
 */
public class SocketClient {
	
	public static final int TIMEOUT = 10000;
	public static final String TAG = "SocketClient";
    
    private final Object lock = new Object();
    
    private boolean stop = true;
    
    protected SSLSocket sock;
    protected SSLContext sslContext;
    
    protected BufferedInputStream in;
    protected BufferedOutputStream out;
    
    protected volatile LinkedList<Packet> receiveBuffer;
    
    protected OnSocManagerTaskCompleted callBack;
    
    static {
        System.setProperty("javax.net.ssl.trustStorePassword", "WScDrone5A");
    }

    /**
     * Creates a new socket manager which is connected to the requested 
     * host.
     * 
     * @param address the address of the host
     * @param portNr the post to connect to
     * @param timeout time-out to use for connecting
     * @throws IOException 
     */
    public SocketClient(Context context, SSLContext sslContext, OnSocManagerTaskCompleted callBack) { 
    	this.callBack = callBack;
    	this.sslContext = sslContext;
    }
    
    protected void finishConnecting() throws IOException {
        in = new BufferedInputStream(sock.getInputStream());
        out = new BufferedOutputStream(sock.getOutputStream());
        receiveBuffer = new LinkedList<Packet>();
        startReceiverThread();  	
        stop = false;   	
    }
    
    public boolean alive() {
        return sock != null && !stop;
    }
    
    /**
     * Sends one packet to the server.
     * 
     * @param packet to send
     * @throws IOException when connection problems occur.
     */
    public void sendPacket(Packet packet) throws IOException {
        out.write(packet.toSendablePacket());
        out.flush();
        Log.v(TAG, packet.toString());
    }
    
    /**
     * Starts the receiver thread which will read packets and will send 
     * them to any observers.
     */
    private void startReceiverThread() {
    	Thread thread = new Thread(new Runnable() {	
			@Override
			public void run() {
				byte[] headerbuff = new byte[PacketHeader.HEADER_LENGTH];
	            while (!stop) {
	                do {
	                    try {
	                        headerbuff[0] = (byte) in.read();
	                    } catch (IOException ex) {
	                        Log.e(this.toString(), "Connection dead");
	                        stop = true;
	                        continue;
	                    }
	                    Tools.waitForMs(50);
	                } while (!stop && headerbuff[0] == -1);
	                try {
	                    in.read(headerbuff, 1, headerbuff.length - 1);
	                } catch (IOException ex) {
	                	Log.e(this.toString(), "Connection dead");
	                    stop = true;
	                    continue;                  
	                }
	                PacketHeader header = null;
	                try {
	                    header = new PacketHeader(headerbuff);
	                } catch (InvalidPacketException ex) {
	                    Log.e(this.toString(), "Got invalid header: " + 
	                            Arrays.toString(headerbuff));
	                    continue;
	                }
	                int len = header.getPacketLength();
	                byte[] receiverBuff = new byte[len];
	                int i = 0;
	                try {
	                    while (i < len && 
	                            (i += in.read(receiverBuff, i, len - i)) != -1){}
	                } catch (IOException ex) {
	                    ex.printStackTrace();
	                    stop = true;
	                    continue;
	                }
	                Packet packet = new Packet(header, receiverBuff);
	                if (new String(packet.getData()).equalsIgnoreCase("DEAD")) {
	                	callBack.doneTask(sock.getInetAddress(), ValueType.CONN_DEAD, null);
	                }
	                synchronized (lock) {
	                    receiveBuffer.add(packet);
	                }
	                Log.d(this.toString(), "Got packet: " + packet.toString());
	            }
			}
		});
        thread.setName("Packet-receiver");
        thread.setDaemon(true);
        thread.start();
    }
    
    public Packet waitForPacket(int timeOut) {
        Packet packet = null;
        Timer timer = new Timer(timeOut, true);
        while (!stop && !timer.hasExpired()) {
            synchronized (lock) {
                if (!receiveBuffer.isEmpty()) {
                    return receiveBuffer.removeFirst();
                }
            }
            Tools.waitForMs(50);
        }
        return packet;
    }
      
    public void connect(InetAddress address, int portNr, int timeout) {
    	performAsyncAction(ValueType.CONNECTING, false, 
    			new String[]{address.getHostAddress(), portNr+"", timeout+""});
    }   
     
    public void connect(WSc wsc) throws UnknownHostException {
    	connect(InetAddress.getByName(wsc.getHostname()), wsc.getPort(), TIMEOUT);
    }
    
    public synchronized boolean socketIsOn() throws IOException {
    	return performAsyncAction(ValueType.IS_ON);
    }
    
    public synchronized boolean turnOffSocket() throws IOException {
    	return performAsyncAction(ValueType.TURN_OFF);
    }
        
    public synchronized boolean turnOnSocket() throws IOException {
    	return performAsyncAction(ValueType.TURN_ON);
    }
    
    public synchronized boolean getPowerValues() throws IOException {
    	return performAsyncAction(ValueType.VALUES_POWER);
    }
    
    public synchronized boolean getSocketColor() throws IOException { 
    	return performAsyncAction(ValueType.VALUES_COLOR);
    }
    
    /**
     * Shuts down client nicely.
     */
    public boolean disconnect() {
    	stop = true;
        Tools.waitForMs(100);
    	return performAsyncAction(ValueType.DISCONNECTING, true);
    }
    
    private boolean performAsyncAction(ValueType type, boolean ifStarted, String... values) {
    	if (ifStarted && stop) {
    		return false;
    	}
    	String[] data = new String[values.length + 1];
    	data[0] = type.toString();
    	for (int i = 1; i < data.length; i++) {
    		data[i] = values[i - 1];
    	}
    	new AsyncCommunication(this, TIMEOUT).execute(data);
        return true;    	
    }
    
    private boolean performAsyncAction(ValueType type, boolean ifStarted) {
    	return performAsyncAction(type, ifStarted, new String[]{});
    }
    
    private boolean performAsyncAction(ValueType type) {
    	return performAsyncAction(type, true, new String[]{});
    }
    
}

class AsyncCommunication extends AsyncTask<String, Integer, Object> {
	
	private SocketClient client;
	private int timeout;
	
	public AsyncCommunication(SocketClient client, int timeout) {
		this.client = client;
		this.timeout = timeout;
	}
	
	@Override
	protected Object doInBackground(String... params) {
		ValueType type = ValueType.getType(params[0]);
		Object returnValue = null;
		try {
			if (type.equals(ValueType.IS_ON)) {		
		        client.sendPacket(Packet.createCommandPacket(Command.isTurnedOn()));
		        Packet ans = client.waitForPacket(timeout);
		        returnValue = Packet.isSuccesResponse(ans);	
			} else if (type.equals(ValueType.TURN_OFF)) {
				client.sendPacket(Packet.createCommandPacket(Command.turnOff()));
		        Packet ans = client.waitForPacket(timeout);
		        returnValue = Packet.isSuccesResponse(ans);	
			} else if (type.equals(ValueType.TURN_ON)) {
				client.sendPacket(Packet.createCommandPacket(Command.turnOn()));
		        Packet ans = client.waitForPacket(timeout);
		        returnValue = Packet.isSuccesResponse(ans);	
			} else if (type.equals(ValueType.VALUES_POWER)) {
				client.sendPacket(Packet.createCommandPacket(Command.getValues()));  
		        Packet ans = client.waitForPacket(timeout);
		        if (!Packet.isDataPacket(ans)) {
		            return null;
		        }
		        String data = new String(ans.getData());
		        String[] valuePairs = data.split(";");
		        Map<DateTime, Integer> values = new HashMap<DateTime, Integer>();
		        for (String valuePair : valuePairs) {
		        	String[] splitted = valuePair.split(",");
		        	try {
		        		values.put(DateTime.parse(splitted[0]), Integer.parseInt(splitted[1]));
		        	} catch (Exception e) {
		        		Log.e(this.toString(), "invalid value pair: " + valuePair);
		        	}
		        }
		        returnValue = values;
			} else if (type.equals(ValueType.VALUES_COLOR)) {
				client.sendPacket(Packet.createCommandPacket(Command.getColor()));  
		        Packet ans = client.waitForPacket(timeout);
		        if (!Packet.isResponsePacket(ans)) {
		            return null;
		        }    	
		        returnValue = ColorType.getType(new String(ans.getData()));
			} else if (type.equals(ValueType.CONNECTING)) {
				try {
					// Open SSLSocket to wall socket
			        SocketFactory ssf = client.sslContext.getSocketFactory();
			        client.sock = (SSLSocket)ssf.createSocket(params[1], Integer.parseInt(params[2]));
			        HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
			        SSLSession session = client.sock.getSession();
			        // Verify that the certificate host name is for the wall socket
			        // This is due to lack of SNI support in the current SSLSocket.
			        if (!hv.verify("WSc", session)) {
			            throw new SSLHandshakeException("Expected " + "WSc" +
			                                            ", found " + session.getPeerPrincipal());
			        }
			        client.finishConnecting();
				} catch (IOException e) {
					e.printStackTrace();
					client.callBack.doneTask(InetAddress.getByName(params[1]), type, false);
					return null;
				}
				returnValue = true;
			} else if (type.equals(ValueType.DISCONNECTING)) {
		        try {
		            client.in.close();
		            client.out.close();
		            client.sock.close();
		        } catch (IOException ex) {/* socket closed by server */}	
		        client.sock = null;
		        returnValue = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return returnValue;
		}
		try {
			client.callBack.doneTask(client.sock.getInetAddress(), type, returnValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnValue;
	}
}

package nl.utwente.wsc.com.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.joda.time.DateTime;

import android.util.Log;
import nl.utwente.wsc.com.model.ColorType;
import nl.utwente.wsc.com.model.Command;
import nl.utwente.wsc.com.model.Packet;
import nl.utwente.wsc.com.model.PacketHeader;
import nl.utwente.wsc.com.model.exception.InvalidPacketException;
import nl.utwente.wsc.utils.Timer;
import nl.utwente.wsc.utils.Tools;

/**
 * Client socket implementation.
 *
 * @author rvemous
 */
public class SocManagerClient extends java.util.Observable {
    
    public static final int TIMEOUT = 10000; //ms
    public static final String CONNECTION_DEAD = "DEAD";
    private final Object lock = new Object();
    
    private boolean stop = false;
    
    private Socket sock;
    private SSLSession session;
    
    private BufferedInputStream in;
    private BufferedOutputStream out;
    
    private volatile LinkedList<Packet> receiveBuffer;

    /**
     * Creates a new socket manager which is connected to the requested 
     * host.
     * 
     * @param address the address of the host
     * @param portNr the post to connect to
     * @param timeout time-out to use for connecting
     * @throws IOException 
     */
    public SocManagerClient(InetAddress address, int portNr, int timeout) throws IOException {
        System.setProperty("javax.net.ssl.trustStore", "keystore");
        System.setProperty("javax.net.ssl.trustStorePassword", "picloudkeypass");

        SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        sock = ssf.createSocket(address.getHostAddress(), portNr);
        session = ((SSLSocket) sock).getSession();
        
        in = new BufferedInputStream(sock.getInputStream());
        out = new BufferedOutputStream(sock.getOutputStream());
        
        receiveBuffer = new LinkedList<Packet>();
        startReceiverThread();
    }
    
    public boolean alive() {
        return session != null && session.isValid() && !stop;
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
    }
    
    /**
     * Starts the receiver thread which will read packats and will send 
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
    
    public synchronized boolean socketIsOn() throws IOException {
        sendPacket(Packet.createCommandPacket(Command.isTurnedOn()));
        Packet ans = waitForPacket(TIMEOUT);
        return Packet.isSuccesResponse(ans);
    }
    
    public synchronized boolean turnOffSocket() throws IOException {
        sendPacket(Packet.createCommandPacket(Command.turnOff()));
        Packet ans = waitForPacket(TIMEOUT);
        return Packet.isSuccesResponse(ans);
    }
        
    public synchronized boolean turnOnSocket() throws IOException {
        sendPacket(Packet.createCommandPacket(Command.turnOff()));
        Packet ans = waitForPacket(TIMEOUT);
        return Packet.isSuccesResponse(ans);
    }
    
    public synchronized Map<DateTime, Integer> getPowerValues() throws IOException {
        sendPacket(Packet.createCommandPacket(Command.getValues()));  
        Packet ans = waitForPacket(TIMEOUT);
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
        return values;
    }
    
    public synchronized ColorType getSocketColor() throws IOException { 
    	sendPacket(Packet.createCommandPacket(Command.getColor()));  
        Packet ans = waitForPacket(TIMEOUT);
        if (!Packet.isResponsePacket(ans)) {
            return null;
        }    	
    	return ColorType.getType(new String(ans.getData()));
    }
    
    /**
     * Shuts down client nicely.
     */
    public void shutdown() {
        stop = true;
        Tools.waitForMs(100);
        session.invalidate();
        try {
            sock.close();
        } catch (IOException ex) {/* socket closed by server */}
    }
    
    public void notifyObserversSetchanged(Object arg) {
        setChanged();
        notifyObservers(arg);
    }
    
    private void printSessionInfo() {
        Certificate[] cchain = null;
        try {
            cchain = session.getPeerCertificates();
        } catch (SSLPeerUnverifiedException ex) {
            ex.printStackTrace();
        }
        System.out.println("The Certificates used by peer");
        for (int i = 0; i < cchain.length; i++) {
            System.out.println(((X509Certificate) cchain[i]).getSubjectDN());
        }
        System.out.println("Peer host is " + session.getPeerHost());
        System.out.println("Cipher is " + session.getCipherSuite());
        System.out.println("Protocol is " + session.getProtocol());
        System.out.println("ID is " + new BigInteger(session.getId()));
        System.out.println("Session created in " + session.getCreationTime());
        System.out.println("Session accessed in " + session.getLastAccessedTime());
    }
    
}

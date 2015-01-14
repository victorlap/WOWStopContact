package nl.utwente.wsc.communication;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import nl.utwente.wsc.exceptions.InvalidPacketException;
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
public class SocketClient extends AsyncTask<String, Integer, Object> {
	
	public static final int TIMEOUT = 10000;
	public static final String TAG = "SocketClient";
    
    private final Object lock = new Object();
    
    private boolean stop = true;
    
    private SSLSocket sock;
    private static SSLContext sslc;
    private static final String CERTIFICATE = "server.crt";
    
    private BufferedInputStream in;
    private BufferedOutputStream out;
    
    private volatile LinkedList<Packet> receiveBuffer;
    
    private OnSocManagerTaskCompleted callBack;
    
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
    public SocketClient(Context context, OnSocManagerTaskCompleted callBack) { 
    	this.callBack = callBack;
    	try {
			getSSLContext(context.getAssets().open(CERTIFICATE));
		} catch (IOException e) {
			Log.e(TAG, "Cannot load certificate: " + e);
			System.exit(15);
		}
    }
    
    public void connect(String address, int portNr, int timeout) throws IOException {
    	this.execute(new String[]{ValueType.CONNECTING.toString(), 
    			address, portNr+"", timeout+""});
    }    
    
    public boolean alive() {
        return !stop;
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
    	return performAsyncAction(ValueType.DISCONNECTING);
    }
    
    private boolean performAsyncAction(ValueType type) {
    	if (stop) {
    		return false;
    	}
    	this.execute(new String[]{type.toString()});
        return true;
    }
    
    private SSLContext getSSLContext(InputStream certificate) {
    	if (sslc != null) {
    		return sslc;
    	}
    	// Load CA from a .crt file
    	CertificateFactory cf = null;
		try {
			cf = CertificateFactory.getInstance("X.509");
		} catch (CertificateException e) {
			Log.e(TAG, "Cannot load certificate factory for X.509: " + e);
			System.exit(10);
		}
		Certificate ca = null;
    	try {
    	    ca = cf.generateCertificate(certificate);
    	    //System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
    	} catch (CertificateException e) {
			Log.e(TAG, "Cannot generate certificate: " + e);
			System.exit(12);
		} finally {
    	    try {
				certificate.close();
			} catch (IOException e) {
				Log.e(TAG, "Cannot close certificate file: " + e);
				System.exit(13);				
			}
    	}
		try {
	    	// Create a KeyStore containing our trusted CAs
	    	String keyStoreType = KeyStore.getDefaultType();
	    	KeyStore keyStore = null;
			keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(null, null);
			keyStore.setCertificateEntry("WSc", ca);
	    	// Create a TrustManager that trusts the CAs in our KeyStore
	    	String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
	    	TrustManagerFactory tm = TrustManagerFactory.getInstance(tmfAlgorithm);
	    	tm.init(keyStore);
	    	// Create an SSLContext that uses our TrustManager
	    	sslc = SSLContext.getInstance("TLS");
	    	sslc.init(null, tm.getTrustManagers(), null);
		} catch (KeyStoreException e) {
			Log.e(TAG, "Cannot generate keystore: " + e);
			System.exit(14);	
		} catch (CertificateException e) {
			Log.e(TAG, "Cannot use certificate: " + e);
			System.exit(15);	
		} catch (NoSuchAlgorithmException e) {
			// will not happen
			System.exit(16);
		} catch (IOException e) {
			// will not happen
			System.exit(17);
		} catch (KeyManagementException e) {
			// will not happen
			System.exit(18);
		}   
		return sslc;
    }

	@Override
	protected Object doInBackground(String... params) {
		ValueType type = ValueType.getType(params[0]);
		Object returnValue = null;
		try {
			if (type.equals(ValueType.IS_ON)) {		
		        sendPacket(Packet.createCommandPacket(Command.isTurnedOn()));
		        Packet ans = waitForPacket(TIMEOUT);
		        returnValue = Packet.isSuccesResponse(ans);	
			} else if (type.equals(ValueType.TURN_OFF)) {
		        sendPacket(Packet.createCommandPacket(Command.turnOff()));
		        Packet ans = waitForPacket(TIMEOUT);
		        returnValue = Packet.isSuccesResponse(ans);	
			} else if (type.equals(ValueType.TURN_ON)) {
		        sendPacket(Packet.createCommandPacket(Command.turnOn()));
		        Packet ans = waitForPacket(TIMEOUT);
		        returnValue = Packet.isSuccesResponse(ans);	
			} else if (type.equals(ValueType.VALUES_POWER)) {
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
		        returnValue = values;
			} else if (type.equals(ValueType.VALUES_COLOR)) {
		    	sendPacket(Packet.createCommandPacket(Command.getColor()));  
		        Packet ans = waitForPacket(TIMEOUT);
		        if (!Packet.isResponsePacket(ans)) {
		            return null;
		        }    	
		        returnValue = ColorType.getType(new String(ans.getData()));
			} else if (type.equals(ValueType.CONNECTING)) {
				// Open SSLSocket to wall socket
		        SocketFactory ssf = sslc.getSocketFactory();
		        sock = (SSLSocket)ssf.createSocket(params[1], Integer.parseInt(params[2]));
		        HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
		        SSLSession session = sock.getSession();
		        // Verify that the certificate host name is for the wall socket
		        // This is due to lack of SNI support in the current SSLSocket.
		        if (!hv.verify("WSc", session)) {
		            throw new SSLHandshakeException("Expected " + "WSc" +
		                                            ", found " + session.getPeerPrincipal());
		        }
		        in = new BufferedInputStream(sock.getInputStream());
		        out = new BufferedOutputStream(sock.getOutputStream());
		        receiveBuffer = new LinkedList<Packet>();
		        startReceiverThread();  	
		        returnValue = true;
		        stop = false;
			} else if (type.equals(ValueType.DISCONNECTING)) {
		        stop = true;
		        Tools.waitForMs(100);
		        try {
		            in.close();
		            out.close();
		            sock.close();
		        } catch (IOException ex) {/* socket closed by server */}	
		        returnValue = true;
			}
		} catch (IOException e) {}
		callBack.doneTask(sock.getInetAddress(), type, returnValue);
		return returnValue;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		// TODO use this??
		super.onProgressUpdate(values);
	}
    
}

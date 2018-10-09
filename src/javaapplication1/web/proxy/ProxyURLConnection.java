/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1.web.proxy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 *
 * @author alibaba0507
 */
public class ProxyURLConnection extends URLConnection{
   private Socket s;
    /**
     * Default TOR Proxy port.
     */
    private int proxyPort = 10001;//9050;
    /**
     * Default TOR Proxy hostaddr.
     */
    private String proxyAddr = "127.0.0.1";//"localhost";
    /**
     * Constant tells SOCKS4/4a to connect. Use it in the <i>req</i> parameter.
     */
    private final byte TOR_CONNECT = (byte) 0x01;
    /**
     * Constant tells TOR to do a DNS resolve. Use it in the <i>req</i>
     * parameter.
     */
    private final byte TOR_RESOLVE = (byte) 0xF0;
    /**
     * Constant indicates what SOCKS version are talking Either SOCKS4 or
     * SOCKS4a
     */
    private final byte SOCKS_VERSION = (byte) 0x04;
    /**
     * SOCKS uses Nulls as field delimiters
     */
    private final byte SOCKS_DELIM = (byte) 0x00;
    /**
     * Setting the IP field to 0.0.0.1 causes SOCKS4a to be enabled.
     */
    private final int SOCKS4A_FAKEIP = (int) 0x01;

    private static final ArrayList openPorts = new ArrayList();
    public ProxyURLConnection(URL url) throws MalformedURLException
    {
        super(new URL(url.getFile().substring(1)));
        if (url.getProtocol().equals("tor"))
            this.proxyPort = Integer.parseInt(url.getHost());
       // String addr = url.getFile();
        
        
    }
    @Override
    public void connect() throws IOException {
       TorSocket();
    }
    
     /**
     * This method creates a socket to the target host and port using
     * TorSocketPre, then reads the SOCKS information.
     *
     * @param targetHostname Hostname of destination host.
     * @param targetPort Port on remote destination host.
     * @return Fully initialized TCP Socket that tunnels to the target Host/Port
     * via the Tor Proxy host/port.
     * @throws IOException when Socket and Read/Write exceptions occur.
     */
    private void TorSocket()throws IOException {
        this.s = TorSocketPre( TOR_CONNECT);
        DataInputStream is = new DataInputStream(s.getInputStream());

        // only the status is useful on a TOR CONNECT
        byte version = is.readByte();
        byte status = is.readByte();
        if (status != (byte) 90) {
            //failed for some reason, return useful exception
            throw (new IOException(ParseSOCKSStatus(status)));
        }
//		System.out.println("status: "+ParseSOCKSStatus(status));
        int port = is.readShort();
        int ipAddr = is.readInt();
        //return (s);
    }
    
    /**
     * This method Creates a socket, then sends the inital SOCKS request info It
     * stops before reading so that other methods may differently interpret the
     * results. It returns the open socket.
     *
     * @param targetHostname The hostname of the destination host.
     * @param targetPort The port to connect to
     * @param req SOCKS/TOR request code
     * @return An open Socket that has been sent the SOCK4a init codes.
     * @throws IOException from any Socket problems
     */
    private Socket TorSocketPre( byte req)
            throws IOException {

       // Socket s;
//		System.out.println("Opening connection to "+targetHostname+":"+targetPort+
//				" via proxy "+proxyAddr+":"+proxyPort+" of type "+req);
       Socket sock = new Socket(proxyAddr, proxyPort);
        DataOutputStream os = new DataOutputStream(sock.getOutputStream());
        os.writeByte(SOCKS_VERSION);
        os.writeByte(req);
        // 2 bytes 
        int targetPort = this.url.getPort();
        if (targetPort == 0)
            targetPort = 80;
        os.writeShort(targetPort);
        // 4 bytes, high byte first
        os.writeInt(SOCKS4A_FAKEIP);
        os.writeByte(SOCKS_DELIM);
        String targetHostname = this.url.getHost();
        os.writeBytes(targetHostname);
        os.writeByte(SOCKS_DELIM);
        return (sock);
    }

     /**
     * This helper method allows us to decode the SOCKS4 status codes into Human
     * readible input.<br />
     * Based upon info from
     * http://archive.socks.permeo.com/protocol/socks4.protocol
     *
     * @param status Byte containing the status code.
     * @return String human-readible representation of the error.
     */
    private String ParseSOCKSStatus(byte status) {
        // func to turn the status codes into useful output
        // reference 
        String retval;
        switch (status) {
            case 90:
                retval = status + " Request granted.";
                break;
            case 91:
                retval = status + " Request rejected/failed - unknown reason.";
                break;
            case 92:
                retval = status + " Request rejected: SOCKS server cannot connect to identd on the client.";
                break;
            case 93:
                retval = status + " Request rejected: the client program and identd report different user-ids.";
                break;
            default:
                retval = status + " Unknown SOCKS status code.";
        }
        return (retval);

    }
    @Override
    public InputStream getInputStream() throws IOException {
        return this.s.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return this.s.getOutputStream(); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1.web.proxy;

/**
 * Tinfoil is an RFID Privacy and Security Enhancement library.
 *
 * Copyright (c) 2005 Joe Foley, MIT AutoID Labs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
// $HeadURL: $
// $Id: $
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javafx.scene.web.WebView;

/**
 * The Onion Router Java Library routines<br />
 * These methods allow us to setup an anonymized TCP socket through the Tor
 * network and do safe anonymized DNS lookups.<br />
 * This code was written with the help of Roger Dingledine and Nick
 * Mathewson.<br />
 * The code is open-source under the MIT X11 license.
 * <ul>
 * <li><a href = "http://tor.eff.org"> http://tor.eff.org</a>
 * <li><a href = "http://tor.eff.org/cvs/control/doc/howto.txt">http://tor.eff.org/cvs/control/doc/howto.txt</a>
 * <li><a href = "http://www.mokabyte.it/2000/06/firewallutil.htm">http://www.mokabyte.it/2000/06/firewallutil.htm</a>
 * </ul>
 *
 * @author Joe Foley<foley at MIT dot EDU>, MIT AutoID Labs
 * @version 1.0
 * <p>
 */
public class TorLib {
    
    public static final String GET = "GET";
    public static final String POST = "POST";
    public final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36";

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
    
    public static void clearHostobjects() {
        openPorts.clear();
    }
     
    
    public TorLib(String host, int targetPort)
    {
         if (host != null && !host.equals("")) {
                this.proxyAddr = host;
            }
            if (targetPort > 0) {
                this.proxyPort = targetPort;
            }
    }
    
    /**
     * @param host - proxyHost usually 127.0.0.1
     * @parma targetPort proxy port this is the port where tor instance is running
     *    you can have more than one Tor instance running on different ports so 
     *   connect with desire one
     * @param connectionType one of GET or POST
     * @param url - address
     * @param headerSetings this is a header parameters if any or empty map
     *      "Cookies","Value" or "User-Agent", USER_AGENT
     */
    public String openTorConnection( String url,HashMap headerSettings,String connectionType) {
        StringBuffer b = new StringBuffer();
        try {
            
            if (connectionType == null || connectionType.equals("")
                     || (!connectionType.equals(GET) && !connectionType.equals(POST)))
                connectionType = GET; // default one
            URL u = new URL(url);
            String targetHostname = u.getHost();
            String targetDir = u.getFile();//"index.html";
           int targetPort = 80;

            Socket s = TorSocket(targetHostname, targetPort);
            DataInputStream is = new DataInputStream(s.getInputStream());
            PrintStream out = new java.io.PrintStream(s.getOutputStream());

            //Construct an HTTP request
            String userAgent = (String)headerSettings.get("User-Agent");
            if (userAgent != null && !userAgent.equals(""))
            {
                headerSettings.remove("User-Agent");
            }else
            {
                userAgent = USER_AGENT; 
                         
            }
            out.print(connectionType + "  /" + targetDir + " HTTP/1.0\r\n");
            out.print("Host: " + targetHostname + ":" + targetPort + "\r\n");
            out.print("User-Agent:" + userAgent + "\r\n");
            out.print("Accept: */*\r\n");
            if (headerSettings.keySet().size() > 0)
            {
                Iterator it = headerSettings.keySet().iterator();
                while (it.hasNext())
                {
                    String key = (String)it.next();
                    String value = (String)headerSettings.get(key);
                    out.print(key + ":" + value +"\r\n");
                }
            }
            out.print("Connection: Keep-Alive\r\n");
            out.print("Pragma: no-cache\r\n");
            out.print("\r\n");
            out.flush();

            // this is from Java Examples In a Nutshell
            final InputStreamReader from_server = new InputStreamReader(is);
            char[] buffer = new char[1024];
            int chars_read;
            
            // read until stream closes
            while ((chars_read = from_server.read(buffer)) != -1) {
                // loop through array of chars
                // change \n to local platform terminator
                // this is a nieve implementation
                for (int j = 0; j < chars_read; j++) {
                    if (buffer[j] == '\n') {
                        System.out.println();
                    } else {
                        b.append(buffer);
                        System.out.print(buffer[j]);
                    }
                }
                System.out.flush();
            }
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            return b.toString();
        }
    }

    class Hostobject {

        public String hostName;
        public int port;

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Hostobject) {
                return (this.port == ((Hostobject) obj).port);
            } else {
                return false; //To change body of generated methods, choose Tools | Templates.
            }
        }

    }

    /**
     * This method allows you to demo/access the resolver/socket generation from
     * the command line. Run with "-h" to get the help menu.
     *
     * @param args Command line arguments for test main method.
     */
    public static void main(String[] args) {
        String req = "-w";
        String targetHostname = "how2seduce.blogspot.com";//"tor.eff.org";
        String targetDir = "/";//"index.html";
        int targetPort = 80;
        TorLib torLib = new TorLib(null,0);
        //WebView v = new WebView();
        //v.getEngine().setU

        if (args.length > 0 && args[0].equals("-h")) {
            System.out.println("Tinfoil/TorLib - interface for using Tor from Java\n"
                    + "By Joe Foley<foley@mit.edu>\n"
                    + "Usage: java Tinfoil.TorLib <cmd> <args>\n"
                    + "<cmd> can be: -h for help\n"
                    + "              -r for resolve\n"
                    + "              -w for wget\n"
                    + "For -r, the arg is:\n"
                    + "  <hostname> Hostname to DNS resolve\n"
                    + "For -w, the args are:\n"
                    + "   <host> <path> <optional port>\n"
                    + " for example, http://tor.eff.org:80/index.html would be\n"
                    + "   tor.eff.org index.html 80\n"
                    + " Since this is a demo, the default is the tor website.\n");
            System.exit(2);
        }

        if (args.length >= 4) {
            targetPort = new Integer(args[2]).intValue();
        }
        if (args.length >= 3) {
            targetDir = args[2];
        }
        if (args.length >= 2) {
            targetHostname = args[1];
        }
        if (args.length >= 1) {
            req = args[0];
        }

        if (req.equals("-r")) {
            System.out.println(torLib.TorResolve(targetHostname));
        } else if (req.equals("-w")) {
            try {
                Socket s = torLib.TorSocket(targetHostname, targetPort);
                DataInputStream is = new DataInputStream(s.getInputStream());
                PrintStream out = new java.io.PrintStream(s.getOutputStream());

                //Construct an HTTP request
                out.print("GET  /" + targetDir + " HTTP/1.0\r\n");
                out.print("Host: " + targetHostname + ":" + targetPort + "\r\n");
                out.print("Accept: */*\r\n");
                out.print("Connection: Keep-Alive\r\n");
                out.print("Pragma: no-cache\r\n");
                out.print("\r\n");
                out.flush();

                // this is from Java Examples In a Nutshell
                final InputStreamReader from_server = new InputStreamReader(is);
                char[] buffer = new char[1024];
                int chars_read;

                // read until stream closes
                while ((chars_read = from_server.read(buffer)) != -1) {
                    // loop through array of chars
                    // change \n to local platform terminator
                    // this is a nieve implementation
                    for (int j = 0; j < chars_read; j++) {
                        if (buffer[j] == '\n') {
                            System.out.println();
                        } else {
                            System.out.print(buffer[j]);
                        }
                    }
                    System.out.flush();
                }
                s.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
    private Socket TorSocketPre(String targetHostname, int targetPort, byte req)
            throws IOException {

        Socket s;
//		System.out.println("Opening connection to "+targetHostname+":"+targetPort+
//				" via proxy "+proxyAddr+":"+proxyPort+" of type "+req);
        s = new Socket(proxyAddr, proxyPort);
        DataOutputStream os = new DataOutputStream(s.getOutputStream());
        os.writeByte(SOCKS_VERSION);
        os.writeByte(req);
        // 2 bytes 
        os.writeShort(targetPort);
        // 4 bytes, high byte first
        os.writeInt(SOCKS4A_FAKEIP);
        os.writeByte(SOCKS_DELIM);
        os.writeBytes(targetHostname);
        os.writeByte(SOCKS_DELIM);
        return (s);
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
    private Socket TorSocket(String targetHostname, int targetPort)
            throws IOException {
        Socket s = TorSocketPre(targetHostname, targetPort, TOR_CONNECT);
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
        return (s);
    }

    /**
     * This method opens a TOR socket, and does an anonymous DNS resolve through
     * it. Since Tor caches things, this is a very fast lookup if we've already
     * connected there The resolve does a gethostbyname() on the exit node.
     *
     * @param targetHostname String containing the hostname to look up.
     * @return String representation of the IP address: "x.x.x.x"
     */
    private String TorResolve(String targetHostname) {
        int targetPort = 0; // we dont need a port to resolve

        try {
            Socket s = TorSocketPre(targetHostname, targetPort, TOR_RESOLVE);
            DataInputStream is = new DataInputStream(s.getInputStream());

            byte version = is.readByte();
            byte status = is.readByte();
            if (status != (byte) 90) {
                //failed for some reason, return useful exception
                throw (new IOException(ParseSOCKSStatus(status)));
            }
            int port = is.readShort();
            byte[] ipAddrBytes = new byte[4];
            is.read(ipAddrBytes);
            InetAddress ia = InetAddress.getByAddress(ipAddrBytes);
            //System.out.println("Resolved into:"+ia);
            is.close();
            String addr = ia.toString().substring(1); // clip off the "/"
            return (addr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (null);
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
}

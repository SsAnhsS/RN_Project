package data;

import java.io.BufferedReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class SenderThread {
	private BufferedReader reader;
	private Socket socket;
	private Client client;
	
	private InetAddress serverIPAddress;
	private DatagramSocket udpClientSocket;
	private boolean stopped = false; 
	private int serverPort;
}

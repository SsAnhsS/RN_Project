package data;

import java.io.*;
import java.net.*;

/**
 * UDP Sender Thread
 */
public class SenderThread extends Thread{

	private InetAddress ipAddress;
	private int port;
	
	private DatagramSocket udpClientSocket;
	
	
	public SenderThread(int port, InetAddress ipAddress)throws SocketException{
		this.port = port;
		this.ipAddress = ipAddress;
		
		this.udpClientSocket = new DatagramSocket();
		//this.udpClientSocket.connect(serverIPAddress, serverPort);
	}
	
	public void run() {
		try {
			BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			
			String sendInput = inFromUser.readLine();
			
			sendMessage(sendInput);
			
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * message send
	 * @param message
	 * @throws IOException
	 */
	public void sendMessage(String message) throws IOException{
		byte[] sendData = message.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
		udpClientSocket.send(sendPacket);
	}
	
	
}

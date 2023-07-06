package data;

import java.io.IOException;
import java.net.*;

/**
 * UDP Receiver Thread
 */
public class ReceiverThread extends Thread{
	private DatagramSocket udpClientSocket;
	
	public ReceiverThread(int port, InetAddress ipAddress) throws SocketException {
		this.udpClientSocket = new DatagramSocket(port, ipAddress);
	}
	
	public void run() {
		
		byte[] receiveData = new byte[1024];
		
		try {
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			
			udpClientSocket.setSoTimeout(2000);
			udpClientSocket.receive(receivePacket);
			
			String receiveMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
			
			/**
			 * boolean received = false;
			
			while(!received) {
				try {
					
					received = true;
				}catch(Exception e) {
					System.out.println("Timeout occurred. Resending ...");
					
				}
			}
			 */
			
			
			
			
			Thread.sleep(2000);
		}
		catch(IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

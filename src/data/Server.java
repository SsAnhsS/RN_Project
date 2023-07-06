package data;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * TCP Server 
 */
public class Server {
	
	private ServerSocket serverSocket;
	
	public Server() {
		try {
			this.serverSocket = new ServerSocket(6789);
			System.out.println("Wait for Client ...");
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run_forever() {
		while(true) {
			try{

				//HashMap<String, Integer> activeUsersList = new HashMap<>(); 
				
				Socket clientSocket = this.serverSocket.accept();
				System.out.printf("Client %s connected", clientSocket.getRemoteSocketAddress());
				System.out.println();
				Thread newThread = new ClientHandler(clientSocket);
				newThread.start();
				
			}catch(IOException e) {
				System.out.println("Connection with Client is closed!");
				return;
			}
		}
		
		
		
	}
	
	public static void main(String [] args) {
		final Server server = new Server();
		server.run_forever();
	}
}

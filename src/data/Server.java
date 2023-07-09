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
	private HashMap<String, Socket> activeUsersList;
	
	public Server() {
		try {
			serverSocket = new ServerSocket(6789);
			activeUsersList = new HashMap<>();
			
			System.out.println("Wait for Client ...");
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run_forever() {
		while(true) {
			try{
				
				Socket clientSocket = serverSocket.accept();
				System.out.printf("Client %s connected", clientSocket.getRemoteSocketAddress());
				System.out.println();
				
				Thread newThread = new ClientHandler(clientSocket, activeUsersList);
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

package data;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
	
	private ServerSocket serverSocket;
	private Management management; 
	
	BufferedReader reader;
	DataOutputStream outToClient;
	DataInputStream inFromClient;
	
	public Server() {
		try {
			this.serverSocket = new ServerSocket(6789);
			management = new Management();
			System.out.println("Wait for Client ...");
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run_forever() {
		try{
			
			while(true) {
				Socket clientSocket = this.serverSocket.accept();
				System.out.println("Client %s connected" + clientSocket.getRemoteSocketAddress());
				Thread newThread = new ClientHandler(clientSocket);
				newThread.start();
			}
			
		}catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String [] args) {
		Server server = new Server();
		server.run_forever();
	}
}

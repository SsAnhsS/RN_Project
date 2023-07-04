package data;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class ClientHandler extends Thread {
	Socket socket;
	
	BufferedReader inFromClient;
	DataOutputStream outToClient;
	
	BufferedWriter outToServer;
	
	String command;
	
	String databaseFile = "database/database.txt";
	BufferedReader fileReader;
	BufferedWriter fileWriter;
	
	private List<User> activeUsers = new ArrayList<>();
	
	public ClientHandler (Socket socket) throws IOException {
		this.socket = socket;
	}
	
	public void run() throws RuntimeException{
		while(true) {
			try {
				String username, passwort;
				
				outToClient = new DataOutputStream(socket.getOutputStream());
				
				inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				outToServer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				
				command = inFromClient.readLine();
				
				if(command!= null) {
					System.out.printf("FROM CLIENT (%s) %s%n", socket.getRemoteSocketAddress(), command);
					
					switch (command) {
					case "R": case "Registier":
						boolean confirm = false;
						while(!confirm) {
							outToClient.writeBytes("Username: " + '\n');
							username = inFromClient.readLine();
							outToClient.writeBytes("Passwort: " + '\n');
							passwort = inFromClient.readLine();
							
							outToClient.writeBytes("Confirm \"Yes(Y)\" or \"No(N)\" ?" + '\n');
							String confirmCommand = inFromClient.readLine();
							switch(confirmCommand) {
							case "Yes": case "Y":
								User newUser = new User(username, passwort);
								register(newUser);
								System.out.println(username + " registed.");
								outToClient.writeBytes("OK" + '\n');
								confirm = true;
								break;
								
							case "No": case "N": default:
								outToClient.writeBytes("You hasn't confirm!" + '\n');
								confirm = false;
								break;
							}
						}	
						
						break;
						
					case "LI": case "Login":
						boolean logIn = false;
						while(!logIn) {
							outToClient.writeBytes("Username: " + '\n');
							username = inFromClient.readLine();
							outToClient.writeBytes("Passwort: " + '\n');
							passwort = inFromClient.readLine();
							User user = new User(username, passwort);
							if(login(user)) {
								outToClient.writeBytes("OK" + '\n');
								logIn = true;
							}
							else {
								outToClient.writeBytes("Username or passwort is wrong!" + '\n');
								logIn = false;
								continue;
							}
						}
						
						
						break;
						
					case "LO": case "Logout":
						System.out.printf("Client %s logout");
						break;
					}
					
				}
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void register(User user) throws Exception {
		fileWriter = new BufferedWriter(new FileWriter(databaseFile, true));
		fileWriter.write(user.getUsername() + " " + user.getPasswort() + '\n');
		fileWriter.close();
	}
	
	public boolean login(User user) throws Exception{
		fileReader = new BufferedReader(new FileReader(databaseFile));
		String fileLine = fileReader.readLine();
		
		while(fileLine != null) {
			if(user.getUsername().equals(fileLine.split(" ")[0]) && user.getPasswort().equals(fileLine.split(" ")[1])) {
				fileReader.close();
				return true;
			}
			fileLine = fileReader.readLine();
		}
		
		fileReader.close();
		return false;
	}
	
	public synchronized void addActiveUser(User user) {
		activeUsers.add(user);
	}
	
	public synchronized void removeActiveUser(User user) {
		activeUsers.remove(user);
	}
	
	public synchronized List<User> getActiveUser(){
		return activeUsers;
	}
	
	
}

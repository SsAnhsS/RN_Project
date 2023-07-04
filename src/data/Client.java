package data;

import java.io.*;
import java.net.*;

public class Client {
	
	Socket clientSocket;
	
	DataOutputStream outToServer;
	
	BufferedReader inFromUser;
	BufferedReader inFromServer;
	

	String databaseFile = "database/database.txt";
	BufferedReader fileReader;
	
	public Client() {
		try {
			clientSocket = new Socket("localhost", 6789);
			
			outToServer = new DataOutputStream(clientSocket.getOutputStream());
			
			inFromUser = new BufferedReader(new InputStreamReader(System.in));
			inFromServer= new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() throws Exception {
		String command;
		String message;
		String username, passwort;
		
		try {
			do {
				System.out.println("Client connected end with \"END\"");
				System.out.println("Choose with \"Register(R)\", \"Login(LI)\" oder \"Logout(LO)\"");
				command = inFromUser.readLine();
				
				if(command != null) {
					outToServer.writeBytes(command + '\n');
					switch(command) {
					case "R": case "Register":
						boolean isValid = false;
						
						message = inFromServer.readLine();
						System.out.println(message);
						//check that username was used or not
						while(!isValid) {
							username = inFromUser.readLine();
							
							if(validCheck(username)) {
								outToServer.writeBytes(username + '\n');
								isValid = true;
							}
							else {
								System.out.println("Usename was used! Write another!");
								isValid = false;
							}
						}
						
						message = inFromServer.readLine();
						System.out.println(message);
						passwort = inFromUser.readLine();
						outToServer.writeBytes(passwort + '\n');
						
						message = inFromServer.readLine();
						System.out.println("FROM SERVER: " + message);
						String confirm = inFromUser.readLine();
						outToServer.writeBytes(confirm + '\n');
						message = inFromServer.readLine();
						System.out.println("FROM SERVER: " + message);
						break;
						
					case "LI": case "Login":
						while(true) {
							message = inFromServer.readLine();
							System.out.println(message);
							username = inFromUser.readLine();
							outToServer.writeBytes(username + '\n');
							
							message = inFromServer.readLine();
							System.out.println(message);
							passwort = inFromUser.readLine();
							outToServer.writeBytes(passwort + '\n');
							
							message = inFromServer.readLine();
							System.out.println("FROM SERVER: " + message);
							
							if(message.equals("OK")) {
								break;
							}
						}
						break;
						
					case "LO": case "Logout":
						break;
					default:
						System.out.println("Unknow command!");
						break;
					}
					
					System.out.println();
				}
				
				
				
			} while(!command.equals("END"));
			
			clientSocket.close();
			
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if(clientSocket != null && !clientSocket.isClosed()) {
				try {
					clientSocket.close();
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public static void main(String[] args) throws Exception{
		Client client = new Client();
		client.run();
	}
	
	public boolean validCheck(String username) throws Exception {
		fileReader = new BufferedReader(new FileReader(databaseFile));
		String fileLine = fileReader.readLine();
		
		while(fileLine != null) {
			if(username.equals(fileLine.split(" ")[0])) {
				fileReader.close();
				return false;
			}
			fileLine = fileReader.readLine();
		}
		
		fileReader.close();
		return true;
	}
	
	
}

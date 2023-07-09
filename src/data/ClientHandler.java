package data;

import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.*;

/**
 * ClientHandler Thread
 */
public class ClientHandler extends Thread {
	Socket socket;

	HashMap<String, Socket> activeUsersList; 
	
	BufferedReader inFromClient;
	DataOutputStream outToClient;
	
	String command;
	
	String databaseFile = "database/database.txt";
	String activeUsersFile = "database/activeUsers.txt";
	BufferedReader fileReader;
	BufferedWriter fileWriter;
	
	String host;
	String guest;
	boolean accepted = false;
	
	String localhost = "localhost";
	
	public ClientHandler (Socket socket, HashMap<String, Socket> activeUsersList) throws IOException {
		this.socket = socket;
		this.activeUsersList = activeUsersList;
	}
	
	public void run(){
		try {
			String username, passwort;
			boolean isLogin = false;
			
			outToClient = new DataOutputStream(socket.getOutputStream());
			
			inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			do {
				command = inFromClient.readLine();
				
				if(command!= null) {
					System.out.printf("FROM CLIENT (%s) %s%n", socket.getRemoteSocketAddress(), command);
					
					switch (command) {
					case "R": case "Registier":
						outToClient.writeBytes("Username: " + '\n');
						username = inFromClient.readLine();
						outToClient.writeBytes("Passwort: " + '\n');
						passwort = inFromClient.readLine();
						
						outToClient.writeBytes("Confirm ? \"Yes(Y)\" or \"No(N)\"" + '\n');
						String confirmCommand = inFromClient.readLine();
						switch(confirmCommand) {
						case "Yes": case "Y":
							User newUser = new User(username, passwort);
							register(newUser);
							System.out.println(username + " registed.");
							outToClient.writeBytes("OK" + '\n');
		
							break;
							
						case "No": case "N":
							outToClient.writeBytes("You hasn't confirm!" + '\n');
							
							break;
						default:
							System.out.println("Unknow command!");
							break;
						}
						
						break;
						
					case "LI": case "Login":
						
						while(!isLogin) {

							outToClient.writeBytes("Username: " + '\n');
							username = inFromClient.readLine();
							outToClient.writeBytes("Passwort: " + '\n');
							passwort = inFromClient.readLine();
							User user = new User(username, passwort);
							
							if(login(user)) {
								outToClient.writeBytes("OK" + '\n');
								
								activeUsersList.put(username, socket);
								saveActiveUsersInFile();
								
								isLogin = true;
								break;
							}
							else {
								outToClient.writeBytes("Username or passwort is wrong!" + '\n');
								isLogin = false;
								continue;
							}
						}
						
						
						break;
						
					case "LO": case "Logout":
						
						String name = null;
						
						for(Map.Entry<String, Socket> i : activeUsersList.entrySet()) {
							if(i.getValue().getRemoteSocketAddress().equals(socket.getRemoteSocketAddress()))
								name = i.getKey();
						}
						
						activeUsersList.remove(name);
						
						saveActiveUsersInFile();
						outToClient.writeBytes("OK" + '\n');
						
						break;
						
					case "C": case "Chat":
						outToClient.writeBytes("Choose one active user to connect with form \"guestUsername\"." + '\n');
						outToClient.writeBytes("List of active Users: " + getActiveUsersList() + '\n');
						
						String hostName = null;
						
						for(Map.Entry<String, Socket> i : activeUsersList.entrySet()) {
							if(i.getValue().getRemoteSocketAddress().equals(socket.getRemoteSocketAddress()))
								hostName = i.getKey();
						}
						
						String guestName = inFromClient.readLine();
						
						outToClient.writeBytes("Connection confirm? \"Yes(Y)\" or \"No(N)\"" + '\n');
						confirmCommand = inFromClient.readLine();
						
						if(confirmCommand.equals("Y") || confirmCommand.equals("Yes")) {
							outToClient.writeBytes("Waiting the connection!" +'\n');
							sendInvite(hostName, guestName);
						}
						else break;
						
						break;
					}
				}
				
				//send the invite or next to client
				outToClient.writeBytes("Next" + '\n'); 
				
			} while (!command.equals("LO") && !command.equals("Logout"));
			
			socket.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param sendPort
	 * @param receivePort
	 * @throws IOException
	 */
	public void chat(int sendPort, int receivePort) throws IOException {
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];
		DatagramSocket udpServerSocket = new DatagramSocket(sendPort);
		
		while(true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			udpServerSocket.receive(receivePacket);
			
			String sendMessage = (new String(receivePacket.getData())).trim();
			
			InetAddress sendIP = receivePacket.getAddress();
			
			sendData = sendMessage.getBytes();
			
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, sendIP, receivePort);
			udpServerSocket.send(sendPacket);
		}
	}
	
	public void sendInvite(String senderName, String receiverName) throws IOException {
		String confirm = null;
		for (String username : activeUsersList.keySet()) {
			if(username.equals(receiverName)) {
				Socket socket = activeUsersList.get(username);
				DataOutputStream output = new DataOutputStream(socket.getOutputStream());
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				output.writeBytes("INVITE" + '\n');
				output.writeBytes("Accept invite from " + senderName + "? \"Yes(Y)\" or \"No(N)\"" + '\n');
				confirm = input.readLine();
			}
		}
		
		if(confirm.equals("Y") || confirm.equals("Yes"))
			accepted = true;
		else if (confirm.equals("N") || confirm.equals("No"))
			accepted = false;
	}
	/**
	 * Show the list of active users in string
	 * @return string 
	 */
	public String getActiveUsersList(){
		String activeList = "";

		for(String user : activeUsersList.keySet()) {
			activeList += user + " | ";
		}
		return activeList;
	}
	
	
	/**
	 * register new user
	 * @param user
	 * @throws IOException
	 */
	public void register(User user) throws IOException {
		fileWriter = new BufferedWriter(new FileWriter(databaseFile, true));
		fileWriter.write(user.getUsername() + " " + user.getPasswort()+'\n');
		fileWriter.newLine();
		fileWriter.flush();
		fileWriter.close();
	}
	
	/**
	 * login 
	 * vergleichen username und passwort mit Daten in database
	 * @param user
	 * @return
	 * @throws IOException
	 */
	public boolean login(User user) throws IOException{
		fileReader = new BufferedReader(new FileReader(databaseFile));
		String fileLine = null;
		
		while((fileLine = fileReader.readLine())!= null) {
			if(user.getUsername().equals(fileLine.split(" ")[0]) && user.getPasswort().equals(fileLine.split(" ")[1])) {
				fileReader.close();
				return true;
			}
		}
		
		fileReader.close();
		
		
		return false;
	}
	
	/**
	 * save active users in activeUsers.txt
	 * @throws IOException
	 */
	public void saveActiveUsersInFile() throws IOException{
		
		Set<Map.Entry<String, Socket>> setHashMap = activeUsersList.entrySet();
		
		fileWriter = new BufferedWriter(new FileWriter(activeUsersFile, false));
		
		for(Map.Entry<String, Socket> i : setHashMap) {
			fileWriter.write(i.getKey() + " " + i.getValue().getRemoteSocketAddress());
			fileWriter.newLine();
		}
		fileWriter.close();	
	}
	
}

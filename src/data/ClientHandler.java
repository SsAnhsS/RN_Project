package data;

import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.io.*;

/**
 * ClientHandler Thread
 */
public class ClientHandler extends Thread {
	Socket socket;
	
	BufferedReader inFromClient;
	DataOutputStream outToClient;
	
	BufferedWriter outToServer;
	
	String command;
	
	String databaseFile = "database/database.txt";
	String activeUsersFile = "database/activeUsers.txt";
	BufferedReader fileReader;
	BufferedWriter fileWriter;
	
	String host;
	String guest;
	int hostPort;
	int guestPort;
	String localhost = "localhost";
	
	int sendPort;
	int receivePort;
	
	HashMap<String, Integer> activeUsersList = new HashMap<>(); 
	
	public ClientHandler (Socket socket) throws IOException {
		this.socket = socket;
	}
	
	public void run() throws RuntimeException{
		while(true) {
			try {
				String username, passwort;
				boolean isLogin = false;
				
				outToClient = new DataOutputStream(socket.getOutputStream());
				
				inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				outToServer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				
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
							int userPort = getPort(socket.getRemoteSocketAddress());
							
							if(login(user)) {
								outToClient.writeBytes("OK" + '\n');
								
								activeUsersList.put(user.getUsername(), userPort);
								
								String port = user.getUsername() +" " + String.valueOf(userPort);
								saveActiveUser();
								
								outToClient.writeBytes(port + '\n');
								
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
						String portString = inFromClient.readLine();
						int port = Integer.parseInt(portString);
						
						String name = getName(port);
						//activeUsersList.remove(name);
						//saveActiveUser();
						logout(name);
						outToClient.writeBytes("OK" + '\n');
						
						break;
						
					case "C": case "Chat":
						outToClient.writeBytes("Choose one active user to connect with form \"guestUsername\"." + '\n');
						outToClient.writeBytes("List of active Users: " + getActiveUsersList() + '\n');
						
						String guestName = inFromClient.readLine();
						
						outToClient.writeBytes("Connection confirm? \"Yes(Y)\" or \"No(N)\"" + '\n');
						confirmCommand = inFromClient.readLine();
						
						switch (confirmCommand) {
						case "Y": case "Yes":
							outToClient.writeBytes("Waiting the connection!" +'\n');
							hostPort = getPort(socket.getRemoteSocketAddress());
							guestPort = getPortnumber(guestName);
							
							outToClient.writeInt(hostPort);
							outToClient.writeInt(guestPort);
							
							chat(hostPort, guestPort);
							break;
						case "N": case "No":
							outToClient.writeBytes("Connect request is refused!" + '\n');
							break;
						}
						
						
						break;
					}
					
					
				}
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
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
	 * get portnumber of user
	 * @param user
	 * @return portnumber
	 */
	public int getPortnumber(String user){
		Integer num = null;
		Set<Map.Entry<String, Integer>> setHashMap = activeUsersList.entrySet();
		for(Map.Entry<String, Integer> i : setHashMap) {
			if(i.getKey().equals(user))
				num = i.getValue();
		}
		return num!= null ? num : 0;
	}
	
	/**
	 * get username of portnumber 
	 * @param port
	 * @return username
	 */
	public String getName(int port){
		
		Set<Map.Entry<String, Integer>> setHashMap = activeUsersList.entrySet();
		for(Map.Entry<String, Integer> i : setHashMap) {
			if(i.getValue() == port)
				return i.getKey();
		}
		return null;
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
	 * save new active user in activeUsers.txt
	 * @throws IOException
	 */
	public void saveActiveUser() throws IOException{
		Set<Map.Entry<String, Integer>> setHashMap = activeUsersList.entrySet();
		
		fileWriter = new BufferedWriter(new FileWriter(activeUsersFile, true));
		
		for(Map.Entry<String, Integer> i : setHashMap) {
			fileWriter.write(i.getKey() + " " + i.getValue());
			fileWriter.newLine();
		}
		fileWriter.close();	
	}
	
	/**
	 * delete user from list of active users and file activeUsers.txt
	 * @param name
	 * @throws Exception
	 */
	public void logout(String name) throws Exception{
		activeUsersList.remove(name);
		
		fileWriter = new BufferedWriter(new FileWriter(activeUsersFile, false));
		
		for(Map.Entry<String, Integer> i : activeUsersList.entrySet()) {
			//System.out.println(i.getKey() + " " + i.getValue());
			fileWriter.write(i.getKey() + " " + i.getValue());
			fileWriter.newLine();
		}
		fileWriter.close();	
		
		/**
		 * ArrayList <String> ports = new ArrayList <>();
		fileReader = new BufferedReader(new FileReader(activeUsersFile));
		
		String lineReader = null;
		
		while((lineReader = fileReader.readLine())!= null) {
			if(!lineReader.equals(s)) {
				ports.add(lineReader);
			}
		}
		
		fileReader.close();
		
		fileWriter = new BufferedWriter(new FileWriter(activeUsersFile, false));
		
		for(int i = 0; i < ports.size(); i++) {
			fileWriter.write(ports.get(i));
			fileWriter.newLine();
		}
		fileWriter.flush();
		fileWriter.close();
		
		 */
		
	}
	
	/**
	 * get portnumber of a socket address
	 * @param address
	 * @return portnumber
	 */
	public int getPort(SocketAddress address) {
	    return ((InetSocketAddress) address).getPort();
	}
	
	/**
	public String userAndAddress(User user, SocketAddress address) {
		String string = user.getUsername() + " " + String.valueOf(getPort(socket.getRemoteSocketAddress()));
		return string;
	}
	**/
	
}

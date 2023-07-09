package data;

import java.io.*;
import java.net.*;

/**
 * TCP Client
 */
public class Client {
	
	Socket clientSocket;
	
	DataOutputStream outToServer;
	
	BufferedReader inFromUser;
	BufferedReader inFromServer;
	
	String name;
	String portString;
	
	int hostPort;
	int guestPort;
	
	String databaseFile = "database/database.txt";
	String activeUsersFile = "database/activeUsers.txt";
	BufferedReader fileReader;
	BufferedWriter fileWriter;
	
	String localhost = "localhost";
	
	public Client() {
		try {
			clientSocket = new Socket("localhost", 6789);
			
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() throws Exception {
		String command;
		String message;
		String username, passwort;
		boolean isValid;
		
		outToServer = new DataOutputStream(clientSocket.getOutputStream());
		
		inFromUser = new BufferedReader(new InputStreamReader(System.in));
		inFromServer= new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
		
		try {
			System.out.println("Choose \"Register(R)\" | \"Login(LI)\"");
			
			do {
				
				System.out.println("Command: ");
				//Because the another client id still here, that why can't receive the invite from another 
				command = inFromUser.readLine();
				
				if(command != null) {
					outToServer.writeBytes(command + '\n');
					switch(command) {
					case "R": case "Register":
						isValid = false;
						
						message = inFromServer.readLine();
						System.out.println(message);
						//check that username was used or not
						while(!isValid) {
							username = inFromUser.readLine();
							
							if(validCheckToRegister(username)) {
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
						
						if(message.equals("OK")) {
							System.out.println("Register successfull!");
							System.out.println("Choose \"Login(LI)\"");
						}
						else {
							System.out.println("Choose \"Register(R)\" | \"Login(LI)\"");
						}
						
						break;
						
					case "LI": case "Login":
						
						while(true) {
							isValid = false;
							
							message = inFromServer.readLine();
							System.out.println(message);
							//check that username was used or not
							while(!isValid) {
								username = inFromUser.readLine();
								
								if(!activeUserCheck(username)) {
									outToServer.writeBytes(username + '\n');
									isValid = true;
								}
								else {
									System.out.println("Usename was logged! Login with another!");
									isValid = false;
								}
							}
							
							message = inFromServer.readLine();
							System.out.println(message);
							passwort = inFromUser.readLine();
							outToServer.writeBytes(passwort + '\n');
							
							message = inFromServer.readLine();
							System.out.println("FROM SERVER: " + message);
							
							if(message.equals("OK")) {
								System.out.println("Login successfull!");
								
								System.out.println();
								System.out.println("Choose \"Chat(C)\" | \"Logout(LO)\"");
								break;
							}
							else {
								System.out.println("Login failed. Please try again.");
								continue;
							}
						}
							
						
			
						break;
						
					case "LO": case "Logout":
						
						message = inFromServer.readLine();
						System.out.println("FROM SERVER: " + message);
						
						if(message.equals("OK")) {
							System.out.println("You hat logged out!");
							System.out.println();
						}
						
						break;
						
					case "C": case "Chat":
						message = inFromServer.readLine();
						System.out.println("FROM SERVER: " + message);
						message = inFromServer.readLine();
						System.out.println("FROM SERVER: " + message);
						
						
						String guestName = null;
						
						do {
							System.out.println("Your guest username: ");
							guestName = inFromUser.readLine();
						} while(!activeUserCheck(guestName));
						
						
						outToServer.writeBytes(guestName + '\n');
						
						message = inFromServer.readLine();
						
						System.out.println("FROM SERVER: " + message);
						String confirmCommand = null;
						
						do {
							confirmCommand = inFromUser.readLine();
							
							switch (confirmCommand) {
							case "Y": case "Yes": case "N": case "No":
								outToServer.writeBytes(confirmCommand + '\n');
								break;
							default:
								System.out.println("Unknown command!");
								break;
							}
						}while(!confirmCommand.equals("Y") && !confirmCommand.equals("Yes") && !confirmCommand.equals("N") && !confirmCommand.equals("No"));
						
						message = inFromServer.readLine();
						System.out.println("FROM SERVER: " + message);
						
						break;
					default:
						System.out.println("Unknow command!");
						break;
					}
					
				}
			
				//when the client receive the invite, can accept to connect with the sender
				if((message = inFromServer.readLine()).equals("INVITE")) {
					String confirm = null;
					message = inFromServer.readLine();
					System.out.println("FROM SERVER: " + message);
					do {
						confirm = inFromUser.readLine();
					}while(!confirm.equals("Y") && !confirm.equals("N") && !confirm.equals("Yes") && !confirm.equals("N0") );
					
					outToServer.writeBytes(confirm);
				}else continue;
				
				
			} while(!command.equals("LO") && !command.equals("Logout"));
			
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
	
	
	/**
	 *
	 * @param hostPort
	 * @param guestPort
	 * @throws UnknownHostException
	 * @throws SocketException
	 */
	public void chat(int hostPort, int guestPort) throws UnknownHostException, SocketException {
		InetAddress ia = InetAddress.getByName(localhost);
		
		SenderThread sender = new SenderThread(guestPort, ia);
		ReceiverThread receiver = new ReceiverThread(hostPort, ia);
		
		sender.start();
		receiver.start();
	}
	
	/**
	 * check username is valid for register
	 * @param username
	 * @return
	 * @throws IOException
	 */
	public boolean validCheckToRegister(String username) throws IOException {
		if(username != null) {
			fileReader = new BufferedReader(new FileReader(databaseFile));
			String fileLine = null;
			
			while((fileLine = fileReader.readLine())!= null) {
				if(username.equals(fileLine.split(" ")[0])) {
					fileReader.close();
					return false;
				}
				
			}
			fileReader.close();
			return true;
		}
		else return false;
	}
	
	/**
	 * check user is active or not
	 * @param username
	 * @return
	 * @throws IOException
	 */
	public boolean activeUserCheck(String username) throws IOException {
		if(username != null) {
			fileReader = new BufferedReader(new FileReader(activeUsersFile));
			String fileLine = null;
			
			while((fileLine = fileReader.readLine())!= null) {
				if(username.equals(fileLine.split(" ")[0])) {
					fileReader.close();
					return true;
				}
			}
			fileReader.close();
			return false;
		}
		else return false;
	}
	
	/**
	 * get portnumber of user
	 * @param address
	 * @return
	 */
	public int getPort(SocketAddress address) {
	    return ((InetSocketAddress) address).getPort();
	}
	
	public static void main(String[] args) throws Exception{
		Client client = new Client();
		client.run();
	}
	
}

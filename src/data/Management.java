package data;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Management {
	private List<User> activeUsers = new ArrayList<>();
	
	BufferedReader reader;
	BufferedWriter writer;
	FileWriter fileWriter;
	
	int userPort;
	int guestPort;
	
	
	public Management() {
		
	}
	
	/**
	 * save new user in a file
	 * @param user
	 */
	public synchronized void registerUser(User user) {
		
	}
	
	/**
	 * Check if username and password are valid from database-file
	 * @param user
	 * @return
	 */
	public synchronized boolean authenticateUser(User user) {
		return true;
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

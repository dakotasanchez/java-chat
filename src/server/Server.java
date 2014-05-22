/*
 * Dakota Sanchez
 * Chatroom Socket Server
 */

import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

//This handles all of the Clients in the Server.
public class Server {
	
	private ServerSocket serverSocket;
	private Socket socket;
	private ArrayList<User> users;
	private Thread thread;
	private PrintWriter logWriter;
	private DateFormat dateFormat;
	private Date date;

	public static void main(String[] args) throws UnknownHostException, IOException {
		
		new Server();
	}

	//Creates a new Server object
	public Server() throws UnknownHostException, IOException {
		
		//Attempt to create server socket
		try {
			
			serverSocket = new ServerSocket(55555);

		} catch(IOException e){
			System.out.println("Could not open server socket.");
			e.printStackTrace();
			return;
		}

		System.out.print("Socket " + serverSocket + " created.\nConnect to: ");

		Enumeration e = NetworkInterface.getNetworkInterfaces();
		while(e.hasMoreElements()) {
			Enumeration ee = ((NetworkInterface)e.nextElement()).getInetAddresses();
			while(ee.hasMoreElements()) {
				InetAddress i = (InetAddress)ee.nextElement();
				if(i.isSiteLocalAddress()) {
					System.out.println(i.getHostAddress());
				}
			}
		}

		users = new ArrayList<User>();
		
		//Create a new Connect Object
		thread = new Thread(new Connect());
		thread.start();
	}
	
	//The data stream for each client
	private class Outport extends Thread {
		
		private Socket socket;
		private BufferedReader in;
		private PrintWriter out;
		private User thisUser;
		
		public Outport(Socket newSocket) {
			socket = newSocket;
			start();
		}
		
		public void run() {
			
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
				
				String exit = in.readLine();
				
				if(!exit.equals("EXITINGCHATROOM")) {
					thisUser = new User(exit, out);
					users.add(thisUser);
				}
				
				while(true) {					
					
					String str = in.readLine();	
						
					if(str != null) {
						
						//Kick
						if(str.equalsIgnoreCase("CloseStreams")) {
							System.out.println("Client " + socket + " has disconnected.");
							users.remove(users.indexOf(thisUser));

							for(User u : users) {
								if(u != thisUser)
									u.writer().println("SERVER: " + thisUser.name() +
											" has been kicked from the server, laugh at their misfortune.");
							}
							in.close();
							out.close();
							socket.close();
							thisUser.closeWriter();
							break;
						}
						
						if(str.length() > 0) {
							
							//Send data to every client
							for(User u : users) {
								
								if(u != thisUser) {

									if(str.equals("EXITINGCHATROOM"))
										u.writer().println("SERVER: " + thisUser.name() + " has disconnected");							
									else if(str.contains("FIRSTCONNECT"))
										u.writer().println(str.substring(12));
									else
										u.writer().println(thisUser.name() + ": " + str);								
								}
							}	

							if(str.equals("EXITINGCHATROOM")) {
								System.out.println("Client " + socket + " has disconnected.");
								users.remove(users.indexOf(thisUser));
								break;							
							}					
						}
					}					
				}

			}catch(IOException e) {
				System.out.println("Client " + socket + " has disconnected.");
				users.remove(users.indexOf(thisUser));
			}
		}
	}
	
	//Starts the client accepting process
	private class Connect extends Thread {
		
		public void run() {
			
			System.out.println("Server has been started.");

			while(true) {
								
				try {
					socket = serverSocket.accept();
					if(socket != null) {
						System.out.println("Client " + socket + " has connected.");
						new Outport(socket);
					}
				}catch(IOException e) {
					e.printStackTrace();
				}			
			}
		}
	}
	
	//Represents a client
	private class User {
		
		private String name;
		private PrintWriter out;
		
		public User(String name, PrintWriter out) {
			this.name = name;
			this.out = out;
		}
		
		public String name() {
			return name;
		}
		
		public PrintWriter writer() {
			return out;
		}

		public void closeWriter() {
			out.close();
		}
	}
}
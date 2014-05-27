/*
 * Dakota Sanchez
 * Chatroom Socket Server
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

//This handles all of the Clients in the Server.
public class Server {

	private final int PORT = 1201;
	
	private ServerSocket serverSocket;
	private Socket socket;

	private List<User> users;
	private PrintWriter logWriter;
	private DateFormat dateFormat;
	private Date date;

	private String machineAddress;

	//Creates a new Server object
	public Server() throws UnknownHostException, IOException {
		
		//Attempt to create server socket
		try {
			
			serverSocket = new ServerSocket(PORT);

		} catch(IOException e1){
			System.out.println("Could not open server socket.");
			e1.printStackTrace();
			return;
		}

		System.out.println("Server has been started.");
		System.out.print("Socket " + serverSocket + " created.\nConnect to: ");

		Enumeration e = NetworkInterface.getNetworkInterfaces();
		while(e.hasMoreElements()) {
			Enumeration ee = ((NetworkInterface)e.nextElement()).getInetAddresses();
			while(ee.hasMoreElements()) {
				InetAddress i = (InetAddress)ee.nextElement();
				if(i.isSiteLocalAddress()) {
					machineAddress = i.getHostAddress();
					System.out.println(machineAddress);
				}
			}
		}

		users = new ArrayList<User>();
		// lock for users list
		Object lock = new Object();

		//Start accepting clients
		while(true) {				
			try {
				socket = serverSocket.accept();
				if(socket != null) {
					System.out.println("Client " + socket + " has connected.");
					new Connection(socket, users, lock);
				}
			}catch(IOException e2) {
				e2.printStackTrace();
			}			
		}
	}
}
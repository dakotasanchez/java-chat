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

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.text.DefaultCaret;

//This handles all of the Clients in the Server.
public class Server implements ActionListener, WindowListener {
	
	private ServerSocket serverSocket;
	private Socket socket;
	private ArrayList<User> users;
	private Thread thread;
	private PrintWriter logWriter;
	private DateFormat dateFormat;
	private Date date;
	
	private JFrame frame;
	private JTextArea area;
	private JPanel bottom;
	private JButton admin, save;
	private DefaultCaret caret;
	
	private String dateString;
	private boolean saveBool = false;

	public static void main(String[] args) throws UnknownHostException, IOException {
		
		new Server();
	}

	//Creates a new Server object
	public Server() throws UnknownHostException, IOException {
		
		//Name of log
		dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
		date = new Date();
		dateString = dateFormat.format(date);
		logWriter = new PrintWriter(new BufferedWriter(new FileWriter(dateString + ".txt", true)));	
		
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setTitle("Server (Connect to " + InetAddress.getLocalHost().getHostAddress() + ")");
		frame.setSize(500, 500);
		frame.setResizable(false);
		
		frame.addWindowListener(this);
		
		setLAF();
		setComponents();
		
		frame.setVisible(true);
		
		//Attempt to create server socket
		try {
			
			serverSocket = new ServerSocket(55555);

		} catch(IOException e){
			area.append("Could not open server socket.\n");
			e.printStackTrace();
			return;
		}

		//Notify which address to connect to
		area.append("Socket " + serverSocket + " created.\nConnect to: " + 
				InetAddress.getLocalHost().getHostAddress() + "\n");
		
		users = new ArrayList<User>();
		
		//Create a new Connect Object
		thread = new Thread(new Connect());
		thread.setDaemon(true);
		thread.start();
	}
	
	//Set the scroll pane and buttons
	public void setComponents() {
		
		area = new JTextArea();
		area.setEditable(false);
		area.setLineWrap(true);
		
		caret = (DefaultCaret)area.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); 
		
		frame.add(new JScrollPane(area));
		
		admin = new JButton("Remove Clients");
		admin.setBackground(new Color(20,20,20));
		admin.setForeground(Color.WHITE);
		admin.addActionListener(this);
		
		save = new JButton("Save log");
		save.setBackground(new Color(150,20,20));
		save.setForeground(Color.WHITE);
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				saveBool = true;
				save.setEnabled(false);
				save.setForeground(new Color(50,50,50));
			}
		});
		
		bottom = new JPanel();
		bottom.add(admin);
		bottom.add(save);

		frame.add(bottom, BorderLayout.SOUTH);		
	}
	
	//Change Look & Feel of user interface
	public void setLAF() {
		
		try{
			for(LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
			        break;
			    }
			}

		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//User removal dialog
	public void actionPerformed(ActionEvent evt) {
		
		Object[] list = new Object[users.size()];
		String name;
		
		for(int i = 0; i < list.length; i++) {
			list[i] = users.get(i).name();
		}
		
		if(list.length <= 0) {return;}
		
		name = (String)JOptionPane.showInputDialog (
				frame,
				"Choose who to remove",
				"User Removal",
				JOptionPane.PLAIN_MESSAGE,
				null,
				list,
				list[0]);
		
		for(int i = 0; i < users.size(); i++) {
			if(users.get(i).name().equals(name))
				users.get(i).writer().println("AdminRemovalMessage");	
		}
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
							area.append("Client " + socket + " has disconnected.\n");
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

							//Send to log
							if(str.equals("EXITINGCHATROOM")) {
								logWriter.println("\n" + thisUser.name() + " has disconnected\n");
								area.append("Client " + socket + " has disconnected.\n");
								users.remove(users.indexOf(thisUser));
								break;
															
							} else if(str.contains("FIRSTCONNECT"))
								logWriter.println(str.substring(12));
							else
								logWriter.println(thisUser.name() + ": " + str);					
						}
					}					
				}

			}catch(IOException e) {
				area.append("Client " + socket + " has disconnected.\n");
				users.remove(users.indexOf(thisUser));
				area.setCaretPosition(area.getDocument().getLength());
			}
		}
	}
	
	//Starts the client accepting process
	private class Connect extends Thread {
		
		public void run() {
			
			area.append("Server has been started.\n");

			while(true) {
				
				try {
					socket = serverSocket.accept();
					if(socket != null) {
						area.append("Client " + socket + " has connected.\n");
						area.setCaretPosition(area.getDocument().getLength());
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
	
	//Save or delete log
	public void windowClosing(WindowEvent arg0) {
		
		logWriter.close();
		if(!saveBool) {
			File file = new File(dateString + ".txt");
			file.delete();
		}
		frame.dispose();
		System.exit(0);
	}

	//Non-implemented methods for WindowListener interface
	public void windowActivated(WindowEvent arg0) {}

	public void windowClosed(WindowEvent arg0) {}

	public void windowDeactivated(WindowEvent arg0) {}

	public void windowDeiconified(WindowEvent arg0) {}

	public void windowIconified(WindowEvent arg0) {}

	public void windowOpened(WindowEvent arg0) {}

}
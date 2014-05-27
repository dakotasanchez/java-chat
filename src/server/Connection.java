import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

//The data stream for each client
public class Connection extends Thread {
		
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private User thisUser;
	private boolean exitThread;

	private List<User> users;
	private Object lock;
		
	public Connection(Socket socket, List<User> users, Object lock) {
		exitThread = false;
		this.socket = socket;
		this.users = users;
		this.lock = lock;

		start();
	}
		
	public void run() {
			
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
				
			String exit = in.readLine();
			
			if(!exit.equals("EXITINGCHATROOM")) {
				thisUser = new User(exit, out);

				synchronized(lock) {
					users.add(thisUser);
				}
			}
				
			while(!exitThread) {	

				//Message from client
				String str = in.readLine();	
				sendMessage(str);					
			}
		}catch(IOException e) {
			exit();
		}
	}

	private void sendMessage(String str) throws IOException {
		if((str != null) && (str.length() > 0)) {		
	
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
				exit();				
			}					
		}
	}

	private void exit() {
		System.out.println("Client " + socket + " has disconnected.");
			
		synchronized (lock) {
			users.remove(users.indexOf(thisUser));
		}

		try {
			in.close();
			out.close();
			socket.close();
			thisUser.closeWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		exitThread = true;
	}
}
/*Dakota Sanchez
 * GUI Chatroom Client
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.text.DefaultCaret;

@SuppressWarnings("restriction")
public class Client implements ActionListener, WindowListener {

	private final int PORT = 1201;
	private final int BROADCAST_PORT = 1202;

	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private DateFormat dateFormat;
	private Thread t;

	private DatagramSocket sock;
	private DatagramPacket pack;

	private String name, file, date;
	private boolean canada, playAudio;

	private JFrame frame;
	private JPanel bottomPanel, topPanel;
	private JTextArea chatArea;
	private JTextField textField;
	private JButton canadaButton, muteButton, alertButton;
	private JLabel label;
	private DefaultCaret caret;

	public Client() throws IOException {

		canada = false;
		playAudio = true;
		file = "alert.wav";

		dateFormat = new SimpleDateFormat("HH:mm");

		frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setTitle("Dakota Sanchez Chatroom Project");
		frame.setSize(600, 600);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.addWindowListener(this);

		setLAF();
		setComponents();
		frame.setVisible(true);

		connect("Please enter an ip address:");
	}

	//Set user interface components
	public void setComponents() {

		bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
		bottomPanel.setBackground(new Color(55, 55, 55));

		topPanel = new JPanel();
		topPanel.setBackground(new Color(55, 55, 55));

		canadaButton = new JButton("Translate to Canadian: OFF");
		canadaButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
		canadaButton.setBackground(new Color(30, 30, 30));
		canadaButton.setForeground(Color.WHITE);

		canadaButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if(canada) {
					canada = false;
					canadaButton.setText("Translate to Canadian: OFF");
					canadaButton.setBackground(new Color(30, 30, 30));
				} else {
					canada = true;
					canadaButton.setText("Translate to Canadian: ON");
					canadaButton.setBackground(new Color(150,20,20));
				}
			}
		});

		muteButton = new JButton("Mute: OFF");
		muteButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
		muteButton.setBackground(new Color(30,30,30));
		muteButton.setForeground(Color.WHITE);

		muteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if(playAudio) {
					playAudio = false;
					muteButton.setText("Mute: ON");
					muteButton.setBackground(new Color(150,20,20));
				} else {
					playAudio = true;
					muteButton.setText("Mute: OFF");
					muteButton.setBackground(new Color(30,30,30));
				}
			}
		});

		alertButton = new JButton("Alert Sound: 1");
		alertButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
		alertButton.setBackground(new Color(0, 34, 102));
		alertButton.setForeground(Color.WHITE);

		alertButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if(alertButton.getText().endsWith("1")) {
					alertButton.setText("Alert Sound: 2");
					file = "alert2.wav";
					if(playAudio)
						new AudioClip(file).start();
				} else if(alertButton.getText().endsWith("2")) {
					alertButton.setText("Alert Sound: 3");
					file = "alert3.wav";
					if(playAudio)
						new AudioClip(file).start();
				} else {
					alertButton.setText("Alert Sound: 1");
					file = "alert.wav";
					if(playAudio)
						new AudioClip(file).start();
				}
			}
		});

		label = new JLabel("                         ");
		label.setForeground(new Color(255, 255, 255));

		textField = new JTextField(40);
		textField.setVisible(false);
		textField.addActionListener(this);

		//Set main chat area
		chatArea = new JTextArea();
		chatArea.setBackground(new Color(220, 220, 220));
		chatArea.setFont(new Font("SansSerif", Font.BOLD, 13));
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);
		chatArea.setWrapStyleWord(true);

		caret = (DefaultCaret)chatArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		bottomPanel.add(label);
		bottomPanel.add(textField);

		topPanel.add(canadaButton);
		topPanel.add(muteButton);
		topPanel.add(alertButton);

		frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
		frame.add(bottomPanel, BorderLayout.SOUTH);
		frame.add(topPanel, BorderLayout.NORTH);
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
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//Add 'eh' suffix to strings
	public String canadianString(String s) {

		if(s.endsWith(".") || s.endsWith("?") || s.endsWith("!")) {
			String end = s.substring(s.length() - 1);
			int ends = 0;
			while(s.substring(s.length() - 1).equals(end)) {
				s = s.substring(0, s.length() - 1);
				ends++;
			}
			s = s + " eh";
			for(int i = 0; i < ends; i++) {
				s = s + end;
			}
		} else {
			s = s + " eh";
		}

		return s;
	}

	//Get date
	public String getDate()	{

		date = dateFormat.format(new Date());

		if(date.startsWith("0"))
			date = date.substring(1) + "am";
		else if(Integer.parseInt((date.substring(0, 2))) < 12)
			date = date + "am";
		else if(Integer.parseInt((date.substring(0, 2))) == 12)
			date = date + "pm";
		else
			date = (Integer.parseInt(date.substring(0, 2)) - 12) + date.substring(2) + "pm";

		return date;
	}

	//Send data from text field when 'return' is pressed
	public void actionPerformed(ActionEvent evt) {

		if(textField.getText().length() > 0) {

			String s = textField.getText();
			out.println(s);

			//Add "eh" if "Canadian" button selected
			if(canada) {
				s = canadianString(s);
			}

			//Print own message on scrollpane
			chatArea.append(getDate() + " " + name + "(Me): " + s + "\n");
		}

		textField.setText("");
	}

	//Attempt to establish connection to server on entered address
	public void connect(String message) {

		try {

			if(!message.startsWith("Invalid")) {
				name = (String)JOptionPane.showInputDialog(
						null,
						"",
						"Please enter a username:",
						JOptionPane.PLAIN_MESSAGE,
						null,
						null,
						"");
			}

			if(name == null) System.exit(0);

			chatArea.setText("Waiting for ip.....");
			try {
				// recieve server packet with host address
				sock = new DatagramSocket(BROADCAST_PORT);
				pack = new DatagramPacket(new byte[64], 64);
				sock.receive(pack);
			} catch(Exception e) {
				chatArea.setText("");
				chatArea.setText("Sorry could not connect...");
			} finally {
				sock.close();
			}

			chatArea.setText("");
			chatArea.setText("Connecting to " + pack.getAddress().getHostAddress());
			
			/* Manual address input
            String address = (String)JOptionPane.showInputDialog(
                    null,
                    "",
                    message,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "");

            if(address == null) System.exit(0);
			

            if(address.equalsIgnoreCase("local"))
                socket = new Socket(InetAddress.getLocalHost(), PORT);
            else
            */
            socket = new Socket(pack.getAddress(), PORT);

			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			out.println(name);

			label.setText(" Chat as you please: ");
			label.repaint();
			textField.setVisible(true);
			textField.repaint();

			chatArea.setText("");
			chatArea.append("Welcome " + name + "!\n");
			chatArea.append("You have connected to the chatroom.\n\n");
			out.println("FIRSTCONNECTSERVER: " + name + " has connected to the chatroom.\n");


			t = new Thread(new Input());
			t.setDaemon(true);
			t.start();

			textField.requestFocus();

		} catch (Exception e) {
			try {
				if(out != null) {out.close();}
				if(in != null) {in.close();}
				if(socket != null) {socket.close();}
				System.exit(0);
				//connect("Invalid address");
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void windowClosing(WindowEvent arg0) {

		//Let Server knoww to remove this user
		if(out != null)
			out.println("EXITINGCHATROOM");

		//Clean up references
		try {
			if(out != null)
				out.close();
			if(in != null)
				in.close();
			if(socket != null)
				socket.close();
		} catch (IOException e) {
			e.printStackTrace();
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

		//Handles all incoming data from server.
	private class Input extends Thread {

		public void run() {

			while(true) {
				try {
					String s = in.readLine();

					//Add "eh" to incoming messages
					if(canada) {
						s = canadianString(s);
					}

					if(playAudio) {
						new AudioClip(file).start();
					}

					chatArea.append(getDate() + " " + s + "\n");
					chatArea.setCaretPosition(chatArea.getDocument().getLength());

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
/*Dakota Sanchez
 * GUI Chatroom Client
 */

import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;

import sun.audio.*;

import javax.swing.*;
import javax.swing.UIManager.*;
import javax.swing.text.DefaultCaret;

import java.awt.*;
import java.awt.event.*;

@SuppressWarnings("restriction")
public class Client implements ActionListener, WindowListener {

	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private DateFormat dateFormat;
	private Thread t;

	private String name, file, date;
	private boolean connected, canada, playAudio;

	private JFrame frame;
	private JPanel bottomPanel, topPanel;
	private JTextArea chatArea;
	private JTextField textField;
	private JButton canadaButton, muteButton, alertButton;
	private JLabel label;
	private DefaultCaret caret;

	public static void main(String[] args) throws IOException {

		new Client();
	}

	public Client() throws IOException {

		connected = false;
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
						new AudioClip().start();
				} else if(alertButton.getText().endsWith("2")) {
					alertButton.setText("Alert Sound: 3");
					file = "alert3.wav";
					if(playAudio)
						new AudioClip().start();
				} else {
					alertButton.setText("Alert Sound: 1");
					file = "alert.wav";
					if(playAudio)
						new AudioClip().start();
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
						new AudioClip().start();
					}

					chatArea.append(getDate() + " " + s + "\n");
					chatArea.setCaretPosition(chatArea.getDocument().getLength());

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	//Handles alert sounds
	private class AudioClip extends Thread implements ActionListener {

		private AudioStream audioStream;

		public void run() {

			//Open audio stream
			try {
				audioStream = new AudioStream(this.getClass().getResourceAsStream("/res/" + file));
				AudioPlayer.player.start(audioStream);

				Timer time = new Timer(500, this);
				time.setRepeats(false);
				time.start();

			} catch(FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void actionPerformed(ActionEvent event) {

			AudioPlayer.player.stop(audioStream);
		}
	}

	//Attempt to establish connection to server on entered address
	public void connect(String message) {

		try {

			name = (String)JOptionPane.showInputDialog(
					null,
					"",
					"Please enter a username:",
					JOptionPane.PLAIN_MESSAGE,
					null,
					null,
					"");

			if(name == null) System.exit(0);

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
                socket = new Socket(InetAddress.getLocalHost(), 55555);
            else
                socket = new Socket(InetAddress.getByName(address), 55555);

			connected = true;

			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			out.println(name);

			label.setText(" Chat as you please: ");
			label.repaint();
			textField.setVisible(true);
			textField.repaint();

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

				connect("Invalid address");
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

}

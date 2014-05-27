import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.Timer;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

//Handles alert sounds
public class AudioClip extends Thread implements ActionListener {

	private AudioStream audioStream;
	private String file;

	public AudioClip(String file) {
		this.file = file;
	}

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
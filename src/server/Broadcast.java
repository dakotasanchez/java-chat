import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Broadcast extends Thread {

	private final int BROADCAST_PORT = 1202;

	private InetAddress broadcastAddr;
	private DatagramSocket sock;

	public Broadcast(InetAddress addr) {
		broadcastAddr = addr;
		start();
	}

	public void run() {
		try {
			sock = new DatagramSocket(null);
			
			byte[] buffer = new byte[64];

			DatagramPacket pack = new DatagramPacket(buffer, buffer.length,
					broadcastAddr, BROADCAST_PORT);
			while(true) {
				sock.send(pack);
				System.out.println("sent packet");
				Thread.sleep(1500);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sock.close();
		}
	}
}

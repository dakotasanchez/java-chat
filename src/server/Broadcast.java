import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Broadcast extends Thread {

	private final int BROADCAST_PORT = 1202;

	private InetAddress broadcastAddr;
	private DatagramSocket sock;

	public Broadcast(InetAddress addr) {
		// convert machine ip to broadcast ip
		byte[] rawAddr = addr.getAddress();
		rawAddr[3] = (byte)255;
		try {
			broadcastAddr = InetAddress.getByAddress(rawAddr);
		} catch(UnknownHostException e) {
			e.printStackTrace();
		}
		start();
	}

	public void run() {
		try {
			sock = new DatagramSocket(null);

			// empty data
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

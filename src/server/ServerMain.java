public class ServerMain {
	public static void main(String[] args) {	
		try {
			new Server();
		} catch (Exception e) {
			System.out.println("There was a problem...");
			e.printStackTrace();
		}
	}
}
public class ClientMain {
	public static void main(String[] args) {
		try {
			new Client();
		 } catch(Exception e) {
		 	System.out.println("There was a problem...");
		 	e.printStackTrace();
		 }
	}
}
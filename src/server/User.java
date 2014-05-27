import java.io.PrintWriter;

//Represents a client
public class User {
		
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
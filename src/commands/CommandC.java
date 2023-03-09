package commands;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class CommandC {
	
	private String ip;
	private int port;
	private List<String> files;
	private String option;

	public CommandC(String ip, int port, List<String> files, String option) {
		this.ip = ip;
		this.port = port;
		this.files = files;
		this.option = option;
	}
	
	public void cipher() {
		
	}
	
	public void sendToServer() throws UnknownHostException, IOException, ClassNotFoundException {
		
		Socket socket = new Socket(this.ip, this.port);
		
		ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
		outStream.writeObject(this.option);
        ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
		System.out.println("O server disse " + (String)inStream.readObject());
		
		
		
		
		
		socket.close();
		
	}
	
}

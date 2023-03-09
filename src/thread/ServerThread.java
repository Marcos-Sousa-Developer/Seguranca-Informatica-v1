package thread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerThread extends Thread {
	
	//Server socket
	public Socket socket = null;
	
	//Thread server for each client
	public ServerThread(Socket inSoc) {
		socket = inSoc;
		//System.out.println("thread do server para cada cliente");
	}
	
	public void run(){
		System.out.println("Teste");
		
		try {
			ObjectInputStream inStream = new ObjectInputStream(this.socket.getInputStream());
			String option = (String) inStream.readObject();
			System.out.println("Eu server recebi " + option);
			
			//if options
			
			
			ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
			outStream.writeObject("Recebido");
		
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void receive() {
		
	}
	
	public void send() {
		
	}
}

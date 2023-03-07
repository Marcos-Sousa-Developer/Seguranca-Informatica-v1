package thread;

import java.net.Socket;

public class ServerThread extends Thread {
	
	//Server socket
	public Socket socket = null;
	
	//Thread server for each client
	public ServerThread(Socket inSoc) {
		socket = inSoc;
		System.out.println("thread do server para cada cliente");
	}
	
	public void run(){
		System.out.println("Teste");
		
	}
	
	public void receive() {
		
	}
	
	public void send() {
		
	}
}

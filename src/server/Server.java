package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


public class Server {
	
	public Server() {
	}

	public void startServer() {
		ServerSocket sSoc = null;
		Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a port number: ");
        int port = Integer.parseInt(scanner.nextLine());
        
		try {
			sSoc = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
		while(true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start();
		    }
		    catch (IOException e) {
		        e.printStackTrace();
		    }
		}
		//sSoc.close();
	}
}

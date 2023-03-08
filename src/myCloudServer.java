import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import thread.ServerThread;

public class myCloudServer {

	private static int verifyPort(String[] args) {
		
		//System.out.println(args[0]);
		int port = 1;
		if(args.length == 1){
			try {
		        port = Integer.parseInt(args[0]);
		    } catch (NumberFormatException errr) {
		    	System.err.println("Number port not valid.");
		    	System.exit(-1);
		    }
		} else {
			System.err.println("You must provide a port.");
	    	System.exit(-1);
		}
		return port;
	}
	
	public static void main(String[] args) {
		int port = verifyPort(args);
		myCloudServer server = new myCloudServer();
		server.startServer(port);
	}
	
	private void startServer(int port) {
		ServerSocket sSoc = null;
		try {
			sSoc = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println(e.getMessage() + ", you should use a number port between 1024 and 65535.");
			System.exit(-1);
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage() + ", you should use a number port between 1024 and 65535.");
			System.exit(-1);
		}
		System.out.println("Server connected");
		
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

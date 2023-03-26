import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import commands.VerifyPort;
import thread.ServerThread;

public class myCloudServer {

	private static int verifyPort(String[] args) {
		
		String port = "";
		if(args.length == 1){
			port = new VerifyPort(args[0]).verifyPort();
		} else {
			System.err.println("You must provide a port.");
	    	System.exit(-1);
		}
		return Integer.parseInt(port);
	}
	
	public static void main(String[] args) throws IOException  {
		int port = verifyPort(args);
		myCloudServer server = new myCloudServer();
		server.startServer(port);
	}
	
	private void startServer(int port) throws IOException {
		ServerSocket sSoc = null;
		sSoc = new ServerSocket(port);
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

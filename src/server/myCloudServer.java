package server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import thread.ServerThread;

public class myCloudServer {

	public static void main(String[] args) {
		System.out.println("Server connected");
		Server server = new Server();
		server.startServer();
	}
}

package commands;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
	
	public void cipherFile(String fileName) {
		
	}

	public void cipherKey(String fileName) {
		
	}
	
	public void sendToServer() throws UnknownHostException, IOException, ClassNotFoundException {
		
		Socket socket = new Socket(this.ip, this.port);
		
		ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
		outStream.writeObject(this.option); //opcao
		outStream.writeObject(this.files.size()); //files size
        ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
		System.out.println("O server disse: " + (String)inStream.readObject());
		
		for (String fileName : this.files) {
			
			//falta cifrar files
			//falta cifrar as keys
			
			File myFile = new File(fileName);
//          Long dim = myFile.length();
	        
//          outStream.writeObject(fileName);
//          outStream.writeObject(dim);
	        
//	        BufferedInputStream myFileB = new BufferedInputStream(new FileInputStream(fileName));
//	        byte[] buffer = new byte[1024];
//	        int x = 0;
//	        while((x = myFileB.read(buffer, 0, 1024)) > 0){
//	            outStream.write(buffer, 0, x);
//	        }
//	        myFileB.close();
    	}

		outStream.close();
        inStream.close();
		socket.close();
	}
}

package thread;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
		try {
			ObjectInputStream inStream = new ObjectInputStream(this.socket.getInputStream());
			ObjectOutputStream outStream = new ObjectOutputStream(this.socket.getOutputStream());
			String option = (String) inStream.readObject();
			System.out.println("Option: " + option);
			
			if (option.equals("-c")){
				
				verifyCommandC(inStream, outStream);
				
			} else if (option.equals("-s")) {
				
				verifyCommandS(inStream);
				
				
			} else if (option.equals("-e")) {
				
			} else if (option.equals("-g")) {
				
			} 
			
			inStream.close();
			this.socket.close();
		
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void verifyCommandC(ObjectInputStream inStream, ObjectOutputStream outStream) throws ClassNotFoundException, IOException {
		int filesDim = (int) inStream.readObject();
		System.out.println("filesDim: " + filesDim);
		
		for (int i = 0; i < filesDim; i++) {
			
			System.out.println("-----------New File-----------");
			
			Boolean fileExistClient = (Boolean) inStream.readObject();
			
			if (fileExistClient) {
				String fileName = (String) inStream.readObject();
			 
				File f = new File("../cloud/files/" + fileName + ".cifrado");
				
				Boolean fileExistServer = f.exists();
				
				outStream.writeObject(fileExistServer);
				
				if(!fileExistServer) {
					
					//---------------Receber Ficheiro Cifrado----------------------
					
					String fileNameCif = (String) inStream.readObject();
					System.out.println(fileNameCif);
					
					FileOutputStream outFileStreamCif = new FileOutputStream("../cloud/files/" + fileNameCif);
					BufferedOutputStream outFileCif = new BufferedOutputStream(outFileStreamCif);
					
					try{
						Long fileSizeCif = (Long)inStream.readObject();
						System.out.println(fileSizeCif);
						
						byte[] bufferCif = new byte[1024];
						int xCif = 0;
						int tempCif = fileSizeCif.intValue();
						
						while(tempCif > 0){
							xCif = inStream.read(bufferCif, 0, tempCif > 1024 ? 1024 : tempCif);
							outFileCif.write(bufferCif, 0, xCif);
							tempCif -= xCif;
						}
						System.out.println(outFileCif.toString());
		
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					}
					outFileCif.close();
					
					
					//---------------Receber Chave Cifrada----------------------
					
					String fileNameKey = (String) inStream.readObject();
					System.out.println(fileNameKey);
					
					FileOutputStream outFileStreamKey = new FileOutputStream("../cloud/keys/" + fileNameKey);
					BufferedOutputStream outFileKey = new BufferedOutputStream(outFileStreamKey);
					
					try{
						Long fileSizeKey = (Long)inStream.readObject();
						System.out.println(fileSizeKey);
						
						byte[] bufferKey = new byte[1024];
						int xKey = 0;
						int tempKey = fileSizeKey.intValue();
						
						while(tempKey > 0){
							xKey = inStream.read(bufferKey, 0, tempKey > 1024 ? 1024 : tempKey);
							outFileKey.write(bufferKey, 0, xKey);
							tempKey -= xKey;
						}
						System.out.println(outFileKey.toString());
		
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					}
					outFileKey.close();
				} else {
					System.out.println("The file " + fileName + " already exist in server.");
				}
			} else {
				System.out.println("The file doesn't exist in client.");
			}
		}	
	}
	
	private void verifyCommandS(ObjectInputStream inStream) throws IOException, ClassNotFoundException {  
		
		String fileName = (String) inStream.readObject(); 
		
		//Read hashed sign file 
		byte[] bufferData = new byte[1024]; 
		
		int contentLength = inStream.available(); 
		
		while(contentLength != -1) {
			
			
			
		}
		
		//FileOutputStream fileOutputStream = new FileOutputStream(filename);
		
		
		
		
		
	}
	
	public void send() {
		
	}
}

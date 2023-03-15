package thread;

import java.io.BufferedOutputStream;
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
		System.out.println("Teste");
		
		try {
			ObjectInputStream inStream = new ObjectInputStream(this.socket.getInputStream());
			String option = (String) inStream.readObject();
			System.out.println("Option: " + option);
			
			if (option.equals("-c")){
				
				int filesDim = (int) inStream.readObject();
				System.out.println("filesDim: " + filesDim);
				
				for (int i = 0; i < filesDim; i++) {
					
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

				}
				//dar erro caso o ficheiro já exista no servidor
				
			} else if (option.equals("-s")) {
				
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
	
	public void receive() {
		
	}
	
	public void send() {
		
	}
}

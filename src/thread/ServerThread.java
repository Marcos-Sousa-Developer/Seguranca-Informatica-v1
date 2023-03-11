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
					
					String fileName = (String) inStream.readObject();	//está a dar erro
					System.out.println(fileName);
					
//                  FileOutputStream outFileStream = new FileOutputStream("../cloud/files/teste.txt");
//                  BufferedOutputStream outFile = new BufferedOutputStream(outFileStream);

//					try{
//						
//						Long fileSize = (Long)inStream.readObject();
//
//						byte[] buffer = new byte[1024];
//
//						int x = 0;
//
//						int temp = fileSize.intValue();
//
//						while(temp > 0){
//							x = inStream.read(buffer, 0, temp > 1024 ? 1024 : temp);
//							outFile.write(buffer, 0, x);
//							temp -= x;
//						}
//						System.out.println(outFile.toString());
//
//					} catch (ClassNotFoundException e1) {
//						e1.printStackTrace();
//					}
//
//					outFile.close();
					inStream.close();
					socket.close();
					
					
					
					
				}
				//receber os filheiros cifrados
				//guardar na pasta cloud/files com extensão .cifrado
				//receber as keys
				//guardar na pasta cloud/files com extensão .chave_secreta
				//dar erro caso o ficheiro já exista no servidor
				//
				
			} else if (option.equals("-s")) {
				
			} else if (option.equals("-e")) {
				
			} else if (option.equals("-g")) {
				
			} else {
				
			}

			ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
			outStream.writeObject("Recebido");
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void receive() {
		
	}
	
	public void send() {
		
	}
}

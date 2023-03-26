package thread;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerThread extends Thread {

	// Server socket
	public Socket socket = null;

	// Thread server for each client
	public ServerThread(Socket inSoc) {
		this.socket = inSoc;
	}
	
	/*
	 * Overrides Thread run 
	 * This method listen the option and redirect to the correct manager
	 */
	public void run() {
		try {
			
			ObjectInputStream inStream = new ObjectInputStream(this.socket.getInputStream());
			ObjectOutputStream outStream = new ObjectOutputStream(this.socket.getOutputStream());
			
			String option = (String) inStream.readObject();

			if (option.equals("-c")) {
				verifyCommandC(inStream, outStream);
				
			} else if (option.equals("-s")) {
				verifyCommandS(inStream, outStream);

			} else if (option.equals("-e")) {
				verifyCommandE(inStream, outStream);

			} else if (option.equals("-g")) {
				verifyCommandG(inStream, outStream);
			}

			inStream.close();
			this.socket.close();

		} catch (IOException | ClassNotFoundException e) {
			System.out.println("I got an error or something was interrupted!");
			System.out.println(e);
			//e.printStackTrace();
		}
	}

	/**
	 * Verify commandC and check what to do
	 * @ObjectInputStream inStream
	 * @ObjectOutputStream outStream
	 */
	private void verifyCommandC(ObjectInputStream inStream, ObjectOutputStream outStream)
			throws ClassNotFoundException, IOException {
		int filesDim = (int) inStream.readObject();

		for (int i = 0; i < filesDim; i++) {

			Boolean fileExistClient = (Boolean) inStream.readObject();

			// check if file exists on client
			if (fileExistClient) {
				String fileName = (String) inStream.readObject();

				File fcifrado = new File("../cloud/files/" + fileName + ".cifrado");
				File fassinado = new File("../cloud/files/" + fileName + ".assinado");
				File fseguro = new File("../cloud/files/" + fileName + ".seguro");

				//check if file does not exists on the server
				Boolean fileExistServer = fcifrado.exists() || fassinado.exists() || fseguro.exists();

				//send the output
				outStream.writeObject(fileExistServer);

				//if does not exists
				if (!fileExistServer) {

					// ---------------Receives the cipher file----------------------

					String fileNameCif = (String) inStream.readObject();

					FileOutputStream outFileStreamCif = new FileOutputStream("../cloud/files/" + fileNameCif);
					BufferedOutputStream outFileCif = new BufferedOutputStream(outFileStreamCif);

					try {
						Long fileSizeCif = (Long) inStream.readObject();

						int fileSizeCifInt = fileSizeCif.intValue();

						byte[] bufferDataCif = new byte[Math.min(fileSizeCifInt, 1024)];

						int contentLengthCif = inStream.read(bufferDataCif);

						while (fileSizeCifInt > 0 && contentLengthCif > 0) {
							if (fileSizeCifInt >= contentLengthCif) {
								outFileCif.write(bufferDataCif, 0, contentLengthCif);
							} else {
								outFileCif.write(bufferDataCif, 0, fileSizeCifInt);
							}
							contentLengthCif = inStream.read(bufferDataCif);
							fileSizeCifInt -= contentLengthCif;
						}

					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					}
					outFileCif.close();

					// ---------------Receives the cipher key----------------------

					String fileNameKey = (String) inStream.readObject();

					FileOutputStream outFileStreamKey = new FileOutputStream("../cloud/keys/" + fileNameKey);

					BufferedOutputStream outFileKey = new BufferedOutputStream(outFileStreamKey);

					try {

						Long fileSizeKey = (Long) inStream.readObject();

						int fileSizeKeyInt = fileSizeKey.intValue();

						byte[] bufferDataKey = new byte[Math.min(fileSizeKeyInt, 1024)];

						int contentLengthKey = inStream.read(bufferDataKey);

						while (fileSizeKeyInt > 0 && contentLengthKey > 0) {
							if (fileSizeKeyInt >= contentLengthKey) {
								outFileKey.write(bufferDataKey, 0, contentLengthKey);
							} else {
								outFileKey.write(bufferDataKey, 0, fileSizeKeyInt);
							}
							contentLengthKey = inStream.read(bufferDataKey);
							fileSizeKeyInt -= contentLengthKey;
						}

					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					}
					outFileKey.close();
					System.out.println("The file " + fileName + " received!");
					
				//can't proceed if the file already exists on the server
				} else {
					System.out.println("The file " + fileName + " already exist in server.");
				}
			} 
		}
	}
	
	/**
	 * Verify commandS and check what to do
	 * @ObjectInputStream inStream
	 * @ObjectOutputStream outStream
	 */
	private void verifyCommandS(ObjectInputStream inStream, ObjectOutputStream outStream)
			throws IOException, ClassNotFoundException {

		// Get numbers of files
		int numbersOfFiles = (int) inStream.readObject();

		for (int i = 0; i < numbersOfFiles; i++) {

			// check if file exists to continue
			if ((boolean) inStream.readObject()) {

				// Read the file name received by client
				String fileName = (String) inStream.readObject();

				// Check file exists on server
				File fcifrado = new File("../cloud/files/" + fileName + ".cifrado");
				File fassinado = new File("../cloud/files/" + fileName + ".assinado");
				File fseguro = new File("../cloud/files/" + fileName + ".seguro");
				
				Boolean fileExistServer = fcifrado.exists() || fassinado.exists() || fseguro.exists();

				// Verify if file exists
				if (!fileExistServer) {

					// File does not exist
					outStream.writeObject(false);

					// Create new fileOutput ".assign"
					FileOutputStream outFile = new FileOutputStream("../cloud/files/" + fileName + ".assinado");

					// get the total buffer size for each file Math.min(totalbytesOfFile,1024)
					int totalFileLength = (int) inStream.readObject();

					// Buffer
					byte[] bufferData = new byte[Math.min(totalFileLength, 1024)];

					// Read chunk file
					int contentLength = inStream.read(bufferData);

					while (totalFileLength > 0 && contentLength > 0) {
						if (totalFileLength >= contentLength) {
							outFile.write(bufferData, 0, contentLength);
						} else {
							outFile.write(bufferData, 0, totalFileLength);
						}
						totalFileLength -= contentLength;
						contentLength = inStream.read(bufferData);
					}
					outFile.close();

					// Get Signature
					FileOutputStream outSignature = new FileOutputStream("../cloud/signatures/" + fileName + ".assinatura");

					// Get out put of signature
					outSignature.write((byte[]) inStream.readObject());
					outSignature.close();
					System.out.println("The file " + fileName + " received!");

				}
				// File exist on server
				else {
					outStream.writeObject(true);
					System.out.println("The file " + fileName + " already exist in server.");
				}
			}
		}
	}

	/**
	 * Verify commandE and check what to do
	 * @ObjectInputStream inStream
	 * @ObjectOutputStream outStream
	 */
	private void verifyCommandE(ObjectInputStream inStream, ObjectOutputStream outStream) throws ClassNotFoundException, IOException {
		
		int numFiles = (int) inStream.readObject(); 
		
		for (int i = 0; i < numFiles; i++) {
			
			Boolean fileExistClient = (Boolean) inStream.readObject();
		
			if (fileExistClient) {
				String fileName = (String) inStream.readObject();
				
				// Check file exists on server
				File fcifrado = new File("../cloud/files/" + fileName + ".cifrado");
				File fassinado = new File("../cloud/files/" + fileName + ".assinado");
				File fseguro = new File("../cloud/files/" + fileName + ".seguro");
				
				Boolean fileExistServer = fcifrado.exists() || fassinado.exists() || fseguro.exists();

				outStream.writeObject(fileExistServer);

				if (!fileExistServer) {
		
					FileOutputStream out = new FileOutputStream("../cloud/files/" + fileName + ".seguro");
					
					//save the signature of the file
					FileOutputStream outSignature = new FileOutputStream("../cloud/signatures/" + fileName + ".assinatura"); 
					
					outSignature.write((byte[]) inStream.readObject()); 
					
					outSignature.close();
					
					//get total cipher text
					int totalFileLength = (int) inStream.readObject();
			
					//byte array for file
					byte[] dataToBytes = new byte[Math.min(totalFileLength, 1024)]; 
					
					//Length of the contents of the read file 
					int contentLength = inStream.read(dataToBytes); 
					
					while(contentLength > 0  &&  totalFileLength > 0) { 
						
						if(totalFileLength >= contentLength) {
							out.write(dataToBytes,0,contentLength);
						} 
						
						else {
							out.write(dataToBytes,0,totalFileLength);
						}
	
						totalFileLength -= contentLength;
						
						if(contentLength > 0  &&  totalFileLength > 0) {
							//continue to read fileInStream
							contentLength = inStream.read(dataToBytes);
						}
							
					}
					
					out.close(); 
					
					//Save the cipher secret key
					FileOutputStream outKey = new FileOutputStream("../cloud/keys/" + fileName + ".chave_secreta"); 
					
					outKey.write((byte[]) inStream.readObject()); 
					
					outKey.close();
					System.out.println("The file " + fileName + " received!");
					
				} else {
					System.out.println("The file " + fileName + " already exist in server.");
				}
			}
		}
	}
	
	/**
	 * Verify commandG and check what to do
	 * @ObjectInputStream inStream
	 * @ObjectOutputStream outStream
	 */
	private void verifyCommandG(ObjectInputStream inStream, ObjectOutputStream outStream) throws IOException, ClassNotFoundException {

		int numbersOfFiles = (int) inStream.readObject();
				
		for (int i = 0; i < numbersOfFiles; i++) {
			
			boolean alreadySent = (boolean) inStream.readObject(); 
			
			if(!alreadySent) {
				
				String fileName = (String) inStream.readObject(); 
				
				// Check file exists on server
				File fcifrado = new File("../cloud/files/" + fileName + ".cifrado");
				File fassinado = new File("../cloud/files/" + fileName + ".assinado");
				File fseguro = new File("../cloud/files/" + fileName + ".seguro");
				
				Boolean fileExistServer = fcifrado.exists() || fassinado.exists() || fseguro.exists();

				outStream.writeObject(fileExistServer);
				
				if(fileExistServer) { 
					
					//case file is type ASSINADO
					File fileToReadSign = new File("../cloud/files/" + fileName + ".assinado");
					if(fileToReadSign.exists()){				
						sendToClient(outStream, "-s", fileToReadSign, fileName);
					}
					else {
						//case file is type CIFRADO
						File fileToReadCif = new File("../cloud/files/" + fileName + ".cifrado");
						if(fileToReadCif.exists()){
							sendToClient(outStream, "-c", fileToReadCif, fileName);
						}
						//case file is type SEGURO
						else {
							File fileToReadSecure = new File("../cloud/files/" + fileName + ".seguro");
							if(fileToReadSecure.exists()){ 
								sendToClient(outStream, "-e", fileToReadSecure, fileName);
								
							}
						}
					}
					System.out.println("The file " + fileName + " already sent!");				
				}
				else {
					System.out.println("The file " + fileName + " is not recognized!");
				}
			}
		}				
	}
	
	
	/**
	 * Send files to client
	 * @ObjectOutputStream outStream
	 * @String option client manager option
	 * @File fileToRead 
	 * @String fileName
	 */
	private void sendToClient(ObjectOutputStream outStream, String option, File fileToRead, String fileName) throws IOException {
		
		outStream.writeObject(option); 
						
		if(option.equals("-c")) {
			FileInputStream fileInStreamSecretKey = new FileInputStream("../cloud/keys/" + fileName + ".chave_secreta"); 
			outStream.write(fileInStreamSecretKey.readAllBytes());
			fileInStreamSecretKey.close();
		} 
		
		else if (option.equals("-s")) {
			FileInputStream fileInStreamSignature = new FileInputStream("../cloud/signatures/" + fileName + ".assinatura"); 
			outStream.write(fileInStreamSignature.readAllBytes()); 
			fileInStreamSignature.close();
		}
		
		else {
			FileInputStream fileInStreamSecretKey = new FileInputStream("../cloud/keys/" + fileName + ".chave_secreta"); 
			outStream.write(fileInStreamSecretKey.readAllBytes());
			fileInStreamSecretKey.close();
			
			FileInputStream fileInStreamSignature = new FileInputStream("../cloud/signatures/" + fileName + ".assinatura"); 
			outStream.write(fileInStreamSignature.readAllBytes()); 
			fileInStreamSignature.close();
		}
	
		FileInputStream fileInStream = new FileInputStream(fileToRead); 
				
		int totalFileLength = fileInStream.available();
		
		outStream.writeObject(totalFileLength);

		byte[] dataToBytes = new byte[Math.min(totalFileLength, 1024)];
		
		int contentLength = fileInStream.read(dataToBytes);  
								
		while(contentLength > 0) {
			outStream.write(dataToBytes,0,contentLength); 
			outStream.flush();						
			contentLength = fileInStream.read(dataToBytes);
		}
	}
}

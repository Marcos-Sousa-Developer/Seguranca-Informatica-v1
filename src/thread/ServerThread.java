package thread;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
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
		socket = inSoc;
	}

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void verifyCommandC(ObjectInputStream inStream, ObjectOutputStream outStream)
			throws ClassNotFoundException, IOException {
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

				if (!fileExistServer) {

					// ---------------Receber Ficheiro Cifrado----------------------

					String fileNameCif = (String) inStream.readObject();
					System.out.println(fileNameCif);

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

					// ---------------Receber Chave Cifrada----------------------

					String fileNameKey = (String) inStream.readObject();
					System.out.println(fileNameKey);

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
				} else {
					System.out.println("The file " + fileName + " already exist in server.");
				}
			} else {
				System.out.println("The file doesn't exist in client.");
			}
		}
	}

	private void verifyCommandS(ObjectInputStream inStream, ObjectOutputStream outStream)
			throws IOException, ClassNotFoundException {

		// Get numbers of files
		int numbersOfFiles = (int) inStream.readObject();

		for (int i = 0; i < numbersOfFiles; i++) {

			// check if file exists to continue
			if ((boolean) inStream.readObject()) {

				// Read the file name received by client
				String fileName = (String) inStream.readObject();

				// Check file
				File file = new File(fileName + ".assinado");

				// Verify if file exists
				if (!file.exists()) {

					// File does not exist
					outStream.writeObject(false);

					// Create new fileOutput ".assign"
					FileOutputStream outFile = new FileOutputStream(fileName + ".assinado");

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
					FileOutputStream outSignature = new FileOutputStream(fileName + ".assinatura");

					// Get out put of signature
					outSignature.write((byte[]) inStream.readObject());
					outSignature.close();

				}
				// File exist
				else {
					outStream.writeObject(true);
				}
			}
		}
	}

	private void verifyCommandE(ObjectInputStream inStream, ObjectOutputStream outStream)
			throws IOException, ClassNotFoundException {

		int numbersOfFiles = (int) inStream.readObject();

		for (int i = 0; i < numbersOfFiles; i++) {

			String fileName = (String) inStream.readObject();

			FileOutputStream outFile = new FileOutputStream(fileName + ".seguro");

			int totalFileLength = (int) inStream.readObject();

			byte[] bufferData = new byte[Math.min(totalFileLength, 1024)];

			int contentFileLength = inStream.read(bufferData);

			while (contentFileLength > 0 && totalFileLength > 0) {
				if (totalFileLength >= contentFileLength) {
					outFile.write(bufferData, 0, contentFileLength);
				} else {
					outFile.write(bufferData, 0, totalFileLength);
				}
				totalFileLength -= contentFileLength;
				contentFileLength = inStream.read(bufferData);
			}
			
			outFile.close();

			FileOutputStream outSignature = new FileOutputStream(fileName + ".assinatura");

			outSignature.write((byte[]) inStream.readObject());

			FileOutputStream outCipherKey = new FileOutputStream(fileName + ".chave_secreta");

			outCipherKey.write((byte[]) inStream.readObject());

		}

	}

	private void verifyCommandG(ObjectInputStream inStream, ObjectOutputStream outStream) throws IOException, ClassNotFoundException {
		
		int numbersOfFiles = (int) inStream.readObject();
				
		for (int i = 0; i < numbersOfFiles; i++) {

			String fileName = (String) inStream.readObject();
				
			File fileToReadSign = new File(fileName + ".assinado");
			
			if(fileToReadSign.exists()){
				
				sendToClient(outStream, "-s", fileToReadSign, fileName);
			
			}
			
			File fileToReadCif = new File(fileName + ".cifrado");
			
			if(fileToReadCif.exists()){
				
				sendToClient(outStream, "-c", fileToReadCif, fileName);
			
			}
			
			File fileToReadSecure = new File(fileName + ".seguro");
			
			if(fileToReadSecure.exists()){

				sendToClient(outStream, "-e", fileToReadSecure, fileName);
			
			}
				
		}		
				
	}
	
	private void sendToClient(ObjectOutputStream outStream, String option, File fileToRead, String fileName) throws IOException {
		
		outStream.writeObject(option);
		
		if(option.equals("-c")) {
			
		} 
		else if (option.equals("-s")) {
			
			FileInputStream fileInStreamSignature = new FileInputStream(fileName + ".assinatura"); 
			
			outStream.write(fileInStreamSignature.readAllBytes()); 
			
			fileInStreamSignature.close();
			
			
		}
		else {
			FileInputStream fileInStreamSignature = new FileInputStream(fileName + ".assinatura");
			FileInputStream fileInStreamKey = new FileInputStream(fileName + ".chave_secreta");
			
			outStream.write(fileInStreamSignature.readAllBytes());
			outStream.write(fileInStreamKey.readAllBytes());
			
			fileInStreamSignature.close();
			fileInStreamKey.close();
			
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
		
		fileInStream.close();
	
	
	}
	
}

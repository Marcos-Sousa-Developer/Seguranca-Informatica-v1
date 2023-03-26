package commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CommandG {
	
	private String ip;
	private int port;
	private List<String> files;

	public CommandG(String ip, int port, List<String> files) {
		this.ip = ip;
		this.port = port;
		this.files = files;
	}
	
	/**
	 * Verify if file was not tempered.
	 * @byte[] signatureInByte received signature in bytes
	 * @String fileToVerify file that you want to verify
	 */
	private void initVerifyFile(byte[] signatureInByte, String fileName) throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException, InvalidKeyException, UnrecoverableKeyException, ClassNotFoundException, SignatureException {
		
		Signature s = Signature.getInstance("SHA256withRSA");
		
		FileInputStream kfile = new FileInputStream(new File("../src/KeyStore.si027Cloud"));
		KeyStore keystore = KeyStore.getInstance("PKCS12");
		keystore.load(kfile, "si027marcos&rafael".toCharArray()); 
		//Key key = keystore.getKey("si027", "si027marcos&rafael".toCharArray());  

		Certificate cert = keystore.getCertificate("si027");
		
		PublicKey publicKey = cert.getPublicKey(); 	
		
		s.initVerify(publicKey);
		
		//get the received file to verify
		FileInputStream fileToVerify = new FileInputStream("../receiveidFiles/"+fileName);
		
		//get total file length
		int totalFileLength =  fileToVerify.available();
		
		byte[] bufferData = new byte[Math.min(totalFileLength, 1024)];
										
		int contentFileLength = fileToVerify.read(bufferData);
		
		//verify file
		while (contentFileLength > 0) {
			s.update(bufferData,0,contentFileLength);
			contentFileLength = fileToVerify.read(bufferData);
		}
		fileToVerify.close(); 
		
		//check if file was not tempered
		boolean bool = s.verify(signatureInByte);
    	if(bool) {
            System.out.println("Signature of file " +  fileName + " was verified.");   
         } else {
            System.out.println("Signature of file " + fileName + " was failed.");
         }	
	}

	/**
	 * Receive a secret key encoded and make a DECRYPT
	 * than initialize a cipher DECRYPT mode.
	 * @byte[] AESkey
	 * @return Cipher initialized
	 */
	private Cipher initDecryptMode(byte[] AESkey) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		
		FileInputStream kfile = new FileInputStream("KeyStore.si027Cloud"); 
	    KeyStore kstore = KeyStore.getInstance("PKCS12");
	    kstore.load(kfile, "si027marcos&rafael".toCharArray());
	    
	    Key privateKey = kstore.getKey("si027", "si027marcos&rafael".toCharArray());
		
	    Cipher cRSA = Cipher.getInstance("RSA");
	    
	    cRSA.init(Cipher.UNWRAP_MODE, privateKey);

	    Key unwrappedKey = cRSA.unwrap(AESkey, "AES", Cipher.SECRET_KEY);
	    
	    Cipher c = Cipher.getInstance("AES");
        
        c.init(Cipher.DECRYPT_MODE, unwrappedKey);
	    
	    return c;
	}
	
	/**
	 * Read a chunk files, DECRYPT file and save it on client machine
	 * @byte[] secretKeyInByte
	 * @ObjectInputStream inStream
	 * @fileName fileName to get the fileOutput
	 */
	private void decryptFile(byte[] secretKeyInByte, ObjectInputStream inStream, String fileName) throws UnrecoverableKeyException, InvalidKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, ClassNotFoundException {
		
		//get cipher in DECRYPT mode
		Cipher c = initDecryptMode(secretKeyInByte);
		
		FileOutputStream fileOutput = new FileOutputStream("../receiveidFiles/" + fileName);
		
		CipherOutputStream cipherOut= new CipherOutputStream(fileOutput, c);
		
		int totalFileLength = (int) inStream.readObject();
		
		byte[] bufferData = new byte[Math.min(totalFileLength, 1024)];
										
		int contentFileLength = inStream.read(bufferData);
										
		while (contentFileLength > 0 && totalFileLength > 0) {
			if (totalFileLength >= contentFileLength) {
				cipherOut.write(bufferData, 0, contentFileLength);
				
			} else {
				cipherOut.write(bufferData, 0, totalFileLength);
				
			}
			totalFileLength -= contentFileLength;
			
			if(totalFileLength > 0) {
				contentFileLength = inStream.read(bufferData);
			}
		}
		
		//cipherOut.flush();
		cipherOut.close();
		fileOutput.close();
		
		System.out.println("File " + fileName + " already decrypted!");
	}
	
	/**
	 * Method to communicate with the server
	 */
	public void sendToServer() throws UnknownHostException, IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnrecoverableKeyException, KeyStoreException, CertificateException, IllegalBlockSizeException, SignatureException, BadPaddingException {

		Socket socket = null;
		try {
			 socket = new Socket(this.ip, this.port);
		}
		catch (ConnectException e) {
			System.out.println("Connection refused, please check the address or port");
			System.exit(-1);
		}
		catch (UnknownHostException e) {
			
			System.out.println("Connection refused, please check the Host");
			System.exit(-1);
		}
		catch (NoRouteToHostException e) {
			System.out.println("Connection refused, please check the Host");
			System.exit(-1);
		}
		
		ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
		
		//send type option to get a correct manager
		outStream.writeObject("-g");
		outStream.writeObject(this.files.size());
		
		for (String fileName : this.files) {
			
			File fileToVerify = new File("../receiveidFiles/" + fileName); 
			
			boolean alreadyReceived = fileToVerify.exists();
			
			outStream.writeObject(alreadyReceived); 
			
			if(!alreadyReceived) {
				
				outStream.writeObject(fileName); 
				
				boolean fileExistsOnServer = (boolean) inStream.readObject(); 
				
				if(fileExistsOnServer) {
					
					//after send the requested file, gets correct manager
					String option = (String) inStream.readObject();
					
					if(option.equals("-c")) {
						byte[] secretKeyInByte = new byte[256];
						inStream.read(secretKeyInByte);
						decryptFile(secretKeyInByte, inStream, fileName);
					} 
					
					else if (option.equals("-s")) {
						
						//get signature
						byte[] signatureInByte = new byte[256];
						inStream.read(signatureInByte);
						
						FileOutputStream outFile = new FileOutputStream("../receiveidFiles/" + fileName); 
										
						int totalFileLength = (int) inStream.readObject();
						
						byte[] bufferData = new byte[Math.min(totalFileLength, 1024)];
														
						int contentFileLength = inStream.read(bufferData);
						
						//get file chunks and store in "../receiveidFiles/"
						while (contentFileLength > 0 && totalFileLength > 0) {
							if (totalFileLength >= contentFileLength) {
								outFile.write(bufferData, 0, contentFileLength);
							} else {
								outFile.write(bufferData, 0, totalFileLength);
							}
							totalFileLength -= contentFileLength; 
							
							if(contentFileLength > 0 && totalFileLength > 0) {
								contentFileLength = inStream.read(bufferData);
							}
						}
						outFile.close(); 
						//initialize verify file
						initVerifyFile(signatureInByte, fileName);
						
					}
					
					else {
						//get secret key
						byte[] secretKeyInByte = new byte[256];
						inStream.read(secretKeyInByte);
						
						//get signature
						byte[] signatureInByte = new byte[256];
						inStream.read(signatureInByte); 
											
						decryptFile(secretKeyInByte, inStream, fileName); 
						initVerifyFile(signatureInByte, fileName);

					}
				}
				
				else {
					System.err.println("The file " + fileName + " doesn't exist on the server. You must provide a existing file.");
				}
			}
			
			else {
				System.err.println("Your already have the file " + fileName + ".");

			}
		}
	}
}
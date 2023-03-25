package commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
	
	private void initVerifySign(byte[] signatureInByte, ObjectInputStream inStream, FileOutputStream fileOutput) throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException, InvalidKeyException, UnrecoverableKeyException, ClassNotFoundException, SignatureException {
		
		Signature s = Signature.getInstance("SHA256withRSA");
		
		FileInputStream kfile = new FileInputStream(new File("../src/KeyStore.si027"));
		KeyStore keystore = KeyStore.getInstance("PKCS12");
		keystore.load(kfile, "si027marcos&rafael".toCharArray()); 
		Key key = keystore.getKey("si027", "si027marcos&rafael".toCharArray());  

		Certificate cert = keystore.getCertificate("si027");
		
		//obter public key, vai ser usada para cifra hibrida
		PublicKey publicKey = cert.getPublicKey(); 	
		
		s.initVerify(publicKey);
		
		int totalFileLength = (int) inStream.readObject();
		
		byte[] bufferData = new byte[Math.min(totalFileLength, 1024)];
										
		int contentFileLength = inStream.read(bufferData);
										
		while (contentFileLength > 0 && totalFileLength > 0) {
			if (totalFileLength >= contentFileLength) {
				s.update(bufferData, 0, contentFileLength);
				fileOutput.write(bufferData, 0, contentFileLength);
				
			} else {
				s.update(bufferData, 0, totalFileLength);
				fileOutput.write(bufferData, 0, totalFileLength);
				
			}
			totalFileLength -= contentFileLength;
			contentFileLength = inStream.read(bufferData);
		}
		fileOutput.close();
		
		boolean bool = s.verify(signatureInByte);
    	
    	if(bool) {
            System.out.println("Signature verified");   
         } else {
            System.out.println("Signature failed");
         }	
	}

	
	private Cipher decryptSecretKey(byte[] AESkey) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		
		FileInputStream kfile = new FileInputStream("KeyStore.si027"); 
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
	
	
	private void decryptFile(byte[] secretKeyInByte, ObjectInputStream inStream,FileOutputStream fileOutput) throws UnrecoverableKeyException, InvalidKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, ClassNotFoundException {
		
		Cipher c = decryptSecretKey(secretKeyInByte);
		
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
			contentFileLength = inStream.read(bufferData);
		}
		cipherOut.close();
		fileOutput.close();
		System.out.println("Terminou a decifra");
	}
	
	private void decryptAndVerifySignFile(byte[] signatureInByte, File fileToRead) throws UnrecoverableKeyException, InvalidKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, ClassNotFoundException, SignatureException {
		
		Signature s = Signature.getInstance("SHA256withRSA");
		
		FileInputStream kfile = new FileInputStream(new File("../src/KeyStore.si027"));
		KeyStore keystore = KeyStore.getInstance("PKCS12");
		keystore.load(kfile, "si027marcos&rafael".toCharArray()); 
		Key key = keystore.getKey("si027", "si027marcos&rafael".toCharArray());  

		Certificate cert = keystore.getCertificate("si027");
		
		PublicKey publicKey = cert.getPublicKey(); 	
		
		s.initVerify(publicKey);
		
		FileInputStream fileInStream = new FileInputStream(fileToRead);
		
		int totalFileLength = (int) fileInStream.available();
		
		byte[] bufferData = new byte[Math.min(totalFileLength, 1024)];
										
		int contentFileLength = fileInStream.read(bufferData);
										
		while (contentFileLength > 0 && totalFileLength > 0) {
			if (totalFileLength >= contentFileLength) {
				s.update(bufferData, 0, contentFileLength);
			} else {
				s.update(bufferData, 0, totalFileLength);
			}
			totalFileLength -= contentFileLength;
			contentFileLength = fileInStream.read(bufferData);
		}
		
		fileInStream.close();
		
		boolean bool = s.verify(signatureInByte);
    	
    	if(bool) {
            System.out.println("Signature verified");   
         } else {
            System.out.println("Signature failed");
         }
	}
	
	public void sendToServer() throws UnknownHostException, IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnrecoverableKeyException, KeyStoreException, CertificateException, IllegalBlockSizeException, SignatureException, BadPaddingException {

		Socket socket = new Socket(this.ip, this.port);
		
		ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
		outStream.writeObject("-g");
		outStream.writeObject(this.files.size());
		
		for (String fileName : this.files) {
			
			outStream.writeObject(fileName);
			
			String option = (String) inStream.readObject();
			
			if(option.equals("-c")) {
				
				byte[] secretKeyInByte = new byte[256];
				inStream.read(secretKeyInByte);

				decryptFile(secretKeyInByte, inStream, new FileOutputStream(fileName));
			} 
			else if (option.equals("-s")) {
				
				byte[] signatureInByte = new byte[256];
				inStream.read(signatureInByte); 
				
				initVerifySign(signatureInByte, inStream, new FileOutputStream(fileName));
			}
			
			else {
				
				//byte[] signatureInByte = new byte[256];
				//inStream.read(signatureInByte);
				
				byte[] secretKeyInByte = new byte[256];
				inStream.read(secretKeyInByte);
				
				FileOutputStream f = new FileOutputStream("chaveProv");
				f.write(secretKeyInByte);
				f.close();
				
				decryptFile(secretKeyInByte, inStream, new FileOutputStream(fileName));
				//decryptAndVerifySignFile(signatureInByte, new File(fileName));
				
			}

				
			/*else {
				
				Signature s = initVerifySign();
				
				byte[] signatureInByte = new byte[256];
				inStream.read(signatureInByte);
								
				byte[] key = new byte[256];
				inStream.read(key);
				
				Cipher cipher = decryptKey(key);
				
				FileOutputStream outFile = new FileOutputStream(fileName);
				
				CipherOutputStream cos = new CipherOutputStream(outFile, cipher);
				
				int totalFileLength = (int) inStream.readObject();
			
				byte[] bufferData = new byte[Math.min(totalFileLength, 1024)];
												
				int contentFileLength = inStream.read(bufferData);
												
				while (contentFileLength > 0 && totalFileLength > 0) {
					if (totalFileLength >= contentFileLength) {
						cos.write(bufferData, 0, contentFileLength);
						
					} else {
						cos.write(bufferData, 0, totalFileLength);
						
					}
					totalFileLength -= contentFileLength;
					contentFileLength = inStream.read(bufferData);
				}
				cos.close();
				
				
				/*
				if (s.verify(signatureInByte)) {
					System.out.println("Message is valid");
					
				}
				else {
					System.out.println("Message was corrupted");
				}*/
		}
	}
}

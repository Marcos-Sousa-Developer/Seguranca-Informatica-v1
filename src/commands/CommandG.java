package commands;

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
	
	private Cipher decryptKey(byte[] AESkey) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		
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
	
	
	private Signature initVerifySign() throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException, InvalidKeyException {
		FileInputStream kfile = new FileInputStream("KeyStore.si027"); 
	    KeyStore kstore = KeyStore.getInstance("PKCS12");
	    kstore.load(kfile, "si027marcos&rafael".toCharArray());
	    
	    Certificate cert = kstore.getCertificate("si027");
	    PublicKey publicKey = cert.getPublicKey( );
				
		Signature s = Signature.getInstance("SHA256withRSA");
		s.initVerify(publicKey);
		
		return s;
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
				
			} 
			else if (option.equals("-s")) {
				
			} 
			else {
				
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
}

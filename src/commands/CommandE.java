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
import java.security.PrivateKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class CommandE {

	private final String ip;
	private final int port;
	private List<String> files;

	public CommandE(String ip, int port, List<String> files) {
		this.ip = ip;
		this.port = port;
		this.files = files;
	}
	
	private SecretKey getSymetricKey() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		
		KeyGenerator kg = KeyGenerator.getInstance("AES");
	    kg.init(128);
	    SecretKey key = kg.generateKey();
		return key;
	}
	
	private Signature assignFile() throws NoSuchAlgorithmException {
		
		//Set the signature
		Signature signature = Signature.getInstance("MD5withRSA");   
		
		try {
			//Read the KeyStore File
			FileInputStream keyStorefile = new FileInputStream(new File("../src/KeyStore.si027")); 
			
			//Alias from set up in KeyStore file
			String alias = "si027";
			
			//Get the instance of keyStore
			KeyStore kstore = KeyStore.getInstance("PKCS12"); 
			
			//verify password and load KeyStore file
			kstore.load(keyStorefile, "si027marcos&rafael".toCharArray()); 
			
			//Get the private key 
			Key key = kstore.getKey(alias, "si027marcos&rafael".toCharArray()); 
			
			//turn key in instance of private key
			PrivateKey privatekey = (PrivateKey) key; 
			
			//Encrypted digital assign
			signature.initSign(privatekey); 
						
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return signature;
	} 
	
	public void sendToServer() throws UnknownHostException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
		
		Socket socket = new Socket(this.ip, this.port);
		
		ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
		outStream.writeObject("-e");
		outStream.writeObject(this.files.size());
		
		for (String fileName : this.files) {
		
			//falta verificar se existe 
			
			SecretKey symetricKey = getSymetricKey();
			
			Cipher c = Cipher.getInstance("AES");
		    c.init(Cipher.ENCRYPT_MODE, symetricKey);
		    
		    FileInputStream fileInStream = new FileInputStream("../files/" + fileName);
		    int totalFileLenght = fileInStream.available();
		    
		    outStream.writeObject(Math.min(totalFileLenght, 1024));
		    byte[] bufferData = new byte[Math.min(totalFileLenght, 1024)];  
		    int i = fileInStream.read(bufferData);
		    
		}
	}
}

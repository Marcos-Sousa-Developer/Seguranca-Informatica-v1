package commands;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;


public class CommandS {
	
	private String ip; 
	
	private int port; 
	
	private List<String> files;
	
	
	public CommandS(String ip, int port, List<String> files) { 
		
		this.ip = ip;
		this.port = port;
		this.files = files;
		
	}
	
	private String removeExtension(String fileName){
		
		return "";
		
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
			
			System.out.println(privatekey);
			

			//Encrypted digital assign
			signature.initSign(privatekey); 
			
			System.out.println("init");
			
			
			
			
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
	
	public void sendToServer() throws UnknownHostException, IOException, NoSuchAlgorithmException, SignatureException {
		
		//Set the server
		Socket socket = new Socket(this.ip, this.port);
		
		//Send data to server
		ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
		
		//Send the option first
		outStream.writeObject("-s");
		
		for (String fileName : this.files) { 
			
			
			
			//Send the file name
			outStream.writeObject(fileName);
			
			//Read the received file 
			FileInputStream fileInStream = new FileInputStream(fileName); 
			
			//get signature object 
			Signature signature = assignFile(); 
			
			//byte array for file
			byte[] dataToBytes = new byte[1024]; 
			
			//Length of the contents of the read file 
			int contentLength = fileInStream.read(dataToBytes); 
			
			//read files chunk 
			while(contentLength != -1) {
				//Hash the data
				signature.update(dataToBytes, 0, contentLength);
				//send data to server
				outStream.write(dataToBytes);
				//continue to read fileInStream
				contentLength = fileInStream.read();
			}
			outStream.writeObject("signature");
			outStream.write(signature.sign());

		}
		
	}
	
	
}

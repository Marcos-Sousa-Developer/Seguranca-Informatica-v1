package commands;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CommandC {
	
	private String ip;
	private int port;
	private List<String> files;

	public CommandC(String ip, int port, List<String> files) {
		this.ip = ip;
		this.port = port;
		this.files = files;
	}
	
	/**
	 * Ciphers the file and save it on client
	 * @String the fileName that wants to cipher
	 */
	private void cipherFile(String fileName) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException {
		
		//random key
		KeyGenerator kg = KeyGenerator.getInstance("AES");
	    kg.init(128);
	    SecretKey key = kg.generateKey();

	    Cipher c = Cipher.getInstance("AES");
	    c.init(Cipher.ENCRYPT_MODE, key);

	    //get file in files
	    FileInputStream fis = new FileInputStream("../files/" + fileName);
	    // create file.cif in files
	    FileOutputStream fos = new FileOutputStream("../files/" + fileName + ".cifrado");
	    CipherOutputStream cos = new CipherOutputStream(fos, c);
	    
	    int totalFileLength = fis.available();
		byte[] dataToBytes = new byte[Math.min(totalFileLength, 1024)]; 
	    
		int contentLength = fis.read(dataToBytes);
	    while (contentLength != -1) {
	        cos.write(dataToBytes, 0, contentLength);
	        contentLength = fis.read(dataToBytes);
	    }
	    cos.close();
	    fis.close();

	    byte[] keyEncoded = key.getEncoded();
	    //save file.key in files
	    FileOutputStream kos = new FileOutputStream("../files/" + fileName + ".key");
	    
	    kos.write(keyEncoded);
	    kos.close();
	}
	
	/**
	 * Cipher the key and save it
	 * @String fileName
	 */
	private void cipherKey(String fileName) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException {
		
		//get the 	KeyStore
		FileInputStream kfile = new FileInputStream("KeyStore.si027Cloud"); 
	    KeyStore kstore = KeyStore.getInstance("PKCS12");
	    kstore.load(kfile, "si027marcos&rafael".toCharArray());
	    
	    //get key from KeyStore
	    Key key = kstore.getKey("si027", "si027marcos&rafael".toCharArray());
	    
	    if(key instanceof PrivateKey) {
	    	
	    	Certificate cert = kstore.getCertificate("si027");
	    	
	    	PublicKey publicKey =cert.getPublicKey();
	    
		    Cipher cRSA = Cipher.getInstance("RSA");
		    
		    cRSA.init(Cipher.WRAP_MODE, publicKey);
		    
		    // get the file.key previous created
		    FileInputStream kis = new FileInputStream("../files/" + fileName + ".key");
		    
		    byte[] keyEncoded = new byte [kis.available()];
		    
		    kis.read(keyEncoded);
		    kis.close();
		    
		    SecretKey keyAES = new SecretKeySpec(keyEncoded,"AES");
		    
		    byte [] chaveAEScifrada = cRSA.wrap(keyAES);
		    
		    FileOutputStream kos = new FileOutputStream("../files/" + fileName + ".chave_secreta");
		    
		    kos.write(chaveAEScifrada);
		    
		    kos.close();
		    
		    //delete the key after generate the secret key
	        File fKey = new File("../files/" + fileName + ".key");
	        fKey.delete();
	    }
	}
	
	/**
	 * Method to communicate with the server
	 */
	public void sendToServer() throws UnknownHostException, IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnrecoverableKeyException, KeyStoreException, CertificateException, IllegalBlockSizeException {
		
		Socket socket = null;
		try {
			 socket = new Socket(this.ip, this.port);
		}
		catch (ConnectException e) {
			System.out.println("Connection refused, please check the Port");
			System.exit(-1);
		}
		catch (UnknownHostException e) {
			
			System.out.println("Connection refused, please check the Host");
			System.exit(-1);
		}
		
		ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
		
		outStream.writeObject("-c");
		outStream.writeObject(this.files.size());
		
		for (String fileName : this.files) {
			
			// get the file that you want to send
			File f = new File("../files/" + fileName);
			
			// check if file exists
			Boolean fileExistClient = f.exists();
			
			//if file exists on client can proceed 
			if(fileExistClient) {
				
				//say to server that file exists
				outStream.writeObject(fileExistClient); 
				
				//send the file name
				outStream.writeObject(fileName);
				
				//check if file exists on the server
				Boolean fileExistServer = (Boolean) inStream.readObject();
				
				// if file does not exists on the client send it
				if(!fileExistServer) {
					
					// cipher the file
					cipherFile(fileName);
					//cipher the key
					cipherKey(fileName);
					
					//get the file previous create
					File fileCif = new File("../files/" + fileName + ".cifrado");
			        Long dimFileCif = fileCif.length();
			        
			        outStream.writeObject(fileName + ".cifrado");
			        outStream.writeObject(dimFileCif);
			        
			        BufferedInputStream myFileCif = new BufferedInputStream(new FileInputStream("../files/" + fileName + ".cifrado"));
			        
			        int dimFileCifInt = dimFileCif.intValue();
			        
					byte[] dataToBytesCif = new byte[Math.min(dimFileCifInt, 1024)]; 
				    
					int contentLengthCif = myFileCif.read(dataToBytesCif);
				    while (contentLengthCif > 0) {
				    	outStream.write(dataToBytesCif, 0, contentLengthCif);
				    	contentLengthCif = myFileCif.read(dataToBytesCif);
				    }
			        myFileCif.close();
			        //after send it can delete the file .cipher
			        fileCif.delete();
			        
			        //get the file.key
			        File keyCif = new File("../files/" + fileName + ".chave_secreta");
			        Long dimKeyCif = keyCif.length();
			        
			        outStream.writeObject(fileName + ".chave_secreta");
			        outStream.writeObject(dimKeyCif);      
			        
			        BufferedInputStream myKeyCif = new BufferedInputStream(new FileInputStream("../files/" + fileName + ".chave_secreta"));
			        
			        int dimKeyCifInt = dimKeyCif.intValue();
			        
					byte[] dataToBytesKey = new byte[Math.min(dimKeyCifInt, 1024)]; 
				    
					int contentLengthKey = myKeyCif.read(dataToBytesKey);
				    while (contentLengthKey > 0) {
				    	outStream.write(dataToBytesKey, 0, contentLengthKey);
				    	contentLengthKey = myKeyCif.read(dataToBytesKey);
				    }
			        myKeyCif.close();
			        //after send it can delete
			        keyCif.delete();
			       
			        System.out.println("The file " + fileName + " have been sent correctly.");
					
			        
				} else {
					System.err.println("The file " + fileName + " already exist in server.");
				}
				
			//file does not exists on client so can't proceed 
			} else {
				outStream.writeObject(fileExistClient);
				System.err.println("The file " + fileName + " doesn't exist. You must provide a existing file.");
			}
		}
		outStream.close();
		socket.close();
	}
}

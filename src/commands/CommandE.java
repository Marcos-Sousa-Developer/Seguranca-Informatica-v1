package commands;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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
	
	private Signature initSignature() throws NoSuchAlgorithmException {
		
		//Set the signature
		Signature signature = Signature.getInstance("SHA256withRSA");   
		
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
	
	
	private byte[] cipherKey(SecretKey secretkey) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException {
		FileInputStream kfile = new FileInputStream("KeyStore.si027"); 
	    KeyStore kstore = KeyStore.getInstance("PKCS12");
	    kstore.load(kfile, "si027marcos&rafael".toCharArray());
	    
	    String alias = "si027";
	    
	    Key key = kstore.getKey(alias, "si027marcos&rafael".toCharArray());
	    
	    Certificate cert = kstore.getCertificate("si027");
    	
    	PublicKey publicKey = cert.getPublicKey();
    
	    Cipher cRSA = Cipher.getInstance("RSA");
	    
	    cRSA.init(Cipher.WRAP_MODE, publicKey);
	    	    
	    byte[] AESkey = secretkey.getEncoded();
	    
	    SecretKey keyAES = new SecretKeySpec(AESkey, "AES");
	    
	    byte[] wrappedkey = cRSA.wrap(keyAES);
	    
	    return wrappedkey;
	    
	}
	
	
	public void sendToServer() throws UnknownHostException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, SignatureException, UnrecoverableKeyException, KeyStoreException, CertificateException, IllegalBlockSizeException, BadPaddingException {
		
		Socket socket = new Socket(this.ip, this.port);
		
		ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
		outStream.writeObject("-e");
		outStream.writeObject(this.files.size());
		
		for (String fileName : this.files) {
			
			outStream.writeObject(fileName);
			
			File fileToRead = new File("../files/" + fileName);
			FileInputStream fileInStream = new FileInputStream(fileToRead);
			
			Signature signature = initSignature();
			
		    SecretKey secretkey = getSymetricKey();

			int totalFileLength = fileInStream.available();
	        
	        outStream.writeObject(totalFileLength);
	        
	        byte[] dataToBytes = new byte[Math.min(totalFileLength, 1024)];
	        //byte[] dataToBytes2 = new byte[Math.min(totalFileLength, 1024)];
			
	        int contentFileLength = fileInStream.read(dataToBytes);
	        //dataToBytes2 = dataToBytes;
	        
		    Cipher c = Cipher.getInstance("AES");
		    
		    c.init(Cipher.ENCRYPT_MODE, secretkey);
		    
		    FileOutputStream fos = new FileOutputStream(fileName + ".cifrado");
		    CipherOutputStream cos = new CipherOutputStream(fos, c); 

		    
		    while (contentFileLength != -1) {
		        cos.write(dataToBytes, 0, contentFileLength);
		        contentFileLength = fileInStream.read(dataToBytes);
		    }
		    cos.close();
		    fileInStream.close();
		    
		    BufferedInputStream myFileCif = new BufferedInputStream(new FileInputStream(fileName + ".cifrado"));
		    
		    File fileCif = new File(fileName + ".cifrado");
	        Long dimFileCif = fileCif.length();
		    
		    int dimFileCifInt = dimFileCif.intValue();
	        
			byte[] dataToBytesCif = new byte[Math.min(dimFileCifInt, 1024)]; 
		    
			int contentLengthCif = myFileCif.read(dataToBytesCif);
		    while (contentLengthCif != -1) {
		    	signature.update(dataToBytes);
		    	outStream.write(dataToBytesCif, 0, contentLengthCif);
		    	contentLengthCif = myFileCif.read(dataToBytesCif);
		    }
		    
		    myFileCif.close();
		    
		    //BufferedInputStream myFileCif = new BufferedInputStream(fileInStream);
	        //CipherOutputStream cipherOutStream = new CipherOutputStream(outStream, c);
		    
	        //int contentFileLength = myFileCif.read(dataToBytes);
	        //myFileCif.read(dataToBytes2);

	        /*while (contentFileLength != -1) {
	        	signature.update(dataToBytes);
	        	byte[] byteEncripted = c.update(dataToBytes2, 0, contentFileLength);
	        	outStream.write(byteEncripted);
	        	//outStream.write(c.update(dataToBytes));
	        	contentFileLength = fileInStream.read(dataToBytes);
	        	dataToBytes2 = dataToBytes;
	        }
	        
	        outStream.write(c.doFinal());
	        */
		    
		    
		    
		    
	        outStream.writeObject(signature.sign());
	        
	        outStream.writeObject(cipherKey(secretkey));

		
			//falta verificar se existe 
			
//			SecretKey symetricKey = getSymetricKey();
//			
//			Cipher c = Cipher.getInstance("AES");
//		    c.init(Cipher.ENCRYPT_MODE, symetricKey);
//		    
//		    FileInputStream fileInStream = new FileInputStream("../files/" + fileName);
//		    int totalFileLenght = fileInStream.available();
//		    
//		    outStream.writeObject(Math.min(totalFileLenght, 1024));
//		    byte[] bufferData = new byte[Math.min(totalFileLenght, 1024)];  
//		    int i = fileInStream.read(bufferData);
			
		
			
		    
		}
	}
}

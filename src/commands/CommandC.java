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
	private String option;

	public CommandC(String ip, int port, List<String> files, String option) {
		this.ip = ip;
		this.port = port;
		this.files = files;
		this.option = option;
	}
	
	public void cipherFile(String fileName) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException {
		
		KeyGenerator kg = KeyGenerator.getInstance("AES");
	    kg.init(128);
	    SecretKey key = kg.generateKey();

	    Cipher c = Cipher.getInstance("AES");
	    c.init(Cipher.ENCRYPT_MODE, key);

	    FileInputStream fis;
	    FileOutputStream fos;
	    CipherOutputStream cos;
	    
	    fis = new FileInputStream("../files/" + fileName);
	    fos = new FileOutputStream("../files/" + fileName + ".cifrado");

	    cos = new CipherOutputStream(fos, c);
	    byte[] b = new byte[16];  
	    int i = fis.read(b);
	    while (i != -1) {
	        cos.write(b, 0, i);
	        i = fis.read(b);
	    }
	    cos.close();
	    fis.close();

	    byte[] keyEncoded = key.getEncoded();
	    FileOutputStream kos = new FileOutputStream("../files/" + fileName + ".key");
	    
	    kos.write(keyEncoded);
	    kos.close();
	}
	

	public void cipherKey(String fileName) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException {
		FileInputStream kfile = new FileInputStream("keystore.si027"); 
	    KeyStore kstore = KeyStore.getInstance("PKCS12");
	    kstore.load(kfile, "si027marcos&rafael".toCharArray());
	    
	    String alias = "si027";
	    
	    Key key = kstore.getKey(alias, "si027marcos&rafael".toCharArray());
	    
	    if(key instanceof PrivateKey) {
	    	
	    	Certificate cert = kstore.getCertificate("si027");
	    	
	    	PublicKey publicKey =cert.getPublicKey();
	    
		    Cipher cRSA = Cipher.getInstance("RSA");
		    
		    cRSA.init(Cipher.WRAP_MODE, publicKey);
		    
		    FileInputStream kis = new FileInputStream("../files/" + fileName + ".key");
		    
		    byte[] keyEncoded = new byte [kis.available()];
		    
		    kis.read(keyEncoded);
		    kis.close();
		    
		    SecretKey keyAES = new SecretKeySpec(keyEncoded,"AES");
		    
		    byte [] chaveAEScifrada = cRSA.wrap(keyAES);
		    
		    FileOutputStream kos = new FileOutputStream("../files/" + fileName + ".chave_secreta");
		    
		    kos.write(chaveAEScifrada);
		    
		    kos.close();
		    }
	}
	
	
	public void sendToServer() throws UnknownHostException, IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnrecoverableKeyException, KeyStoreException, CertificateException, IllegalBlockSizeException {
		
		Socket socket = new Socket(this.ip, this.port);
		
		ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
		outStream.writeObject(this.option);
		outStream.writeObject(this.files.size());
		
		for (String fileName : this.files) {
			
			 //Caso algum dos ficheiros já exista no servidor 
			 // ou caso algum dos ficheiros não exista localmente, 
			 //apresenta uma mensagem de erro ao 
			 //utilizador e continua para os seguintes ficheiros.
			
			try {
				cipherFile(fileName);
				cipherKey(fileName);
				
				//---------------Enviar Ficheiro Cifrado----------------------
				
				File fileCif = new File("../files/" + fileName + ".cifrado");
		        Long dimFileCif = fileCif.length();
		        
		        outStream.writeObject(fileName + ".cifrado");
		        outStream.writeObject(dimFileCif);
		        
		        BufferedInputStream myFileCif = new BufferedInputStream(new FileInputStream("../files/" + fileName + ".cifrado"));
		        byte[] bufferFileCif = new byte[1024];
		        int xCif = 0;
		        while((xCif = myFileCif.read(bufferFileCif, 0, 1024)) > 0){
		            outStream.write(bufferFileCif, 0, xCif);
		        }
		        myFileCif.close();
		        
		      //---------------Enviar Chave Cifrada----------------------
		        
		        File keyCif = new File("../files/" + fileName + ".chave_secreta");
		        Long dimKeyCif = keyCif.length();
		        
		        outStream.writeObject(fileName + ".chave_secreta");
		        outStream.writeObject(dimKeyCif);
		        
		        BufferedInputStream myKeyCif = new BufferedInputStream(new FileInputStream("../files/" + fileName + ".chave_secreta"));
		        byte[] bufferKeyCif = new byte[1024];
		        int xKey = 0;
		        while((xKey = myKeyCif.read(bufferKeyCif, 0, 1024)) > 0){
		            outStream.write(bufferKeyCif, 0, xKey);
		        }
		        myKeyCif.close();
		        
		        System.out.println("The file " + fileName + " have been sent correctly.");

			} catch (FileNotFoundException error) {
				System.err.println("The file " + fileName + " doesn't exist. You must provide a existing file.");
			}
		}

		outStream.close();
		socket.close();
	}
}

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
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class CommandC {
	
	private final String ip;
	private final int port;
	private List<String> files;
	private final String option;

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
	    FileOutputStream kos = new FileOutputStream("../files/" + fileName + ".chave_secreta");
	    
	    
	    
	    //Falta cifrar a key
	    
	    
	    
	    
	    kos.write(keyEncoded);
	    kos.close();
	}

	public void cipherKey(String fileName) {
		
	}
	
	public void sendToServer() throws UnknownHostException, IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
		
		Socket socket = new Socket(this.ip, this.port);
		
		ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
		outStream.writeObject(this.option);
		outStream.writeObject(this.files.size());
		
		for (String fileName : this.files) {

			//---------------Enviar Ficheiro Cifrado----------------------
			
			cipherFile(fileName);
			
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
    	}

		outStream.close();
		socket.close();
	}
}

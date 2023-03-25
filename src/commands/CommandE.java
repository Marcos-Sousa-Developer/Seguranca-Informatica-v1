package commands;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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
				
	public void sendToServer() throws UnknownHostException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, SignatureException, UnrecoverableKeyException, KeyStoreException, CertificateException, IllegalBlockSizeException, BadPaddingException {
		
		
		Socket socket = new Socket(this.ip, this.port);
		
		ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
		
		outStream.writeObject("-e");  
		
		cifraText(); 
		
		cifraChave();

		FileInputStream fileInStream = new FileInputStream("a.cif"); 

		//get total file length
		int totalFileLength = fileInStream.available();
		
		//send to server exact buffer size
		outStream.writeObject(totalFileLength);

		//byte array for file
		byte[] dataToBytes = new byte[Math.min(totalFileLength, 1024)]; 
		
		//Length of the contents of the read file 
		int contentLength = fileInStream.read(dataToBytes); 
		
		//read files chunk 
		while(contentLength != -1 ) {
			
			//send data to server
			outStream.write(dataToBytes,0,contentLength);
			//continue to read fileInStream
			contentLength = fileInStream.read(dataToBytes);
		}
		
		fileInStream.close(); 
		
		
		FileInputStream fileInStreamkey = new FileInputStream("aa.key");  
		
		outStream.writeObject(fileInStreamkey.readAllBytes()); 
		
		fileInStreamkey.close();

	}
	
	public void cifraText() throws NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidKeyException {
		
		/////CHAVEEEEE/////
		
	    //gerar uma chave aleatoria para utilizar com o AES
	    KeyGenerator kg = KeyGenerator.getInstance("AES");
	    kg.init(128);
	    SecretKey key = kg.generateKey();

	    Cipher c = Cipher.getInstance("AES");
	    c.init(Cipher.ENCRYPT_MODE, key);

	    FileInputStream fis;
	    FileOutputStream fos;
	    CipherOutputStream cos;
	    
	    fis = new FileInputStream("../files/okok.txt");
	    fos = new FileOutputStream("a.cif");

	    cos = new CipherOutputStream(fos, c);
	    byte[] b = new byte[16];  
	    int i = fis.read(b);
	    while (i != -1) {
	        cos.write(b, 0, i);
	        i = fis.read(b);
	    }
	    cos.close();

	    byte[] keyEncoded = key.getEncoded();
	    FileOutputStream kos = new FileOutputStream("a.key");
	    kos.write(keyEncoded);
	    kos.close();
		
	}
	
	private void cifraChave() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException {
		
    	
    	// ler minha keystote
    	FileInputStream kfile = new FileInputStream("KeyStore.si027");
    	
    	//criar key store do tipo
    	KeyStore keystore = KeyStore.getInstance("PKCS12");
    	
    	//qual a password e fazer load
    	keystore.load(kfile, "si027marcos&rafael".toCharArray());
    	
    	//meu utilizador dentro da keysotre
    	String alias = "si027"; 
    	
    	//obter key do utilizador com respetiva pass
    	Key key = keystore.getKey(alias, "si027marcos&rafael".toCharArray());  
    	
    	Certificate cert = keystore.getCertificate(alias);
    	
    	//obter public key, vai ser usada para cifra hibrida
    	PublicKey pubkey = cert.getPublicKey();
    	
    	// criar cifra do tipo rsaokokokokokok
    	Cipher c = Cipher.getInstance("RSA"); 
    	
    	//iniciar da cifra no wrap mode com a pub key
    	c.init(Cipher.WRAP_MODE, pubkey);
    	
    	//ler a chave
    	FileInputStream fis = new FileInputStream("a.key");
    	
    	//ler o conteudo dos bytes
    	byte[] AESkey = new byte[fis.available()]; 
    	int i = fis.read(AESkey);
    	fis.close();
    	
    	SecretKey keyAES = new SecretKeySpec(AESkey, "AES");
    	
    	byte[] wrappedKey = c.wrap(keyAES);	
    	
    	FileOutputStream fos = new FileOutputStream("aa.key");
    	
    	fos.write(wrappedKey); 
    	
    	fos.close();
		
	}
}

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
	
	private void cipherFile(String fileName) throws NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidKeyException {
		
	    KeyGenerator kg = KeyGenerator.getInstance("AES");
	    kg.init(128);
	    SecretKey key = kg.generateKey();

	    Cipher c = Cipher.getInstance("AES");
	    c.init(Cipher.ENCRYPT_MODE, key);

	    FileInputStream fis = new FileInputStream("../files/" + fileName);
	    FileOutputStream fos = new FileOutputStream(fileName + ".seguro");
	    CipherOutputStream cos = new CipherOutputStream(fos, c);

	    int totalFileLength = fis.available();
		byte[] dataToBytes = new byte[Math.min(totalFileLength, 1024)];
		
	    int i = fis.read(dataToBytes);
	    while (i != -1) {
	        cos.write(dataToBytes, 0, i);
	        i = fis.read(dataToBytes);
	    }
	    cos.close();
	    fis.close();

	    byte[] keyEncoded = key.getEncoded();
	    FileOutputStream kos = new FileOutputStream(fileName + ".key");
	    kos.write(keyEncoded);
	    kos.close();
		
	}
	
	private void cipherKey(String fileName) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException {
		
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
    	FileInputStream fis = new FileInputStream(fileName + ".key");
    	
    	//ler o conteudo dos bytes
    	byte[] AESkey = new byte[fis.available()]; 
    	int i = fis.read(AESkey); //NÃO FOI USADO
    	fis.close();
    	
    	SecretKey keyAES = new SecretKeySpec(AESkey, "AES");
    	
    	byte[] wrappedKey = c.wrap(keyAES);	
    	
    	FileOutputStream fos = new FileOutputStream(fileName + ".chave_secreta");
    	
    	fos.write(wrappedKey); 
    	
    	fos.close();
	}
	
	public void sendToServer() throws UnknownHostException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, SignatureException, UnrecoverableKeyException, KeyStoreException, CertificateException, IllegalBlockSizeException, BadPaddingException, ClassNotFoundException {
		
		Socket socket = new Socket(this.ip, this.port);
		
		ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
		
		outStream.writeObject("-e");  
		outStream.writeObject(this.files.size());
		
		for (String fileName : this.files) {
			
			File f = new File("../files/" + fileName);
			
			Boolean fileExistClient = f.exists();
			
			if(fileExistClient) {//
				outStream.writeObject(fileExistClient);
				outStream.writeObject(fileName);
				
				Boolean fileExistServer = (Boolean) inStream.readObject();
				
				if(!fileExistServer) {
		
					cipherFile(fileName); 
					cipherKey(fileName);
			
					FileInputStream fileInStream = new FileInputStream(fileName + ".seguro");
			
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
					
					FileInputStream fileInStreamkey = new FileInputStream(fileName + ".chave_secreta");  
					
					outStream.writeObject(fileInStreamkey.readAllBytes());
					
					fileInStreamkey.close();
					
					System.out.println("The file " + fileName + " have been sent correctly.");
					
					/*File fCif = new File(fileName + ".seguro");
			        File fKey = new File(fileName + ".key");
			        File fKeyCif = new File(fileName + ".chave_secreta");
			        
			        fCif.delete();
			        fKey.delete();
			        fKeyCif.delete();*/
				}
				else {
					System.err.println("The file " + fileName + " already exist in server.");
				}
			} else {
				outStream.writeObject(fileExistClient);
				System.err.println("The file " + fileName + " doesn't exist. You must provide a existing file.");
			}
		}
	}
}

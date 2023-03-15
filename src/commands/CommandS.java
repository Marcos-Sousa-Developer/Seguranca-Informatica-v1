package commands;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class CommandS {
	
	private String ip; 
	
	private int port; 
	
	private List<String> files;
	
	
	public CommandS(String ip, int port, List<String> files) { 
		
		this.ip = ip;
		this.port = port;
		this.files = files;
		
	}
	
	private void assignFile(String fileName) {
		
		
		try {
			//Read the received file 
			FileInputStream fis = new FileInputStream(fileName); 
			
			//Read the KeyStore File
			FileInputStream keyStorefile = new FileInputStream("../keystore.si027"); 
			
			
			
			
			
			
			
			
			
			
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} 
		
		
		
	}
}

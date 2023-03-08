import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class myCloud {
	
	private static String verifyCommand(String[] args) {
		
		if(!args[0].equals("-a")){
			System.err.println("You must provide -a option.");
	    	System.exit(-1);
		}else {
			String[] address = args[1].split(":");
			if(address.length == 2) {
				String ip = address[0];
				String port = address[1];
				
				String[] options = new String[]{"-c", "-s", "-e", "-g"};
				
				if(!Arrays.asList(options).contains(args[2])){
					System.err.println("You must provide a valid option.");
					System.err.println("Valid options: -c, -s, -e, -g");
			    	System.exit(-1);
				} 
				
				 if (args[2].equals("-c")) {
					 //method
				 }else if (args[2].equals("-s")) {
		             //method
		         }else if (args[2].equals("-e")) {
		             //method
		         }else if (args[2].equals("-g")) {
		             //method
		         } 
				
				
				
			} else {
				System.err.println("You must provide a valid address.");
				System.err.println("Example: 127.0.0.1:23456");
		    	System.exit(-1);
			}
		}
		
		return null;
	}

	public static void main(String[] args) throws Exception, IOException {
		
		
		verifyCommand(args);
		
		
		
		
		
		Socket socket = new Socket("127.0.0.1", 23456);
		
		socket.close();
	}
}
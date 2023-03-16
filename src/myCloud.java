import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import commands.CommandC;
import commands.CommandS;
import commands.VerifyPort;

public class myCloud {
	
	private static String[] verifyCommand(String[] args) {
		
		String ip = "";
		String port = ""; 
		
		if(args.length < 4) {
			System.err.println("Command not valid.");
			System.err.println("Example: myCloud -a <serverAddress> {-c || -s || -e || -g} {<filenames>}+");
	    	System.exit(-1);
		}

		if(!args[0].equals("-a")){
			System.err.println("You must provide -a option.");
	    	System.exit(-1);
		}else {
			String[] address = args[1].split(":");
			if(address.length == 2) {
				ip = address[0];
				port = new VerifyPort(address[1]).verifyPort();
				
				String[] options = new String[]{"-c", "-s", "-e", "-g"};
				
				if(!Arrays.asList(options).contains(args[2])){
					System.err.println("You must provide a valid option.");
					System.err.println("Valid options: -c || -s || -e || -g");
			    	System.exit(-1);
				}
			} else {
				System.err.println("You must provide a valid address.");
				System.err.println("Example: 127.0.0.1:23456");
		    	System.exit(-1);
			}
		}
		return new String[]{ip, port};
	}

	public static void main(String[] args) throws Exception {
		
		
		String[] address = verifyCommand(args);
		
		List<String> files = new ArrayList<>(Arrays.asList(args)).subList(3, args.length);

		switch (args[2]) {
			case "-c":

				new CommandC(address[0], Integer.parseInt(address[1]), files, "-c").sendToServer();

				break;
			case "-s":

				new CommandS(address[0], Integer.parseInt(address[1]), files).sendToServer();

				break;
			case "-e":

				//method

				break;
			case "-g":

				//method

				break;
		}
	}
}
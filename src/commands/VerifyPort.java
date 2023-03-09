package commands;

public class VerifyPort {

	private String port;
	
	public VerifyPort(String port) {
		this.port = port;
	}
	
	public String verifyPort() {
		
		try {
	        Integer.parseInt(this.port);
	    } catch (NumberFormatException errr) {
	    	System.err.println("Number port not valid.");
	    	System.exit(-1);
	    }
		
		int port = Integer.parseInt(this.port);
		
		if (!(port > 1024 && port < 65535)) {
			System.err.println("You should use a number port between 1024 and 65535.");
			System.exit(-1);
		}	
		return this.port;
	}
}

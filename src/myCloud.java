import java.io.IOException;
import java.net.Socket;

public class myCloud {

	public static void main(String[] args) throws Exception, IOException {
		
		Socket socket = new Socket("127.0.0.1", 23456);
		
		socket.close();
	}
}
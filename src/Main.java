/**
 * 
 */

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.concurrent.Sleep;
import com.trendrr.zmq.client.ZMQClient;
import com.trendrr.zmq.client.ZMQClientMessageHandler;
import com.trendrr.zmq.server.ZMQChannel;
import com.trendrr.zmq.server.ZMQMessageHandler;
import com.trendrr.zmq.server.ZMQServer;


/**
 * @author Dustin Norlander
 * @created May 11, 2012
 * 
 */
public class Main {

	protected static Log log = LogFactory.getLog(Main.class);
	
	public static void main(String ...strings) throws UnsupportedEncodingException {
//		startServer();
		ZMQClient client = startClient();
		
		client.send("this is a message".getBytes("utf8"));
		Sleep.seconds(1);
		client.send("this is a message 2".getBytes("utf8"));
		Sleep.seconds(20);
		System.exit(1);
	}
	
	public static ZMQClient startClient() {
		
		ZMQClientMessageHandler handler = new ZMQClientMessageHandler() {
			
			@Override
			public void incoming(ZMQClient client, byte[] message) {
				try {
					System.out.println("Client INCOMING " + client + ": " + new String(message, "utf8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			@Override
			public void error(Exception x) {
				x.printStackTrace();
			}
		};
		
		ZMQClient client = new ZMQClient("tcp://localhost:8988", handler);
		client.connect();
		return client;
		
	}
	
	public static void startServer() {
		
		ZMQServer server = new ZMQServer();
		ZMQMessageHandler handler = new ZMQMessageHandler() {
			
			@Override
			public void incoming(ZMQChannel channel, byte[] message) {
				try {
					String received = new String(message, "utf8");
					String id = new String(channel.getId(), "utf8");
					System.out.println("SERVER RECIEVED: " + received + " from ID: " + id);
					//send back the message..
					channel.send(("RECEIVED: " + received).getBytes("utf8"));					
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void error(Exception x) {
				x.printStackTrace();
			}
		};
		
		server.listen(8988, handler, true);
	}
}

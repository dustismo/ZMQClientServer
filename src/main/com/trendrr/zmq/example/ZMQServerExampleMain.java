/**
 * 
 */
package com.trendrr.zmq.example;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.zmq.server.ZMQChannel;
import com.trendrr.zmq.server.ZMQServer;
import com.trendrr.zmq.server.ZMQServerMessageHandler;


/**
 * @author Dustin Norlander
 * @created May 29, 2012
 * 
 */
public class ZMQServerExampleMain {

	protected static Log log = LogFactory.getLog(ZMQServerExampleMain.class);

	/**
	 * This is a simple echo server.  
	 * 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		ZMQServer server = new ZMQServer();
		ZMQServerMessageHandler handler = new ZMQServerMessageHandler() {
//			AtomicLong received = new AtomicLong(0);
			
			@Override
			public void incoming(ZMQChannel channel, byte[] message) {
				try {
					String received = new String(message, "utf8");
					String id = new String(channel.getId(), "utf8");
//					System.out.println("SERVER RECIEVED: " + received + " from ID: " + id);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				//send back the message..
				channel.send(message);
			}
			
			@Override
			public void error(Exception x) {
				x.printStackTrace();
			}
		};
		
		server.listen(8988, handler, false);

	}
}

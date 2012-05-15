/**
 * 
 */
package com.trendrr.zmq.tests;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.trendrr.oss.StringHelper;
import com.trendrr.oss.concurrent.Sleep;
import com.trendrr.zmq.client.ZMQClient;
import com.trendrr.zmq.client.ZMQClientMessageHandler;
import com.trendrr.zmq.server.ZMQChannel;
import com.trendrr.zmq.server.ZMQServerMessageHandler;
import com.trendrr.zmq.server.ZMQServer;

/**
 * @author Dustin Norlander
 * @created May 14, 2012
 * 
 */
public class ClientServerTest {

	protected static Log log = LogFactory.getLog(ClientServerTest.class);
	ZMQServer server = new ZMQServer();
	Set<String> sent = Collections.synchronizedSet(new HashSet<String>());
	
	@Test
	public void testMessagePassing() {
		//simple, counts echo receives
		
		this.startEchoServer();
		
		ZMQClient client = this.startEchoClient();
		for (int i=0; i < 10000; i++) {
			try {
				String message = "message: " + StringHelper.randomString(10) + " " + i;
				sent.add(message);
				client.send(message.getBytes("utf8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Sleep.seconds(2);
		//cleanup 
		Assert.assertTrue(sent.isEmpty());
	}
	
	
	
	
	public ZMQClient startEchoClient() {
		
		ZMQClientMessageHandler handler = new ZMQClientMessageHandler() {
			
			@Override
			public void incoming(ZMQClient client, byte[] message) {
				
				
				try {
					String msg = new String(message, "utf8");
					sent.remove(msg);
//					System.out.println("Client INCOMING " + client + ": " + new String(message, "utf8"));
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
	
	public void startEchoServer() {
		
		
		ZMQServerMessageHandler handler = new ZMQServerMessageHandler() {
			
			@Override
			public void incoming(ZMQChannel channel, byte[] message) {
//				try {
//					String received = new String(message, "utf8");
//					String id = new String(channel.getId(), "utf8");
//					System.out.println("SERVER RECIEVED: " + received + " from ID: " + id);
//					//send back the message..
//					channel.send(("RECEIVED: " + received).getBytes("utf8"));			
//					
//					
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//				}
				channel.send(message);
			}
			
			@Override
			public void error(Exception x) {
				x.printStackTrace();
			}
		};
		
		server.listen(8988, handler, true);
	}
	
	
}

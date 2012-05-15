/**
 * 
 */
package com.trendrr.zmq.tests;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
public class EchoTests {

	protected static Log log = LogFactory.getLog(EchoTests.class);
	ZMQServer server = new ZMQServer();
	
	@Test
	public void multiClientTest() {
		//Tests multiple concurrent clients
		int numClients = 5;
		
		this.startEchoServer();
		List<EchoClient> clients = new ArrayList<EchoClient>();
		for (int i=0; i < numClients; i++) {
			clients.add(new EchoClient("tcp://localhost:8988"));
		}
		
		
		
		for (int i=0; i < 1000; i++) {
			for (int cind=0; cind < numClients; cind++) {
				try {
					EchoClient client = clients.get(cind);
					
					String message = "message: CLIENT: " + cind + " " + StringHelper.randomString(10) + " " + i;
//					System.out.println(message);
					client.send(message.getBytes("utf8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		Sleep.seconds(4);
		//TODO: cleanup 
		for (EchoClient c: clients) {
			c.close();
		}
		server.close();
		Sleep.seconds(2);//make sure it gets closed

		for (EchoClient client: clients) {
			System.out.println("Messages remaining: " + client.getSent().size());
			Assert.assertTrue(client.getSent().isEmpty());
		}
		
	}
	
	@Test
	public void singleClientTest() {
		//simple, counts echo receives
		
		this.startEchoServer();
		EchoClient client = new EchoClient("tcp://localhost:8988");
		for (int i=0; i < 1000; i++) {
			try {
				String message = "message: " + StringHelper.randomString(10) + " " + i;
//				System.out.println(message);
				client.send(message.getBytes("utf8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Sleep.seconds(4);
		//TODO: cleanup 
		client.close();
		server.close();
		Sleep.seconds(2);//make sure it gets closed

		System.out.println("Messages remaining: " + client.getSent().size());
		Assert.assertTrue(client.getSent().isEmpty());
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

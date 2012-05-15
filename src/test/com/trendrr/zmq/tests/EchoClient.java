/**
 * 
 */
package com.trendrr.zmq.tests;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.zmq.client.ZMQClient;
import com.trendrr.zmq.client.ZMQClientMessageHandler;


/**
 * Simple client that keeps track of what is sent, then removes when response comes.
 * 
 * 
 * @author Dustin Norlander
 * @created May 14, 2012
 * 
 */
public class EchoClient extends ZMQClient {

	Set<String> sent = Collections.synchronizedSet(new HashSet<String>());
	AtomicInteger received = new AtomicInteger(0);
	static ZMQClientMessageHandler handler = new ZMQClientMessageHandler() {

		@Override
		public void incoming(ZMQClient client, byte[] message) {
			try {
				((EchoClient)client).sent.remove(new String(message, "utf8"));
				((EchoClient)client).received.incrementAndGet();
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
	/**
	 * @param connection
	 * @param handler
	 */
	public EchoClient(String connection) {
		super(connection, handler);
	}

	protected static Log log = LogFactory.getLog(EchoClient.class);
	
	@Override
	public void send(byte[] message) {
		try {
			this.sent.add(new String(message,"utf8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.send(message);
	}
	
	/**
	 * gets the sent messages that have not received a response.  messages are removed once a response is sent
	 * @return
	 */
	public Set<String> getSent() {
		return this.sent;
	}
	
	public int getTotalReceived() {
		return this.received.get();
	}
	
}

/**
 * 
 */
package com.trendrr.zmq.server;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zeromq.ZMQ;


/**
 * @author Dustin Norlander
 * @created May 9, 2012
 * 
 */
public class ZMQServerOutgoing implements Runnable{

	protected static Log log = LogFactory.getLog(ZMQServerOutgoing.class);
	
	protected class ZMQOutMessage {
		public byte[] id;
		public byte[] message;
		public ZMQOutMessage(byte[] id, byte[] message) {
			this.id = id;
			this.message = message;
		}
	}
	
	ArrayBlockingQueue<ZMQOutMessage> messages = new ArrayBlockingQueue<ZMQOutMessage>(10);
	ZMQ.Context context;
	String connection;
	public ZMQServerOutgoing(ZMQ.Context context, String connection) {
		this.context = context;
		this.connection = connection;
	}
	
	public void start() {
		Thread t = new Thread(this);
		t.setDaemon(true);
		t.start();
	}
	
	public void run() {
		ZMQ.Socket socket = context.socket(ZMQ.DEALER);
		socket.connect(this.connection);
		while(true) {
			try {
				ZMQOutMessage message = messages.take();
				System.out.println("GOT FROM QUEUE: " + new String(message.message, "utf8"));
				socket.send(message.id, ZMQ.SNDMORE);
				socket.send(message.message, 0);//messages.isEmpty() ? 0 : ZMQ.SNDMORE);
			} catch (InterruptedException e) {
				log.error("Caught", e);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void send(byte[] id, byte[] message) {
		try {
			System.out.println("ADDING TO QUEUE: " + new String(message, "utf8"));
			this.messages.put(new ZMQOutMessage(id, message));
		} catch (InterruptedException e) {
			e.printStackTrace();
			log.error("caught", e);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

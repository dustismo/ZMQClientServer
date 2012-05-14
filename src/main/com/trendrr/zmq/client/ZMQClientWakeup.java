/**
 * 
 */
package com.trendrr.zmq.client;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zeromq.ZMQ;

import com.trendrr.oss.HashFunctions;


/**
 * @author Dustin Norlander
 * @created May 9, 2012
 * 
 */
public class ZMQClientWakeup implements Runnable{

	protected static Log log = LogFactory.getLog(ZMQClientWakeup.class);
	
	ArrayBlockingQueue<byte[]> messages = new ArrayBlockingQueue<byte[]>(10);
	ZMQ.Context context;
	String connection;
	public ZMQClientWakeup(ZMQ.Context context, String connection) {
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
		socket.setIdentity(HashFunctions.secureId());
		socket.connect(this.connection);
		while(true) {
			try {
				byte[] message = messages.take();
				socket.send(message, 0);
			} catch (InterruptedException e) {
				log.error("Caught", e);
			}
		}
	}
	
	public void send(byte[] message) {
		try {
			this.messages.put(message);
		} catch (InterruptedException e) {
			log.error("caught", e);
		}
	}
}

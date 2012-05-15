/**
 * 
 */
package com.trendrr.zmq.client;

import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zeromq.ZMQ;

import com.trendrr.oss.HashFunctions;
import com.trendrr.oss.concurrent.LazyInit;
import com.trendrr.zmq.server.ZMQServerOutgoing;


/**
 * @author Dustin Norlander
 * @created May 10, 2012
 * 
 */
public class ZMQClient {

	protected static Log log = LogFactory.getLog(ZMQClient.class);
	protected String connection;
	
	

	int pollerIndex; //used by the poller thread..
	ZMQ.Socket socket;
	ZMQClientMessageHandler handler;
	byte[] id;
	ArrayBlockingQueue<byte[]> outqueue = new ArrayBlockingQueue<byte[]>(25);
	LazyInit connectLock = new LazyInit();
	
	public ZMQClient(String connection, ZMQClientMessageHandler handler) {
		this.connection = connection;
		this.handler = handler;
		this.id = HashFunctions.secureId();
	}
	
	public String getConnection() {
		return connection;
	}
	
	public void send(byte[] message) {
		if (this.connectLock.start()) {
			try {
				this.connect();
			} finally {
				this.connectLock.end();
			}
		}
		
		try {
			this.outqueue.put(message);
			ZMQClientPoller.instance().wakeup();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * called lazily
	 */
	protected void connect() {
		ZMQClientPoller.instance().connect.add(this);
		ZMQClientPoller.instance().wakeup();
	}
	public void close() {
		System.out.println("Client CLOSE!");
		ZMQClientPoller.instance().disconnect.add(this);
		ZMQClientPoller.instance().wakeup();
	}

	void _connected() {
//		System.out.println("CONNECTED!");
		
	}
	
	void _closed() {
		socket = null;
		handler = null;
	}
}

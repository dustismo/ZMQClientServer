/**
 * 
 */
package com.trendrr.zmq.client;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zeromq.ZMQ;

import com.trendrr.oss.concurrent.LazyInitObject;
import com.trendrr.zmq.server.ZMQChannel;
import com.trendrr.zmq.server.ZMQServerOutgoing;


/**
 * @author Dustin Norlander
 * @created May 10, 2012
 * 
 */
public class ZMQClientPoller implements Runnable {

	protected static Log log = LogFactory.getLog(ZMQClientPoller.class);

	static LazyInitObject<ZMQClientPoller> instance = new LazyInitObject<ZMQClientPoller>() {

		@Override
		public ZMQClientPoller init() {
			ZMQClientPoller poller = new ZMQClientPoller();
			poller.init();
			Thread t = new Thread(poller);
			t.setDaemon(true);
			t.start();
			return poller;
		}
	};
	
	public static ZMQClientPoller instance() {
		return instance.get();
	}
	
	HashMap<Integer, ZMQClient> clients = new HashMap<Integer, ZMQClient>();
	//queue of clients waiting to connect.
	ArrayBlockingQueue<ZMQClient> connect = new ArrayBlockingQueue<ZMQClient>(10);
	//queue of clients waiting to disconnect.
	ArrayBlockingQueue<ZMQClient> disconnect = new ArrayBlockingQueue<ZMQClient>(10);
	ZMQClientWakeup outgoing;
	ZMQ.Context context;
	ZMQ.Socket backend;
	
	
	ZMQClientPoller() {
		this.context = ZMQ.context(1);
	}
	
	/**
	 * wakes up the poller, checks for disconnects, connects, outgoing messages, ect.
	 */
	public void wakeup() {
		outgoing.send(new byte[1]);
		
	}
	
	/**
	 * do the initialization outside of the main thread execution.
	 */
	void init() {
		backend = context.socket(ZMQ.DEALER);
		backend.bind("inproc://clientbackend");
		outgoing = new ZMQClientWakeup(context, "inproc://clientbackend");
		outgoing.start();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		
		ZMQ.Poller poller = context.poller();
		int alert = poller.register(backend, ZMQ.Poller.POLLIN);
		boolean more = false;
		while(true) {
			poller.poll();
			//process the wakeup alerts
			if (poller.pollin(alert)) {
				do {
					//ingest and discard the message.
					byte[] id = backend.recv(0);
					more = backend.hasReceiveMore();
				} while(more);
			}
			/*
			 * handle disconnections
			 */
			ZMQClient disconnection = disconnect.poll();
			while(disconnection != null) {
				//connect to remote.
				this.clients.remove(disconnection.pollerIndex);
				poller.unregister(disconnection.socket);
				disconnection.socket.setLinger(0l);
				disconnection.socket.close();
				disconnection._closed();
				disconnection = disconnect.poll();
			}
			/*
			 * handle new connections
			 */
			ZMQClient newConnection = connect.poll();
			while(newConnection != null) {
				//connect to remote.
				try {
					newConnection.socket = context.socket(ZMQ.DEALER);
					newConnection.socket.setIdentity(newConnection.id);
					System.out.println("CONNECTING: " + newConnection.getConnection());
					newConnection.socket.connect(newConnection.getConnection());
					newConnection.pollerIndex = poller.register(newConnection.socket, ZMQ.Poller.POLLIN);
					this.clients.put(newConnection.pollerIndex, newConnection);
					newConnection._connected();
					newConnection = connect.poll();
				} catch (Exception x) {
					log.error("Unable to connect!", x);
					log.error(newConnection.getConnection());
					newConnection.close();
				}
			}

			
			for (Integer index : this.clients.keySet()) {
				ZMQClient c = this.clients.get(index);
				//now handle any real incoming messages
				if (poller.pollin(index)) {
					//there is message!
					do {
						byte[] message = c.socket.recv(0);
						more = c.socket.hasReceiveMore();
						c.handler.incoming(c, message);
					} while(more);
				}
				
				//check for error?
				if (poller.pollerr(index)) {
					//TODO
				}
				
				//check for outgoing..
				while (!c.outqueue.isEmpty()) {
					byte[] message = c.outqueue.poll();
					c.socket.send(message, 0);//c.outqueue.isEmpty() ? 0 : ZMQ.SNDMORE);		
				}
			}
		}

	}
	

}

/**
 * 
 */
package com.trendrr.zmq.server;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zeromq.ZMQ;



/**
 * @author Dustin Norlander
 * @created May 9, 2012
 * 
 */
public class ZMQServer implements Runnable{

	protected static Log log = LogFactory.getLog(ZMQServer.class);
	private int port = 8653;
	private ZMQServerMessageHandler handler;
	private AtomicBoolean stopped = new AtomicBoolean(true);
	private ZMQ.Context context;
	private long pollingTimeout = 1000*1000;
	
	/**
	 * starts the listener.
	 * @param threaded should this be started in a new thread? if true, will return immediately, if false will never return.
	 * 
	 */
	public void listen(int port, ZMQServerMessageHandler handler, boolean threaded) {
		this.port = port;
		this.handler = handler;
		if (threaded) {
			Thread t = new Thread(this);
			t.start();
		} else {
			this.run();
		}
	}
	
	
	
	public void run() {
		context = ZMQ.context(1);
		ZMQ.Socket frontend = context.socket(ZMQ.ROUTER);
		frontend.bind ("tcp://*:" + port);
		
		ZMQ.Socket backend = context.socket(ZMQ.DEALER);
		backend.bind("inproc://serverbackend");
		
		//set up the outgoing
		ZMQServerOutgoing outgoing = new ZMQServerOutgoing(context, "inproc://serverbackend");
		outgoing.start();
		
		this.stopped.set(false);
		
		
		ZMQ.Poller poller = context.poller(2);
		int frontIndex = poller.register(frontend, ZMQ.Poller.POLLIN);
		int backIndex = poller.register(backend, ZMQ.Poller.POLLIN);
		
		boolean more = false;
		System.out.println("Server Listening on : " + port);
		while(true) {
			poller.poll(this.pollingTimeout);
			if (this.stopped.get()) {
				poller.unregister(frontend);
				poller.unregister(backend);
				frontend.setLinger(0l);
				frontend.close();
				backend.setLinger(0l);
				backend.close();
				context.term();
				return;
			}
			
			if (poller.pollin(frontIndex)) {
				//incoming messages.
				do {
					byte[] id = frontend.recv(0);
					byte[] message = frontend.recv(0);
					more = frontend.hasReceiveMore();
					this.handleIncoming(new ZMQChannel(id, outgoing), message);
				} while(more);
			}
			
			if (poller.pollin(backIndex)) {
				//theres a message needs to be written.
				do {
					byte[] id = backend.recv(0);
					byte[] message = backend.recv(0);

	                more = backend.hasReceiveMore();
	                // Broker it
	                frontend.send(id, ZMQ.SNDMORE);
	                frontend.send(message,  0);// more ? ZMQ.SNDMORE : 0);
				} while(more);
			}
		}
	}
	
	/**
	 * closes and cleans up this server.
	 * 
	 * Currently this is an asynch call, it returns immediately, but it may take a 
	 * second or three to actually clean up.  
	 * 
	 * TODO: block until the operation completes.
	 */
	public void close() {
		this.stopped.set(true);
	}
	
	/**
	 * 
	 */
	protected void handleIncoming(ZMQChannel channel, byte[] message) {
		this.handler.incoming(channel, message);
	}
}

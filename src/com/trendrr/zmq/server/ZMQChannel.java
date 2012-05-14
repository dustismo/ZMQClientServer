/**
 * 
 */
package com.trendrr.zmq.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.zmq.ZMQOutgoing;


/**
 * @author Dustin Norlander
 * @created May 10, 2012
 * 
 */
public class ZMQChannel {

	protected static Log log = LogFactory.getLog(ZMQChannel.class);
	
	byte[] id;
	
	ZMQOutgoing outgoing;
	
	public ZMQChannel(byte[] id, ZMQOutgoing out) {
		this.id = id;
		this.outgoing = out;
	}
	
	public byte[] getId() {
		return id;
	}
	public void send(byte[] message) {
		this.outgoing.send(id, message);
	}
}

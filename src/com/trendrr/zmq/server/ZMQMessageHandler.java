/**
 * 
 */
package com.trendrr.zmq.server;



/**
 * @author Dustin Norlander
 * @created May 10, 2012
 * 
 */
public interface ZMQMessageHandler {

	public void incoming(ZMQChannel channel, byte[] message);
	public void error(Exception x);
}

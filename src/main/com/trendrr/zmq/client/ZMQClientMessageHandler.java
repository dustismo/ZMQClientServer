/**
 * 
 */
package com.trendrr.zmq.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.zmq.server.ZMQChannel;


/**
 * @author Dustin Norlander
 * @created May 10, 2012
 * 
 */
public interface ZMQClientMessageHandler {
	public void incoming(ZMQClient client, byte[] message);
	public void error(Exception x);
}

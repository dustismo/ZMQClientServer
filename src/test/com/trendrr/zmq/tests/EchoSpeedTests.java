/**
 * 
 */
package com.trendrr.zmq.tests;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.trendrr.oss.StringHelper;
import com.trendrr.oss.concurrent.Sleep;


/**
 * 
 * maybe not that usefull, but nice to see what the throughput is.
 * @author Dustin Norlander
 * @created May 15, 2012
 * 
 */
public class EchoSpeedTests extends EchoTests{

	protected static Log log = LogFactory.getLog(EchoSpeedTests.class);


	@Test
	public void singleClientSpeedTest() {
		//simple, counts echo receives
		
		this.startEchoServer();
		EchoClient client = new EchoClient("tcp://localhost:8988");
		Date start = new Date();
		int numRequests = 1000000;
		for (int i=0; i < numRequests; i++) {
			try {
				String message = "message: " + StringHelper.randomString(10) + " " + i;
//				System.out.println(message);
				client.send(message.getBytes("utf8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//busy wait..  ehh, bad form..
		while(client.getSent().size() > 0) {
			Sleep.millis(2);
		}
		
		System.out.println("COMPLETED: " + numRequests + " in " + (new Date().getTime()-start.getTime()) + " millis");
		
		client.close();
		server.close();
		Sleep.seconds(2);//make sure it gets closed

		System.out.println("Messages remaining: " + client.getSent().size());
		Assert.assertTrue(client.getSent().isEmpty());
	}



}

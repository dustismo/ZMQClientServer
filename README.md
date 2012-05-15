This is a wrapper around the hot-mess that is zeromq async sockets.  This provides a threadsafe and latency free way to 
handle message passing in zeromq.

Usage:

```java
/*
 * create a client
 * 
 * you only need a client and a class to handle incoming messages
 */
 ZMQClientMessageHandler handler = new ZMQClientMessageHandler() {

		@Override
		public void incoming(ZMQClient client, byte[] message) {
			try {
				System.out.println(new String(message, "utf8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void error(Exception x) {
			x.printStackTrace();
		}
	};
 
 
 ZMQClient client = new ZMQClient("tcp://localhost:8988", handler);
 client.send("this is a message".getBytes());

```

Server

```java
 /*
  * this creates a simple echo server
  */
	ZMQServerMessageHandler handler = new ZMQServerMessageHandler() {
			
			@Override
			public void incoming(ZMQChannel channel, byte[] message) {
				channel.send(message);
			}
			
			@Override
			public void error(Exception x) {
				x.printStackTrace();
			}
		};
		ZMQServer server = new ZMQServer();
		server.listen(8988, handler, true);

```



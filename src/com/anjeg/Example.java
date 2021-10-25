package com.anjeg;

import com.anjeg.socket.Client;
import com.anjeg.socket.Request;

public class Example {

	/* ************************************************************************
	 * Constants
	 * ***********************************************************************/

	public static final String HOST = "127.0.0.1";
	public static final int PORT = 8000;
	
	/* ************************************************************************
	 * Attributes
	 * ***********************************************************************/

	/* ************************************************************************
	 * Constructor
	 * ***********************************************************************/

	/* ************************************************************************
	 * Methods
	 * ***********************************************************************/

	public static void main(String[] args) {
		
		Client client = new Client();
		client.connect(Example.HOST, Example.PORT);
		client.startThreads();
		
		client.send(Request.build("GET", "/shop", null),
				(response) -> {
					System.out.println(response.getData());
					client.interruptThreads();
				});
		
		while(client.areThreadsAlive());
		System.out.println("end");
		client.disconnect();
	}
	
	/* ************************************************************************
	 * Overrides
	 * ***********************************************************************/

}

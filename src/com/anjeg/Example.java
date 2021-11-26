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
		/*
		 * Test client only 
		 *
		Client client = new Client();
		client.connect(HOST, PORT);
		client.write(Request.build("GET", "/shop", null));
		System.out.println(client.read().getData());
		client.disconnect();*/
		
		Client client = new Client(HOST, PORT);
		client.connect();
		client.startThread();
		client.send(Request.build("GET", "/shop", null), (response) -> {
			System.out.println(response.getData());
			client.interruptThread();
		});
		
		/*
		 * Test when 2 threads
		 *
		Client client = new Client();
		client.connect(Example.HOST, Example.PORT);
		client.startThreads();
		
		client.send(Request.build("GET", "/shop", null),
				(response) -> {
					System.out.println(response.getData());
					client.interruptThreads();
				});
		
		while(client.areThreadsAlive()) {
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("end");
		client.disconnect();*/
	}
	
	/* ************************************************************************
	 * Overrides
	 * ***********************************************************************/

}

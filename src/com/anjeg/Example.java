package com.anjeg;

import com.anjeg.controller.socket.Client;
import com.anjeg.controller.socket.Request;

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
		client.write(Request.build("GET", "/shop", null));
		System.out.println(client.read().getData());
		client.disconnect();
		
	}
	
	/* ************************************************************************
	 * Overrides
	 * ***********************************************************************/

}

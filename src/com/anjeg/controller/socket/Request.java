package com.anjeg.controller.socket;

import java.util.Random;

import org.json.simple.JSONObject;

/**
 * 
 * @author quentin
 *
 * This class has been made to encapsulate request from the client,
 * it's main purpose is to build the request header, and join the
 * optional data.
 */
public class Request {

	/* ************************************************************************
	 * Constants
	 * ***********************************************************************/

	/* ************************************************************************
	 * Attributes
	 * ***********************************************************************/

	// To generate request id while building request
	private static Random randomizer = new Random();

	private String command;
	private String url;
	private String request_id;
	private String client_id;
	private boolean client_id_needed;
	private String data;
	
	/* ************************************************************************
	 * Constructor
	 * ***********************************************************************/


	/**
	 * Save the arguments of the header for a future build. The full request can
	 * be created with Client::toString.
	 * 
	 * @param command The header command
	 * @param url The command url, it always start with a slash, like http url.
	 * @param request_id The request ID, it is use to identify the future response which will have the same ID
	 * @param data The data to send with the header, it can be null
	 * @param client_id_needed Tell if the client id is either mandatory or not
	 */
	public Request(String command, String url, String request_id, String data, boolean client_id_needed) {
		super();
		
		this.command = command;
		this.url = url;
		this.request_id = request_id;
		this.client_id_needed = client_id_needed;
		this.client_id = ".";
		this.data = data;
	}
	
	/**
	 * Save the arguments of the header for a future build. The full request can
	 * be created with Client::toString.
	 * When using this constructor, the client ID is mandatory by default.
	 * 
	 * @param command The header command (e.g. LOG, GET, POST)
	 * @param url The command url, it always start with a slash, like http url.
	 * @param request_id The request ID, it is use to identify the future response which will have the same ID
	 * @param data The data to send with the header, it can be null
	 */
	public Request(String command, String url, String request_id, String data) {
		this(command, url, request_id, data, true);
	}
	
	
	/* ************************************************************************
	 * Methods
	 * ***********************************************************************/

	/**
	 * Get the request ID
	 * 
	 * @return The request ID
	 */
	public String getRequestId() {
		return this.request_id;
	}
	
	/**
	 * Set the client ID
	 * 
	 * @param client_id The client ID
	 * @return The current request, to make it easier to use
	 */
	public Request withId(String client_id) {
		if(this.client_id_needed)
			this.client_id = client_id;
		return this;
	}
	
	/**
	 * Build a request, it is not mandatory to use this method but it generate
	 * random request ID, to make request easier to build.
	 * 
	 * @param command The request command
	 * @param url The command url
	 * @param json The request data
	 * @return A request object with a random ID
	 */
	public static Request build(String command, String url, JSONObject json) {

		// Generate request id from alphanumeric value
		String request_id = randomizer.ints(48, 123)
			.filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
			.limit(8)
			.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
			.toString();

		// Data
		String data = "";
		if(json != null) {
			data = json.toJSONString();
		}
		
		return new Request(command, url, request_id, data);
	}
	
	/* ************************************************************************
	 * Overrides
	 * ***********************************************************************/

	@Override
	public String toString() {

		// Header
		String header = this.command
				+ " " + this.url
				+ " " + Client.VERSION
				+ " " + this.request_id
				+ " " + this.client_id;
		
		if(this.data.length() > 0)
			header += " " + this.data.length();
		
		return header + "\n" + this.data;
	}
}

package com.anjeg.socket;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * 
 * @author Quiscale
 *
 * The client class is able to connect itself through a socket, send
 * requests and recieve response.
 *
 */
public class Client {

	/* ************************************************************************
	 * Constants
	 * ***********************************************************************/

	/* ************************************************************************
	 * Attributes
	 * ***********************************************************************/

	public static final String VERSION = "Ozad/0.0.42.2021";
	
	private Socket socket;
	private InputStream input;
	private PrintWriter output;
	
	/* ************************************************************************
	 * Constructor
	 * ***********************************************************************/

	/**
	 * Create a new client.
	 * To start a connection with a server, use Client::connect.
	 */
	public Client() {
		super();
		
		this.socket = null;
		this.input = null;
		this.output = null;
	}
	
	/* ************************************************************************
	 * Methods
	 * ***********************************************************************/

	/**
	 * Connect the client to a server using socket.
	 * 
	 * @param host IP address "x.x.x.x"
	 * @param port port address
	 */
	public void connect(String host, int port) {

		try {
			this.socket = new Socket(host, port);
			System.out.println("[socket] connected");
			this.input = this.socket.getInputStream();
			this.output = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()));
			System.out.println("[socket] reader & writer ready");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("[socket] error while connecting");
		}
	}
	
	/**
	 * Close all the stream (input/output) and the socket.
	 * Client::read & Client::write are not usable anymore after the
	 * use of this method.
	 */
	public void disconnect() {
		try {
			this.socket.close();
			this.input.close();
			this.output.close();
			System.out.println("[socket] disconnected");
		}
		catch (IOException e) {
			e.printStackTrace();
			System.err.println("[socket] error while disconnecting");
		}
	}
	
	/**
	 * Send a request to the server
	 * 
	 * @param request The request to send encapsulated in a Request object
	 */
	public void write(Request request) {
		output.print(request);
		output.flush();
		System.out.println("[client] request sent");
	}
	
	/**
	 * Read response from a server, this method can block itself if the
	 * server sent nothing.
	 * 
	 * @return a Response object which encapsulate the server's response
	 */
	public Response read() {
		try {
			// Read header
			DataInputStream dis = new DataInputStream(this.input);
			byte[] in = new byte[2048];
			int i = 0;
			byte b = 0;
			while(b != '\n') {
				b = dis.readByte();
				in[i++] = b;
			}
			// Parse header
			String header = new String(in, StandardCharsets.UTF_8);
			String[] args = header.trim().split(" ");
			Response response = new Response(
					Integer.valueOf(args[0]).intValue(), // Status code
					// args[1], // Version
					args[2] // Response id
				);
			String type = args[3];
			int dataLength = Integer.valueOf(args[4]).intValue();
			// Read data
			if(type.matches("(JSON)|(TEXT)")) {
				dis.read(in, 0, dataLength);
				in[dataLength] = 0;
				String data = (new String(in, StandardCharsets.UTF_8)).substring(0, dataLength);
				
				if(type.matches("JSON")) {
					JSONParser parser = new JSONParser();
					response.setData((JSONObject) parser.parse(data));
				}
				else {
					response.setData(data);
				}
			}
			else if(type.matches("IMAGE")) {
				System.out.println("[client] image received");
			}
			else {
				System.err.println("[client] WTF is that data type ! '" + type + "' is unknown");
			}
			System.out.println("[client] response recieved");
			return response;
			
		}
		catch (IOException e) {
			e.printStackTrace();
			System.err.println("[socket] error while reading");
		} 
		catch (ParseException e) {
			e.printStackTrace();
			System.err.println(e);
		}
		
		return null;
	}
 	
	/* ************************************************************************
	 * Overrides
	 * ***********************************************************************/

}

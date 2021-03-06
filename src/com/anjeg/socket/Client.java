package com.anjeg.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.anjeg.socket.data_type.JsonData;
import com.anjeg.socket.data_type.TextData;

/**
 * 
 * @author Quiscale
 *
 * The client class is able to connect itself through a socket, send
 * requests and receive response.
 *
 */
public class Client {

	/* ************************************************************************
	 * Constants
	 * ***********************************************************************/

	/* ************************************************************************
	 * Attributes
	 * ***********************************************************************/
	
	// Server ip & port
	private String host;
	private int port;
	
	// Web socket & streams
	private Socket socket;
	private InputStream input;
	private OutputStream output;
	
	// Thread's usefull attributes
	private Thread thread;
	private ConcurrentLinkedQueue<Request<?>> requests;
	private Map<String, ClientListener> listeners; // <requestId, listener>
	private List<ClientListener> connectListeners;
	
	/* ************************************************************************
	 * Constructor
	 * ***********************************************************************/

	/**
	 * Create a new client.
	 * To start a connection with a server, use Client::connect.
	 * 
	 * @param host IP address "x.x.x.x"
	 * @param port port address
	 */
	public Client(String host, int port) {
		super();
		
		this.host = host;
		this.port = port;
		
		this.socket = null;
		this.input = null;
		this.output = null;
	}
	
	/* ************************************************************************
	 * Methods
	 * ***********************************************************************/

	/**
	 * Connect the client to a server using socket.
	 */
	public void connect() {

		try {
			this.socket = new Socket(this.host, this.port);
			System.out.println("[client] connected");
			this.input = this.socket.getInputStream();
			this.output = this.socket.getOutputStream();
			System.out.println("[client] reader & writer ready");
		}
		catch (IOException e) {
			//System.err.println("[client] error while connecting");
		}
	}
	
	/**
	 * Close all the stream (input/output) and the socket.
	 * Client::read & Client::write are not usable anymore after the
	 * use of this method.
	 */
	public void disconnect() {
		
		if(this.socket != null) {
			try {
				this.socket.close();
				this.input.close();
				this.output.close();
				System.out.println("[client] disconnected");
			}
			catch (IOException e) {
				System.err.println("[client] error while disconnecting");
			}
		}
	}
	
	/**
	 * Send a request to the server
	 * 
	 * @param request The request to send encapsulated in a Request object
	 */
	public void write(Request<?> request) {
		try {
			output.write(request.toBytes());
			output.flush();
			System.out.println("[client] request sent");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Read response from a server, this method can block itself if the
	 * server sent nothing.
	 * 
	 * @return a Response object which encapsulate the server's response
	 */
	public Response<?> read() {
		
		try {
			if(this.input.available() > 0) {
				// Read header
				byte[] in = new byte[2048];
				int i = 0;
				byte b = 0;
				while(b != '\n') {
					b = (byte) this.input.read();
					in[i++] = b;
				}
				// Parse header
				String header = new String(in, StandardCharsets.UTF_8);
				String[] args = header.trim().split(" ");
				// split header into variables
				int 	status = Integer.valueOf(args[0]).intValue();
				String 	id = args[1];
				String 	dataType = args[2];
				int 	dataLength = Integer.valueOf(args[3]).intValue();
				// Prepare response
				Response<?> response = null;
				// Read data
				if(dataType.matches("(JSON)|(TEXT)")) {
					// Read text
					this.input.read(in, 0, dataLength);
					in[dataLength] = 0;
					String data = (new String(in, StandardCharsets.UTF_8)).substring(0, dataLength);
					
					if(dataType.matches("JSON")) {
						JSONParser parser = new JSONParser();
						response = new Response<JsonData>(status, id);
						((Response<JsonData>) response).setData(
								new JsonData((JSONObject) parser.parse(data)));
					}
					else {
						response = new Response<TextData>(status, id);
						((Response<TextData>) response).setData(new TextData(data));
					}
				}
				else if(dataType.matches("IMAGE")) {
					// TODO implements image parsing
					System.out.println("[client] image received");
				}
				else {
					System.err.println("[client] WTF is that data type ! '" + dataType + "' is unknown");
				}
				System.out.println("[client] response recieved");
				return response;
			}
			else {
				//System.err.println("[client] tried to read but nothing is available");
				return null;
			}
		} catch (NumberFormatException | IOException | ParseException e) {
			System.err.println("[client] error while reading");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Start the thread associated to the client, also create a queue to handle request and a map to handle listeners
	 * If the thread can't write or read the client, it will try to connect to the server, the use of Client::connect is not mandatory
	 */
	public void startThread() {
		this.requests = new ConcurrentLinkedQueue<>();
		this.listeners = new HashMap<>();
		this.connectListeners = new ArrayList<>();
		this.thread = new Thread(this::run, "client");
		this.thread.start();
	}
	
	/**
	 * Interrupt the client's thread
	 * note: this method does not disconnect the client and the thread will not do it
	 */
	public void interruptThread() {
		if(this.thread != null)
			this.thread.interrupt();
	}
	
	/**
	 * Client's thread, it handles the requests queue and the client listeners
	 * If the queue is not empty, it will send all the first request in it.
	 * Then it tries to read incoming response, if it receives one, it is sent to the listener.
	 * And it goes back to the queue.
	 */
	private void run() {
		
		boolean run = true;
		while(run) {
			
			if(this.socket != null) {
			
				// Send part
				if(!this.requests.isEmpty()) {
					this.write(this.requests.poll());
				}
				
				// Read
				Response<?> rep = this.read();
				// Give the response to the listener if it is registered
				if(rep != null) {
					if(this.listeners.containsKey(rep.getId())) {
						this.listeners.remove(rep.getId()).handleResponse(rep);
					}
					else {
						System.err.println("[t_client] response lost");
						System.err.println(rep.getData());
					}
				}
			}
			else {
				// Connect if needed
				this.connect();
				if(this.socket != null) {
					this.connectListeners.forEach((listener) -> {
						listener.handleResponse(null);
					});
				}
			}
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				run = false;
			}
		}
	}
	
	/**
	 * Ask the thread to send a request, the request is sent to the back of the queue.
	 * 
	 * @param request Request to send
	 * @param listener The listener will be call back when a response is received
	 */
	public void send(Request<?> request, ClientListener listener) {
		
		this.requests.add(request);
		this.listeners.put(request.getRequestId(), listener);
	}
	
	/**
	 * Register a client listener which will handle a connection event when the socket is connected
	 * 
	 * @param listener 
	 */
	public void addOnConnect(ClientListener listener) {
		this.connectListeners.add(listener);
	}

	/**
	 * Ask the client if it is connected or not
	 * 
	 * @return True if connected
	 */
	public boolean isConnected() {
		return this.socket != null;
	}

	/* ************************************************************************
	 * Overrides
	 * ***********************************************************************/

}

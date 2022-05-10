package net.anjeg.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.anjeg.socket.data.JsonData;
import net.anjeg.socket.data.TextData;

public class Client {

	// /////////////////////////////////////////////////////////////////////////////
	// Attributes
	// /////////////////////////////////////////////////////////////////////////////

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
	private Map<String, ResponseListener> responseListeners; // <requestId, listener>
	private List<StatusListener> statusListeners;
	private int lastStatus;
	
	// /////////////////////////////////////////////////////////////////////////////
	// Constructors
	// /////////////////////////////////////////////////////////////////////////////

	/**
	 * Create a new websocket thread
	 * 
	 * @param host Host IP address "x.x.x.x"
	 * @param port Host port address
	 */
	public Client(String host, int port) {
		super();
		
		this.host = host;
		this.port = port;
		
		this.socket = null;
		this.input = null;
		this.output = null;
		
		this.requests = new ConcurrentLinkedQueue<>();
		this.responseListeners = new HashMap<>();
		this.statusListeners = new ArrayList<>();
		this.lastStatus = StatusListener.STARTING;
		this.thread = new Thread(this::run, "client");
		this.thread.start();
	}
	
	// /////////////////////////////////////////////////////////////////////////////
	// Methods
	// /////////////////////////////////////////////////////////////////////////////

	/**
	 * Main thread method
	 */
	private void run() {
		
		boolean run = true;
		
		while(run) {
		
			switch(this.lastStatus) {
			
			case StatusListener.STARTING:
				this.connect();
				break;
				
			case StatusListener.CONNECTED:
				
				this.write();
				
				if(this.lastStatus != StatusListener.ERROR)
					this.read();
				
				break;
				
			case StatusListener.DISCONNECTED:

				this.disconnect();
				run = false;
				break;
				
			case StatusListener.ERROR:
				this.disconnect();
				this.connect();
				break;
			
			default:
				System.out.println("Client status, default : " + this.lastStatus);
				this.setStatus(StatusListener.ERROR);
			
			}
			
		}
		
		// this.disconnect();
		
	}
	
	/**
	 * Thread method for connecting
	 */
	private void connect() {
		
		try {
			
			this.socket = new Socket(this.host, this.port);
			System.out.println("[client] connected");
			this.input = this.socket.getInputStream();
			this.output = this.socket.getOutputStream();
			System.out.println("[client] reader & writer ready");
			this.setStatus(StatusListener.CONNECTED);
		}
		catch (UnknownHostException e) {
			this.setStatus(StatusListener.ERROR);
			//e.printStackTrace();
		}
		catch (IOException e) {
			this.setStatus(StatusListener.ERROR);
			//e.printStackTrace();
		}

	}
	
	/**
	 * Thread method for disconnecting
	 */
	private void disconnect() {
		
		if(this.socket != null) {
			try {
				this.socket.close();
				this.input.close();
				this.output.close();
				System.out.println("[client] disconnected");
				this.setStatus(StatusListener.DISCONNECTED);
			}
			catch (IOException e) {
				this.setStatus(StatusListener.ERROR);
				System.err.println("[client] error while disconnecting");
			}
		}
	}
	
	/**
	 * Thread method for receiving data
	 */
	private void read() {

		Response<?> response = null;
		
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
			}
		} catch (NumberFormatException | IOException | ParseException e) {
			System.err.println("[client] error while reading");
			e.printStackTrace();
		}
		
		if(response != null) {
			if(this.responseListeners.containsKey(response.getId())) {
				this.responseListeners.remove(response.getId()).handle(response);
			}
			else {
				System.err.println("[client] response lost");
				System.err.println(response.getData());
			}
		}
		
	}
	
	/**
	 * Thread method for sending data
	 */
	private void write() {
		
		if(!this.requests.isEmpty()) {
			try {
				Request<?> request = this.requests.poll();
				output.write(request.toBytes());
				output.flush();
				System.out.println("[client] request sent");
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	// ////////////////////////////////////////////////////
	// Status methods
	
	private void setStatus(int status) {

		if(this.lastStatus != status && this.lastStatus != StatusListener.DISCONNECTED) {
			
			this.lastStatus = status;
			for(StatusListener listener : this.statusListeners) {
				listener.handle(status);
			}
		}
	}
	
	public void addStatusListener(StatusListener listener) {
		
		this.statusListeners.add(listener);
		listener.handle(this.lastStatus);
		
	}
	
	// ////////////////////////////////////////////////////
	// Request / Response methods
	
	/**
	 * Ask the client thread to send a request (the request is sent to the back of the queue).
	 * 
	 * @param request The request to send
	 * @param listener The listener will be called back when a response is received
	 */
	public void send(Request<?> request, ResponseListener listener) {

		this.requests.add(request);
		this.responseListeners.put(request.getRequestId(), listener);
		
	}
	
	// ////////////////////////////////////////////////////
	// Utils
	
	public void interrupt() {
		this.setStatus(StatusListener.DISCONNECTED);
	}
	
	// /////////////////////////////////////////////////////////////////////////////
	// Override
	// /////////////////////////////////////////////////////////////////////////////

}

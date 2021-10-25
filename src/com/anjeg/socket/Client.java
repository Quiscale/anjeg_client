package com.anjeg.socket;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

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
	
	private ThreadGroup threads;
	private LinkedBlockingQueue<Request> requestQueue;
	private Map<String, ClientListener> listeners;
	
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
			System.out.println("[client] connected");
			this.input = this.socket.getInputStream();
			this.output = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()));
			System.out.println("[client] reader & writer ready");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("[client] error while connecting");
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
			System.out.println("[client] disconnected");
		}
		catch (IOException e) {
			e.printStackTrace();
			System.err.println("[client] error while disconnecting");
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
			System.err.println("[client] error while reading");
		} 
		catch (ParseException e) {
			e.printStackTrace();
			System.err.println(e);
		}
		
		return null;
	}
	
	/**
	 * Start two thread to handle request & response.
	 * One will be in charge of sending packages.
	 * The other will handle the response from the server and give the response to a registered client listener
	 */
	public void startThreads() {
		
		this.requestQueue = new LinkedBlockingQueue<>();
		this.listeners = new HashMap<>();
		
		this.threads = new ThreadGroup("client");
		this.threads.setDaemon(true);
		Thread tSend = new Thread(this.threads, this::runSend, "send");
		Thread tRecieve = new Thread(this.threads, this::runRecieve, "recieve");
		
		tSend.start();
		tRecieve.start();
	}
	
	/**
	 * Check if one of the thread is alive
	 * 
	 * @return either the group is destroyed or not
	 */
	public boolean areThreadsAlive() {
		return !this.threads.isDestroyed();
	}
	
	/**
	 * Interrupt and notify all the running threads
	 */
	public void interruptThreads() {
		this.threads.interrupt();
		this.threads.notifyAll();
	}
	
	/**
	 * Offer a new request to the request blocking queue,
	 * Map the listener with the request id.
	 * 
	 * @param request The request to send to the server
	 * @param listener The listener which will handle the response
	 * @return either the request is in the queue or not
	 */
	public boolean send(Request request, ClientListener listener) {
		boolean offered = this.requestQueue.offer(request);
		if(offered)
			this.listeners.put(request.getRequestId(), listener);
		return offered;
	}
	
	/**
	 * Sending thread,
	 * it get the request to send from the blocking queue,
	 * the thread stops when it is interrupted.
	 */
	private void runSend() {
		System.out.println("[client-send] start");
		
		boolean run = true;
		while(run) {
			
			try {
				Request request = this.requestQueue.take();
				this.write(request);
			}
			catch (InterruptedException e) {
				run = false;
			}
			
		}
		
		System.out.println("[client-send] end");
	}
	
	/**
	 * Recieving thread,
	 * it recieve responses from the server and give the to listeners,
	 * the threads stops when it is notified.
	 */
	private void runRecieve() {
		System.out.println("[client-recv] start");

		boolean run = true;
		while(run) {
			try {
				Response response = this.read();
				
				if(this.listeners.containsKey(response.getId())) {
					this.listeners.remove(response.getId()).handleResponse(response);
				}
				else {
					System.err.println("[client-recv] response can't be handled");
				}
			}
			catch(IllegalMonitorStateException e) {
				run = false;
			}
		}

		System.out.println("[client-recv] end");
	}

	/* ************************************************************************
	 * Overrides
	 * ***********************************************************************/

}

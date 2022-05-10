package net.anjeg.socket;

public interface StatusListener {

	public static int DISCONNECTED = -2;
	public static int ERROR = -1;
	public static int STARTING = 1;
	public static int CONNECTED = 2;
	
	// /////////////////////////////////////////////////////////////////////////////
	// Methods
	// /////////////////////////////////////////////////////////////////////////////

	/**
	 * Handle status sent by the Client thread
	 * 
	 * @param status A StatusListener code
	 */
	public void handle(int status);
	
}

package net.anjeg.socket;

public interface ResponseListener {

	// /////////////////////////////////////////////////////////////////////////////
	// Methods
	// /////////////////////////////////////////////////////////////////////////////

	/**
	 * Handle response sent by the Client thread
	 * 
	 * @param response A response from the server
	 */
	public void handle(Response<?> response);
	
}

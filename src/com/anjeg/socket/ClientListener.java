package com.anjeg.socket;

public interface ClientListener {

	/* ************************************************************************
	 * Methods
	 * ***********************************************************************/

	/**
	 * This method is made to handle the response from the client
	 * for a given request
	 * 
	 * @param response Response sent by the server
	 */
	public void handleResponse(Response<?> response);
	
}

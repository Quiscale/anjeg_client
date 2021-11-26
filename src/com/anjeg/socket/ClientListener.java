package com.anjeg.socket;

public interface ClientListener {

	/* ************************************************************************
	 * Methods
	 * ***********************************************************************/

	/**
	 * This method is made to 
	 * @param response
	 */
	public void handleResponse(Response<?> response);
	
}

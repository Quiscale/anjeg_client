package net.anjeg.socket;


/**
 * 
 * @author quentin
 *
 * This class is made to encapsulate responses, and make it easier to read ID or data.
 *
 */
public class Response<T extends Data<?>> {

	/* ************************************************************************
	 * Constants
	 * ***********************************************************************/

	/* ************************************************************************
	 * Attributes
	 * ***********************************************************************/

	private int code;
	private String response_id;
	private T data;
	
	/* ************************************************************************
	 * Constructor
	 * ***********************************************************************/

	/**
	 * Create a new response object from a code status and the response ID.
	 * The data is set to null in case there is nothing, you can set it with Response::setData
	 * 
	 * @param code HTTP Code Status
	 * @param response_id Response ID, it should be equal to a previously sent request ID
	 */
	public Response(int code, String response_id) {
		super();
		
		this.code = code;
		this.response_id = response_id;
		this.data = null;
	}
	
	/* ************************************************************************
	 * Methods
	 * ***********************************************************************/

	/**
	 * Get the code status
	 * 
	 * @return HTTP code status
	 */
	public int getCode() {
		return this.code;
	}
	
	/**
	 * Get the response ID
	 * @return Response ID
	 */
	public String getId() {
		return this.response_id;
	}
	
	/**
	 * Get the data linked with the response, you should check the object
	 * type before using it.
	 * 
	 * @return The data object
	 */
	public Object getData() {
		return this.data.getData();
	}
	
	/**
	 * Set the data which was with the response header
	 * 
	 * @param data An object which represents the data
	 */
	public void setData(T data) {
		this.data = data;
	}
	
	/* ************************************************************************
	 * Overrides
	 * ***********************************************************************/
	
}
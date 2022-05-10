package net.anjeg.socket;

public interface Data<T> {

	/**
	 * Get the name of the type
	 * 
	 * @return The name of the type
	 */
	public String getName();
	
	/**
	 * Get the byte array of the data
	 * 
	 * @return A byte array containing the data
	 */
	public byte[] toBytes();
	
	/**
	 * Get the data stored
	 * 
	 * @return Raw data
	 */
	public T getData();
	
}

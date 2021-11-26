package com.anjeg.socket.data_type;

import org.json.simple.JSONObject;

import com.anjeg.socket.Data;

public class JsonData implements Data<JSONObject> {

	/* ************************************************************************
	 * Constants
	 * ***********************************************************************/

	/* ************************************************************************
	 * Attributes
	 * ***********************************************************************/

	private JSONObject json;
	
	/* ************************************************************************
	 * Constructor
	 * ***********************************************************************/

	public JsonData(JSONObject json) {
		super();
		
		this.json = json;
	}
	
	/* ************************************************************************
	 * Methods
	 * ***********************************************************************/

	/* ************************************************************************
	 * Overrides
	 * ***********************************************************************/

	@Override
	public String getName() {
		
		return "JSON";
	}

	@Override
	public byte[] toBytes() {
		
		return this.json.toJSONString().getBytes();
	}

	@Override
	public JSONObject getData() {

		return this.json;
	}

}

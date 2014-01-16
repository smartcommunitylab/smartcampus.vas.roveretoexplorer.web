package eu.trentorise.smartcampus.dt.model;

import java.util.Map;


public class ServiceDataObject  {

	protected String id;
	
	protected Map<String, Object> data;

	public ServiceDataObject() {
	}
	
	public ServiceDataObject(String id) {
		this.id = id;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}
	


	
	
}

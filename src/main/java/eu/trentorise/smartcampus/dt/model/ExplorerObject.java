package eu.trentorise.smartcampus.dt.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;

public class ExplorerObject extends BaseDTObject {

//	private Map<String, Object> serviceData;
	
	private String whenWhere;
	private String image;
	private String url;
	private String origin;
	private String category;
	private Map<String,Object> contacts;
	private Map<String,Object> address;
	
	public ExplorerObject() {
		communityData = new CommunityData();
	}
	
	public String getWhenWhere() {
		return whenWhere;
	}

	public void setWhenWhere(String whenWhere) {
		this.whenWhere = whenWhere;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String source) {
		this.origin = source;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Map<String, Object> getContacts() {
		return contacts;
	}

	public void setContacts(Map<String, Object> contacts) {
		this.contacts = contacts;
	}

	public Map<String, Object> getAddress() {
		return address;
	}

	public void setAddress(Map<String, Object> address) {
		this.address = address;
	}

}

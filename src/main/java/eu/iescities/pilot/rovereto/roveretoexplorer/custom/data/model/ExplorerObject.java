/*******************************************************************************
 * Copyright 2012-2014 Trento RISE
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/
package eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;

public class ExplorerObject extends BaseDTObject {

//	private Map<String, Object> serviceData;
	
	private String whenWhere;
	private String image;
	private String origin;
	private List<String> category;
	private Map<String,Object> contacts;
	
  private String websiteUrl = null;
  private String facebookUrl = null;
  private String twitterUrl = null;	
	
	private Address address;
	
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

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String source) {
		this.origin = source;
	}

	public List<String> getCategory() {
		return category;
	}

	public void setCategory(List<String> category) {
		this.category = category;
	}

	public Map<String, Object> getContacts() {
		return contacts;
	}

	public void setContacts(Map<String, Object> contacts) {
		this.contacts = contacts;
	}

	public String getWebsiteUrl() {
		return websiteUrl;
	}

	public void setWebsiteUrl(String websiteUrl) {
		this.websiteUrl = websiteUrl;
	}

	public String getFacebookUrl() {
		return facebookUrl;
	}

	public void setFacebookUrl(String facebookUrl) {
		this.facebookUrl = facebookUrl;
	}

	public String getTwitterUrl() {
		return twitterUrl;
	}

	public void setTwitterUrl(String twitterUrl) {
		this.twitterUrl = twitterUrl;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

}

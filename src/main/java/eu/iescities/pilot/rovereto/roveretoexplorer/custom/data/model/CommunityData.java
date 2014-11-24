/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommunityData {
	private static final long serialVersionUID = 5926048335916274968L;

	protected List<String> tags = new ArrayList<String>();
	protected String notes;

	protected List<Rating> ratings = new ArrayList<Rating>();
	protected int ratingsCount = 0;
	protected int averageRating;
	
	protected Set<String> attending = new HashSet<String>();
	protected Integer attendees = 0;	
	
	public CommunityData() {
		super();
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public int getAverageRating() {
		return averageRating;
	}

	public void setAverageRating(int averageRating) {
		this.averageRating = averageRating;
	}

	public List<Rating> getRatings() {
		return ratings;
	}

	public Set<String> getAttending() {
		return attending;
	}

	public void setAttending(Set<String> attending) {
		this.attending = attending;
	}

	public Integer getAttendees() {
		return attendees;
	}

	public void setAttendees(Integer attendees) {
		this.attendees = attendees;
	}
	
	public int getRatingsCount() {
		return ratingsCount;
	}

	public void setRatingsCount(int ratingsCount) {
		this.ratingsCount = ratingsCount;
	}		
	
	public void setRatings(List<Rating> ratings) {
		this.ratings = ratings;
		setRatingsCount(ratings == null ? 0 : ratings.size());
	}

	public static void filterUserData(CommunityData data, String userId) {
		if (data == null) {
			return;
		}
		List<Rating> ratings = data.getRatings();
		if (userId != null) {
			if (ratings != null && !ratings.isEmpty()) {
				boolean found = false;
				for (Rating r : ratings) {
					if (r.getUserId().equals(userId)) {
						data.setRatings(Collections.singletonList(r));
						found = true;
						break;
					}
				}
				if (!found) {
					data.setRatings(Collections.<Rating> emptyList());
				}
				data.setRatingsCount(ratings.size());
			}
		} else {
			data.setRatings(Collections.<Rating> emptyList());
			if (ratings != null) {
				data.setRatingsCount(ratings.size());
			}
		}

		Set<String> attending = data.getAttending();
		if (userId != null) {
			if (attending != null && attending.contains(userId)) {
				data.setAttending(Collections.singleton(userId));
			} else {
				data.setAttending(Collections.<String> emptySet());
			}
		} else {
			data.setAttending(Collections.<String> emptySet());
		}

	}

	public static void filterUserData(List<CommunityData> datas, String userId) {
		for (CommunityData data : datas) {
			filterUserData(data, userId);
		}
	}

}

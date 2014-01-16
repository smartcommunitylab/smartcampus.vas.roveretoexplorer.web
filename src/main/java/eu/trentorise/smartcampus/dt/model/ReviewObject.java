package eu.trentorise.smartcampus.dt.model;

import java.util.ArrayList;
import java.util.List;

import org.mvel2.optimizers.impl.refl.nodes.ArrayLength;

import eu.trentorise.smartcampus.presentation.data.BasicObject;

public class ReviewObject  {

	protected String id;
	
	protected List<Review> reviews;

	public ReviewObject() {
	}
	
	public ReviewObject(String id) {
		this.id = id;
		reviews = new ArrayList<Review>();
	}
	
	public List<Review> getReviews() {
		return reviews;
	}

	public void setReviews(List<Review> reviews) {
		this.reviews = reviews;
	}

	public String getParentId() {
		return id;
	}

	public void setParentId(String parentId) {
		this.id = parentId;
	}
	
	
	
}

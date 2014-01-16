package eu.trentorise.smartcampus.storage;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import eu.trentorise.smartcampus.dt.model.ReviewObject;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;

public class ReviewsMongoStorage {

	protected MongoOperations mongoTemplate = null;
	
	private static final String REVIEWS = "reviews";
	
	public ReviewsMongoStorage(MongoOperations mongoTemplate) {
		super();
		this.mongoTemplate = mongoTemplate;
	}
	
	public void storeObject(ReviewObject review) {
		mongoTemplate.save(review, REVIEWS);
	}
	
	public ReviewObject getObjectById(String id) throws NotFoundException {
		return mongoTemplate.findById(id, ReviewObject.class, REVIEWS);
	}
	
//	public ReviewObject getObjectByParentId(String id) {
//		Criteria criteria = Criteria.where("id").is(id);
//		return mongoTemplate.findOne(Query.query(criteria), ReviewObject.class, REVIEWS);
//	}	
	
}

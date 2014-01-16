package eu.trentorise.smartcampus.storage;

import org.springframework.data.mongodb.core.MongoOperations;

import eu.trentorise.smartcampus.dt.model.ServiceDataObject;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;

public class ServiceDataMongoStorage {

	protected MongoOperations mongoTemplate = null;
	
	private static final String SERVICE_DATA = "serviceData";
	
	public ServiceDataMongoStorage(MongoOperations mongoTemplate) {
		super();
		this.mongoTemplate = mongoTemplate;
	}
	
	public void storeObject(ServiceDataObject review) {
		mongoTemplate.save(review, SERVICE_DATA);
	}
	
	public ServiceDataObject getObjectById(String id) throws NotFoundException {
		return mongoTemplate.findById(id, ServiceDataObject.class, SERVICE_DATA);
	}
	
}

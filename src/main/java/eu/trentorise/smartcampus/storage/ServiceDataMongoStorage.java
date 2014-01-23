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
package eu.trentorise.smartcampus.storage;

import org.springframework.data.mongodb.core.MongoOperations;

import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.model.ServiceDataObject;
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

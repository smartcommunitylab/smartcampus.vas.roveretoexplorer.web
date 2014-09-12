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
package eu.trentorise.smartcampus.roveretoexplorer.listener;

import it.sayservice.platform.client.InvocationException;
import it.sayservice.platform.client.ServiceBusClient;

import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class Subscriber {

	public static final String GET_EVENTI_ROVERETO = "GetEventiRovereto";
	public static final String ROVERETO_EXPLORER = "smartcampus.service.roveretoexplorer";
	
	public static final String GET_EVENTI_FB = "GetEvents";
	public static final String FB_EVENTS = "eu.trentorise.smartcampus.services.fb.events.FacebookEvents";	

	private Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	@Value("${fb.events}")
	private String fbEvents;	
	
	@Autowired
	@Value("${fb.token}")
	private String fbToken;		
	
	@Autowired
	private ServiceBusClient client;

	public Subscriber() {
	}
	
	@PostConstruct
	public void init() {
		try {
			System.out.println("SUBSCRIBE");
			Map<String, Object> params = new TreeMap<String, Object>();
			client.subscribeService(ROVERETO_EXPLORER, GET_EVENTI_ROVERETO, params);

			params.put("token", fbToken);
			params.put("overrideLocation", "");
			
			String ids[] = fbEvents.split(",");
			for (String id: ids) {
				params.put("source", id);
				client.subscribeService(FB_EVENTS, GET_EVENTI_FB, params);	
			}
			
			
		} catch (InvocationException e) {
			logger.error("Failed to subscribe for service events: " + e.getMessage());
		}		
	}
	
}

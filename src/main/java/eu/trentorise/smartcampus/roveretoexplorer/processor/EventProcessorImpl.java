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
package eu.trentorise.smartcampus.roveretoexplorer.processor;

import it.sayservice.platform.client.ServiceBusClient;
import it.sayservice.platform.client.ServiceBusListener;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import com.googlecode.protobuf.format.JsonFormat;

import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.model.Address;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.model.ExplorerObject;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.model.ServiceDataObject;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.presentation.storage.sync.BasicObjectSyncStorage;
import eu.trentorise.smartcampus.roveretoexplorer.listener.Subscriber;
import eu.trentorise.smartcampus.service.roveretoexplorer.data.message.Roveretoexplorer.EventoRovereto;
import eu.trentorise.smartcampus.storage.ServiceDataMongoStorage;

//import eu.trentorise.smartcampus.dt.model.InfoObject;

public class EventProcessorImpl implements ServiceBusListener {

	@Autowired
	private BasicObjectSyncStorage storage;
	
	@Autowired
	private ServiceDataMongoStorage serviceDataStorage;

	@Autowired
	ServiceBusClient client;
	
	private Map<String, Object> customMap;

	private static Log logger = LogFactory.getLog(EventProcessorImpl.class);

	public EventProcessorImpl() {
		buildCustomMap();
	}
	
	@Override
	public void onServiceEvents(String serviceId, String methodName, String subscriptionId, List<ByteString> data) {
		System.out.println(new Date() + " -> " + methodName + "@" + serviceId);
		try {
			if (Subscriber.ROVERETO_EXPLORER.equals(serviceId) || Subscriber.GET_EVENTI_ROVERETO.endsWith(serviceId)) {
				updateEvents(data);
			}

		} catch (Exception e) {
			logger.error("Error updating " + methodName);
			e.printStackTrace();
		}
	}
	
	private Map<String, Object> updateEventsSources(EventoRovereto er) throws Exception {
			String json = JsonFormat.printToString(er);
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> newMap = mapper.readValue(json, Map.class);
			newMap.putAll(customMap);
			return newMap;
	}
	
	
	private void buildCustomMap() {
		customMap = new TreeMap<String, Object>();
		Map<String, Object> subMap = new TreeMap<String, Object>();
		subMap.put("phone", new ArrayList<String>());
		subMap.put("email", "");
		subMap.put("fax", "");
//		subMap.put("facebook", "");
//		subMap.put("twitter", "");
		customMap.put("contacts", subMap);
	}

	private void updateEvents(List<ByteString> bsl) throws Exception {
		for (ByteString bs : bsl) {
			EventoRovereto er = EventoRovereto.parseFrom(bs);
			Map<String, Object> newMap = updateEventsSources(er);
			
			String titolo = er.getTitolo();
			String id = encode(Subscriber.GET_EVENTI_ROVERETO + "_" + titolo + "_" + er.getId());

			ServiceDataObject oldServiceData = null;
			Map<String, Object> oldMap = null;
			
			try {
				oldServiceData = (ServiceDataObject) serviceDataStorage.getObjectById(id);
			} catch (NotFoundException e) {}

			if (oldServiceData != null) {
				oldMap = oldServiceData.getData();
			} else {
				oldMap = new TreeMap<String, Object>();
			}			
			
			ExplorerObject oldDtobj = null;
			
			try {
				oldDtobj = (ExplorerObject) storage.getObjectById(id);
			} catch (NotFoundException e) {}

			ExplorerObject explorerObject = null;
			if (oldDtobj == null) {
				explorerObject	 = new ExplorerObject();
				oldMap = new TreeMap<String, Object>();
			} else {
				explorerObject = oldDtobj;
			}
			explorerObject.setType("Event");
			explorerObject.setSource(Subscriber.ROVERETO_EXPLORER);
			explorerObject.setId(id);

			Set<String> diff = findDifferences(oldMap, newMap);
			copySourceData(explorerObject, oldMap, newMap, diff);
			
			ServiceDataObject sdo = new ServiceDataObject(id);
			sdo.setData(newMap);
			serviceDataStorage.storeObject(sdo);
			
			if (!diff.isEmpty()) {
				storage.storeObject(explorerObject);
				System.out.println("CHANGED "  + titolo + ": " + id);
			}
		}

	}

	private Set<String> findDifferences(Map<String, Object> oldMap, Map<String, Object> newMap) {
		if (oldMap == null) {
			return newMap.keySet();
		}
		MapDifference<String, Object> diffMap = Maps.difference(oldMap, newMap);
		Set<String> diff = new HashSet(diffMap.entriesDiffering().keySet());
		diff.addAll(diffMap.entriesOnlyOnRight().keySet());
		return diff;
	}
	
	private void copySourceData(ExplorerObject explorerObject, Map<String, Object> oldMap, Map<String, Object> newMap, Set<String> only) {
		if (only.contains("titolo")) {
			explorerObject.setTitle((String)newMap.get("titolo"));
		}
		if (only.contains("descrizione")) {
			explorerObject.setDescription((String)newMap.get("descrizione"));
		}
		if (only.contains("lat") && only.contains("lon")) {
		double loc[] = new double[] { (Double)newMap.get("lat"), (Double)newMap.get("lon") };
		explorerObject.setLocation(loc);
		}
		if (only.contains("fromTime")) {
			explorerObject.setFromTime((Long)newMap.get("fromTime"));
		}		
		if (only.contains("toTime")) {
			explorerObject.setToTime((Long)newMap.get("toTime"));
		}				
		if (only.contains("whenWhere")) {
			explorerObject.setWhenWhere((String)newMap.get("whenWhere"));
		}					
		if (only.contains("tipo")) {
			explorerObject.setCategory((String)newMap.get("tipo"));
		}					
		if (only.contains("fonte")) {
			explorerObject.setOrigin((String)newMap.get("fonte"));
		}			
		if (only.contains("image")) {
			explorerObject.setImage((String)newMap.get("image"));
		}
		if (only.contains("websiteUrl")) {
			explorerObject.setWebsiteUrl((String)newMap.get("websiteUrl"));
		}				
		if (only.contains("websiteUrl")) {
			explorerObject.setWebsiteUrl((String)newMap.get("websiteUrl"));
		}						
		
		// TODO check different fields?
		
		if (only.contains("indirizzo")) {
			Map nMap = (Map)newMap.get("indirizzo");
			Set<String> diff = findDifferences((Map)oldMap.get("indirizzo"), nMap);
			Address address = explorerObject.getAddress();
			if (address == null) {
				address = new Address();
			}
			if (diff.contains("place")) {
				address.setLuogo((String)nMap.get("place"));
			}
			if (diff.contains("street")) {
				address.setVia((String)nMap.get("street"));
			}
			if (diff.contains("town")) {
				address.setCitta((String)nMap.get("town"));
			}			
			explorerObject.setAddress(address);
		}				
		if (only.contains("contacts")) {
//			explorerObject.setContacts((Map)newMap.get("contacts"));
			explorerObject.setContacts(updateMap(explorerObject.getContacts(), (Map)oldMap.get("indirizzo"), (Map)newMap.get("contacts")));
		}					
		
	}
	
	private Map<String, Object> updateMap(Map<String, Object> dtMap, Map<String, Object> oldMap, Map<String, Object> newMap) {
		Map<String, Object> newDtMap;
		if (dtMap == null) {
			newDtMap = new TreeMap<String, Object>();
		} else {
			newDtMap = dtMap;
		}
		Set<String> diff = findDifferences(oldMap, newMap);
		for (String d: diff) {
			newDtMap.put(d, newMap.get(d));
		}
		return newDtMap;
	}
	
	public BasicObjectSyncStorage getStorage() {
		return storage;
	}

	public void setStorage(BasicObjectSyncStorage storage) {
		this.storage = storage;
	}

	public ServiceBusClient getClient() {
		return client;
	}

	public void setClient(ServiceBusClient client) {
		this.client = client;
	}

	private Long parseDate(String date) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			return sdf.parse(date).getTime();
		} catch (Exception e) {
			return null;
		}
	}

	private static String encode(String s) {
		return new BigInteger(s.getBytes()).toString(16);
	}

}

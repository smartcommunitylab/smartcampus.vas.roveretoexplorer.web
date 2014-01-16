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

import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.dt.model.ExplorerObject;
import eu.trentorise.smartcampus.dt.model.ServiceDataObject;
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
		subMap.put("facebook", "");
		subMap.put("twitter", "");
		customMap.put("contacts", subMap);
	}

	private void updateEvents(List<ByteString> bsl) throws Exception {
		for (ByteString bs : bsl) {
			EventoRovereto er = EventoRovereto.parseFrom(bs);
			Map<String, Object> newMap = updateEventsSources(er);
			
			String titolo = er.getTitolo();
			System.out.println(titolo);
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

			Set<String> diff = findDifferences(oldMap, newMap);
			
			ExplorerObject explorerObject = null;
			if (oldDtobj == null) {
				explorerObject	 = new ExplorerObject(); 
			} else {
				explorerObject = oldDtobj;
			}
			explorerObject.setType("Event");
			explorerObject.setSource(Subscriber.ROVERETO_EXPLORER);
			explorerObject.setId(id);
//			explorerObject.setServiceData(newMap);

			copySourceData(explorerObject, newMap, diff);
			
			ServiceDataObject sdo = new ServiceDataObject(id);
			sdo.setData(newMap);
			serviceDataStorage.storeObject(sdo);
			
			if (!diff.isEmpty()) {
				storage.storeObject(explorerObject);
				System.out.println("CHANGED " + id);
			}
		}

	}

	private Set<String> findDifferences(Map<String, Object> oldMap, Map<String, Object> newMap) {
		MapDifference<String, Object> diffMap = Maps.difference(oldMap, newMap);
		Set<String> diff = new HashSet(diffMap.entriesDiffering().keySet());
		diff.addAll(diffMap.entriesOnlyOnRight().keySet());
		return diff;
	}
	
	private void copySourceData(ExplorerObject explorerObject, Map<String, Object> map, Set<String> only) {
		if (only.contains("titolo")) {
			explorerObject.setTitle((String)map.get("titolo"));
		}
		if (only.contains("descrizione")) {
			explorerObject.setDescription((String)map.get("descrizione"));
		}
		if (only.contains("lat") && only.contains("lon")) {
		double loc[] = new double[] { (Double)map.get("lat"), (Double)map.get("lon") };
		explorerObject.setLocation(loc);
		}
		if (only.contains("fromTime")) {
			explorerObject.setFromTime((Long)map.get("fromTime"));
		}		
		if (only.contains("toTime")) {
			explorerObject.setFromTime((Long)map.get("toTime"));
		}				
		if (only.contains("whenWhere")) {
			explorerObject.setWhenWhere((String)map.get("whenWhere"));
		}					
		if (only.contains("tipo")) {
			explorerObject.setCategory((String)map.get("tipo"));
		}					
		if (only.contains("fonte")) {
			explorerObject.setOrigin((String)map.get("fonte"));
		}			
		if (only.contains("image")) {
			explorerObject.setImage((String)map.get("image"));
		}
		if (only.contains("url")) {
			explorerObject.setUrl((String)map.get("url"));
		}				
		
		// TODO check different fields?
		
		if (only.contains("indirizzo")) {
			explorerObject.setAddress((Map)map.get("indirizzo"));
		}				
		if (only.contains("contacts")) {
			explorerObject.setContacts((Map)map.get("contacts"));
		}					
		
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

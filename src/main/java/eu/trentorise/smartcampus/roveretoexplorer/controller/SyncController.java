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
package eu.trentorise.smartcampus.roveretoexplorer.controller;


import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.dt.model.ExplorerObject;
import eu.trentorise.smartcampus.presentation.common.util.Util;
import eu.trentorise.smartcampus.presentation.data.BasicObject;
import eu.trentorise.smartcampus.presentation.data.SyncData;
import eu.trentorise.smartcampus.presentation.data.SyncDataRequest;

@Controller
public class SyncController extends AbstractObjectController {

	@RequestMapping(method = RequestMethod.POST, value = "/sync")
	public ResponseEntity<SyncData> synchronize(HttpServletRequest request, @RequestParam long since, @RequestBody Map<String,Object> obj) throws Exception{
		try {
			String userId = null;
			try {
				userId = getUserId();
			} catch (SecurityException e) {
				
			}
			// no change through sync is supported!
			obj.put("updated",Collections.<String,Object>emptyMap());
			obj.put("deleted",Collections.<String,Object>emptyMap());
			SyncDataRequest syncReq = Util.convertRequest(obj, since);
			Map<String, Object> exclude = new HashMap<String, Object>();
			if (syncReq.getSyncData().getExclude() != null) {
				exclude.putAll(syncReq.getSyncData().getExclude());
			}			
			SyncData result = storage.getSyncData(syncReq.getSince(), userId, syncReq.getSyncData().getInclude(), exclude);
			filterResult(result, userId);
			storage.cleanSyncData(syncReq.getSyncData(), userId);
			return new ResponseEntity<SyncData>(result,HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private void filterResult(SyncData result, String userId) {
		if (result.getUpdated() != null) {
			List<BasicObject> list;
			list = result.getUpdated().get(ExplorerObject.class.getName());
			if (list != null && !list.isEmpty()) {
				for (Iterator<BasicObject> iterator = list.iterator(); iterator.hasNext();) {
					ExplorerObject obj = (ExplorerObject) iterator.next();
					if (!checkDate(obj)) {
							iterator.remove();
							continue;
						}
					obj.filterUserData(userId);
				}
			}			
		}
	}
	

	private boolean checkDate(BaseDTObject obj) {
		long ref = System.currentTimeMillis()-24*60*60*1000;
		if (obj.getFromTime() == null || obj.getFromTime() == 0) {
			return true;
		}
		if (obj.getToTime() == null || obj.getToTime() == 0) {
			return true;
		}		
		return (obj.getFromTime() > ref || obj.getToTime() > ref);
	}
}

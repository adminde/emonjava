/*
 * Copyright 2016 ISC Konstanz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.isc.emonjava.com.http;

import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.isc.emonjava.Feed;
import de.isc.emonjava.com.EmoncmsException;
import de.isc.emonjava.com.http.json.Const;
import de.isc.emonjava.com.http.json.JsonTimevalue;
import de.isc.emonjava.com.http.json.ToJson;
import de.isc.emonjava.data.Field;
import de.isc.emonjava.data.ProcessList;
import de.isc.emonjava.data.Timevalue;


public class HttpFeed extends Feed {
	private static final Logger logger = LoggerFactory.getLogger(HttpFeed.class);

	/**
	 * The Feeds' current callback object, which is notified of request events
	 */
	private final HttpFeedCallbacks callbacks;
	
	/**
	 * Interface used by {@link HttpFeed} to notify the {@link HttpEmoncms} 
	 * implementation about request events
	 */
	public static interface HttpFeedCallbacks {
		
		HttpEmoncmsResponse onFeedRequest(HttpRequestAction action, HttpRequestParameters parameters, HttpRequestMethod method)
			throws EmoncmsException;
	}
	
	
	public HttpFeed(HttpFeedCallbacks callbacks, int id) {
		super(id);
		this.callbacks = callbacks;
	}

	@Override
	public String getField(Field field) throws EmoncmsException {

		logger.debug("Requesting to get field \"{}\" for feed with id: {}", field.getValue(), id);

		HttpRequestAction action = new HttpRequestAction("get");
		action.addParameter(Const.ID, id);
		action.addParameter(Const.FIELD, field.getValue());
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = callbacks.onFeedRequest(action, parameters, HttpRequestMethod.GET);
		return response.getResponse().replaceAll("\"", "");
	}

	@Override
	protected void setFields(Map<String, String> fields) throws EmoncmsException {

		logger.debug("Requesting to set {} fields for feed with id: {}", fields.size(), id);

		HttpRequestAction action = new HttpRequestAction("set");
		action.addParameter(Const.ID, id);
		ToJson json = new ToJson();
		for (Map.Entry<String, String> field : fields.entrySet()) {
			json.addString(field.getKey(), field.getValue());
		}
		action.addParameter(Const.FIELDS, json);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		callbacks.onFeedRequest(action, parameters, HttpRequestMethod.GET);
	}

	@Override
	public double getLatestValue() throws EmoncmsException {
		
		logger.debug("Requesting to get latest value for feed with id: {}", id);

		HttpRequestAction action = new HttpRequestAction("value");
		action.addParameter(Const.ID, id);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = callbacks.onFeedRequest(action, parameters, HttpRequestMethod.GET);
		return Double.valueOf(response.getResponse().replaceAll("\"", ""));
	}

	@Override
	public Timevalue getLatestTimevalue() throws EmoncmsException {
		
		logger.debug("Requesting to get latest timevalue for feed with id: {}", id);

		HttpRequestAction action = new HttpRequestAction("timevalue");
		action.addParameter(Const.ID, id);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = callbacks.onFeedRequest(action, parameters, HttpRequestMethod.GET);
		try {
			JsonTimevalue jsonTimevalue = response.getTimevalue();
			Timevalue timevalue = new Timevalue(jsonTimevalue.getTime(), jsonTimevalue.getValue());
			
			return timevalue;
			
		} catch (ClassCastException e) {
			throw new EmoncmsException("Error parsing JSON response: " + e.getMessage());
		}
	}

	@Override
	public LinkedList<Timevalue> getData(long start, long end, int interval) throws EmoncmsException {
		
		logger.debug("Requesting to fetch data from {} to {} for feed with id: {}", start, end, id);

		HttpRequestAction action = new HttpRequestAction("data");
		action.addParameter(Const.ID, id);
		action.addParameter(Const.START, start);
		action.addParameter(Const.END, end);
		action.addParameter(Const.INTERVAL, interval);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = callbacks.onFeedRequest(action, parameters, HttpRequestMethod.GET);
		try {
			return response.getTimevalues();
			
		} catch (ClassCastException e) {
			throw new EmoncmsException("Error parsing JSON response: " + e.getMessage());
		}
	}

	@Override
	protected void insertData(long time, double value) throws EmoncmsException {
		
		logger.debug("Requesting to insert value: {}, time: {} for feed with id: {}", value, time, id);

		HttpRequestAction action = new HttpRequestAction("insert");
		action.addParameter(Const.ID, id);
		action.addParameter(Const.TIME, time);
		action.addParameter(Const.VALUE, value);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		callbacks.onFeedRequest(action, parameters, HttpRequestMethod.GET);
	}

	@Override
	protected void updateData(long time, double value) throws EmoncmsException {
		
		logger.debug("Requesting to update value: {} at time: {} for feed with id: {}", value, time, id);

		HttpRequestAction action = new HttpRequestAction("update");
		action.addParameter(Const.ID, id);
		action.addParameter(Const.TIME, time);
		action.addParameter(Const.VALUE, value);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		callbacks.onFeedRequest(action, parameters, HttpRequestMethod.GET);
	}

	@Override
	public void deleteData(long time) throws EmoncmsException {
		
		logger.debug("Requesting to delete value at time: {} for feed with id: {}", time, id);

		HttpRequestAction action = new HttpRequestAction("deletedatapoint");
		action.addParameter(Const.ID, id);
		action.addParameter(Const.TIME, time);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		callbacks.onFeedRequest(action, parameters, HttpRequestMethod.GET);
	}

	@Override
	public void deleteDataRange(long start, long end) throws EmoncmsException {
		
		logger.debug("Requesting to delete values from {} to {} for feed with id: {}", start, end, id);

		HttpRequestAction action = new HttpRequestAction("deletedatarange");
		action.addParameter(Const.ID, id);
		action.addParameter(Const.START, start);
		action.addParameter(Const.END, end);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		callbacks.onFeedRequest(action, parameters, HttpRequestMethod.GET);
	}

	@Override
	public ProcessList getProcessList() throws EmoncmsException {

		logger.debug("Requesting process list for feed with id: {}", id);
		
		HttpRequestAction action = new HttpRequestAction("process/get");
		action.addParameter(Const.ID, id);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = callbacks.onFeedRequest(action, parameters, HttpRequestMethod.GET);
		return new ProcessList(response.getResponse().replaceAll("\"", ""));
	}

	@Override
	public void setProcessList(ProcessList processList) throws EmoncmsException {

		logger.debug("Requesting to set process list for feed with id: {}", id, processList);
		
		HttpRequestAction action = new HttpRequestAction("process/set");
		action.addParameter(Const.ID, id);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		parameters.addParameter(Const.PROCESSLIST.toLowerCase(), processList.toString());
		
		callbacks.onFeedRequest(action, parameters, HttpRequestMethod.POST);
	}

	@Override
	public void resetProcessList() throws EmoncmsException {

		logger.debug("Requesting to reset process list for feed with id: {}", id);
		
		HttpRequestAction action = new HttpRequestAction("process/reset");
		action.addParameter(Const.ID, id);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		callbacks.onFeedRequest(action, parameters, HttpRequestMethod.GET);
	}

	@Override
	public void delete() throws EmoncmsException {

		logger.debug("Requesting to delete feed with id: {}", id);
		
		HttpRequestAction action = new HttpRequestAction("delete");
		action.addParameter(Const.ID, id);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		callbacks.onFeedRequest(action, parameters, HttpRequestMethod.GET);
	}

}

/* 
 * Copyright 2016-18 ISC Konstanz
 * 
 * This file is part of emonjava.
 * For more information visit https://github.com/isc-konstanz/emonjava
 * 
 * Emonjava is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Emonjava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with emonjava.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.emoncms.com.http;

import java.util.LinkedList;
import java.util.Map;

import org.emoncms.Emoncms;
import org.emoncms.Feed;
import org.emoncms.com.EmoncmsException;
import org.emoncms.com.EmoncmsUnavailableException;
import org.emoncms.com.http.json.Const;
import org.emoncms.com.http.json.JsonFeed;
import org.emoncms.com.http.json.JsonTimevalue;
import org.emoncms.com.http.json.ToJsonObject;
import org.emoncms.com.http.request.HttpEmoncmsResponse;
import org.emoncms.com.http.request.HttpRequestCallbacks;
import org.emoncms.com.http.request.HttpRequestParameters;
import org.emoncms.com.http.request.HttpRequestURI;
import org.emoncms.data.Datatype;
import org.emoncms.data.Engine;
import org.emoncms.data.ProcessList;
import org.emoncms.data.Timevalue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HttpFeed extends Feed {
	private static final Logger logger = LoggerFactory.getLogger(HttpFeed.class);

	/**
	 * The Feeds' current callback object, which is notified of request events
	 */
	private final HttpRequestCallbacks callbacks;

	public HttpFeed(HttpRequestCallbacks callbacks, int id, String name, String tag, 
			boolean visible, Integer size, Datatype datatype, Engine engine, ProcessList pocessList, Timevalue value) {
		super(id, name, tag, visible, size, datatype, engine, pocessList, value);
		this.callbacks = callbacks;
	}

	public HttpFeed(HttpRequestCallbacks callbacks, int id) {
		super(id);
		this.callbacks = callbacks;
	}

	@Override
	public String getField(String field) throws EmoncmsException {

		logger.debug("Requesting to get field \"{}\" for feed with id: {}", field, id);

		HttpRequestURI uri = new HttpRequestURI("get.json");
		uri.addParameter(Const.ID, id);
		uri.addParameter(Const.FIELD, field);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = callbacks.onGet("feed", uri, parameters);
		return response.getResponse().replaceAll("\"", "");
	}

	@Override
	protected void setFields(Map<String, String> fields) throws EmoncmsException {

		logger.debug("Requesting to set {} fields for feed with id: {}", fields.size(), id);

		HttpRequestURI uri = new HttpRequestURI("set.json");
		uri.addParameter(Const.ID, id);
		ToJsonObject json = new ToJsonObject();
		for (Map.Entry<String, String> field : fields.entrySet()) {
			json.addString(field.getKey(), field.getValue());
		}
		uri.addParameter(Const.FIELDS, json);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		callbacks.onGet("feed", uri, parameters);
	}

	@Override
	public Double getLatestValue() throws EmoncmsException {
		
		logger.debug("Requesting to get latest value for feed with id: {}", id);

		HttpRequestURI uri = new HttpRequestURI("value.json");
		uri.addParameter(Const.ID, id);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = callbacks.onGet("feed", uri, parameters);
		return Double.valueOf(response.getResponse().replaceAll("\"", ""));
	}

	@Override
	public Timevalue getLatestTimevalue() throws EmoncmsException {
		
		logger.debug("Requesting to get latest timevalue for feed with id: {}", id);

		HttpRequestURI uri = new HttpRequestURI("timevalue.json");
		uri.addParameter(Const.ID, id);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = callbacks.onGet("feed", uri, parameters);
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

		HttpRequestURI uri = new HttpRequestURI("data.json");
		uri.addParameter(Const.ID, id);
		uri.addParameter(Const.START, start);
		uri.addParameter(Const.END, end);
		uri.addParameter(Const.INTERVAL, interval);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = callbacks.onGet("feed", uri, parameters);
		try {
			return response.getTimevalues();
			
		} catch (ClassCastException e) {
			throw new EmoncmsException("Error parsing JSON response: " + e.getMessage());
		}
	}

	@Override
	protected void insertData(long time, double value) throws EmoncmsException {
		
		logger.debug("Requesting to insert value: {}, time: {} for feed with id: {}", value, time, id);

		HttpRequestURI uri = new HttpRequestURI("insert.json");
		uri.addParameter(Const.ID, id);
		uri.addParameter(Const.TIME, time);
		uri.addParameter(Const.VALUE, value);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		callbacks.onGet("feed", uri, parameters);
	}

	@Override
	protected void updateData(long time, double value) throws EmoncmsException {
		
		logger.debug("Requesting to update value: {} at time: {} for feed with id: {}", value, time, id);

		HttpRequestURI uri = new HttpRequestURI("update.json");
		uri.addParameter(Const.ID, id);
		uri.addParameter(Const.TIME, time);
		uri.addParameter(Const.VALUE, value);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		callbacks.onGet("feed", uri, parameters);
	}

	@Override
	public void deleteData(long time) throws EmoncmsException {
		
		logger.debug("Requesting to delete value at time: {} for feed with id: {}", time, id);

		HttpRequestURI uri = new HttpRequestURI("deletedatapoint.json");
		uri.addParameter(Const.ID, id);
		uri.addParameter(Const.TIME, time);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		callbacks.onGet("feed", uri, parameters);
	}

	@Override
	public void deleteDataRange(long start, long end) throws EmoncmsException {
		
		logger.debug("Requesting to delete values from {} to {} for feed with id: {}", start, end, id);

		HttpRequestURI uri = new HttpRequestURI("deletedatarange.json");
		uri.addParameter(Const.ID, id);
		uri.addParameter(Const.START, start);
		uri.addParameter(Const.END, end);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		callbacks.onGet("feed", uri, parameters);
	}

	@Override
	public void setProcessList(String processList) throws EmoncmsException {

		logger.debug("Requesting to set process list for feed with id: {}", id, processList);
		
		HttpRequestURI uri = new HttpRequestURI("process/set.json");
		uri.addParameter(Const.ID, id);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		parameters.addParameter(Const.PROCESSLIST.toLowerCase(), processList);
		
		callbacks.onPost("feed", uri, parameters);
	}

	@Override
	public void resetProcessList() throws EmoncmsException {

		logger.debug("Requesting to reset process list for feed with id: {}", id);
		
		HttpRequestURI uri = new HttpRequestURI("process/reset.json");
		uri.addParameter(Const.ID, id);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		callbacks.onGet("feed", uri, parameters);
	}

	@Override
	public void delete() throws EmoncmsException {

		logger.debug("Requesting to delete feed with id: {}", id);
		
		HttpRequestURI uri = new HttpRequestURI("delete.json");
		uri.addParameter(Const.ID, id);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		callbacks.onGet("feed", uri, parameters);
	}

	@Override
	public void load() throws EmoncmsException {

		logger.debug("Requesting feed with id: {}", id);

		HttpRequestURI uri = new HttpRequestURI("aget.json");
		uri.addParameter(Const.ID, id);
		
		HttpRequestParameters parameters = new HttpRequestParameters();
		
		HttpEmoncmsResponse response = callbacks.onGet("feed", uri, parameters);
		try {
			JsonFeed jsonFeed = response.getFeed();
			
			this.name = jsonFeed.getName();
			this.tag = jsonFeed.getTag();
			this.visible = jsonFeed.isPublic();
			this.size = jsonFeed.getSize();
			this.datatype =  Datatype.getEnum(jsonFeed.getDatatype());
			this.engine = Engine.getEnum(jsonFeed.getEngine());
			if (jsonFeed.getTime() != null && jsonFeed.getValue() != null) {
				this.timevalue = new Timevalue(jsonFeed.getTime(), jsonFeed.getValue());
			}
			
		} catch (ClassCastException e) {
			throw new EmoncmsException("Error parsing JSON response: " + e.getMessage());
		}
	}
	
	public static Feed connect(Emoncms connection, int id) throws EmoncmsUnavailableException {
		if (connection != null && connection instanceof HttpRequestCallbacks) {
			return new HttpFeed((HttpRequestCallbacks) connection, id);
		}
		else throw new EmoncmsUnavailableException("HTTP connection to emoncms webserver invalid");
	}
}

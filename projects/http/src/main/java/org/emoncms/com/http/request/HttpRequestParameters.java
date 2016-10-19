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
package org.emoncms.com.http.request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.emoncms.com.http.json.ToJson;


public class HttpRequestParameters extends LinkedHashMap<String, String> {
	private static final long serialVersionUID = 8225350318869217570L;
	
	
	public HttpRequestParameters() {
		super();
	}
	
	public void addParameter(String key, ToJson value) {
		super.put(key, value.toString());
	}
	
	public void addParameter(String key, String value) {
		super.put(key, value);
	}
	
	public void addParameter(String key, double value) {
		super.put(key, String.valueOf(value));
	}
	
	public void addParameter(String key, long value) {
		super.put(key, String.valueOf(value));
	}
	
	public void addParameter(String key, int value) {
		super.put(key, String.valueOf(value));
	}
	
	public void addParameter(String key, boolean value) {
		super.put(key, String.valueOf(value));
	}
	
	public String parseParameters() throws UnsupportedEncodingException {
        StringBuilder parameterListBuilder = new StringBuilder();

		Iterator<Map.Entry<String, String>> iteratorParameterList = super.entrySet().iterator();
		while (iteratorParameterList.hasNext()) {
			Map.Entry<String, String> parameter = iteratorParameterList.next();
			
        	parameterListBuilder.append(URLEncoder.encode(parameter.getKey(), "UTF-8"));
        	parameterListBuilder.append('=');
        	parameterListBuilder.append(URLEncoder.encode(parameter.getValue(), "UTF-8"));

        	if (iteratorParameterList.hasNext()) {
        		parameterListBuilder.append('&');
        	}
		}
        
        return parameterListBuilder.toString();
	}

	@Override
	public String toString() {
        StringBuilder parameterListBuilder = new StringBuilder();

		Iterator<Map.Entry<String, String>> iteratorParameterList = super.entrySet().iterator();
		while (iteratorParameterList.hasNext()) {
			Map.Entry<String, String> parameter = iteratorParameterList.next();
			
        	parameterListBuilder.append(parameter.getKey());
        	parameterListBuilder.append('=');
        	parameterListBuilder.append(parameter.getValue());

        	if (iteratorParameterList.hasNext()) {
        		parameterListBuilder.append('&');
        	}
		}
        
        return parameterListBuilder.toString();
	}
}

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

import org.emoncms.com.http.json.Const;

public class HttpRequestAuthentication {

	public static final String NONE = "none";

	private final String type;
	private final String key;

	public HttpRequestAuthentication(String type, String key) {
		this.type = type;
		this.key = key;
	}

	public HttpRequestAuthentication(String key) {
		this(Const.API_KEY, key);
	}

	public String getAuthentication() throws UnsupportedEncodingException {
		return type + "=" + URLEncoder.encode(key, "UTF-8");
	}
}
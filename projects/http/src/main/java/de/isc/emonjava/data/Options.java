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
package de.isc.emonjava.data;

import java.util.HashMap;


public class Options extends HashMap<String, String> {
	private static final long serialVersionUID = -8755240981107809706L;
	

	/**
	 * Sets the logging interval of the corresponding feed engine in milliseconds.
	 * 
	 * @param interval
	 * 	the logging interval of the corresponding feed engine in milliseconds
	 * 
	 * @return 
	 * 	the previous value associated with key, or null if there was no mapping for key. 
	 * 	(A null return can also indicate that the map previously associated null with key.)
	 */
	public String setInterval(int interval) {
		return put("interval", String.valueOf(interval));
	}
}

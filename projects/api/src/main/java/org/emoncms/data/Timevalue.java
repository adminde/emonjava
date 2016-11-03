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
package org.emoncms.data;


public class Timevalue {

	private final Long time;
	private final double value;


	public Timevalue(Long timestamp, double value) {
		this.time = timestamp;
		this.value = value;
	}

	public Timevalue(double value) {
		this(null, value);
	}

	public double getValue() {
		return value;
	}

	public Long getTime() {
		return time;
	}
	
	@Override
	public String toString() {
		if (time != null) {
			return "value: " + value + ", time: " + time;
		}
		else {
			return "value: " + value;
		}
	}
}
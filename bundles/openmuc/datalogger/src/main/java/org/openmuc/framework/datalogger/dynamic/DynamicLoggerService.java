/* 
 * Copyright 2016-19 ISC Konstanz
 * 
 * This file is part of emonjava.
 * For more information visit https://github.com/isc-konstanz/emonjava
 * 
 * Emonjava is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Emonjava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with emonjava.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openmuc.framework.datalogger.dynamic;

import java.io.IOException;
import java.util.List;

import org.openmuc.framework.data.Record;
import org.openmuc.framework.datalogger.data.Channel;
import org.openmuc.framework.datalogger.data.Configuration;

public interface DynamicLoggerService {

	public String getId();

	default boolean isActive() {
		return true;
	}

	default void onActivate(Configuration config) throws IOException {
		// Optional method
	}

	default void onConfigure(List<Channel> channels) throws IOException {
		// Optional method
	}

	default void onDeactivate() {
		// Optional method
	}

	public void doLog(Channel channel, long timestamp) throws IOException;

	public void doLog(List<Channel> channels, long timestamp) throws IOException;

	public List<Record> getRecords(Channel channel, long startTime, long endTime) throws IOException;

}

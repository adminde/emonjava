/* 
 * Copyright 2016-18 ISC Konstanz
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
package org.openmuc.framework.datalogger.emoncms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.emoncms.Emoncms;
import org.emoncms.Feed;
import org.emoncms.Input;
import org.emoncms.com.EmoncmsException;
import org.emoncms.com.EmoncmsSyntaxException;
import org.emoncms.com.EmoncmsUnavailableException;
import org.emoncms.com.http.HttpEmoncmsFactory;
import org.emoncms.com.http.HttpFeed;
import org.emoncms.com.http.HttpInput;
import org.emoncms.data.Authentication;
import org.emoncms.data.Data;
import org.emoncms.data.Namevalue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.TypeConversionException;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.datalogger.emoncms.data.ChannelAverage;
import org.openmuc.framework.datalogger.emoncms.data.ChannelDynamic;
import org.openmuc.framework.datalogger.emoncms.data.ChannelHandler;
import org.openmuc.framework.datalogger.emoncms.data.ChannelLogSettings;
import org.openmuc.framework.datalogger.emoncms.data.DeviceDataList;
import org.openmuc.framework.datalogger.spi.DataLoggerService;
import org.openmuc.framework.datalogger.spi.LogChannel;
import org.openmuc.framework.datalogger.spi.LogRecordContainer;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class EmonLogger implements DataLoggerService {
	private final static Logger logger = LoggerFactory.getLogger(EmonLogger.class);

    @Reference
    private DataAccessService dataAccessService;
	private Emoncms connection = null;

	private final HashMap<String, ChannelHandler> channelInputs = new HashMap<String, ChannelHandler>();

	@Activate
	protected void activate(ComponentContext context) {
		logger.info("Activating Emoncms Logger");
		try {
			EmoncmsConfig configs = new EmoncmsConfig();
			
			String address = configs.getAddress();
			try {
				int maxThreads = configs.getMaxThreads();
				
				if (configs.hasAuthentication()) {
					String authorization = configs.getAuthorization();
					String authentication = configs.getAuthentication();
					
					connection = HttpEmoncmsFactory.newAuthenticatedConnection(address, authorization, authentication, maxThreads);
				}
				else {
					connection = HttpEmoncmsFactory.newConnection(address, maxThreads);
				}
				connection.start();
				
			} catch (EmoncmsUnavailableException e) {
				logger.warn("Unable to connect to \"{}\"", address);
			}
		} catch (IOException e) {
			logger.error("Error while reading emoncms configuration: {}", e.getMessage());
		}
	}

	@Deactivate
	protected void deactivate(ComponentContext context) {
		logger.info("Deactivating Emoncms Logger");
		if (connection != null) {
			connection.stop();
			connection = null;
		}
	}

	@Override
	public String getId() {
		return "emoncms";
	}

	@Override
	public void setChannelsToLog(List<LogChannel> channels) {
		// Will be called if OpenMUC receives new logging configurations
		if (connection == null) {
			logger.error("Requested to configure log Channels for deactivated Emoncms logger");
			return;
		}
		for (ChannelHandler channel : channelInputs.values()) {
			if (channel instanceof ChannelAverage && dataAccessService.getAllIds().contains(channel.getId())) {
				dataAccessService.getChannel(channel.getId()).removeListener((ChannelAverage) channel);
			}
		}
		channelInputs.clear();
		
		for (LogChannel channel : channels) {
			String id = channel.getId();
			
			ChannelLogSettings settings = new ChannelLogSettings(channel.getLoggingSettings());
			if (settings.isValid()) {
				try {
					Input input = HttpInput.connect(connection, settings.getNode(), id);					
					
					ChannelHandler channelInput;
					if (settings.isAveraging()) {
						channelInput = new ChannelAverage(id, input, settings);
						if (dataAccessService.getAllIds().contains(id)) {
							dataAccessService.getChannel(id).addListener((ChannelAverage) channelInput);
							((ChannelAverage) channelInput).setListening(true);
						}
					}
					else if (settings.isDynamic()) {
						channelInput = new ChannelDynamic(id, input, settings);
					}
					else {
						channelInput = new ChannelHandler(id, input, settings);
					}
					if (settings.hasFeedId()) {
						Feed feed = HttpFeed.connect(connection, settings.getFeedId());
					}
					channelInputs.put(id, channelInput);
							
				} catch (EmoncmsUnavailableException | EmoncmsSyntaxException e) {
					logger.warn("Unable to configure logging for Channel \"{}\": {}", id, e.getMessage());
				}
				
				if (logger.isTraceEnabled() && channel.getLoggingInterval() != null) {
					logger.trace("Channel \"{}\" configured to log every {}s", id, channel.getLoggingInterval()/1000);
				}
			}
			else if (settings.hasAuthorization()) {
				logger.warn("Unable to configure logging due to invalid syntax for Channel \"{}\": {}", channel.getId(), channel.getLoggingSettings());
			}
		}
	}

	@Override
	public void log(List<LogRecordContainer> containers, long timestamp) {
		if (connection == null) {
			logger.error("Requested to log values for deactivated Emoncms logger");
		}
		else if (containers == null || containers.isEmpty()) {
			logger.debug("Requested Emoncms logger to log an empty container list");
		}
		else if (containers.size() == 1) {
			LogRecordContainer container = containers.get(0);
			
			ChannelHandler channel = getChannel(container.getChannelId());
			if (isValid(container)) {
				try {
					Record record = container.getRecord();
					Long time = record.getTimestamp();
					if (time == null) {
						time = timestamp;
					}
					
					channel.post(time, record.getValue().asDouble());
					
				} catch (EmoncmsException | TypeConversionException e) {
					logger.warn("Failed to log record for channel \"{}\": {}", container.getChannelId(), e.getMessage());
				}
			}
		}
		else {
			List<DeviceDataList> devices = new ArrayList<DeviceDataList>();
			for (LogRecordContainer container : containers) {
				ChannelHandler channel = getChannel(container.getChannelId());
				if (isValid(container)) {
					try {
						Record record = container.getRecord();
						Long time = record.getTimestamp();
						if (time == null) {
							time = timestamp;
						}
						
						if (channel.update(time, record.getValue().asDouble())) {
							Namevalue namevalue = channel.getNamevalue();
							Authentication authenticator = channel.getAuthenticator();
							
							DeviceDataList device = null;
							for (DeviceDataList d : devices) {
								if (d.hasSameAuthentication(authenticator)) {
									device = d;
									break;
								}
							}
							if (device == null) {
								// No input collection for that device exists yet, so it needs to be created
								device = new DeviceDataList(authenticator);
								devices.add(device);
							}
							device.add(time, channel.getInput().getNode(), namevalue);
						}
						
					} catch (TypeConversionException e) {
						logger.warn("Failed to prepare record to log to Channel \"{}\": {}", container.getChannelId(), e.getMessage());
					}
				}
			}
			for (DeviceDataList device : devices) {
				logger.debug("Logging {} values with authentication \"{}\"", device.size(), device.getAuthenticator());
				try {
					Authentication authenticator = device.getAuthenticator();
					if (device.size() == 1) {
						Data data = device.get(0);
						
						// Data time is already in seconds, but needs to be in milliseconds for post()
						long time = data.getTime()*1000;
						if (authenticator.isDefault()) {
							connection.post(data.getNode(), time, data.getNamevalues());
						}
						else {
							connection.post(data.getNode(), time, data.getNamevalues(), authenticator);
						}
					}
					else {
						if (authenticator.isDefault()) {
							connection.post(device);
						}
						else {
							connection.post(device, device.getAuthenticator());
						}
					}
				} catch (EmoncmsException e) {
					logger.warn("Error logging values: {}", e.getMessage());
				}
			}
		}
	}

	private boolean isValid(LogRecordContainer container) {
		if (channelInputs.containsKey(container.getChannelId())) {
		
			if (container.getRecord() != null) {
				if (container.getRecord().getFlag() == Flag.VALID && container.getRecord().getValue() != null) {
					
					if (logger.isTraceEnabled()) {
						logger.trace("Preparing record to log for channel \"{}\": {}", container.getChannelId(), container.getRecord());
					}
					return true;
				}
				else if (logger.isDebugEnabled()) {
					logger.debug("Skipped logging an invalid or empty value for channel \"{}\": {}",
							container.getChannelId(), container.getRecord().getFlag().toString());
				}
			}
			else if (logger.isTraceEnabled()) {
				logger.trace("Failed to log an empty record for channel \"{}\"", container.getChannelId());
			}
		}
		else logger.warn("Failed to log record for unconfigured channel \"{}\"", container.getChannelId());
		
		return false;
	}

	private ChannelHandler getChannel(String id) {
		ChannelHandler channel = channelInputs.get(id);
		if (channel.isAveraging()) {
			ChannelAverage listener = (ChannelAverage) channel;
			if (!listener.isListening() && dataAccessService.getAllIds().contains(id)) {
				dataAccessService.getChannel(id).addListener(listener);
				listener.setListening(true);
			}
		}
		return channel;
	}

	@Override
	public List<Record> getRecords(String channelId, long startTime, long endTime) throws IOException {
		// TODO: fetch feed id and retrieve data from web server
		throw new IOException();
	}
}

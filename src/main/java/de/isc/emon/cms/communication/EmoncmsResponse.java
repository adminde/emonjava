package de.isc.emon.cms.communication;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.isc.emon.cms.EmoncmsException;


public class EmoncmsResponse {
    private final String response;

	public EmoncmsResponse(String response) {
		this.response = response;
	}

	public Object parseJSON() throws EmoncmsException {
        JSONParser parser = new JSONParser();
        
		try {
			return parser.parse(response);
			
		} catch (ParseException e) {
			throw new EmoncmsException("Error parsing JSON string \"" + response + "\": " + e.getMessage());
		}
	}
	
	public String getMessage() {
		return response.replace("\"", "").replace("<br>", ""); //.replace("\n", "")
	}
	
	@Override
	public String toString() {
		return response;
	}
}

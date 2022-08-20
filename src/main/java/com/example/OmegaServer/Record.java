package com.example.OmegaServer;

import org.json.JSONObject;

import java.time.Instant;

public class Record {
	String jsonDataStr;
	String expirationTimeStamp;

	boolean multipleUse = false;
	boolean used = false;

	public Record(JSONObject inputJsonObj){
		int validityTime = inputJsonObj.getInt(Parsers.RequestFields.ProLongedExpTime.text());
		expirationTimeStamp = ServerLogic.getFutureTimeStamp(validityTime);
		jsonDataStr = inputJsonObj.getJSONObject(Parsers.RequestFields.Data.text()).toString();
		multipleUse = inputJsonObj.getBoolean(Parsers.RequestFields.MultipleUseField.text());
	}
	public boolean alreadyExpired(){
		int secondsMargin = 8;
		long timestampTime = Instant.parse(expirationTimeStamp).getEpochSecond();
		long currentTime =  Instant.now().getEpochSecond() + secondsMargin;
		return timestampTime <= currentTime;
	}
	public Long getSecondsToExpiration(){
		long timestampTime = Instant.parse(expirationTimeStamp).getEpochSecond();
		long currentTime =  Instant.now().getEpochSecond();
		return timestampTime - currentTime;
	}
}

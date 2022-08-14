package com.example.OmegaServer;

import java.time.Instant;

public class Record {
	String jsonDataStr;
	String expirationTimeStamp;
	boolean done = false;

	public Record(){}
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

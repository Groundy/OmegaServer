package com.example.OmegaServer;

import java.time.Instant;

public class Record {
	String jsonDataStr;
	String expirationTimeStamp;

	public Record(){}
	public boolean alreadyExpired(){
		int secondsMargin = 8;
		long timestampTime = Instant.parse(expirationTimeStamp).getEpochSecond();
		long currentTime =  Instant.now().getEpochSecond() + secondsMargin;
		return timestampTime <= currentTime;
	}
}

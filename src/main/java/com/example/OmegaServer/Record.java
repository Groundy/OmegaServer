package com.example.OmegaServer;

import java.time.Instant;

public class Record {
	String jsonDataStr;
	String expirationTimeStamp;

	public Record(){}
	public boolean isStillValid(){
		int secondsMargin = 8;
		Long timestampTime = Instant.parse(expirationTimeStamp).getEpochSecond();
		Long currentTime =  Instant.now().getEpochSecond() + secondsMargin;
		return timestampTime > currentTime;
	}
}

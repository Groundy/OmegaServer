package com.example.OmegaServer;

import org.json.JSONObject;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class ServerLogic {
	static public int getRandom6DigitCode(){
		Random random = new Random();
		int code = Math.abs(random.nextInt() % 999999) + 1;
		return code;
	}
	static public String getFutureTimeStamp(int minutes){
		Instant time = Instant.now().plusSeconds(60 * minutes);
		return DateTimeFormatter.ISO_INSTANT.format(time);
	}
}

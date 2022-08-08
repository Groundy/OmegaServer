package com.example.OmegaServer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataService {
	DataService(){
		startCleaningExecutor();
	}
	Map<Integer, Record> list = new TreeMap();

	public int addRecord (String jsonObjectStr){
		try {
			int tmpCode = getCodeThatDoesNotExistInDBYet();
			if(tmpCode == -1){
				System.out.println("Failed to add new Record");
				return -1;
			}

			Record toAdd = new Record();
			toAdd.expirationTimeStamp = ServerLogic.getFutureTimeStamp(5);
			toAdd.jsonDataStr = new JSONObject(jsonObjectStr).getJSONObject(Parsers.RequestFields.Data.text()).toString();
			list.put(tmpCode, toAdd);
			System.out.println("Successes to add new Record");
			return tmpCode;
		}catch (Exception e){
			System.out.println("Failed to add new Record");
			return -1;
		}
	}
	private int getCodeThatDoesNotExistInDBYet(){
		int tries = 0;
		while (tries < 10000){
			tries++;
			int codeCandidate = ServerLogic.getRandom6DigitCode();
			if(!list.containsKey(codeCandidate))
				return codeCandidate;
		}
		return -1;
	}
	private void deleteCode(int code){
		try {
			if(list.containsKey(code))
				list.remove(code);
		}catch (Exception e){
			;
		}

	}
	private void startCleaningExecutor(){
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate( () -> {
			deleteAllExpiredCodes();
		}, 5, 15, TimeUnit.MINUTES);
	}
	private void deleteAllExpiredCodes(){
		ArrayList<Integer> codesToDelete = new ArrayList<>();

		for(var entry : list.entrySet()){
			if(entry.getValue().alreadyExpired())
				codesToDelete.add(entry.getKey());
		}
		if(codesToDelete.size() == 0){
			System.out.println("No records to clear");
			return;

		}

		for (int codeToDel : codesToDelete){
			deleteCode(codeToDel);
		}
		System.out.println("Cleared " + codesToDelete.size() + " records");
	}

	String getListStatus() {
		JSONObject toRet = new JSONObject();
		String header = "Database size: " + list.size();
		toRet.put("serverState", header);

		JSONArray arr = new JSONArray();
		for(var entry : list.entrySet()){
			JSONObject toAdd = new JSONObject();
			toAdd.put(Parsers.ResponseFields.Code.text(), entry.getKey().toString());
			toAdd.put(Parsers.ResponseFields.ExpirationTime.text(),  entry.getValue().expirationTimeStamp);
			arr.put(toAdd);
		}
		toRet.put("array", arr);

		return toRet.toString();
	}
	ReturnCode getDataFromCodeErrorStr(int code){
		if(!list.containsKey(code))
			return ReturnCode.CodeNotExist;

		var record = list.get(code);
		if(record.alreadyExpired())
			return ReturnCode.CodeExpired;

		return ReturnCode.OK;
	}
	JSONObject getResponseFromCode(int code){
		try {
			Record record = list.get(code);
			JSONObject toRet = new JSONObject();
			toRet.put(Parsers.ResponseFields.Status.text(), Parsers.ResponseFields.Ok.text());
			toRet.put(Parsers.ResponseFields.ExpirationTime.text() , record.expirationTimeStamp);
			toRet.put(Parsers.ResponseFields.TransferData.text() , record.jsonDataStr);
			return toRet;
		}catch (Exception e){
			return null;
		}
	}
}

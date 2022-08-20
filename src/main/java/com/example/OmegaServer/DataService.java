package com.example.OmegaServer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataService {
	private final Map<Integer, Record> list = new TreeMap<>();

	DataService(){
		startCleaningExecutor();
	}



	public Integer addRecord (String jsonObjectStr){
		try {
			int tmpCode = getCodeThatDoesNotExistInDBYet();
			if(tmpCode == -1)
				return null;

			JSONObject inputObj = getJsonForSetCodeRequest(jsonObjectStr);
			if(inputObj == null)
				return null;

			Record toAdd = new Record(inputObj);
			list.put(tmpCode, toAdd);
			return tmpCode;
		}catch (Exception e){
			return null;
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
	private Boolean deleteCode(int code){
		try {
			if(list.containsKey(code)){
				list.remove(code);
				return true;
			}
			return false;
		}catch (Exception e){
			return false;
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
	boolean markRecordAsDone(int code){
		Record record = list.get(code);
		if(record == null)
			return false;

		if(record.used)
			return false;

		if(record.multipleUse)
			return false;

		record.used = true;
		return true;
	}
	Record getRecordByCode(int code){
		Record record = list.get(code);
		return record;
	}

	String getExpirationTimeFromRecord(int code){
		return list.get(code).expirationTimeStamp;
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
			toAdd.put(Parsers.ResponseFields.IsDone.text(),  entry.getValue().used);
			arr.put(toAdd);
		}
		toRet.put("array", arr);

		return toRet.toString();
	}
	ReturnCode getDataFromCode(int code){
		if(!list.containsKey(code))
			return ReturnCode.CodeNotExist;

		var record = list.get(code);
		if(record.alreadyExpired())
			return ReturnCode.CodeExpired;

		if(record.used)
			return ReturnCode.CodeUsed;

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
	JSONObject getJsonForSetCodeRequest(String jsonInputStr){
		String dataField = Parsers.RequestFields.Data.text();
		String prolongedTimeField = Parsers.RequestFields.ProLongedExpTime.text();
		String isMultipleUseField = Parsers.RequestFields.MultipleUseField.text();
		int defaultExpTime = 5;

		try {
			JSONObject inputJson = new JSONObject(jsonInputStr);

			boolean hasDataField = inputJson.has(dataField);
			if(!hasDataField) {
				return null;
			}

			JSONObject toRet = new JSONObject();
			toRet.put(dataField, inputJson.getJSONObject(dataField));

			if(inputJson.has(isMultipleUseField))
				toRet.put(isMultipleUseField, inputJson.getBoolean(isMultipleUseField));
			else
				toRet.put(isMultipleUseField, false);

			if(inputJson.has(prolongedTimeField))
				toRet.put(prolongedTimeField, inputJson.getInt(prolongedTimeField));
			else
				toRet.put(prolongedTimeField, defaultExpTime);

			return toRet;
		}catch (Exception e){
			return null;
		}
	}
}

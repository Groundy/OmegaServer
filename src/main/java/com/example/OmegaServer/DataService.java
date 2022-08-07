package com.example.OmegaServer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
public class DataService {
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
			toAdd.jsonDataStr = jsonObjectStr;
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
	void deleteAllExpiredCodes(){
		ArrayList<Integer> codesToDelete = new ArrayList<>();

		for(var entry : list.entrySet()){
			if(!entry.getValue().isStillValid())
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
			toAdd.put(Parsers.Fields.Code.text(), entry.getKey().toString());
			toAdd.put(Parsers.Fields.ExpirationTime.text(),  entry.getValue().expirationTimeStamp);
			arr.put(toAdd);
		}
		toRet.put("array", arr);

		return toRet.toString();
	}

	String getDataFromCodeErrorStr(int code){
		if(!list.containsKey(code))
			return "There's not such code!";

		var record = list.get(code);
		if(!record.isStillValid())
			return "That code expired!";

		return null;
	}
	JSONObject getResponseFromCode(int code){
		try {
			Record record = list.get(code);
			JSONObject toRet = new JSONObject();
			toRet.put("expirationTime" , record.expirationTimeStamp);
			toRet.put("transferData", record.jsonDataStr);
			return toRet;
		}catch (Exception e){
			return null;
		}
	}
}

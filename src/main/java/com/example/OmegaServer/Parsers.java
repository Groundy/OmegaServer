package com.example.OmegaServer;

import org.json.JSONException;
import org.json.JSONObject;

public class Parsers {
	enum RequestFields {
	//SenderAccNumber("senderAccNumber"),
	//SenderAccName("senderAccName"),
	ReceiverAccNumber("receiverAccNumber"),
	ReceiverName("receiverName"),
	Description("description"),
	Amount("amount"),
	Currency("currency"),
	ExecutionDate("executionDate"),

	Data("data"),
	MultipleUseField("multipleUse"),
	ProLongedExpTime("proLongedExpTime");

	private String fieldName;
	RequestFields(String fieldName) {
		this.fieldName = fieldName;
	}
	public String text() {
		return fieldName;
	}
}
	enum ResponseFields {
		Code("code"),
		Status("status"),
		Ok("ok"),
		Failed("failed"),
		ErrorMsg("errorMsg"),
		ExpirationTime("expirationTime"),
		Size("size"),
		TransferData("transferData"),
		IsDone("isDone");


		private String fieldName;
		ResponseFields(String fieldName) {
			this.fieldName = fieldName;
		}
		public String text() {
			return fieldName;
		}
	}

	static ReturnCode setCodeRequestParsingErrorStr(String input){
		try {
			JSONObject obj = new JSONObject(input).getJSONObject(RequestFields.Data.text());
			obj.getString(RequestFields.ReceiverName.text());
			obj.getString(RequestFields.ReceiverAccNumber.text());
			obj.getString(RequestFields.ReceiverName.text());
			obj.getString(RequestFields.Description.text());
			obj.getDouble(RequestFields.Amount.text());
			obj.getString(RequestFields.Currency.text());
			return ReturnCode.OK;
		}catch (Exception e){
			return ReturnCode.BadRequest;
		}
	}
	static Integer getCodeFromRequest(String input){
		try {
			int code = new JSONObject(input).getInt(ResponseFields.Code.text());
			return code;
		}catch (Exception e){
			return null;
		}
	}
	static JSONObject setCodeResultOk(int code, String expirationTime){
		JSONObject toRet = new JSONObject();
		toRet.put(ResponseFields.Status.text(), ResponseFields.Ok.text());
		toRet.put(ResponseFields.Code.text(), String.valueOf(code));
		toRet.put(ResponseFields.ExpirationTime.text(), expirationTime);
		return toRet;
	}
	static JSONObject getFailureResponse(ReturnCode code){
		JSONObject toRet = new JSONObject();
		String msg = "";
		switch (code){
			case CodeNotExist:{
				msg = "Taki kod nie istnieje w systemie.";
				break;
			}
			case CodeExpired:{
				msg = "Ten kod wygas??.";
				break;
			}
			case CodeUsed:{
				msg = "Ten kod zosta?? ju?? wykorzystany.";
				break;
			}
			case ServerError:{
				msg = "B????d serwera.";
				break;
			}
			case BadRequest:{
				msg = "B????dne ????danie.";
				break;
			}
			case CodeAlreadyDone:{
				msg = "Kod zosta?? ju?? wykorzystany.";
				break;
			}
			case CodeMultipleUse:{
				msg = "Kod jest wielokrotnego u??ytku.";
				break;
			}
		}
		toRet.put(ResponseFields.Status.text(), ResponseFields.Failed.text());
		toRet.put(ResponseFields.ErrorMsg.text(), msg);
		return toRet;
	}

	static Integer getCodeFromCodeDoneRequest(String inputJsonStr){
		Integer requestCode = null;
		try{
			JSONObject inputJson = new JSONObject(inputJsonStr);
			requestCode = inputJson.getInt(ResponseFields.Code.text());
		} catch (JSONException e) {

		}
		return requestCode;
	}
}

package com.example.OmegaServer;

import org.json.JSONObject;

public class Parsers {
	enum SetRequestFields {
	//SenderAccNumber("senderAccNumber"),
	//SenderAccName("senderAccName"),
	ReceiverAccNumber("receiverAccNumber"),
	ReceiverName("receiverName"),
	Description("description"),
	Amount("amount"),
	Currency("currency"),
	ExecutionDate("executionDate");

	private String fieldName;
	SetRequestFields(String fieldName) {
		this.fieldName = fieldName;
	}
	public String text() {
		return fieldName;
	}
}
	enum Fields{
		Code("code"),
		Status("Status"),
		Ok("ok"),
		Failed("failed"),
		ErrorMsg("errorMsg"),
		ExpirationTime("expirationTime"),
		TransferData("transferData"),
		Data("data");

		private String fieldName;
		Fields(String fieldName) {
			this.fieldName = fieldName;
		}
		public String text() {
			return fieldName;
		}
	}

	static String setCodeRequestParsingErrorStr(String input){
		try {
			JSONObject obj = new JSONObject(input).getJSONObject(Fields.Data.text());
			obj.getString(SetRequestFields.ReceiverName.text());
			obj.getString(SetRequestFields.ReceiverAccNumber.text());
			obj.getString(SetRequestFields.ReceiverName.text());
			obj.getString(SetRequestFields.Description.text());
			obj.getDouble(SetRequestFields.Amount.text());
			obj.getString(SetRequestFields.Currency.text());
			obj.getString(SetRequestFields.ExecutionDate.text());
		}catch (Exception e){
			return e.toString();
		}
		return null;
	}
	static String getCodeParsingRequestErrorStr(String input){
		try {
			int code = new JSONObject(input).getInt(Fields.Code.text());
			return null;
		}catch (Exception e){
			return e.toString();
		}
	}
	static int getCodeParsingOK(String input){
		return new JSONObject(input).getInt(Fields.Code.text());
	}

	static JSONObject setCodeResultOk(int code){
		JSONObject toRet = new JSONObject();
		toRet.put(Fields.Status.text(), Fields.Ok.text());
		toRet.put(Fields.Code.text(), String.valueOf(code));
		return toRet;
	}
	static JSONObject setCodeResultFail(String errorCode){
		JSONObject toRet = new JSONObject();
		toRet.put(Fields.Status.text(), Fields.Failed.text());
		toRet.put(Fields.ErrorMsg.text(), errorCode);
		return toRet;
	}

	static JSONObject getCodeResultOk(JSONObject data){
		try {
			JSONObject toRet = new JSONObject(data);
			toRet.put(Fields.Status.text(), Fields.Ok.text());
			return toRet;
		}catch (Exception e){
			return getCodeResultFailed(e.toString());
		}
	}
	static JSONObject getCodeResultFailed(String errorMsg){
		JSONObject toRet = new JSONObject();
		toRet.put(Fields.Status.text(), Fields.Failed.text());
		toRet.put(Fields.ErrorMsg.text(), errorMsg);
		return toRet;
	}
}

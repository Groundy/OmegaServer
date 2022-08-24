package com.example.OmegaServer;

import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

enum ReturnCode{
	OK(200),
	BadRequest(400),

	ServerError(500),
	CodeExpired(501),
	CodeNotExist(502),
	CodeAlreadyDone(503),
	CodeUsed(504),
	CodeMultipleUse(505);

	private int code;
	ReturnCode(int code) {
		this.code = code;
	}
	public int code() {
		return code;
	}
}

@RequestMapping
@RestController
public class Controller {
	DataService dataService = new DataService();

	@PostMapping(value = "/setCode", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> setCode(@RequestBody String requestJsonBody){
		return setCodeBody(requestJsonBody);
	}

	@PostMapping("/getCode")
	public ResponseEntity<String> getCodeData(@RequestBody String requestJsonBody){
		return getCodeDataBody(requestJsonBody);
	}

	@PostMapping("/getInfo")
	public ResponseEntity<String> getInfo(){
		return getInfoBody();
	}

	@PostMapping("/waitCodeDone")
	public ResponseEntity<String> waitCodeDone(@RequestBody String requestJsonBody) throws InterruptedException {
		return waitCodeDoneBody(requestJsonBody);
	}

	@PostMapping("/codeDone")
	public ResponseEntity<String> codeDone(@RequestBody String requestJsonBody){
		return codeDoneBody(requestJsonBody);
	}



	ResponseEntity<String> setCodeBody(String requestJsonBody){
		ReturnCode returnCode = Parsers.setCodeRequestParsingErrorStr(requestJsonBody);
		if(returnCode != ReturnCode.OK){
			String badJsonStr = Parsers.getFailureResponse(ReturnCode.BadRequest).toString();
			return ResponseEntity.status(ReturnCode.BadRequest.code()).body(badJsonStr);
		}

		Integer code = dataService.addRecord(requestJsonBody);
		if(code == null){
			String badJsonStr = Parsers.getFailureResponse(ReturnCode.ServerError).toString();
			return ResponseEntity.status(ReturnCode.ServerError.code()).body(badJsonStr);
		}

		String expirationTimeStr = dataService.getExpirationTimeFromRecord(code);
		String okJsonStr = Parsers.setCodeResultOk(code, expirationTimeStr).toString();
		return ResponseEntity.ok(okJsonStr);
	}
	ResponseEntity<String> getCodeDataBody(String requestJsonBody){
		Integer requestCode = Parsers.getCodeFromRequest(requestJsonBody);
		if(requestCode == null){
			String badJsonStr = Parsers.getFailureResponse(ReturnCode.BadRequest).toString();
			return ResponseEntity.badRequest().body(badJsonStr);
		}

		ReturnCode responseCode = dataService.getDataFromCode(requestCode);
		if(responseCode != ReturnCode.OK){
			String badJsonStr = Parsers.getFailureResponse(responseCode).toString();
			return ResponseEntity.badRequest().body(badJsonStr);
		}

		JSONObject data = dataService.getResponseFromCode(requestCode);
		if(data==null){
			String badJsonStr = Parsers.getFailureResponse(ReturnCode.ServerError).toString();
			return ResponseEntity.badRequest().body(badJsonStr);
		}

		JSONObject responseJson = dataService.getResponseFromCode(requestCode);
		if(responseJson == null){
			String badJsonStr = Parsers.getFailureResponse(ReturnCode.ServerError).toString();
			return ResponseEntity.badRequest().body(badJsonStr);
		}
		return ResponseEntity.ok(responseJson.toString());
	}
	ResponseEntity<String> getInfoBody(){
		String info = dataService.getListStatus();
		return ResponseEntity.ok(info);
	}
	ResponseEntity<String> codeDoneBody(String requestJsonBody){
		Integer requestCode = Parsers.getCodeFromCodeDoneRequest(requestJsonBody);
		if(requestCode == null){
			String badJsonStr = Parsers.getFailureResponse(ReturnCode.BadRequest).toString();
			return ResponseEntity.badRequest().body(badJsonStr);
		}

		dataService.markRecordAsDone(requestCode);
		JSONObject response = new JSONObject();
		response.put(Parsers.ResponseFields.Status.text(), Parsers.ResponseFields.Ok.text());

		return ResponseEntity.ok(response.toString());
	}
	ResponseEntity<String> waitCodeDoneBody(String requestJsonBody) throws InterruptedException {
		Integer requestCode = Parsers.getCodeFromCodeDoneRequest(requestJsonBody);
		if(requestCode == null){
			String badJsonStr = Parsers.getFailureResponse(ReturnCode.CodeAlreadyDone).toString();
			return ResponseEntity.badRequest().body(badJsonStr);
		}

		Record record = dataService.getRecordByCode(requestCode);
		if(record.used){
			String badJsonStr = Parsers.getFailureResponse(ReturnCode.CodeAlreadyDone).toString();
			return ResponseEntity.badRequest().body(badJsonStr);
		}
		if(record.multipleUse){
			String badJsonStr = Parsers.getFailureResponse(ReturnCode.CodeMultipleUse).toString();
			return ResponseEntity.badRequest().body(badJsonStr);
		}

		while (record.getSecondsToExpiration() > 0){
			if(record.used) {
				JSONObject responseOk = new JSONObject();
				responseOk.put(Parsers.ResponseFields.Status.text(), Parsers.ResponseFields.Ok.text());
				return ResponseEntity.ok(responseOk.toString());
			}
			else
				Thread.sleep(2000);
		}
		String badJsonStr = Parsers.getFailureResponse(ReturnCode.CodeExpired).toString();
		return ResponseEntity.badRequest().body(badJsonStr);
	}
}

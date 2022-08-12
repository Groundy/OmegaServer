package com.example.OmegaServer;

import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

enum ReturnCode{
	OK(200),
	BadRequest(400),
	CodeExpired(501),
	CodeNotExist(502),
	ServerError(500);

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

	@PostMapping("/test")
	public ResponseEntity<String> getInfo(){
		return getInfoBody();
	}


	ResponseEntity<String> setCodeBody(String requestJsonBody){
		ReturnCode returnCode = Parsers.setCodeRequestParsingErrorStr(requestJsonBody);
		if(returnCode != ReturnCode.OK){
			String badJsonStr = Parsers.getFailureResponse(ReturnCode.BadRequest).toString();
			return ResponseEntity.status(ReturnCode.BadRequest.code()).body(badJsonStr);
		}

		int code = dataService.addRecord(requestJsonBody);
		if(code == -1){
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

}

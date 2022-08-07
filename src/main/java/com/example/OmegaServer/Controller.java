package com.example.OmegaServer;

import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

enum ReturnCode{
	OK(200),
	BadRequest(400),
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
	public ResponseEntity<String> getCode(@RequestBody String requestJsonBody){
		return getCodeBody(requestJsonBody);
	}



	ResponseEntity<String> setCodeBody(String requestJsonBody){
		var errorStr = Parsers.setCodeRequestParsingErrorStr(requestJsonBody);
		if(errorStr != null){
			String badJsonStr = Parsers.setCodeResultFail(errorStr).toString();
			int retCode = ReturnCode.BadRequest.code();
			ResponseEntity<String> response = ResponseEntity.status(retCode).body(badJsonStr);
			return response;
		}

		int code = dataService.addRecord(requestJsonBody);
		if(code == -1){
			String badJsonStr = Parsers.setCodeResultFail("Good request but still error").toString();
			int retCode = ReturnCode.ServerError.code();
			ResponseEntity<String> response = ResponseEntity.status(retCode).body(badJsonStr);
			return response;
		}


		String okJsonStr = Parsers.setCodeResultOk(code).toString();
		return ResponseEntity.ok(okJsonStr);
	}
	ResponseEntity<String> getCodeBody(String requestJsonBody){
		String errorStr = Parsers.getCodeParsingRequestErrorStr(requestJsonBody);
		if(errorStr != null){
			String badJsonStr = Parsers.getCodeResultFailed(errorStr).toString();
			ResponseEntity<String> response = ResponseEntity.badRequest().body(badJsonStr);
			return response;
		}
		int requestCode = Parsers.getCodeParsingOK(requestJsonBody);

		errorStr = dataService.getDataFromCodeErrorStr(requestCode);
		if(errorStr != null){
			String badJsonStr = Parsers.getCodeResultFailed(errorStr).toString();
			ResponseEntity<String> response = ResponseEntity.badRequest().body(badJsonStr);
			return response;
		}

		JSONObject data = dataService.getResponseFromCode(requestCode);
		if(data==null){
			String badJsonStr = Parsers.getCodeResultFailed("Good request but still error").toString();
			ResponseEntity<String> response = ResponseEntity.badRequest().body(badJsonStr);
			return response;
		}

		String strToRet = dataService.getResponseFromCode(requestCode).toString();
		return ResponseEntity.ok(strToRet);
	}











/*
	@PostMapping("/codeUsed")
	public String a3(){
		//todo
		return "";
	}
	*/

	@PostMapping("/test")
	public ResponseEntity<String> a3(){
		String info = dataService.getListStatus();
		return ResponseEntity.ok(info);
	}
}

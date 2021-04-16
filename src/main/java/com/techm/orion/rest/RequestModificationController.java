package com.techm.orion.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.techm.orion.entitybeans.RequestInfoEntity;
import com.techm.orion.repositories.RequestInfoDetailsRepositories;
import com.techm.orion.service.RequestModificationService;
@RestController
@RequestMapping("/RequestModify")
public class RequestModificationController {
	private static final Logger logger = LogManager.getLogger(RequestModificationController.class);
	
	@Autowired
	RequestModificationService modifyService;
	
	@Autowired
	private RequestInfoDetailsRepositories repository;
	
	@POST
	@RequestMapping(value = "/getModifyRequestDetails", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public JSONObject getFeatureForRequestId(@RequestBody String request) {
		try {
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(request);
			String requestId = json.get("requestId").toString();
			String hostName = json.get("hostName").toString();
			String version = json.get("requestVersion").toString();
			String templateId = json.get("templateId").toString();

			return modifyService.getModifyRequestDetails(requestId,hostName,version,templateId);
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
	
	@GET
	@RequestMapping(value = "/requestDropDown", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<JSONObject> requestDropDown(@RequestParam String requestId) throws Exception {
		JSONObject json = new JSONObject();
		JSONArray array = new JSONArray();
		List<RequestInfoEntity> requestEntity = repository.findAllByAlphanumericReqId(requestId);
		requestEntity.forEach(entity -> {
			JSONObject object = new JSONObject();
			object.put("id", entity.getRequestVersion());
			array.add(object);
		});
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("response", array);
		return new ResponseEntity<JSONObject>(jsonObject, HttpStatus.OK);
	}
	}


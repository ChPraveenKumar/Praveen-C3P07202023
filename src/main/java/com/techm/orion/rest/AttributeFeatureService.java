package com.techm.orion.rest;

import java.util.List;
import java.util.Observer;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.techm.orion.models.TemplateAttributeJSONModel;
import com.techm.orion.service.AttributeFeatureNewService;

@Controller
@RequestMapping("/AttributeFeatureService")
public abstract class AttributeFeatureService implements Observer {

	@POST
	@RequestMapping(value = "/getFeaturesForAttributes", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public Response getFeaturesForAttributes(@RequestBody String configRequest) {

		JSONObject obj = new JSONObject();
		String jsonArray = "";
		JSONArray array = new JSONArray();
		JSONObject jsonObj;
		try {

			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(configRequest);
			AttributeFeatureNewService attributeFeatureNewService = new AttributeFeatureNewService();
			TemplateAttributeJSONModel templateAttributeJSONModel = new TemplateAttributeJSONModel();
			templateAttributeJSONModel.setAttributename(json.get("Attributename").toString());
			templateAttributeJSONModel.setAttributeFeature(json.get("Attributefeature").toString());
			templateAttributeJSONModel.setTemplateId(json.get("templateId").toString());
			List<String> attriList = attributeFeatureNewService.getAttributeListSuggestion(templateAttributeJSONModel);

			if (attriList.size() > 0) {
				for (int i = 0; i < attriList.size(); i++) {
					jsonObj = new JSONObject();
					jsonObj.put("value", attriList.get(i));

					array.put(jsonObj);
				}

			}
			obj.put(new String("attriList"), jsonArray);
		} catch (Exception e) {
			System.out.println(e);
		}
		return Response.status(200).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").entity(obj).build();

	}

}
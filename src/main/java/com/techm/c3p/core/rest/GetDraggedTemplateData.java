package com.techm.c3p.core.rest;

import java.util.Observable;
import java.util.Observer;

import javax.ws.rs.POST;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.techm.c3p.core.service.TemplateManagementDetailsService;


@Controller
@RequestMapping("/GetDraggedTemplateData")
public class GetDraggedTemplateData implements Observer {
	private static final Logger logger = LogManager.getLogger(GetDraggedTemplateData.class);
	
	@Autowired
	private TemplateManagementDetailsService service;

	/**
	 *This Api is marked as ***************c3p-ui Api Impacted****************
	 **/
	@POST
	@RequestMapping(value = "/getData", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public Response getAll(@RequestBody String response) {

		JSONObject obj = new JSONObject();
		
		String confId=null,dragId=null,dragData=null,templateID=null;

		try {
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(response);
			confId=json.get("confId").toString();
			dragId=json.get("dragId").toString();
			dragData=json.get("dragData").toString();
			templateID=json.get("templateID").toString();
			
			boolean saveincommandlist=service.savenewfeatureinCommandList(confId,dragId,dragData,templateID);
			if(saveincommandlist)
			{
			}

			obj.put(new String("output"), "");

		} catch (Exception e) {
			logger.error(e);
		}

		return Response
				.status(200)
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Headers",
						"origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods",
						"GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").entity(obj)
				.build();
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub

	}

}

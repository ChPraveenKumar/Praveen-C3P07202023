package com.techm.orion.rest;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.POST;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.techm.orion.entitybeans.MasterCharacteristicsEntity;
import com.techm.orion.entitybeans.MasterFeatureEntity;
import com.techm.orion.entitybeans.ResourceCharacteristicsEntity;
import com.techm.orion.pojo.ServiceRequestPojo;
import com.techm.orion.repositories.MasterCharacteristicsRepository;
import com.techm.orion.repositories.MasterFeatureRepository;
import com.techm.orion.repositories.ResourceCharacteristicsRepository;
import com.techm.orion.service.DeviceRequestService;

@RestController
public class DeviceRequestController {

	private static final Logger logger = LogManager.getLogger(DeviceDiscoveryController.class);

	@Autowired
	private DeviceRequestService service;
	
	@Autowired
	private ResourceCharacteristicsRepository resourcecharateristicRepo;
	
	@Autowired
	private MasterFeatureRepository masterfeatureRepo;
	
	@Autowired
	private MasterCharacteristicsRepository masterCharacteristicRepo;
	
	@POST
	@RequestMapping(value = "/getConfigRequest", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public Response getConfigRequest(@RequestBody String request) {
		List<ServiceRequestPojo> requestDeatils = null;
		try {
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(request);
			String hostName = null;
			String requestType = null;
			if (json.containsKey("hostName")) {
				hostName = json.get("hostName").toString();
			}
			if (json.containsKey("requestType")) {
				requestType = json.get("requestType").toString();
			}
			if (hostName != null) {
				requestDeatils = service.getConfigServiceRequest(hostName, requestType);
			}

		} catch (Exception e) {
			logger.error(e);
		}
		return Response.status(200).entity(requestDeatils).build();
	}
	@POST
	@RequestMapping(value = "/getConfigFeatures", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public Response getConfigFeatures(@RequestBody String request) {
		List<String>featureids=null;
		JSONArray output=new JSONArray();
		try {
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(request);
			String hostName = null;
			String requestType = null;
			if (json.containsKey("hostName")) {
				hostName = json.get("hostName").toString();
			}
			if (json.containsKey("requestType")) {
				requestType = json.get("requestType").toString();
			}
			if (hostName != null) {
				
				featureids=new ArrayList<String>();
				featureids=resourcecharateristicRepo.findDistinctFeaturesForHostname(hostName);
				List<ResourceCharacteristicsEntity>listOfCharacteristics;
				if(featureids!=null || !featureids.isEmpty())
				{
				for(String fid: featureids)
				{
					JSONObject feature=new JSONObject();
					feature.put("featureId", fid);
					MasterFeatureEntity featureFromDB=masterfeatureRepo.findByFId(fid);
					feature.put("featureName", featureFromDB.getfName());
					feature.put("featureCreatedDate",featureFromDB.getfCreatedDate().toString()) ;
					feature.put("featureUpdatedDate", featureFromDB.getfUpdatedDate().toString());

					listOfCharacteristics=resourcecharateristicRepo.findByRcFeatureIdAndRcDeviceHostname(fid, hostName);
					JSONArray charachteristicArray=new JSONArray();
					for(ResourceCharacteristicsEntity characteristic: listOfCharacteristics)
					{
						//Find charachteristic name
						JSONObject characteristicObj=new JSONObject();
						characteristicObj.put("characteristicName", characteristic.getRcCharacteristicName());
						characteristicObj.put("characteristicValue", characteristic.getRcCharacteristicValue());
						characteristicObj.put("characteristicCreatedDate", characteristic.getRc_created_date().toString());
						characteristicObj.put("characteristicUpdatedDate", characteristic.getRc_updated_date().toString());
						charachteristicArray.add(characteristicObj);
						
					}
					feature.put("charachteristics", charachteristicArray);
					output.add(feature);
				}
				}
			}

		} catch (Exception e) {
			logger.error(e);
		}
		return Response.status(200).entity(output).build();
	}
	
}

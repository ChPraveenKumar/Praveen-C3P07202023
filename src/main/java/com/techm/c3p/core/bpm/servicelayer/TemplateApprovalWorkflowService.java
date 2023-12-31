package com.techm.c3p.core.bpm.servicelayer;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.techm.c3p.core.dao.TemplateManagementDao;
import com.techm.c3p.core.entitybeans.MasterFeatureEntity;
import com.techm.c3p.core.entitybeans.Notification;
import com.techm.c3p.core.entitybeans.TemplateFeatureEntity;
import com.techm.c3p.core.pojo.SearchParamPojo;
import com.techm.c3p.core.pojo.TemplateBasicConfigurationPojo;
import com.techm.c3p.core.repositories.MasterFeatureRepository;
import com.techm.c3p.core.repositories.NotificationRepo;
import com.techm.c3p.core.repositories.TemplateFeatureRepo;
import com.techm.c3p.core.rest.GetTemplateConfigurationData;
import com.techm.c3p.core.utility.WAFADateUtil;

@Controller
@RequestMapping("/createTemplate")
public class TemplateApprovalWorkflowService {
	private static final Logger logger = LogManager.getLogger(TemplateApprovalWorkflowService.class);

	@Autowired
	private MasterFeatureRepository masterFeatureRepository;
	
	@Autowired
	private TemplateFeatureRepo templateFeatureRepo;
	
	@Autowired
	private NotificationRepo notificationRepo;
	
	@Autowired
	private WAFADateUtil timeUtil;
	
	@Autowired
	private GetTemplateConfigurationData getTemplateConfigurationData;
	
	@Autowired
	private TemplateManagementDao templateManagementDao;
	
	@Autowired
	private CamundaServiceTemplateApproval camundaServiceTemplateApproval;
	
	
	@POST
	@RequestMapping(value = "/saveTemplate", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public ResponseEntity<JSONObject> saveTemplate(@RequestBody String string) {
		JSONParser parser = new JSONParser();
		String templateId = null, templateVersion = null;
		ResponseEntity<JSONObject> response = null;
		DecimalFormat numberFormat = new DecimalFormat("#.0");

		try {
			JSONObject json = (JSONObject) parser.parse(string);
			templateId = json.get("templateid").toString();
			templateId = templateId.replace("-", "_");
			if (json.get("templateVersion") != null) {
				templateVersion = numberFormat.format(Double.parseDouble(json.get("templateVersion").toString()));
			} else {
				templateVersion = templateId.substring(templateId.indexOf("_V")+2, templateId.length());
				templateId = templateId.substring(0, templateId.indexOf("_V"));
			}
			response = getTemplateConfigurationData.saveConfigurationTemplate(string, templateId, templateVersion);
			camundaServiceTemplateApproval.initiateApprovalFlow(templateId, templateVersion, "Admin");
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@POST
	@RequestMapping(value = "/updateTemplateStatus", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public Response updateTemplateStatus(@RequestBody String string) {
		JSONObject obj = new JSONObject();		
		JSONParser parser = new JSONParser();
		String templateId = null, templateVersion = null, status = null, approverComment = null,featureID=null, featureVersion=null;
		String userTaskId = null, userName = null;
		DecimalFormat numberFormat = new DecimalFormat("#.0");
		Date date = new Date();
		Timestamp timestamp = new Timestamp(date.getTime());
		Calendar cal = Calendar.getInstance();

		int response = 0, notifId = 0;
		try {
			JSONObject json = (JSONObject) parser.parse(string);
			if(json.get("notif_id") != null)
				notifId = Integer.parseInt(json.get("notif_id").toString());
			if (json.get("userName") != null) 
				userName = json.get("userName").toString();
			
			Notification notificationData = notificationRepo.findById(notifId);
			if(json.containsKey("status") && json.get("status")!=null  && !json.get("status").toString().isEmpty())
			{
				
				status = json.get("status").toString();
			}
			else
			{
				status="";
			}
			if(json.containsKey("comment") && json.get("comment")!=null && !json.get("comment").toString().isEmpty())
			{
				String timeStamp="00-00-0000 00:00:00";
				if(json.containsKey("timezone"))
				{
					if(json.get("timezone")!=null)
					{
					timeStamp=timeUtil.currentDateTimeFromUserTimeZoneToServerTimzeZone(json.get("timezone").toString());
					}
					else
					{
					timeStamp=timeUtil.currentDateTime();
					}
				}
				else
				{
					timeStamp=timeUtil.currentDateTime();
				}
				String varComment = timeStamp+" "+userName + " : " +json.get("comment").toString().concat("\n");
				approverComment = varComment;
			}
			else
			{
				approverComment="";
			}
			if (Boolean.parseBoolean(json.get("isTemplate").toString())) {
				templateId = json.get("templateid").toString().replace("-", "_");
				String templateidForFeatureExtraction=templateId;
				if (json.get("templateVersion") != null) {
					templateVersion = (json.get("templateVersion").toString());
				} else {
					templateVersion = templateId.substring(templateId.indexOf("_V")+2, templateId.length());
					templateId = templateId.substring(0, templateId.indexOf("_V"));

				}
				
				//get feature id based of command type
				List<TemplateFeatureEntity> listFeatures = templateFeatureRepo
						.findMasterFIdByCommand(templateidForFeatureExtraction);

				for (TemplateFeatureEntity feature : listFeatures) {
					MasterFeatureEntity masterFeature = masterFeatureRepository.findByFId(feature.getMasterFId());
					if ("Pending".equalsIgnoreCase(masterFeature.getfStatus())) {
						masterFeatureRepository.updateMasterFeatureStatus(json.get("status").toString(),
								approverComment, notificationData.getNotifFromUser(), userName,
								Timestamp.valueOf(LocalDateTime.now()), feature.getMasterFId(), "1.0");
					}
				}
				response = templateManagementDao.updateTemplateStatus(templateId, templateVersion, status,
						approverComment);
				userTaskId = templateManagementDao
						.getUserTaskIdForTemplate(json.get("templateId").toString().replace("-", "_"), templateVersion);
				camundaServiceTemplateApproval.completeApprovalFlow(userTaskId, status, approverComment);
			}
			else
			{
				/*In case of feature*/
				featureID=json.get("featureid").toString();
				
				MasterFeatureEntity entity=masterFeatureRepository.findByFId(featureID);
				String comment=null;
				String str=entity.getfComments();
				if(str!=null)
				{
					comment=entity.getfComments().concat(approverComment);

				}
				else
				{
					comment=approverComment;
				}
				featureVersion = numberFormat.format(Double.parseDouble(json.get("featureversion").toString()));
				
				response=masterFeatureRepository.updateMasterFeatureStatus(status, comment , notificationData.getNotifFromUser(), userName,Timestamp.valueOf(LocalDateTime.now()), featureID, featureVersion);
				
				userTaskId = templateManagementDao.getUserTaskIdForTemplate(featureID, featureVersion);
				
				
				camundaServiceTemplateApproval.completeApprovalFlow(userTaskId, status, approverComment);
				
			}
			notificationData.setNotifStatus("Completed");
			notificationData.setNotifCompletedby(userName);
			notificationRepo.save(notificationData);
			Notification newNotification = new Notification();
			newNotification.setNotifFromUser(notificationData.getNotifCompletedby());
			newNotification.setNotifToUser(notificationData.getNotifFromUser());
			newNotification.setNotifType(notificationData.getNotifType());
			newNotification.setNotifCreatedDate(timestamp);
			newNotification.setNotifReference(notificationData.getNotifReference());
			newNotification.setNotifLabel(notificationData.getNotifReference() +" : " + status);
			newNotification.setNotifMessage(status);
			newNotification.setNotifPriority("1");
			newNotification.setNotifStatus("Pending");
			cal.setTimeInMillis(timestamp.getTime());
		    cal.add(Calendar.DAY_OF_MONTH, 30);
		    timestamp = new Timestamp(cal.getTime().getTime());
			newNotification.setNotifExpiryDate(timestamp);
			notificationRepo.save(newNotification);
			// camundaService.initiateApprovalFlow(templateId, templateVersion, "Admin");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (response >= 1) {
			obj.put(new String("Status"), "success");
		} else {
			obj.put(new String("Status"), "failure");

		}
		return Response.status(200).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").entity(obj).build();
	}

	@SuppressWarnings("unchecked")
	@POST
	@RequestMapping(value = "/search", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public Response search(@RequestBody String searchParameters) {

		JSONObject obj = new JSONObject();
		String jsonArray = "";
		String key = null, value = null;
		try {
			Gson gson = new Gson();
			SearchParamPojo dto = gson.fromJson(searchParameters, SearchParamPojo.class);
			key = dto.getKey();
			value = dto.getValue();
			List<TemplateBasicConfigurationPojo> detailsList = new ArrayList<TemplateBasicConfigurationPojo>();
			if (value != null && !value.isEmpty()) {
				try {
					// quick fix for json not getting serialized

					detailsList = templateManagementDao.searchResults(key, value);

					jsonArray = new Gson().toJson(detailsList);
					obj.put(new String("output"), jsonArray);
					obj.put(new String("Result"), "success");

				} catch (Exception e) {
					logger.error(e);
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}

		return Response.status(200).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").entity(obj).build();
	}

}

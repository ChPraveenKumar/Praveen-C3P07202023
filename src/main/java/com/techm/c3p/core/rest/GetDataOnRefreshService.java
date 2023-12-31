package com.techm.c3p.core.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.techm.c3p.core.dao.RequestInfoDetailsDao;
import com.techm.c3p.core.dao.TemplateManagementDao;
import com.techm.c3p.core.pojo.RequestInfoCreateConfig;
import com.techm.c3p.core.pojo.TemplateBasicConfigurationPojo;

@Controller
@RequestMapping("/GetNotificationsService")
public class GetDataOnRefreshService {
	private static final Logger logger = LogManager.getLogger(GetDataOnRefreshService.class);
	@Autowired
	private RequestInfoDetailsDao requestInfoDao;
	@Autowired
	private TemplateManagementDao templateManagementDao;

	/**
	 *This Api is marked as ***************c3p-ui Api Impacted****************
	 **/
	@SuppressWarnings("unchecked")
	@GET
	@RequestMapping(value = "/get", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public Response getAll() {
		logger.info("in Refresh");
		JSONObject obj = new JSONObject();
		int notificationCount = 0;
		List<TemplateBasicConfigurationPojo> list = null;
		String jsonList, templateNameList;
		int totalCount = 0;

		List<TemplateBasicConfigurationPojo> templateNames = new ArrayList<TemplateBasicConfigurationPojo>();
		List<RequestInfoCreateConfig> requestList = new ArrayList<RequestInfoCreateConfig>();

		String user = "feuser";
		String jsonFeRequestList = "";
		switch (user) {
		case "feuser":
			requestList = requestInfoDao.getOwnerAssignedRequestList("feuser");
			int numberOfNotificationsForFE = 0;
			List<RequestInfoCreateConfig> feRequestListNum = new ArrayList<RequestInfoCreateConfig>();
			requestList.forEach(request -> {
				if (request.getReadFE() == true) {
					feRequestListNum.add(request);
				}
			});

			numberOfNotificationsForFE = feRequestListNum.size();
			totalCount = notificationCount + numberOfNotificationsForFE;
			jsonList = new Gson().toJson(list);
			jsonFeRequestList = new Gson().toJson(requestList);
			templateNameList = new Gson().toJson(templateNames);
			obj.put(new String("TemplateList"), templateNameList);
			obj.put(new String("NotificationCount"), totalCount);

			obj.put(new String("SENotificationCount"), 0);
			obj.put(new String("SERequestDetailedList"), "");

			obj.put(new String("TemplateNotificationCount"), 0);
			obj.put(new String("TemplateDetailedList"), "");

			obj.put(new String("TemplateNotificationCount"), notificationCount);
			obj.put(new String("FENotificationCount"), numberOfNotificationsForFE);
			obj.put(new String("FERequestDetailedList"), jsonFeRequestList);
			break;
		case "seuser":
			requestList = requestInfoDao.getOwnerAssignedRequestList("seuser");
			int numberOfNotificationsForSE = 0;
			List<RequestInfoCreateConfig> seRequestListNum = new ArrayList<RequestInfoCreateConfig>();
			for (int i = 0; i < requestList.size(); i++) {
				if (requestList.get(i).getReadSE() == false) {
					seRequestListNum.add(requestList.get(i));
				}
			}
			numberOfNotificationsForSE = seRequestListNum.size();
			totalCount = notificationCount + numberOfNotificationsForSE;
			jsonList = new Gson().toJson(list);
			jsonFeRequestList = new Gson().toJson(requestList);

			templateNameList = new Gson().toJson(templateNames);
			obj.put(new String("TemplateList"), templateNameList);
			obj.put(new String("TemplateNotificationCount"), notificationCount);
			obj.put(new String("SENotificationCount"), numberOfNotificationsForSE);
			obj.put(new String("NotificationCount"), totalCount);

			obj.put(new String("FENotificationCount"), 0);
			obj.put(new String("FERequestDetailedList"), "");

			obj.put(new String("TemplateNotificationCount"), 0);
			obj.put(new String("TemplateDetailedList"), "");

			obj.put(new String("SERequestDetailedList"), jsonFeRequestList);

			break;

		default:
			notificationCount = templateManagementDao.getNumberOfTemplatesForApprovalForLoggedInUser(user);
			list = new ArrayList<TemplateBasicConfigurationPojo>();
			list = templateManagementDao.getTemplatesForApprovalForLoggedInUser(user);
			for (int i = 0; i < list.size(); i++) {
				TemplateBasicConfigurationPojo temp = new TemplateBasicConfigurationPojo();
				temp.setTemplateId(list.get(i).getTemplateId());
				temp.setVersion(list.get(i).getVersion());
				temp.setStatus(list.get(i).getStatus());
				temp.setRead(list.get(i).getRead());
				temp.setEditable(list.get(i).isEditable());
				templateNames.add(temp);
			}

			jsonList = new Gson().toJson(list);
			jsonFeRequestList = "undefined";
			totalCount = notificationCount;

			templateNameList = new Gson().toJson(templateNames);
			obj.put(new String("TemplateDetailedList"), jsonList);
			obj.put(new String("TemplateList"), templateNameList);

			obj.put(new String("TemplateNotificationCount"), notificationCount);
			obj.put(new String("NotificationCount"), totalCount);

			obj.put(new String("FENotificationCount"), 0);
			obj.put(new String("FERequestDetailedList"), "");

			obj.put(new String("SENotificationCount"), 0);
			obj.put(new String("SERequestDetailedList"), "");

			break;
		}

		return Response.status(200).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").entity(obj).build();
	}

}

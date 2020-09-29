package com.techm.orion.rest;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.techm.orion.dao.RequestInfoDao;
import com.techm.orion.entitybeans.BatchIdEntity;
import com.techm.orion.entitybeans.DeviceDiscoveryEntity;
import com.techm.orion.entitybeans.FirmwareUpgradeSingleDeviceEntity;
import com.techm.orion.entitybeans.RequestDetailsBackUpAndRestoreEntity;
import com.techm.orion.entitybeans.RequestDetailsEntity;
import com.techm.orion.entitybeans.RequestInfoEntity;
import com.techm.orion.entitybeans.SiteInfoEntity;
import com.techm.orion.entitybeans.VendorDetails;
import com.techm.orion.models.BackUpRequestVersioningJSONModel;
import com.techm.orion.pojo.BatchPojo;
import com.techm.orion.pojo.CreateConfigRequestDCM;
import com.techm.orion.pojo.FirmwareUpgradeDetail;
import com.techm.orion.pojo.Global;
import com.techm.orion.pojo.RequestInfoPojo;
import com.techm.orion.pojo.SearchParamPojo;
import com.techm.orion.repositories.BatchInfoRepo;
import com.techm.orion.repositories.DeviceDiscoveryRepository;
import com.techm.orion.repositories.DeviceInterfaceRepo;
import com.techm.orion.repositories.FirmUpgradeSingleDeviceRepository;
import com.techm.orion.repositories.InternetInfoRepo;
import com.techm.orion.repositories.RequestDetailsBackUpAndRestoreRepo;
import com.techm.orion.repositories.RequestDetailsImportRepo;
import com.techm.orion.repositories.RequestInfoDetailsRepositories;
import com.techm.orion.repositories.RouterVfRepo;
import com.techm.orion.repositories.SiteInfoRepository;
import com.techm.orion.repositories.VendorDetailsRepository;
import com.techm.orion.repositories.WebServiceRepo;
import com.techm.orion.service.DcmConfigService;

@Controller
@RequestMapping("/BackUpConfigurationAndTest")
public class BackUpAndRestoreController {

	private static final Logger logger = LogManager.getLogger(BackUpAndRestoreController.class);

	public static String TSA_PROPERTIES_FILE = "TSA.properties";
	public static final Properties TSA_PROPERTIES = new Properties();

	List<String> files = new ArrayList<String>();

	@Autowired
	public RequestDetailsImportRepo requestDetailsImportRepo;

	@Autowired
	public DeviceInterfaceRepo deviceInterfaceRepo;

	@Autowired
	public InternetInfoRepo internetInfoRepo;

	@Autowired
	public RouterVfRepo routerVfRepo;

	@Autowired
	public WebServiceRepo webServiceRepo;

	@Autowired
	public VendorDetailsRepository vendorDetailsRepo;

	@Autowired
	public RequestDetailsBackUpAndRestoreRepo requestDetailsBackUpAndRestoreRepo;

	@Autowired
	DcmConfigService dcmConfigService;

	@Autowired
	ConfigMngmntService myObj;

	@Autowired
	public DeviceDiscoveryRepository deviceDiscoveryRepository;

	@Autowired
	public RequestInfoDetailsRepositories requestInfoDetailsRepositories;

	@Autowired
	RequestInfoDao dao;

	@Autowired
	public BatchInfoRepo batchInfoRepo;

	@Autowired
	public SiteInfoRepository siteInfoRepository;
	
	@Autowired
	FirmUpgradeSingleDeviceRepository firmUpgradeSingleDeviceRepository;
	
	@Autowired
	DeviceDiscoveryRepository deviceInforepo;

	@SuppressWarnings("unchecked")
	@POST
	@RequestMapping(value = "/getVendorCheck", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public Response getVendorStatus(@RequestBody String request) {

		JSONObject obj = new JSONObject();
		JSONParser parser = new JSONParser();
		String vendorCheck = null, str = null;
		List<VendorDetails> vendorList = new ArrayList<VendorDetails>();

		try {
			obj = (JSONObject) parser.parse(request);

			vendorCheck = obj.get("Vendor").toString();

			vendorList = vendorDetailsRepo.findByVendor(vendorCheck);

			for (int i = 0; i < vendorList.size(); i++) {

				if (vendorList.get(i).getVendor().contains(vendorCheck)) {
					str = "true";
				}

			}

			if (vendorList.isEmpty()) {
				str = "false";
			}

		} catch (Exception e) {
			logger.error(e);
		}

		return Response.status(200).entity(str).build();
	}

	@SuppressWarnings("unchecked")
	@POST
	@RequestMapping(value = "/getManagementIP", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public Response getManagementIP(@RequestBody String request) {

		JSONObject obj = new JSONObject();
		JSONParser parser = new JSONParser();
		String hostName = null, str = null;

		List<RequestDetailsEntity> hostNameList = new ArrayList<RequestDetailsEntity>();

		try {
			obj = (JSONObject) parser.parse(request);

			hostName = obj.get("Hostname").toString();

			hostNameList = requestDetailsImportRepo.findByHostname(hostName);

			for (int i = 0; i < hostNameList.size(); i++) {

				if (hostNameList.get(i).getHostname().contains(hostName)) {
					str = hostName;
				}

			}

			if (hostNameList.isEmpty()) {
				str = "This hostName is not supported. Please contact system admin";
			}

		} catch (Exception e) {
			logger.error(e);
		}

		return Response.status(200).entity(str).build();
	}

	@SuppressWarnings("unchecked")
	@POST
	@RequestMapping(value = "/getBackUpSRList", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public Response getBackUpSRList() {

		JSONObject obj = new JSONObject();
		String jsonArray = "";
		RequestDetailsBackUpAndRestoreEntity service = new RequestDetailsBackUpAndRestoreEntity();
		List<RequestDetailsBackUpAndRestoreEntity> list = new ArrayList<RequestDetailsBackUpAndRestoreEntity>();

		try {
			JSONParser parser = new JSONParser();
			List<BackUpRequestVersioningJSONModel> versioningModel = new ArrayList<BackUpRequestVersioningJSONModel>();
			List<RequestDetailsBackUpAndRestoreEntity> versioningModelChildList = new ArrayList<RequestDetailsBackUpAndRestoreEntity>();
			RequestDetailsBackUpAndRestoreEntity objToAdd = null;
			BackUpRequestVersioningJSONModel versioningModelObject = null;
			String requestType = null;
			list = requestDetailsBackUpAndRestoreRepo.findAll();
			// create treeview json
			for (int i = 0; i < list.size(); i++) {
				boolean objectPrsent = false;
				if (versioningModel.size() > 0) {
					for (int j = 0; j < versioningModel.size(); j++) {
						if (versioningModel.get(j).getHostname().equalsIgnoreCase(list.get(i).getHostname())) {
							objectPrsent = true;
							break;
						}
					}
				}

				objToAdd = list.get(i);
				String backUpRequestCheck = objToAdd.getAlphanumericReqId().substring(0, 4);

				if (objectPrsent == false && backUpRequestCheck.contains("SLGB")) {
					versioningModelObject = new BackUpRequestVersioningJSONModel();
					objToAdd = new RequestDetailsBackUpAndRestoreEntity();
					objToAdd = list.get(i);
					versioningModelObject.setHostname(objToAdd.getHostname());

					versioningModelObject.setVendor(objToAdd.getVendor());
					versioningModelObject.setDevice_type(objToAdd.getDevice_type());

					versioningModelObject.setModel(objToAdd.getModel());

					versioningModelObject.setManagementIp(objToAdd.getManagementIp());
					versioningModelObject.setModel(objToAdd.getModel());

					versioningModelObject.setRequest_creator_name(objToAdd.getRequest_creator_name());

					versioningModelChildList = new ArrayList<RequestDetailsBackUpAndRestoreEntity>();
					for (int k = 0; k < list.size(); k++) {

						requestType = list.get(k).getAlphanumericReqId().substring(0,
								Math.min(list.get(k).getAlphanumericReqId().length(), 4));

						if (list.get(k).getHostname().equalsIgnoreCase(versioningModelObject.getHostname())
								&& requestType.equals("SLGB")) {
							versioningModelChildList.add(list.get(k));
						}
					}
					Collections.reverse(versioningModelChildList);

					versioningModelObject.setChildList(versioningModelChildList);
					versioningModel.add(versioningModelObject);

				}

			}

			jsonArray = new Gson().toJson(versioningModel);
			obj.put(new String("output"), jsonArray);

		} catch (Exception e) {
			logger.error(e);
		}

		return Response.status(200).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").entity(obj).build();

	}

	@SuppressWarnings("unchecked")
	@POST
	@RequestMapping(value = "/getBaselineVersion", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public Response getBaselineData(@RequestBody String request) {

		JSONObject obj = new JSONObject();
		JSONParser parser = new JSONParser();
		String hostName = null, requestId = null, requestIdToCheck = null, str = null;

		List<RequestDetailsBackUpAndRestoreEntity> baseLineVersionList = new ArrayList<RequestDetailsBackUpAndRestoreEntity>();

		try {
			obj = (JSONObject) parser.parse(request);

			hostName = obj.get("hostname").toString();
			requestId = obj.get("alphanumericReqId").toString();

			baseLineVersionList = requestDetailsBackUpAndRestoreRepo.findByHostname(hostName);
			for (int i = 0; i < baseLineVersionList.size(); i++) {

				requestIdToCheck = baseLineVersionList.get(i).getAlphanumericReqId();

				if (requestIdToCheck.equals(requestId)) {
					baseLineVersionList.get(i).setBaselinedFlag(true);
				} else

				{

					baseLineVersionList.get(i).setBaselinedFlag(false);
				}
			}

			requestDetailsBackUpAndRestoreRepo.save(baseLineVersionList);
			str = "Baseline version reset successfully";

		} catch (Exception e) {
			logger.error(e);
		}

		return Response.status(200).entity(str).build();
	}

	@POST
	@RequestMapping(value = "/createConfigurationDcmBackUpAndRestore", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public JSONObject createConfigurationDcmBackUpAndRestore(@RequestBody String configRequest) {

		JSONObject obj = new JSONObject();
		String hostName = "", managementIp = "", scheduledTime = "", alphaneumeric_req_id = "";
		Boolean startup;

		List<DeviceDiscoveryEntity> requestDetail = null;

		Timestamp timestamp = null;

		ObjectMapper mapper = new ObjectMapper();

		try {

			JSONParser parser = new JSONParser();

			JSONObject json = (JSONObject) parser.parse(configRequest);

			CreateConfigRequestDCM configReqToSendToC3pCode = new CreateConfigRequestDCM();

			hostName = json.get("hostname").toString();
			managementIp = json.get("managementIp").toString();

			scheduledTime = json.get("scheduleDate").toString();

			requestDetail = deviceDiscoveryRepository.findByDHostNameAndDMgmtIp(hostName, managementIp);

			for (int i = 0; i < requestDetail.size(); i++) {

				RequestInfoEntity requestInfoEntity = new RequestInfoEntity();

				requestInfoEntity.setRequestType("SLGB");
				alphaneumeric_req_id = "SLGB-" + UUID.randomUUID().toString().toUpperCase().substring(0, 7);
				requestInfoEntity.setAlphanumericReqId(alphaneumeric_req_id);

				Boolean isStartUp = (Boolean) json.get("startup");
				requestInfoEntity.setStartUp(isStartUp);
				LocalDateTime nowDate = LocalDateTime.now();
				timestamp = Timestamp.valueOf(nowDate);
				requestInfoEntity.setDateofProcessing(timestamp);

				requestInfoEntity.setBatchId(null);

				requestInfoEntity.setCustomer(requestDetail.get(i).getCustSiteId().getcCustName());

				requestInfoEntity.setSiteId(requestDetail.get(i).getCustSiteId().getcSiteId());

				requestInfoEntity.setDeviceType(requestDetail.get(i).getdType());

				requestInfoEntity.setModel(requestDetail.get(i).getdModel());

				requestInfoEntity.setOs(requestDetail.get(i).getdOs());

				requestInfoEntity.setOsVersion(requestDetail.get(i).getdOsVersion());

				requestInfoEntity.setManagmentIP(requestDetail.get(i).getdMgmtIp());

				requestInfoEntity.setRegion(requestDetail.get(i).getCustSiteId().getcSiteRegion());

				requestInfoEntity.setService(requestDetail.get(i).getdVNFSupport());

				requestInfoEntity.setHostName(requestDetail.get(i).getdHostName());

				requestInfoEntity.setVendor(requestDetail.get(i).getdVendor());

				requestInfoEntity.setNetworkType(requestDetail.get(i).getdVNFSupport());

				requestInfoEntity.setRequestVersion(1.0);
				
				requestInfoEntity.setFamily(requestDetail.get(i).getdDeviceFamily());

				requestInfoEntity.setSiteName(requestDetail.get(i).getCustSiteId().getcSiteName());
				requestInfoEntity.setCertificationSelectionBit("1010011");
				requestInfoEntity.setRequestParentVersion(1.0);
				requestInfoEntity.setRequestTypeFlag("M");

				requestInfoEntity.setRequestCreatorName("admin");

				if (!(scheduledTime.isEmpty())) {
					requestInfoEntity.setBackUpScheduleTime(scheduledTime);
				}
				String jsonString = mapper.writeValueAsString(requestInfoEntity);

				obj = myObj.createConfigurationDcm(jsonString);

				obj.put("output", "Backup Request created successfully");

			}

		}

		catch (Exception e) {
		}
		return obj;

	}

	/* Web service call to search request based on user input */
	@SuppressWarnings("unchecked")
	@POST
	@RequestMapping(value = "/search", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public Response getAllBackUpRequest(@RequestBody String searchParameters) {

		JSONObject obj = new JSONObject();
		Gson gson = new Gson();
		String jsonArray = "";
		String key = null, value = null;
		Boolean flag = false;

		List<RequestDetailsBackUpAndRestoreEntity> detailsList = new ArrayList<RequestDetailsBackUpAndRestoreEntity>();
		List<BackUpRequestVersioningJSONModel> detailsListFinal = new ArrayList<BackUpRequestVersioningJSONModel>();
		List<RequestDetailsBackUpAndRestoreEntity> emptyList = new ArrayList<RequestDetailsBackUpAndRestoreEntity>();
		detailsList = requestDetailsBackUpAndRestoreRepo.findAll();

		try {

			SearchParamPojo dto = gson.fromJson(searchParameters, SearchParamPojo.class);

			key = dto.getKey();
			value = dto.getValue();

			if (value != null && !value.isEmpty()) {
				/*
				 * Search request based on Region, Vendor, Status, Model, Host Name and
				 * Management IP
				 */
				if (key.equalsIgnoreCase("Request ID")) {
					detailsList = requestDetailsBackUpAndRestoreRepo.findByAlphanumericReqId(value);

					detailsListFinal = searchImportDashboard(detailsList);
					flag = true;

				} else if (key.equalsIgnoreCase("Vendor")) {
					detailsList = requestDetailsBackUpAndRestoreRepo.findByVendor(value);

					detailsListFinal = searchImportDashboard(detailsList);
					flag = true;

				} else if (key.equalsIgnoreCase("Device Type")) {
					detailsList = requestDetailsBackUpAndRestoreRepo.findByDeviceType(value);

					detailsListFinal = searchImportDashboard(detailsList);
					flag = true;

				} else if (key.equalsIgnoreCase("Model")) {
					detailsList = requestDetailsBackUpAndRestoreRepo.findByModel(value);

					detailsListFinal = searchImportDashboard(detailsList);
					flag = true;

				} else if (key.equalsIgnoreCase("Hostname")) {
					detailsList = requestDetailsBackUpAndRestoreRepo.findByHostname(value);

					detailsListFinal = searchImportDashboard(detailsList);
					flag = true;

				} else if (key.equalsIgnoreCase("IP Address")) {
					detailsList = requestDetailsBackUpAndRestoreRepo.findByManagementIp(value);

					detailsListFinal = searchImportDashboard(detailsList);
					flag = true;

				}

			}

			if (flag == true) {
				jsonArray = gson.toJson(detailsListFinal);
				obj.put(new String("output"), jsonArray);
				flag = false;
			} else

			{

				jsonArray = new Gson().toJson(emptyList);
				obj.put(new String("output"), jsonArray);
			}

		}

		catch (Exception e) {
			logger.error(e);

		}

		return Response.status(200).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").entity(obj).build();
	}

	/* Method to iterate over all possible search result */
	private List<BackUpRequestVersioningJSONModel> searchImportDashboard(
			List<RequestDetailsBackUpAndRestoreEntity> list) {

		JSONObject obj = new JSONObject();
		String jsonArray = "";
		RequestDetailsBackUpAndRestoreEntity service = new RequestDetailsBackUpAndRestoreEntity();

		List<BackUpRequestVersioningJSONModel> versioningModel = new ArrayList<BackUpRequestVersioningJSONModel>();

		try {
			JSONParser parser = new JSONParser();

			List<RequestDetailsBackUpAndRestoreEntity> versioningModelChildList = new ArrayList<RequestDetailsBackUpAndRestoreEntity>();
			RequestDetailsBackUpAndRestoreEntity objToAdd = null;
			BackUpRequestVersioningJSONModel versioningModelObject = null;
			String requestType = null;

			// create treeview json
			for (int i = 0; i < list.size(); i++) {
				boolean objectPrsent = false;
				if (versioningModel.size() > 0) {
					for (int j = 0; j < versioningModel.size(); j++) {
						if (versioningModel.get(j).getHostname().equalsIgnoreCase(list.get(i).getHostname())) {
							objectPrsent = true;
							break;
						}
					}
				}

				objToAdd = list.get(i);
				String backUpRequestCheck = objToAdd.getAlphanumericReqId().substring(0, 4);

				if (objectPrsent == false && backUpRequestCheck.contains("SLGB")) {
					versioningModelObject = new BackUpRequestVersioningJSONModel();
					objToAdd = new RequestDetailsBackUpAndRestoreEntity();
					objToAdd = list.get(i);
					versioningModelObject.setHostname(objToAdd.getHostname());

					versioningModelObject.setVendor(objToAdd.getVendor());
					versioningModelObject.setDevice_type(objToAdd.getDevice_type());

					versioningModelObject.setModel(objToAdd.getModel());

					versioningModelObject.setManagementIp(objToAdd.getManagementIp());
					versioningModelObject.setModel(objToAdd.getModel());

					versioningModelObject.setRequest_creator_name(objToAdd.getRequest_creator_name());

					versioningModelChildList = new ArrayList<RequestDetailsBackUpAndRestoreEntity>();
					for (int k = 0; k < list.size(); k++) {

						requestType = list.get(k).getAlphanumericReqId().substring(0,
								Math.min(list.get(k).getAlphanumericReqId().length(), 4));

						if (list.get(k).getHostname().equalsIgnoreCase(versioningModelObject.getHostname())
								&& requestType.equals("SLGB")) {
							versioningModelChildList.add(list.get(k));
						}
					}
					Collections.reverse(versioningModelChildList);

					versioningModelObject.setChildList(versioningModelChildList);
					versioningModel.add(versioningModelObject);

				}

			}

		} catch (Exception e) {
			logger.error(e);
		}

		return versioningModel;

	}

	@POST
	@RequestMapping(value = "/batchBackUpAndRestore", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public JSONObject batchBackUpAndRestore(@RequestBody String configRequest) {

		JSONObject obj = new JSONObject();
		String scheduledTime = "", alphaneumeric_req_id = "", tempManagementIp = "";

		Map<String, String> result = new HashMap<String, String>();

		List<DeviceDiscoveryEntity> requestDetail = null;
		List<SiteInfoEntity> requestDetail1 = null;

		BatchIdEntity batchIdEntity = new BatchIdEntity();

		String tempHostName = null;
		Double request_version = 1.0;
		;
		boolean tempStartUp = false;
		Timestamp timestamp = null;

		RequestInfoPojo requestInfoPojo = new RequestInfoPojo();
		final String batchId = "BI-" + UUID.randomUUID().toString().toUpperCase().substring(0, 7);

		try {

			Gson gson = new Gson();

			BatchPojo[] userArray = gson.fromJson(configRequest, BatchPojo[].class);

			for (int j = 0; j < userArray.length; j++) {

				tempHostName = userArray[j].getHostname();
				tempStartUp = userArray[j].isStartup();
				tempManagementIp = userArray[j].getManagementIp();

				requestDetail = deviceDiscoveryRepository.findByDHostNameAndDMgmtIp(tempHostName, tempManagementIp);

				for (int i = 0; i < requestDetail.size(); i++) {
					RequestInfoEntity requestInfoEntity = new RequestInfoEntity();

					requestInfoEntity.setRequestType("SLGB");
					alphaneumeric_req_id = "SLGB-" + UUID.randomUUID().toString().toUpperCase().substring(0, 7);
					requestInfoEntity.setAlphanumericReqId(alphaneumeric_req_id);

					/*
					 * SiteInfoEntity tempId = requestDetail.get(i) .getCustSiteId();
					 * 
					 * requestDetail1 = siteInfoRepository.findByCCustId(tempId);
					 */

					if (j == 0) {

						requestInfoEntity.setStatus("In Progress");
					} else {

						requestInfoEntity.setStatus("In Progress");
					}
					LocalDateTime nowDate = LocalDateTime.now();
					timestamp = Timestamp.valueOf(nowDate);
					requestInfoEntity.setDateofProcessing(timestamp);
					/*
					 * if ((requestDetail.get(i).getSceheduledTime()) != null) {
					 * 
					 * timestamp = Timestamp.valueOf(requestDetail.get(i)
					 * .getSceheduledTime().toString());
					 * requestInfoEntity.setSceheduledTime(timestamp); }
					 */

					requestInfoEntity.setBatchId(batchId);

					requestInfoEntity.setCustomer(requestDetail.get(i).getCustSiteId().getcCustName());

					requestInfoEntity.setSiteId(requestDetail.get(i).getCustSiteId().getcSiteId());

					requestInfoEntity.setDeviceType(requestDetail.get(i).getdType());

					requestInfoEntity.setModel(requestDetail.get(i).getdModel());

					requestInfoEntity.setOs(requestDetail.get(i).getdOs());

					requestInfoEntity.setOsVersion(requestDetail.get(i).getdOsVersion());

					requestInfoEntity.setManagmentIP(requestDetail.get(i).getdMgmtIp());

					requestInfoEntity.setRegion(requestDetail.get(i).getCustSiteId().getcSiteRegion());

					requestInfoEntity.setService(requestDetail.get(i).getdVNFSupport());

					requestInfoEntity.setHostName(requestDetail.get(i).getdHostName());

					requestInfoEntity.setVendor(requestDetail.get(i).getdVendor());

					requestInfoEntity.setNetworkType(requestDetail.get(i).getdVNFSupport());
					
					requestInfoEntity.setFamily(requestDetail.get(i).getdDeviceFamily());

					requestInfoEntity.setRequestVersion(request_version);

					requestInfoEntity.setSiteName(requestDetail.get(i).getCustSiteId().getcSiteName());
					requestInfoEntity.setCertificationSelectionBit("1010011");
					requestInfoEntity.setRequestParentVersion(1.0);
					requestInfoEntity.setRequestTypeFlag("M");

					requestInfoEntity.setRequestCreatorName(Global.loggedInUser);

					requestInfoEntity.setStartUp(tempStartUp);

					if (!(scheduledTime.isEmpty())) {

						timestamp = Timestamp.valueOf(scheduledTime);
						requestInfoEntity.setSceheduledTime(timestamp);
					}
					batchIdEntity.setBatchStatus("In Progress");

					batchIdEntity.setBatchId(batchId);

					batchIdEntity.setRequestInfoEntity(requestInfoEntity);

					RequestInfoEntity resultEntity = requestInfoDetailsRepositories.save(requestInfoEntity);
					if (resultEntity.getInfoId() > 0) {
						/* Creating request Agains device then update isNew flag */
						int isNew = requestDetail.get(i).getdNewDevice();
						if (isNew == 1) {
							requestDetail.get(i).setdNewDevice(0);
							deviceDiscoveryRepository.save(requestDetail.get(i));
						}
					}

					dao.addRequestIDtoWebserviceInfo(alphaneumeric_req_id, Double.toString(request_version));
					dao.addCertificationTestForRequest(alphaneumeric_req_id, Double.toString(request_version), "0");
					// result = dao.insertRequestInDB(requestInfoPojo);
					batchInfoRepo.save(batchIdEntity);

				}
			}

			obj.put("batchId", batchId);
			obj.put("output", "Batch Request created successfully");
		}

		catch (Exception e) {
			logger.error(e);
		}
		return obj;

	}

	@GET
	@RequestMapping(value = "/getAllRequest", method = RequestMethod.GET, produces = "application/json")
	public Response getAllRequest() {
		JSONObject obj = new JSONObject();
		String jsonArray = "";

		List<RequestInfoEntity> detailsList = new ArrayList<RequestInfoEntity>();

		detailsList = requestInfoDetailsRepositories.findAll();

		jsonArray = new Gson().toJson(detailsList);
		obj.put(new String("output"), jsonArray);

		return Response.status(200).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").entity(obj).build();

	}

	@GET
	@RequestMapping(value = "/getAllBatchRequest", method = RequestMethod.GET, produces = "application/json")
	public Response getAllBatchId() {
		JSONObject obj = new JSONObject();
		String jsonArray = "";

		List<RequestInfoEntity> detailsList = new ArrayList<RequestInfoEntity>();
		List<RequestInfoEntity> detailsList1 = new ArrayList<RequestInfoEntity>();

		detailsList = requestInfoDetailsRepositories.findAll();

		for (int i = 0; i < detailsList.size(); i++) {
			if (!(detailsList.get(i).getBatchId().equals(""))) {
				String tempId = detailsList.get(i).getAlphanumericReqId();
				detailsList1 = requestInfoDetailsRepositories.findAllByAlphanumericReqId(tempId);
			}
		}

		jsonArray = new Gson().toJson(detailsList1);
		obj.put(new String("output"), jsonArray);

		return Response.status(200).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").entity(obj).build();

	}

	@POST
	@RequestMapping(value = "/getSingleBatch", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public Response getSingleBatchRequest(@RequestBody String configRequest) {

		JSONObject obj = new JSONObject();
		String batchId = "", jsonArray = "";

		List<RequestInfoEntity> detailsList = new ArrayList<RequestInfoEntity>();

		try {

			JSONParser parser = new JSONParser();

			JSONObject json = (JSONObject) parser.parse(configRequest);

			CreateConfigRequestDCM configReqToSendToC3pCode = new CreateConfigRequestDCM();

			batchId = json.get("batchId").toString();

			detailsList = requestInfoDetailsRepositories.findByBatchId(batchId);

		} catch (Exception e) {

		}
		jsonArray = new Gson().toJson(detailsList);
		obj.put(new String("output"), jsonArray);

		return Response.status(200).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").entity(obj).build();

	}

	@SuppressWarnings("unchecked")
	@POST
	@RequestMapping(value = "/batchConfig", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public JSONObject batchConfig(@RequestBody String configRequest) throws ParseException {

		JSONObject obj = new JSONObject();

		JsonArray attribJson = null;
		String scheduledTime = "", alphaneumeric_req_id = "", tempManagementIp = "", tempHostName = null,
				templateId = null, requestType = "Config MACD", requestId = null;

		HashMap<String, String> map = new HashMap<String, String>();
		ArrayList<String> tempRequest = new ArrayList<String>();
		ArrayList<String> tempRequest1 = new ArrayList<String>();

		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(configRequest);

		JsonObject jsonObject = new JsonParser().parse(configRequest).getAsJsonObject();

		attribJson = jsonObject.getAsJsonArray("requests");

		for (int n = 0; n < attribJson.size(); n++) {
			tempHostName = attribJson.get(n).getAsJsonObject().get("hostname").getAsString();
			tempManagementIp = attribJson.get(n).getAsJsonObject().get("managementIp").getAsString();
			templateId = attribJson.get(n).getAsJsonObject().get("templateId").getAsString();
			map.put(tempHostName, tempManagementIp);
			tempRequest.add(templateId);
		}

		List<DeviceDiscoveryEntity> requestDetail = null;
		ObjectMapper mapper = new ObjectMapper();

		Double request_version = 1.0;

		Timestamp timestamp = null;
		String temp = null;

		final String batchId = "BI-" + UUID.randomUUID().toString().toUpperCase().substring(0, 7);
		int k = 0;

		try {

			for (int j = 0; j < tempRequest.size(); j++) {

				for (Map.Entry m : map.entrySet()) {

					tempHostName = (String) m.getKey();
					tempManagementIp = (String) m.getValue();

					temp = tempHostName;

					if (tempRequest1.contains(temp)) {

						continue;
					}

					tempRequest1.add(tempHostName);

					requestDetail = deviceDiscoveryRepository.findByDHostNameAndDMgmtIp(tempHostName, tempManagementIp);

					for (int i = 0; i < requestDetail.size(); i++) {
						RequestInfoEntity requestInfoEntity = new RequestInfoEntity();

						requestInfoEntity.setRequestType(requestType);
						
						alphaneumeric_req_id = "SLGC-" + UUID.randomUUID().toString().toUpperCase().substring(0, 7);
						requestInfoEntity.setAlphanumericReqId(alphaneumeric_req_id);
						if (j == 0) {
							requestInfoEntity.setStatus("In Progress");
						} else {

							requestInfoEntity.setStatus("In Progress");
						}
						LocalDateTime nowDate = LocalDateTime.now();
						timestamp = Timestamp.valueOf(nowDate);
						requestInfoEntity.setDateofProcessing(timestamp);
						requestInfoEntity.setTemplateUsed(templateId);
						requestInfoEntity.setSelectedFeatures(json.get("selectedFeatures"));

						requestInfoEntity.setDynamicAttribs(json.get("dynamicAttribs"));

						requestInfoEntity.setBatchId(batchId);
						requestInfoEntity.setBatchSize(map.size());
						requestInfoEntity.setCustomer(requestDetail.get(i).getCustSiteId().getcCustName());

						requestInfoEntity.setSiteId(requestDetail.get(i).getCustSiteId().getcSiteId());

						requestInfoEntity.setDeviceType(requestDetail.get(i).getdType());

						requestInfoEntity.setModel(requestDetail.get(i).getdModel());

						requestInfoEntity.setOs(requestDetail.get(i).getdOs());

						requestInfoEntity.setOsVersion(requestDetail.get(i).getdOsVersion());

						requestInfoEntity.setManagmentIP(requestDetail.get(i).getdMgmtIp());

						requestInfoEntity.setRegion(requestDetail.get(i).getCustSiteId().getcSiteRegion());

						requestInfoEntity.setService(requestDetail.get(i).getdVNFSupport());

						requestInfoEntity.setHostName(requestDetail.get(i).getdHostName());

						requestInfoEntity.setVendor(requestDetail.get(i).getdVendor());

						requestInfoEntity.setNetworkType(requestDetail.get(i).getdVNFSupport());

						requestInfoEntity.setRequestVersion(request_version);
						requestInfoEntity.setFamily(requestDetail.get(i).getdDeviceFamily());
						requestInfoEntity.setStartUp(false);

						requestInfoEntity.setSiteName(requestDetail.get(i).getCustSiteId().getcSiteName());
						requestInfoEntity.setCertificationSelectionBit("1010011");
						requestInfoEntity.setRequestParentVersion(1.0);
						requestInfoEntity.setRequestTypeFlag("M");

						requestInfoEntity.setRequestCreatorName("admin");

						if (!(scheduledTime.isEmpty())) {

							timestamp = Timestamp.valueOf(scheduledTime);
							requestInfoEntity.setSceheduledTime(timestamp);
						}

						String jsonString = mapper.writeValueAsString(requestInfoEntity);

						requestId = myObj.getTemplateId(jsonString);
						k++;
					}
					break;
				}
			}
			if (k == 1) {
				obj.put("output", "Request created successfully");
				obj.put(new String("requestId"), new String(requestId));
				obj.put(new String("version"), "1.0");
			} else {

				obj.put("batchId", batchId);
				obj.put("output", "Batch Request created successfully");

			}
		}

		catch (Exception e) {
			logger.error(e);
		}
		return obj;

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@POST
	@RequestMapping(value = "/batchTest", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public JSONObject batchTest(@RequestBody String configRequest) throws ParseException {

		JSONObject obj = new JSONObject();

		JsonArray attribJson = null;
		String scheduledTime = "", alphaneumeric_req_id = "", tempManagementIp = "", tempHostName = null,
				templateId = null, requestType = null, requestId = null;

		HashMap<String, String> map = new HashMap<String, String>();

		ArrayList<String> tempRequest1 = new ArrayList<String>();
		int j = 0;

		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(configRequest);

		JsonObject jsonObject = new JsonParser().parse(configRequest).getAsJsonObject();

		attribJson = jsonObject.getAsJsonArray("requests");

		for (int n = 0; n < attribJson.size(); n++) {
			tempHostName = attribJson.get(n).getAsJsonObject().get("hostname").getAsString();
			tempManagementIp = attribJson.get(n).getAsJsonObject().get("managementIp").getAsString();
			requestType = attribJson.get(n).getAsJsonObject().get("requestType").getAsString();
			map.put(tempHostName, tempManagementIp);

		}

		List<DeviceDiscoveryEntity> requestDetail = null;
		ObjectMapper mapper = new ObjectMapper();

		Double request_version = 1.0;

		Timestamp timestamp = null;
		String temp = null;

		final String batchId = "BI-" + UUID.randomUUID().toString().toUpperCase().substring(0, 7);

		try {

			for (Map.Entry m : map.entrySet()) {

				tempHostName = (String) m.getKey();
				tempManagementIp = (String) m.getValue();

				temp = tempHostName;

				if (tempRequest1.contains(temp)) {

					continue;
				}

				tempRequest1.add(tempHostName);

				requestDetail = deviceDiscoveryRepository.findByDHostNameAndDMgmtIp(tempHostName, tempManagementIp);

				for (int i = 0; i < requestDetail.size(); i++) {
					RequestInfoEntity requestInfoEntity = new RequestInfoEntity();

					if (map.size() > 1) {

						requestInfoEntity.setBatchId(batchId);

					}

					if (j == 0) {

						requestInfoEntity.setStatus("In Progress");
					} else {

						requestInfoEntity.setStatus("In Progress");

					}

					LocalDateTime nowDate = LocalDateTime.now();
					timestamp = Timestamp.valueOf(nowDate);
					requestInfoEntity.setDateofProcessing(timestamp);
					requestInfoEntity.setTemplateUsed(templateId);
					requestInfoEntity.setCertificationTests(json.get("certificationTests"));

					requestInfoEntity.setBatchSize(map.size());

					requestInfoEntity.setRequestType(requestType);
					alphaneumeric_req_id = "SLGT-" + UUID.randomUUID().toString().toUpperCase().substring(0, 7);
					requestInfoEntity.setAlphanumericReqId(alphaneumeric_req_id);

					requestInfoEntity.setCustomer(requestDetail.get(i).getCustSiteId().getcCustName());

					requestInfoEntity.setSiteId(requestDetail.get(i).getCustSiteId().getcSiteId());

					requestInfoEntity.setDeviceType(requestDetail.get(i).getdType());

					requestInfoEntity.setModel(requestDetail.get(i).getdModel());

					requestInfoEntity.setOs(requestDetail.get(i).getdOs());

					requestInfoEntity.setOsVersion(requestDetail.get(i).getdOsVersion());

					requestInfoEntity.setManagmentIP(requestDetail.get(i).getdMgmtIp());

					requestInfoEntity.setRegion(requestDetail.get(i).getCustSiteId().getcSiteRegion());

					requestInfoEntity.setService(requestDetail.get(i).getdVNFSupport());

					requestInfoEntity.setHostName(requestDetail.get(i).getdHostName());

					requestInfoEntity.setVendor(requestDetail.get(i).getdVendor());

					requestInfoEntity.setNetworkType(requestDetail.get(i).getdVNFSupport());

					requestInfoEntity.setRequestVersion(request_version);
					requestInfoEntity.setFamily(requestDetail.get(i).getdDeviceFamily());
					requestInfoEntity.setStartUp(false);

					requestInfoEntity.setSiteName(requestDetail.get(i).getCustSiteId().getcSiteName());
					requestInfoEntity.setCertificationSelectionBit("1010011");
					requestInfoEntity.setRequestParentVersion(1.0);
					requestInfoEntity.setRequestTypeFlag("M");

					requestInfoEntity.setRequestCreatorName("admin");

					if (!(scheduledTime.isEmpty())) {

						timestamp = Timestamp.valueOf(scheduledTime);
						requestInfoEntity.setSceheduledTime(timestamp);
					}

					String jsonString = mapper.writeValueAsString(requestInfoEntity);

					requestId = myObj.getTemplateId(jsonString);

					j++;
				}

			}
			if (j == 1) {

				obj.put("output", "Request created successfully");
				obj.put(new String("requestId"), new String(requestId));
				obj.put(new String("version"), "1.0");
			} else {
				obj.put("batchId", batchId);
				obj.put("output", "Batch Request created successfully");
			}
		}

		catch (Exception e) {
			logger.error(e);
		}
		return obj;

	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@POST
	@RequestMapping(value = "/batchOsUpgrade", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public JSONObject batchOsUpgradeRequest(@RequestBody String configRequest) throws ParseException {

		JSONObject obj = new JSONObject();

		JsonArray attribJson = null;
		String scheduledTime = "", alphaneumeric_req_id = "", tempManagementIp = "", tempHostName = null,
				templateId = null, requestType = null, requestId=null;

		HashMap<String, String> map = new HashMap<String, String>();

		ArrayList<String> tempRequest1 = new ArrayList<String>();
		int j = 0;
		BatchIdEntity batchIdEntity = new BatchIdEntity();

		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(configRequest);

		JsonObject jsonObject = new JsonParser().parse(configRequest).getAsJsonObject();

		attribJson = jsonObject.getAsJsonArray("requests");

		for (int n = 0; n < attribJson.size(); n++) {
			tempHostName = attribJson.get(n).getAsJsonObject().get("hostname").getAsString();
			tempManagementIp = attribJson.get(n).getAsJsonObject().get("managementIp").getAsString();
			requestType = attribJson.get(n).getAsJsonObject().get("requestType").getAsString();
			templateId = attribJson.get(n).getAsJsonObject().get("templateId").getAsString();
			map.put(tempHostName, tempManagementIp);

		}

		List<DeviceDiscoveryEntity> requestDetail = null;
		ObjectMapper mapper = new ObjectMapper();

		Double request_version = 1.0;

		Timestamp timestamp = null;
		String temp = null;

		final String batchId = "BI-" + UUID.randomUUID().toString().toUpperCase().substring(0, 7);

		try {

			for (Map.Entry m : map.entrySet()) {

				tempHostName = (String) m.getKey();
				tempManagementIp = (String) m.getValue();

				temp = tempHostName;

				if (tempRequest1.contains(temp)) {

					continue;
				}

				tempRequest1.add(tempHostName);

				requestDetail = deviceDiscoveryRepository.findByDHostNameAndDMgmtIp(tempHostName, tempManagementIp);

				for (int i = 0; i < requestDetail.size(); i++) {
					RequestInfoEntity requestInfoEntity = new RequestInfoEntity();

					if (j == 0) {

						requestInfoEntity.setStatus("In Progress");
					} else {

						requestInfoEntity.setStatus("In Progress");

					}

					LocalDateTime nowDate = LocalDateTime.now();
					timestamp = Timestamp.valueOf(nowDate);
					requestInfoEntity.setDateofProcessing(timestamp);
					requestInfoEntity.setCertificationTests(json.get("certificationTests"));

					requestInfoEntity.setBatchSize(map.size());

					requestInfoEntity.setRequestType(requestType);
					alphaneumeric_req_id = "SLGF-" + UUID.randomUUID().toString().toUpperCase().substring(0, 7);
					requestInfoEntity.setAlphanumericReqId(alphaneumeric_req_id);

					requestInfoEntity.setCustomer(requestDetail.get(i).getCustSiteId().getcCustName());

					requestInfoEntity.setSiteId(requestDetail.get(i).getCustSiteId().getcSiteId());

					requestInfoEntity.setDeviceType(requestDetail.get(i).getdType());

					requestInfoEntity.setModel(requestDetail.get(i).getdModel());

					requestInfoEntity.setOs(requestDetail.get(i).getdOs());

					requestInfoEntity.setOsVersion(requestDetail.get(i).getdOsVersion());

					requestInfoEntity.setManagmentIP(requestDetail.get(i).getdMgmtIp());

					requestInfoEntity.setRegion(requestDetail.get(i).getCustSiteId().getcSiteRegion());

					requestInfoEntity.setService(requestDetail.get(i).getdVNFSupport());

					requestInfoEntity.setHostName(requestDetail.get(i).getdHostName());

					requestInfoEntity.setVendor(requestDetail.get(i).getdVendor());

					requestInfoEntity.setNetworkType(requestDetail.get(i).getdVNFSupport());

					requestInfoEntity.setRequestVersion(request_version);
					requestInfoEntity.setFamily(requestDetail.get(i).getdDeviceFamily());
					requestInfoEntity.setStartUp(false);

					requestInfoEntity.setSiteName(requestDetail.get(i).getCustSiteId().getcSiteName());
					requestInfoEntity.setCertificationSelectionBit("1010011");
					requestInfoEntity.setRequestParentVersion(1.0);
					requestInfoEntity.setRequestTypeFlag("M");

					requestInfoEntity.setRequestCreatorName("admin");

					if (!(scheduledTime.isEmpty())) {

						timestamp = Timestamp.valueOf(scheduledTime);
						requestInfoEntity.setSceheduledTime(timestamp);
					}
					if (map.size() > 1) {
						requestInfoEntity.setBatchId(batchId);
						batchIdEntity.setBatchStatus("In Progress");	
						batchIdEntity.setBatchId(batchId);
						batchIdEntity.setRequestInfoEntity(requestInfoEntity);
						requestInfoDetailsRepositories.save(requestInfoEntity);
						
						dao.addRequestIDtoWebserviceInfo(alphaneumeric_req_id, Double.toString(request_version));
						dao.addCertificationTestForRequest(alphaneumeric_req_id, Double.toString(request_version), "0");
						dao.addRequestID_to_Os_Upgrade_dilevary_flags(alphaneumeric_req_id, Double.toString(request_version));
						batchInfoRepo.save(batchIdEntity);
					}
					else
					{
						requestInfoEntity.setTemplateUsed(templateId);
						String jsonString = mapper.writeValueAsString(requestInfoEntity);
						if(attribJson.size()==1)
						requestId = myObj.getTemplateId(jsonString);
					}
					j++;
				}

			}
			if (j == 1) {

				obj.put("output", "Request created successfully");
				obj.put(new String("requestId"), new String(requestId));
				obj.put(new String("version"), "1.0");
			} else {
				obj.put("batchId", batchId);
				obj.put("output", "Batch Request created successfully");
			}
		}

		catch (Exception e) {
			logger.error(e);
		}
		return obj;

	}
	@POST
	@RequestMapping(value = "/getAllDeviceFamily", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public ResponseEntity getAllFamily(@RequestBody String request) {
		Set<String> model = new HashSet<>();
		try {
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(request);
			String vendor = json.get("vendor").toString();
			List<FirmwareUpgradeDetail> mainList = new ArrayList<FirmwareUpgradeDetail>();
			mainList = dao.findByVendorName(vendor);

			mainList.forEach(site -> {
				model.add(site.getFamily());
			});
		} catch (Exception e) {
			logger.error(e);
		}
		return new ResponseEntity(model, HttpStatus.OK);
	}

	@POST
	@RequestMapping(value = "/getAllOs", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public ResponseEntity getAllOsToUpdate(@RequestBody String request) {
		Set<String> model = new HashSet<>();
		try {
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(request);
			String vendor = json.get("vendor").toString();
			String deviceFamily = json.get("deviceFamily").toString();
			List<FirmwareUpgradeSingleDeviceEntity> mainList = new ArrayList<FirmwareUpgradeSingleDeviceEntity>();
			mainList = firmUpgradeSingleDeviceRepository.findByVendorAndFamily(vendor, deviceFamily);

			mainList.forEach(site -> {
				model.add(site.getDisplayName());
			});
		} catch (Exception e) {
			logger.error(e);
		}
		return new ResponseEntity(model, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@POST
	@RequestMapping(value = "/searchOsUpgradeDashboard", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public ResponseEntity searchOsUpgrade(@RequestBody String searchParameters) {

		JSONObject obj = new JSONObject();
		String name=null;
		try {
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(searchParameters);
			JSONObject object = new JSONObject();

			String vendor = null, deviceFamily = null, osVersion = null;
			List<DeviceDiscoveryEntity> getAllDevice = new ArrayList<DeviceDiscoveryEntity>();

			if (json.containsKey("vendor")) {
				vendor = json.get("vendor").toString();
			}
			if (json.containsKey("deviceFamily")) {
				deviceFamily = json.get("deviceFamily").toString();
			}
			if (json.containsKey("osVersion")) {
				osVersion = json.get("osVersion").toString();
			}

			getAllDevice = deviceInforepo.findAllDevices(vendor, deviceFamily, osVersion);
		
			JSONArray outputArray = new JSONArray();
			for (int i = 0; i < getAllDevice.size(); i++) {
				name=getAllDevice.get(i).getdHostName();
				System.out.println();
				object = new JSONObject();
				object.put("hostName", getAllDevice.get(i).getdHostName());
				if(getAllDevice.get(i).getdMgmtIp()!=null)
				{
				object.put("managementIp", getAllDevice.get(i).getdMgmtIp());
				}
				else
				{
					object.put("managementIp", "");

				}
				object.put("DeviceFamily", getAllDevice.get(i).getdDeviceFamily());
				if( getAllDevice.get(i).getdDeviceFamily()!=null)
				{
				object.put("DeviceFamily", getAllDevice.get(i).getdDeviceFamily());
				}
				else
				{
					object.put("DeviceFamily", "");

				}
				if(getAllDevice.get(i).getdModel()!=null)
				{
				object.put("model", getAllDevice.get(i).getdModel());
				}
				else
				{
					object.put("model", "");
	
				}
				if(getAllDevice.get(i).getdOs()!=null)
				{
				object.put("os", getAllDevice.get(i).getdOs());
				}
				else
				{
					object.put("os","");
	
				}
				if(getAllDevice.get(i).getdOsVersion()!=null)
				{
				object.put("osVersion", getAllDevice.get(i).getdOsVersion());
				}
				else
				{
					object.put("osVersion", "");

				}
				if( getAllDevice.get(i).getdVendor()!=null)
				{
				object.put("vendor", getAllDevice.get(i).getdVendor());
				}
				else
				{
					object.put("vendor", "");

				}
				
				object.put("status", "Available");
				if( getAllDevice.get(i).getCustSiteId()!=null)
				{
				object.put("customer", getAllDevice.get(i).getCustSiteId().getcCustName());
				}
				else
				{
					object.put("customer", "");

				}
				object.put("eos", getAllDevice.get(i).getdEndOfSupportDate());
				object.put("eol", getAllDevice.get(i).getdEndOfSaleDate());
				if(getAllDevice.get(i).getCustSiteId()!=null)
				{
				SiteInfoEntity site = getAllDevice.get(i).getCustSiteId();
				object.put("site", site.getcSiteName());
				object.put("region", site.getcSiteRegion());
				}
				else
				{
					object.put("site", "");
					object.put("region", "");	
				}

				outputArray.add(object);
			}
			obj.put("data", outputArray);

		} catch (Exception e) {
			logger.error(e);
			obj.put("data", "Bad Request");
		}
		return new ResponseEntity(obj, HttpStatus.OK);
	}
	
	@POST
	@RequestMapping(value = "/filterOsUpgradeDashboard", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public ResponseEntity filterOsUpgradeDashboard(@RequestBody String searchParameters) {

		JSONObject obj = new JSONObject();
		try {
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(searchParameters);
			JSONObject object = new JSONObject();

			String customer = null, region = null, vendor = null, osVersion = null, site = null, deviceFamily = null, modeltosearch = null;
			List<DeviceDiscoveryEntity> getAllDevice = new ArrayList<DeviceDiscoveryEntity>();

			if (json.containsKey("customer")) {
				customer = json.get("customer").toString();
			}
			if (json.containsKey("region")) {
				region = json.get("region").toString();
			}
			if (json.containsKey("vendor")) {
				vendor = json.get("vendor").toString();
			}
			if (json.containsKey("osVersion")) {
				osVersion = json.get("osVersion").toString();
			}
			if (json.containsKey("site")) {
				site = json.get("site").toString();
			}
			if (json.containsKey("deviceFamily")) {
				deviceFamily = json.get("deviceFamily").toString();
			}
			

			// Implementation of search logic based on fields received from UI
			String nonMandatoryfiltersbits = "000";

			if (vendor != null) {
				nonMandatoryfiltersbits = "100";
			}
			if (deviceFamily != null) {
				nonMandatoryfiltersbits = "110";
			}
			if (osVersion != null) {
				nonMandatoryfiltersbits = "111";
			}
		
			if (!(customer.equals(""))) {
				nonMandatoryfiltersbits = "211";
			}
			if (!(region.equals(""))) {
				nonMandatoryfiltersbits = "221";
			}
			if (!(site.equals(""))) {
				nonMandatoryfiltersbits = "222";
			}
		
		
		
			if (nonMandatoryfiltersbits.equalsIgnoreCase("111")) {
			getAllDevice = deviceInforepo.findAllDevices(vendor, deviceFamily, osVersion);
			}
			if (nonMandatoryfiltersbits.equalsIgnoreCase("211")) {
				// find with vendor and deviceFamily and osVersion and Customer
				getAllDevice = deviceInforepo
						.findByVendorFamilyVersionCustomer(
								vendor, deviceFamily, osVersion,
								customer);

			}
			if (nonMandatoryfiltersbits.equalsIgnoreCase("221")) {
				// find with vendor and deviceFamily and osVersion and Customer and Region
				getAllDevice = deviceInforepo
						.findByVendorFamilyVersionCustomerRegion(
								vendor, deviceFamily, osVersion,
								customer,region);

			}
			if (nonMandatoryfiltersbits.equalsIgnoreCase("222")) {
				// find with vendor and deviceFamily and osVersion and Customer and Region and Site
				getAllDevice = deviceInforepo
						.findByVendorFamilyVersionCustomerRegionSite(
								vendor, deviceFamily, osVersion,
								customer,region,site);

			}
		

			JSONArray outputArray = new JSONArray();
			for (int i = 0; i < getAllDevice.size(); i++) {
			
				object = new JSONObject();
				object.put("hostName", getAllDevice.get(i).getdHostName());
				object.put("managementIp", getAllDevice.get(i).getdMgmtIp());
				object.put("type", "Router");
				object.put("series", getAllDevice.get(i).getdDeviceFamily());
				object.put("model", getAllDevice.get(i).getdModel());
				object.put("os", getAllDevice.get(i).getdOs());
				object.put("osVersion", getAllDevice.get(i).getdOsVersion());
				object.put("vendor", getAllDevice.get(i).getdVendor());
				object.put("status", "Available");
				object.put("customer", getAllDevice.get(i).getCustSiteId()
						.getcCustName());
				object.put("eos", getAllDevice.get(i).getdEndOfSupportDate());
				object.put("eol", getAllDevice.get(i).getdEndOfSaleDate());
				SiteInfoEntity site1 = getAllDevice.get(i).getCustSiteId();
				object.put("site", site1.getcSiteName());
				object.put("region", site1.getcSiteRegion());

			

				outputArray.add(object);
			}
			obj.put("data", outputArray);

		} catch (Exception e) {
			logger.error(e);
			obj.put("data", "Bad Request");
		}

		return new ResponseEntity(obj, HttpStatus.OK);
	}

}

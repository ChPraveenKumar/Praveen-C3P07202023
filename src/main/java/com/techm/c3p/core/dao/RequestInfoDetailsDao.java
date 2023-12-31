package com.techm.c3p.core.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.techm.c3p.core.connection.DBUtil;
import com.techm.c3p.core.connection.JDBCConnection;
import com.techm.c3p.core.entitybeans.CredentialManagementEntity;
import com.techm.c3p.core.entitybeans.DeviceDiscoveryEntity;
import com.techm.c3p.core.entitybeans.RequestInfoEntity;
import com.techm.c3p.core.entitybeans.ResourceCharacteristicsEntity;
import com.techm.c3p.core.entitybeans.ResourceCharacteristicsHistoryEntity;
import com.techm.c3p.core.entitybeans.RfoDecomposedEntity;
import com.techm.c3p.core.entitybeans.ServiceOrderEntity;
import com.techm.c3p.core.entitybeans.UserManagementEntity;
import com.techm.c3p.core.entitybeans.VendorCommandEntity;
import com.techm.c3p.core.entitybeans.WebServiceEntity;
import com.techm.c3p.core.mapper.RequestInfoMappper;
import com.techm.c3p.core.pojo.RequestInfoCreateConfig;
import com.techm.c3p.core.pojo.RequestInfoPojo;
import com.techm.c3p.core.repositories.DeviceDiscoveryRepository;
import com.techm.c3p.core.repositories.RequestInfoDetailsRepositories;
import com.techm.c3p.core.repositories.ResourceCharacteristicsHistoryRepository;
import com.techm.c3p.core.repositories.ResourceCharacteristicsRepository;
import com.techm.c3p.core.repositories.RfoDecomposedRepository;
import com.techm.c3p.core.repositories.ServiceOrderRepo;
import com.techm.c3p.core.repositories.UserManagementRepository;
import com.techm.c3p.core.repositories.VendorCommandRepository;
import com.techm.c3p.core.repositories.WebServiceRepo;
import com.techm.c3p.core.service.BackupCurrentRouterConfigurationService;
import com.techm.c3p.core.service.DcmConfigService;
import com.techm.c3p.core.utility.C3PCoreAppLabels;
import com.techm.c3p.core.utility.InvokeFtl;
import com.techm.c3p.core.utility.PythonServices;
import com.techm.c3p.core.utility.TextReport;
import com.techm.c3p.core.utility.UtilityMethods;
import com.techm.c3p.core.utility.WAFADateUtil;

@Component
public class RequestInfoDetailsDao {
	private static final Logger logger = LogManager.getLogger(RequestInfoDetailsDao.class);
	@Autowired
	private RequestInfoDetailsRepositories reository;
	@Autowired
	private ServiceOrderRepo serviceOrderRepo;
	@Autowired
	private RfoDecomposedRepository rfoDecomposedRepo;
	@Autowired
	private ResourceCharacteristicsHistoryRepository resourceCharHistoryRepo; 
	@Autowired
	private ResourceCharacteristicsRepository resourceCharRepo; 
	@Autowired
	private UserManagementRepository userManagementRepository;
	@Autowired
	private DeviceDiscoveryRepository deviceDiscoveryRepository;
	@Autowired
	private DcmConfigService dcmConfigService;
	@Autowired
	private WebServiceRepo webservicerepo;	
	@Autowired
	private BackupCurrentRouterConfigurationService backupCurrentRouterConfigurationService;	
	@Autowired
	private PythonServices pythonService;
	@Autowired
	private VendorCommandRepository vendorCommandRepository;
	@Autowired
	private JDBCConnection jDBCConnection;
	@Autowired
	private WAFADateUtil dateUtil;	
	
	private static final String JSCH_CONFIG_INPUT_BUFFER= "max_input_buffer_size";
	
	public void editRequestforReportWebserviceInfo(String requestId, String version, String field, String flag,
			String status) {
		String query = null;
		
		if ("health_check".equalsIgnoreCase(field)) {
			query = "update webserviceinfo set health_checkup = ? where alphanumeric_req_id = ? and version = ? ";
		} else if ("deliver_configuration".equalsIgnoreCase(field)) {
			query = "update webserviceinfo set deliever_config = ? where alphanumeric_req_id = ? and version = ? ";
		} else if ("network_test".equalsIgnoreCase(field)) {
			query = "update webserviceinfo set network_test = ? where alphanumeric_req_id = ? and version = ? ";
		} else if ("deliever_config".equalsIgnoreCase(field)) {
			query = "update webserviceinfo set deliever_config = ? where alphanumeric_req_id = ? and version = ? ";
		} else if ("Application_test".equalsIgnoreCase(field)) {
			query = "update webserviceinfo set application_test = ? where alphanumeric_req_id = ? and version = ? ";
		} else if ("customer_report".equalsIgnoreCase(field)) {
			query = "update webserviceinfo set customer_report = ? where alphanumeric_req_id = ? and version = ? ";
		} else if ("generate_configuration".equalsIgnoreCase(field)) {
			query = "update webserviceinfo set generate_config = ? where alphanumeric_req_id = ? and version = ? ";
		} else if ("pre_health_checkup".equalsIgnoreCase(field)) {
			query = "update webserviceinfo set pre_health_checkup = ? where alphanumeric_req_id = ? and version = ? ";
		} else if ("others_test".equalsIgnoreCase(field)) {
			query = "update webserviceinfo set others_test = ? where alphanumeric_req_id = ? and version = ? ";
		} else if ("network_audit".equalsIgnoreCase(field)) {
			query = "update webserviceinfo set network_audit = ? where alphanumeric_req_id = ? and version = ? ";
		}else if ("post_health_checkup".equalsIgnoreCase(field)) {
			query = "update webserviceinfo set health_checkup = ? where alphanumeric_req_id = ? and version = ? ";
		}
		else if ("instantiation".equalsIgnoreCase(field)) {
			query = "update webserviceinfo set instantiation = ? where alphanumeric_req_id = ? and version = ? ";
		}
		else if ("cnfinstantiation".equalsIgnoreCase(field)) {
			query = "update webserviceinfo set cnfinstantiation = ? where alphanumeric_req_id = ? and version = ? ";
		}
		else if("preprocess".equals(field)) {
			query = "update webserviceinfo set preprocess = ? where alphanumeric_req_id = ? and version = ? ";
		}


		try(Connection connection = jDBCConnection.getConnection();
				PreparedStatement preparedStmt = connection.prepareStatement(query);) {			
			preparedStmt.setString(1, flag);
			preparedStmt.setString(2, requestId);
			preparedStmt.setString(3, version);
			preparedStmt.executeUpdate();
		} catch (SQLException exe) {
			logger.error("SQL Exception in editRequestforReportWebserviceInfo update webserviceinfo method "+exe.getMessage());
		}
		logger.info("editRequestforReportWebserviceInfo - field ->"+field);
		logger.info("editRequestforReportWebserviceInfo - status ->"+status);
		logger.info("pythonService ->"+pythonService);
		if (field.equalsIgnoreCase("customer_report") && status.contains("Success")) {
			Double finalVersion = Double.valueOf(version);
			RequestInfoEntity request = reository.findByAlphanumericReqIdAndRequestVersion(requestId, finalVersion);
			try {
				if (request.getAlphanumericReqId() != null && !request.getAlphanumericReqId().isEmpty()) {
					Date date = new Date();
					Timestamp timestamp = new Timestamp(date.getTime());
					Timestamp d = null;
					if (request.getTempElapsedTime() == null) {
						if (request.getRequestTypeFlag().equals("M")) {
							Date dateofProcessing = request.getDateofProcessing();
							d = new java.sql.Timestamp(dateofProcessing.getTime());
						} else {
							Date dateofProcessing = request.getSceheduledTime();
							d = new java.sql.Timestamp(dateofProcessing.getTime());
						}
						String diff = calcTimeDiffInMins(timestamp, d);
						diff = StringUtils.replace(diff, ".", ":");
						diff = "00:" + diff;
						reository.updateElapsedTimeStatus(status, timestamp, diff, requestId, finalVersion);
					} else {
						Timestamp d1 = null;
						date = new java.util.Date();
						timestamp = new java.sql.Timestamp(date.getTime());
						d1 = request.getTempProcessingTime();
						String diff1 = calcTimeDiffInMins(timestamp, d1);
						Timestamp tempElapsedTime = request.getTempElapsedTime();
						String elapTime = tempElapsedTime.toString();
						String diff2 = String.format("%.2f",
								Float.toString((Float.parseFloat(diff1) + Float.parseFloat(elapTime))));
						diff2 = StringUtils.replace(diff2, ".", ":");
						diff2 = "00:" + diff2;
						reository.updateElapsedTimeStatus(status, timestamp, diff2, requestId, finalVersion);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();

			}
			ServiceOrderEntity ent = serviceOrderRepo.findByRequestId(requestId);
			
			if (ent != null) {
				serviceOrderRepo.updateStatusAndRequestId(requestId, "Success", ent.getServiceOrder(), "admin", Timestamp.valueOf(LocalDateTime.now()));
			}
			
			RfoDecomposedEntity rfoDecomposedEntity=rfoDecomposedRepo.findByOdRequestIdAndOdRequestVersion(requestId, Double.valueOf(version));
			if(rfoDecomposedEntity != null)
			{
				//rfoDecomposedRepo.updateStatus("Success", Global.loggedInUser, Timestamp.valueOf(LocalDateTime.now()), requestId, Double.valueOf(version));

				rfoDecomposedRepo.updateStatus("Success", "Admin", Timestamp.valueOf(LocalDateTime.now()), requestId, Double.valueOf(version));
				//To uncomment when python services available
				//pythonService.runNextRequest(rfoDecomposedEntity.getOdRfoId());
				logger.info("Before calling customer report sucess status runDecomposeWorkflow");
				pythonService.runDecomposeWorkflow(rfoDecomposedEntity.getOdRfoId());
			}
			// update the request status column to success after Configuration Request gets
			// Completed Successfully.
			List<ResourceCharacteristicsHistoryEntity> charHistoryEnity = resourceCharHistoryRepo
					.findBySoRequestId(requestId);
			charHistoryEnity.forEach(entity -> {
				entity.setRcRequestStatus("Success");
				resourceCharHistoryRepo.save(entity);
			});
			// INSERT OR update table after Configuration Request gets Completed
			// Successfully
			for (ResourceCharacteristicsHistoryEntity attributes : charHistoryEnity) {
				ResourceCharacteristicsEntity resourceCharEntity  = new ResourceCharacteristicsEntity();
				if(attributes.getRcCharacteristicId()!= null) {
					ResourceCharacteristicsEntity resourceCharEntityData = resourceCharRepo
						.findByDeviceIdAndRcFeatureIdAndRcCharacteristicIdAndRcKeyValue(attributes.getDeviceId(), attributes.getRcFeatureId(),
								attributes.getRcCharacteristicId(), attributes.getRcKeyValue());
					if(resourceCharEntityData==null) {
						resourceCharEntity.setRcFeatureId(attributes.getRcFeatureId());
						resourceCharEntity.setRcCharacteristicId(attributes.getRcCharacteristicId());
						resourceCharEntity.setRcCharacteristicName(attributes.getRcName());
						resourceCharEntity.setRcCharacteristicValue(attributes.getRcValue());
						resourceCharEntity.setRcKeyValue(attributes.getRcKeyValue());
						resourceCharEntity.setRcFeatureId(attributes.getRcFeatureId());			
						resourceCharEntity.setDeviceId(attributes.getDeviceId());
						resourceCharEntity.setRcDeviceHostname(attributes.getRcDeviceHostname());
						resourceCharEntity.setRc_created_date(new Timestamp(new Date().getTime()));
						resourceCharEntity.setRc_updated_date(new Timestamp(new Date().getTime()));
						resourceCharRepo.save(resourceCharEntity);
					}
					
				}else {
				ResourceCharacteristicsEntity featureData = resourceCharRepo.findByDeviceIdAndRcFeatureId(attributes.getDeviceId(), attributes.getRcFeatureId());
				if(featureData==null) {
					resourceCharEntity.setRcFeatureId(attributes.getRcFeatureId());			
					resourceCharEntity.setDeviceId(attributes.getDeviceId());
					resourceCharEntity.setRcDeviceHostname(attributes.getRcDeviceHostname());
					resourceCharEntity.setRc_created_date(new Timestamp(new Date().getTime()));
					resourceCharEntity.setRc_updated_date(new Timestamp(new Date().getTime()));
					resourceCharRepo.save(resourceCharEntity);
					}
				}
			}
			
			if(request.getAlphanumericReqId().substring(0, Math.min(request.getAlphanumericReqId().length(), 4)).equalsIgnoreCase("SLGB"))
			{
				//If request is back up and is successful then only execute this block.
				//Find the baseline version for this hostname
				RequestInfoEntity entity = reository.findByHostnameAndIsBaseline(request.getHostName(), "1");
				//fetch the running configuration for this request.
				if(entity!=null)
				{
					String baseLineFilePath=C3PCoreAppLabels.RESPONSE_DOWNLOAD_PATH.getValue()+
							entity.getAlphanumericReqId() + "V"
									+ Double.toString(entity.getRequestVersion())
									+ "_PreviousConfig.txt";
					String currentRunningFilePath=C3PCoreAppLabels.RESPONSE_DOWNLOAD_PATH.getValue()+
							request.getAlphanumericReqId() + "V"
							+ Double.toString(request.getRequestVersion())
							+ "_PreviousConfig.txt";
					if(baseLineFilePath!=null && currentRunningFilePath!=null)
					{
						//Call python service to fetch delta between two files
						Boolean resp=pythonService.pythonDeltaCompute(baseLineFilePath, currentRunningFilePath);
						//If resp is true update request info r_has_delta_with_baseline flag for this request to 1
						if(resp == true)
						{
							int res=reository.updatehasdeltawithbaseline(true, request.getAlphanumericReqId(), request.getRequestVersion());
							if(res==0)
							{
								logger.error("Could not update the record for has delta flag for request id:::",request.getAlphanumericReqId());	
							}
						}
						else
						{
							logger.error("No delta found for request id::::",request.getAlphanumericReqId());	
	
						}
						
					}
					else
					{
						logger.error("Backup files not found",baseLineFilePath);	
						logger.error("Backup files not found::: baseline path %s",baseLineFilePath);	
						logger.error("Backup files not found::: currentRunning path %s",currentRunningFilePath);	
					}
				}
				else
				{
					logger.error("No backup baselined for this version");
				}
			}
		} else if (field.equalsIgnoreCase("customer_report") && status.equals("Failure") && "2".equals(flag)) {
			Double finalVersion = Double.valueOf(version);
			RequestInfoEntity request = reository.findByAlphanumericReqIdAndRequestVersion(requestId, finalVersion);
			try {
				java.util.Date date = new java.util.Date();
				java.sql.Timestamp timestamp = new java.sql.Timestamp(date.getTime());
				Timestamp d = null;
				if (request.getAlphanumericReqId() != null && !request.getAlphanumericReqId().isEmpty()) {
					if (request.getRequestTypeFlag().equals("M")) {
						Date dateofProcessing = request.getDateofProcessing();
						d = new java.sql.Timestamp(dateofProcessing.getTime());
					} else {
						Date dateofProcessing = request.getSceheduledTime();
						d = new java.sql.Timestamp(dateofProcessing.getTime());
					}
					String diff = calcTimeDiffInMins(timestamp, d);
					diff = StringUtils.replace(diff, ".", ":");
					diff = "00:" + diff;
					reository.updateElapsedTimeStatus(status, timestamp, diff, requestId, finalVersion);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			ServiceOrderEntity ent = serviceOrderRepo.findByRequestId(requestId);
		
			if (ent != null) 
				serviceOrderRepo.updateStatusAndRequestId(requestId, "Failure", ent.getServiceOrder(), "admin", Timestamp.valueOf(LocalDateTime.now()));
			
			RfoDecomposedEntity rfoDecomposedEntity=rfoDecomposedRepo.findByOdRequestIdAndOdRequestVersion(requestId, Double.valueOf(version));
			if(rfoDecomposedEntity != null)
			{
				//rfoDecomposedRepo.updateStatus("Failure", Global.loggedInUser, Timestamp.valueOf(LocalDateTime.now()), requestId, Double.valueOf(version));

				rfoDecomposedRepo.updateStatus("Failure", "Admin", Timestamp.valueOf(LocalDateTime.now()), requestId, Double.valueOf(version));
				//To uncomment when python services available
				//pythonService.runNextRequest(rfoDecomposedEntity.getOdRfoId());
				logger.info("Before calling customer report failure status runDecomposeWorkflow");
				pythonService.runDecomposeWorkflow(rfoDecomposedEntity.getOdRfoId());

			}
			// update the request status column to Failure after Configuration Request gets
			// Failed.
			List<ResourceCharacteristicsHistoryEntity> charHistoryEnity = resourceCharHistoryRepo
					.findBySoRequestId(requestId);
			charHistoryEnity.forEach(entity -> {
				entity.setRcRequestStatus("Failure");
				resourceCharHistoryRepo.save(entity);
			});
			
		} else {
			try {
				Double finalVersion = Double.valueOf(version);
				Date date = new Date();
				Timestamp timestamp = new Timestamp(date.getTime());				
				reository.updateElapsedTimeStatus(status, timestamp, "00:00:00", requestId, finalVersion);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/* method Added for UIREVAmp */

	public RequestInfoPojo getRequestDetailTRequestInfoDBForVersion(String requestId, String version) {
		RequestInfoPojo pojo = null;
		try {
			Double finalVersion = Double.valueOf(version);
			RequestInfoEntity entity = reository.findByAlphanumericReqIdAndRequestVersion(requestId, finalVersion);
			if (entity != null) {
				pojo = new RequestInfoPojo();
				pojo.setInfoId(entity.getInfoId());
				pojo.setAlphanumericReqId(entity.getAlphanumericReqId());
				pojo.setManagementIp(entity.getManagmentIP());
				pojo.setTemplateID(entity.getTemplateUsed());
				pojo.setRequestCreatedOn(entity.getDateofProcessing().toString());
				pojo.setRequestVersion(entity.getRequestVersion());
				pojo.setRequestParentVersion(entity.getRequestParentVersion());
				pojo.setModel(entity.getModel());
				pojo.setVendor(entity.getVendor());
//				pojo.setDeviceType(entity.getDeviceType());
				pojo.setFamily(entity.getFamily());
				pojo.setOs(entity.getOs());
				pojo.setOsVersion(entity.getOsVersion());
				pojo.setRegion(entity.getRegion());
				pojo.setCertificationSelectionBit(entity.getCertificationSelectionBit());
				pojo.setHostname(entity.getHostName());
				pojo.setStatus(entity.getStatus());
				pojo.setNetworkType(entity.getNetworkType());
				pojo.setRequestCreatorName(entity.getRequestCreatorName());
				pojo.setStartUp(entity.getStartUp());
				pojo.setConfigurationGenerationMethods(entity.getrConfigGenerationMethod());
				pojo.setCustomer(entity.getCustomer());
				pojo.setSiteid(entity.getSiteId());
				pojo.setSiteName(entity.getSiteName());
				pojo.setCloudName(entity.getrCloudName());
				pojo.setClustername(entity.getrClusterName());
				pojo.setClusterid(entity.getrClusterId());
				pojo.setRequestType(entity.getRequestType());
				if(entity.getrConfigGenerationMethod()!=null) {
					pojo.setConfigurationGenerationMethods(entity.getrConfigGenerationMethod());
				}
				
			}
		} catch (Exception e) {
			logger.error("Exception in getRequestDetailTRequestInfoDBForVersion method " +e);
		}
		return pojo;
	}

	/* Change requestStatus for new Table */
	@Transactional
	public boolean changeRequestInRequestInfoStatus(String requestid, String version, String status) {

		int updateStatus = 0;
		boolean result = false;
		try {
			Double finalVersion = Double.valueOf(version);
			RequestInfoEntity entity = reository.findByAlphanumericReqIdAndRequestVersion(requestid, finalVersion);
			if (entity.getInfoId() > 0) {
				updateStatus = reository.updateStatus(status, entity.getInfoId());
			}
			if (updateStatus < 0) {
				result = true;
			} else {
				result = false;
			}

		} catch (Exception e) {
			logger.error("Exception in changeRequestInRequestInfoStatus method " +e);
		}

		return result;
	}

	public String calcTimeDiffInMins(Timestamp t1, Timestamp t2) {

		// create a calendar and assign it the same time
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(t1.getTime());

		// add a bunch of seconds to the calendar
		cal.add(Calendar.SECOND, 98765);

		// get time difference in seconds
		long milliseconds = t1.getTime() - t2.getTime();
		/*
		 * int seconds = (int) milliseconds / 1000; int minutes = (seconds % 3600) / 60;
		 * String str = minutes + "." + seconds;
		 */
		logger.info("we are here" + milliseconds);
		return String.format("%02d.%02d", TimeUnit.MILLISECONDS.toMinutes(milliseconds),
				TimeUnit.MILLISECONDS.toSeconds(milliseconds)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
	}

	@Transactional
	public boolean changeRequestOwner(String requestid, String version, String owner) {
		boolean result = false;
		int res = 0;
		String userRole = null;
		try {
			Double finalVersion = Double.valueOf(version);
			List<UserManagementEntity> userRoleDetails = userManagementRepository.findByUserName(owner);
			if(!userRoleDetails.isEmpty())
				userRole = userRoleDetails.get(0).getRole();
			if (userRole.equalsIgnoreCase("seuser")) {
				res = reository.updateRequestOwner(owner, false, true, requestid, finalVersion);
			} else if (userRole.equalsIgnoreCase("feuser")) {
				res = reository.updateRequestOwner(owner, true, false, requestid, finalVersion);
			}
			if (res != 0) {
				result = true;
			} else {
				result = false;
			}
		} catch (Exception e) {
			logger.error("Exception in changeRequestOwner method " +e);
			e.printStackTrace();
		}
		return result;
	}

	@Transactional
	public boolean changeRequestStatus(String requestid, String version, String status) {
		boolean result = false;
		try {
			Double finalVersion = Double.valueOf(version);
			Date date = new Date();
			Timestamp timestamp = new Timestamp(date.getTime());
			int val = reository.updateStatus(status, timestamp, requestid, finalVersion);
			if (val < 0) {
				result = true;
			} else {
				result = false;
			}
		} catch (Exception e) {
			logger.error("Exception in changeRequestStatus method " +e);
			e.printStackTrace();
		}
		return result;
	}

	public List<RequestInfoCreateConfig> getOwnerAssignedRequestList(String ownerName) {
		List<RequestInfoCreateConfig> list = new ArrayList<RequestInfoCreateConfig>();
		try {
			List<RequestInfoEntity> requestByOwner = reository.findByRequestOwnerName(ownerName);
			RequestInfoMappper mapper = new RequestInfoMappper();
			for (RequestInfoEntity entity : requestByOwner) {
				RequestInfoCreateConfig setEntityToPojo = mapper.setEntityToPojo(entity);
				if (setEntityToPojo != null) {
					list.add(setEntityToPojo);
				}
			}
		} catch (Exception e) {
			logger.error("Exception in getOwnerAssignedRequestList method " +e);
			e.printStackTrace();
		}
		return list;
	}


	
	@Transactional
	public void setReadFlagFESE(String requestId, String version, boolean status, String key) {
		Double finalVersion = Double.valueOf(version);
		String keyValue = key.toUpperCase();
		switch (keyValue) {
		case "FE":
			reository.upadateFeuser(status, requestId, finalVersion);
			break;
		case "SE":
			reository.upadateSeuser(status, requestId, finalVersion);
			break;

		default:
			break;
		}

	}

	private List<RequestInfoCreateConfig> setEntityDate(List<RequestInfoEntity> requestEntity) {
		List<RequestInfoCreateConfig> pojoList = new ArrayList<>();
		if (!requestEntity.isEmpty()) {
			requestEntity.forEach(entity -> {
				RequestInfoCreateConfig pojo = new RequestInfoCreateConfig();
				RequestInfoMappper mapper = new RequestInfoMappper();
				pojo = mapper.setEntityToPojo(entity);
				pojoList.add(pojo);
			});
		}

		return pojoList;
	}

	public List<RequestInfoCreateConfig> getCertificationtestvalidation(String value,Double version) {
		List<RequestInfoCreateConfig> requestInfoList = new ArrayList<>();
		List<RequestInfoEntity> requestEntity = reository.findAllByAlphanumericReqIdAndRequestVersion(value,version);
		requestEntity.forEach(entity -> {
			RequestInfoCreateConfig pojo = new RequestInfoCreateConfig();
			pojo.setAlphanumericReqId(entity.getAlphanumericReqId());
			pojo.setInfoId(entity.getInfoId());
			pojo.setCertificationSelectionBit(entity.getCertificationSelectionBit());
			pojo.setRequestVersion(entity.getRequestVersion());
			requestInfoList.add(pojo);
		});

		return requestInfoList;
	}

	public List<RequestInfoCreateConfig> getAllResquestsFromDB(String userRole) throws IOException {
		List<RequestInfoCreateConfig> requestInfoList1 = new ArrayList<>();
		String user = userRole;
		List<RequestInfoEntity> entity = new ArrayList<>();
		switch (user) {
		case "feuser":
			entity = reository.findAllByStatusNotInAndRequestOwnerName("Cancelled", user);
			break;
		case "seuser":
			entity = reository.findAllByStatusNotInAndRequestCreatorName("Cancelled", user);
			break;
		default:
			entity = reository.findAllByStatusNotInAndImportStatusIsNullOrImportStatusIn("Cancelled", "Success");
			break;
		}
		RequestInfoMappper mapper = new RequestInfoMappper();
		entity.forEach(value -> {
			String type = value.getAlphanumericReqId().substring(0, Math.min(value.getAlphanumericReqId().length(), 4));

			if (!(type.equals("SLGB"))) {
				RequestInfoCreateConfig setEntityToPojo = mapper.setEntityToPojo(value);
				requestInfoList1.add(setEntityToPojo);
			}

		});

		return requestInfoList1;

	}

	public List<RequestInfoCreateConfig> getRequestWithVersion(String key, String requestId, String version, String userName, String userRole)
			throws IOException {
		List<RequestInfoEntity> requestEntity = null;
		//Global.loggedInUser = "admin";
		Double finalVersion = Double.valueOf(version);
		if (!userRole.equalsIgnoreCase("admin")) {
			if (key.equalsIgnoreCase("Request ID") || key.equalsIgnoreCase("Request")) {
				requestEntity = reository.findAllByAlphanumericReqIdAndRequestVersionAndRequestCreatorName(requestId,
						finalVersion, userName);
				return setEntityDate(requestEntity);
			}
			if (key.equalsIgnoreCase("Region")) {
				requestEntity = reository.findAllByRegionContainingAndRequestCreatorName(key, userName);
				return setEntityDate(requestEntity);
			}
		}
		if (key.equalsIgnoreCase("Request ID") || key.equalsIgnoreCase("Request")) {
			requestEntity = reository.findAllByAlphanumericReqIdAndRequestVersion(requestId, finalVersion);
			return setEntityDate(requestEntity);
		} else if (key.equalsIgnoreCase("Region")) {
			requestEntity = reository.findAllByRegionContaining(key);
			return setEntityDate(requestEntity);
		}

		return null;
	}

	/* method overloading for UIRevamp */
	
	public boolean getRouterConfig(RequestInfoPojo requestinfo, String routerVersionType) {
		
		InvokeFtl invokeFtl = new InvokeFtl();	
		boolean backupdone = false;
		JSch jsch = new JSch();
		Channel channel = null;
		Session session = null;
		try {
			String host = requestinfo.getManagementIp();
			DeviceDiscoveryEntity deviceDetails = deviceDiscoveryRepository
					.findByDHostNameAndDMgmtIpAndDDeComm(requestinfo.getHostname(),requestinfo.getManagementIp(),"0");
			
			CredentialManagementEntity routerCredential = dcmConfigService.getRouterCredential(
					deviceDetails);
			String user = routerCredential.getLoginRead();
			String password = routerCredential.getPasswordWrite();


			session = jsch.getSession(user, host, Integer.parseInt(C3PCoreAppLabels.PORT_SSH.getValue()));
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			config.put(JSCH_CONFIG_INPUT_BUFFER, C3PCoreAppLabels.JSCH_CHANNEL_INPUT_BUFFER_SIZE.getValue());
			session.setConfig(config);
			session.setPassword(password);
			session.connect();
			UtilityMethods.sleepThread(3000);
			InputStream input = null;
			try {
				channel = session.openChannel("shell");
				OutputStream ops = channel.getOutputStream();

				PrintStream printStream = new PrintStream(ops, true);
				logger.info("Channel Connected to machine " + host + " server");
				channel.connect();
				input = channel.getInputStream();
				//ps.println("terminal length 0");
				//ps.println("show run");
				printStream = setCommandStream(printStream, requestinfo, "backup", false);
				UtilityMethods.sleepThread(25000);
				logger.info("RequestInfoDetailsDao - getRouterConfig - Total size of the Channel InputStream -->"+input.available());
				if (routerVersionType.equalsIgnoreCase("previous")) {
					backupdone = true;
					backupCurrentRouterConfigurationService.printPreviousVersionInfo(input, channel, requestinfo.getAlphanumericReqId(),
							Double.toString(requestinfo.getRequestVersion()));
				} else {
					backupdone = true;
					backupCurrentRouterConfigurationService.printCurrentVersionInfo(input, channel, requestinfo.getAlphanumericReqId(),
							Double.toString(requestinfo.getRequestVersion()));
				}
		
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				if(input !=null) {
					input.close();
				}
			}
		}

		catch (Exception ex) {
			logger.error("Exception occure inside backup.."+ex.getMessage());
			String response = "";
			try {
				editRequestforReportWebserviceInfo(requestinfo.getAlphanumericReqId(),
						Double.toString(requestinfo.getRequestVersion()), "deliever_config", "2", "Failure");
				response = invokeFtl.generateDeliveryConfigFileFailure(requestinfo);
				TextReport.writeFile(C3PCoreAppLabels.RESPONSE_DOWNLOAD_PATH.getValue(), requestinfo.getAlphanumericReqId() + "V"
						+ Double.toString(requestinfo.getRequestVersion()) + "_deliveredConfig.txt", response,null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		finally {

			if (channel != null) {
				try {
					session = channel.getSession();

					if (channel.getExitStatus() == -1) {

						UtilityMethods.sleepThread(5000);

					}
				} catch (Exception e) {
					logger.error("Exception in getRouterConfig" +e.getMessage());
				}
				channel.disconnect();
				session.disconnect();
			}
		}
		return backupdone;

	}

	public String getPreviousMileStoneStatus(String alphanumericReqId, Double version) {
		RequestInfoEntity requestEntity = reository.findByAlphanumericReqIdAndRequestVersion(alphanumericReqId,
				version);
		return requestEntity.getStatus();
	}

	public int getStatusForMilestone(String alphanumericReqId, String version, String field) {
		String query = null;
		ResultSet rs = null;
		int status = 0;
		if ("health_check".equalsIgnoreCase(field)) {
			query = "select health_checkup as dataValue  from  webserviceinfo  where alphanumeric_req_id = ? and version = ? ";
		} else if ("network_test".equalsIgnoreCase(field)) {
			query = "select  network_test as dataValue from webserviceinfo  where alphanumeric_req_id = ? and version = ? ";
		} else if ("others_test".equalsIgnoreCase(field)) {
			query = "select others_test as dataValue from  webserviceinfo where alphanumeric_req_id = ? and version = ? ";
		} else if ("network_audit".equalsIgnoreCase(field)) {
			query = "select network_audit as dataValue from  webserviceinfo where alphanumeric_req_id = ? and version = ? ";
		}
		else if ("instantiation".equalsIgnoreCase(field)) {
			query = "select instantiation as dataValue from  webserviceinfo where alphanumeric_req_id = ? and version = ? ";
		}else if ("preprocess".equalsIgnoreCase(field)) {
			query = "select preprocess as dataValue from  webserviceinfo where alphanumeric_req_id = ? and version = ? ";
		}
		else if ("cnfinstantiation".equalsIgnoreCase(field)) {
			query = "select cnfinstantiation as dataValue from  webserviceinfo where alphanumeric_req_id = ? and version = ? ";
		}
		if ("pre_health_checkup".equalsIgnoreCase(field)) {
			query = "select pre_health_checkup as dataValue  from  webserviceinfo  where alphanumeric_req_id = ? and version = ? ";
		}
		if ("post_health_checkup".equalsIgnoreCase(field)) {
			query = "select health_checkup as dataValue  from  webserviceinfo  where alphanumeric_req_id = ? and version = ? ";
		}

		try(Connection connection = jDBCConnection.getConnection();
				PreparedStatement preparedStmt = connection.prepareStatement(query);) {
			preparedStmt.setString(1, alphanumericReqId);
			preparedStmt.setString(2, version);
			rs = preparedStmt.executeQuery();

			while (rs.next()) {
				status = rs.getInt("dataValue");
			}
		} catch (SQLException exe) {
			logger.error("SQL Exception in getStatusForMilestone method "+exe.getMessage());
		}finally {
			DBUtil.close(rs);
		}
		return status;
	}

	public List<ResourceCharacteristicsHistoryEntity>getListResourcesfromChHistory(String requestid)
	{
		List<ResourceCharacteristicsHistoryEntity>list=new ArrayList<>();
		list=resourceCharHistoryRepo.findBySoRequestId(requestid);
		return list;
	}
	public String reasonForInstantiationFailure(String requestid, Double version)
	{
		String reason=null;
		WebServiceEntity entity=webservicerepo.findTextFoundDeliveryTestByAlphanumericReqIdAndVersion(requestid,version);
		reason=entity.getTextFoundDeliveryTest();
		return reason;
	}
	
	/**
	 * This method is useful to fetch the Image instance information from
	 * c3p_deviceinfo_ext table.
	 * 
	 * @param deviceId
	 * @return imageInstanceId
	 */
	public String fetchImageInstanceFromDeviceExt(String deviceId) {
		String imageInstanceId = null;
		String sqlQuery = "select r_imageInstanceId from c3p_deviceinfo_ext where r_device_id = ?";
		try (Connection connection = jDBCConnection.getConnection();
				PreparedStatement preparedStmt = connection.prepareStatement(sqlQuery);) {
			preparedStmt.setString(1, deviceId);
			ResultSet rs = preparedStmt.executeQuery();

			while (rs.next()) {
				imageInstanceId = rs.getString("r_imageInstanceId");
			}
		} catch (SQLException exe) {
			logger.error("SQL Exception in fetchImageInstanceFromDeviceExt method " + exe.getMessage());
		}
		return imageInstanceId;
	}
	
	/**
	 * 
	 * @param deviceId
	 * @return location
	 */
	@SuppressWarnings("unchecked")
	public JSONObject fetchFromDeviceExtLocationDescription(String deviceId) {
		JSONObject response=new JSONObject();
		String sqlQuery = "select r_latitude,r_longitude,r_description from c3p_deviceinfo_ext where r_device_id = ?";
		try (Connection connection = jDBCConnection.getConnection();
				PreparedStatement preparedStmt = connection.prepareStatement(sqlQuery);) {
			preparedStmt.setString(1, deviceId);
			ResultSet rs = preparedStmt.executeQuery();
			while (rs.next()) {
				response.put("lat", rs.getString("r_latitude"));
				response.put("long", rs.getString("r_longitude"));
				response.put("description", rs.getString("r_description"));
				
			}
		} catch (SQLException exe) {
			logger.error("SQL Exception in fetchImageInstanceFromDeviceExt method " + exe.getMessage());
		}
		return response;
	}
	
	/**
	 * This method is useful to fetch the oeprational status from
	 * c3p_deviceinfo_ext table.
	 * 
	 * @param deviceId
	 * @return imageInstanceId
	 */
	public String fetchOprStatusDeviceExt(String deviceId) {
		String imageInstanceId = null;
		String sqlQuery = "select r_opertionalState from c3p_deviceinfo_ext where r_device_id = ?";
		try (Connection connection = jDBCConnection.getConnection();
				PreparedStatement preparedStmt = connection.prepareStatement(sqlQuery);) {
			preparedStmt.setString(1, deviceId);
			ResultSet rs = preparedStmt.executeQuery();

			while (rs.next()) {
				imageInstanceId = rs.getString("r_opertionalState");
			}
		} catch (SQLException exe) {
			logger.error("SQL Exception in fetchImageInstanceFromDeviceExt method " + exe.getMessage());
		}
		return imageInstanceId;
	}
	public void saveInDeviceExtension(String deviceId, String modelDescription) {
		String sqlQuery = "INSERT INTO c3p_deviceinfo_ext(r_device_id, r_description) "
				+ "VALUES (?,?)";
		try (Connection connection = jDBCConnection.getConnection();
				PreparedStatement preparedStmt = connection.prepareStatement(sqlQuery);) {
			preparedStmt.setString(1, deviceId);
			preparedStmt.setString(2, modelDescription);
			preparedStmt.executeUpdate();
		} catch (SQLException exe) {
			logger.error("SQL Exception in saveInDeviceExtension method " + exe.getMessage());
		}
	}
	
	public PrintStream setCommandStream(PrintStream printStream, RequestInfoPojo requestinfo, String commandType,
			Boolean isStartUp) {
		List<VendorCommandEntity> vcCommandTypes = vendorCommandRepository
				.findAllByVcVendorNameAndVcNetworkTypeAndVcOsAndVcRecordIdStartsWith(requestinfo.getVendor(),requestinfo.getNetworkType(),requestinfo.getOs(), "CB");
		for (VendorCommandEntity cmd : vcCommandTypes) {
			if (commandType.equals("Test")) {
				printStream = setModeData(cmd.getVcParentId(), printStream);
			} else {
				printStream = setModeData(cmd.getVcParentId(), printStream);
				if (cmd.getVcStart().contains("::")) {
					if (isStartUp) {						
						printStream.println(StringUtils.substringAfter(cmd.getVcStart(), "::"));
					}else {						
						printStream.println(StringUtils.substringBefore(cmd.getVcStart(), "::"));
					}
				} else {
					printStream.println(cmd.getVcStart());
				}
			}
		}
		return printStream;
	}
@SuppressWarnings("unused")
	public void saveLCMDeleteDetails(String managmentIp,String hostName,String requestId,String status) {
		DeviceDiscoveryEntity deviceData = deviceDiscoveryRepository.findHostNameAndMgmtip(managmentIp,hostName);
		deviceData.setdDecommReason("LCM Delete Request Raised");
		deviceData.setdDeComm("1");
		deviceDiscoveryRepository.save(deviceData);
		updateC3PDeviceInfoExt(deviceData.getdId());
		List<ResourceCharacteristicsHistoryEntity> charHistoryEnity = resourceCharHistoryRepo
				.findBydeviceId(deviceData.getdId());
		for (ResourceCharacteristicsHistoryEntity attributes : charHistoryEnity) {
			ResourceCharacteristicsHistoryEntity resourceCharEntity  = new ResourceCharacteristicsHistoryEntity();
			 resourceCharEntity.setDeviceId(attributes.getDeviceId());
			 resourceCharEntity.setRcDeviceHostname(attributes.getRcDeviceHostname());
			 resourceCharEntity.setSoRequestId(requestId);
			 resourceCharEntity.setRcRequestStatus(status);
			 resourceCharEntity.setRcActionPerformed("DELETE");
             resourceCharEntity.setRcFeatureId(attributes.getRcFeatureId());
			 resourceCharHistoryRepo.save(resourceCharEntity);
		}
			
	}

public final boolean updateC3PDeviceInfoExt(int id) {
	boolean result = false;
	String query = "update c3p_deviceinfo_ext set r_endOperatingDate=? WHERE r_device_id=?";
	try (Connection connection = jDBCConnection.getConnection();
			PreparedStatement ps = connection.prepareStatement(query);) {
		ps.setString(1, new Timestamp(new Date().getTime()).toString());
		ps.setInt(2, id);
		int i = ps.executeUpdate();
		if (i > 0) {
			result = true;
		}
	} catch (SQLException exe) {
		logger.error("SQL Exception in resetUsersDB method "
				+ exe.getMessage());
	}
	return result;
}

	private PrintStream setModeData(String parentId, PrintStream printStream) {
		if (parentId !=null && parentId.contains("::")) {
			String startParentId = StringUtils.substringBefore(parentId, "::");
			VendorCommandEntity startParentModeCommand = vendorCommandRepository.findByVcRecordId(startParentId);
			if (startParentModeCommand!=null && !startParentModeCommand.getVcParentId().contains("000")) {
				printStream = setModeData(startParentModeCommand.getVcParentId(), printStream);
			}
			printStream.println(startParentModeCommand.getVcStart());			
			String endParentId = StringUtils.substringAfter(parentId, "::");
			VendorCommandEntity endParentMode = vendorCommandRepository.findByVcRecordId(endParentId);
			printStream.println(endParentMode.getVcStart());
		}else {
			VendorCommandEntity startModeCommand = vendorCommandRepository.findByVcRecordId(parentId);
			if (startModeCommand!=null && !startModeCommand.getVcParentId().contains("000")) {
				printStream = setModeData(startModeCommand.getVcParentId(), printStream);
			}
			printStream.println(startModeCommand.getVcStart());			
		}
		return printStream;
	}
}
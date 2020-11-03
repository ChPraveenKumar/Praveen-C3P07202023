package com.techm.orion.dao;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.techm.orion.connection.ConnectionFactory;
import com.techm.orion.connection.DBUtil;
import com.techm.orion.entitybeans.MasterAttributes;
import com.techm.orion.entitybeans.RequestInfoEntity;
import com.techm.orion.entitybeans.ResourceCharacteristicsEntity;
import com.techm.orion.entitybeans.ResourceCharacteristicsHistoryEntity;
import com.techm.orion.entitybeans.RfoDecomposedEntity;
import com.techm.orion.entitybeans.ServiceOrderEntity;
import com.techm.orion.mapper.RequestInfoMappper;
import com.techm.orion.pojo.Global;
import com.techm.orion.pojo.RequestInfoCreateConfig;
import com.techm.orion.pojo.RequestInfoPojo;
import com.techm.orion.pojo.UserPojo;
import com.techm.orion.repositories.AttribCreateConfigRepo;
import com.techm.orion.repositories.DeviceDiscoveryRepository;
import com.techm.orion.repositories.RequestInfoDetailsRepositories;
import com.techm.orion.repositories.ResourceCharacteristicsHistoryRepository;
import com.techm.orion.repositories.ResourceCharacteristicsRepository;
import com.techm.orion.repositories.RfoDecomposedRepository;
import com.techm.orion.repositories.ServiceOrderRepo;
import com.techm.orion.service.BackupCurrentRouterConfigurationService;
import com.techm.orion.utility.InvokeFtl;
import com.techm.orion.utility.PythonServices;
import com.techm.orion.utility.TSALabels;
import com.techm.orion.utility.TextReport;

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
	private DeviceDiscoveryRepository deviceDiscoveryRepository;
	@Autowired
	private ResourceCharacteristicsRepository resourceCharRepo; 
	@Autowired
	private AttribCreateConfigRepo attribCreateConfigRepo;
	
	public void editRequestforReportWebserviceInfo(String requestId, String version, String field, String flag,
			String status) {
		String query = null;
		
		if (field.equalsIgnoreCase("health_check")) {
			query = "update webserviceinfo set health_checkup = ? where alphanumeric_req_id = ? and version = ? ";
		} else if (field.equalsIgnoreCase("deliver_configuration")) {
			query = "update webserviceinfo set deliever_config = ? where alphanumeric_req_id = ? and version = ? ";
		} else if (field.equalsIgnoreCase("network_test")) {
			query = "update webserviceinfo set network_test = ? where alphanumeric_req_id = ? and version = ? ";
		} else if (field.equalsIgnoreCase("deliever_config")) {
			query = "update webserviceinfo set deliever_config = ? where alphanumeric_req_id = ? and version = ? ";
		} else if (field.equalsIgnoreCase("Application_test")) {
			query = "update webserviceinfo set application_test = ? where alphanumeric_req_id = ? and version = ? ";
		} else if (field.equalsIgnoreCase("customer_report")) {
			query = "update webserviceinfo set customer_report = ? where alphanumeric_req_id = ? and version = ? ";
		} else if (field.equalsIgnoreCase("generate_configuration")) {
			query = "update webserviceinfo set generate_config = ? where alphanumeric_req_id = ? and version = ? ";
		} else if (field.equalsIgnoreCase("pre_health_checkup")) {
			query = "update webserviceinfo set pre_health_checkup = ? where alphanumeric_req_id = ? and version = ? ";
		} else if (field.equalsIgnoreCase("others_test")) {
			query = "update webserviceinfo set others_test = ? where alphanumeric_req_id = ? and version = ? ";
		} else if (field.equalsIgnoreCase("network_audit")) {
			query = "update webserviceinfo set network_audit = ? where alphanumeric_req_id = ? and version = ? ";
		}

		try(Connection connection = ConnectionFactory.getConnection();
				PreparedStatement preparedStmt = connection.prepareStatement(query);) {			
			preparedStmt.setString(1, flag);
			preparedStmt.setString(2, requestId);
			preparedStmt.setString(3, version);
			preparedStmt.executeUpdate();
		} catch (SQLException exe) {
			logger.error("SQL Exception in editRequestforReportWebserviceInfo update webserviceinfo method "+exe.getMessage());
		}
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
				PythonServices pythonService=new PythonServices();
				pythonService.runNextRequest(rfoDecomposedEntity.getOdRfoId());
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
			int did = deviceDiscoveryRepository.findDid(request.getHostName());
			List<MasterAttributes> featureIdAndmCharIdAndLabel = attribCreateConfigRepo
					.findfeatureCharIdAndLabel(requestId);
			for (MasterAttributes attributes : featureIdAndmCharIdAndLabel) {
				ResourceCharacteristicsEntity resourceCharEntity = resourceCharRepo
						.findByDeviceIdAndRcFeatureIdAndRcCharacteristicId(did, attributes.getMasterFID(),
								attributes.getCharacteristicId());
				if (resourceCharEntity == null)
					resourceCharEntity = new ResourceCharacteristicsEntity();
				resourceCharEntity.setRcFeatureId(attributes.getMasterFID());
				resourceCharEntity.setRcCharacteristicId(attributes.getCharacteristicId());
				resourceCharEntity.setRcCharacteristicName(attributes.getLabel());
				resourceCharEntity.setRcCharacteristicValue(attributes.getLabelValue());
				resourceCharEntity.setDeviceId(did);
				resourceCharEntity.setRcDeviceHostname(request.getHostName());
				resourceCharRepo.save(resourceCharEntity);
			}
		} else if (field.equalsIgnoreCase("customer_report") && status.equals("Failure")) {
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
				PythonServices pythonService=new PythonServices();
				pythonService.runNextRequest(rfoDecomposedEntity.getOdRfoId());
			}
			// update the request status column to Failure after Configuration Request gets
			// Failed.
			List<ResourceCharacteristicsHistoryEntity> charHistoryEnity = resourceCharHistoryRepo
					.findBySoRequestId(requestId);
			charHistoryEnity.forEach(entity -> {
				entity.setRcRequestStatus("Failure");
				resourceCharHistoryRepo.save(entity);
			});
			// INSERT OR update table after Configuration Request gets Failed
			int did = deviceDiscoveryRepository.findDid(request.getHostName());
			List<MasterAttributes> featureIdAndmCharIdAndLabel = attribCreateConfigRepo
					.findfeatureCharIdAndLabel(requestId);
			for (MasterAttributes attributes : featureIdAndmCharIdAndLabel) {
				ResourceCharacteristicsEntity resourceCharEntity = resourceCharRepo
						.findByDeviceIdAndRcFeatureIdAndRcCharacteristicId(did, attributes.getMasterFID(),
								attributes.getCharacteristicId());
				if (resourceCharEntity == null)
					resourceCharEntity = new ResourceCharacteristicsEntity();
				resourceCharEntity.setRcFeatureId(attributes.getMasterFID());
				resourceCharEntity.setRcCharacteristicId(attributes.getCharacteristicId());
				resourceCharEntity.setRcCharacteristicName(attributes.getLabel());
				resourceCharEntity.setRcCharacteristicValue(attributes.getLabelValue());
				resourceCharEntity.setDeviceId(did);
				resourceCharEntity.setRcDeviceHostname(request.getHostName());
				resourceCharRepo.save(resourceCharEntity);
			}
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
				pojo.setAlphanumericReqId(entity.getAlphanumericReqId());
				pojo.setManagementIp(entity.getManagmentIP());
				pojo.setTemplateID(entity.getTemplateUsed());
				pojo.setRequestCreatedOn(entity.getDateofProcessing().toString());
				pojo.setRequestVersion(entity.getRequestVersion());
				pojo.setRequestParentVersion(entity.getRequestParentVersion());
				pojo.setModel(entity.getModel());
				pojo.setVendor(entity.getVendor());
				pojo.setDeviceType(entity.getDeviceType());
				pojo.setFamily(entity.getFamily());
				pojo.setOs(entity.getOs());
				pojo.setOsVersion(entity.getOsVersion());
				pojo.setRegion(entity.getRegion());
				pojo.setCertificationSelectionBit(entity.getCertificationSelectionBit());
				pojo.setHostname(entity.getHostName());
				pojo.setStatus(entity.getStatus());
				pojo.setNetworkType(entity.getNetworkType());

			}
		} catch (Exception e) {
			logger.error(e);
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

		try {
			Double finalVersion = Double.valueOf(version);

			if (owner.equalsIgnoreCase("seuser")) {
				res = reository.updateRequestOwner(owner, false, true, requestid, finalVersion);
			} else if (owner.equalsIgnoreCase("feuser")) {
				res = reository.updateRequestOwner(owner, true, false, requestid, finalVersion);
			}
			if (res != 0) {
				result = true;
			} else {
				result = false;
			}
		} catch (Exception e) {
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

	public List<RequestInfoCreateConfig> getAllResquestsFromDB() throws IOException {
		List<RequestInfoCreateConfig> requestInfoList1 = new ArrayList<>();
		Global.loggedInUser = "admin";
		String user = Global.loggedInUser;
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

	public List<RequestInfoCreateConfig> getRequestWithVersion(String key, String requestId, String version)
			throws IOException {
		List<RequestInfoEntity> requestEntity = null;
		Global.loggedInUser = "admin";
		Double finalVersion = Double.valueOf(version);
		if (!Global.loggedInUser.equalsIgnoreCase("admin")) {
			if (key.equalsIgnoreCase("Request ID") || key.equalsIgnoreCase("Request")) {
				requestEntity = reository.findAllByAlphanumericReqIdAndRequestVersionAndRequestCreatorName(requestId,
						finalVersion, Global.loggedInUser);
				return setEntityDate(requestEntity);
			}
			if (key.equalsIgnoreCase("Region")) {
				requestEntity = reository.findAllByRegionContainingAndRequestCreatorName(key, Global.loggedInUser);
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
		RequestInfoDao requestInfoDao = new RequestInfoDao();
		InvokeFtl invokeFtl = new InvokeFtl();

		BackupCurrentRouterConfigurationService service = new BackupCurrentRouterConfigurationService();
		boolean backupdone = false;
		try {
			BackupCurrentRouterConfigurationService.loadProperties();
			String host = requestinfo.getManagementIp();
			UserPojo userPojo = new UserPojo();
			userPojo = requestInfoDao.getRouterCredentials(host);
			String user, password;

			String port = BackupCurrentRouterConfigurationService.TSA_PROPERTIES.getProperty("portSSH");

			JSch jsch = new JSch();

			user = userPojo.getUsername();
			password = userPojo.getPassword();
			
			Channel channel = null;
			Session session = jsch.getSession(user, host, Integer.parseInt(port));
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.setPassword(password);
			session.connect();
			try {
				Thread.sleep(10000);
			} catch (Exception ee) {
			}

			try {
				channel = session.openChannel("shell");
				OutputStream ops = channel.getOutputStream();

				PrintStream ps = new PrintStream(ops, true);
				logger.info("Channel Connected to machine " + host + " server");
				channel.connect();
				InputStream input = channel.getInputStream();
				ps.println("terminal length 0");
				ps.println("show run");
				try {
					Thread.sleep(3000);
				} catch (Exception ee) {
				}
				if (routerVersionType.equalsIgnoreCase("previous")) {
					backupdone = true;
					service.printPreviousVersionInfo(input, channel, requestinfo.getAlphanumericReqId(),
							Double.toString(requestinfo.getRequestVersion()));
				} else {
					backupdone = true;
					service.printCurrentVersionInfo(input, channel, requestinfo.getAlphanumericReqId(),
							Double.toString(requestinfo.getRequestVersion()));
				}
				channel.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		catch (Exception ex) {
			String response = "";
			try {
				BackupCurrentRouterConfigurationService.loadProperties();
				editRequestforReportWebserviceInfo(requestinfo.getAlphanumericReqId(),
						Double.toString(requestinfo.getRequestVersion()), "deliever_config", "2", "Failure");
				response = invokeFtl.generateDeliveryConfigFileFailure(requestinfo);
				TextReport.writeFile(TSALabels.RESPONSE_DOWNLOAD_PATH.getValue(), requestinfo.getAlphanumericReqId() + "V"
						+ Double.toString(requestinfo.getRequestVersion()) + "_deliveredConfig.txt", response,null);
			} catch (Exception e) {
				e.printStackTrace();
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
		if (field.equalsIgnoreCase("health_check")) {
			query = "select health_checkup as dataValue  from  webserviceinfo  where alphanumeric_req_id = ? and version = ? ";
		} else if (field.equalsIgnoreCase("network_test")) {
			query = "select  network_test as dataValue from webserviceinfo  where alphanumeric_req_id = ? and version = ? ";
		} else if (field.equalsIgnoreCase("others_test")) {
			query = "select others_test as dataValue from  webserviceinfo where alphanumeric_req_id = ? and version = ? ";
		} else if (field.equalsIgnoreCase("network_audit")) {
			query = "select network_audit as dataValue from  webserviceinfo where alphanumeric_req_id = ? and version = ? ";
		}

		try(Connection connection = ConnectionFactory.getConnection();
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

}
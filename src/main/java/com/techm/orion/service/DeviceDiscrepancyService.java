package com.techm.orion.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.techm.orion.entitybeans.DeviceDiscoveryEntity;
import com.techm.orion.entitybeans.DiscoveryDashboardEntity;
import com.techm.orion.entitybeans.DiscoveryStatusEntity;
import com.techm.orion.entitybeans.ForkDiscoveryResultEntity;
import com.techm.orion.entitybeans.ForkDiscrepancyResultEntity;
import com.techm.orion.entitybeans.HostDiscoveryResultEntity;
import com.techm.orion.entitybeans.HostDiscrepancyResultEntity;
import com.techm.orion.entitybeans.MasterOIDEntity;
import com.techm.orion.repositories.DeviceDiscoveryRepository;
import com.techm.orion.repositories.DiscoveryDashboardRepository;
import com.techm.orion.repositories.DiscoveryStatusEntityRepository;
import com.techm.orion.repositories.ForkDiscoveryResultRepository;
import com.techm.orion.repositories.ForkDiscrepancyResultRepository;
import com.techm.orion.repositories.HostDiscoveryResultRepository;
import com.techm.orion.repositories.HostDiscrepancyResultRepository;
import com.techm.orion.repositories.MasterOIDRepository;

@Service
public class DeviceDiscrepancyService {
	private static final Logger logger = LogManager.getLogger(DeviceDiscrepancyService.class);

	@Autowired
	private DeviceDiscoveryRepository discoveryRepo;
	@Autowired
	private DiscoveryDashboardRepository dashboardRepo;
	@Autowired
	private ForkDiscrepancyResultRepository forkDiscrepancyResultRepository;
	@Autowired
	private ForkDiscoveryResultRepository forkDiscoveryResultRepository;
	@Autowired
	private HostDiscrepancyResultRepository hostDiscrepancyResultRepository;
	@Autowired
	private HostDiscoveryResultRepository hostDiscoveryResultRepository;
	@Autowired
	private MasterOIDRepository masterOIDRepository;
	@Autowired
	private DiscoveryStatusEntityRepository discoveryStatusEntityRepository;

	@SuppressWarnings("unchecked")
	public JSONObject discripancyService(String discoveryId) {
		JSONObject finalObject = new JSONObject();
		DiscoveryDashboardEntity discoveryDetails = dashboardRepo.findByDisDashId(discoveryId);
		if (discoveryDetails != null) {
			logger.info("discoveryDetails  - disDash Id-" + discoveryDetails.getDisDashId());
			finalObject = getDiscoveryDetails(discoveryDetails);
			List<DiscoveryStatusEntity> details = discoveryStatusEntityRepository.findByDiscoveryId(discoveryDetails);
			logger.info("discoveryDetails  - details-" + details.size());
			JSONArray discrepancyStatusArray = new JSONArray();
			details.forEach(discoveryStatusEntity -> {
				JSONObject discrepencyObject = getDiscrepancyBasicData(discoveryStatusEntity);
				DeviceDiscoveryEntity deviceDetails = discoveryRepo.findBydMgmtIp(discoveryStatusEntity.getDsIpAddr());
				JSONArray discreapancyObjectValue = new JSONArray();
				if (deviceDetails != null) {
					if (deviceDetails.getdNewDevice() == 0) {
						discrepencyObject.put("newOrExisting", "New");
					} else {
						discrepencyObject.put("newOrExisting", "Existing");
					}
					List<HostDiscoveryResultEntity> hostDeviceDiscoveryValue = hostDiscoveryResultRepository
							.findHostDeviceDiscoveryValue(String.valueOf(deviceDetails.getdId()),
									discoveryDetails.getDisId());

					for (HostDiscoveryResultEntity hostDiscrepancy : hostDeviceDiscoveryValue) {
						discreapancyObjectValue.add(hostDiscrepancyValue(hostDiscrepancy, deviceDetails.getdVendor()));
					}
					List<ForkDiscoveryResultEntity> forkDiscrepancyValue = forkDiscoveryResultRepository
							.findHostDeviceDiscoveryValue(String.valueOf(deviceDetails.getdId()),
									discoveryDetails.getDisId());
					discreapancyObjectValue = getDiscreapncyInterfaceName(forkDiscrepancyValue,
							deviceDetails.getdVendor(), deviceDetails.getdVNFSupport(), deviceDetails.getdId(),
							discoveryDetails.getDisId(), discreapancyObjectValue);
				}

				discrepencyObject.put("discrepancy", discreapancyObjectValue);
				discrepancyStatusArray.add(discrepencyObject);
			});

			finalObject.put("discrepancyStatusArray", discrepancyStatusArray);
		}
		return finalObject;
	}

	@SuppressWarnings("unchecked")
	private JSONObject getDiscrepancyBasicData(DiscoveryStatusEntity discoveryStatusEntity) {
		JSONObject discrepencyObject = new JSONObject();
		discrepencyObject.put("dsIpAddr", discoveryStatusEntity.getDsIpAddr());
		discrepencyObject.put("dsCreatedDate", discoveryStatusEntity.getDsCreatedDate());
		discrepencyObject.put("dsCreatedBy", discoveryStatusEntity.getDsCreatedBy());
		discrepencyObject.put("dsUpdatedDate", discoveryStatusEntity.getDsUpdatedDate());
		discrepencyObject.put("dsStatus", discoveryStatusEntity.getDsStatus());
		discrepencyObject.put("dsComment", discoveryStatusEntity.getDsComment());
		discrepencyObject.put("dsDeviceId", discoveryStatusEntity.getDsDeviceId());
		discrepencyObject.put("dsHostName", discoveryStatusEntity.getDsHostName());
		discrepencyObject.put("dsDeviceFlag", discoveryStatusEntity.getDsDeviceFlag());
		return discrepencyObject;

	}

	/* return Decrepancy value to UI according to flag table */
	@SuppressWarnings("unchecked")
	public JSONObject discripancyValue(String mgmtip, String hostName) {
		DeviceDiscoveryEntity devicedetails = discoveryRepo.findAllByMgmtId(mgmtip);
		JSONObject details = new JSONObject();
		if (devicedetails != null) {
			try {
				Integer findDiscoveryId = hostDiscrepancyResultRepository
						.findDiscoveryId(String.valueOf(devicedetails.getdId()));
				JSONArray discrepancyObject = new JSONArray();
				boolean isDiscoveryData = false;
				if (findDiscoveryId != null) {
					List<HostDiscrepancyResultEntity> discrepancyDetails = hostDiscrepancyResultRepository
							.findHostDiscrepancyValueByDeviceId(String.valueOf(devicedetails.getdId()),
									findDiscoveryId);
					for (HostDiscrepancyResultEntity deviceDiscrepancy : discrepancyDetails) {
						String displayName = masterOIDRepository.findOidDisplayName(deviceDiscrepancy.getHidOIDNo(),
								devicedetails.getdVendor());
						JSONObject discrepancy = discrepancyStatusForLatestDiscover(
								deviceDiscrepancy.getHidDiscrepancyFalg(), displayName,
								deviceDiscrepancy.getHidExistingValue(), deviceDiscrepancy.getHidDiscoverValue(), true);
						discrepancy.put("oid", deviceDiscrepancy.getHidOIDNo());
						discrepancy.put("childOid", "");
						discrepancyObject.add(discrepancy);
					}
				}
				Integer findForkDiscoveryId = forkDiscrepancyResultRepository
						.findForkDiscoveryId(String.valueOf(devicedetails.getdId()));
				if (findDiscoveryId != null && findForkDiscoveryId != null) {
					if (findDiscoveryId.equals(findForkDiscoveryId)) {
						isDiscoveryData = true;
					}
				} else {
					isDiscoveryData = true;
				}
				if (findForkDiscoveryId != null && isDiscoveryData) {
					List<ForkDiscrepancyResultEntity> findForkDiscrepancyValue = forkDiscrepancyResultRepository
							.findForkDiscrepancyValueByDeviceId(String.valueOf(devicedetails.getdId()),
									findForkDiscoveryId);
					discrepancyObject = getDscoveryResultInterfaceName(findForkDiscrepancyValue,
							devicedetails.getdVendor(), devicedetails.getdVNFSupport(), devicedetails.getdId(),
							findForkDiscoveryId, discrepancyObject);
				}
				DiscoveryDashboardEntity discoveryDetails = null;
				if (!isDiscoveryData) {
					discoveryDetails = dashboardRepo.findByDisId(findDiscoveryId);
					details.put("discrepancyData", getDiscoveryDetails(discoveryDetails));

				} else {
					if (findForkDiscoveryId != null) {
						discoveryDetails = dashboardRepo.findByDisId(findForkDiscoveryId);
						details.put("discrepancyData", getDiscoveryDetails(discoveryDetails));
					}
				}
				details.put("discrepancy", discrepancyObject);
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return details;
	}

	@SuppressWarnings("unchecked")
	private JSONArray getDscoveryResultInterfaceName(
			List<ForkDiscrepancyResultEntity> findForkDiscrepancyValueByDeviceId, String vendor, String networkType,
			int deviceId, int discoveryId, JSONArray discrepancyObject) {
		String dicreapancyvalue = null;
		for (ForkDiscrepancyResultEntity forkDiscrepancyValue : findForkDiscrepancyValueByDeviceId) {
			String fidChildOIDNo = forkDiscrepancyValue.getFidChildOIDNo();
			fidChildOIDNo = StringUtils.substringAfterLast(fidChildOIDNo, ".");
			String oidNumber = masterOIDRepository.findInterFaceOidAndDisplayName(vendor, networkType);
			String finalOid = oidNumber + "." + fidChildOIDNo;
			dicreapancyvalue = forkDiscrepancyResultRepository.findForkDiscrepancyValueByDeviceIdAndoidNo(finalOid,
					String.valueOf(deviceId), discoveryId);
			if (dicreapancyvalue != null) {
				for (ForkDiscrepancyResultEntity forkDiscrepancy : findForkDiscrepancyValueByDeviceId) {					
					String oidValue = forkDiscrepancy.getFidChildOIDNo();
					oidValue = StringUtils.substringAfterLast(oidValue, ".");
					if (oidValue.equals(fidChildOIDNo)) {
						String displayName = masterOIDRepository.findOidDisplayName(forkDiscrepancy.getFidOIDNo(),
								vendor);
						JSONObject discrepancy = discrepancyStatusForLatestDiscover(
								forkDiscrepancy.getFidDiscrepancyFalg(),
								displayName + " for Interface" + " '" + dicreapancyvalue + "'",
								forkDiscrepancy.getFidExistingValue(), forkDiscrepancy.getFidDiscoverValue(), true);
						discrepancy.put("oid", forkDiscrepancy.getFidOIDNo());
						discrepancy.put("childOid", forkDiscrepancy.getFidChildOIDNo());
						boolean flag = getFlag(discrepancyObject, discrepancy);
						if (!flag) {
							discrepancyObject.add(discrepancy);
						}
					}
				}
				dicreapancyvalue = null;
			}
		}
		return discrepancyObject;
	}

	@SuppressWarnings("unchecked")
	private JSONObject discrepancyStatusForLatestDiscover(String fidDiscrepancyFalg, String oidDisplayName,
			String existingValue, String discoverdValue, boolean action) {
		JSONObject discrepancy = new JSONObject();
		if (fidDiscrepancyFalg.equals("1")) {
			discrepancy.put("discrepancyType", "Missing");
		} else if (fidDiscrepancyFalg.equals("2")) {
			discrepancy.put("discrepancyType", "Mismatch");
		} else if (fidDiscrepancyFalg.equals("3")) {
			discrepancy.put("discrepancyType", "New");
		}
		discrepancy.put("discrepancyMsg", oidDisplayName);
		if (action) {
			discrepancy.put("action1", "Ignore");
			discrepancy.put("action2", "Overwrite");
		}
		JSONArray valueArray = new JSONArray();
		JSONObject oldValueObject = new JSONObject();
		oldValueObject.put("key", "Existing");
		oldValueObject.put("value", existingValue);
		valueArray.add(oldValueObject);
		JSONObject newValueObject = new JSONObject();
		newValueObject.put("key", "Discovered");
		newValueObject.put("value", discoverdValue);
		valueArray.add(newValueObject);
		discrepancy.put("values", valueArray);
		return discrepancy;
	}

	@SuppressWarnings("unchecked")
	public JSONArray getDiscrepancyReport(String managmentIp, String hostName) {
		JSONArray discrepancyArray = new JSONArray();
		DeviceDiscoveryEntity devicedetails = discoveryRepo.findHostNameAndMgmtip(managmentIp, hostName);

		if (devicedetails != null) {
			try {
				Set<Integer> discoverIdList = hostDiscoveryResultRepository
						.findDiscoveryId(String.valueOf(devicedetails.getdId()));
				Integer hostDiscoveryId = hostDiscrepancyResultRepository
						.findDiscoveryId(String.valueOf(devicedetails.getdId()));

				discoverIdList
						.addAll(forkDiscoveryResultRepository.findDiscoveryId(String.valueOf(devicedetails.getdId())));

				Integer findForkDiscoveryId = forkDiscrepancyResultRepository
						.findForkDiscoveryId(String.valueOf(devicedetails.getdId()));

				if (hostDiscoveryId != null) {
					discoverIdList.remove(hostDiscoveryId);
				}
				if (hostDiscoveryId != null) {
					discoverIdList.remove(findForkDiscoveryId);
				}

				if (discoverIdList != null && !discoverIdList.isEmpty()) {
					for (Integer discoveryId : discoverIdList) {
						JSONArray discrepancyObject = new JSONArray();
						JSONObject details = new JSONObject();
						DiscoveryDashboardEntity discoveryDetails = dashboardRepo.findByDisId(discoveryId);
						details = getDiscoveryDetails(discoveryDetails);
						discrepancyObject.addAll(discroveryDiscreapncy(devicedetails.getdId(),
								devicedetails.getdVendor(), devicedetails.getdVNFSupport(), discoveryId));
						details.put("discrepancy", discrepancyObject);
						discrepancyArray.add(details);
					}
				}

			} catch (Exception e) {
				logger.error(e);
			}
		}
		return discrepancyArray;
	}

	@SuppressWarnings("unchecked")
	private JSONArray discroveryDiscreapncy(int deviceId, String vendor, String networkType, Integer discoveryId) {
		JSONArray discrepancyObject = new JSONArray();
		List<HostDiscoveryResultEntity> discrepancyDetails = hostDiscoveryResultRepository
				.findHostDiscoveryValue(String.valueOf(deviceId), discoveryId);

		for (HostDiscoveryResultEntity deviceDiscrepancy : discrepancyDetails) {
			if (deviceDiscrepancy != null) {
				discrepancyObject.add(hostDiscrepancyValue(deviceDiscrepancy, vendor));
			}
		}
		List<ForkDiscoveryResultEntity> forkDiscrepancyValue = forkDiscoveryResultRepository
				.findHostDiscoveryValue(String.valueOf(deviceId), discoveryId);
		discrepancyObject = getDiscreapncyInterfaceName(forkDiscrepancyValue, vendor, networkType, deviceId,
				discoveryId, discrepancyObject);

		return discrepancyObject;
	}

	@SuppressWarnings("unchecked")
	private JSONArray getDiscreapncyInterfaceName(List<ForkDiscoveryResultEntity> forkDiscrepancyValue, String vendor,
			String networkType, int deviceId, int discoveryId, JSONArray discrepancyObject) {
		String dicreapancyvalue = null;
		for (ForkDiscoveryResultEntity forkDiscrepancy : forkDiscrepancyValue) {
			String fidChildOIDNo = forkDiscrepancy.getFdrChildOIDNo();
			fidChildOIDNo = StringUtils.substringAfterLast(fidChildOIDNo, ".");
			String oidNumber = masterOIDRepository.findInterFaceOidAndDisplayName(vendor, networkType);
			String finalOid = oidNumber + "." + fidChildOIDNo;
			dicreapancyvalue = forkDiscoveryResultRepository.findForkDiscrepancyValueByDeviceIdAndoidNo(finalOid,
					String.valueOf(deviceId), discoveryId);
			if (dicreapancyvalue != null) {
				for (ForkDiscoveryResultEntity forkDiscrepancyData : forkDiscrepancyValue) {

					String oidValue = forkDiscrepancyData.getFdrChildOIDNo();
					oidValue = StringUtils.substringAfterLast(oidValue, ".");
					if (oidValue.equals(fidChildOIDNo)) {
						String displayName = masterOIDRepository.findOidDisplayName(forkDiscrepancyData.getFdrOIDNo(),
								vendor);
						JSONObject discrepancy = discrepancyStatusForLatestDiscover(
								forkDiscrepancyData.getFdrDiscrepancyFalg(),
								displayName + " for Interface" + " '" + dicreapancyvalue + "'",
								forkDiscrepancyData.getFdrExistingValue(), forkDiscrepancyData.getFdrDiscoverValue(),
								false);
						discrepancy.put("oid", forkDiscrepancyData.getFdrOIDNo());
						discrepancy.put("childOid", forkDiscrepancyData.getFdrChildOIDNo());
						boolean flag = getFlag(discrepancyObject, discrepancy);
						if (!flag) {
							discrepancyObject.add(discrepancy);
						}
					}

				}
				dicreapancyvalue = null;
			}
		}
		return discrepancyObject;
	}

	private boolean getFlag(JSONArray discrepancyObject, JSONObject discrepancy) {
		boolean flag = false;
		for (int j = 1; j < discrepancyObject.size(); j++) {
			JSONObject jObject = (JSONObject) discrepancyObject.get(j);
			if (jObject.get("childOid")!= null) {
				if (discrepancy.get("discrepancyMsg").toString().equals(jObject.get("discrepancyMsg").toString())
						&& discrepancy.get("childOid").toString().equals(jObject.get("childOid").toString())) {
					flag = true;
				}
			}
		}
		return flag;
	}

	@SuppressWarnings("unchecked")
	private JSONObject hostDiscrepancyValue(HostDiscoveryResultEntity deviceDiscrepancy, String vendor) {
		String displayName = masterOIDRepository.findOidDisplayName(deviceDiscrepancy.getHdrOIDNo(), vendor);
		JSONObject discrepancy = discrepancyStatusForLatestDiscover(deviceDiscrepancy.getHdrDiscrepancyFalg(),
				displayName, deviceDiscrepancy.getHdrExistingValue(), deviceDiscrepancy.getHdrDiscoverValue(), false);
		discrepancy.put("oid", deviceDiscrepancy.getHdrOIDNo());
		discrepancy.put("childOid", "");
		return discrepancy;
	}

	@SuppressWarnings("unchecked")
	private JSONObject getDiscoveryDetails(DiscoveryDashboardEntity discoveryDetails) {
		JSONObject details = new JSONObject();
		if (discoveryDetails != null) {
			details.put("discoveryId", discoveryDetails.getDisId());
			details.put("discoveryDashboardId", discoveryDetails.getDisDashId());
			details.put("discoveryName", discoveryDetails.getDisName());
			details.put("discoveryStatus", discoveryDetails.getDisStatus());
			details.put("discoveryIpType", discoveryDetails.getDisIpType());
			details.put("discoveryType", discoveryDetails.getDisDiscoveryType());
			details.put("discoveryStartIp", discoveryDetails.getDisStartIp());
			details.put("discoveryEndIp", discoveryDetails.getDisEndIp());
			details.put("discoveryNetworkMask", discoveryDetails.getDisNetworkMask());
			details.put("discoveryProfileName", discoveryDetails.getDisProfileName());
			details.put("discoveryScheduledId", discoveryDetails.getDisScheduleId());
			details.put("discoveryCreatedDate", discoveryDetails.getDisCreatedDate());
			details.put("discoveryCreatedBy", discoveryDetails.getDisCreatedBy());
			details.put("discoveryUpdatedDate", discoveryDetails.getDisUpdatedDate());
			details.put("discoveryImportId", discoveryDetails.getDisImportId());
		}
		return details;
	}

	@SuppressWarnings("unchecked")
	public JSONObject ignoreAndOverWrite(@RequestBody String request) {
		HostDiscrepancyResultEntity hostDiscrepancyResultEntity = null;
		ForkDiscrepancyResultEntity forkDiscrepancyResultEntity = null;
		DeviceDiscoveryEntity deviceDiscovertEntity = null;
		JSONObject resultObj = null;
		JSONObject obj = new JSONObject();
		JSONParser parser = new JSONParser();
		String ipAddress = null, logedInUserName = null;
		boolean isSucess = false;
		try {
			obj = (JSONObject) parser.parse(request);
			ipAddress = obj.get("ipAddress").toString();
			deviceDiscovertEntity = discoveryRepo.findAllByMgmtId(ipAddress);
			if (obj.get("userName") != null)
				logedInUserName = obj.get("userName").toString();
			logger.info(" logedInUserName " + logedInUserName);
			if (deviceDiscovertEntity != null) {
				logger.info(" deviceDiscovertEntity id" + deviceDiscovertEntity.getdId());
				// if child oid is not null and not empty fetch data from fork tables
				if ((obj.get("childOid") != null && !obj.get("childOid").equals(""))) {
					forkDiscrepancyResultEntity = forkDiscrepancyResultRepository.findDeviceForkDiscrepancy(
							String.valueOf(deviceDiscovertEntity.getdId()), obj.get("oid").toString(),
							obj.get("childOid").toString(), ipAddress);

					if (forkDiscrepancyResultEntity != null) {
						logger.info(" forkDiscrepancyResultEntity.getFidChildOIDNo() ->"
								+ forkDiscrepancyResultEntity.getFidChildOIDNo());
						if ("Overwrite".equals(obj.get("Action"))) {
							forkDiscrepancyResultEntity
									.setFidPreviousValue(forkDiscrepancyResultEntity.getFidExistingValue());
							forkDiscrepancyResultEntity
									.setFidExistingValue(forkDiscrepancyResultEntity.getFidDiscoverValue());
						}
						forkDiscrepancyResultEntity.setFidDiscrepancyFalg("0");
						forkDiscrepancyResultEntity.setFidResolvedFalg("Y");
						forkDiscrepancyResultEntity.setFidResolvedTimestamp(Timestamp.valueOf(LocalDateTime.now()));
						forkDiscrepancyResultEntity.setFidResolvedBy(logedInUserName);
						forkDiscrepancyResultEntity.setFidUpdatedBy(logedInUserName);
						forkDiscrepancyResultEntity.setFidUpdateDate(Timestamp.valueOf(LocalDateTime.now()));
						forkDiscrepancyResultRepository.save(forkDiscrepancyResultEntity);
						isSucess = true;
					}

				} else {
					// if child oid is null and empty fetch data from host tables
					hostDiscrepancyResultEntity = hostDiscrepancyResultRepository.findDeviceHostDiscrepancy(
							String.valueOf(deviceDiscovertEntity.getdId()), obj.get("oid").toString(), ipAddress);
					if (hostDiscrepancyResultEntity != null) {
						logger.info(" hostDiscrepancyResultEntity.getHidOIDNo() ->"
								+ hostDiscrepancyResultEntity.getHidOIDNo());
						if ("Overwrite".equals(obj.get("Action"))) {
							hostDiscrepancyResultEntity
									.setHidPreviousValue(hostDiscrepancyResultEntity.getHidExistingValue());
							hostDiscrepancyResultEntity
									.setHidExistingValue(hostDiscrepancyResultEntity.getHidDiscoverValue());
						}
						hostDiscrepancyResultEntity.setHidDiscrepancyFalg("0");
						hostDiscrepancyResultEntity.setHidResolvedFalg("Y");
						hostDiscrepancyResultEntity.setHidResolvedBy(logedInUserName);
						hostDiscrepancyResultEntity.setHidUpdatedBy(logedInUserName);
						hostDiscrepancyResultEntity.setHidResolvedTimestamp(Timestamp.valueOf(LocalDateTime.now()));
						hostDiscrepancyResultEntity.setHidUpdateDate(Timestamp.valueOf(LocalDateTime.now()));
						hostDiscrepancyResultRepository.save(hostDiscrepancyResultEntity);
						isSucess = true;
					}

				}
			}

			resultObj = new JSONObject();
			if (isSucess) {
				int discrepancys = deviceDiscovertEntity.getdDiscrepancy();
				if (discrepancys >0) {
					discrepancys = discrepancys - 1;
					deviceDiscovertEntity.setdDiscrepancy(discrepancys);
					discoveryRepo.save(deviceDiscovertEntity);
				}
				if ("Overwrite".equals(obj.get("Action"))) {
					resultObj.put("msg", "Discrepancy overwritten successfully");
				} else {
					resultObj.put("msg", "Discrepancy ignored successfully");
				}
			} else {
				if ("Overwrite".equals(obj.get("Action"))) {
					resultObj.put("msg", "Discrepancy overwritten is failed");
				} else {
					resultObj.put("msg", "Discrepancy ignore is failed");
				}
			}
			logger.info("resultObj " + resultObj);
		} catch (Exception exe) {
			exe.printStackTrace();
			logger.error("exception of ignoreAndOverWrite method" + exe.getMessage());
		}
		return resultObj;
	}

	/*
	 * Get the master table information based on category, vendor and networ_type
	 * and child information based on ip_address, device_id and OID
	 */
	@SuppressWarnings("unchecked")
	public JSONObject getInterfaceDetails(String vendor, String networkType, String ipAddress, String deviceId) {
		List<MasterOIDEntity> masterOidEntities = masterOIDRepository.findOidAndDisplayName(vendor, networkType);
		List<ForkDiscrepancyResultEntity> childOids = null;
		JSONObject objInterfaces = new JSONObject();
		JSONArray objectArrayInterfaceData = getInterfaceData(masterOidEntities, childOids, ipAddress, deviceId);
		if (objectArrayInterfaceData != null)
			objInterfaces.put("interfaces", objectArrayInterfaceData);
		return objInterfaces;
	}

	@SuppressWarnings("unchecked")
	public JSONObject getMasterOids() throws ParseException {
		JSONArray array = new JSONArray();
		JSONObject masterOids = new JSONObject();
		masterOIDRepository.findAllByOrderByOidCreatedDateDesc().forEach(masterEntity -> {
			JSONObject object = new JSONObject();
			object.put("vendor", masterEntity.getOidVendor());
			object.put("oid", masterEntity.getOidNo());
			object.put("category", masterEntity.getOidCategory());
			object.put("rfAttrib", masterEntity.getOidAttrib());
			object.put("label", masterEntity.getOidDisplayName());
			object.put("inScope", masterEntity.getOidScopeFlag());
			object.put("networkType", masterEntity.getOidNetworkType());
			object.put("sub", masterEntity.getOidForkFlag());
			object.put("compare", masterEntity.getOidCompareFlag());
			object.put("default", masterEntity.getOidDefaultFlag());
			array.add(object);
		});
		masterOids.put("output", array);
		return masterOids;
	}

	@SuppressWarnings({ "unchecked" })
	public JSONObject saveMasterOids(String request) throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject json = new JSONObject();
		json = (JSONObject) parser.parse(request);
		JSONObject object = new JSONObject();
		boolean isAdd = false;
		String vendor = null, oid = null, category = null, rfAttrib = null, label = null, inScope = null,
				networkType = null, sub = null, compare = null;
		String defaultFlag = null, userName = null;

		JSONArray jsonArray = (JSONArray) (json.get("oidDetails"));
		if (json.containsKey("userName")) {
			userName = json.get("userName").toString();
		}
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject oidObject = (JSONObject) jsonArray.get(i);
			if (oidObject.get("vendor") != null) {
				vendor = oidObject.get("vendor").toString();
			}
			if (oidObject.get("oid") != null) {
				oid = oidObject.get("oid").toString();
			}
			if (oidObject.get("category") != null) {
				category = oidObject.get("category").toString();
			}
			if (oidObject.get("rfAttrib") != null) {
				rfAttrib = oidObject.get("rfAttrib").toString();
			}
			if (oidObject.get("label") != null) {
				label = oidObject.get("label").toString();
			}
			if (oidObject.get("inScope") != null) {
				inScope = oidObject.get("inScope").toString();
			}
			if (oidObject.get("networkType") != null) {
				networkType = oidObject.get("networkType").toString();
			}
			if (oidObject.get("sub") != null) {
				sub = oidObject.get("sub").toString();
			}
			if (oidObject.get("compare") != null) {
				compare = oidObject.get("compare").toString();
			}
			if (oidObject.get("default") != null) {
				defaultFlag = oidObject.get("default").toString();
			}

			// List<MasterOIDEntity> masterEntity = masterOIDRepository.findByOidNo(oid);
			List<MasterOIDEntity> masterEntity = masterOIDRepository
					.findByOidNoAndOidCategoryAndOidScopeFlagAndOidVendorAndOidNetworkTypeAndOidDisplayName(oid,
							category, inScope, vendor, networkType, label);
			if (masterEntity.isEmpty()) {
				MasterOIDEntity masterOidsEntity = new MasterOIDEntity();
				masterOidsEntity.setOidNo(oid);
				masterOidsEntity.setOidVendor(vendor);
				masterOidsEntity.setOidCategory(category);
				// entity.setOidAttrib(masterOid.getOidAttrib());
				masterOidsEntity.setOidDisplayName(label);
				masterOidsEntity.setOidScopeFlag(inScope);
				masterOidsEntity.setOidNetworkType(networkType);
				masterOidsEntity.setOidForkFlag(sub);
				masterOidsEntity.setOidCompareFlag(compare);
				masterOidsEntity.setOidDefaultFlag(defaultFlag);
				masterOidsEntity.setOidCreatedBy(userName);
				masterOidsEntity.setOidCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
				masterOIDRepository.save(masterOidsEntity);
				isAdd = true;
			}
		}
		if (isAdd) {
			object.put("output", "Oids added successfully");
		} else {
			object.put("output", "Oids is Duplicate");
		}
		return object;
	}

	@SuppressWarnings({ "unchecked" })
	private JSONArray getInterfaceData(List<MasterOIDEntity> masterOidEntities,
			List<ForkDiscrepancyResultEntity> childOids, String ipAddress, String deviceId) {
		JSONArray outputArray = new JSONArray();
		int maxLength = 0, length = 0;
		String dotCheck = ".";
		List<ForkDiscrepancyResultEntity> tempChildOids = new ArrayList<ForkDiscrepancyResultEntity>();
		for (MasterOIDEntity masterEntity : masterOidEntities) {
			childOids = forkDiscrepancyResultRepository.findForkDiscrepancy(ipAddress, deviceId,
					masterEntity.getOidNo());
			{
				JSONObject childJson = null;
				JSONArray childList = new JSONArray();
				JSONObject masterJson = new JSONObject();
				if ("Name".equalsIgnoreCase(masterEntity.getOidDisplayName())) {
					maxLength = childOids.size();
					tempChildOids.addAll(childOids);
				}
				length = childOids.size();
				if (maxLength > length) {
					masterJson.put("id", masterEntity.getOidNo());
					masterJson.put("category", masterEntity.getOidCategory());
					masterJson.put("displayName", masterEntity.getOidDisplayName());
					String tempChildSuffix = null, childSuffix = null;
					boolean isSuffixMatch = true;
					for (ForkDiscrepancyResultEntity tempChildOid : tempChildOids) {
						if (tempChildOid.getFidChildOIDNo().contains(dotCheck)) {
							tempChildSuffix = tempChildOid.getFidChildOIDNo()
									.substring(tempChildOid.getFidChildOIDNo().lastIndexOf(dotCheck) + 1);
						}
						for (ForkDiscrepancyResultEntity childOid : childOids) {
							if (childOid.getFidChildOIDNo().contains(dotCheck)) {
								childSuffix = childOid.getFidChildOIDNo()
										.substring(childOid.getFidChildOIDNo().lastIndexOf(dotCheck) + 1);
							}
							if (tempChildSuffix != null && tempChildSuffix.equals(childSuffix)) {
								isSuffixMatch = false;
								childJson = new JSONObject();
								childJson.put("id", childOid.getFidChildOIDNo());
								childJson.put("discoveredValue", childOid.getFidDiscoverValue());
								childList.add(childJson);
								break;
							}
							isSuffixMatch = true;
						}
						if (isSuffixMatch) {
							childJson = new JSONObject();
							childJson.put("id", "");
							childJson.put("discoveredValue", "");
							childList.add(childJson);
						}
					}
				} else {
					masterJson.put("id", masterEntity.getOidNo());
					masterJson.put("category", masterEntity.getOidCategory());
					masterJson.put("displayName", masterEntity.getOidDisplayName());

					for (ForkDiscrepancyResultEntity childOid : childOids) {
						childJson = new JSONObject();
						childJson.put("id", childOid.getFidChildOIDNo());
						childJson.put("discoveredValue", childOid.getFidDiscoverValue());
						childList.add(childJson);
					}
				}
				masterJson.put("childOid", childList);
				outputArray.add(masterJson);
			}
		}
		return outputArray;
	}
}

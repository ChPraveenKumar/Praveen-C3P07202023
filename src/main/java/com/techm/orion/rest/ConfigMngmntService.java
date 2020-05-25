package com.techm.orion.rest;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.ws.rs.POST;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techm.orion.dao.TemplateManagementDao;
import com.techm.orion.pojo.AttribCreateConfigPojo;
import com.techm.orion.pojo.CommandPojo;
import com.techm.orion.pojo.CreateConfigPojo;
import com.techm.orion.pojo.CreateConfigRequestDCM;
import com.techm.orion.repositories.TemplateFeatureRepo;
import com.techm.orion.service.AttribCreateConfigService;
import com.techm.orion.service.DcmConfigService;
import com.techm.orion.utility.InvokeFtl;

@Controller
@RequestMapping("/ConfigMngmntService")
public class ConfigMngmntService implements Observer {
	@Autowired
	AttribCreateConfigService service;

	@Autowired
	DcmConfigService dcmConfigService;

	@Autowired
	TemplateFeatureRepo templatefeatureRepo;

	@POST
	@RequestMapping(value = "/createConfigurationDcm", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public JSONObject createConfigurationDcm(@RequestBody String configRequest) {
		// DcmConfigService dcmConfigService=new DcmConfigService();
		JSONObject obj = new JSONObject();
		String jsonMessage = "", requestType = null;
		String requestIdForConfig = "";
		String res = "false";
		String data = "Failure";
		String request_creator_name = null;

		try {

			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(configRequest);

			CreateConfigRequestDCM configReqToSendToC3pCode = new CreateConfigRequestDCM();

			// For IOS Upgrade

			if (json.containsKey("requestType")) {
				configReqToSendToC3pCode.setRequestType(json.get("requestType").toString());
				requestType = json.get("requestType").toString();
			} else {
				configReqToSendToC3pCode.setRequestType("SLGC");

			}
			if (json.containsKey("networkType")) {
				configReqToSendToC3pCode.setNetworkType(json.get("networkType").toString());
			} else {
				configReqToSendToC3pCode.setNetworkType("Legacy");

			}
			
			Boolean isStartUp = (Boolean) json.get("isStartUp");
			configReqToSendToC3pCode.setIsStartUp(isStartUp);
			
			
			
			// template suggestion
			if(json.get("requestType").equals("SLGB"))
			{
				configReqToSendToC3pCode.setTemplateID(json.get("templateID").toString());
			}
			else
			{
				configReqToSendToC3pCode.setTemplateID(json.get("templateId").toString());
			}
			configReqToSendToC3pCode.setCustomer(json.get("customer").toString());
			configReqToSendToC3pCode.setStatus(json.get("status").toString());
			configReqToSendToC3pCode.setSiteid(json.get("siteid").toString().toUpperCase());
			// configReqToSendToC3pCode.setDeviceName(json.get("deviceName").toString());
			configReqToSendToC3pCode.setDeviceType(json.get("deviceType").toString());
			configReqToSendToC3pCode.setModel(json.get("model").toString());
			configReqToSendToC3pCode.setOs(json.get("os").toString());
			if (json.containsKey("osVersion")) {
				configReqToSendToC3pCode.setOsVersion(json.get("osVersion").toString());
			}
			if (json.containsKey("vrfName")) {
				configReqToSendToC3pCode.setVrfName(json.get("vrfName").toString());
			}
			configReqToSendToC3pCode.setManagementIp(json.get("managementIp").toString());
			if (json.containsKey("enablePassword")) {
				configReqToSendToC3pCode.setEnablePassword(json.get("enablePassword").toString());
			} else {
				configReqToSendToC3pCode.setEnablePassword(null);
			}
			if (json.containsKey("banner")) {
				configReqToSendToC3pCode.setBanner(json.get("banner").toString());
			} else {
				configReqToSendToC3pCode.setBanner(null);
			}
			configReqToSendToC3pCode.setRegion(json.get("region").toString().toUpperCase());
			if (json.containsKey("service")) {
			configReqToSendToC3pCode.setService(json.get("service").toString().toUpperCase());
			}
			configReqToSendToC3pCode.setHostname(json.get("hostname").toString().toUpperCase());
			configReqToSendToC3pCode.setRequestType_Flag(json.get("requestType_Flag").toString().toUpperCase());
			// configReqToSendToC3pCode.setVpn(json.get("VPN").toString());
			configReqToSendToC3pCode.setVendor(json.get("vendor").toString().toUpperCase());
			configReqToSendToC3pCode.setSiteName(json.get("siteName").toString().toUpperCase());
			configReqToSendToC3pCode.setFamily(json.get("family").toString().toUpperCase());
			
			
			SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

			String strDate1 = sdf1.format(json.get("dateofProcessing"));
			configReqToSendToC3pCode.setDateofProcessing((convertStringToTimestamp(strDate1)));
			JSONObject internetLvcrf = (JSONObject) json.get("internetLcVrf");
			if(!(json.get("requestType").equals("SLGB")))
			{
			
			if (internetLvcrf.containsKey("networkIp") && internetLvcrf.get("networkIp").toString() != ""
					&& !internetLvcrf.get("networkIp").toString().isEmpty()) {
				configReqToSendToC3pCode.setNetworkIp(internetLvcrf.get("networkIp").toString());
			}
			
			if (internetLvcrf.containsKey("neighbor1") && !(internetLvcrf.get("neighbor1")== null)) {
				configReqToSendToC3pCode
						.setNeighbor1(internetLvcrf.get("neighbor1").toString().toUpperCase());
			}
			if (internetLvcrf.containsKey("neighbor2") && !((internetLvcrf.get("neighbor2")==null))) {
				configReqToSendToC3pCode.setNeighbor2(internetLvcrf.get("neighbor2").toString().toUpperCase());
			} else {
				configReqToSendToC3pCode.setNeighbor2(null);
			}
			if (internetLvcrf.containsKey("neighbor1_remoteAS") && !(internetLvcrf.get("neighbor1_remoteAS") == null)) {
				configReqToSendToC3pCode
						.setNeighbor1_remoteAS(internetLvcrf.get("neighbor1_remoteAS").toString().toUpperCase());
			}
			if (internetLvcrf.containsKey("neighbor2_remoteAS") && !(internetLvcrf.get("neighbor2_remoteAS") == null)) {
				configReqToSendToC3pCode
						.setNeighbor2_remoteAS(internetLvcrf.get("neighbor2_remoteAS").toString().toUpperCase());
			} else {
				configReqToSendToC3pCode.setNeighbor2_remoteAS(null);
			}

			if (internetLvcrf.containsKey("networkIp_subnetMask")
					&& internetLvcrf.get("networkIp_subnetMask").toString() != "") {
				configReqToSendToC3pCode.setNetworkIp_subnetMask(internetLvcrf.get("networkIp_subnetMask").toString());
			}
			if (internetLvcrf.containsKey("routingProtocol") && !(internetLvcrf.get("routingProtocol")== null)) {
				configReqToSendToC3pCode
						.setRoutingProtocol(internetLvcrf.get("routingProtocol").toString().toUpperCase());
			}
			if (internetLvcrf.containsKey("AS") && !(internetLvcrf.get("AS") == null)) {
				configReqToSendToC3pCode.setBgpASNumber(internetLvcrf.get("AS").toString().toUpperCase());
			}

			JSONObject c3p_interface = (JSONObject) json.get("c3p_interface");
			if (c3p_interface.containsKey("name")) {
				configReqToSendToC3pCode.setName(c3p_interface.get("name").toString());
			}
			if (c3p_interface.containsKey("description")) {
				configReqToSendToC3pCode.setDescription(c3p_interface.get("description").toString());
			} else {
				configReqToSendToC3pCode.setDescription(null);

			}

			if (c3p_interface.containsKey("ip") && c3p_interface.get("ip").toString() != "") {
				configReqToSendToC3pCode.setIp(c3p_interface.get("ip").toString());
			}
			if (c3p_interface.containsKey("mask") && c3p_interface.get("mask").toString() != "") {
				configReqToSendToC3pCode.setMask(c3p_interface.get("mask").toString());
			}

			if (c3p_interface.containsKey("speed") && c3p_interface.get("speed").toString() != "") {
				configReqToSendToC3pCode.setSpeed(c3p_interface.get("speed").toString());
			}
			if (c3p_interface.containsKey("bandwidth")) {

				configReqToSendToC3pCode.setBandwidth(c3p_interface.get("bandwidth").toString());
			}

			if (c3p_interface.containsKey("encapsulation")) {
				configReqToSendToC3pCode.setEncapsulation(c3p_interface.get("encapsulation").toString());
			}

			if (json.containsKey("misArPe")) {
				JSONObject mis = (JSONObject) json.get("misArPe");
				{
					configReqToSendToC3pCode.setRouterVrfVpnDGateway(mis.get("routerVrfVpnDIp").toString());
					configReqToSendToC3pCode.setRouterVrfVpnDIp(mis.get("routerVrfVpnDGateway").toString());
					configReqToSendToC3pCode.setFastEthernetIp(mis.get("fastEthernetIp").toString());
				}
			}
			}
			if (json.containsKey("isAutoProgress")) {
				configReqToSendToC3pCode.setIsAutoProgress((Boolean) json.get("isAutoProgress"));
			} else {
				configReqToSendToC3pCode.setIsAutoProgress(true);
			}
			// This version is 1 is this will be freshly created request every time so
			// version will be 1.
			configReqToSendToC3pCode.setRequest_version(1.0);
			// This version is 1 is this will be freshly created request every time so
			// parent will be 1.
			configReqToSendToC3pCode.setRequest_parent_version(1.0);
			ObjectMapper mapper = new ObjectMapper();
			/*
			 * CreateConfigRequestDCM mappedObj = mapper.readValue(configRequest,
			 * CreateConfigRequestDCM.class);
			 */

			// get request creator name

			if (requestType.equals("SLGB")) {
				request_creator_name = json.get("request_creator_name")
						.toString();
			} else {

				request_creator_name = dcmConfigService.getLogedInUserName();
			}
			// String request_creator_name="seuser";
			if (request_creator_name.isEmpty()) {
				configReqToSendToC3pCode.setRequest_creator_name("seuser");
			} else {
				configReqToSendToC3pCode.setRequest_creator_name(request_creator_name);
			}

			if (json.containsKey("snmpHostAddress")) {
				configReqToSendToC3pCode.setSnmpHostAddress(json.get("snmpHostAddress").toString());
			} else {
				configReqToSendToC3pCode.setSnmpHostAddress(null);
			}
			if (json.containsKey("snmpString")) {
				configReqToSendToC3pCode.setSnmpString(json.get("snmpString").toString());
			} else {
				configReqToSendToC3pCode.setSnmpString(null);
			}
			if (json.containsKey("loopBackType")) {
				configReqToSendToC3pCode.setLoopBackType(json.get("loopBackType").toString());
			} else {
				configReqToSendToC3pCode.setLoopBackType(null);
			}
			if (json.containsKey("loopbackIPaddress")) {
				configReqToSendToC3pCode.setLoopbackIPaddress(json.get("loopbackIPaddress").toString());
			} else {
				configReqToSendToC3pCode.setLoopbackIPaddress(null);
			}
			if (json.containsKey("loopbackSubnetMask")) {
				configReqToSendToC3pCode.setLoopbackSubnetMask(json.get("loopbackSubnetMask").toString());
			} else {
				configReqToSendToC3pCode.setLoopbackSubnetMask(null);
			}
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

			String strDate = sdf.format(date);
			configReqToSendToC3pCode.setRequestCreatedOn(strDate);

			JSONObject certificationTestFlag = (JSONObject) json.get("certificationOptionListFlags");
			
			
			if(!(requestType.equals("SLGB")))
			{

			if (certificationTestFlag.containsKey("defaults")) {
				// flag test selection
				JSONObject defaultObj = (JSONObject) certificationTestFlag.get("defaults");
				if (defaultObj.get("Interfaces status").toString().equals("1")) {
					configReqToSendToC3pCode.setInterfaceStatus(defaultObj.get("Interfaces status").toString());
				}

				if (defaultObj.get("WAN Interface").toString().equals("1")) {
					configReqToSendToC3pCode.setWanInterface(defaultObj.get("WAN Interface").toString());
				}

				if (defaultObj.get("Platform & IOS").toString().equals("1")) {
					configReqToSendToC3pCode.setPlatformIOS(defaultObj.get("Platform & IOS").toString());
				}

				if (defaultObj.get("BGP neighbor").toString().equals("1")) {
					configReqToSendToC3pCode.setBGPNeighbor(defaultObj.get("BGP neighbor").toString());
				}
				if (defaultObj.get("Throughput").toString().equals("1")) {
					configReqToSendToC3pCode.setThroughputTest(defaultObj.get("Throughput").toString());
				}
				if (defaultObj.get("FrameLoss").toString().equals("1")) {
					configReqToSendToC3pCode.setFrameLossTest(defaultObj.get("FrameLoss").toString());
				}
				if (defaultObj.get("Latency").toString().equals("1")) {
					configReqToSendToC3pCode.setLatencyTest(defaultObj.get("Latency").toString());
				}

				String bit = defaultObj.get("Interfaces status").toString() + defaultObj.get("WAN Interface").toString()
						+ defaultObj.get("Platform & IOS").toString() + defaultObj.get("BGP neighbor").toString()
						+ defaultObj.get("Throughput").toString() + defaultObj.get("FrameLoss").toString()
						+ defaultObj.get("Latency").toString();
				System.out.println(bit);
				configReqToSendToC3pCode.setCertificationSelectionBit(bit);

			}
		}
			
			if(!(requestType.equals("SLGB")))
			{

			if (certificationTestFlag.containsKey("dynamic")) {
				JSONArray dynamicArray = (JSONArray) certificationTestFlag.get("dynamic");
				JSONArray toSaveArray = new JSONArray();

				for (int i = 0; i < dynamicArray.size(); i++) {
					JSONObject arrayObj = (JSONObject) dynamicArray.get(i);
					long isSelected = (long) arrayObj.get("selected");
					if (isSelected == 1) {
						toSaveArray.add(arrayObj);
					}
				}

				String testsSelected = toSaveArray.toString();
				configReqToSendToC3pCode.setTestsSelected(testsSelected);

			}
			}

			// LAN interface
			if (json.containsKey("lanTnterface")) {
				configReqToSendToC3pCode.setLanInterface(json.get("lanTnterface").toString());
			}
			if (json.containsKey("lanIPaddress")) {
				configReqToSendToC3pCode.setLanIp(json.get("lanIPaddress").toString());
			}
			if (json.containsKey("lanSubnetMask")) {
				configReqToSendToC3pCode.setLanMaskAddress(json.get("lanSubnetMask").toString());
			}
			if (json.containsKey("lanDescription")) {
				configReqToSendToC3pCode.setLanDescription(json.get("lanDescription").toString() + "\n");
			}

			if (configReqToSendToC3pCode.getNetworkType().equalsIgnoreCase("VNF")) {
				configReqToSendToC3pCode.setVnfConfig(json.get("vnfConfig").toString());
			}
			// to get the scheduled time for the requestID
			if (json.containsKey("scheduledTime")&& !(requestType.equals("SLGB"))) {
				configReqToSendToC3pCode.setScheduledTime(json.get("scheduledTime").toString());
			}
			else if(json.containsKey("backUpScheduleTime"))
			{
				configReqToSendToC3pCode.setScheduledTime(json.get("backUpScheduleTime").toString());
			}
			if (configReqToSendToC3pCode.getRequestType().equalsIgnoreCase("IOSUPGRADE")) {
				configReqToSendToC3pCode.setZipcode(json.get("zipcode").toString());
				configReqToSendToC3pCode.setManaged(json.get("managed").toString());
				configReqToSendToC3pCode.setDownTimeRequired(json.get("downtimeRequired").toString());
				configReqToSendToC3pCode.setLastUpgradedOn(json.get("lastUpgradedOn").toString());
			}

			Map<String, String> result = null;
			if (configReqToSendToC3pCode.getRequestType().contains("configDelivery")
					&& configReqToSendToC3pCode.getNetworkType().equalsIgnoreCase("Legacy")) {
				/*
				 * Extract dynamicAttribs Json Value and map it to MasteAtrribute List
				 */
				org.json.simple.JSONArray attribJson = null;
				if (json.containsKey("dynamicAttribs")) {
					attribJson = (org.json.simple.JSONArray) json.get("dynamicAttribs");
				}

				/*
				 * create SeriesId for getting master configuration Commands and master
				 * Atrribute
				 */
				String seriesId = dcmConfigService.getSeriesId(configReqToSendToC3pCode.getVendor(),
						configReqToSendToC3pCode.getDeviceType(), configReqToSendToC3pCode.getModel());
				/* Get Series according to template id */
				TemplateManagementDao templatemanagementDao = new TemplateManagementDao();
				seriesId = templatemanagementDao.getSeriesId(configReqToSendToC3pCode.getTemplateID(), seriesId);
				seriesId = StringUtils.substringAfter(seriesId, "Generic_");

				List<AttribCreateConfigPojo> masterAttribute = new ArrayList<>();
				List<AttribCreateConfigPojo> byAttribSeriesId = service.getByAttribSeriesId(seriesId);
				if (byAttribSeriesId != null && !byAttribSeriesId.isEmpty()) {
					masterAttribute.addAll(byAttribSeriesId);
				}

				/*
				 * Create TemplateId for creating master configuration when template id is null
				 * or empty
				 */
				if (configReqToSendToC3pCode.getTemplateID().equals("")
						|| configReqToSendToC3pCode.getTemplateID() == null) {
					createTemplateId(configReqToSendToC3pCode, seriesId, masterAttribute);
				}

				org.json.simple.JSONArray featureListJson = null;
				if (json.containsKey("selectedFeatures")) {
					featureListJson = (org.json.simple.JSONArray) json.get("selectedFeatures");
				}
				List<String> featureList = new ArrayList<String>();
				if (featureListJson != null && !featureListJson.isEmpty()) {
					for (int i = 0; i < featureListJson.size(); i++) {
						featureList.add((String) featureListJson.get(i));
					}
				}
				List<AttribCreateConfigPojo> templateAttribute = new ArrayList<>();
				for (String feature : featureList) {
					String templateId = configReqToSendToC3pCode.getTemplateID();
					List<AttribCreateConfigPojo> byAttribTemplateAndFeatureName = service
							.getByAttribTemplateAndFeatureName(templateId, feature);
					if (byAttribTemplateAndFeatureName != null && !byAttribTemplateAndFeatureName.isEmpty()) {
						templateAttribute.addAll(byAttribTemplateAndFeatureName);
					}
				}
				/* Extract Json and map to CreateConfigPojo fields */
				List<CreateConfigPojo> createConfigList = new ArrayList<>();
				if (attribJson != null) {
					for (int i = 0; i < attribJson.size(); i++) {
						JSONObject object = (JSONObject) attribJson.get(i);
						String attribLabel = object.get("label").toString();
						String attriValue = object.get("value").toString();
						String attribType = object.get("type").toString();
						for (AttribCreateConfigPojo attrib : masterAttribute) {

							if (attribLabel.contains(attrib.getAttribLabel())) {
								String attribName = attrib.getAttribName();
								CreateConfigPojo createConfigPojo = new CreateConfigPojo();
								createConfigPojo.setMasterLabelId(attrib.getId());
								createConfigPojo.setMasterLabelValue(attriValue);
								createConfigPojo.setTemplateId(configReqToSendToC3pCode.getTemplateID());
								createConfigList.add(createConfigPojo);

								if (attrib.getAttribType().equals("Master")) {

									if (attribType.equals("configAttrib")) {
										if (attribName.equals("Os Ver")) {
											configReqToSendToC3pCode.setOsVer(attriValue);
											break;
										}
										if (attribName.equals("Host Name Config")) {
											configReqToSendToC3pCode.setHostNameConfig(attriValue);
											break;
										}
										if (attribName.equals("Logging Buffer")) {
											configReqToSendToC3pCode.setLoggingBuffer(attriValue);
											break;
										}
										if (attribName.equals("Memory Size")) {
											configReqToSendToC3pCode.setMemorySize(attriValue);
											break;
										}
										if (attribName.equals("Logging SourceInterface")) {
											configReqToSendToC3pCode.setLoggingSourceInterface(attriValue);
											break;
										}
										if (attribName.equals("IP TFTP SourceInterface")) {
											configReqToSendToC3pCode.setiPTFTPSourceInterface(attriValue);
											break;
										}
										if (attribName.equals("IP FTP SourceInterface")) {
											configReqToSendToC3pCode.setiPFTPSourceInterface(attriValue);
											break;
										}
										if (attribName.equals("Line Con Password")) {
											configReqToSendToC3pCode.setLineConPassword(attriValue);
											break;
										}
										if (attribName.equals("Line Aux Password")) {
											configReqToSendToC3pCode.setLineAuxPassword(attriValue);
											break;
										}
										if (attribName.equals("Line VTY Password")) {
											configReqToSendToC3pCode.setLineVTYPassword(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib1")) {
											configReqToSendToC3pCode.setM_Attrib1(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib2")) {
											configReqToSendToC3pCode.setM_Attrib2(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib3")) {
											configReqToSendToC3pCode.setM_Attrib3(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib4")) {
											configReqToSendToC3pCode.setM_Attrib4(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib5")) {
											configReqToSendToC3pCode.setM_Attrib5(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib6")) {
											configReqToSendToC3pCode.setM_Attrib6(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib7")) {
											configReqToSendToC3pCode.setM_Attrib7(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib8")) {
											configReqToSendToC3pCode.setM_Attrib8(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib9")) {
											configReqToSendToC3pCode.setM_Attrib9(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib10")) {
											configReqToSendToC3pCode.setM_Attrib10(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib11")) {
											configReqToSendToC3pCode.setM_Attrib11(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib12")) {
											configReqToSendToC3pCode.setM_Attrib12(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib13")) {
											configReqToSendToC3pCode.setM_Attrib13(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib14")) {
											configReqToSendToC3pCode.setM_Attrib14(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib15")) {
											configReqToSendToC3pCode.setM_Attrib15(attriValue);
											break;
										}
									}
								}
							}
						}
						for (AttribCreateConfigPojo templateAttrib : templateAttribute) {

							if (attribLabel.contains(templateAttrib.getAttribLabel())) {
								String attribName = templateAttrib.getAttribName();

								CreateConfigPojo createConfigPojo = new CreateConfigPojo();
								createConfigPojo.setMasterLabelId(templateAttrib.getId());
								createConfigPojo.setMasterLabelValue(attriValue);
								createConfigPojo.setTemplateId(configReqToSendToC3pCode.getTemplateID());
								createConfigList.add(createConfigPojo);
								if (templateAttrib.getAttribType().equals("Template")) {
									if (attribType.equals("templateAttrib")) {

										if (attribName.equals("LANInterfaceIP1")) {
											configReqToSendToC3pCode.setlANInterfaceIP1(attriValue);
											break;
										}
										if (attribName.equals("LANInterfaceMask1")) {
											configReqToSendToC3pCode.setlANInterfaceMask1(attriValue);
											break;
										}
										if (attribName.equals("LANInterfaceIP2")) {
											configReqToSendToC3pCode.setlANInterfaceIP2(attriValue);
											break;
										}
										if (attribName.equals("LANInterfaceMask2")) {
											configReqToSendToC3pCode.setlANInterfaceMask2(attriValue);
											break;
										}
										if (attribName.equals("WANInterfaceIP1")) {
											configReqToSendToC3pCode.setwANInterfaceIP1(attriValue);
											break;
										}

										if (attribName.equals("WANInterfaceMask1")) {
											configReqToSendToC3pCode.setwANInterfaceMask1(attriValue);
											break;
										}
										if (attribName.equals("WANInterfaceIP2")) {
											configReqToSendToC3pCode.setwANInterfaceIP2(attriValue);
											break;
										}
										if (attribName.equals("WANInterfaceMask2")) {
											configReqToSendToC3pCode.setwANInterfaceMask2(attriValue);
											break;
										}
										if (attribName.equals("ResInterfaceIP")) {
											configReqToSendToC3pCode.setResInterfaceIP(attriValue);
											break;
										}

										if (attribName.equals("ResInterfaceMask")) {
											configReqToSendToC3pCode.setResInterfaceMask(attriValue);
											break;
										}

										if (attribName.equals("VRFName")) {
											configReqToSendToC3pCode.setvRFName(attriValue);
											break;
										}

										if (attribName.equals("BGPASNumber")) {
											configReqToSendToC3pCode.setbGPASNumber(attriValue);
											break;
										}

										if (attribName.equals("BGPRouterID")) {
											configReqToSendToC3pCode.setbGPRouterID(attriValue);
											break;
										}

										if (attribName.equals("BGPNeighborIP1")) {
											configReqToSendToC3pCode.setResInterfaceIP(attriValue);
											break;
										}

										if (attribName.equals("BGPRemoteAS1")) {
											configReqToSendToC3pCode.setbGPRemoteAS1(attriValue);
											break;
										}

										if (attribName.equals("BGPNeighborIP2")) {
											configReqToSendToC3pCode.setbGPNeighborIP1(attriValue);
											break;
										}

										if (attribName.equals("BGPRemoteAS2")) {
											configReqToSendToC3pCode.setbGPRemoteAS2(attriValue);
											break;
										}

										if (attribName.equals("BGPNetworkIP1")) {
											configReqToSendToC3pCode.setbGPNetworkIP1(attriValue);
											break;
										}

										if (attribName.equals("BGPNetworkWildcard1")) {
											configReqToSendToC3pCode.setbGPNetworkWildcard1(attriValue);
											break;
										}

										if (attribName.equals("BGPNetworkIP2")) {
											configReqToSendToC3pCode.setbGPNetworkIP2(attriValue);
											break;
										}

										if (attribName.equals("BGPNetworkWildcard2")) {
											configReqToSendToC3pCode.setbGPNetworkWildcard2(attriValue);
											break;
										}

										if (attribName.equals("Attrib1")) {
											configReqToSendToC3pCode.setAttrib1(attriValue);
											break;
										}
										if (attribName.equals("Attrib2")) {
											configReqToSendToC3pCode.setAttrib2(attriValue);
											break;
										}
										if (attribName.equals("Attrib3")) {
											configReqToSendToC3pCode.setAttrib3(attriValue);
											break;
										}
										if (attribName.equals("Attrib4")) {
											configReqToSendToC3pCode.setAttrib4(attriValue);
											break;
										}
										if (attribName.equals("Attrib5")) {
											configReqToSendToC3pCode.setAttrib5(attriValue);
											break;
										}
										if (attribName.equals("Attrib6")) {
											configReqToSendToC3pCode.setAttrib6(attriValue);
											break;
										}
										if (attribName.equals("Attrib7")) {
											configReqToSendToC3pCode.setAttrib7(attriValue);
											break;
										}
										if (attribName.equals("Attrib8")) {
											configReqToSendToC3pCode.setAttrib8(attriValue);
											break;
										}
										if (attribName.equals("Attrib9")) {
											configReqToSendToC3pCode.setAttrib9(attriValue);
											break;
										}
										if (attribName.equals("Attrib10")) {
											configReqToSendToC3pCode.setAttrib10(attriValue);
											break;
										}
										if (attribName.equals("Attrib11")) {
											configReqToSendToC3pCode.setAttrib11(attriValue);
											break;
										}
										if (attribName.equals("Attrib12")) {
											configReqToSendToC3pCode.setAttrib12(attriValue);
											break;
										}
										if (attribName.equals("Attrib13")) {
											configReqToSendToC3pCode.setAttrib13(attriValue);
											break;
										}
										if (attribName.equals("Attrib14")) {
											configReqToSendToC3pCode.setAttrib14(attriValue);
											break;
										}
										if (attribName.equals("Attrib15")) {
											configReqToSendToC3pCode.setAttrib15(attriValue);
											break;
										}
										if (attribName.equals("Attrib16")) {
											configReqToSendToC3pCode.setAttrib16(attriValue);
											break;
										}
										if (attribName.equals("Attrib17")) {
											configReqToSendToC3pCode.setAttrib17(attriValue);
											break;
										}
										if (attribName.equals("Attrib18")) {
											configReqToSendToC3pCode.setAttrib18(attriValue);
											break;
										}
										if (attribName.equals("Attrib19")) {
											configReqToSendToC3pCode.setAttrib19(attriValue);
											break;
										}
										if (attribName.equals("Attrib20")) {
											configReqToSendToC3pCode.setAttrib20(attriValue);
											break;
										}
										if (attribName.equals("Attrib21")) {
											configReqToSendToC3pCode.setAttrib21(attriValue);
											break;
										}
										if (attribName.equals("Attrib22")) {
											configReqToSendToC3pCode.setAttrib22(attriValue);
											break;
										}
										if (attribName.equals("Attrib23")) {
											configReqToSendToC3pCode.setAttrib23(attriValue);
											break;
										}
										if (attribName.equals("Attrib24")) {
											configReqToSendToC3pCode.setAttrib24(attriValue);
											break;
										}
										if (attribName.equals("Attrib25")) {
											configReqToSendToC3pCode.setAttrib25(attriValue);
											break;
										}
										if (attribName.equals("Attrib26")) {
											configReqToSendToC3pCode.setAttrib26(attriValue);
											break;
										}
										if (attribName.equals("Attrib27")) {
											configReqToSendToC3pCode.setAttrib27(attriValue);
											break;
										}
										if (attribName.equals("Attrib28")) {
											configReqToSendToC3pCode.setAttrib28(attriValue);
											break;
										}
										if (attribName.equals("Attrib29")) {
											configReqToSendToC3pCode.setAttrib29(attriValue);
											break;
										}
										if (attribName.equals("Attrib30")) {
											configReqToSendToC3pCode.setAttrib30(attriValue);
											break;
										}
										if (attribName.equals("Attrib31")) {
											configReqToSendToC3pCode.setAttrib31(attriValue);
											break;
										}
										if (attribName.equals("Attrib32")) {
											configReqToSendToC3pCode.setAttrib32(attriValue);
											break;
										}
										if (attribName.equals("Attrib33")) {
											configReqToSendToC3pCode.setAttrib33(attriValue);
											break;
										}
										if (attribName.equals("Attrib34")) {
											configReqToSendToC3pCode.setAttrib34(attriValue);
											break;
										}
										if (attribName.equals("Attrib35")) {
											configReqToSendToC3pCode.setAttrib35(attriValue);
											break;
										}
										if (attribName.equals("Attrib36")) {
											configReqToSendToC3pCode.setAttrib36(attriValue);
											break;
										}
										if (attribName.equals("Attrib37")) {
											configReqToSendToC3pCode.setAttrib37(attriValue);
											break;
										}
										if (attribName.equals("Attrib38")) {
											configReqToSendToC3pCode.setAttrib38(attriValue);
											break;
										}
										if (attribName.equals("Attrib39")) {
											configReqToSendToC3pCode.setAttrib39(attriValue);
											break;
										}
										if (attribName.equals("Attrib40")) {
											configReqToSendToC3pCode.setAttrib40(attriValue);
											break;
										}
										if (attribName.equals("Attrib41")) {
											configReqToSendToC3pCode.setAttrib41(attriValue);
											break;
										}
										if (attribName.equals("Attrib42")) {
											configReqToSendToC3pCode.setAttrib42(attriValue);
											break;
										}
										if (attribName.equals("Attrib43")) {
											configReqToSendToC3pCode.setAttrib43(attriValue);
											break;
										}
										if (attribName.equals("Attrib44")) {
											configReqToSendToC3pCode.setAttrib44(attriValue);
											break;
										}
										if (attribName.equals("Attrib45")) {
											configReqToSendToC3pCode.setAttrib45(attriValue);
											break;
										}
										if (attribName.equals("Attrib46")) {
											configReqToSendToC3pCode.setAttrib46(attriValue);
											break;
										}
										if (attribName.equals("Attrib47")) {
											configReqToSendToC3pCode.setAttrib47(attriValue);
											break;
										}
										if (attribName.equals("Attrib48")) {
											configReqToSendToC3pCode.setAttrib48(attriValue);
											break;
										}
										if (attribName.equals("Attrib49")) {
											configReqToSendToC3pCode.setAttrib49(attriValue);
											break;
										}
										if (attribName.equals("Attrib50")) {
											configReqToSendToC3pCode.setAttrib50(attriValue);
											break;
										}

									}
								}
							}
						}
					}
				}
				// Passing Extra parameter createConfigList for saving master
				// attribute data
				result = dcmConfigService.updateAlldetails(configReqToSendToC3pCode, createConfigList);

			} else if (configReqToSendToC3pCode.getRequestType().equalsIgnoreCase("NETCONF")
					&& configReqToSendToC3pCode.getNetworkType().equalsIgnoreCase("VNF")
					|| configReqToSendToC3pCode.getRequestType().equalsIgnoreCase("RESTCONF")
							&& configReqToSendToC3pCode.getNetworkType().equalsIgnoreCase("VNF")) {

				/*
				 * create SeriesId for getting master configuration Commands and master
				 * Atrribute
				 */
				String seriesId = dcmConfigService.getSeriesId(configReqToSendToC3pCode.getVendor(),
						configReqToSendToC3pCode.getDeviceType(), configReqToSendToC3pCode.getModel());
				/* Get Series according to template id */
				TemplateManagementDao templatemanagementDao = new TemplateManagementDao();
				seriesId = templatemanagementDao.getSeriesId(configReqToSendToC3pCode.getTemplateID(), seriesId);
				seriesId = StringUtils.substringAfter(seriesId, "Generic_");

				List<AttribCreateConfigPojo> masterAttribute = new ArrayList<>();
				/*
				 * List<AttribCreateConfigPojo> byAttribSeriesId = service
				 * .getByAttribSeriesId(seriesId); if (byAttribSeriesId != null &&
				 * !byAttribSeriesId.isEmpty()) { masterAttribute.addAll(byAttribSeriesId); }/*
				 * /* Extract dynamicAttribs Json Value and map it to MasteAtrribute List
				 */
				org.json.simple.JSONArray attribJson = null;
				if (json.containsKey("dynamicAttribs")) {
					attribJson = (org.json.simple.JSONArray) json.get("dynamicAttribs");
				}
				org.json.simple.JSONArray featureListJson = null;
				if (json.containsKey("selectedFeatures")) {
					featureListJson = (org.json.simple.JSONArray) json.get("selectedFeatures");
				}
				List<String> featureList = new ArrayList<String>();
				if (featureListJson != null && !featureListJson.isEmpty()) {
					for (int i = 0; i < featureListJson.size(); i++) {
						featureList.add((String) featureListJson.get(i));
					}
				}
				List<AttribCreateConfigPojo> templateAttribute = new ArrayList<>();
				for (String feature : featureList) {
					String templateId = configReqToSendToC3pCode.getTemplateID();
					List<AttribCreateConfigPojo> byAttribTemplateAndFeatureName = service
							.getByAttribTemplateAndFeatureName(templateId, feature);
					if (byAttribTemplateAndFeatureName != null && !byAttribTemplateAndFeatureName.isEmpty()) {
						templateAttribute.addAll(byAttribTemplateAndFeatureName);
					}
				}
				List<CreateConfigPojo> createConfigList = new ArrayList<>();
				if (attribJson != null) {
					for (int i = 0; i < attribJson.size(); i++) {
						JSONObject object = (JSONObject) attribJson.get(i);
						String attribLabel = object.get("label").toString();
						String attriValue = object.get("value").toString();
						String attribType = object.get("type").toString();
						for (AttribCreateConfigPojo attrib : masterAttribute) {

							if (attribLabel.contains(attrib.getAttribLabel())) {
								String attribName = attrib.getAttribName();
								CreateConfigPojo createConfigPojo = new CreateConfigPojo();
								createConfigPojo.setMasterLabelId(attrib.getId());
								createConfigPojo.setMasterLabelValue(attriValue);
								createConfigPojo.setTemplateId(configReqToSendToC3pCode.getTemplateID());
								createConfigList.add(createConfigPojo);

								if (attrib.getAttribType().equals("Master")) {

									if (attribType.equals("configAttrib")) {
										if (attribName.equals("Os Ver")) {
											configReqToSendToC3pCode.setOsVer(attriValue);
											break;
										}
										if (attribName.equals("Host Name Config")) {
											configReqToSendToC3pCode.setHostNameConfig(attriValue);
											break;
										}
										if (attribName.equals("Logging Buffer")) {
											configReqToSendToC3pCode.setLoggingBuffer(attriValue);
											break;
										}
										if (attribName.equals("Memory Size")) {
											configReqToSendToC3pCode.setMemorySize(attriValue);
											break;
										}
										if (attribName.equals("Logging SourceInterface")) {
											configReqToSendToC3pCode.setLoggingSourceInterface(attriValue);
											break;
										}
										if (attribName.equals("IP TFTP SourceInterface")) {
											configReqToSendToC3pCode.setiPTFTPSourceInterface(attriValue);
											break;
										}
										if (attribName.equals("IP FTP SourceInterface")) {
											configReqToSendToC3pCode.setiPFTPSourceInterface(attriValue);
											break;
										}
										if (attribName.equals("Line Con Password")) {
											configReqToSendToC3pCode.setLineConPassword(attriValue);
											break;
										}
										if (attribName.equals("Line Aux Password")) {
											configReqToSendToC3pCode.setLineAuxPassword(attriValue);
											break;
										}
										if (attribName.equals("Line VTY Password")) {
											configReqToSendToC3pCode.setLineVTYPassword(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib1")) {
											configReqToSendToC3pCode.setM_Attrib1(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib2")) {
											configReqToSendToC3pCode.setM_Attrib2(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib3")) {
											configReqToSendToC3pCode.setM_Attrib3(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib4")) {
											configReqToSendToC3pCode.setM_Attrib4(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib5")) {
											configReqToSendToC3pCode.setM_Attrib5(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib6")) {
											configReqToSendToC3pCode.setM_Attrib6(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib7")) {
											configReqToSendToC3pCode.setM_Attrib7(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib8")) {
											configReqToSendToC3pCode.setM_Attrib8(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib9")) {
											configReqToSendToC3pCode.setM_Attrib9(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib10")) {
											configReqToSendToC3pCode.setM_Attrib10(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib11")) {
											configReqToSendToC3pCode.setM_Attrib11(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib12")) {
											configReqToSendToC3pCode.setM_Attrib12(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib13")) {
											configReqToSendToC3pCode.setM_Attrib13(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib14")) {
											configReqToSendToC3pCode.setM_Attrib14(attriValue);
											break;
										}
										if (attribName.equals("M_Attrib15")) {
											configReqToSendToC3pCode.setM_Attrib15(attriValue);
											break;
										}
									}
								}
							}
						}
						for (AttribCreateConfigPojo templateAttrib : templateAttribute) {

							if (attribLabel.contains(templateAttrib.getAttribLabel())) {
								String attribName = templateAttrib.getAttribName();

								CreateConfigPojo createConfigPojo = new CreateConfigPojo();
								createConfigPojo.setMasterLabelId(templateAttrib.getId());
								createConfigPojo.setMasterLabelValue(attriValue);
								createConfigPojo.setTemplateId(configReqToSendToC3pCode.getTemplateID());
								createConfigList.add(createConfigPojo);
								if (templateAttrib.getAttribType().equals("Template")) {
									if (attribType.equals("templateAttrib")) {

										if (attribName.equals("LANInterfaceIP1")) {
											configReqToSendToC3pCode.setlANInterfaceIP1(attriValue);
											break;
										}
										if (attribName.equals("LANInterfaceMask1")) {
											configReqToSendToC3pCode.setlANInterfaceMask1(attriValue);
											break;
										}
										if (attribName.equals("LANInterfaceIP2")) {
											configReqToSendToC3pCode.setlANInterfaceIP2(attriValue);
											break;
										}
										if (attribName.equals("LANInterfaceMask2")) {
											configReqToSendToC3pCode.setlANInterfaceMask2(attriValue);
											break;
										}
										if (attribName.equals("WANInterfaceIP1")) {
											configReqToSendToC3pCode.setwANInterfaceIP1(attriValue);
											break;
										}

										if (attribName.equals("WANInterfaceMask1")) {
											configReqToSendToC3pCode.setwANInterfaceMask1(attriValue);
											break;
										}
										if (attribName.equals("WANInterfaceIP2")) {
											configReqToSendToC3pCode.setwANInterfaceIP2(attriValue);
											break;
										}
										if (attribName.equals("WANInterfaceMask2")) {
											configReqToSendToC3pCode.setwANInterfaceMask2(attriValue);
											break;
										}
										if (attribName.equals("ResInterfaceIP")) {
											configReqToSendToC3pCode.setResInterfaceIP(attriValue);
											break;
										}

										if (attribName.equals("ResInterfaceMask")) {
											configReqToSendToC3pCode.setResInterfaceMask(attriValue);
											break;
										}

										if (attribName.equals("VRFName")) {
											configReqToSendToC3pCode.setvRFName(attriValue);
											break;
										}

										if (attribName.equals("BGPASNumber")) {
											configReqToSendToC3pCode.setbGPASNumber(attriValue);
											break;
										}

										if (attribName.equals("BGPRouterID")) {
											configReqToSendToC3pCode.setbGPRouterID(attriValue);
											break;
										}

										if (attribName.equals("BGPNeighborIP1")) {
											configReqToSendToC3pCode.setResInterfaceIP(attriValue);
											break;
										}

										if (attribName.equals("BGPRemoteAS1")) {
											configReqToSendToC3pCode.setbGPRemoteAS1(attriValue);
											break;
										}

										if (attribName.equals("BGPNeighborIP2")) {
											configReqToSendToC3pCode.setbGPNeighborIP1(attriValue);
											break;
										}

										if (attribName.equals("BGPRemoteAS2")) {
											configReqToSendToC3pCode.setbGPRemoteAS2(attriValue);
											break;
										}

										if (attribName.equals("BGPNetworkIP1")) {
											configReqToSendToC3pCode.setbGPNetworkIP1(attriValue);
											break;
										}

										if (attribName.equals("BGPNetworkWildcard1")) {
											configReqToSendToC3pCode.setbGPNetworkWildcard1(attriValue);
											break;
										}

										if (attribName.equals("BGPNetworkIP2")) {
											configReqToSendToC3pCode.setbGPNetworkIP2(attriValue);
											break;
										}

										if (attribName.equals("BGPNetworkWildcard2")) {
											configReqToSendToC3pCode.setbGPNetworkWildcard2(attriValue);
											break;
										}

										if (attribName.equals("Attrib1")) {
											configReqToSendToC3pCode.setAttrib1(attriValue);
											break;
										}
										if (attribName.equals("Attrib2")) {
											configReqToSendToC3pCode.setAttrib2(attriValue);
											break;
										}
										if (attribName.equals("Attrib3")) {
											configReqToSendToC3pCode.setAttrib3(attriValue);
											break;
										}
										if (attribName.equals("Attrib4")) {
											configReqToSendToC3pCode.setAttrib4(attriValue);
											break;
										}
										if (attribName.equals("Attrib5")) {
											configReqToSendToC3pCode.setAttrib5(attriValue);
											break;
										}
										if (attribName.equals("Attrib6")) {
											configReqToSendToC3pCode.setAttrib6(attriValue);
											break;
										}
										if (attribName.equals("Attrib7")) {
											configReqToSendToC3pCode.setAttrib7(attriValue);
											break;
										}
										if (attribName.equals("Attrib8")) {
											configReqToSendToC3pCode.setAttrib8(attriValue);
											break;
										}
										if (attribName.equals("Attrib9")) {
											configReqToSendToC3pCode.setAttrib9(attriValue);
											break;
										}
										if (attribName.equals("Attrib10")) {
											configReqToSendToC3pCode.setAttrib10(attriValue);
											break;
										}
										if (attribName.equals("Attrib11")) {
											configReqToSendToC3pCode.setAttrib11(attriValue);
											break;
										}
										if (attribName.equals("Attrib12")) {
											configReqToSendToC3pCode.setAttrib12(attriValue);
											break;
										}
										if (attribName.equals("Attrib13")) {
											configReqToSendToC3pCode.setAttrib13(attriValue);
											break;
										}
										if (attribName.equals("Attrib14")) {
											configReqToSendToC3pCode.setAttrib14(attriValue);
											break;
										}
										if (attribName.equals("Attrib15")) {
											configReqToSendToC3pCode.setAttrib15(attriValue);
											break;
										}
										if (attribName.equals("Attrib16")) {
											configReqToSendToC3pCode.setAttrib16(attriValue);
											break;
										}
										if (attribName.equals("Attrib17")) {
											configReqToSendToC3pCode.setAttrib17(attriValue);
											break;
										}
										if (attribName.equals("Attrib18")) {
											configReqToSendToC3pCode.setAttrib18(attriValue);
											break;
										}
										if (attribName.equals("Attrib19")) {
											configReqToSendToC3pCode.setAttrib19(attriValue);
											break;
										}
										if (attribName.equals("Attrib20")) {
											configReqToSendToC3pCode.setAttrib20(attriValue);
											break;
										}
										if (attribName.equals("Attrib21")) {
											configReqToSendToC3pCode.setAttrib11(attriValue);
											break;
										}
										if (attribName.equals("Attrib22")) {
											configReqToSendToC3pCode.setAttrib22(attriValue);
											break;
										}
										if (attribName.equals("Attrib23")) {
											configReqToSendToC3pCode.setAttrib23(attriValue);
											break;
										}
										if (attribName.equals("Attrib24")) {
											configReqToSendToC3pCode.setAttrib24(attriValue);
											break;
										}
										if (attribName.equals("Attrib25")) {
											configReqToSendToC3pCode.setAttrib25(attriValue);
											break;
										}
										if (attribName.equals("Attrib26")) {
											configReqToSendToC3pCode.setAttrib26(attriValue);
											break;
										}
										if (attribName.equals("Attrib27")) {
											configReqToSendToC3pCode.setAttrib27(attriValue);
											break;
										}
										if (attribName.equals("Attrib28")) {
											configReqToSendToC3pCode.setAttrib28(attriValue);
											break;
										}
										if (attribName.equals("Attrib29")) {
											configReqToSendToC3pCode.setAttrib29(attriValue);
											break;
										}
										if (attribName.equals("Attrib30")) {
											configReqToSendToC3pCode.setAttrib30(attriValue);
											break;
										}
										if (attribName.equals("Attrib31")) {
											configReqToSendToC3pCode.setAttrib31(attriValue);
											break;
										}
										if (attribName.equals("Attrib32")) {
											configReqToSendToC3pCode.setAttrib32(attriValue);
											break;
										}
										if (attribName.equals("Attrib33")) {
											configReqToSendToC3pCode.setAttrib33(attriValue);
											break;
										}
										if (attribName.equals("Attrib34")) {
											configReqToSendToC3pCode.setAttrib34(attriValue);
											break;
										}
										if (attribName.equals("Attrib35")) {
											configReqToSendToC3pCode.setAttrib35(attriValue);
											break;
										}
										if (attribName.equals("Attrib36")) {
											configReqToSendToC3pCode.setAttrib36(attriValue);
											break;
										}
										if (attribName.equals("Attrib37")) {
											configReqToSendToC3pCode.setAttrib37(attriValue);
											break;
										}
										if (attribName.equals("Attrib38")) {
											configReqToSendToC3pCode.setAttrib38(attriValue);
											break;
										}
										if (attribName.equals("Attrib39")) {
											configReqToSendToC3pCode.setAttrib39(attriValue);
											break;
										}
										if (attribName.equals("Attrib40")) {
											configReqToSendToC3pCode.setAttrib40(attriValue);
											break;
										}
										if (attribName.equals("Attrib41")) {
											configReqToSendToC3pCode.setAttrib41(attriValue);
											break;
										}
										if (attribName.equals("Attrib42")) {
											configReqToSendToC3pCode.setAttrib42(attriValue);
											break;
										}
										if (attribName.equals("Attrib43")) {
											configReqToSendToC3pCode.setAttrib43(attriValue);
											break;
										}
										if (attribName.equals("Attrib44")) {
											configReqToSendToC3pCode.setAttrib44(attriValue);
											break;
										}
										if (attribName.equals("Attrib45")) {
											configReqToSendToC3pCode.setAttrib45(attriValue);
											break;
										}
										if (attribName.equals("Attrib46")) {
											configReqToSendToC3pCode.setAttrib36(attriValue);
											break;
										}
										if (attribName.equals("Attrib47")) {
											configReqToSendToC3pCode.setAttrib47(attriValue);
											break;
										}
										if (attribName.equals("Attrib48")) {
											configReqToSendToC3pCode.setAttrib48(attriValue);
											break;
										}
										if (attribName.equals("Attrib49")) {
											configReqToSendToC3pCode.setAttrib49(attriValue);
											break;
										}
										if (attribName.equals("Attrib50")) {
											configReqToSendToC3pCode.setAttrib50(attriValue);
											break;
										}

									}
								}
							}
						}
					}
				}
				// Passing Extra parameter createConfigList for saving master
				// attribute data
				result = dcmConfigService.updateAlldetails(configReqToSendToC3pCode, createConfigList);
				System.out.println("log");

			} else {
				result = dcmConfigService.updateAlldetails(configReqToSendToC3pCode, null);
			}

			for (Map.Entry<String, String> entry : result.entrySet()) {
				if (entry.getKey() == "requestID") {
					requestIdForConfig = entry.getValue();

				}
				if (entry.getKey() == "result") {
					res = entry.getValue();
					if (res.equalsIgnoreCase("true")) {
						data = "Submitted";
					}

				}

			}
			
		
			
			obj.put(new String("output"), new String(data));
			obj.put(new String("requestId"), new String(requestIdForConfig));
			obj.put(new String("version"), configReqToSendToC3pCode.getRequest_version());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return obj;

	}

	/* If Template Id in null or Empty only push Basic COnfiguration */
	private void createTemplateId(CreateConfigRequestDCM configReqToSendToC3pCode, String seriesId,
			List<AttribCreateConfigPojo> masterAttribute) {
		String templateName = "";
		templateName = dcmConfigService.getTemplateName(configReqToSendToC3pCode.getRegion(),
				configReqToSendToC3pCode.getVendor(), configReqToSendToC3pCode.getModel(),
				configReqToSendToC3pCode.getOs(), configReqToSendToC3pCode.getOsVersion());
		templateName = templateName + "_V1.0";
		configReqToSendToC3pCode.setTemplateID(templateName);

		InvokeFtl invokeFtl = new InvokeFtl();
		TemplateManagementDao dao = new TemplateManagementDao();
		// Getting Commands Using Series Id
		List<CommandPojo> cammandsBySeriesId = dao.getCammandsBySeriesId(seriesId, null);
		invokeFtl.createFinalTemplate(cammandsBySeriesId, null, masterAttribute, null,
				configReqToSendToC3pCode.getTemplateID());
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub

	}
	 public static Timestamp convertStringToTimestamp(String str_date) {
		    try {
		      DateFormat formatter;
		      formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		      Date date = (Date) formatter.parse(str_date);
		      java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());
		 
		      return timeStampDate;
		    } catch (ParseException e) {
		      System.out.println("Exception :" + e);
		      return null;
		    }
		  }
}
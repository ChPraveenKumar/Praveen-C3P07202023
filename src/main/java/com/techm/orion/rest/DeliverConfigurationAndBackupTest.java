package com.techm.orion.rest;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.POST;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.XML;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.gson.Gson;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.techm.orion.dao.RequestInfoDao;
import com.techm.orion.dao.RequestInfoDetailsDao;
import com.techm.orion.entitybeans.RequestInfoEntity;
import com.techm.orion.pojo.CreateConfigRequest;
import com.techm.orion.pojo.CreateConfigRequestDCM;
import com.techm.orion.pojo.RequestInfoPojo;
import com.techm.orion.pojo.UserPojo;
import com.techm.orion.repositories.RequestInfoDetailsRepositories;
import com.techm.orion.service.BackupCurrentRouterConfigurationService;
import com.techm.orion.service.ErrorCodeValidationDeliveryTest;
import com.techm.orion.utility.InvokeFtl;
import com.techm.orion.utility.ODLClient;
import com.techm.orion.utility.TextReport;
import com.techm.orion.utility.VNFHelper;

@Controller
@RequestMapping("/DeliverConfigurationAndBackupTest")
public class DeliverConfigurationAndBackupTest extends Thread {

	public static String TSA_PROPERTIES_FILE = "TSA.properties";
	public static final Properties TSA_PROPERTIES = new Properties();
	@Autowired
	RequestInfoDao requestInfoDao;

	@Autowired
	RequestInfoDetailsDao requestDao;
	
	@Autowired
	public RequestInfoDetailsRepositories requestInfoDetailsRepositories;

	@POST
	@RequestMapping(value = "/deliverConfigurationTest", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public JSONObject deliverConfigurationTest(@RequestBody String request) {

		JSONObject obj = new JSONObject();
		String jsonArray = "";

		InvokeFtl invokeFtl = new InvokeFtl();
		ErrorCodeValidationDeliveryTest errorCodeValidationDeliveryTest = new ErrorCodeValidationDeliveryTest();
		CreateConfigRequest createConfigRequest = new CreateConfigRequest();
		Boolean value = false;
		BackupCurrentRouterConfigurationService bckupConfigService = new BackupCurrentRouterConfigurationService();
		List<RequestInfoEntity> requestDetailEntity = new ArrayList<RequestInfoEntity>();
		long ftp_image_size = 0, available_flash_size = 0;
		Boolean isStartUp=false;
		RequestInfoPojo requestinfo = new RequestInfoPojo();

		try {
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(request);

			// Require requestId and version from camunda
			String RequestId = json.get("requestId").toString();
			String version = json.get("version").toString();
			createConfigRequest = requestInfoDao.getRequestDetailFromDBForVersion(RequestId, version);
			requestinfo = requestDao.getRequestDetailTRequestInfoDBForVersion(RequestId, version);
			
            requestDetailEntity = requestInfoDetailsRepositories.findAllByAlphanumericReqId(RequestId);
			
			for(int i=0; i<requestDetailEntity.size();i++)
			{
				isStartUp= requestDetailEntity.get(i).getStartUp();
			}

			if (createConfigRequest.getManagementIp() != null && !createConfigRequest.getManagementIp().equals("")) {

				createConfigRequest.setRequestId(RequestId);
				createConfigRequest.setRequest_version(Double.parseDouble(json.get("version").toString()));

				DeliverConfigurationAndBackupTest.loadProperties();
				String host = createConfigRequest.getManagementIp();
				UserPojo userPojo = new UserPojo();
				userPojo = requestInfoDao.getRouterCredentials();

				String user = userPojo.getUsername();
				String password = userPojo.getPassword();
				String port = DeliverConfigurationAndBackupTest.TSA_PROPERTIES.getProperty("portSSH");

				if (json.get("requestType").toString().equalsIgnoreCase("SLGF")) {

					boolean isBackUpSccessful = false, isFlashSizeAvailable = false, copyFtpStatus = false,
							isBootSystemFlashSuccessful = false;

					// Verify flash size needs to be done
					// available_flash_size=getAvailableFlashSizeOnDevice(user,password,host);

					// ftp_image_size=getFTPImageSize();
					// String ftp_image_name=getFTPImageName();
					String ftp_image_name = "test2.bin";
					available_flash_size = 9;
					ftp_image_size = 4;
					// set login to csr flag in DB to 1
					String key = "login_flag";
					requestInfoDao.update_dilevary_step_flag_in_db(key, 1, RequestId, version);
					if (available_flash_size > ftp_image_size) {
						// free size in flash and then go to back up and dilevary to be
						// done!!!!!!!!!!!!!!!!!!!

						// if flash sized freed successfully
						isFlashSizeAvailable = true;

						if (isFlashSizeAvailable) {
							// flash size available flag in DB to 1
							key = "flash_size_flag";
							requestInfoDao.update_dilevary_step_flag_in_db(key, 1, RequestId, version);

							isBackUpSccessful = BackUp(createConfigRequest, user, password, "previous");

							// isBackUpSccessful=true;
							if (isBackUpSccessful) {
								// back up flag in DB to 1
								key = "back_up_flag";
								requestInfoDao.update_dilevary_step_flag_in_db(key, 1, RequestId, version);

								// issue copy ftp flash command on csr
								ArrayList<String> commandToPush = new ArrayList<String>();
								commandToPush.add("copy ftp: flash:");
								commandToPush.add("127.0.0.1");
								commandToPush.add("soucename");
								commandToPush.add("destinationname");

								// Erase flash: before copying? [confirm]n one param is skipped as gns has no
								// flash
								String cmd = "copy ftp: flash:" + "\n" + "127.0.0.1" + "\n" + "soucename" + "\n"
										+ "destination";

								String port1 = DeliverConfigurationAndBackupTest.TSA_PROPERTIES.getProperty("portSSH");
								JSch jsch = new JSch();
								Channel channel = null;
								Session session = jsch.getSession(user, host, Integer.parseInt(port1));
								Properties config = new Properties();
								config.put("StrictHostKeyChecking", "no");
								session.setConfig(config);
								session.setPassword(password);

								session.connect();
								try {
									Thread.sleep(10000);
								} catch (Exception ee) {
								}
								channel = session.openChannel("shell");
								OutputStream ops = channel.getOutputStream();
								PrintStream ps = new PrintStream(ops, true);
								System.out
										.println("Channel Connected to machine " + host + " server for copy ftp flash");
								channel.connect();
								InputStream input = channel.getInputStream();

								ps.println(cmd);

								try {
									// change this sleep in case of longer wait
									Thread.sleep(1000);
								} catch (Exception ee) {
								}
								BufferedWriter bw = null;
								FileWriter fw = null;
								int SIZE = 1024;
								byte[] tmp = new byte[SIZE];
								while (input.available() > 0) {
									int i = input.read(tmp, 0, SIZE);
									if (i < 0)
										break;
									// we will get response from router here
									// String s = new String(tmp, 0, i);
									// Hardcoding it for time being
									String s = "Loading c7200-a3js-mz.122-15.T16.bin from 172.22.1.84 (via GigabitEthernet0/1):/n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!/n[OK - 19187152 bytes]/n/nVerifying checksum...  OK (0x15C1)19187152 bytes/ncopied in 482.920 secs (39732 bytes/sec)";
									System.out.println("EOSubBlock");
									List<String> outList = new ArrayList<String>();
									String str[] = s.split("/n");
									outList = Arrays.asList(str);
									int size = outList.size();
									if (outList.get(size - 1).indexOf("OK") != 0) {
										copyFtpStatus = true;
									}

								}
								// requestInfoDao.editRequestforReportWebserviceInfo(createConfigRequest.getRequestId(),Double.toString(createConfigRequest.getRequest_version()),"deliever_config","2","Failure");
								System.out.println("EOBlock");
								if (copyFtpStatus) {
									// set copy ftp flag in DB to 1
									key = "os_download_flag";
									requestInfoDao.update_dilevary_step_flag_in_db(key, 1, RequestId, version);

									// Do boot system flash to copy latest image on top

									// 1. Do show run on router and get boot commands and append no to them

									List<String> exsistingBootCmdsOnRouter = getExsistingBootCmds(user, password, host);

									// append no commands to exsisting boot commands and add to cmds array to be
									// pushed

									List<String> cmdsToPushInorder = new ArrayList<String>();

									for (int i = 0; i < exsistingBootCmdsOnRouter.size(); i++) {
										cmdsToPushInorder.add("no " + exsistingBootCmdsOnRouter.get(i) + "\r\n");
									}

									// cmdsToPushInorder.add(1,"boot system flash ");
									cmdsToPushInorder.add(0, "conf t\r\nboot system flash " + ftp_image_name);

									// cmdsToPushInorder.add(cmdsToPushInorder.size()+1,"exit");

									isBootSystemFlashSuccessful = pushOnRouter(user, password, host, cmdsToPushInorder);
									// push the array on router

									if (isBootSystemFlashSuccessful) {
										// set bootsystemflash,reload,postloginflag in DB to 1
										key = "boot_system_flash_flag";
										requestInfoDao.update_dilevary_step_flag_in_db(key, 1, RequestId, version);
										key = "reload_flag";
										requestInfoDao.update_dilevary_step_flag_in_db(key, 1, RequestId, version);
										key = "post_login_flag";
										requestInfoDao.update_dilevary_step_flag_in_db(key, 1, RequestId, version);

										value = true;

										requestInfoDao.editRequestforReportWebserviceInfo(
												createConfigRequest.getRequestId(),
												Double.toString(createConfigRequest.getRequest_version()),
												"deliever_config", "1", "In Progress");
										// requestInfoDao.editRequestForReportIOSWebserviceInfo(createConfigRequest.getRequestId(),Double.toString(createConfigRequest.getRequest_version()),"Boot
										// System Flash","Failure","Could not load image on top on boot commands.");
										// CODE for write and reload to be done!!!!!
										BackUp(createConfigRequest, user, password, "current");
										jsonArray = new Gson().toJson(value);
										obj.put(new String("output"), jsonArray);
									} else {
										value = false;
										key = "boot_system_flash_flag";
										requestInfoDao.update_dilevary_step_flag_in_db(key, 2, RequestId, version);
										key = "reload_flag";
										requestInfoDao.update_dilevary_step_flag_in_db(key, 2, RequestId, version);
										key = "post_login_flag";
										requestInfoDao.update_dilevary_step_flag_in_db(key, 2, RequestId, version);
										requestInfoDao.editRequestforReportWebserviceInfo(
												createConfigRequest.getRequestId(),
												Double.toString(createConfigRequest.getRequest_version()),
												"deliever_config", "2", "Failure");
										requestInfoDao.editRequestForReportIOSWebserviceInfo(
												createConfigRequest.getRequestId(),
												Double.toString(createConfigRequest.getRequest_version()),
												"Boot System Flash", "Failure",
												"Could not load image on top on boot commands.");
										jsonArray = new Gson().toJson(value);
										BackUp(createConfigRequest, user, password, "current");
										obj.put(new String("output"), jsonArray);
									}

								} else {
									value = false;
									key = "os_download_flag";
									requestInfoDao.update_dilevary_step_flag_in_db(key, 2, RequestId, version);
									// could not copy image to csr
									// return error String s,output
									requestInfoDao.editRequestforReportWebserviceInfo(
											createConfigRequest.getRequestId(),
											Double.toString(createConfigRequest.getRequest_version()),
											"deliever_config", "2", "Failure");
									requestInfoDao.editRequestForReportIOSWebserviceInfo(
											createConfigRequest.getRequestId(),
											Double.toString(createConfigRequest.getRequest_version()),
											"Copy FTP to CSR", "Failure", "Could not copy image from FTP to CSR.");
									jsonArray = new Gson().toJson(value);
									obj.put(new String("output"), jsonArray);
								}

							} else {
								value = false;
								isBackUpSccessful = false;
								key = "back_up_flag";
								requestInfoDao.update_dilevary_step_flag_in_db(key, 2, RequestId, version);
								// throw corresponding error from router on screen
								requestInfoDao.editRequestforReportWebserviceInfo(createConfigRequest.getRequestId(),
										Double.toString(createConfigRequest.getRequest_version()), "deliever_config",
										"2", "Failure");
								requestInfoDao.editRequestForReportIOSWebserviceInfo(createConfigRequest.getRequestId(),
										Double.toString(createConfigRequest.getRequest_version()), "Back up", "Failure",
										"Back up unsuccessful.");
								jsonArray = new Gson().toJson(value);
								obj.put(new String("output"), jsonArray);
							}
						}
					} else {
						isFlashSizeAvailable = false;
						key = "flash_size_flag";
						requestInfoDao.update_dilevary_step_flag_in_db(key, 2, RequestId, version);
						// throw error
						value = false;
						requestInfoDao.editRequestforReportWebserviceInfo(createConfigRequest.getRequestId(),
								Double.toString(createConfigRequest.getRequest_version()), "deliever_config", "2",
								"Failure");
						requestInfoDao.editRequestForReportIOSWebserviceInfo(createConfigRequest.getRequestId(),
								Double.toString(createConfigRequest.getRequest_version()), "Flash Size", "Failure",
								"No enough flash size available, flash could not be cleared");
						jsonArray = new Gson().toJson(value);
						obj.put(new String("output"), jsonArray);
					}

				} else if (json.get("requestType").toString().equalsIgnoreCase("SLGT")) {

					value = true;
					jsonArray = new Gson().toJson(value);
					// get previous milestone status as the status of request will not change in
					// this milestone
					String status = requestInfoDao.getPreviousMileStoneStatus(createConfigRequest.getRequestId(),
							Double.toString(createConfigRequest.getRequest_version()));
					String switchh = "0";
					if (status.equalsIgnoreCase("Partial Success")) {
						switchh = "3";
					} else if (status.equalsIgnoreCase("In Progress")) {
						switchh = "0";
					}

					requestInfoDao.editRequestforReportWebserviceInfo(createConfigRequest.getRequestId(),
							Double.toString(createConfigRequest.getRequest_version()), "deliever_config", "0", status);

					obj.put(new String("output"), jsonArray);
					System.out.println("Out of dilever config");
				}

				else if(json.get("requestType").toString()
						.equalsIgnoreCase("SLGB"))
				{

					ArrayList<String> commandToPush = new ArrayList<String>();

					JSch jsch = new JSch();
					Channel channel = null;
					Session session = jsch.getSession(user, host,
							Integer.parseInt(port));
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
						System.out.println("Channel Connected to machine " + host
								+ " server");
						channel.connect();
						InputStream input = channel.getInputStream();

						// to save the backup and deliver the
						// configuration(configuration in the router)
						boolean isCheck = bckupConfigService.getRouterConfig(createConfigRequest,
								"previous");

					
								// db call for success deliver config
							
							
							if(isStartUp==true)
							{


								ArrayList<String> commandToPush1 = new ArrayList<String>();

								JSch jsch1 = new JSch();
								Channel channel1 = null;
								Session session1 = jsch1.getSession(user, host,
										Integer.parseInt(port));
								Properties config1 = new Properties();
								config1.put("StrictHostKeyChecking", "no");
								session1.setConfig(config1);
								session1.setPassword(password);
								session1.connect();
								try {
									Thread.sleep(10000);
								} catch (Exception ee) {
								}
								try {
									channel1 = session1.openChannel("shell");
									OutputStream ops1 = channel1.getOutputStream();

									PrintStream ps1 = new PrintStream(ops1, true);
									System.out.println("Channel Connected to machine " + host
											+ " server");
									channel1.connect();
									InputStream input1 = channel1.getInputStream();

									// to save the backup and deliver the
									// configuration(configuration in the router)
									boolean isCheck1 = bckupConfigService.getRouterConfigStartUp(createConfigRequest,
											"startup");

								
											// db call for success deliver config
										
									channel1.disconnect();
									session1.disconnect();	
											
										
									}
								catch (Exception ee) {
								}  
							}
								requestInfoDao.editRequestforReportWebserviceInfo(
										createConfigRequest.getRequestId(), Double
												.toString(createConfigRequest
														.getRequest_version()),
										"deliever_config", "1", "In Progress");
								
								
								requestInfoDao.updateRequestforReportWebserviceInfo(
										createConfigRequest.getRequestId());
								
								
							if(isCheck)
							{
								value=true;
							} else
							{
								value=false;
							}

									channel.disconnect();
									session.disconnect();
									jsonArray = new Gson().toJson(value);
									obj.put(new String("output"), jsonArray);
								
							
						}
					catch (Exception ee) {
					}

						channel.disconnect();
						session.disconnect();
						jsonArray = new Gson().toJson(value);
						obj.put(new String("output"), jsonArray);
					} else if (json.get("requestType").toString().equalsIgnoreCase("SLGC")) {
					ArrayList<String> commandToPush = new ArrayList<String>();

					JSch jsch = new JSch();
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
						System.out.println("Channel Connected to machine " + host + " server");
						channel.connect();
						InputStream input = channel.getInputStream();

						// to save the backup and deliver the
						// configuration(configuration in the router)
						bckupConfigService.getRouterConfig(createConfigRequest, "previous");

						session = jsch.getSession(user, host, Integer.parseInt(port));
						config = new Properties();
						config.put("StrictHostKeyChecking", "no");
						session.setConfig(config);
						session.setPassword(password);
						session.connect();

						channel = session.openChannel("shell");
						ops = channel.getOutputStream();

						ps = new PrintStream(ops, true);

						channel.connect();

						input = channel.getInputStream();

						Map<String, String> resultForFlag = new HashMap<String, String>();
						resultForFlag = requestInfoDao.getRequestFlag(createConfigRequest.getRequestId(),
								createConfigRequest.getRequest_version());
						String flagForPrevalidation = "";
						String flagFordelieverConfig = "";
						for (Map.Entry<String, String> entry : resultForFlag.entrySet()) {
							if (entry.getKey() == "flagForPrevalidation") {
								flagForPrevalidation = entry.getValue();

							}
							if (entry.getKey() == "flagFordelieverConfig") {
								flagFordelieverConfig = entry.getValue();
							}

						}

						// print the no config for child version
						if ((!(createConfigRequest.getRequest_version() == createConfigRequest
								.getRequest_parent_version())) && flagForPrevalidation.equalsIgnoreCase("1")
								&& flagFordelieverConfig.equalsIgnoreCase("1")) {

							commandToPush = readFileNoCmd(createConfigRequest.getRequestId(),
									Double.toString(createConfigRequest.getRequest_version()));
							if (!(commandToPush.get(0).contains("null"))) {
								ps.println("config t");
								for (String arr : commandToPush) {

									ps.println(arr);

									printResult(input, channel, createConfigRequest.getRequestId(),
											Double.toString(createConfigRequest.getRequest_version()));

								}
								ps.println("exit");
								try {
									Thread.sleep(4000);
								} catch (Exception ee) {
								}
							}
						}
						// then deliver or push the configuration
						// ps.println("config t");

						commandToPush = readFile(createConfigRequest.getRequestId(),
								Double.toString(createConfigRequest.getRequest_version()));

						if (!(commandToPush.get(0).contains("null"))) {
							ps.println("config t");
							for (String arr : commandToPush) {

								ps.println(arr);

								printResult(input, channel, createConfigRequest.getRequestId(),
										Double.toString(createConfigRequest.getRequest_version()));

							}
							printResult(input, channel, createConfigRequest.getRequestId(),
									Double.toString(createConfigRequest.getRequest_version()));

							String errorType = errorCodeValidationDeliveryTest.checkErrorCode(
									createConfigRequest.getRequestId(), createConfigRequest.getRequest_version());
							// get the router configuration after delivery

							createConfigRequest.setHostname(createConfigRequest.getHostname());
							createConfigRequest.setRequestId(createConfigRequest.getRequestId());
							// do error code validation
							if (errorType.equalsIgnoreCase("Warning") || errorType.equalsIgnoreCase("No Error")) {
								value = true;

								String response = invokeFtl.generateDileveryConfigFile(createConfigRequest);
								try {
									String responseDownloadPath = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
											.getProperty("responseDownloadPath");
									TextReport.writeFile(responseDownloadPath,
											createConfigRequest.getRequestId() + "V"
													+ Double.toString(createConfigRequest.getRequest_version())
													+ "_deliveredConfig.txt",
											response);

								} catch (IOException exe) {
									exe.printStackTrace();

								}

								bckupConfigService.getRouterConfig(createConfigRequest, "current");
								// db call for success deliver config
								requestInfoDao.editRequestforReportWebserviceInfo(createConfigRequest.getRequestId(),
										Double.toString(createConfigRequest.getRequest_version()), "deliever_config",
										"1", "In Progress");
								// network test
								// networkTestSSH.NetworkTest(configRequest);
							} else {
								value = false;

								errorCodeValidationDeliveryTest.pushNoCommandConfiguration(createConfigRequest);

								// errorCodeValidationDeliveryTest.pushPreviousVersionConfiguration(createConfigRequest);
								String response = invokeFtl.generateDileveryConfigFile(createConfigRequest);
								try {
									String responseDownloadPath = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
											.getProperty("responseDownloadPath");
									TextReport.writeFile(responseDownloadPath,
											createConfigRequest.getRequestId() + "V"
													+ Double.toString(createConfigRequest.getRequest_version())
													+ "_deliveredConfig.txt",
											response);

								} catch (IOException exe) {
									exe.printStackTrace();

								}

								bckupConfigService.getRouterConfig(createConfigRequest, "current");

								// db call for failure in deliver config
								requestInfoDao.editRequestforReportWebserviceInfo(createConfigRequest.getRequestId(),
										Double.toString(createConfigRequest.getRequest_version()), "deliever_config",
										"2", "Failure");

							}
						}

						// called if we have nothing to push and we directly change
						// the staus og delivery to success
						else {
							value = true;

							String response = invokeFtl.generateDileveryConfigFile(createConfigRequest);
							try {
								String responseDownloadPath = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
										.getProperty("responseDownloadPath");
								TextReport.writeFile(responseDownloadPath,
										createConfigRequest.getRequestId() + "V"
												+ Double.toString(createConfigRequest.getRequest_version())
												+ "_deliveredConfig.txt",
										response);

							} catch (IOException exe) {
								exe.printStackTrace();

							}

							bckupConfigService.getRouterConfig(createConfigRequest, "current");
							// db call for success deliver config
							requestInfoDao.editRequestforReportWebserviceInfo(createConfigRequest.getRequestId(),
									Double.toString(createConfigRequest.getRequest_version()), "deliever_config", "1",
									"In Progress");

						}

						channel.disconnect();
						session.disconnect();
						jsonArray = new Gson().toJson(value);
						obj.put(new String("output"), jsonArray);
					} catch (IOException ex) {
						jsonArray = new Gson().toJson(value);
						obj.put(new String("output"), jsonArray);
						requestInfoDao.editRequestforReportWebserviceInfo(createConfigRequest.getRequestId(),
								Double.toString(createConfigRequest.getRequest_version()), "deliever_config", "2",
								"Failure");
						String response = invokeFtl.generateDeliveryConfigFileFailure(createConfigRequest);
						try {
							String responseDownloadPath = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
									.getProperty("responseDownloadPath");
							TextReport.writeFile(responseDownloadPath,
									createConfigRequest.getRequestId() + "V"
											+ Double.toString(createConfigRequest.getRequest_version())
											+ "_deliveredConfig.txt",
									response);

						} catch (IOException exe) {

						}
					}
					session.disconnect();

				} else if (json.get("requestType").toString().equalsIgnoreCase("SNRC")) {
					

					//for restconf
					VNFHelper helper=new VNFHelper();
					//call method for backup from vnf utils
					ODLClient client=new ODLClient();
					boolean result=client.doGetODLBackUp(createConfigRequest.getRequestId(), Double.toString(createConfigRequest.getRequest_version()), "http://10.62.0.119:8181/restconf/config/network-topology:network-topology/topology/topology-netconf/node/CSR1000v/yang-ext:mount/ned:native", "previous");
					//boolean result=true;
					//call method for dilevary from vnf utils
					if(result==true)
					{
						//go for dilevary
						
						boolean dilevaryresult=false;
						
						//dilevaryresult=true;
						
						//Get XML to be pushed from local
						String responseDownloadPathRestConf = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
								.getProperty("VnfConfigCreationPath");
						String path=responseDownloadPathRestConf+"/"+createConfigRequest.getRequestId()+"_ConfigurationToPush.xml";
						//VNFHelper helper = new VNFHelper();
						String payload=helper.readConfigurationXML(path);
						
						System.out.println("log");
						//dilevaryresult=client.doPUTDilevary(createConfigRequest.getRequestId(), Double.toString(createConfigRequest.getRequest_version()),"http://10.62.0.119:8181/restconf/config/network-topology:network-topology/topology/topology-netconf/node/CSR1000v/yang-ext:mount/ned:native/interface ");
						
						 
						 String payloadLoopback=helper.getPayload("Loopback",payload);
						 
						 //dilevaryresult=client.doPUTDilevary(createConfigRequest.getRequestId(), Double.toString(createConfigRequest.getRequest_version()),"http://10.62.0.119:8181/restconf/config/network-topology:network-topology/topology/topology-netconf/node/CSR1000v/yang-ext:mount/ned:native/interface",payloadLoopback);
						 dilevaryresult=true;
						 if(dilevaryresult)
						 {
						 String payloadMultilink=helper.getPayload("Multilink",payload);
						dilevaryresult=client.doPUTDilevary(createConfigRequest.getRequestId(), Double.toString(createConfigRequest.getRequest_version()),"http://10.62.0.119:8181/restconf/config/network-topology:network-topology/topology/topology-netconf/node/CSR1000v/yang-ext:mount/ned:native/interface",payloadMultilink);
						 //dilevaryresult=true;
						 if(dilevaryresult)
						 {
							 String payloadVT=helper.getPayload("Virtual-Template",payload);

							// dilevaryresult=client.doPUTDilevary(createConfigRequest.getRequestId(), Double.toString(createConfigRequest.getRequest_version()),"http://10.62.0.119:8181/restconf/config/network-topology:network-topology/topology/topology-netconf/node/CSR1000v/yang-ext:mount/ned:native/interface",payloadVT);
							 dilevaryresult=true;
							 if(dilevaryresult)
							 {
								 dilevaryresult=true;
							 }
							 else
							 {
								 dilevaryresult=false;
							 }
						 }
						 else
						 {
							 dilevaryresult=false;

							 //error handling
						 }
						 }
						 else
						 {
							 dilevaryresult=false;

							//error handling
						 }

							
							
						/////////////////Need to write code for put service for dilevary of config
						if (dilevaryresult==true)
						{
							//take current config back up
							boolean currentconfig=client.doGetODLBackUp(createConfigRequest.getRequestId(), Double.toString(createConfigRequest.getRequest_version()), "http://10.62.0.119:8181/restconf/config/network-topology:network-topology/topology/topology-netconf/node/CSR1000v/yang-ext:mount/ned:native", "current");
							//boolean currentconfig=true;
							if(currentconfig== true)
							{
								requestInfoDao.editRequestforReportWebserviceInfo(
										createConfigRequest.getRequestId(), Double
												.toString(createConfigRequest
														.getRequest_version()),
										"deliever_config", "1", "In Progress");
								value = true;
								jsonArray = new Gson().toJson(value);
								obj.put(new String("output"), jsonArray);

								String response = invokeFtl
										.generateDileveryConfigFile(createConfigRequest);
								try {
									String responseDownloadPath = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
											.getProperty("responseDownloadPath");
									TextReport
											.writeFile(
													responseDownloadPath,
													createConfigRequest.getRequestId()
															+ "V"
															+ Double.toString(createConfigRequest
																	.getRequest_version())
															+ "_deliveredConfig.txt",
													response);

								} catch (IOException exe) {
									exe.printStackTrace();

								}
							}
							else
							{
								value=false;
								requestInfoDao.editRequestforReportWebserviceInfo(
										createConfigRequest.getRequestId(), Double
												.toString(createConfigRequest
														.getRequest_version()),
										"deliever_config", "2", "Failure");
								jsonArray = new Gson().toJson(value);
								obj.put(new String("output"), jsonArray);
								String response="";
								String responseDownloadPath="";
								try {
									requestInfoDao.editRequestforReportWebserviceInfo(createConfigRequest.getRequestId(),Double.toString(createConfigRequest.getRequest_version()),"deliever_config","2","Failure");
									response = invokeFtl.generateDeliveryConfigFileFailure(createConfigRequest);
									responseDownloadPath = BackupCurrentRouterConfigurationService.TSA_PROPERTIES
											.getProperty("responseDownloadPath");
									TextReport.writeFile(responseDownloadPath, createConfigRequest.getRequestId()
											+"V"+Double.toString(createConfigRequest.getRequest_version())+"_deliveredConfig.txt", response);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						else
						{
							value=false;
							jsonArray = new Gson().toJson(value);
							obj.put(new String("output"), jsonArray);
							String response="";
							String responseDownloadPath="";
							try {
								requestInfoDao.editRequestforReportWebserviceInfo(createConfigRequest.getRequestId(),Double.toString(createConfigRequest.getRequest_version()),"deliever_config","2","Failure");
								response = invokeFtl.generateDeliveryConfigFileFailure(createConfigRequest);
								responseDownloadPath = BackupCurrentRouterConfigurationService.TSA_PROPERTIES
										.getProperty("responseDownloadPath");
								TextReport.writeFile(responseDownloadPath, createConfigRequest.getRequestId()
										+"V"+Double.toString(createConfigRequest.getRequest_version())+"_deliveredConfig.txt", response);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					else
					{
						value=false;
						String response="";
						String responseDownloadPath="";
						try {
							requestInfoDao.editRequestforReportWebserviceInfo(createConfigRequest.getRequestId(),Double.toString(createConfigRequest.getRequest_version()),"deliever_config","2","Failure");
							response = invokeFtl.generateDeliveryConfigFileFailure(createConfigRequest);
							responseDownloadPath = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
									.getProperty("responseDownloadPath");
							TextReport.writeFile(responseDownloadPath, createConfigRequest.getRequestId()
									+"V"+Double.toString(createConfigRequest.getRequest_version())+"_deliveredConfig.txt", response);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					//call method for back up from vnf utils for current configuration
				
					
					
				} else if (json.get("requestType").toString().equalsIgnoreCase("SNNC")) {
					
					// push configuration for Netconf devices String
					String requestId = createConfigRequest.getRequestId();
					String responseDownloadPathNetconf = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
							.getProperty("VnfConfigCreationPath");
					String path = responseDownloadPathNetconf + "/" + requestId
							+ "_ConfigurationToPush.xml";
					VNFHelper helper = new VNFHelper();
					String payload = helper.readConfigurationXML(path);
					// get file from vnf config requests folder
					// pass file path to vnf helper class push on device method.
					boolean result = helper.pushOnVnfDevice(path);
					if (result) {
						value = true;

						String response = invokeFtl
								.generateDileveryConfigFile(createConfigRequest);
						try {
							String responseDownloadPath = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
									.getProperty("responseDownloadPath");
							TextReport
									.writeFile(
											responseDownloadPath,
											createConfigRequest.getRequestId()
													+ "V"
													+ Double.toString(createConfigRequest
															.getRequest_version())
													+ "_deliveredConfig.txt",
											response);

						} catch (IOException exe) {
							exe.printStackTrace();

						}
					} else {
						value = false;
						jsonArray = new Gson().toJson(value);
						obj.put(new String("output"), jsonArray);
						String response = "";
						String responseDownloadPath = "";
						try {
							requestInfoDao.editRequestforReportWebserviceInfo(
									createConfigRequest.getRequestId(), Double
											.toString(createConfigRequest
													.getRequest_version()),
									"deliever_config", "2", "Failure");
							response = invokeFtl
									.generateDeliveryConfigFileFailure(createConfigRequest);
							responseDownloadPath = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
									.getProperty("responseDownloadPath");
							TextReport
									.writeFile(
											responseDownloadPath,
											createConfigRequest.getRequestId()
													+ "V"
													+ Double.toString(createConfigRequest
															.getRequest_version())
													+ "_deliveredConfig.txt",
											response);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							// e.printStackTrace();
						}
					}
				}

			} else if (requestinfo.getManagementIp() != null && !requestinfo.getManagementIp().equals("")) {

				requestinfo.setAlphanumericReqId(RequestId);
				requestinfo.setRequestVersion(Double.parseDouble(json.get("version").toString()));

				DeliverConfigurationAndBackupTest.loadProperties();
				String host = requestinfo.getManagementIp();
				UserPojo userPojo = new UserPojo();
				userPojo = requestInfoDao.getRouterCredentials();

				String user = userPojo.getUsername();
				String password = userPojo.getPassword();
				String port = DeliverConfigurationAndBackupTest.TSA_PROPERTIES.getProperty("portSSH");

				if (json.get("requestType").toString().equalsIgnoreCase("SLGF")) {

					boolean isBackUpSccessful = false, isFlashSizeAvailable = false, copyFtpStatus = false,
							isBootSystemFlashSuccessful = false;

					// Verify flash size needs to be done
					// available_flash_size=getAvailableFlashSizeOnDevice(user,password,host);

					// ftp_image_size=getFTPImageSize();
					// String ftp_image_name=getFTPImageName();
					String ftp_image_name = "test2.bin";
					available_flash_size = 9;
					ftp_image_size = 4;
					// set login to csr flag in DB to 1
					String key = "login_flag";
					requestInfoDao.update_dilevary_step_flag_in_db(key, 1, RequestId, version);
					if (available_flash_size > ftp_image_size) {
						// free size in flash and then go to back up and dilevary to be
						// done!!!!!!!!!!!!!!!!!!!

						// if flash sized freed successfully
						isFlashSizeAvailable = true;

						if (isFlashSizeAvailable) {
							// flash size available flag in DB to 1
							key = "flash_size_flag";
							requestInfoDao.update_dilevary_step_flag_in_db(key, 1, RequestId, version);

							isBackUpSccessful = BackUp(requestinfo, user, password, "previous");

							// isBackUpSccessful=true;
							if (isBackUpSccessful) {
								// back up flag in DB to 1
								key = "back_up_flag";
								requestInfoDao.update_dilevary_step_flag_in_db(key, 1, RequestId, version);

								// issue copy ftp flash command on csr
								ArrayList<String> commandToPush = new ArrayList<String>();
								commandToPush.add("copy ftp: flash:");
								commandToPush.add("127.0.0.1");
								commandToPush.add("soucename");
								commandToPush.add("destinationname");

								// Erase flash: before copying? [confirm]n one param is skipped as gns has no
								// flash
								String cmd = "copy ftp: flash:" + "\n" + "127.0.0.1" + "\n" + "soucename" + "\n"
										+ "destination";

								String port1 = DeliverConfigurationAndBackupTest.TSA_PROPERTIES.getProperty("portSSH");
								JSch jsch = new JSch();
								Channel channel = null;
								Session session = jsch.getSession(user, host, Integer.parseInt(port1));
								Properties config = new Properties();
								config.put("StrictHostKeyChecking", "no");
								session.setConfig(config);
								session.setPassword(password);

								session.connect();
								try {
									Thread.sleep(10000);
								} catch (Exception ee) {
								}
								channel = session.openChannel("shell");
								OutputStream ops = channel.getOutputStream();
								PrintStream ps = new PrintStream(ops, true);
								System.out
										.println("Channel Connected to machine " + host + " server for copy ftp flash");
								channel.connect();
								InputStream input = channel.getInputStream();

								ps.println(cmd);

								try {
									// change this sleep in case of longer wait
									Thread.sleep(1000);
								} catch (Exception ee) {
								}
								BufferedWriter bw = null;
								FileWriter fw = null;
								int SIZE = 1024;
								byte[] tmp = new byte[SIZE];
								while (input.available() > 0) {
									int i = input.read(tmp, 0, SIZE);
									if (i < 0)
										break;
									// we will get response from router here
									// String s = new String(tmp, 0, i);
									// Hardcoding it for time being
									String s = "Loading c7200-a3js-mz.122-15.T16.bin from 172.22.1.84 (via GigabitEthernet0/1):/n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!/n[OK - 19187152 bytes]/n/nVerifying checksum...  OK (0x15C1)19187152 bytes/ncopied in 482.920 secs (39732 bytes/sec)";
									System.out.println("EOSubBlock");
									List<String> outList = new ArrayList<String>();
									String str[] = s.split("/n");
									outList = Arrays.asList(str);
									int size = outList.size();
									if (outList.get(size - 1).indexOf("OK") != 0) {
										copyFtpStatus = true;
									}

								}
								// requestInfoDao.editRequestforReportWebserviceInfo(createConfigRequest.getRequestId(),Double.toString(createConfigRequest.getRequest_version()),"deliever_config","2","Failure");
								System.out.println("EOBlock");
								if (copyFtpStatus) {
									// set copy ftp flag in DB to 1
									key = "os_download_flag";
									requestInfoDao.update_dilevary_step_flag_in_db(key, 1, RequestId, version);

									// Do boot system flash to copy latest image on top

									// 1. Do show run on router and get boot commands and append no to them

									List<String> exsistingBootCmdsOnRouter = getExsistingBootCmds(user, password, host);

									// append no commands to exsisting boot commands and add to cmds array to be
									// pushed

									List<String> cmdsToPushInorder = new ArrayList<String>();

									for (int i = 0; i < exsistingBootCmdsOnRouter.size(); i++) {
										cmdsToPushInorder.add("no " + exsistingBootCmdsOnRouter.get(i) + "\r\n");
									}

									// cmdsToPushInorder.add(1,"boot system flash ");
									cmdsToPushInorder.add(0, "conf t\r\nboot system flash " + ftp_image_name);

									// cmdsToPushInorder.add(cmdsToPushInorder.size()+1,"exit");

									isBootSystemFlashSuccessful = pushOnRouter(user, password, host, cmdsToPushInorder);
									// push the array on router

									if (isBootSystemFlashSuccessful) {
										// set bootsystemflash,reload,postloginflag in DB to 1
										key = "boot_system_flash_flag";
										requestInfoDao.update_dilevary_step_flag_in_db(key, 1, RequestId, version);
										key = "reload_flag";
										requestInfoDao.update_dilevary_step_flag_in_db(key, 1, RequestId, version);
										key = "post_login_flag";
										requestInfoDao.update_dilevary_step_flag_in_db(key, 1, RequestId, version);

										value = true;

										requestDao.editRequestforReportWebserviceInfo(
												requestinfo.getAlphanumericReqId(),
												Double.toString(requestinfo.getRequestVersion()), "deliever_config",
												"1", "In Progress");
										// requestInfoDao.editRequestForReportIOSWebserviceInfo(createConfigRequest.getRequestId(),Double.toString(createConfigRequest.getRequest_version()),"Boot
										// System Flash","Failure","Could not load image on top on boot commands.");
										// CODE for write and reload to be done!!!!!
										BackUp(requestinfo, user, password, "current");
										jsonArray = new Gson().toJson(value);
										obj.put(new String("output"), jsonArray);
									} else {
										value = false;
										key = "boot_system_flash_flag";
										requestInfoDao.update_dilevary_step_flag_in_db(key, 2, RequestId, version);
										key = "reload_flag";
										requestInfoDao.update_dilevary_step_flag_in_db(key, 2, RequestId, version);
										key = "post_login_flag";
										requestInfoDao.update_dilevary_step_flag_in_db(key, 2, RequestId, version);
										requestDao.editRequestforReportWebserviceInfo(
												requestinfo.getAlphanumericReqId(),
												Double.toString(requestinfo.getRequestVersion()), "deliever_config",
												"2", "Failure");
										requestInfoDao.editRequestForReportIOSWebserviceInfo(
												requestinfo.getAlphanumericReqId(),
												Double.toString(requestinfo.getRequestVersion()), "Boot System Flash",
												"Failure", "Could not load image on top on boot commands.");
										jsonArray = new Gson().toJson(value);
										BackUp(createConfigRequest, user, password, "current");
										obj.put(new String("output"), jsonArray);
									}

								} else {
									value = false;
									key = "os_download_flag";
									requestInfoDao.update_dilevary_step_flag_in_db(key, 2, RequestId, version);
									// could not copy image to csr
									// return error String s,output
									requestDao.editRequestforReportWebserviceInfo(requestinfo.getAlphanumericReqId(),
											Double.toString(requestinfo.getRequestVersion()), "deliever_config", "2",
											"Failure");
									requestInfoDao.editRequestForReportIOSWebserviceInfo(
											requestinfo.getAlphanumericReqId(),
											Double.toString(requestinfo.getRequestVersion()), "Copy FTP to CSR",
											"Failure", "Could not copy image from FTP to CSR.");
									jsonArray = new Gson().toJson(value);
									obj.put(new String("output"), jsonArray);
								}

							} else {
								value = false;
								isBackUpSccessful = false;
								key = "back_up_flag";
								requestInfoDao.update_dilevary_step_flag_in_db(key, 2, RequestId, version);
								// throw corresponding error from router on screen
								requestDao.editRequestforReportWebserviceInfo(requestinfo.getAlphanumericReqId(),
										Double.toString(requestinfo.getRequestVersion()), "deliever_config", "2",
										"Failure");
								requestInfoDao.editRequestForReportIOSWebserviceInfo(requestinfo.getAlphanumericReqId(),
										Double.toString(requestinfo.getRequestVersion()), "Back up", "Failure",
										"Back up unsuccessful.");
								jsonArray = new Gson().toJson(value);
								obj.put(new String("output"), jsonArray);
							}
						}
					} else {
						isFlashSizeAvailable = false;
						key = "flash_size_flag";
						requestInfoDao.update_dilevary_step_flag_in_db(key, 2, RequestId, version);
						// throw error
						value = false;
						requestDao.editRequestforReportWebserviceInfo(requestinfo.getAlphanumericReqId(),
								Double.toString(requestinfo.getRequestVersion()), "deliever_config", "2", "Failure");
						requestInfoDao.editRequestForReportIOSWebserviceInfo(requestinfo.getAlphanumericReqId(),
								Double.toString(requestinfo.getRequestVersion()), "Flash Size", "Failure",
								"No enough flash size available, flash could not be cleared");
						jsonArray = new Gson().toJson(value);
						obj.put(new String("output"), jsonArray);
					}

				} else if (json.get("requestType").toString().equalsIgnoreCase("SLGT")) {

					value = true;
					jsonArray = new Gson().toJson(value);
					// get previous milestone status as the status of request will not change in
					// this milestone
					String status = requestDao.getPreviousMileStoneStatus(requestinfo.getAlphanumericReqId(),
							requestinfo.getRequestVersion());
					String switchh = "0";
					if (status.equalsIgnoreCase("Partial Success")) {
						switchh = "3";
					} else if (status.equalsIgnoreCase("In Progress")) {
						switchh = "0";
					}
					requestDao.editRequestforReportWebserviceInfo(requestinfo.getAlphanumericReqId(),
							Double.toString(requestinfo.getRequestVersion()), "deliever_config", "0", status);
					obj.put(new String("output"), jsonArray);
					System.out.println("Out of dilever config");
				}

				else if (json.get("requestType").toString().equalsIgnoreCase("SLGB")) {

					ArrayList<String> commandToPush = new ArrayList<String>();
                    String tempRequestId = requestinfo.getAlphanumericReqId();
                    Double tempVersion = requestinfo.getRequestVersion();
					JSch jsch = new JSch();
					Channel channel = null;
					Session session = jsch.getSession(user, host,
							Integer.parseInt(port));
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
						System.out.println("Channel Connected to machine " + host
								+ " server");
						channel.connect();
						InputStream input = channel.getInputStream();

						// to save the backup and deliver the
						// configuration(configuration in the router)
						boolean isCheck = bckupConfigService.getRouterConfig(requestinfo,
								"previous");

					
								// db call for success deliver config
							
							
							if(isStartUp==true)
							{


								ArrayList<String> commandToPush1 = new ArrayList<String>();

								JSch jsch1 = new JSch();
								Channel channel1 = null;
								Session session1 = jsch1.getSession(user, host,
										Integer.parseInt(port));
								Properties config1 = new Properties();
								config1.put("StrictHostKeyChecking", "no");
								session1.setConfig(config1);
								session1.setPassword(password);
								session1.connect();
								try {
									Thread.sleep(10000);
								} catch (Exception ee) {
								}
								try {
									channel1 = session1.openChannel("shell");
									OutputStream ops1 = channel1.getOutputStream();

									PrintStream ps1 = new PrintStream(ops1, true);
									System.out.println("Channel Connected to machine " + host
											+ " server");
									channel1.connect();
									InputStream input1 = channel1.getInputStream();

									// to save the backup and deliver the
									// configuration(configuration in the router)
									boolean isCheck1 = bckupConfigService.getRouterConfigStartUp(requestinfo,
											"startup");

								
											// db call for success deliver config
										
									channel1.disconnect();
									session1.disconnect();	
											
										
									}
								catch (Exception ee) {
								}  
							}
								requestInfoDao.editRequestforReportWebserviceInfo(
										tempRequestId, Double
												.toString(tempVersion),
										"deliever_config", "1", "In Progress");
								
								
								requestInfoDao.updateRequestforReportWebserviceInfo(
										tempRequestId);
								
								
							if(isCheck)
							{
								value=true;
							} else
							{
								value=false;
							}

									channel.disconnect();
									session.disconnect();
									jsonArray = new Gson().toJson(value);
									obj.put(new String("output"), jsonArray);
								
							
						}
					catch (Exception ee) {
					}

						channel.disconnect();
						session.disconnect();
						jsonArray = new Gson().toJson(value);
						obj.put(new String("output"), jsonArray);
					} else if (json.get("requestType").toString().equalsIgnoreCase("SLGC")) {
					ArrayList<String> commandToPush = new ArrayList<String>();

					JSch jsch = new JSch();
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
						System.out.println("Channel Connected to machine " + host + " server");
						channel.connect();
						InputStream input = channel.getInputStream();

						// to save the backup and deliver the
						// configuration(configuration in the router)
						requestDao.getRouterConfig(requestinfo, "previous");

						session = jsch.getSession(user, host, Integer.parseInt(port));
						config = new Properties();
						config.put("StrictHostKeyChecking", "no");
						session.setConfig(config);
						session.setPassword(password);
						session.connect();

						channel = session.openChannel("shell");
						ops = channel.getOutputStream();

						ps = new PrintStream(ops, true);

						channel.connect();

						input = channel.getInputStream();

						Map<String, String> resultForFlag = new HashMap<String, String>();
						resultForFlag = requestInfoDao.getRequestFlag(requestinfo.getAlphanumericReqId(),
								requestinfo.getRequestVersion());
						String flagForPrevalidation = "";
						String flagFordelieverConfig = "";
						for (Map.Entry<String, String> entry : resultForFlag.entrySet()) {
							if (entry.getKey() == "flagForPrevalidation") {
								flagForPrevalidation = entry.getValue();

							}
							if (entry.getKey() == "flagFordelieverConfig") {
								flagFordelieverConfig = entry.getValue();
							}

						}

						// print the no config for child version
						if ((!(requestinfo.getRequestVersion() == requestinfo.getRequestParentVersion()))
								&& flagForPrevalidation.equalsIgnoreCase("1")
								&& flagFordelieverConfig.equalsIgnoreCase("1")) {

							commandToPush = readFileNoCmd(requestinfo.getAlphanumericReqId(),
									Double.toString(requestinfo.getRequestVersion()));
							if (!(commandToPush.get(0).contains("null"))) {
								ps.println("config t");
								for (String arr : commandToPush) {

									ps.println(arr);

									printResult(input, channel, requestinfo.getAlphanumericReqId(),
											Double.toString(requestinfo.getRequestVersion()));

								}
								ps.println("exit");
								try {
									Thread.sleep(4000);
								} catch (Exception ee) {
								}
							}
						}
						// then deliver or push the configuration
						// ps.println("config t");

						commandToPush = readFile(requestinfo.getAlphanumericReqId(),
								Double.toString(requestinfo.getRequestVersion()));

						if (!(commandToPush.get(0).contains("null"))) {
							ps.println("config t");
							for (String arr : commandToPush) {

								ps.println(arr);

								printResult(input, channel, requestinfo.getAlphanumericReqId(),
										Double.toString(requestinfo.getRequestVersion()));

							}
							printResult(input, channel, requestinfo.getAlphanumericReqId(),
									Double.toString(requestinfo.getRequestVersion()));

							String errorType = errorCodeValidationDeliveryTest.checkErrorCode(
									requestinfo.getAlphanumericReqId(), requestinfo.getRequestVersion());
							// get the router configuration after delivery

							requestinfo.setHostname(requestinfo.getHostname());
							requestinfo.setAlphanumericReqId(requestinfo.getAlphanumericReqId());
							// do error code validation
							if (errorType.equalsIgnoreCase("Warning") || errorType.equalsIgnoreCase("No Error")) {
								value = true;

								String response = invokeFtl.generateDileveryConfigFile(requestinfo);
								try {
									String responseDownloadPath = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
											.getProperty("responseDownloadPath");
									TextReport.writeFile(responseDownloadPath, requestinfo.getAlphanumericReqId() + "V"
											+ Double.toString(requestinfo.getRequestVersion()) + "_deliveredConfig.txt",
											response);

								} catch (IOException exe) {
									exe.printStackTrace();

								}

								requestDao.getRouterConfig(requestinfo, "current");
								// db call for success deliver config
								requestDao.editRequestforReportWebserviceInfo(requestinfo.getAlphanumericReqId(),
										Double.toString(requestinfo.getRequestVersion()), "deliever_config", "1",
										"In Progress");
								// network test
								// networkTestSSH.NetworkTest(configRequest);
							} else {
								value = false;
								/* Check because constant feature removed */
								// errorCodeValidationDeliveryTest
								// .pushNoCommandConfiguration(createConfigRequest);

								String response = invokeFtl.generateDileveryConfigFile(requestinfo);
								try {
									String responseDownloadPath = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
											.getProperty("responseDownloadPath");
									TextReport.writeFile(responseDownloadPath, requestinfo.getAlphanumericReqId() + "V"
											+ Double.toString(requestinfo.getRequestVersion()) + "_deliveredConfig.txt",
											response);

								} catch (IOException exe) {
									exe.printStackTrace();

								}

								requestDao.getRouterConfig(requestinfo, "current");

								// db call for failure in deliver config
								requestDao.editRequestforReportWebserviceInfo(requestinfo.getAlphanumericReqId(),
										Double.toString(requestinfo.getRequestVersion()), "deliever_config", "2",
										"Failure");

							}
						}

						// called if we have nothing to push and we directly change
						// the staus og delivery to success
						else {
							value = true;

							String response = invokeFtl.generateDileveryConfigFile(requestinfo);
							try {
								String responseDownloadPath = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
										.getProperty("responseDownloadPath");
								TextReport.writeFile(responseDownloadPath, requestinfo.getAlphanumericReqId() + "V"
										+ Double.toString(requestinfo.getRequestVersion()) + "_deliveredConfig.txt",
										response);

							} catch (IOException exe) {
								exe.printStackTrace();

							}

							requestDao.getRouterConfig(requestinfo, "current");
							// db call for success deliver config
							requestDao.editRequestforReportWebserviceInfo(requestinfo.getAlphanumericReqId(),
									Double.toString(requestinfo.getRequestVersion()), "deliever_config", "1",
									"In Progress");

						}

						channel.disconnect();
						session.disconnect();
						jsonArray = new Gson().toJson(value);
						obj.put(new String("output"), jsonArray);
					} catch (IOException ex) {
						jsonArray = new Gson().toJson(value);
						obj.put(new String("output"), jsonArray);
						requestDao.editRequestforReportWebserviceInfo(requestinfo.getAlphanumericReqId(),
								Double.toString(requestinfo.getRequestVersion()), "deliever_config", "2", "Failure");
						String response = invokeFtl.generateDeliveryConfigFileFailure(requestinfo);
						try {
							String responseDownloadPath = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
									.getProperty("responseDownloadPath");
							TextReport.writeFile(responseDownloadPath,
									requestinfo.getAlphanumericReqId() + "V"
											+ Double.toString(requestinfo.getRequestVersion()) + "_deliveredConfig.txt",
									response);

						} catch (IOException exe) {

						}
					}
					session.disconnect();

				}else if(json.get("requestType").toString().equalsIgnoreCase("SNRC"))
				{
					//for restconf
					
					//call method for backup from vnf utils
					ODLClient client=new ODLClient();
					boolean result=client.doGetODLBackUp(requestinfo.getAlphanumericReqId(), Double.toString(requestinfo.getRequestVersion()), "http://10.62.0.119:8181/restconf/config/network-topology:network-topology/topology/topology-netconf/node/CSR1000v/yang-ext:mount/ned:native", "previous");
					//boolean result=true;
					//call method for dilevary from vnf utils
					if(result==true)
					{
						//go for dilevary
						
						boolean dilevaryresult=false;
						
						//dilevaryresult=true;
						
						//Get XML to be pushed from local
						String responseDownloadPathRestConf = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
								.getProperty("VnfConfigCreationPath");
						String path=responseDownloadPathRestConf+"/"+requestinfo.getAlphanumericReqId()+"_ConfigurationToPush.xml";
						VNFHelper helper = new VNFHelper();
						String payload=helper.readConfigurationXML(path);
						
						System.out.println("log");
						//dilevaryresult=client.doPUTDilevary(createConfigRequest.getRequestId(), Double.toString(createConfigRequest.getRequest_version()),"http://10.62.0.119:8181/restconf/config/network-topology:network-topology/topology/topology-netconf/node/CSR1000v/yang-ext:mount/ned:native/interface ");
						
						 
						 String payloadLoopback=helper.getPayload("Loopback",payload);
						 
						 //dilevaryresult=client.doPUTDilevary(createConfigRequest.getRequestId(), Double.toString(createConfigRequest.getRequest_version()),"http://10.62.0.119:8181/restconf/config/network-topology:network-topology/topology/topology-netconf/node/CSR1000v/yang-ext:mount/ned:native/interface",payloadLoopback);
						 dilevaryresult=true;
						 if(dilevaryresult)
						 {
						 String payloadMultilink=helper.getPayload("Multilink",payload);
						dilevaryresult=client.doPUTDilevary(requestinfo.getAlphanumericReqId(), Double.toString(requestinfo.getRequestVersion()),"http://10.62.0.119:8181/restconf/config/network-topology:network-topology/topology/topology-netconf/node/CSR1000v/yang-ext:mount/ned:native/interface",payloadMultilink);
						 dilevaryresult=true;
						 if(dilevaryresult)
						 {
							 String payloadVT=helper.getPayload("Virtual-Template",payload);

							//dilevaryresult=client.doPUTDilevary(createConfigRequest.getRequestId(), Double.toString(createConfigRequest.getRequest_version()),"http://10.62.0.119:8181/restconf/config/network-topology:network-topology/topology/topology-netconf/node/CSR1000v/yang-ext:mount/ned:native/interface",payloadVT);
							 dilevaryresult=true;
							 if(dilevaryresult)
							 {
								 dilevaryresult=true;
							 }
							 else
							 {
								 dilevaryresult=false;
							 }
						 }
						 else
						 {
							 dilevaryresult=false;

							 //error handling
						 }
						 }
						 else
						 {
							 dilevaryresult=false;

							//error handling
						 }

							
							
						/////////////////Need to write code for put service for dilevary of config
						if (dilevaryresult==true)
						{
							//take current config back up
							boolean currentconfig=client.doGetODLBackUp(requestinfo.getAlphanumericReqId(), Double.toString(requestinfo.getRequestVersion()), "http://10.62.0.119:8181/restconf/config/network-topology:network-topology/topology/topology-netconf/node/CSR1000v/yang-ext:mount/ned:native", "current");
							//boolean currentconfig=true;
							if(currentconfig== true)
							{
								requestDao.editRequestforReportWebserviceInfo(requestinfo.getAlphanumericReqId(),
										Double.toString(requestinfo.getRequestVersion()), "deliever_config", "1",
										"In Progress");
								value = true;
								jsonArray = new Gson().toJson(value);
								obj.put(new String("output"), jsonArray);

								String response = invokeFtl
										.generateDileveryConfigFile(requestinfo);
								try {
									String responseDownloadPath = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
											.getProperty("responseDownloadPath");
									TextReport
											.writeFile(
													responseDownloadPath,
													requestinfo.getAlphanumericReqId()
															+ "V"
															+ Double.toString(requestinfo.getRequestVersion())
															+ "_deliveredConfig.txt",
													response);

								} catch (IOException exe) {
									exe.printStackTrace();

								}
							}
							else
							{
								value=false;
								requestDao.editRequestforReportWebserviceInfo(requestinfo.getAlphanumericReqId(),
										Double.toString(requestinfo.getRequestVersion()), "deliever_config", "2",
										"Failure");
								jsonArray = new Gson().toJson(value);
								obj.put(new String("output"), jsonArray);
								String response="";
								String responseDownloadPath="";
								try {
									requestDao.editRequestforReportWebserviceInfo(requestinfo.getAlphanumericReqId(),
											Double.toString(requestinfo.getRequestVersion()), "deliever_config", "2",
											"Failure");									
									response = invokeFtl.generateDeliveryConfigFileFailure(requestinfo);
									responseDownloadPath = BackupCurrentRouterConfigurationService.TSA_PROPERTIES
											.getProperty("responseDownloadPath");
									TextReport.writeFile(responseDownloadPath, createConfigRequest.getRequestId()
											+"V"+Double.toString(requestinfo.getRequestVersion())+"_deliveredConfig.txt", response);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						else
						{
							value=false;
							jsonArray = new Gson().toJson(value);
							obj.put(new String("output"), jsonArray);
							String response="";
							String responseDownloadPath="";
							try {
								requestDao.editRequestforReportWebserviceInfo(requestinfo.getAlphanumericReqId(),
										Double.toString(requestinfo.getRequestVersion()), "deliever_config", "2",
										"Failure");								
								response = invokeFtl.generateDeliveryConfigFileFailure(requestinfo);
								responseDownloadPath = BackupCurrentRouterConfigurationService.TSA_PROPERTIES
										.getProperty("responseDownloadPath");
								TextReport.writeFile(responseDownloadPath, requestinfo.getAlphanumericReqId()
										+"V"+Double.toString(requestinfo.getRequestVersion())+"_deliveredConfig.txt", response);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					else
					{
						value=false;
						String response="";
						String responseDownloadPath="";
						try {
							requestDao.editRequestforReportWebserviceInfo(requestinfo.getAlphanumericReqId(),
									Double.toString(requestinfo.getRequestVersion()), "deliever_config", "2",
									"Failure");							response = invokeFtl.generateDeliveryConfigFileFailure(createConfigRequest);
							responseDownloadPath = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
									.getProperty("responseDownloadPath");
							TextReport.writeFile(responseDownloadPath,requestinfo.getAlphanumericReqId()
									+"V"+Double.toString(requestinfo.getRequestVersion())+"_deliveredConfig.txt", response);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					//call method for back up from vnf utils for current configuration
				
					
					
				
				}else if (json.get("requestType").toString().equalsIgnoreCase("SNNC")) {
					
					// push configuration for Netconf devices String
					String requestId = requestinfo.getAlphanumericReqId();
					String responseDownloadPathNetconf = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
							.getProperty("VnfConfigCreationPath");
					String path = responseDownloadPathNetconf + "/" + requestId
							+ "_ConfigurationToPush.xml";
					VNFHelper helper = new VNFHelper();
					String payload = helper.readConfigurationXML(path);
					// get file from vnf config requests folder
					// pass file path to vnf helper class push on device method.
					boolean result = helper.pushOnVnfDevice(path);
					if (result) {
						value = true;

						String response = invokeFtl
								.generateDileveryConfigFile(requestinfo);
						try {
							String responseDownloadPath = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
									.getProperty("responseDownloadPath");
							TextReport
									.writeFile(
											responseDownloadPath,
											requestinfo.getAlphanumericReqId()
													+ "V"
													+ Double.toString(requestinfo.getRequestVersion())
													+ "_deliveredConfig.txt",
											response);

						} catch (IOException exe) {
							exe.printStackTrace();

						}
					} else {
						value = false;
						jsonArray = new Gson().toJson(value);
						obj.put(new String("output"), jsonArray);
						String response = "";
						String responseDownloadPath = "";
						try {
							requestInfoDao.editRequestforReportWebserviceInfo(
									requestinfo.getAlphanumericReqId(), Double
											.toString(requestinfo.getRequestVersion()),
									"deliever_config", "2", "Failure");
							response = invokeFtl
									.generateDeliveryConfigFileFailure(requestinfo);
							responseDownloadPath = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
									.getProperty("responseDownloadPath");
							TextReport
									.writeFile(
											responseDownloadPath,
											requestinfo.getAlphanumericReqId()
													+ "V"
													+ Double.toString(requestinfo.getRequestVersion())
													+ "_deliveredConfig.txt",
											response);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							// e.printStackTrace();
						}
					}
				}

			}
		}
		// when reachability fails
		catch (Exception ex) {
			if (createConfigRequest.getManagementIp() != null && !createConfigRequest.getManagementIp().equals("")) {
				jsonArray = new Gson().toJson(value);
				obj.put(new String("output"), jsonArray);

				requestInfoDao.editRequestforReportWebserviceInfo(createConfigRequest.getRequestId(),
						Double.toString(createConfigRequest.getRequest_version()), "deliever_config", "2", "Failure");
				String response;
				try {
					response = invokeFtl.generateDeliveryConfigFileFailure(createConfigRequest);
					String responseDownloadPath = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
							.getProperty("responseDownloadPath");
					TextReport.writeFile(responseDownloadPath, createConfigRequest.getRequestId() + "V"
							+ Double.toString(createConfigRequest.getRequest_version()) + "_deliveredConfig.txt",
							response);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (requestinfo.getManagementIp() != null && !requestinfo.getManagementIp().equals("")) {

				jsonArray = new Gson().toJson(value);
				obj.put(new String("output"), jsonArray);

				requestDao.editRequestforReportWebserviceInfo(requestinfo.getAlphanumericReqId(),
						Double.toString(requestinfo.getRequestVersion()), "deliever_config", "2", "Failure");
				String response;
				try {
					response = invokeFtl.generateDeliveryConfigFileFailure(requestinfo);
					String responseDownloadPath = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
							.getProperty("responseDownloadPath");
					TextReport.writeFile(
							responseDownloadPath, requestinfo.getAlphanumericReqId() + "V"
									+ Double.toString(requestinfo.getRequestVersion()) + "_deliveredConfig.txt",
							response);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		/*
		 * return Response .status(200) .header("Access-Control-Allow-Origin", "*")
		 * .header("Access-Control-Allow-Headers",
		 * "origin, content-type, accept, authorization")
		 * .header("Access-Control-Allow-Credentials", "true")
		 * .header("Access-Control-Allow-Methods",
		 * "GET, POST, PUT, DELETE, OPTIONS, HEAD") .header("Access-Control-Max-Age",
		 * "1209600").entity(obj) .build();
		 */

		return obj;

	}

	/* method overloading for UIRevamp */
	private boolean BackUp(RequestInfoPojo requestinfo, String user, String password, String stage)
			throws NumberFormatException, JSchException {

		System.out.println("Inside Backup method for ios upgrade..");
		boolean isSuccess = false;
		String port = DeliverConfigurationAndBackupTest.TSA_PROPERTIES.getProperty("portSSH");
		String host = requestinfo.getManagementIp();
		JSch jsch = new JSch();
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
			System.out.println("Channel Connected to machine " + host + " server for backup");
			channel.connect();

			// to save the backup and deliver the configuration(configuration in the router)
			isSuccess = requestDao.getRouterConfig(requestinfo, stage);
		} catch (Exception e) {

		}
		return isSuccess;
	}

	public static boolean loadProperties() throws IOException {
		InputStream tsaPropFile = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(TSA_PROPERTIES_FILE);

		try {
			TSA_PROPERTIES.load(tsaPropFile);
		} catch (IOException exc) {
			exc.printStackTrace();
			return false;
		}
		return false;
	}

	@SuppressWarnings("resource")
	public ArrayList<String> readFileNoCmd(String requestIdForConfig, String version) throws IOException {
		BufferedReader br = null;
		LineNumberReader rdr = null;
		/* StringBuilder sb2=null; */
		String responseDownloadPath = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
				.getProperty("responseDownloadPath");
		String filePath = responseDownloadPath + "//" + requestIdForConfig + "V" + version + "_ConfigurationNoCmd";

		br = new BufferedReader(new FileReader(filePath));
		// File f = new File(filePath);
		try {
			ArrayList<String> ar = new ArrayList<String>();
			// if(f.exists()){

			StringBuffer send = null;
			StringBuilder sb2 = new StringBuilder();

			rdr = new LineNumberReader(new FileReader(filePath));
			InputStream is = new BufferedInputStream(new FileInputStream(filePath));

			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			boolean empty = true;
			while ((readChars = is.read(c)) != -1) {
				empty = false;
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
			}
			int fileReadSize = Integer
					.parseInt(DeliverConfigurationAndBackupTest.TSA_PROPERTIES.getProperty("fileChunkSize"));
			int chunks = (count / fileReadSize) + 1;
			String line;

			for (int loop = 1; loop <= chunks; loop++) {
				if (loop == 1) {
					rdr = new LineNumberReader(new FileReader(filePath));
					line = rdr.readLine();
					sb2.append(line).append("\n");
					for (line = null; (line = rdr.readLine()) != null;) {

						if (rdr.getLineNumber() <= fileReadSize) {
							sb2.append(line).append("\n");
						}

					}
					ar.add(sb2.toString());
				} else {
					LineNumberReader rdr1 = new LineNumberReader(new FileReader(filePath));
					sb2 = new StringBuilder();
					for (line = null; (line = rdr1.readLine()) != null;) {

						if (rdr1.getLineNumber() > (fileReadSize * (loop - 1))
								&& rdr1.getLineNumber() <= (fileReadSize * loop)) {
							sb2.append(line).append("\n");
						}

					}
					ar.add(sb2.toString());
				}

			}

			// }
			return ar;
		} finally {
			br.close();
		}
	}

	public void printResult(InputStream input, Channel channel, String requestId, String version) throws Exception {
		BufferedWriter bw = null;
		FileWriter fw = null;
		int SIZE = 1024;
		byte[] tmp = new byte[SIZE];
		String responselogpath = DeliverConfigurationAndBackupTest.TSA_PROPERTIES.getProperty("responselogpath");
		File file = new File(responselogpath + "/" + requestId + "_" + version + "theSSHfile.txt");
		/*
		 * if (file.exists()) { file.delete(); }
		 */
		while (input.available() > 0) {
			int i = input.read(tmp, 0, SIZE);
			if (i < 0)
				break;

			String s = new String(tmp, 0, i);
			if (!(s.equals(""))) {

				file = new File(responselogpath + "/" + requestId + "_" + version + "theSSHfile.txt");

				if (!file.exists()) {
					file.createNewFile();

					fw = new FileWriter(file, true);
					bw = new BufferedWriter(fw);
					bw.append(s);
					bw.close();
				} else {
					fw = new FileWriter(file.getAbsoluteFile(), true);
					bw = new BufferedWriter(fw);
					bw.append(s);
					bw.close();
				}
			}

		}
		if (channel.isClosed()) {
			System.out.println("exit-status: " + channel.getExitStatus());

		}
		try {
			Thread.sleep(1000);
		} catch (Exception ee) {
		}

	}

	@SuppressWarnings("resource")
	public ArrayList<String> readFile(String requestIdForConfig, String version) throws IOException {
		BufferedReader br = null;
		LineNumberReader rdr = null;
		/* StringBuilder sb2=null; */
		String responseDownloadPath = DeliverConfigurationAndBackupTest.TSA_PROPERTIES
				.getProperty("responseDownloadPath");
		String filePath = responseDownloadPath + "//" + requestIdForConfig + "V" + version + "_Configuration";

		br = new BufferedReader(new FileReader(filePath));
		try {
			ArrayList<String> ar = new ArrayList<String>();
			// StringBuffer send = null;
			StringBuilder sb2 = new StringBuilder();

			rdr = new LineNumberReader(new FileReader(filePath));
			InputStream is = new BufferedInputStream(new FileInputStream(filePath));

			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			boolean empty = true;
			while ((readChars = is.read(c)) != -1) {
				empty = false;
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
			}
			int fileReadSize = Integer
					.parseInt(DeliverConfigurationAndBackupTest.TSA_PROPERTIES.getProperty("fileChunkSize"));
			int chunks = (count / fileReadSize) + 1;
			String line;

			for (int loop = 1; loop <= chunks; loop++) {
				if (loop == 1) {
					rdr = new LineNumberReader(new FileReader(filePath));
					line = rdr.readLine();
					sb2.append(line).append("\n");
					for (line = null; (line = rdr.readLine()) != null;) {

						if (rdr.getLineNumber() <= fileReadSize) {
							sb2.append(line).append("\n");
						}

					}
					ar.add(sb2.toString());
				} else {
					LineNumberReader rdr1 = new LineNumberReader(new FileReader(filePath));
					sb2 = new StringBuilder();
					for (line = null; (line = rdr1.readLine()) != null;) {

						if (rdr1.getLineNumber() > (fileReadSize * (loop - 1))
								&& rdr1.getLineNumber() <= (fileReadSize * loop)) {
							sb2.append(line).append("\n");
						}

					}
					ar.add(sb2.toString());
				}

			}
			return ar;
		} finally {
			br.close();
		}
	}

	private boolean BackUp(CreateConfigRequest createConfigRequest, String user, String password, String stage)
			throws NumberFormatException, JSchException {
		System.out.println("Inside Backup method for ios upgrade");
		BackupCurrentRouterConfigurationService bckupConfigService = new BackupCurrentRouterConfigurationService();
		boolean isSuccess = false;
		String port = DeliverConfigurationAndBackupTest.TSA_PROPERTIES.getProperty("portSSH");
		ArrayList<String> commandToPush = new ArrayList<String>();
		String host = createConfigRequest.getManagementIp();
		JSch jsch = new JSch();
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
			System.out.println("Channel Connected to machine " + host + " server for backup");
			channel.connect();
			InputStream input = channel.getInputStream();

			// to save the backup and deliver the configuration(configuration in the router)
			isSuccess = bckupConfigService.getRouterConfig(createConfigRequest, stage);
		} catch (Exception e) {

		}
		return isSuccess;
	}

	List<String> getExsistingBootCmds(String user, String password, String host) {
		List<String> array = new ArrayList<String>();
		List<String> array1 = new ArrayList<String>();

		String port = DeliverConfigurationAndBackupTest.TSA_PROPERTIES.getProperty("portSSH");
		JSch jsch = new JSch();
		Channel channel = null;
		Session session = null;
		try {
			session = jsch.getSession(user, host, Integer.parseInt(port));

			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.setPassword(password);
			session.connect();
			try {
				Thread.sleep(10000);
			} catch (Exception ee) {
			}
			channel = session.openChannel("shell");
			OutputStream ops = channel.getOutputStream();
			PrintStream ps = new PrintStream(ops, true);
			System.out.println("Channel Connected to machine " + host + " show run to copy boot cmds");
			channel.connect();
			InputStream input = channel.getInputStream();
			ps.println("show run | i boot");
			try {
				// change this sleep in case of longer wait
				Thread.sleep(1000);
			} catch (Exception ee) {
			}
			BufferedWriter bw = null;
			FileWriter fw = null;
			int SIZE = 1024;
			byte[] tmp = new byte[SIZE];
			while (input.available() > 0) {
				int i = input.read(tmp, 0, SIZE);
				if (i < 0)
					break;
				// we will get response from router here
				String s = new String(tmp, 0, i);
				s = s.replaceAll("\r", "");
				List<String> outList = new ArrayList<String>();
				String str[] = s.split("\n");
				outList = Arrays.asList(str);
				for (int j = 1; j < outList.size() - 2; j++) {
					if (!outList.get(j).isEmpty())
						array.add(outList.get(j));

				}
				for (int k = 0; k < array.size(); k++) {
					if (array.get(k).contains("show")) {

					} else {
						array1.add(array.get(k));
					}
				}

			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		channel.disconnect();
		session.disconnect();
		return array1;
	}

	boolean pushOnRouter(String user, String password, String host, List<String> cmdToPush) {
		boolean isSuccess = false;
		String port = DeliverConfigurationAndBackupTest.TSA_PROPERTIES.getProperty("portSSH");
		JSch jsch = new JSch();
		Channel channel = null;
		Session session = null;
		try {
			session = jsch.getSession(user, host, Integer.parseInt(port));

			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.setPassword(password);
			session.connect();
			try {
				Thread.sleep(10000);
			} catch (Exception ee) {
			}
			channel = session.openChannel("shell");
			OutputStream ops = channel.getOutputStream();
			PrintStream ps = new PrintStream(ops, true);
			System.out.println("Channel Connected to machine " + host + " to pust boot System flash cmd");
			channel.connect();
			InputStream input = channel.getInputStream();
			for (String arr : cmdToPush) {
				System.out.println("commands to push " + arr);
				ps.println(arr);

				// printResult(input,
				// channel,createConfigRequest.getRequestId(),Double.toString(createConfigRequest.getRequest_version()));

			}
			ps.println("exit");

			System.out.println("Done pushing flash commands on" + host);
			isSuccess = checkIdLoadedProperly(user, password, host);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isSuccess;
	}

	boolean checkIdLoadedProperly(String user, String password, String host) {
		boolean isRes = false;
		String port = DeliverConfigurationAndBackupTest.TSA_PROPERTIES.getProperty("portSSH");
		JSch jsch = new JSch();
		Channel channel = null;
		Session session = null;
		try {
			session = jsch.getSession(user, host, Integer.parseInt(port));

			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.setPassword(password);
			session.connect();
			try {
				Thread.sleep(10000);
			} catch (Exception ee) {
			}
			channel = session.openChannel("shell");
			OutputStream ops = channel.getOutputStream();
			PrintStream ps = new PrintStream(ops, true);

			channel.connect();
			System.out
					.println("Channel Connected to machine " + host + " for show run | i boot after pushing new file");
			InputStream input = channel.getInputStream();
			ps.println("show run | i boot");
			try {
				Thread.sleep(10000);
			} catch (Exception ee) {
			}
			BufferedWriter bw = null;
			FileWriter fw = null;
			int SIZE = 1024;
			byte[] tmp = new byte[SIZE];
			while (input.available() > 0) {
				int i = input.read(tmp, 0, SIZE);
				if (i < 0)
					break;
				// we will get response from router here
				String s = new String(tmp, 0, i);
				System.out.println("router output: " + s);
				List<String> outList = new ArrayList<String>();
				String str[] = s.split("\n");
				outList = Arrays.asList(str);
				isRes = true;
			}
			System.out.println("Input size < 0: ");
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		channel.disconnect();
		session.disconnect();
		return isRes;
	}
}
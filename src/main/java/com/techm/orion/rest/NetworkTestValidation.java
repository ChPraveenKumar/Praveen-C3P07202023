package com.techm.orion.rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import javax.ws.rs.POST;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.techm.orion.dao.RequestInfoDao;
import com.techm.orion.dao.RequestInfoDetailsDao;
import com.techm.orion.entitybeans.CredentialManagementEntity;
import com.techm.orion.entitybeans.DeviceDiscoveryEntity;
import com.techm.orion.entitybeans.TestDetail;
import com.techm.orion.pojo.CreateConfigRequest;
import com.techm.orion.pojo.RequestInfoPojo;
import com.techm.orion.repositories.DeviceDiscoveryRepository;
import com.techm.orion.service.DcmConfigService;
import com.techm.orion.utility.InvokeFtl;
import com.techm.orion.utility.ODLClient;
import com.techm.orion.utility.TSALabels;
import com.techm.orion.utility.TestStrategeyAnalyser;
import com.techm.orion.utility.TextReport;
import com.techm.orion.utility.VNFHelper;

@Controller
@RequestMapping("/NetworkTestValidation")
public class NetworkTestValidation extends Thread {
	private static final Logger logger = LogManager.getLogger(NetworkTestValidation.class);
	@Autowired
	private RequestInfoDao requestInfoDao;
	@Autowired
	private RequestInfoDetailsDao requestInfoDetailsDao;
	@Autowired
	private TestStrategeyAnalyser testStrategeyAnalyser;
	@Autowired
	private DeviceDiscoveryRepository deviceDiscoveryRepository;	
	@Autowired
	private DcmConfigService dcmConfigService;
	
	/**
	 *This Api is marked as ***************c3p-ui Api Impacted****************
	 **/
	@SuppressWarnings("unchecked")
	@POST
	@RequestMapping(value = "/networkCommandTest", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
	public JSONObject networkCommandTest(@RequestBody String request) throws ParseException {

		JSONObject obj = new JSONObject();
		String jsonArray = "";
		InvokeFtl invokeFtl = new InvokeFtl();
		RequestInfoPojo requestinfo = new RequestInfoPojo();
		Boolean value = false;

		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(request);

		String RequestId = json.get("requestId").toString();
		String version = json.get("version").toString();

		String type = RequestId.substring(0, Math.min(RequestId.length(), 4));
		JSch jsch = new JSch();
		Channel channel = null;
		Session session = null;
	    if (!((type.equals("SLGB") || (type.equals("SNAI") || (type.equals("SNAD")))))){

			try {				
				logger.info("Request ID in network test validation" + RequestId);
				requestinfo = requestInfoDetailsDao.getRequestDetailTRequestInfoDBForVersion(RequestId, version);
				if (requestinfo.getManagementIp() != null && !requestinfo.getManagementIp().equals("")) {
					DeviceDiscoveryEntity deviceDetails = deviceDiscoveryRepository
							.findByDHostNameAndDMgmtIpAndDDeComm(requestinfo.getHostname(),requestinfo.getManagementIp(),"0");
					String statusVAlue = requestInfoDetailsDao.getPreviousMileStoneStatus(requestinfo.getAlphanumericReqId(),
							requestinfo.getRequestVersion());
					requestInfoDao.editRequestforReportWebserviceInfo(requestinfo.getAlphanumericReqId(),
							Double.toString(requestinfo.getRequestVersion()), "network_test", "4", statusVAlue);

					requestinfo.setAlphanumericReqId(RequestId);
					requestinfo.setRequestVersion(Double.parseDouble(json.get("version").toString()));

					if (type.equalsIgnoreCase("SLGC") || type.equalsIgnoreCase("SLGT") || type.equalsIgnoreCase("SNRC")
							|| type.equalsIgnoreCase("SNNC") || type.equalsIgnoreCase("SLGA")
							|| type.equalsIgnoreCase("SLGM") || type.equalsIgnoreCase("SNRM")
							|| type.equalsIgnoreCase("SNNM")) {
						String host = requestinfo.getManagementIp();
						CredentialManagementEntity routerCredential = dcmConfigService.getRouterCredential(
								deviceDetails);
						String user = routerCredential.getLoginRead();
						String password = routerCredential.getPasswordWrite();
					
						session = jsch.getSession(user, host, Integer.parseInt(TSALabels.PORT_SSH.getValue()));
						Properties config = new Properties();
						config.put("StrictHostKeyChecking", "no");
						session.setConfig(config);
						session.setPassword(password);
						logger.info("Before session.connet in network test validation Username" + user
								+ " Password " + password + " host" + host);
						session.connect();
						try {
							Thread.sleep(5000);
						} catch (Exception ee) {
						}
						try {
							
							channel = session.openChannel("shell");
							OutputStream ops = channel.getOutputStream();

							PrintStream ps = new PrintStream(ops, true);
							logger.info("Channel Connected to machine " + host + " server");
							channel.connect();
							InputStream input = channel.getInputStream();							
							List<Boolean> results = null;
							
							List<TestDetail> listOfTests = new ArrayList<TestDetail>();
							List<TestDetail> finallistOfTests = new ArrayList<TestDetail>();
							listOfTests = requestInfoDao.findTestFromTestStrategyDB(
									requestinfo.getFamily(), requestinfo.getOs(), requestinfo.getOsVersion(),
									requestinfo.getVendor(), requestinfo.getRegion(), "Network Test");
							List<TestDetail> selectedTests = requestInfoDao.findSelectedTests(requestinfo.getAlphanumericReqId(),
									"Network Test",version);
							if (selectedTests.size() > 0) {
								for (int i = 0; i < listOfTests.size(); i++) {
									for (int j = 0; j < selectedTests.size(); j++) {
										if (selectedTests.get(j).getTestName()
												.equalsIgnoreCase(listOfTests.get(i).getTestName())) {
											finallistOfTests.add(listOfTests.get(i));

										}

									}
								}

								if (finallistOfTests.size() > 0) {
									results = new ArrayList<Boolean>();
									for (int i = 0; i < finallistOfTests.size(); i++) {
									


										// conduct and analyse the tests
										
										if(deviceDetails.getdVNFSupport().equalsIgnoreCase("VNF"))
										{
											VNFHelper helper=new VNFHelper();
											helper.performTest(finallistOfTests.get(i),requestinfo, user, password);
										}
										else if(deviceDetails.getdConnect().equalsIgnoreCase("RESTCONF"))
										{
											ODLClient client=new ODLClient();
											client.performTest(finallistOfTests.get(i),requestinfo, user, password);
										}
										else
										{
											ps = requestInfoDetailsDao.setCommandStream(ps,requestinfo,"Test",false);
											ps.println(finallistOfTests.get(i).getTestCommand());
											try {
												Thread.sleep(6000);
											} catch (Exception ee) {
											}

											// printResult(input,
											// channel,configRequest.getRequestId(),Double.toString(configRequest.getRequest_version()));
											Boolean res = testStrategeyAnalyser.printAndAnalyse(input, channel,
													requestinfo.getAlphanumericReqId(),
													Double.toString(requestinfo.getRequestVersion()),
													finallistOfTests.get(i), "Network Test");
											results.add(res);
										}
									
									
									}
								}

							} else {
								// No new network test added
							}

							/*
							 * END
							 */

							logger.info("Certification bits in network test "
									+ requestinfo.getCertificationSelectionBit().substring(0, 1).equals("1")
									+ requestinfo.getCertificationSelectionBit().substring(1, 2)
									+ requestinfo.getCertificationSelectionBit().substring(2, 3)
									+ requestinfo.getCertificationSelectionBit().substring(3, 4));
							// working on simulator so condition has been set to true
						
								boolean result = true;
								if (listOfTests.size() > 0) {
									if (results != null && !results.isEmpty()) {
										for (int i = 0; i < results.size(); i++) {
											if (results.get(i) == false) {
												result = false;
											}
										}
									}
								
								}
								logger.info("result -"+result);
								if (requestinfo.getCertificationSelectionBit().substring(0, 1).equals("1")
										|| requestinfo.getCertificationSelectionBit().substring(1, 2).equals("1")
										|| requestinfo.getCertificationSelectionBit().substring(2, 3).equals("1")
										|| requestinfo.getCertificationSelectionBit().substring(3, 4)
												.equals("1"))
								{
								requestInfoDao.updateNetworkTestStatus(requestinfo.getAlphanumericReqId(),
										Double.toString(requestinfo.getRequestVersion()),
										Integer.parseInt(requestinfo.getCertificationSelectionBit().substring(0, 1)),
										Integer.parseInt(requestinfo.getCertificationSelectionBit().substring(1, 2)),
										Integer.parseInt(requestinfo.getCertificationSelectionBit().substring(2, 3)),
										Integer.parseInt(requestinfo.getCertificationSelectionBit().substring(3, 4)));
								}
								String status = requestInfoDetailsDao.getPreviousMileStoneStatus(
										requestinfo.getAlphanumericReqId(), requestinfo.getRequestVersion());
								int statusData = requestInfoDetailsDao.getStatusForMilestone(requestinfo.getAlphanumericReqId(),
										Double.toString(requestinfo.getRequestVersion()), "network_test");
								if (statusData != 3) {
									requestInfoDao.editRequestforReportWebserviceInfo(
											requestinfo.getAlphanumericReqId(),
											Double.toString(requestinfo.getRequestVersion()), "network_test", "1",
											status);
								}

							
						
							
							//value = true;// hardcoded for default tests

							// this is to evaluate according to newly added tests else it is true by
							// default.
							if (results != null) {
								for (int i = 0; i < results.size(); i++) {
									if (!results.get(i)) {
										value = false;
										break;
									}

								}
							}
							else
							{
								value = true;
							}
							channel.disconnect();
							session.disconnect();
							logger.info("DONE");
							jsonArray = new Gson().toJson(value);
							try {
								Thread.sleep(15000);
							} catch (Exception ee) {
							}
							obj.put(new String("output"), jsonArray);
						} catch (IOException ex) {
							jsonArray = new Gson().toJson(value);
							obj.put(new String("output"), jsonArray);
							requestInfoDetailsDao.editRequestforReportWebserviceInfo(requestinfo.getAlphanumericReqId(),
									Double.toString(requestinfo.getRequestVersion()), "network_test", "2", "Failure");

							String response = "";
							try {
								response = invokeFtl.generateNetworkTestResultFileFailure(requestinfo);
								TextReport.writeFile(TSALabels.RESPONSE_DOWNLOAD_PATH.getValue(),
										requestinfo.getAlphanumericReqId() + "V"
												+ Double.toString(requestinfo.getRequestVersion()) + "_networkTest.txt",
										response);
							} catch (Exception e) {
								// TODO Auto-generated catch block

							}

						}
						channel.disconnect();
						session.disconnect();
					}

					else {
						value = true;
						logger.info("DONE Network Test");
						jsonArray = new Gson().toJson(value);
						obj.put(new String("output"), jsonArray);
					}
				}
			}
			// when reachability fails
			catch (Exception ex) {
				if (requestinfo.getManagementIp() != null && !requestinfo.getManagementIp().equals("")) {

					logger.info("Exception in network tst" + ex.getMessage());
					jsonArray = new Gson().toJson(value);
					obj.put(new String("output"), jsonArray);
					requestInfoDetailsDao.editRequestforReportWebserviceInfo(requestinfo.getAlphanumericReqId(),
							Double.toString(requestinfo.getRequestVersion()), "network_test", "2", "Failure");
					String response = "";
					try {
						response = invokeFtl.generateNetworkTestResultFileFailure(requestinfo);
						TextReport.writeFile(TSALabels.RESPONSE_DOWNLOAD_PATH.getValue(),
								requestinfo.getAlphanumericReqId() + "V"
										+ Double.toString(requestinfo.getRequestVersion()) + "_networkTest.txt",
								response);
					} catch (Exception e) {
						// TODO Auto-generated catch block

					}

				}
			}
			finally {

				if (channel != null) {
					try {
					session = channel.getSession();
					
					if (channel.getExitStatus() == -1) {
						
							Thread.sleep(5000);
						
					}
					} catch (Exception e) {
						System.out.println(e);
					}
					channel.disconnect();
					session.disconnect();
				
				}
			}
			
		} else {
			value = true;

			jsonArray = new Gson().toJson(value);
			obj.put(new String("output"), jsonArray);

		}

		return obj;

	}	

	@SuppressWarnings("resource")
	public String validateNetworkTest(CreateConfigRequest configRequest) throws Exception {
		logger.info("In side validate network test line no 356");
		String content = "";
		String path = TSALabels.RESPONSE_DOWNLOAD_PATH.getValue()
				+ configRequest.getRequestId() + "V" + configRequest.getRequest_version() + "_networkTest.txt";

		File file = new File(path);
		Scanner in = null;
		try {
			if (file.exists()) {
				in = new Scanner(file);
				while (in.hasNext()) {
					String line = in.nextLine();

					String interfacename = configRequest.getC3p_interface().getName();
					if (interfacename == null) {
						configRequest.getC3p_interface().setName("");

					}
					if (line.contains(configRequest.getC3p_interface().getName())) {
						logger.info(line);
						content = line;
						break;
					}

				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}
	

	/* method overloading for UIRevamp */
	public String validateNetworkTest(RequestInfoPojo requestinfo) throws IOException {
		logger.info("In side validate network test line no 356");
		String content = "";
		String path = TSALabels.RESPONSE_DOWNLOAD_PATH.getValue()
				+ requestinfo.getAlphanumericReqId() + "V" + requestinfo.getRequestVersion() + "_networkTest.txt";

		File file = new File(path);
		Scanner in = null;
		try {
			if (file.exists()) {
				in = new Scanner(file);
				while (in.hasNext()) {
					String line = in.nextLine();

					// String interfacename = requestinfo.getC3p_interface()
					// .getName();
					// if (interfacename == null) {
					// requestinfo.getC3p_interface().setName("");
					//
					// }
					// if (line.contains(requestinfo.getC3p_interface()
					// .getName())) {
					// logger.info(line);
					// content = line;
					// break;
					// }

				}
			}
		} catch (FileNotFoundException e) {
			logger.error("Exception in validateNetworkTest method "+e.getMessage());
			e.printStackTrace();
		}
		return content;
	}
}
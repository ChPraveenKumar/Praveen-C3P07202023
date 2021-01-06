package com.techm.orion.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class PingTest {
	@Autowired
	RestTemplate restTemplate;

	private static final Logger logger = LogManager.getLogger(PingTest.class);
	public static String PROPERTIES_FILE = "TSA.properties";
	public static final Properties PROPERTIES = new Properties();

	public boolean cmdPingCall(String managementIp, String routername,
			String region) throws Exception {
		/*
		 * ProcessBuilder builder = new ProcessBuilder("cmd.exe"); Process p =
		 * null; boolean flag = true; try { p = builder.start(); BufferedWriter
		 * p_stdin = new BufferedWriter(new
		 * OutputStreamWriter(p.getOutputStream())); String commandToPing =
		 * "ping " + managementIp + " -n 20"; logger.info("Management ip " +
		 * managementIp); p_stdin.write(commandToPing); p_stdin.newLine();
		 * p_stdin.flush(); try { Thread.sleep(21000); } catch (Exception ee) {
		 * } p_stdin.write("exit"); p_stdin.newLine(); p_stdin.flush(); }
		 * 
		 * catch (IOException e) { e.printStackTrace(); }
		 */

		StringBuilder commadBuilder = new StringBuilder();
		Process process = null;
		boolean flag = true;
		try {
			commadBuilder.append("ping ");
			commadBuilder.append(managementIp);
			// Pings timeout
			if ("Linux".equals(TSALabels.APP_OS.getValue())) {
				commadBuilder.append(" -c ");
			} else {
				commadBuilder.append(" -n ");
			}
			// Number of pings
			commadBuilder.append("5");
			logger.info("commandToPing -" + commadBuilder);
			process = Runtime.getRuntime().exec(commadBuilder.toString());
		} catch (IOException exe) {
			logger.error("Exception in pingResults - " + exe.getMessage());
		}

		// Scanner s = new Scanner( p.getInputStream() );

		InputStream input = process.getInputStream();
		flag = printResult(input, routername, region);

		return flag;
	}

	private boolean printResult(InputStream input, String routername,
			String region) throws Exception {
		BufferedWriter bw = null;
		FileWriter fw = null;
		int SIZE = 1024;
		byte[] tmp = new byte[SIZE];
		boolean flag = true;
		PingTest.loadProperties();
		String filepath = PingTest.PROPERTIES
				.getProperty("responseDownloadPath")
				+ routername
				+ "_"
				+ region + "_Reachability.txt";
		File file = new File(filepath);

		// if file doesnt exists, then create it

		if (!file.exists()) {
			file.createNewFile();

		}

		else {
			file.delete();
			file.createNewFile();
		}
		while (input.available() > 0) {
			int i = input.read(tmp, 0, SIZE);
			if (i < 0)
				break;
			/* logger.info(new String(tmp, 0, i)); */
			String s = new String(tmp, 0, i);

			if (s.contains("Microsoft")) {
				int startIndex = s.indexOf("Microsoft ");
				int endIndex = s.indexOf(">");
				String toBeReplaced = s.substring(startIndex, endIndex + 1);
				logger.info("\n" + s.replace(toBeReplaced, ""));

				s = s.replace(toBeReplaced, "");
			}
			if (!(s.equals("")) && s.contains("Destination host unreachable")) {

				flag = false;

			} else if (!(s.equals("")) && s.contains("Request timed out.")) {
				flag = false;

			} else if (!(s.equals(""))
					&& s.contains("Destination net unreachable")) {
				flag = false;

			}

			fw = new FileWriter(file, true);
			bw = new BufferedWriter(fw);
			bw.append(s);
			bw.close();

		}

		return flag;
	}

	public String readResult(String managementIp, String routername,
			String region) {
		String result = null;

		try {
			PingTest.loadProperties();

			String filepath = PingTest.PROPERTIES
					.getProperty("responseDownloadPath")
					+ routername
					+ "_"
					+ region + "_Reachability.txt";
			result = new String(Files.readAllBytes(Paths.get(filepath)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public JSONArray pingResults(String managementIp) {
		logger.info("Start getPingResults - managementIp- " + managementIp);
		JSONArray responce = null;
		RestTemplate restTemplate = new RestTemplate();
		JSONObject obj = new JSONObject();

		try {
			obj.put(new String("ipAddress"), managementIp);

			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			HttpEntity<JSONObject> entity = new HttpEntity<JSONObject>(obj,
					headers);
			String url=TSALabels.PYTHON_SERVICES.getValue()+TSALabels.PYTHON_PING.getValue();
			String response = restTemplate.exchange(url,
					HttpMethod.POST, entity, String.class).getBody();

			JSONParser parser = new JSONParser();
			JSONObject responsejson = (JSONObject) parser.parse(response);
			if (responsejson.containsKey("pingReply")) {
				responce = (JSONArray) responsejson.get("pingReply");
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("Response" + responce);
		return responce;
	}

	@SuppressWarnings("unchecked")
	public JSONArray pingResults(String managementIp, String testType) {
		logger.info("Start getPingResults - managementIp- " + managementIp);
		JSONArray responce = null;
		RestTemplate restTemplate = new RestTemplate();
		JSONObject obj = new JSONObject();

		try {
			obj.put(new String("ipAddress"), managementIp);

			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			HttpEntity<JSONObject> entity = new HttpEntity<JSONObject>(obj,
					headers);
			String url=TSALabels.PYTHON_SERVICES.getValue()+TSALabels.PYTHON_PING.getValue();
			String response = restTemplate.exchange(url,
					HttpMethod.POST, entity, String.class).getBody();

			JSONParser parser = new JSONParser();
			JSONObject responsejson = (JSONObject) parser.parse(response);
			
			switch(testType)
			{
			case "healthCheck":
				responce=new JSONArray();
				JSONObject latency=new JSONObject();
				JSONObject frameloss=new JSONObject();
				JSONObject pingRep=new JSONObject();
				//this is to calculate latency
				if(responsejson.containsKey("avg"))
				{
					latency.put("latency", responsejson.get("avg").toString());
					responce.add(latency);
				}
				else
				{
					latency.put("latency", "0.0");
					responce.add(latency);
				}
				//this is to calculate frameloss
				if(responsejson.containsKey("pingReply"))
				{
					JSONArray pingArray= (JSONArray) responsejson.get("pingReply");
					List<String>list=new ArrayList<String>();
					for(int i=0; i<pingArray.size();i++)
					{
						list.add(pingArray.get(i).toString());
					}
					int frameslost=0;
					for(String res:list)
					{
						if(res.contains("Request timed out"))
						{
							frameslost++;
						}
					}
					double frameslostpercent=0;
					if(frameslost!=0)
					{
					frameslostpercent=(frameslost/100)*list.size();
					}
					frameloss.put("frameloss", frameslostpercent);
					responce.add(frameloss);
					pingRep.put("pingReply", responsejson.get("pingReply").toString());
					responce.add(pingRep);
					
				}
				else
				{
					frameloss.put("frameloss", "0");
					responce.add(frameloss);
					pingRep.put("pingReply", "Device not reachable");
					responce.add(pingRep);

				}
				
				break;
			default:
				if (responsejson.containsKey("pingReply")) {
					responce = (JSONArray) responsejson.get("pingReply");
				}
				
				break;
			}
		/*	if (responsejson.containsKey("pingReply")) {
				responce = (JSONArray) responsejson.get("pingReply");
			}*/

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("Response" + responce);
		return responce;
	}
	
	public String getPingResults(Process process) {
		logger.info("Start getPingResults - " + process);
		long startTime = System.currentTimeMillis();
		StringBuilder resultBuilder = new StringBuilder();
		String responce = null;
		try (BufferedReader inputStream = new BufferedReader(
				new InputStreamReader(process.getInputStream()));
				BufferedReader errorStream = new BufferedReader(
						new InputStreamReader(process.getErrorStream()));) {
			if (errorStream.readLine() != null) {
				resultBuilder.append("Error");
				resultBuilder.append("\n");
				while ((responce = errorStream.readLine()) != null) {
					resultBuilder.append(responce);
					resultBuilder.append("\n");
				}
			} else {
				while ((responce = inputStream.readLine()) != null) {
					resultBuilder.append(responce);
					resultBuilder.append("\n");
				}
			}
			logger.info("getPingResults - " + resultBuilder);
		} catch (IOException exe) {
			logger.error("getPingResults Error - " + exe.getMessage());
		}
		logger.info("Total time taken to getPingResults in millsecs- "
				+ (System.currentTimeMillis() - startTime));
		return resultBuilder.toString();
	}

	public static boolean loadProperties() throws IOException {
		InputStream PropFile = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(PROPERTIES_FILE);

		try {
			PROPERTIES.load(PropFile);
		} catch (IOException exc) {
			exc.printStackTrace();
			return false;
		}
		return false;
	}
}

package com.techm.orion.camunda.servicelayer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import org.json.JSONException;
import org.json.simple.JSONObject;

import com.techm.orion.pojo.Global;

public class CamundaServiceDecomposeWorkflow {
	
	public static String TSA_PROPERTIES_FILE = "TSA.properties";
	public static final Properties TSA_PROPERTIES = new Properties();

	@SuppressWarnings("unchecked")
	public void uploadToServer(String rfoid, String version) throws IOException, JSONException {

		CamundaServiceDecomposeWorkflow.loadProperties();
		String serverPath = CamundaServiceDecomposeWorkflow.TSA_PROPERTIES.getProperty("serverPath");
		
		String query = serverPath + "/engine-rest/process-definition/key/decompWorkflow/start";

		JSONObject obj = new JSONObject();
		JSONObject obj2 = new JSONObject();

		JSONObject variableObj = new JSONObject();


		obj.put(new String("value"), version);

		variableObj.put(new String("version"), obj);

		obj2.put(new String("businessKey"), rfoid);
		obj2.put(new String("variables"), variableObj);

		URL url = new URL(query);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5000);
		conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestMethod("POST");

		OutputStream os = conn.getOutputStream();
		os.write(obj2.toString().getBytes("UTF-8"));
		os.close();

		// read the response
		InputStream in = new BufferedInputStream(conn.getInputStream());
		String result = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
		JSONObject jsonObject = new JSONObject();

		in.close();
		conn.disconnect();

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
}

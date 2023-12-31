package com.techm.c3p.core.entitybeans;

import java.util.List;

import com.techm.c3p.core.pojo.MasterAttribPojo;

public class BasicConfigurationRqst {

	private String vendor;

	private String deviceFamily;

	private String model;

	private String basicConfiguration;

	private List<MasterAttribPojo> attributeMappings;

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getDeviceFamily() {
		return deviceFamily;
	}

	public void setDeviceFamily(String deviceFamily) {
		this.deviceFamily = deviceFamily;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getBasicConfiguration() {
		return basicConfiguration;
	}

	public void setBasicConfiguration(String basicConfiguration) {
		this.basicConfiguration = basicConfiguration;
	}

	public List<MasterAttribPojo> getAttributeMappings() {
		return attributeMappings;
	}

	public void setAttributeMappings(List<MasterAttribPojo> attributeMappings) {
		this.attributeMappings = attributeMappings;
	}
}

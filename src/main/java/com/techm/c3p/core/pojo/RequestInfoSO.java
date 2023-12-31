package com.techm.c3p.core.pojo;

public class RequestInfoSO {
	
    	private String customer;
	private String siteid;
	private String deviceType;
	private String os;
	private String osVersion=null;
	private String vrfName=null;
	private String managementIp;
	private String enablePassword=null; 
	private String vpn;
	private String vendor;
	private String TimeZ;
	private String banner=null;
	private String deviceName;
	private String model;
	private String region;
	private String service;
	private String hostname;
	private String secret;
	private Boolean isAutoProgress;
	private InternetLcVrfSO internetLcVrf;
	private MisArPeSO misArPeSO;
	private DeviceInterfaceSO deviceInterfaceSO;
	private String processID;
	private String site;
	private int request_id;
	private String protocol;
	private String networkMask=null;
	private String status;
	private String endDateofProcessing;
	private String elapsedTime=null;
	private String minElapsedTime;
	private String maxElapsedTime;
	private String avgElapsedTime;
	private double request_version=0;
	private String request_version_string=null;
	
	private double request_parent_version=0;
	private String request_creator_name=null;
	private String elapsed_time=null;
	private String importsource;
	

	private String import_status;
	
	private String snmpHostAddress=null;
	private String snmpString=null;
	private String loopBackType=null;
	private String loopbackIPaddress=null;
	private String loopbackSubnetMask=null;
	private int successCount=0,failureCount=0,totalCount=0;
	
	
	private String request_assigned_to=null;
	
	
	private int read=0;
	private String alpha_numeric_req_id;

	//LAN interface
	
		private String lanInterface;
		private String lanIp;
		private String lanMaskAddress;
		private String lanDescription;
	
		private String certificationSelectionBit;
		
		private String scheduledTime;
		private String templateId;
		private String request_type=null;
		private String zipcode;
		private String managed;
		private String downTimeRequired;
		private String lastUpgradedOn;
		private String TestsSelected=null;
		//new field added after VNF enhancement development : Ruchita Salvi
		private String networkType=null;
		
		private boolean restoreFlag;
		private boolean recurring_flag;
		private boolean baselined_flag;
		
		
		
		
	    public boolean isRestoreFlag() {
			return restoreFlag;
		}
		public void setRestoreFlag(boolean restoreFlag) {
			this.restoreFlag = restoreFlag;
		}
		public boolean isRecurring_flag() {
			return recurring_flag;
		}
		public void setRecurring_flag(boolean recurring_flag) {
			this.recurring_flag = recurring_flag;
		}
		public boolean isBaselined_flag() {
			return baselined_flag;
		}
		public void setBaselined_flag(boolean baselined_flag) {
			this.baselined_flag = baselined_flag;
		}
		public String getNetworkType() {
			return networkType;
		}
		public void setNetworkType(String networkType) {
			this.networkType = networkType;
		}
		public String getTestsSelected() {
			return TestsSelected;
		}
		public void setTestsSelected(String testsSelected) {
			TestsSelected = testsSelected;
		}
		public String getImportsource() {
			return importsource;
		}
		public void setImportsource(String importsource) {
			this.importsource = importsource;
		}
		public String getImport_status() {
			return import_status;
		}
		public void setImport_status(String import_status) {
			this.import_status = import_status;
		}
		public String getRequest_type() {
			return request_type;
		}
		public void setRequest_type(String request_type) {
			this.request_type = request_type;
		}
		public String getZipcode() {
			return zipcode;
		}
		public void setZipcode(String zipcode) {
			this.zipcode = zipcode;
		}
		public String getManaged() {
			return managed;
		}
		public void setManaged(String managed) {
			this.managed = managed;
		}
		public String getDownTimeRequired() {
			return downTimeRequired;
		}
		public void setDownTimeRequired(String downTimeRequired) {
			this.downTimeRequired = downTimeRequired;
		}
		public String getLastUpgradedOn() {
			return lastUpgradedOn;
		}
		public void setLastUpgradedOn(String lastUpgradedOn) {
			this.lastUpgradedOn = lastUpgradedOn;
		}
		public String getRequest_assigned_to() {
			return request_assigned_to;
		}
		public void setRequest_assigned_to(String request_assigned_to) {
			this.request_assigned_to = request_assigned_to;
		}
		public String getTemplateId() {
			return templateId;
		}
		public void setTemplateId(String templateId) {
			this.templateId = templateId;
		}

	public int getRead() {
			return read;
		}
		public void setRead(int read) {
			this.read = read;
		}
	public String getScheduledTime() {
			return scheduledTime;
		}
		public void setScheduledTime(String scheduledTime) {
			this.scheduledTime = scheduledTime;
		}
	public String getCertificationSelectionBit() {
			return certificationSelectionBit;
		}
		public void setCertificationSelectionBit(String certificationSelectionBit) {
			this.certificationSelectionBit = certificationSelectionBit;
		}
	public String getLanInterface() {
			return lanInterface;
		}
		public void setLanInterface(String lanInterface) {
			this.lanInterface = lanInterface;
		}
		public String getLanIp() {
			return lanIp;
		}
		public void setLanIp(String lanIp) {
			this.lanIp = lanIp;
		}
		public String getLanMaskAddress() {
			return lanMaskAddress;
		}
		public void setLanMaskAddress(String lanMaskAddress) {
			this.lanMaskAddress = lanMaskAddress;
		}
		public String getLanDescription() {
			return lanDescription;
		}
		public void setLanDescription(String lanDescription) {
			this.lanDescription = lanDescription;
		}
	public String getSnmpHostAddress() {
		return snmpHostAddress;
	}
	public void setSnmpHostAddress(String snmpHostAddress) {
		this.snmpHostAddress = snmpHostAddress;
	}
	public String getSnmpString() {
		return snmpString;
	}
	public void setSnmpString(String snmpString) {
		this.snmpString = snmpString;
	}
	public String getLoopBackType() {
		return loopBackType;
	}
	public void setLoopBackType(String loopBackType) {
		this.loopBackType = loopBackType;
	}
	public String getLoopbackIPaddress() {
		return loopbackIPaddress;
	}
	public void setLoopbackIPaddress(String loopbackIPaddress) {
		this.loopbackIPaddress = loopbackIPaddress;
	}
	public String getLoopbackSubnetMask() {
		return loopbackSubnetMask;
	}
	public void setLoopbackSubnetMask(String loopbackSubnetMask) {
		this.loopbackSubnetMask = loopbackSubnetMask;
	}
	public String getElapsed_time() {
		return elapsed_time;
	}
	public void setElapsed_time(String elapsed_time) {
		this.elapsed_time = elapsed_time;
	}
	public String getRequest_creator_name() {
		return request_creator_name;
	}
	public void setRequest_creator_name(String request_creator_name) {
		this.request_creator_name = request_creator_name;
	}
	public double getRequest_parent_version() {
		return request_parent_version;
	}
	public void setRequest_parent_version(double request_parent_version) {
		this.request_parent_version = request_parent_version;
	}
	public double getRequest_version() {
		return request_version;
	}
	public String getRequest_version_string() {
		return request_version_string;
	}
	public void setRequest_version_string(String request_version_string) {
		this.request_version_string = request_version_string;
	}
	public void setRequest_version(double request_version) {
		this.request_version = request_version;
	}
	/**
	 * @return the minElapsedTime
	 */
	public String getMinElapsedTime() {
	    return minElapsedTime;
	}
	/**
	 * @param minElapsedTime the minElapsedTime to set
	 */
	public void setMinElapsedTime(String minElapsedTime) {
	    this.minElapsedTime = minElapsedTime;
	}
	/**
	 * @return the maxElapsedTime
	 */
	public String getMaxElapsedTime() {
	    return maxElapsedTime;
	}
	/**
	 * @param maxElapsedTime the maxElapsedTime to set
	 */
	public void setMaxElapsedTime(String maxElapsedTime) {
	    this.maxElapsedTime = maxElapsedTime;
	}
	/**
	 * @return the avgElapsedTime
	 */
	public String getAvgElapsedTime() {
	    return avgElapsedTime;
	}
	/**
	 * @param avgElapsedTime the avgElapsedTime to set
	 */
	public void setAvgElapsedTime(String avgElapsedTime) {
	    this.avgElapsedTime = avgElapsedTime;
	}
	public String getElapsedTime() {
	    return elapsedTime;
	}
	public void setElapsedTime(String elapsedTime) {
	    this.elapsedTime = elapsedTime;
	}
	public String getEndDateofProcessing() {
	    return endDateofProcessing;
	}
	public void setEndDateofProcessing(String endDateofProcessing) {
	    this.endDateofProcessing = endDateofProcessing;
	}
	public String getManagementIp() {
	    return managementIp;
	}
	public void setManagementIp(String managementIp) {
	    this.managementIp = managementIp;
	}
	public int getSuccessCount() {
	    return successCount;
	}
	public void setSuccessCount(int successCount) {
	    this.successCount = successCount;
	}
	public int getFailureCount() {
	    return failureCount;
	}
	public void setFailureCount(int failureCount) {
	    this.failureCount = failureCount;
	}
	public int getTotalCount() {
	    return totalCount;
	}
	public void setTotalCount(int totalCount) {
	    this.totalCount = totalCount;
	}
	public String getStatus() {
	    return status;
	}
	public void setStatus(String status) {
	    this.status = status;
	}
	public String getNetworkMask() {
		return networkMask;
	}
	public void setNetworkMask(String networkMask) {
		this.networkMask = networkMask;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public int getRequest_id() {
		return request_id;
	}
	public void setRequest_id(int request_id) {
		this.request_id = request_id;
	}
	public String getDisplay_request_id() {
		return display_request_id;
	}
	public void setDisplay_request_id(String display_request_id) {
		this.display_request_id = display_request_id;
	}
	public String getDateOfProcessing() {
		return dateOfProcessing;
	}
	public void setDateOfProcessing(String dateOfProcessing) {
		this.dateOfProcessing = dateOfProcessing;
	}
	private String display_request_id;
	private String dateOfProcessing;
	
	
	
	public String getSite() {
		return site;
	}
	public void setSite(String site) {
		this.site = site;
	}
	public String getCustomer() {
		return customer;
	}
	public void setCustomer(String customer) {
		this.customer = customer;
	}
	public String getProcessID() {
		return processID;
	}
	public void setProcessID(String processID) {
		this.processID = processID;
	}
	public DeviceInterfaceSO getDeviceInterfaceSO() {
		return deviceInterfaceSO;
	}
	public void setDeviceInterfaceSO(DeviceInterfaceSO deviceInterfaceSO) {
		this.deviceInterfaceSO = deviceInterfaceSO;
	}
	public Boolean getIsAutoProgress() {
		return isAutoProgress;
	}
	public void setIsAutoProgress(Boolean isAutoProgress) {
		this.isAutoProgress = isAutoProgress;
	}
	public MisArPeSO getMisArPeSO() {
		return misArPeSO;
	}
	public void setMisArPeSO(MisArPeSO misArPeSO) {
		this.misArPeSO = misArPeSO;
	}
	public InternetLcVrfSO getInternetLcVrf() {
		return internetLcVrf;
	}
	public void setInternetLcVrf(InternetLcVrfSO internetLcVrf) {
		this.internetLcVrf = internetLcVrf;
	}
	public String getBanner() {
		return banner;
	}
	public void setBanner(String banner) {
		this.banner = banner;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public String getSecret() {
		return secret;
	}
	public void setSecret(String secret) {
		this.secret = secret;
	}
	@Override
	public String toString() {
		return "RequestInfoSO ["+", banner=" + banner
				+ ", deviceName=" + deviceName + ", model=" + model
				+ ", region=" + region + ", service=" + service + ","
				+ ", hostname=" + hostname + ", secret=" + secret
				+ "," + ", isAutoProgress=" + isAutoProgress
				+ ", internetLcVrf=" + internetLcVrf + ", misArPeSO="
				+ misArPeSO + ", deviceInterfaceSO=" + deviceInterfaceSO + "]";
	}
	/**
	 * @return the siteid
	 */
	public String getSiteid() {
	    return siteid;
	}
	/**
	 * @param siteid the siteid to set
	 */
	public void setSiteid(String siteid) {
	    this.siteid = siteid;
	}
	/**
	 * @return the deviceType
	 */
	public String getDeviceType() {
	    return deviceType;
	}
	/**
	 * @param deviceType the deviceType to set
	 */
	public void setDeviceType(String deviceType) {
	    this.deviceType = deviceType;
	}
	/**
	 * @return the os
	 */
	public String getOs() {
	    return os;
	}
	/**
	 * @param os the os to set
	 */
	public void setOs(String os) {
	    this.os = os;
	}
	/**
	 * @return the osVersion
	 */
	public String getOsVersion() {
	    return osVersion;
	}
	/**
	 * @param osVersion the osVersion to set
	 */
	public void setOsVersion(String osVersion) {
	    this.osVersion = osVersion;
	}
	/**
	 * @return the vrfName
	 */
	public String getVrfName() {
	    return vrfName;
	}
	/**
	 * @param vrfName the vrfName to set
	 */
	public void setVrfName(String vrfName) {
	    this.vrfName = vrfName;
	}
	/**
	 * @return the enablePassword
	 */
	public String getEnablePassword() {
	    return enablePassword;
	}
	/**
	 * @param enablePassword the enablePassword to set
	 */
	public void setEnablePassword(String enablePassword) {
	    this.enablePassword = enablePassword;
	}
	/**
	 * @return the vpn
	 */
	public String getVpn() {
	    return vpn;
	}
	/**
	 * @param vpn the vpn to set
	 */
	public void setVpn(String vpn) {
	    this.vpn = vpn;
	}
	/**
	 * @return the vendor
	 */
	public String getVendor() {
	    return vendor;
	}
	/**
	 * @param vendor the vendor to set
	 */
	public void setVendor(String vendor) {
	    this.vendor = vendor;
	}
	
	public String getTimeZ() {
		return TimeZ;
	}
	public void setTimeZ(String timeZ) {
		TimeZ = timeZ;
	}
	public String getAlpha_numeric_req_id() {
		return alpha_numeric_req_id;
	}
	public void setAlpha_numeric_req_id(String alpha_numeric_req_id) {
		this.alpha_numeric_req_id = alpha_numeric_req_id;
	}
	
	
}

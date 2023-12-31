package com.techm.c3p.core.entitybeans;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "c3p_host_ip_mgmt")
public class HostIpManagementEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1569379287948927993L;

	@Id
	@Column(name = "h_rowid", length = 20)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int hostRowId;

	@Column(name = "h_ip_pooltype", length = 25)
	private String hostIpPoolType;

	@Column(name = "h_pool_id", length = 20)
	private Integer hostPoolId;

	@Column(name = "h_customer", length = 255)
	private String hostCustomer;

	@Column(name = "h_region", length = 255)
	private String hostRegion;

	@Column(name = "h_site_name", length = 255)
	private String hostSiteName;

	@Column(name = "h_site_id", length = 255)
	private String hostSiteId;

	@Column(name = "h_start_ip", length = 100)
	private String hostStartIp;

	@Column(name = "h_mask", length = 100)
	private String hostMask;

	@Column(name = "h_hostname", length = 255)
	private String hostHostName;

	@Column(name = "h_role", length = 255)
	private String hostRole;

	@Column(name = "h_remarks", length = 255)
	private String hostRemarks;

	@Column(name = "h_status", length = 50)
	private String hostStatus;

	@Column(name = "h_created_by", length = 45)
	private String hostCreatedBy;

	@Column(name = "h_updated_by", length = 45)
	private String hostUpdatedBy;

	@Column(name = "h_created_date")
	private Date hostCreatedDate;

	@Column(name = "h_updated_date")
	private Date hostUpdatedDate;
	
	@Column(name = "h_released_flag", length = 1)
	private String hostReleasedFlag;
	
	@Column(name = "h_released_on")
	private Date hostReleasedOn;

	public String getHostReleasedFlag() {
		return hostReleasedFlag;
	}

	public void setHostReleasedFlag(String hostReleasedFlag) {
		this.hostReleasedFlag = hostReleasedFlag;
	}

	public int getHostRowId() {
		return hostRowId;
	}

	public void setHostRowId(int hostRowId) {
		this.hostRowId = hostRowId;
	}

	public String getHostIpPoolType() {
		return hostIpPoolType;
	}

	public void setHostIpPoolType(String hostIpPoolType) {
		this.hostIpPoolType = hostIpPoolType;
	}

	public Integer getHostPoolId() {
		return hostPoolId;
	}

	public void setHostPoolId(Integer hostPoolId) {
		this.hostPoolId = hostPoolId;
	}

	public String getHostCustomer() {
		return hostCustomer;
	}

	public void setHostCustomer(String hostCustomer) {
		this.hostCustomer = hostCustomer;
	}

	public String getHostRegion() {
		return hostRegion;
	}

	public void setHostRegion(String hostRegion) {
		this.hostRegion = hostRegion;
	}

	public String getHostSiteName() {
		return hostSiteName;
	}

	public void setHostSiteName(String hostSiteName) {
		this.hostSiteName = hostSiteName;
	}

	public String getHostSiteId() {
		return hostSiteId;
	}

	public void setHostSiteId(String hostSiteId) {
		this.hostSiteId = hostSiteId;
	}

	public String getHostStartIp() {
		return hostStartIp;
	}

	public void setHostStartIp(String hostStartIp) {
		this.hostStartIp = hostStartIp;
	}

	public String getHostMask() {
		return hostMask;
	}

	public void setHostMask(String hostMask) {
		this.hostMask = hostMask;
	}

	public String getHostHostName() {
		return hostHostName;
	}

	public void setHostHostName(String hostHostName) {
		this.hostHostName = hostHostName;
	}

	public String getHostRole() {
		return hostRole;
	}

	public void setHostRole(String hostRole) {
		this.hostRole = hostRole;
	}

	public String getHostRemarks() {
		return hostRemarks;
	}

	public void setHostRemarks(String hostRemarks) {
		this.hostRemarks = hostRemarks;
	}

	public String getHostStatus() {
		return hostStatus;
	}

	public void setHostStatus(String hostStatus) {
		this.hostStatus = hostStatus;
	}

	public String getHostCreatedBy() {
		return hostCreatedBy;
	}

	public void setHostCreatedBy(String hostCreatedBy) {
		this.hostCreatedBy = hostCreatedBy;
	}

	public String getHostUpdatedBy() {
		return hostUpdatedBy;
	}

	public void setHostUpdatedBy(String hostUpdatedBy) {
		this.hostUpdatedBy = hostUpdatedBy;
	}

	public Date getHostCreatedDate() {
		return hostCreatedDate;
	}

	public void setHostCreatedDate(Date hostCreatedDate) {
		this.hostCreatedDate = hostCreatedDate;
	}

	public Date getHostUpdatedDate() {
		return hostUpdatedDate;
	}

	public void setHostUpdatedDate(Date hostUpdatedDate) {
		this.hostUpdatedDate = hostUpdatedDate;
	}

	public Date getHostReleasedOn() {
		return hostReleasedOn;
	}

	public void setHostReleasedOn(Date hostReleasedOn) {
		this.hostReleasedOn = hostReleasedOn;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + hostRowId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HostIpManagementEntity other = (HostIpManagementEntity) obj;
		if (hostRowId != other.hostRowId)
			return false;
		return true;
	}
}
package com.techm.c3p.core.entitybeans;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "c3p_t_inventorymgmt_interfaces")
@JsonIgnoreProperties(ignoreUnknown = false)
public class DeviceDiscoveryInterfaceEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4942717149166571780L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(name = "i_Int_type", length = 10)
	private String iIntType;

	@Column(name = "i_int_name", length = 30)
	private String iIntName;

	@Column(name = "i_int_description", length = 64)
	private String iIntDescription;

	@Column(name = "i_int_ipaddr", length = 15)
	private String iIntIpaddr;

	@Column(name = "i_int_subnet", length = 15)
	private String iIntSubnet;

	@Column(name = "i_int_ipv6addr", length = 128)
	private String iIntIpv6addr;

	@Column(name = "i_int_prefix", length = 3)
	private String iIntPrefix;

	@Column(name = "i_int_admin_stat")
	private String iIntAdminStat;

	@Column(name = "i_int_Oper_stat", length = 16)
	private String iIntOperStat;

	@Column(name = "i_int_phy_addr", length = 48)
	private String iIntPhyAddr;

	@Column(name = "i_int_connecting_host", length = 20)
	private String iIntConnectingHost;

	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.MERGE, CascadeType.PERSIST })
	private DeviceDiscoveryEntity device;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getiIntType() {
		return iIntType;
	}

	public void setiIntType(String iIntType) {
		this.iIntType = iIntType;
	}

	public String getiIntName() {
		return iIntName;
	}

	public void setiIntName(String iIntName) {
		this.iIntName = iIntName;
	}

	public String getiIntDescription() {
		return iIntDescription;
	}

	public void setiIntDescription(String iIntDescription) {
		this.iIntDescription = iIntDescription;
	}

	public String getiIntIpaddr() {
		return iIntIpaddr;
	}

	public void setiIntIpaddr(String iIntIpaddr) {
		this.iIntIpaddr = iIntIpaddr;
	}

	public String getiIntSubnet() {
		return iIntSubnet;
	}

	public void setiIntSubnet(String iIntSubnet) {
		this.iIntSubnet = iIntSubnet;
	}

	public String getiIntIpv6addr() {
		return iIntIpv6addr;
	}

	public void setiIntIpv6addr(String iIntIpv6addr) {
		this.iIntIpv6addr = iIntIpv6addr;
	}

	public String getiIntPrefix() {
		return iIntPrefix;
	}

	public void setiIntPrefix(String iIntPrefix) {
		this.iIntPrefix = iIntPrefix;
	}

	public String getiIntAdminStat() {
		return iIntAdminStat;
	}

	public void setiIntAdminStat(String iIntAdminStat) {
		this.iIntAdminStat = iIntAdminStat;
	}

	public String getiIntOperStat() {
		return iIntOperStat;
	}

	public void setiIntOperStat(String iIntOperStat) {
		this.iIntOperStat = iIntOperStat;
	}

	public String getiIntPhyAddr() {
		return iIntPhyAddr;
	}

	public void setiIntPhyAddr(String iIntPhyAddr) {
		this.iIntPhyAddr = iIntPhyAddr;
	}

	public DeviceDiscoveryEntity getDevice() {
		return device;
	}

	public void setDevice(DeviceDiscoveryEntity device) {
		this.device = device;
	}

	public String getiIntConnectingHost() {
		return iIntConnectingHost;
	}

	public void setiIntConnectingHost(String iIntConnectingHost) {
		this.iIntConnectingHost = iIntConnectingHost;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		DeviceDiscoveryInterfaceEntity other = (DeviceDiscoveryInterfaceEntity) obj;
		if (id != other.id)
			return false;
		return true;
	}

}

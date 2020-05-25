package com.techm.orion.entitybeans;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_create_config_m_attrib_info")
public class CreateConfigEntity {

	@GeneratedValue(strategy = GenerationType.AUTO)
	@Id
	private int id;

	//Which is given from Master Attribute
	@Column(name = "master_label_id")
	private int masterLabelId;

	@Column(name = "master_label_value ")
	private String masterLabelValue;

	@Column(name = "request_id ")
	private String requestId;

	@Column(name = "template_id  ")
	private String templateId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMasterLabelId() {
		return masterLabelId;
	}

	public void setMasterLabelId(int masterLabelId) {
		this.masterLabelId = masterLabelId;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getMasterLabelValue() {
		return masterLabelValue;
	}

	public void setMasterLabelValue(String masterLabelValue) {
		this.masterLabelValue = masterLabelValue;
	}


}
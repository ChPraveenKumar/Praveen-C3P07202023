package com.techm.c3p.core.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.techm.c3p.core.entitybeans.TemplateFeatureEntity;

public class AttribCreateConfigPojo {
	private int id;

	private String attribName;

	private String attribLabel;

	private String attribUIComponent;

	private String[] attribValidations;

	private String attribType;

	@JsonInclude(Include.NON_NULL)
	private String attribTemplateId;

	@JsonInclude(Include.NON_NULL)
	private String attribSeriesId;

	private String attribCategoty;

	private TemplateFeatureEntity templateFeature;

	private boolean isKey;

	private String attribValue;

	private List<Integer> poolIdList;
	
	private String attribMasterChId;
	
	private String defaultValue;

	public String getAttribMasterChId() {
		return attribMasterChId;
	}

	public void setAttribMasterChId(String attribMasterChId) {
		this.attribMasterChId = attribMasterChId;
	}

	public List<Integer> getPoolIdList() {
		return poolIdList;
	}

	public void setPoolIdList(List<Integer> poolIdList) {
		this.poolIdList = poolIdList;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAttribName() {
		return attribName;
	}

	public void setAttribName(String attribName) {
		this.attribName = attribName;
	}

	public String getAttribUIComponent() {
		return attribUIComponent;
	}

	public void setAttribUIComponent(String attribUIComponent) {
		this.attribUIComponent = attribUIComponent;
	}

	public String getAttribCategoty() {
		return attribCategoty;
	}

	public void setAttribCategoty(String attribCategoty) {
		this.attribCategoty = attribCategoty;
	}

	public String getAttribType() {
		return attribType;
	}

	public void setAttribType(String attribType) {
		this.attribType = attribType;
	}

	public String getAttribTemplateId() {
		return attribTemplateId;
	}

	public void setAttribTemplateId(String attribTemplateId) {
		this.attribTemplateId = attribTemplateId;
	}

	public String getAttribSeriesId() {
		return attribSeriesId;
	}

	public void setAttribSeriesId(String attribSeriesId) {
		this.attribSeriesId = attribSeriesId;
	}

	public String getAttribLabel() {
		return attribLabel;
	}

	public void setAttribLabel(String attribLabel) {
		this.attribLabel = attribLabel;
	}

	public String[] getAttribValidations() {
		return attribValidations;
	}

	public void setAttribValidations(String[] attribValidations) {
		this.attribValidations = attribValidations;
	}

	public TemplateFeatureEntity getTemplateFeature() {
		return templateFeature;
	}

	public void setTemplateFeature(TemplateFeatureEntity templateFeature) {
		this.templateFeature = templateFeature;
	}

	public boolean isKey() {
		return isKey;
	}

	public void setKey(boolean isKey) {
		this.isKey = isKey;
	}

	public String getAttribValue() {
		return attribValue;
	}

	public void setAttribValue(String attribValue) {
		this.attribValue = attribValue;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
}

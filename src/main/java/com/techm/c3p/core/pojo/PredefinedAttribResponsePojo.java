package com.techm.c3p.core.pojo;

public class PredefinedAttribResponsePojo {
	
	private String name;
	
	private String type;
	
	private String uiComponent;
	
	private String [] validations;
	
	//private List<Category> categoryList;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUiComponent() {
		return uiComponent;
	}

	public void setUiComponent(String uiComponent) {
		this.uiComponent = uiComponent;
	}

	public String[] getValidations() {
		return validations;
	}

	public void setValidations(String[] validations) {
		this.validations = validations;
	}

	/*public List<Category> getCategoryList() {
		return categoryList;
	}

	public void setCategoryList(List<Category> categoryList) {
		this.categoryList = categoryList;
	}*/
	
}

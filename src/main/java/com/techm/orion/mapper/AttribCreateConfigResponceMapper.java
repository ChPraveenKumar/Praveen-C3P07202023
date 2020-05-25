package com.techm.orion.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.techm.orion.entitybeans.MasterAttributes;
import com.techm.orion.pojo.AttribCreateConfigJson;
import com.techm.orion.pojo.AttribCreateConfigPojo;
import com.techm.orion.pojo.CategoryDropDownPojo;
import com.techm.orion.service.CategoryDropDownService;
@Component
public class AttribCreateConfigResponceMapper {
	
	@Autowired 
	CategoryDropDownService  categoryDropDownservice;
	
	public AttribCreateConfigPojo getAttribTemplateSuggestionMapper(MasterAttributes entity) {
		AttribCreateConfigPojo pojo = new AttribCreateConfigPojo();
		pojo.setId(entity.getId()); 
		pojo.setAttribName(entity.getName()); 
		pojo.setAttribUIComponent(entity.getUiComponent());
		pojo.setAttribSeriesId(entity.getSeriesId());
		pojo.setAttribTemplateId(entity.getTemplateId()); 
		pojo.setAttribValidations(entity.getValidations().replace("[", "").replace("]", "").split(", "));
		pojo.setAttribType(entity.getAttribType());
		pojo.setAttribLabel(entity.getLabel());
		pojo.setAttribCategoty(entity.getCategory()); 
		pojo.setTemplateFeature(entity.getTemplateFeature());
		return pojo;
	}

	public List<AttribCreateConfigPojo> getAllAttribTemplateSuggestionMapper(List<MasterAttributes> entityList) {
		List<AttribCreateConfigPojo> pojo = new ArrayList<AttribCreateConfigPojo>();

		for (MasterAttributes entity : entityList) {
			pojo.add(getAttribTemplateSuggestionMapper(entity));
		}
		return pojo;
	}

	
	public List<AttribCreateConfigJson> convertAttribPojoToJson(List<AttribCreateConfigPojo> pojoList){
		List<AttribCreateConfigJson> jsonList = new ArrayList<AttribCreateConfigJson>();
		
		for (AttribCreateConfigPojo entity : pojoList) {

			AttribCreateConfigJson attribJson = new AttribCreateConfigJson();

			attribJson.setId(entity.getId());
			attribJson.setName(entity.getAttribName());
			attribJson.setLabel(entity.getAttribLabel());
			attribJson.setuIComponent(entity.getAttribUIComponent());
			attribJson.setValidations(entity.getAttribValidations());
			attribJson.setType(entity.getAttribType());
			attribJson.setSeriesId(entity.getAttribSeriesId());
			attribJson.setTemplateId(entity.getAttribTemplateId());
			// using Category Name find all category Value 
			if (entity.getAttribCategoty() != null) {
				List<CategoryDropDownPojo> allByCategoryName = categoryDropDownservice
						.getAllByCategoryName(entity.getAttribCategoty());
				attribJson.setCategotyLabel(entity.getAttribCategoty());
				attribJson.setCategory(allByCategoryName);
			}
			jsonList.add(attribJson);

		}
		
		return jsonList;
		
		
	}
}

package com.techm.c3p.core.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techm.c3p.core.pojo.AttribCreateConfigJson;
import com.techm.c3p.core.pojo.AttribCreateConfigPojo;
import com.techm.c3p.core.pojo.AttribRequestInfoSOPojo;
import com.techm.c3p.core.pojo.AttribUIComponentPojo;
import com.techm.c3p.core.pojo.AttribValidationPojo;
import com.techm.c3p.core.pojo.CategoryDropDownPojo;
import com.techm.c3p.core.pojo.CategoryMasterPojo;
import com.techm.c3p.core.pojo.PredefinedMappedAtrribPojo;
import com.techm.c3p.core.response.entity.GetAttribResponseEntity;
import com.techm.c3p.core.service.AttribCreateConfigService;
import com.techm.c3p.core.service.AttribSevice;
import com.techm.c3p.core.service.CategoryDropDownService;
import com.techm.c3p.core.service.CategoryMasterService;

@RestController
@RequestMapping(value = "/attrib")
public class AttribController {
	private static final Logger logger = LogManager.getLogger(AddNewAlertNotificationService.class);
	
	@Autowired
	AttribSevice attribSevice;

	@Autowired()
	CategoryMasterService categoryMasterService;

	@Autowired
	CategoryDropDownService categoryDropDownservice;

	@Autowired
	AttribCreateConfigService service;
	
	
	/**
	 * This Api is marked as ***************c3p-ui Api Impacted****************
	 **/	
	//To get Predefine attribute ,Generic attribute, Validation, Category and UI component list.
	@GET
	@Produces("application/json")
	@RequestMapping(value = "/getMasterAttrib", method = RequestMethod.GET, produces = "application/json")
	public Response getMasterAttribData() {
		List<PredefinedMappedAtrribPojo> predefinedGenericAtrribList = attribSevice
				.getAllMasterPredefinedGenericAtrribData();
		List<AttribUIComponentPojo> attribUIComponentList = attribSevice
				.getALLUIComponents();
		List<AttribValidationPojo> attribValidationList = attribSevice
				.getAllValidations();
		List<CategoryMasterPojo> masterCategoryList = categoryMasterService
				.getAll();

		GetAttribResponseEntity attribResponseEntity = attribSevice
				.createGetAttribResponse(predefinedGenericAtrribList,
						attribUIComponentList, attribValidationList,
						masterCategoryList);

		return Response.status(200).entity(attribResponseEntity).build();
	}
	
	/**
	 * This Api is marked as ***************c3p-ui Api Impacted****************
	 **/
	/*
	 * To get Predefine attribute ,Generic attribute, Validation, Category and
	 * UI component list.
	 */
	@GET
	@Produces("application/json")
	@RequestMapping(value = "/getTemplateAttrib", method = RequestMethod.GET, produces = "application/json")
	public Response getTemplateAttribData(@RequestParam String templateId) { 
		List<PredefinedMappedAtrribPojo> predefinedGenericAtrribList = attribSevice.getAllTemplatePredefinedGenericAtrribData(templateId);
		List<AttribUIComponentPojo> attribUIComponentList = attribSevice
				.getALLUIComponents();
		List<AttribValidationPojo> attribValidationList = attribSevice
				.getAllValidations();
		List<CategoryMasterPojo> masterCategoryList = categoryMasterService
				.getAll();

		GetAttribResponseEntity attribResponseEntity = attribSevice
				.createGetAttribResponse(predefinedGenericAtrribList,
						attribUIComponentList, attribValidationList,
						masterCategoryList);

		return Response.status(200).entity(attribResponseEntity).build();
	}
	
	/**
	 * This Api is marked as ***************c3p-ui Api Impacted****************
	 **/
	/* To get attributes mapped values */
	@POST
	@Produces("application/json")
	@RequestMapping(value = "/getAttribMapping", method = RequestMethod.POST, produces = "application/json")
	public Response getAttribMapping(@RequestBody String configRequest) {
		Response build = null;
		try {
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(configRequest);

			AttribRequestInfoSOPojo createConfigRequest = new AttribRequestInfoSOPojo();
			createConfigRequest.setVendor(json.get("vendor").toString()
					.toUpperCase());
			createConfigRequest.setDeviceType(json.get("deviceType").toString()
					.toUpperCase());
			createConfigRequest.setModel(json.get("model").toString());

			/* Using model,vendor and Device type find Series Id */
			String seriesId = service.getSeriesId(
					createConfigRequest.getVendor(),
					createConfigRequest.getDeviceType(),
					createConfigRequest.getModel());

			/* using Series Id find All arrtibute Information */
			List<AttribCreateConfigPojo> byAttribSeriesId = service
					.getByAttribSeriesId(seriesId);

			List<AttribCreateConfigJson> jsonValue = new ArrayList<AttribCreateConfigJson>();
			/* map byAttribSeriesId List to jsonValue List to return Responce */
			for (AttribCreateConfigPojo entity : byAttribSeriesId) {
				
				AttribCreateConfigJson attribJson = new AttribCreateConfigJson();
				
				attribJson.setId(entity.getId());
				attribJson.setName(entity.getAttribName());
				attribJson.setLabel(entity.getAttribLabel());
				attribJson.setuIComponent(entity.getAttribUIComponent());
				attribJson.setValidations(entity.getAttribValidations());
				attribJson.setType(entity.getAttribType());
				attribJson.setSeriesId(entity.getAttribSeriesId());
				attribJson.setTemplateId(entity.getAttribTemplateId());
				/* using Category Name find all category Value */
				if (entity.getAttribCategoty() != null) {
					List<CategoryDropDownPojo> allByCategoryName = categoryDropDownservice
							.getAllByCategoryName(entity.getAttribCategoty());
					attribJson.setCategotyLabel(entity.getAttribCategoty());
					attribJson.setCategory(allByCategoryName);
				}
				jsonValue.add(attribJson);

			}

			build = Response.status(200).entity(jsonValue).build();

		} catch (Exception e) {
			logger.error(e);
		}

		return build;

	}
	
	/**
	 * This Api is marked as ***************c3p-ui Api Impacted****************
	 **/	
	//GetAttrbute For selected Attribute	
	@POST
	@Produces("application/json")
	@RequestMapping(value = "/getFeatureAttribMapping", method = RequestMethod.POST, produces = "application/json")
	public Response getFeatureAttribMapping(@RequestBody String configRequest) {
		Response build = null;
		try {
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(configRequest);	
			String templateId= json.get("templateid").toString();		
			String featureName= json.get("featurName").toString();		
			
			List<AttribCreateConfigPojo> attribByTemplateAndFeatureName = service.getByAttribTemplateAndFeatureName(templateId, featureName);
			List<AttribCreateConfigJson> jsonValue = new ArrayList<AttribCreateConfigJson>();
			/* map attribByTemplateAndFeatureName List to jsonValue List to return Responce */
			for (AttribCreateConfigPojo entity : attribByTemplateAndFeatureName) {

				AttribCreateConfigJson attribJson = new AttribCreateConfigJson();

				attribJson.setId(entity.getId());
				attribJson.setName(entity.getAttribName());
				attribJson.setLabel(entity.getAttribLabel());
				attribJson.setuIComponent(entity.getAttribUIComponent());
				attribJson.setValidations(entity.getAttribValidations());
				attribJson.setType(entity.getAttribType());
				attribJson.setSeriesId(entity.getAttribSeriesId());
				attribJson.setTemplateId(entity.getAttribTemplateId());
				/* using Category Name find all category Value */
				if (entity.getAttribCategoty() != null) {
					List<CategoryDropDownPojo> allByCategoryName = categoryDropDownservice
							.getAllByCategoryName(entity.getAttribCategoty());
					attribJson.setCategotyLabel(entity.getAttribCategoty());
					attribJson.setCategory(allByCategoryName);
				}
				jsonValue.add(attribJson);

			}

			build = Response.status(200).entity(jsonValue).build();

		} catch (Exception e) {
			logger.error(e);
		}

		return build;

	}
	
	

}

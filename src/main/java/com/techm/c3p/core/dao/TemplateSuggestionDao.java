package com.techm.c3p.core.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.techm.c3p.core.connection.DBUtil;
import com.techm.c3p.core.connection.JDBCConnection;
import com.techm.c3p.core.pojo.AttribCreateConfigJson;
import com.techm.c3p.core.pojo.AttribCreateConfigPojo;
import com.techm.c3p.core.pojo.CategoryDropDownPojo;
import com.techm.c3p.core.pojo.TemplateAttribPojo;
import com.techm.c3p.core.pojo.TemplateBasicConfigurationPojo;
import com.techm.c3p.core.service.AttribCreateConfigService;
import com.techm.c3p.core.service.CategoryDropDownService;

@Component
public class TemplateSuggestionDao {
	private static final Logger logger = LogManager.getLogger(TemplateSuggestionDao.class);
	@Autowired
	private AttribCreateConfigService service;
	@Autowired
	private CategoryDropDownService categoryDropDownservice;
	@Autowired
	private JDBCConnection jDBCConnection;
	
	private Connection connection;

	public List<String> getListOfFeaturesForDeviceDetail(String tempId, String networkType) throws SQLException {
		connection = jDBCConnection.getConnection();
		ResultSet rs = null;
		PreparedStatement preparedStmt = null;
		String query = null;
		String query1 = null;
		List<String> featureList = new ArrayList<String>();

		List<Integer> featuresChecked = new ArrayList<Integer>();
		try {

			query = "Select distinct id from c3p_template_transaction_feature_list where command_feature_template_id in(select CONCAT(temp_id, '_V', temp_version) from templateconfig_basic_details where temp_status='Approved' and temp_id like ?)";
			String query2 = "SELECT * FROM templateconfig_basic_details WHERE temp_id like ?";


			preparedStmt = connection.prepareStatement(query2);
			preparedStmt.setString(1, tempId+"%");

			rs = preparedStmt.executeQuery();
			Set<String> networkTypeList = new HashSet<>();
			while (rs.next()) {
				networkTypeList.add(rs.getString("temp_network_type"));
			}
			preparedStmt.close();
			if (networkTypeList.size() >= 1) {
				for (String data : networkTypeList) {
					if (data.equals(networkType)) {
						preparedStmt = connection.prepareStatement(query);
						preparedStmt.setString(1, tempId+"%");

						rs = preparedStmt.executeQuery();
						while (rs.next()) {
							featuresChecked.add(rs.getInt("id"));
							// itemsToAdd.add(rs.getString("tempCmd.Parent_name"));

						}
						preparedStmt.close();
						query1 = "select * from c3p_template_master_feature_list where id=?";
						for (int i = 0; i < featuresChecked.size(); i++) {

							preparedStmt = connection.prepareStatement(query1);

							preparedStmt.setInt(1, featuresChecked.get(i));
							rs = preparedStmt.executeQuery();
							while (rs.next()) {
								boolean isPresent = false;
								if (rs.getInt("hasParent") == 1) {
									if (featureList.size() > 0) {
										for (int j = 0; j < featureList.size(); j++) {
											if (rs.getString("command_parent_feature")
													.equalsIgnoreCase(featureList.get(j))) {
												isPresent = true;
												break;
											}
										}
										if (!isPresent) {
											featureList.add(rs.getString("command_parent_feature"));

										}
									} else {
										featureList.add(rs.getString("command_parent_feature"));
									}
								} else if (rs.getInt("hasParent") == 0) {
									featureList.add(rs.getString("comand_display_feature"));
								}
							}
						}
					}

				}
			}

		} finally {
			DBUtil.close(rs);
			DBUtil.close(preparedStmt);
			DBUtil.close(connection);
		}
		return featureList;
	}

	public List<String> getListOfFeatureForSelectedTemplate(String tempId) throws SQLException {
		connection = jDBCConnection.getConnection();
		ResultSet rs = null;
		PreparedStatement preparedStmt = null;
		String query = null;
		String query1 = null;
		List<String> featureList = new ArrayList<String>();

		List<Integer> featuresChecked = new ArrayList<Integer>();
		try {
			query = "Select * from c3p_template_transaction_feature_list where command_feature_template_id  = ?";
			preparedStmt = connection.prepareStatement(query);
			preparedStmt.setString(1, tempId);

			rs = preparedStmt.executeQuery();
			while (rs.next()) {
				featuresChecked.add(rs.getInt("id"));
				// itemsToAdd.add(rs.getString("tempCmd.Parent_name"));

			}
			preparedStmt.close();
			query1 = "select * from c3p_template_master_feature_list where id=?";
			for (int i = 0; i < featuresChecked.size(); i++) {

				preparedStmt = connection.prepareStatement(query1);

				preparedStmt.setInt(1, featuresChecked.get(i));
				rs = preparedStmt.executeQuery();
				while (rs.next()) {
					boolean isPresent = false;
					if (rs.getInt("hasParent") == 1) {
						if (featureList.size() > 0) {
							for (int j = 0; j < featureList.size(); j++) {
								if (rs.getString("command_parent_feature").equalsIgnoreCase(featureList.get(j))) {
									isPresent = true;
									break;
								}
							}
							if (!isPresent) {
								featureList.add(rs.getString("command_parent_feature"));

							}
						} else {
							featureList.add(rs.getString("command_parent_feature"));
						}
					} else if (rs.getInt("hasParent") == 0) {
						featureList.add(rs.getString("comand_display_feature"));
					}
				}
			}
			/*
			 * for (String value : featureList) { if(!finalList.contains(value)) {
			 * finalList.add(value); } }
			 */
		} finally {
			DBUtil.close(rs);
			DBUtil.close(preparedStmt);
			DBUtil.close(connection);
		}
		return featureList;
	}

	public List<TemplateBasicConfigurationPojo> getDataGrid(String[] feature, String templateId) {
        String query = createQuery(feature.length);
        // String length=feature.length;
        String query1 = "select * from templateconfig_basic_details  where concat(temp_id,'_V',temp_version) in(Select command_feature_template_id from c3p_template_transaction_feature_list where id in("
                + query
                + ") and command_feature_template_id like ?  group by command_feature_template_id having count(distinct id)=?) and temp_status='Approved'";
        logger.info("Query=" + query1);
        connection = jDBCConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<TemplateBasicConfigurationPojo> listTemplate = new ArrayList<TemplateBasicConfigurationPojo>();
        TemplateBasicConfigurationPojo templateData;
        List<String> getVersionListForTemplate = new ArrayList<String>();
        try {
            ps = connection.prepareStatement(query1);
            for (int i = 1; i <= feature.length; i++) {
                ps.setString(i, feature[i - 1]);
            }
            ps.setString(feature.length + 1, templateId + '%');
            List<String> list = Arrays.asList(feature);
            if (list.contains("Routing Protocol")) {
                // ps.setString(feature.length+2, String.valueOf(feature.length+2)); /* comment
                // as not getting routing protocol template suggestion*/
                ps.setString(feature.length + 2,
                        String.valueOf(feature.length)); /* added for getting routing protocol template suggestion */
            } else {
                ps.setString(feature.length + 2, String.valueOf(feature.length));
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                templateData = new TemplateBasicConfigurationPojo();
                templateData.setTemplateId(rs.getString("temp_id").concat("_V").concat(rs.getString("temp_version")));
                templateData.setComment(rs.getString("temp_comment_section"));
                templateData.setNetworkType(rs.getString("temp_network_type"));
                templateData.setAlias(rs.getString("temp_alias").concat("_V").concat(rs.getString("temp_version")));
                getVersionListForTemplate.add(rs.getString("temp_version"));

                listTemplate.add(templateData);
            }
            // int versionSize=getVersionListForTemplate.size();

 

            String bestTemplate = getBestTemplate(getVersionListForTemplate, templateId);
            for (Iterator iterator = listTemplate.iterator(); iterator.hasNext();) {
                TemplateBasicConfigurationPojo templateBasicConfigurationPojo = (TemplateBasicConfigurationPojo) iterator
                        .next();

 

                if (templateBasicConfigurationPojo.getTemplateId().equalsIgnoreCase(bestTemplate)) {
                    templateBasicConfigurationPojo.setEnabled(true);
                } else {
                    templateBasicConfigurationPojo.setEnabled(false);
                }

 

            }

 

            try {
                rs.close();
            } catch (SQLException e) {
            }

 

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                ps.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return listTemplate;
    }
 

	private String createQuery(int length) {
		String query = "select id from c3p_template_master_feature_list where command_parent_feature in(";
		StringBuilder queryBuilder = new StringBuilder(query);
		for (int i = 0; i < length; i++) {
			queryBuilder.append(" ?");
			if (i != length - 1)
				queryBuilder.append(",");
		}
		queryBuilder.append(")");
		return queryBuilder.toString();
	}

	public void insertTemplateUsageData(String TemplateId) {
		connection = jDBCConnection.getConnection();

		String[] arr = TemplateId.split("_V");
		//String templateId = TemplateId.substring(0, 14);
		//String version = TemplateId.substring(16, 19);
		String templateId =  arr[0];
		String version = arr[1];
		if(arr[1]==null || arr[1].isEmpty())
		{
			arr[1]="1";
		}
		ResultSet rs = null;

		try {

			String query1 = "SELECT * FROM template_usage_data WHERE templateId = ? and Version = ?";

			PreparedStatement ps = connection.prepareStatement(query1);

			ps.setString(1, templateId);
			ps.setString(2, version);

			rs = ps.executeQuery();

			if (rs.next()) {

				int totalUsage = Integer.parseInt(rs.getString("Total_Usage"));

				String query = null;
				query = "update template_usage_data set Total_Usage = ?  WHERE templateId = ? and Version = ?";
				PreparedStatement preparedStmt;

				preparedStmt = connection.prepareStatement(query);

				preparedStmt.setInt(1, ++totalUsage);
				preparedStmt.setString(2, templateId);
				preparedStmt.setString(3, version);

				preparedStmt.executeUpdate();

			}

			else {
				String sql = "INSERT INTO template_usage_data(templateId,Version,Total_Usage)" + "VALUES(?,?,?)";

				ps = connection.prepareStatement(sql);

				ps.setString(1, templateId);

				ps.setString(2, version);
				ps.setInt(3, 1);

				ps.executeUpdate();
			}
		} catch (SQLException e) {
			logger.error("SQLException in insertTemplateUsageData method "+e.getMessage());
		}

	}

	public void updateTemplateUsageData(String TemplateId, String result) {
		connection = jDBCConnection.getConnection();	
		ResultSet rs = null;
		logger.info("updateTemplateUsageData - TemplateId -"+TemplateId);
		try {
			/*Added the TemplateId not null check.. TemplateId will have some data in case of config test. Other tests TemplateId will be null*/
			if(TemplateId !=null) {
				String templateId = TemplateId.substring(0, 14);
				String version = TemplateId.substring(16, 19);
	
				String query1 = "SELECT * FROM template_usage_data WHERE templateId = ? and Version = ?";
	
				PreparedStatement ps = connection.prepareStatement(query1);
	
				ps.setString(1, templateId);
				ps.setString(2, version);
	
				rs = ps.executeQuery();
	
				if (rs.next()) {
	
					int totalUsage = Integer.parseInt(rs.getString("Total_Usage"));
					int successRatio = Integer.parseInt(rs.getString("Success_Ratio"));
					int failureRatio = Integer.parseInt(rs.getString("Failure_Ratio"));
	
					String query = null;
					// double total_success_ratio=successRatio/totalUsage;
					if (result.equalsIgnoreCase("Success")) {
						query = "update template_usage_data set Success_Ratio = ?,total_success_ratio = ?  WHERE templateId = ? and Version = ?";
						PreparedStatement preparedStmt;
	
						preparedStmt = connection.prepareStatement(query);
	
						preparedStmt.setInt(1, ++successRatio);
						double total_success_ratio = ((double) successRatio / (int) totalUsage);
						preparedStmt.setDouble(2, total_success_ratio);
						;
						preparedStmt.setString(3, templateId);
						preparedStmt.setString(4, version);
	
						preparedStmt.executeUpdate();
					} else {
						query = "update template_usage_data set Failure_Ratio = ?,total_success_ratio = ?  WHERE templateId = ? and Version = ?";
						PreparedStatement preparedStmt;
	
						preparedStmt = connection.prepareStatement(query);
	
						preparedStmt.setInt(1, ++failureRatio);
						double total_success_ratio = ((double) successRatio / (int) totalUsage);
						preparedStmt.setDouble(2, total_success_ratio);
						preparedStmt.setString(3, templateId);
						preparedStmt.setString(4, version);
	
						preparedStmt.executeUpdate();
					}
	
				}
			}
		} catch (SQLException e) {
			logger.error("Exception in updateTemplateUsageData method "+e.getMessage());
			e.printStackTrace();
		}

	}

	public String getFailureIssueType(String suggestion) {
		connection = jDBCConnection.getConnection();

		String category = "";
		ResultSet rs = null;

		try {

			String query1 = "SELECT * FROM errorcodedata WHERE suggestion=?";

			PreparedStatement ps = connection.prepareStatement(query1);

			ps.setString(1, suggestion);

			rs = ps.executeQuery();

			while (rs.next()) {
				category = rs.getString("category");
			}
		} catch (SQLException e) {
			logger.error("Exception in getFailureIssueType method "+e.getMessage());
		}
		return category;
	}

	public String getBestTemplate(List<String> templateVersionList, String templateId) {
		connection = jDBCConnection.getConnection();

		String versionWithTemplate = "";
		ResultSet rs = null;
		String query = createQueryForVersion(templateVersionList.size());
		try {

			String query1 = "SELECT * FROM template_usage_data WHERE " + query
					+ " and templateId = ? order by Total_Success_Ratio desc,id desc LIMIT 0,1";

			PreparedStatement ps = connection.prepareStatement(query1);

			ps.setString(1, templateId);
			for (int i = 1; i <= templateVersionList.size(); i++) {
				ps.setString(i, templateVersionList.get(i - 1));
			}
			ps.setString(templateVersionList.size() + 1, templateId);

			rs = ps.executeQuery();
			// List<Double> list=new ArrayList<Double>();

			while (rs.next()) {

				versionWithTemplate = rs.getString("Version");

			}

		} catch (SQLException e) {
			logger.error("Exception in getBestTemplate method "+e.getMessage());
		}

		return templateId + "_V" + versionWithTemplate;
	}

	private String createQueryForVersion(int length) {
		String query = "version in(";
		StringBuilder queryBuilder = new StringBuilder(query);
		for (int i = 0; i < length; i++) {
			queryBuilder.append(" ?");
			if (i != length - 1)
				queryBuilder.append(",");
		}
		queryBuilder.append(")");
		return queryBuilder.toString();
	}

	/* Dhanshri Mane */
	/* Return Attribute and related validation,UIcomponent,Category */
	public List<TemplateAttribPojo> getDynamicAttribDataGrid(String[] feature, String templateId) {

		List<TemplateAttribPojo> templateWithAttrib = new ArrayList<>();

		try {

			List<String> featureList = Arrays.asList(feature);
			List<AttribCreateConfigPojo> attribConfig = null;
			for (String features : featureList) {
				attribConfig = new ArrayList<>();
				if (features.contains("Generic_")) {
					String seriesId = StringUtils.substringAfter(features, "Generic_");
					attribConfig.addAll(service.getByAttribSeriesId(seriesId));
				} else {
					attribConfig.addAll(service.getByAttribTemplateAndFeatureName(templateId, features));
				}
				TemplateAttribPojo templateattrib = new TemplateAttribPojo();
				templateattrib.setName(features);

				List<AttribCreateConfigJson> AttribConfigJson = new ArrayList<AttribCreateConfigJson>();
				/* map byAttribSeriesId List to jsonValue List to return Responce */

				for (AttribCreateConfigPojo attribInfo : attribConfig) {
					AttribCreateConfigJson attribJson = new AttribCreateConfigJson();
					attribJson.setId(attribInfo.getId());
					attribJson.setName(attribInfo.getAttribName());
					attribJson.setLabel(attribInfo.getAttribLabel());
					attribJson.setuIComponent(attribInfo.getAttribUIComponent());
					attribJson.setValidations(attribInfo.getAttribValidations());
					attribJson.setType(attribInfo.getAttribType());
					attribJson.setSeriesId(attribInfo.getAttribSeriesId());
					attribJson.setTemplateId(attribInfo.getAttribTemplateId());
					attribJson.setCategotyLabel(attribInfo.getAttribCategoty());
					/* using Category Name find all category Value */
					if (attribInfo.getAttribCategoty() != null) {
						List<CategoryDropDownPojo> allByCategoryName = categoryDropDownservice
								.getAllByCategoryName(attribInfo.getAttribCategoty());
						attribJson.setCategotyLabel(attribInfo.getAttribCategoty());
						attribJson.setCategory(allByCategoryName);
					}
					AttribConfigJson.add(attribJson);

				}

				templateattrib.setAttribConfig(AttribConfigJson);
				templateWithAttrib.add(templateattrib);

			}

		} catch (Exception e) {
			logger.error("Exception in getDynamicAttribDataGrid method "+e.getMessage());
			e.printStackTrace();
		} finally {

		}
		return templateWithAttrib;
	}

	/* Dhanshri Mane */
	/* Return Attribute and related validation,UIcomponent,Category */
	public List<TemplateAttribPojo> getDynamicAttribDataGridForUI(String[] feature, String templateId) {

		List<TemplateAttribPojo> templateWithAttrib = new ArrayList<>();

		try {

			List<String> featureList = Arrays.asList(feature);
			List<AttribCreateConfigPojo> attribConfig = null;
			for (String features : featureList) {
				attribConfig = new ArrayList<>();
				TemplateAttribPojo templateattrib = new TemplateAttribPojo();

				if (features.contains("Basic Configuration")) {
					String seriesId = StringUtils.substringAfter(features, "Basic Configuration");
					attribConfig.addAll(service.getByAttribSeriesId(seriesId));
					templateattrib.setName("Basic Configuration");
				} else {
					attribConfig.addAll(service.getByAttribTemplateAndFeatureName(templateId, features));
					templateattrib.setName(features);
				}

				List<AttribCreateConfigJson> AttribConfigJson = new ArrayList<AttribCreateConfigJson>();
				/* map byAttribSeriesId List to jsonValue List to return Responce */

				for (AttribCreateConfigPojo attribInfo : attribConfig) {
					AttribCreateConfigJson attribJson = new AttribCreateConfigJson();
					attribJson.setId(attribInfo.getId());
					attribJson.setName(attribInfo.getAttribName());
					attribJson.setLabel(attribInfo.getAttribLabel());
					attribJson.setuIComponent(attribInfo.getAttribUIComponent());
					attribJson.setValidations(attribInfo.getAttribValidations());
					attribJson.setType(attribInfo.getAttribType());
					attribJson.setSeriesId(attribInfo.getAttribSeriesId());
					attribJson.setTemplateId(attribInfo.getAttribTemplateId());
					attribJson.setCategotyLabel(attribInfo.getAttribCategoty());
					/* using Category Name find all category Value */
					if (attribInfo.getAttribCategoty() != null) {
						List<CategoryDropDownPojo> allByCategoryName = categoryDropDownservice
								.getAllByCategoryName(attribInfo.getAttribCategoty());
						attribJson.setCategotyLabel(attribInfo.getAttribCategoty());
						attribJson.setCategory(allByCategoryName);
					}
					AttribConfigJson.add(attribJson);

				}

				templateattrib.setAttribConfig(AttribConfigJson);
				templateWithAttrib.add(templateattrib);

			}

		} catch (Exception e) {
			logger.error("Exception in getDynamicAttribDataGridForUI method "+e.getMessage());
			e.printStackTrace();
		} finally {

		}
		return templateWithAttrib;
	}

	@SuppressWarnings("unchecked")
	public JSONObject getBasicDeatilsOfTemplate(String templateId, String version) {
		connection = jDBCConnection.getConnection();
		JSONObject basicDetails = new JSONObject();
		ResultSet rs = null;
		List<String> versionList = new ArrayList<>();
		try {
			String query1 = "SELECT * FROM templateconfig_basic_details WHERE temp_id=? And temp_version=?";
			String query2 = "SELECT * FROM templateconfig_basic_details WHERE temp_id=? order by temp_version asc;";

			PreparedStatement ps = connection.prepareStatement(query1);
			ps.setString(1, templateId);
			ps.setString(2, version);
			rs = ps.executeQuery();

			PreparedStatement ps2 = connection.prepareStatement(query2);
			ps2.setString(1, templateId);

			String status = null;
			while (rs.next()) {
				basicDetails.put("vendor", rs.getString("temp_vendor"));
				basicDetails.put("deviceFamily", rs.getString("temp_device_family"));
				basicDetails.put("model", rs.getString("temp_model"));
				basicDetails.put("region", rs.getString("temp_region"));
				basicDetails.put("os", rs.getString("temp_device_os"));
				basicDetails.put("osVersion", rs.getString("temp_os_version"));
				basicDetails.put("creationDate", rs.getString("temp_created_date"));
				basicDetails.put("raisedBy", rs.getString("temp_created_by"));
				basicDetails.put("networkType", rs.getString("temp_network_type"));
				basicDetails.put("alias", rs.getString("temp_alias"));
				basicDetails.put("isGoldenTemplate", rs.getBoolean("temp_golden"));
				String comment = rs.getString("temp_comment_section");				
				basicDetails.put("comment", comment);
				status = rs.getString("temp_status");
			}
			rs = ps2.executeQuery();
			while (rs.next()) {
				versionList.add(rs.getString("temp_version"));
			}
			int size = versionList.size();
			String dbVersion = versionList.get(size - 1);

			if (version.equals(dbVersion)) {
				if (status.equals("Approved")) {
					basicDetails.put("isEditable", true);
				} else {
					basicDetails.put("isEditable", false);
				}
			} else {
				basicDetails.put("isEditable", false);
			}
			basicDetails.put("verionsList", versionList);

		} catch (SQLException e) {
			logger.error("Exception in getBasicDeatilsOfTemplate method "+e.getMessage());
		}
		return basicDetails;
	}

	public List<String> getFeatureList(String tempId) throws SQLException {
		connection = jDBCConnection.getConnection();
		ResultSet rs = null;
		PreparedStatement preparedStmt = null;
		String query = null;
		List<String> featureList = new ArrayList<String>();

		try {
			query = "SELECT * FROM c3p_template_master_feature_list where is_Save=\"1\" and command_type=?";
			preparedStmt = connection.prepareStatement(query);
			preparedStmt.setString(1, tempId);

			rs = preparedStmt.executeQuery();
			while (rs.next()) {
				featureList.add(rs.getString("comand_display_feature"));
			}
			preparedStmt.close();
		} finally {
			DBUtil.close(rs);
			DBUtil.close(preparedStmt);
			DBUtil.close(connection);
		}
		return featureList;
	}
	
	public List<String> getListOfFeaturesForDeviceDetail(String tempId, String networkType, String requestType) throws SQLException {
		connection = jDBCConnection.getConnection();
		ResultSet rs = null;
		PreparedStatement preparedStmt = null;
		String query = null;
		String query1 = null;
		List<String> featureList = new ArrayList<String>();

		List<Integer> featuresChecked = new ArrayList<Integer>();
		try {

			query = "Select distinct id from c3p_template_transaction_feature_list where command_feature_template_id in(select CONCAT(temp_id, '_V', temp_version) from templateconfig_basic_details where temp_status='Approved' and temp_id LIKE ?)";
			String query2 = "SELECT * FROM templateconfig_basic_details WHERE temp_id LIKE ?";

			preparedStmt = connection.prepareStatement(query2);
			preparedStmt.setString(1, tempId+"%");

			rs = preparedStmt.executeQuery();
			Set<String> networkTypeList = new HashSet<>();
			while (rs.next()) {
				networkTypeList.add(rs.getString("temp_network_type"));
			}
			preparedStmt.close();
			if (networkTypeList.size() == 1) {
				for (String data : networkTypeList) {
					if (data.equals(networkType)) {
						preparedStmt = connection.prepareStatement(query);
						preparedStmt.setString(1, tempId+"%");

						rs = preparedStmt.executeQuery();
						while (rs.next()) {
							featuresChecked.add(rs.getInt("id"));
							// itemsToAdd.add(rs.getString("tempCmd.Parent_name"));

						}
						preparedStmt.close();
						query1 = "select * from c3p_template_master_feature_list where id=?";
						for (int i = 0; i < featuresChecked.size(); i++) {

							preparedStmt = connection.prepareStatement(query1);

							preparedStmt.setInt(1, featuresChecked.get(i));
							rs = preparedStmt.executeQuery();
							while (rs.next()) {
								boolean isPresent = false;
								if (rs.getInt("hasParent") == 1) {
									if (featureList.size() > 0) {
										for (int j = 0; j < featureList.size(); j++) {
											if (rs.getString("command_parent_feature")
													.equalsIgnoreCase(featureList.get(j))) {
												isPresent = true;
												break;
											}
										}
										if (!isPresent) {
											featureList.add(rs.getString("command_parent_feature"));

										}
									} else {
										featureList.add(rs.getString("command_parent_feature"));
									}
								} else if (rs.getInt("hasParent") == 0) {
									featureList.add(rs.getString("comand_display_feature"));
								}
							}
						}
					}

				}
			}

		} finally {
			DBUtil.close(rs);
			DBUtil.close(preparedStmt);
			DBUtil.close(connection);
		}
		return featureList;
	}
	
	
}
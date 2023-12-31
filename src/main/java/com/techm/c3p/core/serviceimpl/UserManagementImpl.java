package com.techm.c3p.core.serviceimpl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techm.c3p.core.entitybeans.DeviceDiscoveryEntity;
import com.techm.c3p.core.entitybeans.DeviceGroups;
import com.techm.c3p.core.entitybeans.PasswordPolicy;
import com.techm.c3p.core.entitybeans.SiteInfoEntity;
import com.techm.c3p.core.entitybeans.UserManagementEntity;
import com.techm.c3p.core.entitybeans.UserRole;
import com.techm.c3p.core.entitybeans.WorkGroup;
import com.techm.c3p.core.exception.GenericResponse;
import com.techm.c3p.core.pojo.UserManagementResulltDetailPojo;
import com.techm.c3p.core.repositories.DeviceDiscoveryRepository;
import com.techm.c3p.core.repositories.DeviceGroupRepository;
import com.techm.c3p.core.repositories.ErrorValidationRepository;
import com.techm.c3p.core.repositories.SiteInfoRepository;
import com.techm.c3p.core.repositories.UserManagementRepository;
import com.techm.c3p.core.repositories.WorkGroupRepository;
import com.techm.c3p.core.service.UserManagementInterface;
import com.techm.c3p.core.utility.PasswordEncryption;
import com.techm.c3p.core.utility.UserEncryption;

@Service
public class UserManagementImpl implements UserManagementInterface {
	private static final Logger logger = LogManager.getLogger(UserManagementImpl.class);
	private static final int MAX_ATTEMPTS = 3;

	@Autowired
	private UserManagementRepository userManagementRepository;

	@Autowired
	private SiteInfoRepository siteInfoRepository;

	@Autowired
	private UserManagementEntity userManagementEntity;

	@Autowired
	private PasswordEncryption passEncrypt;
	
	@Autowired
	private WorkGroupRepository workGroupRepository;
	
	@Autowired
	private DeviceDiscoveryRepository deviceDiscoveryRepository;
	
	@Autowired
	private DeviceGroupRepository deviceGroupRepository;
	
	@Autowired
	private UserEncryption userEncryption;
	
	@Autowired
	private ErrorValidationRepository errorValidationRepository;

	/*
	 * Create service for create user for user management
	 */
	@Override
	public GenericResponse createUser(String userData) throws Exception {
		GenericResponse res = new GenericResponse();

		logger.info("Inside createUser Service");
		logger.info("Inside createUser user data - > " + userData);
		final String secretKey = "aaabbbbbbbbbbb!!!!";
		JSONParser parser = new JSONParser();
		JSONObject module = new JSONObject();
		JSONObject deviceGroup = new JSONObject();
		JSONObject customerAndSites = null;
		//JSONObject workGroup = new JSONObject();
		String userName = "", role = "", firstName = "", lastName = "", email = "", password = "", timezone = "",
				baseLocation = "", authentication="", manager = "",  userId = null,
				address = null,  workGroupName = null, encryptedPass = null,
				phone =null, mobile =null, workGroup=null;
		long id = 0;
		List<UserManagementEntity> userManager = null;
		UserManagementEntity userCreate2 = null;

		try {
			JSONObject json = (JSONObject) parser.parse(userData);
			customerAndSites = new JSONObject();
			if(json !=null && json.get("role") !=null)
				role = (String) json.get("role");
			WorkGroup workGroupEntity = workGroupRepository.findByDefaultRole(role);
			/*
			 * Logic for enoc_user when no work group is selected if workgroup
			 * json will be empty It will work when
			 * "workGroup":{"workgroupname":""},
			 */
			workGroup = (String) json.get("workGroup");
			if (workGroup.isEmpty() && workGroupEntity !=null) {
				workGroupName = workGroupEntity.getWorkGroupName();
			} 
			else if(workGroup.isEmpty())
				workGroupName = "";
			else
				workGroup = (String) json.get("workGroup");
			module = (JSONObject) json.get("module");
			role = (String) json.get("role");
			userName = (String) json.get("userName");
			firstName = (String) json.get("firstName");
			lastName = (String) json.get("lastName");
			email = (String) json.get("email");
			phone = (String) json.get("phone");
			mobile = (String) json.get("mobile");
			encryptedPass = userEncryption.encryptPassword(userName, secretKey);
			timezone = (String) json.get("timezone");
			address = (String) json.get("address");
			baseLocation = (String) json.get("baseLocation");
			authentication = (String) json.get("authentication");
			manager = (String) json.get("manager");
			userId = (String) json.get("id");
			//deviceGroup = (JSONObject) json.get("deviceGroup");
			JSONArray custRegionSite = (JSONArray) json.get("customerAndSites");
			
			List<DeviceDiscoveryEntity> userDevicesList = null;
			List<DeviceGroups> userDeviceGroupList =null;
			JSONArray userDevices = (JSONArray) json.get("userDevices");
			JSONArray userDeviceGroups = (JSONArray) json.get("userDeviceGroups");
			
			if(userDevices !=null && !userDevices.isEmpty())
				userDevicesList = deviceDiscoveryRepository.findByDId(userDevices);
			if(userDeviceGroups !=null && !userDeviceGroups.isEmpty())
				userDeviceGroupList = deviceGroupRepository.findById(userDeviceGroups);

			userManagementEntity.setRole(role);
			userManagementEntity.setId(id);
			userManagementEntity.setFirstName(firstName);
			userManagementEntity.setLastName(lastName);
			userManagementEntity.setEmail(email);
			userManagementEntity.setCurrentPassword(encryptedPass);
			userManagementEntity.setPhone(phone);
			userManagementEntity.setMobile(mobile);
			userManagementEntity.setManagerName(manager);
			userManagementEntity.setTimeZone(timezone);
			userManagementEntity.setStatus("active");
			userManagementEntity.setAuthentication(authentication);
			userManagementEntity.setBaseLocation(baseLocation);
			userManagementEntity.setAddress(address);
			userManagementEntity.setUserName(userName);
			userManagementEntity.setModuleInfo(module.toString());
			//userManagementEntity.setDeviceGroup(deviceGroup.toString());
			userManagementEntity.setDeviceDetails(userDevicesList);
			userManagementEntity.setDeviceGroups(userDeviceGroupList);
			
			if (role.equals("enoc_user"))
				userManagementEntity.setCustomerSites(custRegionSite.toString());
			else
				userManagementEntity.setCustomerSites(null);

			// It will work when "workGroup":{"workgroupname":""},
			if (workGroup.isEmpty())
				userManagementEntity.setWorkGroup(workGroupName);
			else
				userManagementEntity.setWorkGroup(workGroup.toString());

			String isUserExist = userManagementRepository.isUserNameExist(userName);
			userManager = userManagementRepository.findByUserName(manager);
			if (!userManager.isEmpty()) {
				userCreate2 = userManager.get(0);
				if (userCreate2 != null) {
					String manager1 = userCreate2.getManagerName();
					if (manager1 != null && !manager1.isEmpty()) {
						String userName2 = userManagementEntity.getUserName();
						String sub = userCreate2.getSubOrdinate();

						if (sub != null) {
							sub = sub + "," + userName2;
						} else {
							sub = userName2;
						}
						userCreate2.setSubOrdinate(sub);
						if (isUserExist == null)
							userManagementRepository.saveAndFlush(userCreate2);
					} else {
						if (!manager.isEmpty()) {
							String sub = userCreate2.getSubOrdinate();
							if (sub != null) {
								sub = sub + "," + userName;
								userCreate2.setSubOrdinate(sub);
							} else
								userCreate2.setSubOrdinate(userName);
							if (isUserExist == null)
								userManagementRepository.saveAndFlush(userCreate2);
						}
					}
				}
			}
			UserManagementEntity savedUser = userManagementRepository.save(userManagementEntity);
			res.put("responseResult", "updated");
		} catch (NoSuchElementException e) {
			logger.error("\n" + "exceptin of Added User for User Management Service" + e.getStackTrace(), e);
			res.put(GenericResponse.ERROR_TEXT, "element cant be createUser");
		} catch (Exception e) {
			logger.error("exception ofAdded User for User Management Service is...." + e.getMessage());
			res.put(GenericResponse.ERROR_TEXT, e.getCause().getCause().getMessage());
		}
		logger.info("\n" + "out of Added User for User Management Service");
		return res;

	}

	@Override
	public GenericResponse deleteById(Integer userId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GenericResponse getAllUser() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getManagerName(String userName) throws Exception {
		String nameOfManager = userManagementRepository.findByManagerName(userName);
		return nameOfManager;
	}

	@Override
	public GenericResponse updateDataById(String data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserManagementEntity> getAllSubOrdinate() throws Exception {
		List<UserManagementEntity> subOrdinatelist = userManagementRepository.findAllSubOrdinate();
		return subOrdinatelist;
	}

	@Override
	public List<UserRole> getAllRole() throws Exception {
		List<UserRole> rolelist = userManagementRepository.findAllRole();
		return rolelist;
	}

	@Override
	public List<UserManagementEntity> getAllManager() throws Exception {
		List<UserManagementEntity> managerList = userManagementRepository.findAllManager();
		return managerList;
	}

	/*
	 * Create service for view user specific details for user management
	 */
	@SuppressWarnings("unchecked")
	@Override
	public GenericResponse getUserView(String userName) throws Exception {
		GenericResponse response = new GenericResponse();
		JSONParser parser = new JSONParser();
		JSONObject jsonModule = null;
		JSONArray jsonCustAndSites = new JSONArray();
		Object obj =null;
		List<UserManagementEntity> userDetails = userManagementRepository.findUserDetails(userName);
		UserManagementEntity userInfo = userDetails.get(0);

		// Get moduleInfo and CustomerAndSites details
		String moduleDetails = userInfo.getModuleInfo();
		String customerAndSitesDetails = userInfo.getCustomerSites();
		if (moduleDetails != null)
			jsonModule = (JSONObject) parser.parse(moduleDetails);
		if (customerAndSitesDetails != null)
		{
			obj =  parser.parse(customerAndSitesDetails);
			jsonCustAndSites.add(obj);
		}	
		String managerId = userManagementRepository.findOneByManagerName(userName);
		String managerName = userManagementRepository.findManagerName(managerId);
		response.put("userDetails", userDetails);
		response.put("managerId", managerId);
		response.put("managerName", managerName);
		if (jsonModule != null)
			response.put("module", jsonModule);
		if (jsonCustAndSites != null)
			response.put("customerAndSites", jsonCustAndSites);
		return response;
	}

	@Override
	public List<SiteInfoEntity> getCustomerDetails() throws Exception {
		List<SiteInfoEntity> customerDetails = siteInfoRepository.findCustomerDetails();
		return customerDetails;
	}

	@Override
	public List<SiteInfoEntity> getSitesDetails(List custName, List regionName) throws Exception {
		List<SiteInfoEntity> sitesDetails = siteInfoRepository.findSitesDetailsInfo(custName, regionName);
		return sitesDetails;
	}

	@Override
	public List<SiteInfoEntity> getRegionDetails(List custName) throws Exception {
		List<SiteInfoEntity> regionDetails = siteInfoRepository.findRegionDetails(custName);
		return regionDetails;
	}

	/*
	 * Create service for update user for user management
	 */
	@Override
	public GenericResponse updateUser(String userData) throws Exception {
		GenericResponse res = new GenericResponse();

		logger.info("\n" + "Inside updateUser Service");
		logger.info("\n" + "data=" + userData);
		JSONParser parser = new JSONParser();
		JSONObject module = new JSONObject();
		JSONObject deviceGroup = new JSONObject();
		String userName = "", role = "", firstName = "", lastName = "", email = "",  timezone = "",status = "", manager = "",
			     assignManager = null, phone =null, mobile =null, workGroup=null, address=null, baseLocation=null, authentication=null;
		long userId = 0;

		try {
			JSONObject json = (JSONObject) parser.parse(userData);
			if(json !=null && json.get("role") !=null)
				role = (String) json.get("role");
			WorkGroup workGroupEntity = workGroupRepository.findByDefaultRole(role);
			
			workGroup = (String) json.get("workGroup");
			module = (JSONObject) json.get("module");
			role = (String) json.get("role");
			userName = (String) json.get("userName");
			firstName = (String) json.get("firstName");
			lastName = (String) json.get("lastName");
			email = (String) json.get("email");
			phone = (String) json.get("phone");
			mobile = (String) json.get("mobile");
			address = (String) json.get("address");
			baseLocation = (String) json.get("baseLocation");
			authentication = (String) json.get("authentication");
			timezone = (String) json.get("timezone");
			status = (String) json.get("status");
			manager = (String) json.get("manager");
			assignManager = (String) json.get("assignManager");
			userId = (long) json.get("id");
			//deviceGroup = (JSONObject) json.get("deviceGroup");
			JSONArray custRegionSite = (JSONArray) json.get("customerAndSites");
			
			List<DeviceDiscoveryEntity> userDevicesList = null;
			List<DeviceGroups> userDeviceGroupList =null;
			JSONArray userDevices = (JSONArray) json.get("userDevices");
			JSONArray userDeviceGroups = (JSONArray) json.get("userDeviceGroups");
			
			if(userDevices !=null && !userDevices.isEmpty())
				userDevicesList = deviceDiscoveryRepository.findByDId(userDevices);
			if(userDeviceGroups !=null && !userDeviceGroups.isEmpty())
				userDeviceGroupList = deviceGroupRepository.findById(userDeviceGroups);

			UserManagementEntity userDetails = userManagementRepository.findById(userId);

			if (!role.isEmpty())
				userDetails.setRole(role);

			if (!firstName.isEmpty())
				userDetails.setFirstName(firstName);

			if (!lastName.isEmpty())
				userDetails.setLastName(lastName);

			if (!email.isEmpty())
				userDetails.setEmail(email);

			if (!phone.isEmpty())
				userDetails.setPhone(phone);

			if (!mobile.isEmpty())
				userDetails.setMobile(mobile);

			if (!manager.isEmpty())
				userDetails.setManagerName(manager);

			 if(!timezone.isEmpty())
				 userDetails.setTimeZone(timezone);
			 
			if (!status.isEmpty())
				userDetails.setStatus(status);

			if (manager.isEmpty() && !assignManager.isEmpty())
					userDetails.setManagerName(assignManager);

			if (!module.isEmpty())
				userDetails.setModuleInfo(module.toString());

			if (!deviceGroup.isEmpty())
				userDetails.setDeviceGroup(deviceGroup.toString());

			if (!custRegionSite.isEmpty() && role.equals("enoc_user"))
				userDetails.setCustomerSites(custRegionSite.toString());
			else
				userDetails.setCustomerSites(null);

			if (!workGroup.isEmpty())
				userDetails.setWorkGroup(workGroup.toString());
			else if(workGroup.isEmpty() && workGroupEntity !=null)
				userDetails.setWorkGroup(workGroupEntity.getWorkGroupName());
			else
				userDetails.setWorkGroup("");
			
			if (!authentication.isEmpty())
				userDetails.setAuthentication(authentication);
			
			if (!address.isEmpty())
				userDetails.setAddress(address);	
			
			if (!baseLocation.isEmpty())
				userDetails.setBaseLocation(baseLocation);
			
			userDetails.setDeviceDetails(userDevicesList);
			userDetails.setDeviceGroups(userDeviceGroupList);

			if (status.equalsIgnoreCase("active")) {
				String managerNameFromUI = userDetails.getManagerName();
				String managerNameFromDB = userManagementRepository.findOneByManagerName(userName);
				// Get user manager

				if (!managerNameFromUI.equalsIgnoreCase(managerNameFromDB)) {

					// Finding username in database based on manager name came
					// from Database
					List<UserManagementEntity> userManagersFromDB = userManagementRepository
							.findByUserName(managerNameFromDB);
					// Finding username in database based on manager name came
					// from UI/JSON
					List<UserManagementEntity> userManagersFromUI = userManagementRepository
							.findByUserName(managerNameFromUI);

					UserManagementEntity user1 = userManagersFromDB.get(0);
					UserManagementEntity user2 = userManagersFromUI.get(0);

					// Get previvous subordinate
					String subordinate1 = user1.getSubOrdinate();
					String subordinate2 = user2.getSubOrdinate();
					String userSubordinate1 = "";
					String userSubordinate2 = "";
					if (subordinate1 != null) {
						String subordiateArr[] = subordinate1.split(",");
						for (String subordinateDetails : subordiateArr) {
							if (!subordinateDetails.equals(userName)) {
								if (userSubordinate1.isEmpty())
									userSubordinate1 = subordinateDetails;
								else
									userSubordinate1 = userSubordinate1 + "," + subordinateDetails;
							}
						}
					}
					if (subordinate2 != null) {
						if (subordinate2.isEmpty())
							userSubordinate2 = userName;
						else
							userSubordinate2 = subordinate2 + "," + userName;
					}
					if (subordinate2 == null)
						userSubordinate2 = userName;
					// update previvous manager sub
					user1.setSubOrdinate(userSubordinate1);
					user2.setSubOrdinate(userSubordinate2);
				}
			} else if (status.equalsIgnoreCase("inactive") && !manager.isEmpty()) {
				List<UserManagementEntity> managerNameFromUI = userManagementRepository.findByUserName(userName);

				List<UserManagementEntity> managerNameFromDB = userManagementRepository.findByUserName(manager);

				UserManagementEntity user1 = managerNameFromUI.get(0);
				UserManagementEntity user2 = managerNameFromDB.get(0);
				// Get previvous subordinate
				String subordinate1 = user1.getSubOrdinate();
				String subordinate2 = user2.getSubOrdinate();
				String userSubordinate1 = "";
				String userSubordinate2 = "";
				if (subordinate1 != null) {
					String subordiateArr[] = subordinate1.split(",");
					for (String subordiateDetails : subordiateArr) {
						if (!subordiateDetails.equals(userName)) {
							if (userSubordinate1.isEmpty())
								userSubordinate1 = subordiateDetails;
							else
								userSubordinate1 = userSubordinate1 + "," + subordiateDetails;
						}
					}
				}
				if (subordinate2 != null) {
					if (subordinate2.isEmpty())
						userSubordinate2 = userName;
					else
						userSubordinate2 = subordinate2 + "," + userSubordinate1;
				}
				if (subordinate2 == null)
					userSubordinate2 = userSubordinate1;
				// update previvous manager sub
				user1.setSubOrdinate("");
				user2.setSubOrdinate(userSubordinate2);
			}

			else {
				List<UserManagementEntity> managerNameFromUI = userManagementRepository.findByUserName(userName);

				List<UserManagementEntity> managerNameFromDB = userManagementRepository.findByUserName(assignManager);

				UserManagementEntity user1 = managerNameFromUI.get(0);
				UserManagementEntity user2 = managerNameFromDB.get(0);
				// Get previvous subordinate
				String subordinate1 = user1.getSubOrdinate();
				String subordinate2 = user2.getSubOrdinate();
				String userSubordinate1 = "";
				String userSubordinate2 = "";
				if (subordinate1 != null) {
					String subordiateArr[] = subordinate1.split(",");
					for (String subordiateDetails : subordiateArr) {
						if (!subordiateDetails.equals(userName)) {
							if (userSubordinate1.isEmpty())
								userSubordinate1 = subordiateDetails;
							else
								userSubordinate1 = userSubordinate1 + "," + subordiateDetails;
						}
					}
				}
				if (subordinate2 != null) {

					if (subordinate2.isEmpty())
						userSubordinate2 = userName;
					else
						userSubordinate2 = subordinate2 + "," + userSubordinate1;
				}
				if (subordinate2 == null)
					userSubordinate2 = userSubordinate1;
				// update previvous manager sub
				user1.setSubOrdinate("");
				user2.setSubOrdinate(userSubordinate2);
			}
			UserManagementEntity savedUser = userManagementRepository.save(userDetails);
			res.put("responseResult", "updated");
		} catch (NoSuchElementException e) {
			logger.error("\n" + "exceptin of updateUser User for User Management Service" + e.getStackTrace(), e);
			res.put(GenericResponse.ERROR_TEXT, "element cant be createUser");
		} catch (Exception e) {
			logger.error("exception of updateUser User for User Management Service is...." + e.getMessage());
			res.put(GenericResponse.ERROR_TEXT, e.getMessage());
		}
		logger.info("\n" + "out of updateUser User for User Management Service");
		return res;

	}

	@Override
	public List<PasswordPolicy> getPasswordPolicy() throws Exception {
		List<PasswordPolicy> policyList = userManagementRepository.findPasswordPlocyDetails();
		return policyList;
	}

	@Override
	public List<UserManagementEntity> getAllUserView() throws Exception {
		List<UserManagementEntity> userAllDetails = userManagementRepository.findByAllUser();
		return userAllDetails;
	}

	@Override
	public UserManagementResulltDetailPojo checkUserNamePassword(String userName, String currentPassword, final String secretKey) throws Exception {
		String encryptedPass = getEncryptPassword(userName, currentPassword, secretKey);	
		String userPass = userManagementRepository.findByUserNameCurrentPassword(userName, encryptedPass);
		UserManagementEntity userDetails = getUserAttempts(userName);
		UserManagementResulltDetailPojo userManagementResulltDetailPojo = new UserManagementResulltDetailPojo();
		JSONObject jsonModule = null;
		JSONParser parser = new JSONParser();
		String user = null;
		String pass = null;
		if (userPass != null) {
			user = userPass.split(",")[0];
			pass = userPass.split(",")[1];
		}

		if (userName != null && !userName.isEmpty() && currentPassword != null && !currentPassword.isEmpty()) {

			if (userName.equalsIgnoreCase(user) && encryptedPass.equalsIgnoreCase(pass)) {
				logger.info("correct password");
				boolean didLogin = setUserLoginFlag(userName,encryptedPass, userDetails.getStatus());
				if (didLogin) {
					userManagementResulltDetailPojo.setMessage("Success");
					userManagementResulltDetailPojo.setResult(true);
					userManagementResulltDetailPojo.setRole(userDetails.getRole());
					userManagementResulltDetailPojo.setFirstName(userDetails.getFirstName());
					userManagementResulltDetailPojo.setLastName(userDetails.getLastName());
					userManagementResulltDetailPojo.setWorkGroup(userDetails.getWorkGroup());
					userManagementResulltDetailPojo.setUserName(userDetails.getUserName());
					userManagementResulltDetailPojo.setBaseLocation(userDetails.getBaseLocation());
					jsonModule = (JSONObject) parser.parse(userDetails.getModuleInfo());
					userManagementResulltDetailPojo.setModuleInfo(jsonModule);
					if(userDetails.getSubOrdinate() !=null) {
						userManagementResulltDetailPojo.setSuperUser(true);
					}
					else {
						userManagementResulltDetailPojo.setSuperUser(false);
					}
					userManagementRepository.resetFailAttempts(userName);					
					userDetails.setLastLoginDate(new Date());
					userManagementRepository.save(userDetails);				
				}
				else
				{
					userManagementResulltDetailPojo.setMessage("Your account is inactive, Please contact your Administrator ");
					userManagementResulltDetailPojo.setResult(false);
				}
			} else {

				logger.info("wrong password");
				userManagementResulltDetailPojo.setMessage("Failure");
				userManagementResulltDetailPojo.setResult(false);
				if (userDetails != null) {
					userManagementResulltDetailPojo.setRole(userDetails.getRole());
					userManagementResulltDetailPojo.setFirstName(userDetails.getFirstName());
					userManagementResulltDetailPojo.setLastName(userDetails.getLastName());
					userManagementResulltDetailPojo.setWorkGroup(userDetails.getWorkGroup());
					userManagementResulltDetailPojo.setUserName(userDetails.getUserName());
					userManagementResulltDetailPojo.setBaseLocation(userDetails.getBaseLocation());
					jsonModule = (JSONObject) parser.parse(userDetails.getModuleInfo());
					userManagementResulltDetailPojo.setModuleInfo(jsonModule);
					if (userDetails.getSubOrdinate() != null)
						userManagementResulltDetailPojo.setSuperUser(true);
					else
						userManagementResulltDetailPojo.setSuperUser(false);

					if (userDetails.getAttempts() >= MAX_ATTEMPTS)
						userManagementResulltDetailPojo.setMessage("User account is locked!");
					updateFailAttempts(userName);
				}
				userManagementResulltDetailPojo.setMessage("Invalid username or password");
			}
		}
		return userManagementResulltDetailPojo;
	}

	public void updateFailAttempts(String userName) {

		UserManagementEntity user = getUserAttempts(userName);
		if (user != null) {

			if (isUserExists(userName)) {
				// update attempts count, +1
				userManagementRepository.attemptsUpdate(new Date(), userName);
			}
			if (user.getAttempts() + 1 >= MAX_ATTEMPTS) {
				// locked user
				userManagementRepository.userLocked(true, userName);
			}
		}
	}

	public UserManagementEntity getUserAttempts(String userName) {
		UserManagementEntity userAttempts = null;
		try {
			List<UserManagementEntity> u1 = userManagementRepository.getUserAttempts(userName);
			userAttempts = u1.get(0);
			return userAttempts;
		} catch (Exception e) {
			logger.error("Exception in getUserAttempts method "+e.getMessage());
		}
		return userAttempts;
	}

	private boolean isUserExists(String userName) {
		boolean result = false;
		int count = userManagementRepository.isUserExists(userName);
		if (count > 0) {
			result = true;
		}
		return result;
	}

	@Override
	public int resetPassword(String userName) {
		int success = 0;
		if (!userName.isEmpty()) {
			List<UserManagementEntity> userDetails = userManagementRepository.findByUserName(userName);
			if (!userDetails.isEmpty())
				success = userManagementRepository.resetFailAttempts(userName);
		}
		return success;
	}

	@Override
	public int activeDeletedUser(String status, String userName) {
		int success = 0;
		if (!userName.isEmpty()) {
			List<UserManagementEntity> userDetails = userManagementRepository.findByUserName(userName);
			if (!userDetails.isEmpty())
				success = userManagementRepository.activeUser(status, userName);
		}
		return success;
	}

	@Override
	public int countActiveUser() {
		int activeUserCount = 0;
		activeUserCount = userManagementRepository.countActiveUser();
		return activeUserCount;
	}

	@Override
	public int countInActiveUser() {
		int inActiveUserCount = 0;
		inActiveUserCount = userManagementRepository.countInActiveUser();
		return inActiveUserCount;
	}
	
	/*
	 * When user become inactive then all its sub ordinate will assign to just immediate super supervisor 
	 * lockAndUnlockUser(java.lang.String, java.lang.String)
	 */
	@Override
	public int lockAndUnlockUser(String userName, String action) {
		List<UserManagementEntity> userDetails =null, userManagerDetails=null;
		UserManagementEntity userInfo =null, managerInfo=null;
		String subordinateInfo =null, userManager=null, managerSubordinateInfo=null, userSubordinate="", managerSubordinate="";
		int success = 0;
		
		if (!userName.isEmpty()) {
			userDetails = userManagementRepository.findByUserName(userName);
			userInfo = userDetails.get(0);
			subordinateInfo = userInfo.getSubOrdinate();		
			userManager = userInfo.getManagerName();
			
			if(subordinateInfo ==null && !userDetails.isEmpty())
				success = userManagementRepository.activeUser(action, userName);
			else
			{
				if(userManager !=null && action.equalsIgnoreCase("inactive"))
				{
					userManagerDetails = userManagementRepository.findByUserName(userManager);
					managerInfo = userManagerDetails.get(0);
					managerSubordinateInfo = managerInfo.getSubOrdinate();
				}
				else
				{
					userManagerDetails = userManagementRepository.findByUserName("admin");
					managerInfo = userManagerDetails.get(0);
					managerSubordinateInfo = managerInfo.getSubOrdinate();
				}
				
				if (subordinateInfo != null && action.equalsIgnoreCase("inactive")) {
					String subordiateArr[] = subordinateInfo.split(",");
					for (String subordiateDetails : subordiateArr) {
						if (!subordiateDetails.equals(userName)) {
							if (userSubordinate.isEmpty())
								userSubordinate = subordiateDetails;
							else
								userSubordinate = userSubordinate + "," + subordiateDetails;
						}
					}
				}
				if (managerSubordinateInfo != null && action.equalsIgnoreCase("inactive")) {

					if (managerSubordinateInfo.isEmpty())
						managerSubordinate = userName;
					else
						managerSubordinate = managerSubordinateInfo + "," + userSubordinate;
				}
				
				if (managerSubordinateInfo == null && action.equalsIgnoreCase("inactive"))
					managerSubordinate = userSubordinate;
				
				// update previvous manager sub
				
				if (!userDetails.isEmpty()){
					success = userManagementRepository.activeUser(action, userName);
					if (success>0 && action.equalsIgnoreCase("inactive"))
					{
						userManagementRepository.userSubordinate(userName);
						userManagementRepository.managerSubordinate(managerSubordinate, userManager);
					}
				}
			}
		}
		return success;
	}

	@Override
	public boolean resetUsersDB(String userName) {
		boolean result = false;
		List<UserManagementEntity> resetStatus = userManagementRepository.findByUserName(userName);
		try {
			if (!resetStatus.isEmpty()) 
				result = true;
		} catch (Exception exe) {
			logger.error("Exception in resetUsersDB method " + exe.getMessage());
		}
		return result;
	}

	@Override
	public boolean setUserLoginFlag(String userName, String password, String status) {
		boolean result = false;
		UserManagementEntity updateStatus = null;
		if("active".equalsIgnoreCase(status))
				updateStatus = userManagementRepository.findByUserNameAndCurrentPassword(userName, password);
		try {
			if (updateStatus != null)
				result = true;
		} catch (Exception exe) {
			logger.error("Exception in checkUsersDB method " + exe.getMessage());
		}
		return result;
	}
	
	/*
	 * Create Method for getting user devices in user management
	 */
	@SuppressWarnings("unchecked")
	@Override
	public JSONArray getUserDevices(long userId) {
		logger.info("Inside getUserDevices method ->" + userId);
		JSONArray currentDevices = new JSONArray();
		List<DeviceDiscoveryEntity> currentDevicesList = null;
		UserManagementEntity userDetails = userManagementRepository.findOneById(userId);
		if(userDetails !=null)
			currentDevicesList = userDetails.getDeviceDetails();
		if (currentDevicesList != null && !currentDevicesList.isEmpty()) {
			currentDevicesList.forEach(device -> {
				JSONObject userDevicesJson = new JSONObject();
				userDevicesJson.put("device", device.getdHostName() + "(" + device.getdMgmtIp() + ")");
				userDevicesJson.put("decomm", device.getdDeComm());
				userDevicesJson.put("deviceId", device.getdId());
				currentDevices.add(userDevicesJson);
			});
		}
		logger.info("End of getUserDevices method ->" + currentDevices);
		return currentDevices;
	}

	/*
	 * Create method for getting user device groups in user management
	 */
	@SuppressWarnings("unchecked")
	@Override
	public JSONArray getUserDeviceGroups(long userId) {
		logger.info("Inside getUserDeviceGroups method ->" + userId);
		JSONArray currentDeviceGroups = new JSONArray();
		List<DeviceGroups> deviceGroupList  = null;
		UserManagementEntity userDetails = userManagementRepository.findOneById(userId);
		if(userDetails !=null)
			deviceGroupList= userDetails.getDeviceGroups();
		if (deviceGroupList != null && !deviceGroupList.isEmpty()) {
			deviceGroupList.forEach(group -> {
				JSONObject userDeviceGroupsJson = new JSONObject();
				userDeviceGroupsJson.put("groupName", group.getDeviceGroupName());
				userDeviceGroupsJson.put("groupId", group.getId());
				userDeviceGroupsJson.put("isActive", group.isActive());
				currentDeviceGroups.add(userDeviceGroupsJson);
			});
		}
		logger.info("End of getUserDevices method ->" + currentDeviceGroups);
		return currentDeviceGroups;
	}
	
	@SuppressWarnings({ "static-access", "unchecked" })
	public JSONObject changeUserPassword(String userName, String oldPassword, String newPassword,
			String confirmPassword, final String secretKey) {
		JSONObject userJson = new JSONObject();
		String encryptedNewPassword = null, decryptedPassword = null;
		logger.info("Inside changeUserPassword method -> userName - " + userName + " oldPassword -" + oldPassword
				+ " newPassword " + newPassword);
		UserManagementEntity userDetails = userManagementRepository.findOneByUserName(userName);
		
		decryptedPassword = getDecryptPassword(userName, oldPassword, secretKey);

		if (decryptedPassword != null && decryptedPassword.equals(oldPassword) && newPassword.equals(confirmPassword)) {
			encryptedNewPassword = userEncryption.encryptPassword(newPassword, secretKey);
			userDetails.setCurrentPassword(encryptedNewPassword);
			userDetails.setPasswordUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
			UserManagementEntity userInfo = userManagementRepository.save(userDetails);
			if (userInfo != null) {
				userJson.put("status", "Success");
				userJson.put("description", errorValidationRepository.findByErrorId("C3P_UM_011"));
			}
		} else if (!newPassword.equals(confirmPassword) && decryptedPassword != null && decryptedPassword.equals(oldPassword)) {
			userJson.put("status", "Error");
			userJson.put("description", errorValidationRepository.findByErrorId("C3P_UM_012"));
		} else {
			userJson.put("status", "Error");
			userJson.put("description", errorValidationRepository.findByErrorId("C3P_UM_010"));
		}
		return userJson;
	}

	private boolean isValidMD5(String decryptedPass) {
		return decryptedPass.matches("^[a-fA-F0-9]{32}$");
	}

	@SuppressWarnings("static-access")
	private String getDecryptPassword(String userName, String password, String secretKey) {
		String decryptPassword = null, decryptMD5Password = null;
		String decryptedPass = userManagementRepository.findOneByCurrentPassword(userName);
		decryptMD5Password = passEncrypt.getMd5(password);

		if (isValidMD5(decryptedPass) && decryptMD5Password.equals(decryptedPass))
			decryptPassword = password;
		else
			decryptPassword = userEncryption.decryptPassword(decryptedPass, secretKey);
		return decryptPassword;
	}

	@SuppressWarnings("static-access")
	private String getEncryptPassword(String userName, String password, String secretKey) {
		String encryptPassword = null, encryptMD5Password = null;
		String currentPass = userManagementRepository.findOneByCurrentPassword(userName);
		encryptMD5Password = passEncrypt.getMd5(password);

		if (isValidMD5(encryptMD5Password) && encryptMD5Password.equals(currentPass))
			encryptPassword = encryptMD5Password;
		else
			encryptPassword = userEncryption.encryptPassword(password, secretKey);
		return encryptPassword;
	}
}
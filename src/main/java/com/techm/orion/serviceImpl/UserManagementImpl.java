package com.techm.orion.serviceImpl;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TimeZone;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.techm.orion.entitybeans.PasswordPolicy;
import com.techm.orion.entitybeans.SiteInfoEntity;
import com.techm.orion.entitybeans.UserManagementEntity;
import com.techm.orion.entitybeans.UserRole;
import com.techm.orion.exception.GenericResponse;
import com.techm.orion.repositories.ModuleRepository;
import com.techm.orion.repositories.SiteInfoRepository;
import com.techm.orion.repositories.UserManagementRepository;
import com.techm.orion.repositories.WorkGroupRepository;
import com.techm.orion.service.UserManagementInterface;
import com.techm.orion.utility.PasswordEncryption;

@Service
public class UserManagementImpl implements UserManagementInterface {
	private static final Logger logger = LogManager.getLogger(UserManagementImpl.class);
	private static final int MAX_ATTEMPTS = 3;

	@Autowired
	private UserManagementRepository userManagementRepository;

	@Autowired
	private WorkGroupRepository workGroupRepository;

	@Autowired
	private ModuleRepository moduleRepository;

	@Autowired
	private SiteInfoRepository siteInfoRepository;

	/*
	 * @Autowired private PrivilegeLevelRepository privilegeLevelRepository;
	 */

	@Autowired
	private UserManagementEntity userManagementEntity;

	@Autowired
	private PasswordEncryption passEncrypt;

	/*
	 * Create service for create user for user management
	 */
	@Override
	public GenericResponse createUser(String userData) throws Exception {
		GenericResponse res = new GenericResponse();

		logger.info("\n" + "Inside createUser Service");
		logger.info("\n" + "data=" + userData);
		JSONParser parser = new JSONParser();
		JSONObject module = new JSONObject();
		JSONObject deviceGroup = new JSONObject();
		JSONObject customerAndSites = null;
		JSONObject workGroup = new JSONObject();
		String userName = "", role = "", firstName = "", lastName = "", email = "", password = "", timezone = "",
				status = "", manager = "", devicegroup = "", customersites = "", subordinate = "", userId = null,
				message = null, workgroupname = null, modulename = null, workGroupName = null, encryptedPass = null;
		long phone = 0, mobile = 0, id = 0;
		JSONArray toSaveArray = new JSONArray();
		String testsSelected = toSaveArray.toString();
		List<UserManagementEntity> userManager = null;
		UserManagementEntity userCreate2 = null;

		try {
			JSONObject json = (JSONObject) parser.parse(userData);
			customerAndSites = new JSONObject();
			/*
			 * Logic for enoc_user when no work group is selected if workgroup
			 * json will be empty It will work when
			 * "workGroup":{"workgroupname":""},
			 */
			workGroup = (JSONObject) json.get("workGroup");
			if (workGroup.get("workGroupName").toString().isEmpty()) {
				if (json.get("role").equals("enoc_user"))
					workGroupName = "ENOC_USERS_ALL";
				else if (json.get("role").equals("seuser"))
					workGroupName = "SEUSER_ALL";
				else if (json.get("role").equals("suser"))
					workGroupName = "SUSER_ALL";
				else if (json.get("role").equals("feuser"))
					workGroupName = "FEUSER_ALL";
				else if (json.get("role").toString().equalsIgnoreCase("admin"))
					workGroupName = "ADMIN_ALL";
			} else
				workGroup = (JSONObject) json.get("workGroup");

			module = (JSONObject) json.get("module");
			role = (String) json.get("role");
			userName = (String) json.get("userName");
			firstName = (String) json.get("firstName");
			lastName = (String) json.get("lastName");
			email = (String) json.get("email");
			phone = (long) json.get("phone");
			mobile = (long) json.get("mobile");
			password = (String) json.get("password");
			encryptedPass = passEncrypt.getMd5(password);
			timezone = (String) json.get("timezone");
			status = (String) json.get("status");
			manager = (String) json.get("manager");
			userId = (String) json.get("id");
			deviceGroup = (JSONObject) json.get("deviceGroup");
			customerAndSites = (JSONObject) json.get("customerAndSites");

			userManagementEntity.setRole(role);
			userManagementEntity.setId(id);
			userManagementEntity.setFirstName(firstName);
			userManagementEntity.setLastName(lastName);
			userManagementEntity.setEmail(email);
			userManagementEntity.setCurrentPassword(passEncrypt.getMd5(password));
			userManagementEntity.setPhone(phone);
			userManagementEntity.setMobile(mobile);
			userManagementEntity.setManagerName(manager);
			userManagementEntity.setTimeZone(TimeZone.getDefault());
			userManagementEntity.setStatus(status);
			userManagementEntity.setUserName(userName);
			userManagementEntity.setModuleInfo(module.toString());
			userManagementEntity.setDeviceGroup(deviceGroup.toString());
			if (role.equals("enoc_user"))
				userManagementEntity.setCustomerSites(customerAndSites.toString());
			else
				userManagementEntity.setCustomerSites(null);

			// It will work when "workGroup":{"workgroupname":""},
			if (workGroup.get("workGroupName").toString().isEmpty())
				userManagementEntity.setWorkGroup(workGroupName);
			else
				userManagementEntity.setWorkGroup(workGroup.toString());

			String isUserExist = userManagementRepository.isUserNameExist(userName);
			userManager = userManagementRepository.findOneByUserName(manager);
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
	@Override
	public GenericResponse getUserView(String userName) throws Exception {
		GenericResponse response = new GenericResponse();
		JSONParser parser = new JSONParser();
		JSONObject jsonModule = null, jsonCustAndSites = null;
		List<UserManagementEntity> userDetails = userManagementRepository.findUserDetails(userName);
		UserManagementEntity userInfo = userDetails.get(0);

		// Get moduleInfo and CustomerAndSites details
		String moduleDetails = userInfo.getModuleInfo();
		String customerAndSitesDetails = userInfo.getCustomerSites();
		if (moduleDetails != null)
			jsonModule = (JSONObject) parser.parse(moduleDetails);
		if (customerAndSitesDetails != null)
			jsonCustAndSites = (JSONObject) parser.parse(customerAndSitesDetails);

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
		List<SiteInfoEntity> sitesDetails = siteInfoRepository.findSitesDetails(custName, regionName);
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
		JSONObject customerAndSites = new JSONObject();
		JSONObject workGroup = new JSONObject();
		String userName = "", role = "", firstName = "", lastName = "", email = "", password = "", timezone = "",
				status = "", manager = "", devicegroup = "", customersites = "", subordinate = "", message = null,
				workgroupname = null, modulename = null, workGroupName = null, assignManager = null,
				encryptedPass = null;
		long userId = 0;
		long phone = 0, mobile = 0, id = 0;
		JSONArray toSaveArray = new JSONArray();
		String testsSelected = toSaveArray.toString();

		try {
			JSONObject json = (JSONObject) parser.parse(userData);

			workGroup = (JSONObject) json.get("workGroup");
			module = (JSONObject) json.get("module");
			role = (String) json.get("role");
			userName = (String) json.get("userName");
			firstName = (String) json.get("firstName");
			lastName = (String) json.get("lastName");
			email = (String) json.get("email");
			phone = (long) json.get("phone");
			mobile = (long) json.get("mobile");
			password = (String) json.get("password");
			encryptedPass = passEncrypt.getMd5(password);
			timezone = (String) json.get("timezone");
			status = (String) json.get("status");
			manager = (String) json.get("manager");
			assignManager = (String) json.get("assignManager");
			userId = (long) json.get("id");
			deviceGroup = (JSONObject) json.get("deviceGroup");
			customerAndSites = (JSONObject) json.get("customerAndSites");

			UserManagementEntity userDetails = userManagementRepository.findById(userId);

			if (!role.isEmpty())
				userDetails.setRole(role);

			if (!firstName.isEmpty())
				userDetails.setFirstName(firstName);

			if (!lastName.isEmpty())
				userDetails.setLastName(lastName);

			if (!email.isEmpty())
				userDetails.setEmail(email);

			if (!password.isEmpty())
				userDetails.setCurrentPassword(encryptedPass);

			if (phone != 0)
				userDetails.setPhone(phone);

			if (mobile != 0)
				userDetails.setMobile(mobile);

			if (!manager.isEmpty())
				userDetails.setManagerName(manager);

			/*
			 * if(!userId.isEmpty())
			 * userCreate.setTimeZone(TimeZone.getDefault());
			 */

			if (!status.isEmpty())
				userDetails.setStatus(status);

			if (!userName.isEmpty())
				// userDetails.setUserName(userName);

				if (manager.isEmpty() && !assignManager.isEmpty())
					userDetails.setManagerName(assignManager);

			if (!module.isEmpty())
				userDetails.setModuleInfo(module.toString());

			if (!deviceGroup.isEmpty())
				userDetails.setDeviceGroup(deviceGroup.toString());

			if (!customerAndSites.get("customerName").toString().isEmpty() && role.equals("enoc_user"))
				userDetails.setCustomerSites(customerAndSites.toString());
			else
				userDetails.setCustomerSites(null);

			if (!workGroup.get("workGroupName").toString().isEmpty())
				userDetails.setWorkGroup(workGroup.toString());

			if (!password.isEmpty()) {
				String currentPass = userManagementRepository.findOneByCurrentPassword(userName);
				String previousPass = userManagementRepository.findOneByPreviousPassword(userName);
				String lastPreviousPass = userManagementRepository.findOneByLastPreviousPassword(userName);
				if (encryptedPass.equalsIgnoreCase(currentPass) || encryptedPass.equalsIgnoreCase(previousPass)
						|| encryptedPass.equalsIgnoreCase(lastPreviousPass))
					throw new Exception("old password not accepted ");

				List<UserManagementEntity> userManagers = userManagementRepository.findByUserName(userName);
				UserManagementEntity u1 = userManagers.get(0);
				String pass = u1.getCurrentPassword();
				u1.setPreviousPassword(currentPass);
				if (previousPass != null)
					u1.setLastPreviousPassword(previousPass);
			}

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
	public GenericResponse checkUserNamePassword(String userName, String currentPassword) throws Exception {
		String encryptedPass = passEncrypt.getMd5(currentPassword);
		String userPass = userManagementRepository.findByUserNameCurrentPassword(userName, encryptedPass);
		GenericResponse responseData = new GenericResponse();
		UserManagementEntity userDetails = getUserAttempts(userName);
		String user = null;
		String pass = null;
		if (userPass != null) {
			user = userPass.split(",")[0];
			pass = userPass.split(",")[1];
		}

		if (userName != null && !userName.isEmpty() && currentPassword != null && !currentPassword.isEmpty()) {

			if (userName.equalsIgnoreCase(user) && encryptedPass.equalsIgnoreCase(pass)) {
				if (userDetails.getAttempts() >= MAX_ATTEMPTS)
					throw new Exception("User account is locked!");
				logger.info("\n" + "correct password");
				responseData.put("msg", "correct password");
				userManagementRepository.resetFailAttempts(userName);
			} else {

				logger.info("\n" + "wrong password");
				responseData.put("msg", "wrong password");
				if (userDetails.getAttempts() >= MAX_ATTEMPTS)
					throw new Exception("User account is locked!");
				updateFailAttempts(userName);
				throw new Exception("Invalid username or password");
			}
		}
		return responseData;
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

	@Override
	public int lockAndUnlockUser(String userName, String action) {
		int success = 0;
		if (!userName.isEmpty()) {
			List<UserManagementEntity> userDetails = userManagementRepository.findByUserName(userName);
			if (!userDetails.isEmpty())
				success = userManagementRepository.activeUser(action, userName);
		}
		return success;
	}
}

package com.techm.c3p.core.repositories;
/*

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.techm.c3p.core.entitybeans.RequestDetailsEntity;

JPA Repository to store data from uploaded file into database
@Repository
public interface RequestDetailsImportRepo extends JpaRepository<RequestDetailsEntity, Long> {

	 Query for wild card search based on region 
	String searchRegion = "SELECT * FROM requestinfoso e WHERE e.region LIKE %?1%";

	@Query(value = searchRegion, nativeQuery = true)
	List<RequestDetailsEntity> findByRegion(String region);

	 Query for wild card search based on Import ID 
	String searchImportId = "SELECT * FROM requestinfoso e WHERE e.importid LIKE %?1%";

	@Query(value = searchImportId, nativeQuery = true)
	List<RequestDetailsEntity> findByImportid(String importid);

	 Query for wild card search based on Vendor 
	String searchVendor = "SELECT * FROM requestinfoso e WHERE e.vendor LIKE %?1%";

	@Query(value = searchVendor, nativeQuery = true)
	List<RequestDetailsEntity> findByVendor(String vendor);

	 Query for wild card search based on Model 
	String searchModel = "SELECT * FROM requestinfoso e WHERE e.model LIKE %?1%";

	@Query(value = searchModel, nativeQuery = true)
	List<RequestDetailsEntity> findByModel(String model);

	 Query for wild card search based on Status 
	String searchStaus = "SELECT * FROM requestinfoso e WHERE e.import_status LIKE %?1%";

	@Query(value = searchStaus, nativeQuery = true)
	List<RequestDetailsEntity> findByImportStatus(String status);

	 Query for wild card search based on Management IP 
	String searchManagementIp = "SELECT * FROM requestinfoso e WHERE e.managementIp LIKE %?1%";

	@Query(value = searchManagementIp, nativeQuery = true)
	List<RequestDetailsEntity> findByManagementIp(String value);

	 Native query to update first successful import request status into DB 
	String importUpdate = "update requestinfoso u set u.import_status = ?1 where u.request_info_id = ?2";

	@Query(value = importUpdate, nativeQuery = true)
	@Modifying
	@Transactional
	void updateImportStatus(String importStatus, int id);

	String templateUpdate = "update requestinfoso u set u.TemplateIdUsed = ?1 where u.request_info_id = ?2";

	@Query(value = templateUpdate, nativeQuery = true)
	@Modifying
	@Transactional
	void updateSuggestedTemplateId(String suggestedTemplateId, int id);

	List<RequestDetailsEntity> findByHostname(String hostName);

	List<RequestDetailsEntity> findByHostnameAndManagementIp(String hostName, String managementIp);

	List<RequestDetailsEntity> findByRequestinfoid(int requestinfoid);

	 Query for wild card search based on Request Id 
	String searchRequestId = "SELECT * FROM requestinfoso e WHERE e.alphanumeric_req_id LIKE %?1%";

	@Query(value = searchRequestId, nativeQuery = true)
	List<RequestDetailsEntity> findByAlphanumericReqId(String value);

	 Query for wild card search based on Request Id 
	String searchDeviceType = "SELECT * FROM requestinfoso e WHERE e.device_type LIKE %?1%";

	@Query(value = searchDeviceType, nativeQuery = true)
	List<RequestDetailsEntity> findByDeviceType(String value);

}*/
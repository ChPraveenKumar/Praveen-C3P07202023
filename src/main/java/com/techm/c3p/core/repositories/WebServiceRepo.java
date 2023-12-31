package com.techm.c3p.core.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techm.c3p.core.entitybeans.WebServiceEntity;

/*JPA Repository to store data from uploaded file into database*/
@Repository
public interface WebServiceRepo extends JpaRepository<WebServiceEntity, Long> {

	/*
	 * Methods to manipulate data based on requestinfoid
	 */
	WebServiceEntity findByRequestInfoId(int request_id);
	
	WebServiceEntity findByAlphanumericReqId(String alphanumericReqId);
	
	WebServiceEntity findTextFoundDeliveryTestByAlphanumericReqIdAndVersion(String requestid, Double version);

	WebServiceEntity findByAlphanumericReqIdAndVersion(String requestId, Double version);
	
	

}
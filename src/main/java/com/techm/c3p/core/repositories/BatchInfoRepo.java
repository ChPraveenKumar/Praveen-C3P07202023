package com.techm.c3p.core.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techm.c3p.core.entitybeans.BatchIdEntity;

@Repository
public interface BatchInfoRepo extends JpaRepository<BatchIdEntity, Integer> {

	List<BatchIdEntity> findBatchStatusByBatchId(String batchId);

	/*
	 * Native query to update first successful import request status into DB String
	 * batchStatus = "update c3p_t_request_batch_info u set u.r_batch_status = ?1";
	 * 
	 * @Query(value = batchStatus, nativeQuery = true)
	 * 
	 * @Modifying
	 * 
	 * @Transactional void updateBatchStatus(String batchStatus);
	 */

	/* Dhanshri Ravsaheb Mane : Added Query For RequestDashboard*/
	@Query(value = "select distinct(r_batch_status) from c3p_t_request_batch_info  where r_batch_id= :batchId", nativeQuery = true)
	String getBatchStatus(@Param("batchId") String batchId);

}
package com.techm.orion.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techm.orion.entitybeans.InternetInfoEntity;

/*JPA Repository to store data from uploaded file into database*/
@Repository
public interface InternetInfoRepo extends
		JpaRepository<InternetInfoEntity, Long> {
	/*
	 * Methods to manipulate data based on requestinfoid
	 */
	InternetInfoEntity findByRequestInfoId(int request_info_id);

}
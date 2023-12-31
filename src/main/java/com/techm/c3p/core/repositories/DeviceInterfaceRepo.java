package com.techm.c3p.core.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techm.c3p.core.entitybeans.DeviceInterfaceEntity;

/*JPA Repository to store data from uploaded file into database*/
@Repository
public interface DeviceInterfaceRepo extends
		JpaRepository<DeviceInterfaceEntity, Long> {

	/*
	 * Method to manipulate data based on requestinfoid
	 */
	DeviceInterfaceEntity findByRequestInfoId(int request_info_id);

}
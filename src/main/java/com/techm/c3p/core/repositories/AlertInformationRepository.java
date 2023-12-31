package com.techm.c3p.core.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techm.c3p.core.entitybeans.AlertInformation;

@Repository
public interface AlertInformationRepository extends JpaRepository<AlertInformation, Integer>{
	
	List<AlertInformation> findById(int id);

	 List<AlertInformation> findByAlertcode(String alertcode);
	 
	 List<AlertInformation> findByAlertdescription(String alertdescription);
	 
	 List<AlertInformation> findByAlertcodeAndAlertdescription(String alertcode, String alertdescription);
	
	
	 
	

}

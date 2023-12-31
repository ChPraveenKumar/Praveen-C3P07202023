package com.techm.c3p.core.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techm.c3p.core.entitybeans.ErrorValidationEntity;


@Repository
public interface ErrorValidationRepository extends JpaRepository<ErrorValidationEntity, Long>{
	
	List<ErrorValidationEntity> findById(int id);

	 List<ErrorValidationEntity> findByCategory(String category);
	 
	 @Query(value = "select suggestion from errorcodedata  where errorId= :errorId ", nativeQuery = true)
	 String findByErrorId(@Param("errorId") String errorId);
	 
	 @Query(value = "select ErrorDescription from errorcodedata  where errorId= :errorId and ErrorType= :ErrorType", nativeQuery = true)
	 String findDescriptionByErrorIdandErrorType(@Param("errorId") String errorId, @Param("ErrorType") String ErrorType);

	 @Query(value = "select suggestion from errorcodedata  where errorDescription= :errorDescription ", nativeQuery = true)
	 ErrorValidationEntity findByErrorDescription(@Param("errorDescription") String errorDescription);
	 
	 @Query(value = "select ErrorType, ErrorDescription, router_error_message from errorcodedata where router_error_message is not null ", nativeQuery = true)
	 List<String> findByErrorMessageIsNull();
}

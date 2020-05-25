package com.techm.orion.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techm.orion.entitybeans.CategoryMasterEntity;

@Repository
public interface CategoryMasterDao extends
		JpaRepository<CategoryMasterEntity, Long> {

	public List<CategoryMasterEntity> findAll();

	public CategoryMasterEntity findByCategoryName(String value);

}